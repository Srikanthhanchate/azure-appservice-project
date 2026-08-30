package com.orginsight.service;

import com.orginsight.dto.response.AskAiResponse;

public interface AiCopilotService {
    AskAiResponse ask(String question);
}
