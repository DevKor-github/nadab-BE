package com.devkor.ifive.nadab.domain.ads.infra;

import com.google.crypto.tink.apps.rewardedads.RewardedAdsVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdmobSignatureVerifierTest {

    private final RewardedAdsVerifier tinkVerifier = mock(RewardedAdsVerifier.class);
    private final AdmobSignatureVerifier verifier = new AdmobSignatureVerifier(tinkVerifier);

    @Test
    void 서명이_유효하면_VALID() throws Exception {
        doNothing().when(tinkVerifier).verify(anyString());

        AdRewardVerifyResult result = verifier.verify(request("a=1&b=2&signature=sig&key_id=1"));

        assertThat(result).isEqualTo(AdRewardVerifyResult.VALID);
    }

    @Test
    void 원본_쿼리스트링_그대로_Tink에_전달() throws Exception {
        doNothing().when(tinkVerifier).verify(anyString());

        verifier.verify(request("ad_network=1&custom_data=abc&signature=sig&key_id=1"));

        verify(tinkVerifier).verify(
                eq("http://localhost/api/v1/ad-rewards/ssv?ad_network=1&custom_data=abc&signature=sig&key_id=1"));
    }

    @Test
    void 서명_확정무효면_INVALID_SIGNATURE() throws Exception {
        doThrow(new GeneralSecurityException("invalid ecdsa signature"))
                .when(tinkVerifier).verify(anyString());

        AdRewardVerifyResult result = verifier.verify(request("a=1&signature=bad&key_id=1"));

        assertThat(result).isEqualTo(AdRewardVerifyResult.INVALID_SIGNATURE);
    }

    @Test
    void 키fetch_등_일시오류면_TRANSIENT_ERROR() throws Exception {
        doThrow(new GeneralSecurityException("failed to fetch keys", new IOException("connect timeout")))
                .when(tinkVerifier).verify(anyString());

        AdRewardVerifyResult result = verifier.verify(request("a=1&signature=sig&key_id=1"));

        assertThat(result).isEqualTo(AdRewardVerifyResult.TRANSIENT_ERROR);
    }

    private MockHttpServletRequest request(String queryString) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/ad-rewards/ssv");
        request.setQueryString(queryString);
        return request;
    }
}