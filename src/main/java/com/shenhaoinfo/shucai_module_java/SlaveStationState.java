package com.shenhaoinfo.shucai_module_java;

import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.shenhaoinfo.shucai_module_java.util.ModbusUtils.addCrc;

/**
 * @author jinhang
 * @date 2022/10/26
 */
@Component
public class SlaveStationState {
    private final int MAX_LENGTH = 20;

    private final byte[] slaveStationState = new byte[MAX_LENGTH];

    public byte[] readStationState(byte[] order) {
        // 读取寄存器的起始地址
        int address = Byte.toUnsignedInt(order[3]);
        // 读取寄存器的数量
        int num = Byte.toUnsignedInt(order[5]);
        int length = 5 + num * 2;
        byte[] data = new byte[length];
        data[0] = 0x01;
        data[1] = 0x03;
        data[2] = (byte) (num * 2);
        System.arraycopy(slaveStationState, (address - 1) * 2, data, 3, num * 2);
        addCrc(data);
        return data;
    }

    public byte[] writeStationState(byte[] order) {
        int address = Byte.toUnsignedInt(order[3]);
        int num = Byte.toUnsignedInt(order[5]);
        // 在开始任务前清零各种状态
        Arrays.fill(slaveStationState, (byte) 0);
        System.arraycopy(order, 7, slaveStationState, (address-1)*2, num*2);
        byte[] data = new byte[8];
        System.arraycopy(order, 0, data, 0, 6);
        addCrc(data);
        return data;
    }

    public void taskArrive() {
        slaveStationState[7] = 1;
    }

    public void taskResult(int result) {
        // 将1号寄存器清零
        slaveStationState[0] = 0;
        slaveStationState[1] = 0;
        // 更新结果更新标志
        slaveStationState[9] = 1;
        // 更新任务结果值
        slaveStationState[11] = (byte) result;
    }

    public boolean isCanContinue() {
        return slaveStationState[13] == 1;
    }

    public int getTaskState() {
        return slaveStationState[1];
    }

    public byte[] getTaskInfo() {
        byte[] b = new byte[2];
        b[0] = slaveStationState[3];
        b[1] = slaveStationState[5];
        return b;
    }
}
