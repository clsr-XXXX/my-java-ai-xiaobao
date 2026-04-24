package org.lc4j.assistant;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        chatModel = "qwenChatModel",  // 用 DashScope 自动创建的 bean
        chatMemoryProvider = "chatMemoryProviderXiaoBao",
        tools = "appointmentTools",
        contentRetriever = "contentRetrieverXiaoBaoPincone")

public interface XiaoBaoAgent {

    @SystemMessage(fromResource = "xiaobao-prompt.txt")
    String chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
