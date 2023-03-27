package com.shenhaoinfo.shucai_module_java.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.shenhaoinfo.shucai_module_java.bean.ApiParamNameEnum;
import com.shenhaoinfo.shucai_module_java.bean.GetParam;
import com.shenhaoinfo.shucai_module_java.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.Date;

/**
 * @author jinhang
 * @date 2023/3/14
 */
@Slf4j
@Component
public class SmiaHandler {
    @Resource
    private UploadService uploadService;

    public void handler(String message) {
        JSONObject json = JSONObject.parseObject(message);
        int address = json.getInteger("Address");
        int funCode = json.getInteger("FunCode");
        JSONObject data = json.getJSONObject("Data");

        if (address == 101 && funCode == 16) {
            uploadInvade(data);
        } else if (address == 101 && funCode == 17) {
            uploadVideo(data);
        }
    }

    private void uploadInvade(JSONObject data) {
        log.info("接收到人员入侵消息，开始上传入侵结果");
        // 上传图片
        String filePath = data.getString("visiblevideoimgpath");
        String fileName = null;
        try {
            fileName = uploadService.uploadFile(filePath);
        } catch (FileNotFoundException e) {
            log.error("", e);
        }

        if (StrUtil.isNotBlank(fileName)) {
            // 上传入侵结果
            GetParam param = GetParam.builder()
                    .fileName(fileName)
                    .deviceCode("001")
                    .time(new Date(data.getLong("devicepatroltime")))
                    .desc("人员入侵")
                    .result(0)
                    .build();
            boolean flag = uploadService.uploadData(ApiParamNameEnum.T01_CHECK_ALARM, param);
            log.info("本次入侵识别上传结果：{}", flag);
        }
    }

    private void uploadVideo(JSONObject data) {
        log.info("接收到入侵视频消息，开始上传视频结果");
        String filePath = data.getString("visiblevideoimgpath");
        try {
            String fileName = uploadService.uploadFile(filePath);
            if (fileName != null) {
                log.info("上传入侵视频文件成功，删除本地文件。");
                FileUtil.del(filePath);
            }
        } catch (FileNotFoundException e) {
            log.error("入侵监视文件上传失败！", e);
        }
    }
}
