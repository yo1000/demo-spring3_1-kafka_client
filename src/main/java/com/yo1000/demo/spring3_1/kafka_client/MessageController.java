package com.yo1000.demo.spring3_1.kafka_client;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/message")
public class MessageController {
    private final KafkaConfig.ConfigProperties configProperties;
    private final Producer<String, String> producer;

    @Autowired
    public MessageController(
            KafkaConfig.ConfigProperties configProperties,
            Producer<String, String> producer
    ) {
        this.configProperties = configProperties;
        this.producer = producer;
    }

    @RequestMapping(
            value = "/",
            method = RequestMethod.POST
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void postMessage(
            @RequestBody String message
    ) {
        producer.send(new ProducerRecord<String, String>(
                configProperties.getProperty(KafkaConfig.TOPIC_NAME_CONFIG),
                UUID.randomUUID().toString(),
                message
        ));
    }
}
