package com.shenhaoinfo.shucai_module_java.handler;

import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author jinhang
 * @date 2022/10/26
 */
@Component
@Slf4j
public class MqttHandler {
    @Resource
    private SlaveStationState slaveStationState;

    @Resource
    private HMUploadHandler hmUploadHandler;

    public void handler(String message) {
        try {
            JSONObject json = JSONObject.parseObject(message);
            int address = json.getInteger("Address");
            int funCode = json.getInteger("FunCode");

            if (address == 155 && funCode == 2) {
                // 标志着机器人任务运动到位
                slaveStationState.taskArrive();
            } else if (address == 101 || address == 102) {
                hmUploadHandler.handle(message);
            }
        } catch (Exception e) {
            log.error("mqtt解析数据异常！", e);
        }
    }
}
