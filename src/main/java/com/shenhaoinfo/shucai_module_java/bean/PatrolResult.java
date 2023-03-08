package com.shenhaoinfo.shucai_module_java.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jinhang
 * @date 2023/2/9
 */
@Data
public class PatrolResult implements Serializable {
    private int data1;
    private int data2;
    private String deviceid;
    private String devicename;
    private Date devicepatroltime;
    private long patroltype;
    private String resultstr;
    private String robotcode;
    private String status;
    private String taskid;
    private Date taskstarttime;
    private String unit;
    private String videopath;
    private String visiblevideoimgpath;
}
