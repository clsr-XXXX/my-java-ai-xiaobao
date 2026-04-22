package org.lc4j.assistant;
import dev.langchain4j.service.spring.AiService;

@AiService(
        chatMemory = "chatMemory"
)
public interface Assistant {
    String chat(String message);
}
