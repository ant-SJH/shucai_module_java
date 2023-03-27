package com.shenhaoinfo.shucai_module_java.scheduled;

import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import com.shenhaoinfo.shucai_module_java.bean.PatrolResult;
import com.shenhaoinfo.shucai_module_java.bean.Task;
import com.shenhaoinfo.shucai_module_java.config.SerialPortConfig;
import com.shenhaoinfo.shucai_module_java.service.UploadService;
import com.shenhaoinfo.shucai_module_java.util.MqttSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.shenhaoinfo.shucai_module_java.util.UploadUtil.needUploadList;


@Slf4j
@Component
public class SlaveStateScheduled {

    @Resource
    private SlaveStationState slaveStationState;

    @Resource
    private MqttSend mqttSend;

    @Resource
    private SerialPortConfig serialPortConfig;

    @Resource
    private UploadService uploadService;

    private boolean lastCanState;

    public static Task task;

    public static List<Task> taskList = new ArrayList<>();

    @Scheduled(fixedDelay = 1_000)
    public void slaveStateQuery(){
        try {
            // 检测数采是否有任务下发
            if (task == null && taskList.size() > 0) {
                task = taskList.get(0);
                slaveStationState.taskStart(task);
                sendToRobot();
            }

            // 判断任务是否超时
            if (task != null) {
                // 检测机器人是否到达目标点位并且等待数采打开柜门
                if (slaveStationState.isCanContinue() && !lastCanState) {
                    mqttSend.sendMessage("{\"Address\": 155,\"FunCode\": 2}");
                    if (task != null) {
                        task.setDoorOpen(true);
                    }
                }
                lastCanState = slaveStationState.isCanContinue();

                long gap = System.currentTimeMillis() - task.getLastReceivedTaskTime();
                if (gap > 10 * 60 * 1000) {
                    log.info("长时间未收到机器人任务信息，终止该任务");
                    slaveStationState.taskError();
                    taskList.remove(task);
                    task = null;
                } else if (task.getFinishedTime() != 0 && System.currentTimeMillis() - task.getFinishedTime() > 30_000) {
                    // 任务已完成超过30s，数采应该已经读取到任务完成信号，开始下一个任务
                    taskList.remove(task);
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

    @Scheduled(fixedDelay = 30_000)
    public void uploadPatrolResult() {
        if (needUploadList != null && needUploadList.size() > 0) {
            PatrolResult result = needUploadList.get(0);
            try {
                log.info("尝试上传之前上传失败的结果");
                boolean flag = uploadService.uploadPatrolResult(result);
                if (flag) {
                    log.info("本次上传巡检结果至环茂平台成功！");
                    needUploadList.remove(result);
                } else {
                    log.info("本次上传失败！");
                }
            } catch (InterruptedException e) {
                log.error("", e);
            } catch (FileNotFoundException e) {
                log.error("文件不存在，上传失败！", e);
                needUploadList.remove(result);
            }
            // 清除过多的等待上传结果，避免内存溢出
            if (needUploadList.size() > 1000) {
                needUploadList.clear();
            }
        }
    }

    private void sendToRobot() {
        byte[] b = slaveStationState.getTaskInfo();
        log.info("开始执行来自数采的任务，接口号：{}，任务号：{}", b[0], b[1]);
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
        task.setTime(System.currentTimeMillis());
        task.setDeviceNum(deviceNum);
        task.setCurrentDeviceNum(1);
        task.setLastReceivedTaskTime(System.currentTimeMillis());
        task.setTaskResult(0);
        task.setDoorOpen(false);
        task.setFinishedTime(0);
    }
}
