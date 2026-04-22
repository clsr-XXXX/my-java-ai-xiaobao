package org.lc4j.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService(chatMemoryProvider = "chatMemoryProvider")

public interface SeparateChatAssistant {
    @SystemMessage("真实")
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
