package com.shenhaoinfo.shucai_module_java.service;

import com.shenhaoinfo.shucai_module_java.bean.ApiParamNameEnum;
import com.shenhaoinfo.shucai_module_java.bean.GetParam;
import com.shenhaoinfo.shucai_module_java.bean.PatrolResult;

import java.io.FileNotFoundException;

/**
 * @author jinhang
 * @date 2023/2/9
 */
public interface UploadService {
    /**
     * 上传图片或视频资源到环茂平台
     *
     * @param filePath 文件绝对路径
     * @return 环茂平台返回的文件名
     */
    String uploadFile(String filePath) throws FileNotFoundException;

    boolean uploadData(ApiParamNameEnum api, GetParam param);

    String uploadResource(PatrolResult result) throws FileNotFoundException;

    ApiParamNameEnum getApi(PatrolResult result);

    boolean uploadPatrolResult(PatrolResult result) throws InterruptedException, FileNotFoundException;
}
