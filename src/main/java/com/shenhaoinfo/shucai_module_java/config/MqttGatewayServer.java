package com.shenhaoinfo.shucai_module_java.config;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGatewayServer {
    void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);
}
