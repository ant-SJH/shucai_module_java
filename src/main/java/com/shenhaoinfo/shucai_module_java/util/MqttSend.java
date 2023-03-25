package com.shenhaoinfo.shucai_module_java.util;

import com.shenhaoinfo.shucai_module_java.config.MqttGatewayServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author jinhang
 * @date 2022/10/26
 */
@Component
@Slf4j
public class MqttSend {
    @Resource
    private MqttGatewayServer mqttGatewayServer;

    @Value("${spring.mqtt.sendTopic}")
    private String sendTopic;

    public void sendMessage(String message){
        log.info("发送mqtt消息：{}", message);
        mqttGatewayServer.sendToMqtt(message, sendTopic);
    }
}

