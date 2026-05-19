package org.lc4j.assistant;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "qwenChatModel")
public interface AssistantTemp {
        public String chat(String userMessage);

}
