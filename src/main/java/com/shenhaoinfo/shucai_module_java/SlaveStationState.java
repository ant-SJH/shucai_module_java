package com.shenhaoinfo.shucai_module_java;

import com.shenhaoinfo.shucai_module_java.bean.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.shenhaoinfo.shucai_module_java.scheduled.SlaveStateScheduled.taskList;
import static com.shenhaoinfo.shucai_module_java.util.ModbusUtils.addCrc;

/**
 * @author jinhang
 * @date 2022/10/26
 */
@Component
@Slf4j
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
        taskIssue(order);
        int address = Byte.toUnsignedInt(order[3]);
        int num = Byte.toUnsignedInt(order[5]);
        System.arraycopy(order, 7, slaveStationState, (address-1)*2, num*2);
        byte[] data = new byte[8];
        System.arraycopy(order, 0, data, 0, 6);
        addCrc(data);
        return data;
    }

    public void taskIssue(byte[] order) {
        if (order == null || order.length != 15) {
            return;
        }
        byte[] start = Arrays.copyOfRange(order, 0, 7);
        if (!Arrays.equals(start, new byte[]{0x01, 0x10, 0x00, 0x01, 0x00, 0x03, 0x06})) {
            log.info("非下发任务指令，不添加任务");
            return;
        }
        log.info("接收到数采下发的指令：{}_{}", order[10], order[12]);
        Task task = Task.builder().time(System.currentTimeMillis())
                .interfaceNum(order[10])
                .taskNum(order[12]).build();
        if (taskList.size() > 0) {
            // 如果是重复发送的任务，则不存入任务列表里
            Task lastTask = taskList.get(taskList.size() - 1);
            if (lastTask.getInterfaceNum() != task.getInterfaceNum() || lastTask.getTaskNum() != task.getTaskNum()) {
                taskList.add(task);
            }
        } else {
            taskList.add(task);
        }
    }

    public void taskArrive() {
        slaveStationState[7] = 1;
    }

    public void taskStart(Task task) {
        // 在开始任务前清零各种状态
        Arrays.fill(slaveStationState, (byte) 0);
        slaveStationState[1] = 1;
        slaveStationState[3] = (byte) task.getInterfaceNum();
        slaveStationState[5] = (byte) task.getTaskNum();
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

    public void taskError() {
        // 将1号寄存器清零
        slaveStationState[0] = 0;
        slaveStationState[1] = 0;
        // 将8号寄存器置为1表示任务异常
        slaveStationState[15] = 1;
    }

    public boolean isCanContinue() {
        return slaveStationState[13] == 1;
    }

    public byte[] getTaskInfo() {
        byte[] b = new byte[2];
        b[0] = slaveStationState[3];
        b[1] = slaveStationState[5];
        return b;
    }
}
