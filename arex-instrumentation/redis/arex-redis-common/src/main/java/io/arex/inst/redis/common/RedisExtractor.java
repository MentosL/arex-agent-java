package io.arex.inst.redis.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;

import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisExtractor {
    private static final String SPECIAL_CLASS_NAME = "redis.clients.jedis.BinaryJedis$SetFromList";

    private final String clusterName;
    private final String command;
    private final String key;
    private final String field;

    public RedisExtractor(String url, String method, String key, String field) {
        this.clusterName = RedisCluster.get(url);
        this.command = method;
        this.key = key;
        this.field = field;
    }

    public static class RedisMultiKey {
        private String key;
        private String field;

        public RedisMultiKey() {}

        public RedisMultiKey(String key, String field) {
            this.key = key;
            this.field = field;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

    private String normalizeTypeName(Object response) {
        String typeName = TypeUtil.getName(response);
        if (SPECIAL_CLASS_NAME.equals(typeName)) {
            return "java.util.HashSet";
        }
        return typeName;
    }

    public void record(Object response) {
        MockUtils.recordMocker(makeMocker(response));
    }

    public MockResult replay() {
        boolean ignoreResult = IgnoreUtils.ignoreMockResult(clusterName, command);
        Object replayBody = MockUtils.replayBody(makeMocker(null));
        return MockResult.success(ignoreResult, replayBody);
    }

    private Mocker makeMocker(Object response) {
        Mocker mocker = MockUtils.createRedis(this.command);
        mocker.getTargetRequest().setBody(Serializer.serialize(new RedisMultiKey(key, field)));
        mocker.getTargetRequest().setAttribute("clusterName", this.clusterName);
        mocker.getTargetResponse().setBody(Serializer.serialize(response));
        mocker.getTargetResponse().setType(normalizeTypeName(response));
        return mocker;
    }

    static class RedisCluster {
        private final static ConcurrentHashMap<String, String> REDIS_CLUSTER_CACHE = new ConcurrentHashMap<>(5);
        private final static AtomicInteger sequence = new AtomicInteger();

        static String get(String key) {
            return REDIS_CLUSTER_CACHE.computeIfAbsent(key, k -> "Cluster" + sequence.addAndGet(1));
        }
    }
}