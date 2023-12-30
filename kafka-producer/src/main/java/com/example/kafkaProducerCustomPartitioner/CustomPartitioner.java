package com.example.kafkaProducerCustomPartitioner;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.InvalidRecordException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;

public class CustomPartitioner  implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes,
                         Cluster cluster) {

        if (keyBytes == null) {
            throw new InvalidRecordException("Need message key");
        }
        if (((String)key).equals("Pangyo"))  // 메시지 키를 String 으로 변환하고 해당 값이 Pangyo면 무조건 2번 파티션으로 레코드 전송
            return 2;

        // 메시지 키가 Pangyo가 아닌 레코드는 메시지 키를 해시값으로 변환해서 파티션 번호를 지정하도록 설정
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
    }


    @Override
    public void configure(Map<String, ?> configs) {}

    @Override
    public void close() {}
}
