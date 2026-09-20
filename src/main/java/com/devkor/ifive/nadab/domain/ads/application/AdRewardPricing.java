package com.devkor.ifive.nadab.domain.ads.application;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatWalletChargeService;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import org.springframework.stereotype.Component;

// 기능별 필요 크리스탈. 가격은 여기서 새로 정의하지 않고 기존 소스에 위임(중복정의 방지).
@Component
public class AdRewardPricing {

    public long crystalCost(AdRewardFeature feature) {
        return switch (feature) {
            case PDF_REPORT_ONLY -> PdfExportType.REPORT_ONLY.getCrystalCost();
            case PDF_ANSWER_ONLY -> PdfExportType.ANSWER_ONLY.getCrystalCost();
            case PDF_REPORT_AND_ANSWER -> PdfExportType.REPORT_AND_ANSWER.getCrystalCost();
            case ASK_CHAT_TURN_CHARGE -> AskChatWalletChargeService.ASK_CHAT_TURN_CHARGE_CRYSTAL_COST;
        };
    }
}