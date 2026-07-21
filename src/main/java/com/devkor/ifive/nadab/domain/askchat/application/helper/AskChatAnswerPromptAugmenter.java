package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPrompt;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;

public interface AskChatAnswerPromptAugmenter {

    AskChatAnswerPrompt augment(AskChatAnswerPromptContext context);
}
