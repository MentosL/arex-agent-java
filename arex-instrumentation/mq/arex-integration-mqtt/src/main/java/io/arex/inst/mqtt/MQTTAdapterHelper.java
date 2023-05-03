package io.arex.inst.mqtt;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.util.MockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : MentosL
 * @date : 2023/4/30 09:22
 */
public class MQTTAdapterHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MQTTAdapterHelper.class);

    public static Mocker createMocker(String operationName) {
        Mocker mocker = MockUtils.createMqttConsumer(operationName);
        return mocker;
    }
}
