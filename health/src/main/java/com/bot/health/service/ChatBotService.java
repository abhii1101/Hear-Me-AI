package com.bot.health.service;

import com.bot.health.workflow.ChatWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ChatBotService {

    @Autowired
    private OCIBotService ociChatBot;

    public String chat(String question){

        ChatWorkflow chatWorkflow = ociChatBot.chatWorkflow(question);

        try {
            return chatWorkflow.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void load(){
        ociChatBot.load();
    }

    public void dropTableAndLoad(){
        ociChatBot.dropTableAndLoad();
    }
}
