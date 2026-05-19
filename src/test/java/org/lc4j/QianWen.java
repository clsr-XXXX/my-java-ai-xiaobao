package org.lc4j;

import lombok.Value;
import org.junit.jupiter.api.Test;
import org.lc4j.assistant.AssistantTemp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class QianWen {
    @Autowired
    AssistantTemp assistant;
    @Test
    void testChat() {
        String string = assistant.chat("北京有什么好吃的");
        System.out.println(string);
    }
}

