package com.yo1000.demo.spring3_1.kafka_client;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.text.MessageFormat;

@Component
@EnableScheduling
public class KafkaSubscriber {
    private final Consumer<String, String> consumer;

    @Autowired
    public KafkaSubscriber(Consumer<String, String> consumer) {
        this.consumer = consumer;
    }

    @Scheduled(fixedDelay = 1000L) // 1sec (1,000ms)
    public void eachPolling() {
        ConsumerRecords<String, String> records = consumer.poll(60000L); // 1min (60,000ms)

        for (ConsumerRecord<String, String> record : records) {
            System.out.println(MessageFormat.format(
                    "Consume message: Key={0}, Value={1}, Offset={2}",
                    record.key(),
                    record.value(),
                    record.offset()));
        }
    }

    @PreDestroy
    public void dispose() {
        consumer.close();
    }
}
