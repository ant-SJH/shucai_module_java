package com.shenhaoinfo.shucai_module_java.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import com.shenhaoinfo.shucai_module_java.bean.Task;
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

    public static Task task;

    @Scheduled(fixedDelay = 1_000)
    public void slaveStateQuery(){
        try {
            // 检测数采是否有任务下发
            int currentTaskState = slaveStationState.getTaskState();
            if (currentTaskState == 1 && lastTaskState == 0) {
                sendToRobot();
            }
            lastTaskState = currentTaskState;

            // 检测机器人是否到达目标点位并且等待数采打开柜门
            if (slaveStationState.isCanContinue() && !lastCanState) {
                mqttSend.sendMessage("{\"Address\": 155,\"FunCode\": 2}");
                if (task != null) {
                    task.setDoorOpen(true);
                }
            }
            lastCanState = slaveStationState.isCanContinue();

            // 判断任务是否超时
            if (task != null) {
                long gap = System.currentTimeMillis() - task.getLastReceivedTaskTime();
                if (gap > 10 * 60 * 1000) {
                    log.info("长时间未收到机器人任务信息，终止该任务");
                    slaveStationState.taskError();
                    task = null;
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Scheduled(fixedDelay = 30_000)
    public void checkSerialConnect() {
        serialPortConfig.checkConnect();
    }

    private void sendToRobot() {
        byte[] b = slaveStationState.getTaskInfo();
        log.info("接收到来自数采的任务，接口号：{}，任务号：{}", b[0], b[1]);
        JSONObject data = new JSONObject();
        data.put("interfaceCode", b[0]);
        data.put("taskNum", b[1]);
        JSONObject json = new JSONObject();
        json.put("Address", 155);
        json.put("FunCode", 1);
        json.put("SeqId", System.currentTimeMillis());
        json.put("Data", data);
        mqttSend.sendMessage(json.toJSONString());

        // 创建任务
        int deviceNum = b[0]==20 ? 2 : 1;
        task = Task.builder().time(System.currentTimeMillis()).deviceNum(deviceNum).currentDeviceNum(1).
                lastReceivedTaskTime(System.currentTimeMillis()).taskResult(0).isDoorOpen(false).build();
    }
}
