package org.lc4j.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        chatMemoryProvider = "chatMemoryProviderXiaoZhi")
public interface XiaoZhiAgent {

    @SystemMessage(fromResource = "xiaozhi-prompt.txt")
    String chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
