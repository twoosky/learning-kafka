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
  --partitions 1 \
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

## 2. 메시지를 가진 레코드를 전송하는 Producer
* 메시지 키가 포함된 레코드를 전송하려면 ProducerRecord 생성 시 파라미터로 메시지 키를 넣어줘야 한다.
* 토픽 이름, 메시지 키, 메시지 값을 순서대로 파라미터로 넣고 생성해야 한다.
```java
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "Seoul", "Soongsil");
```

**실습**
* [code](https://github.com/twoosky/learning-kafka/blob/main/kafka-producer/src/main/java/com/example/kafkaProducerKeyValue/ProducerWithKeyValue.java)를 실행한 뒤 아래 명령을 통해 전송된 레코드를 확인할 수 있다.
* --property 옵션을 통해 메시지 키, 메시지 값 함께 확인
* 메시지 키가 없는 레코드의 경우 메시지 키가 null로 나타난다.
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
