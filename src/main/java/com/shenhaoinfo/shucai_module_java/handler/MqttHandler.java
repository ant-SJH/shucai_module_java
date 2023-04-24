package com.shenhaoinfo.shucai_module_java.handler;

import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import com.shenhaoinfo.shucai_module_java.scheduled.SlaveStateScheduled;
import com.shenhaoinfo.shucai_module_java.util.MqttSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author jinhang
 * <p>
 * date 2022/10/26
 * </p>
 */
@Component
@Slf4j
public class MqttHandler {
    @Resource
    private SlaveStationState slaveStationState;

    @Resource
    private HMUploadHandler hmUploadHandler;

    @Resource
    private SmiaHandler smiaHandler;

    @Resource
    private MqttSend mqttSend;

    public void handler(String message) {
        try {
            JSONObject json = JSONObject.parseObject(message);
            int address = json.getInteger("Address");
            int funCode = json.getInteger("FunCode");

            if (address == 9 && funCode == 7 && SlaveStateScheduled.task != null) {
                log.info("接收到机器人任务数据，{}", message);
                SlaveStateScheduled.task.setLastReceivedTaskTime(System.currentTimeMillis());
            } else if (address == 101 && (funCode == 16 || funCode == 17)) {
                smiaHandler.handler(message);
            } else if (address == 101 || address == 102) {
                hmUploadHandler.handle(message);
            } else if (address == 155 && funCode == 2) {
                log.info("接收到机器人运动到位信息，{}", message);
                // 标志着机器人任务运动到位
                if (SlaveStateScheduled.task != null && SlaveStateScheduled.task.isDoorOpen()) {
                    mqttSend.sendMessage("{\"Address\": 155,\"FunCode\": 2}");
                } else {
                    slaveStationState.taskArrive();
                }
            }
        } catch (Exception e) {
            log.error("mqtt解析数据异常!,message: " + message, e);
        }
    }
}
