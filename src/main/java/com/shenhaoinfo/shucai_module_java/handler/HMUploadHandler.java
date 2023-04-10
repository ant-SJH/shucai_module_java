package com.shenhaoinfo.shucai_module_java.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import com.shenhaoinfo.shucai_module_java.bean.PatrolResult;
import com.shenhaoinfo.shucai_module_java.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shenhaoinfo.shucai_module_java.scheduled.SlaveStateScheduled.task;
import static com.shenhaoinfo.shucai_module_java.util.UploadUtil.getResult;
import static com.shenhaoinfo.shucai_module_java.util.UploadUtil.needUploadList;

/**
 * @author jinhang
 * @date 2023/2/9
 * 用于将结果上传到环茂服务器上
 */
@Component
@Slf4j
public class HMUploadHandler {
    private static final Pattern REG_PATTERN = Pattern.compile("\\d+_\\d+_\\d+");
    @Resource
    private UploadService uploadService;
    @Resource
    private SlaveStationState slaveStationState;

    public void handle(String message) throws InterruptedException {
        JSONObject json = JSONObject.parseObject(message);
        int address = json.getInteger("Address");
        int funCode = json.getInteger("FunCode");
        PatrolResult result = JSON.parseObject(json.getString("Data"), PatrolResult.class);
        // 过滤多余信息
        if (filterMessage(address, funCode, result)) {
            return;
        }

        log.info("接收到机器人任务结果，{}", message);

        // 上传到数采平台
        if (task != null && task.getCurrentDeviceNum() == task.getDeviceNum()) {
            int r = getResult(result) + task.getTaskResult();
            r = r == task.getDeviceNum() ? 1 : 0;
            slaveStationState.taskResult(r);
            log.info("任务结束，上传给数采的结果为：{}", r);
            task.setFinishedTime(System.currentTimeMillis());
        } else if (task != null && task.getCurrentDeviceNum() < task.getDeviceNum()) {
            task.setCurrentDeviceNum(task.getCurrentDeviceNum() + 1);
            task.setTaskResult(task.getTaskResult() + getResult(result));
            log.info("任务还未结束，本次执行结果为：{}", getResult(result));
        }

        try {
            boolean flag = uploadService.uploadPatrolResult(result);
            if (flag) {
                log.info("本次上传结果至环茂平台成功！");
            } else {
                needUploadList.add(result);
                log.info("本次上传失败，等待下次上传。");
            }
        } catch (FileNotFoundException e) {
            log.error("文件不存在，上传失败！", e);
        }
    }

    /**
     * 过滤无效信息
     */
    private boolean filterMessage(int address, int funCode, PatrolResult result) {
        String taskId = result.getTaskid();
        Matcher matcher = REG_PATTERN.matcher(taskId);
        boolean flag = !(address == 101 && funCode == 2 && !"9".equals(result.getStatus())) &&
                !(address == 102 && funCode == 1 && !"9".equals(result.getStatus()));
        return !matcher.matches() || flag || task == null;
    }
}
