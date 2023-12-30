package com.example.kafkaProducerSimple;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SimpleProducer {
    private final static Logger logger = LoggerFactory.getLogger(SimpleProducer.class);
    private final static String TOPIC_NAME = "test";
    private final static String BOOTSTRAP_SERVERS = "localhost:9094";

    public static void main(String[] args) {

        Properties configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());    // 메시지 키 String으로 직렬화
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  // 메시지 값 String으로 직렬화

        /*
         * KafkaProducer 인스턴스 생성
         * KafkaProducer<메시지 키 직렬화 타입, 메시지 값 직렬화 타입>으로 선언
         */
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        String messageValue = "testMessage";

        /*
         * ProducerRecord는 필수값으로 Topic 이름과 메시지 값을 받는다.
         * ProducerRecord는 오프셋이 없다.
         * 카프카 브로커의 리더 파티션에 저장될 때 레코드의 오프셋이 지정된다.
         */
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, messageValue);

        producer.send(record);  // 내부적으로는 Accumulator이 동작해 배치 단위로 레코드 전송
        logger.info("{}", record);

        producer.flush();
        producer.close();
    }
}