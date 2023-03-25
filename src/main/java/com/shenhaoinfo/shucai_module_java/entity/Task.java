package com.shenhaoinfo.shucai_module_java.entity;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName(value = "task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.INPUT)
    private String taskid;
    private String taskname;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = DatePattern.NORM_DATETIME_PATTERN)
    private Date taskissuedtime;
    private String taskstartdate;
    private String taskstarttime;
    private Integer taskgrad;
    private Integer taskallnumber;
    private Integer taskfinishednumber;
    private Integer taskstate;
    private Integer taskendwork;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = DatePattern.NORM_DATETIME_PATTERN)
    private Date tasklastedittime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = DatePattern.NORM_DATETIME_PATTERN)
    private Date taskstartdatetime;
    private Integer tasktypeid;
    private Integer checkid;
    private Integer deviceregion;
    private String robotcode;
    private String taskinfo;
    private String taskidinfo;
    /**
     *  由国网A下发任务的标志
     */
    @TableField("taskCode")
    private String taskCode;
}