package com.shenhaoinfo.shucai_module_java.bean;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author jinhang
 * @date 2023/2/9
 */
@Data
@Builder
public class GetParam {
    /**
     * 巡检结果，1-正常，0-异常
     */
    private int result;
    /**
     * 设备编号
     */
    private String deviceCode;
    /**
     * 描述
     */
    private String desc;
    /**
     * 平台保存的照片或文件名字
     */
    private String fileName;
    /**
     * 业务时间，格式yyyy-MM-dd HH:mm:ss
     */
    private Date time;
}
