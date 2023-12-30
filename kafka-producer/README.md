# Producer Application
kafka 컨테이너 접속
```bash
$ docker exec -it my-kafka /bin/bash
```
Topic 생성
```bash
$ kafka-topics.sh \
  --bootstrap-server my-kafka:9092 \
  --replication-factor 1 \
  --partitions 10 \
  --topic test \
  --create
```

## 1. 기본 Producer
* Producer의 필수 옵션은 아래 3가지이다.
* `bootstrap.servers`: 데이터를 전송할 카프카 브로커의 호스트 이름:포트 번호 리스트
* `key.serializer`: 메시지 키를 직렬화하는 클래스
* `value.serializer`: 메시지 값을 직렬화하는 클래스

**실습**
* 필수 옵션만으로 선언한 프로듀서를 사용해 test 토픽으로 레코드를 전송해보자
* [code](https://github.com/twoosky/learning-kafka/blob/main/kafka-producer/src/main/java/com/example/kafkaProducerSimple/SimpleProducer.java)를 실행한 뒤 아래 명령을 통해 전송된 레코드를 확인할 수 있다. (testMessage가 나오면 성공)
```bash
$ kafka-console-consumer.sh \
  --bootstrap-server my-kafka:9092 \
  --topic test \
  --from-beginning

testMessage
```

## 2. 메시지 키를 가진 레코드를 전송하는 Producer
* 메시지 키가 포함된 레코드를 전송하려면 ProducerRecord 생성 시 파라미터로 메시지 키를 넣어줘야 한다.
* 토픽 이름, 메시지 키, 메시지 값을 순서대로 파라미터로 넣고 생성해야 한다.
```java
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "Seoul", "Soongsil");
```

**실습**
* [code](https://github.com/twoosky/learning-kafka/blob/main/kafka-producer/src/main/java/com/example/kafkaProducerKeyValue/ProducerWithKeyValue.java)를 실행한 뒤 아래 명령을 통해 전송된 레코드를 확인할 수 있다.
* --property 옵션을 통해 메시지 키, 메시지 값 함께 확인, 메시지 키가 없는 레코드의 경우 메시지 키가 null로 나타난다.
```bash
$ kafka-console-consumer.sh \
  --bootstrap-server my-kafka:9092 \
  --topic test \
  --property print.key=true \
  --property key.separator="-" \
  --from-beginning

Seoul-Soongsil
Busan-Busan
```

## 3. 레코드에 파티션 번호를 지정하여 전송하는 Producer
* 레코드가 전송되는 파티션을 직접 지정하러면 ProducerRecord 생성 시 파라미터로 파티션 번호를 넣어줘야 한다.
* 토픽 이름, 파티션 번호, 메시지 키, 메시지 값을 순서대로 파라미터로 넣고 생성해야 한다.
* 파티션 번호는 토픽에 존재하는 파티션 번호로 설정해야 한다.
```java
int partitionNo = 1;
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, partitionNo, "Seoul", "Soongsil");
```

**실습**
* [code](https://github.com/twoosky/learning-kafka/blob/main/kafka-producer/src/main/java/com/example/kafkaProducerExactPartition/ProducerExactPartition.java)를 실행한 뒤 아래 명령을 통해 전송된 레코드를 확인할 수 있다.
* --partition 옵션을 통해 특정 파티션의 레코드를 조회할 수 있다.
```bash
$ kafka-console-consumer.sh \
  --bootstrap-server my-kafka:9092 \
  --topic test \
  --partition 1 \
  --from-beginning

Soongsil
```

## 4. 커스텀 파티셔너를 갖는 Producer
* Partitioner 인터페이스를 구현해 사용자 정의 파티셔너를 생성할 수 있다.
* Producer의 Partitioner 옵션에 커스텀 파티셔너 클래스를 넣어 Producer를 생성해야 한다.
* 메시지 키 또는 메시지 값에 따라 파티션 번호를 지정할 수 있다.
  * 커스텀 파티셔너로 지정된 파티션 번호는 토픽의 파티션 개수가 변경되더라도 영향을 받지 않는다.
  * 기본 파티셔너의 메시지 키를 통한 파티션 지정의 경우 토픽의 파티션 개수 변경 시 파티션 번호 매칭이 깨짐
```java
Properties configs = new Properties();
...
configs.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class);

KafkaProducer<String, String> producer = new KafkaProducer<>(configs);
```
```java
public class CustomPartitioner  implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes,
                         Cluster cluster) {

        if (keyBytes == null) {
            throw new InvalidRecordException("Need message key");
        }

        // 메시지 키를 String 으로 변환하고 해당 값이 Pangyo면 무조건 2번 파티션으로 레코드 전송
        if (((String)key).equals("Pangyo"))
            return 2;

        ...
    }
}
```

**실습**
* [code](https://github.com/twoosky/learning-kafka/blob/main/kafka-producer/src/main/java/com/example/kafkaProducerCustomPartitioner/ProducerWithCustomPartitioner.java)를 실행한 뒤 아래 명령을 통해 전송된 레코드를 확인할 수 있다.
* --partition 옵션을 통해 2번 파티션의 레코드를 조회해보자.
```bash
$ kafka-console-consumer.sh \
  --bootstrap-server my-kafka:9092 \
  --topic test \
  --partition 2 \
  --from-beginning

Pangyo
```

## 5. 레코드의 전송 결과를 확인하는 Producer
* KafkaProducer의 send() 메서드는 Future 객체를 반환한다.
* Future는 RecordMetadata의 비동기 결과를 담은 객체로 ProducerRecord가 카프카 브로커에 정상적으로 적재되었는지에 대한 데이터가 포함되어 있다.
* RecordMetadata에는 *토픽 이름, 파티션 번호, 오프셋 번호* 가 담겨 있다.
* 아래와 같이 get() 메서드를 사용하면 프로듀서로 보낸 데이터의 결과를 동기적으로 가져올 수 있다. [code](https://github.com/twoosky/learning-kafka/blob/main/kafka-producer/src/main/java/com/example/kafkaProducerSyncCallback/ProducerWithSyncCallback.java)
```java
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "Pangyo", "Pangyo");

RecordMetadata metadata = producer.send(record).get();
logger.info(metadata.toString());
```
```
[main] INFO com.example.kafkaProducerSyncCallback.ProducerWithSyncCallback - test-7@0
```
* 기본 acks 옵션은 -1(all)이다.
* 만약 acks 옵션을 0으로 주면 리더 파티션에 데이터가 정상적으로 적재되었는지 확인하지 않기 때문에 오프셋 번호가 -1로 반환된다.

## 6. producer의 안전한 종료
* 프로듀서를 안전하게 종료하기 위해서는 close() 메서드를 사용하여 Accumulator에 저장되어 있는 모든 데이터를 카프카 클러스터로 전송해야 한다.
```java
producer.close();
```
