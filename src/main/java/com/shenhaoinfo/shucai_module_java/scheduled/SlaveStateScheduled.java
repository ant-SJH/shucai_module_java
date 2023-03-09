package com.shenhaoinfo.shucai_module_java.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import com.shenhaoinfo.shucai_module_java.config.SerialPortConfig;
import com.shenhaoinfo.shucai_module_java.util.MqttSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
public class SlaveStateScheduled {

    @Resource
    private SlaveStationState slaveStationState;

    @Resource
    private MqttSend mqttSend;

    @Resource
    private SerialPortConfig serialPortConfig;

    private int lastTaskState;

    private boolean lastCanState;

    @Scheduled(fixedDelay = 1_000)
    public void slaveStateQuery(){
        try {
            int currentTaskState = slaveStationState.getTaskState();
            if (currentTaskState == 1 && lastTaskState == 0) {
                sendToRobot();
            }
            lastTaskState = currentTaskState;

            if (slaveStationState.isCanContinue() && !lastCanState) {
                mqttSend.sendMessage("{\"Address\": 155,\"FunCode\": 2}");
            }
            lastCanState = slaveStationState.isCanContinue();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Scheduled(fixedDelay = 10_000)
    public void checkSerialConnect() {
        serialPortConfig.checkConnect();
    }

    private void sendToRobot() {
        byte[] b = slaveStationState.getTaskInfo();
        JSONObject data = new JSONObject();
        data.put("interfaceCode", b[0]);
        data.put("taskNum", b[1]);
        JSONObject json = new JSONObject();
        json.put("Address", 155);
        json.put("FunCode", 1);
        json.put("SeqId", System.currentTimeMillis());
        json.put("Data", data);
        mqttSend.sendMessage(json.toJSONString());
    }
}
