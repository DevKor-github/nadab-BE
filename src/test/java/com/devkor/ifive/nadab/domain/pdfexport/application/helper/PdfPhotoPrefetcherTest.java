package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 창 방식 프리페치 계약 검증. 창 루프의 경계 오류가 곧 "사진 몇 장 누락"으로 나타날 수도 있다.
 */
class PdfPhotoPrefetcherTest {

    private static byte[] source(String key) {
        return ("src:" + key).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] decoded(String key) {
        return ("dec:" + key).getBytes(StandardCharsets.UTF_8);
    }

    /** 원본 바이트를 그대로 "디코드"한 것처럼 바꿔주는 함수(내용 검증용). */
    private static byte[] fakeDecode(byte[] source) {
        return new String(source, StandardCharsets.UTF_8).replace("src:", "dec:").getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void 창보다_키가_많아도_전부_준비되고_순서가_유지된다() {
        List<String> keys = IntStream.range(0, 25).mapToObj(i -> "k" + i).toList();

        Map<String, byte[]> resolved = PdfPhotoPrefetcher.prefetch(keys,
                PdfPhotoPrefetcherTest::source, PdfPhotoPrefetcherTest::fakeDecode,
                (key, e) -> { }, 3, 4);

        assertThat(resolved).hasSize(25);
        assertThat(resolved.keySet()).containsExactlyElementsOf(keys);
        assertThat(resolved.get("k0")).isEqualTo(decoded("k0"));
        assertThat(resolved.get("k24")).isEqualTo(decoded("k24"));
    }

    @Test
    void 다운로드_한장_실패는_그_사진만_빠지고_나머지는_준비된다() {
        List<String> keys = List.of("ok1", "broken", "ok2");
        List<String> skipped = new ArrayList<>();

        Map<String, byte[]> resolved = PdfPhotoPrefetcher.prefetch(keys,
                key -> {
                    if ("broken".equals(key)) {
                        throw new IllegalStateException("object not found");
                    }
                    return source(key);
                },
                PdfPhotoPrefetcherTest::fakeDecode,
                (key, e) -> skipped.add(key), 2, 2);

        assertThat(resolved).containsOnlyKeys("ok1", "ok2");
        assertThat(skipped).containsExactly("broken");
    }

    @Test
    void 디코드_한장_실패도_그_사진만_빠진다() {
        List<String> keys = List.of("ok1", "broken", "ok2");
        List<String> skipped = new ArrayList<>();

        Map<String, byte[]> resolved = PdfPhotoPrefetcher.prefetch(keys,
                PdfPhotoPrefetcherTest::source,
                source -> {
                    if (new String(source, StandardCharsets.UTF_8).contains("broken")) {
                        throw new IllegalStateException("디코드 실패");
                    }
                    return fakeDecode(source);
                },
                (key, e) -> skipped.add(key), 2, 3);

        assertThat(resolved).containsOnlyKeys("ok1", "ok2");
        assertThat(skipped).containsExactly("broken");
    }

    @Test
    void 동시에_들고_있는_원본이_창_크기를_넘지_않는다() {
        List<String> keys = IntStream.range(0, 30).mapToObj(i -> "k" + i).toList();
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger maxInFlight = new AtomicInteger();

        PdfPhotoPrefetcher.prefetch(keys,
                key -> {
                    // 다운로드 완료 시점부터 디코드가 가져갈 때까지가 "들고 있는" 구간이다.
                    maxInFlight.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
                    return source(key);
                },
                source -> {
                    inFlight.decrementAndGet();
                    return fakeDecode(source);
                },
                (key, e) -> { }, 3, 4);

        // 제출 자체가 창으로 묶이므로 다운로드 스레드가 몇 개든 동시 보유는 창을 넘지 않는다.
        assertThat(maxInFlight.get()).isLessThanOrEqualTo(4);
    }

    @Test
    void null_과_중복_키는_걸러진다() {
        List<String> keys = new ArrayList<>(List.of("a", "b", "a"));
        keys.add(null);
        AtomicInteger downloads = new AtomicInteger();

        Map<String, byte[]> resolved = PdfPhotoPrefetcher.prefetch(Collections.unmodifiableList(keys),
                key -> {
                    downloads.incrementAndGet();
                    return source(key);
                },
                PdfPhotoPrefetcherTest::fakeDecode, (key, e) -> { }, 2, 2);

        assertThat(resolved).containsOnlyKeys("a", "b");
        assertThat(downloads).hasValue(2);
    }

    @Test
    void 예상하지_못한_오류는_건너뛰지_않고_위로_던진다() {
        List<String> keys = List.of("k0", "k1");

        assertThatThrownBy(() -> PdfPhotoPrefetcher.prefetch(keys,
                key -> { throw new OutOfMemoryError("heap"); },
                PdfPhotoPrefetcherTest::fakeDecode, (key, e) -> { }, 2, 2))
                .isInstanceOf(OutOfMemoryError.class);
    }
}