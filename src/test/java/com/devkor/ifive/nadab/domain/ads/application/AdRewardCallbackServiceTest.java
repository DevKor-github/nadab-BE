package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.infra.AdRewardVerifyResult;
import com.devkor.ifive.nadab.domain.ads.infra.AdmobSignatureVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SSV 콜백 오케스트레이션의 분기 검증(200=false / 5xx=true, 검증→지급 위임).
 * 검증기·지급 서비스는 mock — 여기서 보는 건 handle의 분기와 재시도 판단이다.
 */
class AdRewardCallbackServiceTest {

    private static final String SESSION_KEY = "sk-1";
    private static final String TX_ID = "tx-1";

    private final AdmobSignatureVerifier verifier = mock(AdmobSignatureVerifier.class);
    private final AdRewardGrantService grantService = mock(AdRewardGrantService.class);
    private final AdRewardCallbackService service = new AdRewardCallbackService(verifier, grantService);

    @Test
    void probe_콜백은_검증없이_200() {
        // custom_data 없음(콘솔 Verify probe)
        boolean retry = service.handle(request(null, TX_ID, "sig", "1"));

        assertThat(retry).isFalse();
        verify(verifier, never()).verify(any());
        verify(grantService, never()).process(anyString(), any(), anyBoolean());
        verify(grantService, never()).logTransientError(anyString(), any());
    }

    @Test
    void 서명메타_없으면_검증없이_200() {
        boolean retry = service.handle(request(SESSION_KEY, TX_ID, null, "1")); // signature 없음

        assertThat(retry).isFalse();
        verify(verifier, never()).verify(any());
    }

    @Test
    void 일시오류면_로그_남기고_5xx() {
        when(verifier.verify(any())).thenReturn(AdRewardVerifyResult.TRANSIENT_ERROR);

        boolean retry = service.handle(request(SESSION_KEY, TX_ID, "sig", "1"));

        assertThat(retry).isTrue();
        verify(grantService).logTransientError(SESSION_KEY, TX_ID);
        verify(grantService, never()).process(anyString(), any(), anyBoolean());
    }

    @Test
    void 일시오류_로그저장_실패해도_5xx() {
        when(verifier.verify(any())).thenReturn(AdRewardVerifyResult.TRANSIENT_ERROR);
        doThrow(new RuntimeException("db down")).when(grantService).logTransientError(anyString(), any());

        boolean retry = service.handle(request(SESSION_KEY, TX_ID, "sig", "1"));

        assertThat(retry).isTrue();
    }

    @Test
    void 유효서명이면_지급처리를_true로_위임하고_200() {
        when(verifier.verify(any())).thenReturn(AdRewardVerifyResult.VALID);

        boolean retry = service.handle(request(SESSION_KEY, TX_ID, "sig", "1"));

        assertThat(retry).isFalse();
        verify(grantService).process(SESSION_KEY, TX_ID, true);
    }

    @Test
    void 무효서명이면_지급처리를_false로_위임하고_200() {
        when(verifier.verify(any())).thenReturn(AdRewardVerifyResult.INVALID_SIGNATURE);

        boolean retry = service.handle(request(SESSION_KEY, TX_ID, "sig", "1"));

        assertThat(retry).isFalse();
        verify(grantService).process(SESSION_KEY, TX_ID, false);
    }

    @Test
    void 지급처리중_예외면_5xx() {
        when(verifier.verify(any())).thenReturn(AdRewardVerifyResult.VALID);
        when(grantService.process(anyString(), any(), anyBoolean())).thenThrow(new RuntimeException("db down"));

        boolean retry = service.handle(request(SESSION_KEY, TX_ID, "sig", "1"));

        assertThat(retry).isTrue();
    }

    private MockHttpServletRequest request(String customData, String transactionId, String signature, String keyId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (customData != null) {
            request.setParameter("custom_data", customData);
        }
        if (transactionId != null) {
            request.setParameter("transaction_id", transactionId);
        }
        if (signature != null) {
            request.setParameter("signature", signature);
        }
        if (keyId != null) {
            request.setParameter("key_id", keyId);
        }
        return request;
    }
}