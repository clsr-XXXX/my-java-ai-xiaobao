package org.lc4j;

import org.junit.jupiter.api.Test;
import org.lc4j.assistant.XiaoBaoAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

@SpringBootTest
@ActiveProfiles("dev")
public class AiService {
    @Autowired
    private XiaoBaoAgent assistant;

    @Test
    void testChat() {
        long id = 1;
        String reply = assistant.chat(id,"你好");
        System.out.println(reply);

    }

}
