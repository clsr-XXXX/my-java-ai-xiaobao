package org.lc4j.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lc4j.bean.ChatForm;
import org.lc4j.service.AgentRouterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "小宝")
@RestController
@RequestMapping("/api")
@Slf4j
public class XiaoBaoController {

    private final AgentRouterService agentRouterService;

    public XiaoBaoController(AgentRouterService agentRouterService) {
        this.agentRouterService = agentRouterService;
    }

    @Operation(summary = "对话")
    @PostMapping("/chat")
    public String chat(@RequestBody ChatForm chatForm) {
        log.info("开始对话");
        return agentRouterService.chat(chatForm.getMemoryId(), chatForm.getMessage());
    }
}
