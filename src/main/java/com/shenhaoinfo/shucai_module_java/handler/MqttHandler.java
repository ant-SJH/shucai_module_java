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

    public void handler(String message) {
        try {
            JSONObject json = JSONObject.parseObject(message);
            int address = json.getInteger("Address");
            int funCode = json.getInteger("FunCode");
            JSONObject data = json.getJSONObject("Data");

            if (address == 155 && funCode == 2) {
                // 标志着机器人任务运动到位
                slaveStationState.taskArrive();
            } else if (address == 155 && funCode == 3) {
                int result = data.getInteger("result");
                slaveStationState.taskResult(result);
            }
        } catch (Exception e) {
            log.error("mqtt解析数据异常！", e);
        }
    }
}
