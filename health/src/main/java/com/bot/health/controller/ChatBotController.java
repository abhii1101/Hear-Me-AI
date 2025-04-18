package com.bot.health.controller;

import com.bot.health.service.ChatBotService;
import com.bot.health.service.OCIBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatBotController {

    @Autowired
    private ChatBotService ociChatBot;

    @PostMapping("/send")
    public String chat(@RequestBody String userInput) {
        return ociChatBot.chat(userInput);
    }

    @PostMapping("/load")
    public void loadData() {
        ociChatBot.load();
    }

    @PostMapping("/dropTableAndLoad")
    public void dropTableAndLoad() {
        ociChatBot.dropTableAndLoad();
    }

}