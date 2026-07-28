package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * 답변 사진을 (imageKey → 렌더용 바이트) 맵으로 미리 준비한다.
 * 다운로드는 전용 스레드가 앞서 돌리고 디코드는 호출 스레드가 입력 순서대로 한 장씩 하며,
 * 창 크기가 동시에 들고 있는 원본 장수를 묶는다. 한 장이 실패하면 onSkip 을 부르고 그 키만 맵에서 빠진다.
 */
public final class PdfPhotoPrefetcher {

    private PdfPhotoPrefetcher() {
    }

    /**
     * imageKeys = 준비할 사진 키(null·중복 제거, 순서 유지), download = 키 하나의 원본 바이트(실패 시 예외),
     * decode = 원본을 렌더용 바이트로(실패 시 예외), onSkip = 그 사진을 건너뛸 때 호출(로그는 호출 측 책임),
     * parallelism = 다운로드 스레드 수, windowSize = 동시에 들고 있을 원본 장수(스레드 수보다 작아지지 않는다).
     */
    public static Map<String, byte[]> prefetch(List<String> imageKeys,
                                               Function<String, byte[]> download,
                                               UnaryOperator<byte[]> decode,
                                               BiConsumer<String, RuntimeException> onSkip,
                                               int parallelism,
                                               int windowSize) {
        List<String> keys = imageKeys.stream().filter(Objects::nonNull).distinct().toList();
        Map<String, byte[]> resolved = new LinkedHashMap<>();
        if (keys.isEmpty()) {
            return resolved;
        }
        int threads = Math.max(1, parallelism);
        prefetchWindowed(keys, download, decode, onSkip, threads, Math.max(threads, windowSize), resolved);
        return resolved;
    }

    /** 앞으로 window 장까지 다운로드를 걸어두고 제출한 순서대로 꺼내 디코드한다. 맵은 호출 스레드만 건드린다. */
    private static void prefetchWindowed(List<String> keys,
                                         Function<String, byte[]> download,
                                         UnaryOperator<byte[]> decode,
                                         BiConsumer<String, RuntimeException> onSkip,
                                         int parallelism, int window,
                                         Map<String, byte[]> resolved) {
        ExecutorService pool = Executors.newFixedThreadPool(parallelism, threadFactory());
        try {
            Deque<Pending> inFlight = new ArrayDeque<>(window);
            int submitted = 0;
            while (true) {
                while (inFlight.size() < window && submitted < keys.size()) {
                    String key = keys.get(submitted++);
                    inFlight.addLast(new Pending(key, pool.submit(() -> download.apply(key))));
                }
                Pending pending = inFlight.pollFirst();
                if (pending == null) {
                    break;   // 남은 제출도 대기 중인 다운로드도 없음 = 전부 처리됨
                }
                decodeInto(pending.key(), awaitSource(pending, onSkip), decode, onSkip, resolved);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /** 다운로드 결과를 기다린다. 그 사진만의 실패면 null 을 돌려 건너뛰고, 그 밖의 오류는 위로 던져 job 을 실패시킨다. */
    private static byte[] awaitSource(Pending pending, BiConsumer<String, RuntimeException> onSkip) {
        try {
            return pending.future().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("답변 사진 준비 중단", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                onSkip.accept(pending.key(), runtime);
                return null;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("답변 사진 준비 실패", cause);
        }
    }

    /** source 가 null 이면(=다운로드 실패) 아무것도 담지 않는다. 디코드 실패도 그 사진만 건너뛴다. */
    private static void decodeInto(String key, byte[] source, UnaryOperator<byte[]> decode,
                                   BiConsumer<String, RuntimeException> onSkip, Map<String, byte[]> resolved) {
        if (source == null) {
            return;
        }
        try {
            resolved.put(key, decode.apply(source));
        } catch (RuntimeException e) {
            onSkip.accept(key, e);
        }
    }

    /** 스레드 덤프에서 사진 다운로드 구간이 바로 보이도록 이름을 단다. 렌더가 끝나면 같이 사라지도록 데몬. */
    private static ThreadFactory threadFactory() {
        AtomicInteger sequence = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, "pdf-photo-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    /** 아직 디코드하지 않은 다운로드 한 건(키 + 진행 중인 다운로드). */
    private record Pending(String key, Future<byte[]> future) {
    }
}