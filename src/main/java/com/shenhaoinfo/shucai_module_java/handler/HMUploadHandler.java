package com.shenhaoinfo.shucai_module_java.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.SlaveStationState;
import com.shenhaoinfo.shucai_module_java.bean.ApiParamNameEnum;
import com.shenhaoinfo.shucai_module_java.bean.GetParam;
import com.shenhaoinfo.shucai_module_java.bean.PatrolResult;
import com.shenhaoinfo.shucai_module_java.service.SqlService;
import com.shenhaoinfo.shucai_module_java.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author jinhang
 * @date 2023/2/9
 * 用于将结果上传到环茂服务器上
 */
@Component
@Slf4j
public class HMUploadHandler {
    @Resource
    private UploadService uploadService;

    @Resource
    private SqlService sqlService;

    @Resource
    private SlaveStationState slaveStationState;

    @Value("${ftp.path:/home/robot/data/ftphome/}")
    private String ftpPath;

    public void handle(String message) throws InterruptedException{
        log.info("HMUploadHandler 接收到数据：{}", message);
        JSONObject json = JSONObject.parseObject(message);
        int address = json.getInteger("Address");
        int funCode = json.getInteger("funCode");
        PatrolResult result = JSON.parseObject(json.getString("Data"), PatrolResult.class);
        // 过滤多余信息
        if (filterMessage(address, funCode, result)) {
            return;
        }

        // 上传图片或者视频到环茂平台
        String fileName = uploadResource(result);
        if (StrUtil.isBlank(fileName)) {
            log.info("上传文件失败！");
            TimeUnit.SECONDS.sleep(3);
            fileName = uploadResource(result);
        }

        // 上传到数采平台
        slaveStationState.taskResult(getResult(result));

        // 上传巡检结果至环茂平台
        GetParam param = new GetParam();
        param.setFileName(fileName);
        param.setDeviceCode(getDeviceCode(result));
        param.setTime(result.getDevicepatroltime());
        param.setDesc(result.getResultstr());
        param.setResult(getResult(result));

        boolean flag = uploadService.uploadData(getApi(result), param);
        log.info("本次任务上传结果：{}", flag);
    }

    /**
     * 过滤无效信息
     */
    private boolean filterMessage(int address, int funCode, PatrolResult result) {
        return !(address == 101 && funCode == 2 && !"9".equals(result.getStatus())) &&
                !(address == 102 && funCode == 1 && !"9".equals(result.getStatus()));
    }

    private String getDeviceCode(PatrolResult result) {
        switch (result.getDevicename()) {
            case "数采机柜#数采显示屏" :
                return "001";
            case "COD分析仪#采样仪显示屏" :
                return "002";
            case "COD分析仪#TN管" :
                return "003";
            case "COD分析仪#水样管" :
                return "004";
            case "COD分析仪#蠕动泵" :
                return "005";
            case "氨氮分析仪#采样仪显示屏" :
                return "006";
            case "氨氮分析仪#TN管" :
                return "007";
            case "氨氮分析仪#水样管" :
                return "008";
            case "氨氮分析仪#蠕动泵" :
                return "009";
            case "总磷分析仪#采样仪显示屏" :
                return "010";
            case "总磷分析仪#TN管" :
                return "011";
            case "总磷分析仪#水样管" :
                return "012";
            case "总磷分析仪#蠕动泵" :
                return "013";
            case "总氮分析仪#采样仪显示屏" :
                return "014";
            case "总氮分析仪#TN管" :
                return "015";
            case "总氮分析仪#水样管" :
                return "016";
            case "总氮分析仪#蠕动泵" :
                return "017";
            case "水质等比例采样器#人工取样位置" :
                return "018";
            case "水质等比例采样器#AB罐" :
                return "019";
            case "COD分析仪#供样罐" :
                return "020";
            case "氨氮分析仪#供样罐" :
                return "021";
            case "总磷分析仪#供样罐" :
                return "022";
            case "总氮分析仪#供样罐" :
                return "023";
            default :
                return "";
        }
    }

    private int getResult(PatrolResult result) {
        String[] deviceName = result.getDevicename().split("#");
        if (deviceName[1].equals("TN管")) {
            switch (deviceName[0]) {
                case "COD分析仪" :
                    return "4".equals(result.getResultstr()) ? 1 : 0;
                case "氨氮分析仪" :
                    return "5".equals(result.getResultstr()) ? 1 : 0;
                case "总磷分析仪" :
                case "总氮分析仪" :
                    return "6".equals(result.getResultstr()) ? 1 : 0;
                default:
                    return 0;
            }
        } else {
            return result.getData1();
        }
    }

    private ApiParamNameEnum getApi(PatrolResult result) {
        String taskName = sqlService.getTaskNameByTaskId(result.getTaskid());
        if (StrUtil.isBlank(taskName)) return null;
        String[] name = taskName.split("_");
        return ApiParamNameEnum.getByDeviceType(name[0]);
    }

    private String uploadResource(PatrolResult result) {
        String fileName = null;
        if (StrUtil.isNotBlank(result.getVideopath())) {
            String filePath = ftpPath + result.getVideopath();
            log.info("上传视频给环茂平台，视频路径为：{}", filePath);
            fileName = uploadService.uploadFile(filePath);
        } else if (StrUtil.isNotBlank(result.getVisiblevideoimgpath())) {
            String filePath = ftpPath + result.getVisiblevideoimgpath();
            log.info("上传图片给环茂平台，图片路径为：{}", filePath);
            fileName = uploadService.uploadFile(filePath);
        }
        return fileName;
    }
}
