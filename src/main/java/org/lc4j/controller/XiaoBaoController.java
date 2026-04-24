package org.lc4j.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.lc4j.assistant.XiaoBaoAgent;
import org.lc4j.bean.ChatForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "小包")
@RestController
@RequestMapping("/api")
@Slf4j
public class XiaoBaoController {
    @Autowired
    private XiaoBaoAgent xiaoBaoAgent;

    @Operation(summary = "对话")
    @PostMapping("/chat")
    public String chat(@RequestBody ChatForm chatForm){
        log.info("开启对话");
        return xiaoBaoAgent.chat(chatForm.getMemoryId(),chatForm.getMessage());

    }
}
