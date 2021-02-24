package com.yo1000.demo.spring3_1.kafka_client;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaConfig {
    protected static final String ENV_PREFIX = "KAFKA_";
    public static final String TOPIC_NAME_CONFIG = "topic.name";

    @Bean
    public ConfigProperties configProperties() {
        final ConfigProperties configProps = new ConfigProperties(ENV_PREFIX);
        configProps.loadSystemProperties();
        configProps.loadEnvVars();

        if (!configProps.containsKey(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG))
            throw new IllegalStateException(MessageFormat.format(
                    "Kafka config property is missing - {0}",
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

        if (!configProps.containsKey(ConsumerConfig.GROUP_ID_CONFIG))
            throw new IllegalStateException(MessageFormat.format(
                    "Kafka config property is missing - {0}",
                    ConsumerConfig.GROUP_ID_CONFIG));

        return configProps;
    }

    @Bean
    public Consumer<String, String> consumer(final ConfigProperties configProperties) {
        final Consumer<String, String> consumer = new KafkaConsumer<String, String>(
                configProperties,
                new StringDeserializer(), // KeyDeserializer
                new StringDeserializer()  // ValueDeserializer
        );

        String topicName = System.getenv(ENV_PREFIX + TOPIC_NAME_CONFIG
                .replace('.', '_')
                .toUpperCase());
        if (topicName == null || topicName.trim().isEmpty())
            topicName = System.getProperty(TOPIC_NAME_CONFIG);
        if (topicName == null || topicName.trim().isEmpty())
            throw new IllegalStateException("Kafka config property is missing - topic.name");

        consumer.subscribe(Collections.singletonList(topicName));
        return consumer;
    }

    @Bean
    public Producer<String, String> producer(ConfigProperties configProperties) {
        return new KafkaProducer<String, String>(
                configProperties,
                new StringSerializer(), // KeySerializer
                new StringSerializer()  // ValueSerializer
        );
    }

    public static class ConfigProperties extends Properties {
        private final String envVarPrefix;

        public ConfigProperties(String envVarPrefix) {
            this.envVarPrefix = envVarPrefix;

            // Offsetの自動更新のみ初期値を設定しておく (外部設定の入力があれば各loadで上書きされる)
            setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        }

        public void loadSystemProperties() {
            final String bootstrapServers = System.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
            if (bootstrapServers != null && !bootstrapServers.trim().isEmpty())
                setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers.trim());

            final String groupId = System.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
            if (groupId != null && !groupId.trim().isEmpty())
                setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId.trim());
        }

        public void loadEnvVars() {
            final Map<String, String> environments = System.getenv();
            for (final Map.Entry<String, String> entry : environments.entrySet()) {
                final String key = entry.getKey();
                if (key.startsWith(envVarPrefix)) {
                    setProperty(
                            key.substring(envVarPrefix.length())
                                    .replace('_', '.')
                                    .toLowerCase(),
                            entry.getValue());
                }
            }
        }
    }
}
