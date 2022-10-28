package com.shenhaoinfo.shucai_module_java.handler;

import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import com.shenhaoinfo.shucai_module_java.util.ModbusUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author jinhang
 * @date 2022/10/26
 */
@Component
@Slf4j
public class ModbusHandler {
    @Resource
    private SlaveStationState slaveStationState;

    public byte[] handler(byte[] data) {
        if (!checkDataLegal(data)) {
            return null;
        }
        // 根据传入功能码判断数采是读取还是写入
        int funCode = data[1];
        if (funCode == ModbusUtils.FUN_READ) {
            return slaveStationState.readStationState(data);
        } else if (funCode == ModbusUtils.FUN_WRITE) {
            return slaveStationState.writeStationState(data);
        } else {
            return null;
        }
    }

    /**
     * 检查数采传入的数据是否合法
     * @param data 数采发送过来的数据
     * @return true-合法
     */
    private boolean checkDataLegal(byte[] data) {
        if (data == null || data.length < 2) return false;
        // 判断其crc校验码是否准确
        int len = data.length;
        byte lowCrc = data[len-2];
        byte highCrc = data[len-1];
        ModbusUtils.addCrc(data);
        return lowCrc == data[len-2] && highCrc == data[len-1];
    }
}