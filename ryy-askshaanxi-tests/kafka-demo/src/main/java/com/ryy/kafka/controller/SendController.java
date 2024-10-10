package com.ryy.kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendController {
    @Autowired
    private KafkaTemplate<String ,String> kafkaTemplate;
    @GetMapping("/send")
    public String send(){
        kafkaTemplate.send("orderCreate","{orderId:12857827842}");
        return "OK";
    }
}
