package com.shenhaoinfo.shucai_module_java.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author jinhang
 * @date 2023/3/11
 */
@Data
@AllArgsConstructor
@Builder
public class Task {
    /**
     * 任务下发时间
     */
    private long time;

    /**
     * 任务点位数量
     */
    private int deviceNum;

    /**
     * 上一次接收到任务信息时间
     */
    private long lastReceivedTaskTime;

    /**
     * 当前任务点位
     */
    private int currentDeviceNum;

    /**
     * 任务结果
     */
    private int taskResult;

    /**
     * 判断柜门是否已打开
     */
    private boolean isDoorOpen;
}
