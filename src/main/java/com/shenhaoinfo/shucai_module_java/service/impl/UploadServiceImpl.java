package com.shenhaoinfo.shucai_module_java.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.shenhaoinfo.shucai_module_java.bean.*;
import com.shenhaoinfo.shucai_module_java.service.SqlService;
import com.shenhaoinfo.shucai_module_java.service.UploadService;
import com.shenhaoinfo.shucai_module_java.util.FFmpegUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.shenhaoinfo.shucai_module_java.util.UploadUtil.getDeviceCode;
import static com.shenhaoinfo.shucai_module_java.util.UploadUtil.getResult;

/**
 * @author jinhang
 * @date 2023/2/9
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    private static final String interfaceStr = "getdata.jsp";

    @Value("${hm.host:http://101.37.75.26:8080/hac_auto/}")
    private String host;

    @Value("${hm.subCode:8A0731CC39614C90A5D474BC17253713}")
    private String subCode;

    @Value("${hm.subUserCode:414A6DB3BBE6419DA3768E6E25127310}")
    private String subUserCode;

    @Value("${hm.siteCode:3301010001}")
    private String siteCode;

    @Value("${ftp.ftpPath:/home/robot/data/ftphome}")
    private String ftpPath;

    @Value("${ftp.algorithmPath:/home/robot/data/log}")
    private String algorithmPath;

    @Resource
    private SqlService sqlService;

    @Override
    public String uploadFile(String filePath) throws FileNotFoundException {
        try {
            filePath = FFmpegUtil.convertVideo2H264(filePath);
            File file = new File(filePath);
            String url = host + "uploadFile?param_name=upLoadFile";
            String result = HttpRequest.post(url)
                    .form("file", file)
                    .execute()
                    .body();
            log.info("上传文件结果：{}", result);
            String data = JSONObject.parseObject(result).getString(ApiParamNameEnum.A01_UpLoadFile.toString());
            List<UploadResult> uploadResults = JSONObject.parseArray(data, UploadResult.class);
            return uploadResults.get(0).getNewFileName();
        } catch (Exception e) {
            log.error("上传资源给环茂平台错误", e);
            if (e instanceof FileNotFoundException) {
                throw new FileNotFoundException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean uploadData(ApiParamNameEnum api, GetParam param) {
        try {
            String url = host + interfaceStr;
            Map<String, Object> map = new HashMap<>();
            map.put("sub_code", subCode);
            map.put("sub_usercode", subUserCode);
            map.put("param_name", api.toString());
            map.put("param_value1", param.getResult());
            map.put("param_value2", siteCode);
            map.put("param_value3", param.getDeviceCode());
            map.put("param_value4", URLEncoder.encode(param.getDesc(), StandardCharsets.UTF_8.name()));
            map.put("param_value5", param.getFileName());
            map.put("param_value6", DateUtil.formatDateTime(param.getTime()));
            String result = HttpRequest.post(url)
                    .form(map)
                    .execute()
                    .body();
            log.info("接口：{}，上传的结果为：{}", api, result);
            String data = JSONObject.parseObject(result).getString(api.toString());
            List<GetDataResult> results = JSONObject.parseArray(data, GetDataResult.class);
            return results.get(0).getSResult() == 1;
        } catch (Exception e) {
            log.info("上传结果错误，上传接口为：{}，上传数据为：{}", api, param);
            log.error("", e);
        }
        return false;
    }

    @Override
    public String uploadResource(PatrolResult result) throws FileNotFoundException {
        String fileName = null;
        if (StrUtil.isNotBlank(result.getVideopath())) {
            String filePath = result.getVideopath();
            if (!filePath.startsWith(ftpPath) && !filePath.startsWith(algorithmPath)) {
                filePath = ftpPath + filePath;
            }
            log.info("上传视频给环茂平台，视频路径为：{}", filePath);
            fileName = uploadFile(filePath);
        } else if (StrUtil.isNotBlank(result.getVisiblevideoimgpath())) {
            String filePath = result.getVisiblevideoimgpath();
            if (!filePath.startsWith(ftpPath) && !filePath.startsWith(algorithmPath)) {
                filePath = ftpPath + filePath;
            }
            log.info("上传图片给环茂平台，图片路径为：{}", filePath);
            fileName = uploadFile(filePath);
        }
        return fileName;
    }

    @Override
    public ApiParamNameEnum getApi(PatrolResult result) {
        String taskName = sqlService.getTaskNameByTaskId(result.getTaskid());
        if (StrUtil.isBlank(taskName)) return null;
        String[] name = taskName.split("_");
        if (name[1].equals("20")) {
            return result.getDevicename().split("#")[1].equals("TN管") ? ApiParamNameEnum.T01_CAPTURE_REAGENT_SAMPLING :
                    ApiParamNameEnum.T01_CAPTURE_ABNORMAL_PERISTALTIC_PUMP;
        }
        return ApiParamNameEnum.getByDeviceType(name[1]);
    }

    @Override
    public boolean uploadPatrolResult(PatrolResult result) throws InterruptedException, FileNotFoundException {
        // 上传图片或者视频到环茂平台
        String fileName = uploadResource(result);
        if (StrUtil.isBlank(fileName)) {
            log.info("上传文件失败！3S后尝试重新上传。");
            TimeUnit.SECONDS.sleep(3);
            fileName = uploadResource(result);
        }

        // 上传巡检结果至环茂平台
        if (StrUtil.isNotBlank(fileName)) {
            GetParam param = GetParam.builder()
                    .fileName(fileName)
                    .deviceCode(getDeviceCode(result))
                    .time(result.getDevicepatroltime())
                    .desc(result.getResultstr())
                    .result(getResult(result))
                    .build();
            return uploadData(getApi(result), param);
        }
        return false;
    }
}
