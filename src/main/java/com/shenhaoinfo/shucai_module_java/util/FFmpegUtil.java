package com.shenhaoinfo.shucai_module_java.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author 何嘉豪
 */
public class FFmpegUtil {

    private static final Logger log = LoggerFactory.getLogger(FFmpegUtil.class);
    public static FFmpeg FFMPEG;
    public static FFmpegExecutor EXECUTOR;

    private static volatile boolean initialized = false;

    private static volatile boolean initializedFailed = false;

    public static String convertVideo2H264(String filePath) {
        //  if empty then return
        if (StringUtils.isBlank(filePath)) {
            return filePath;
        }
        // if not mp4 then return
        if (!filePath.toLowerCase().endsWith("mp4")) {
            return filePath;
        }
        // init failed then return
        if (initializedFailed) {
            return filePath;
        }
        if (!initialized) {
            synchronized (FFmpegUtil.class) {
                if (!initialized) {
                    try {
                        FFMPEG = new FFmpeg();
                        EXECUTOR = new FFmpegExecutor(FFMPEG);
                        initialized = true;
                    } catch (IOException e) {
                        log.error("初始化 ffmpeg 失败", e);
                        initializedFailed = true;
                        return filePath;
                    }
                }
            }
        }
        Path path = Paths.get(filePath);
        // judge exist
        if (!Files.exists(path)) {
            log.error("file not exist");
            return filePath;
        }
        String dest = Paths.get(path.getParent().toString(), UUID.randomUUID() + ".mp4").toString();
        FFmpegJob job = EXECUTOR.createJob(new FFmpegBuilder()
                .overrideOutputFiles(true) // Override the output if it exists
                .setInput(filePath)
                .addOutput(dest)
                .setFormat("mp4")                  // Format is inferred from filename, or can be set
                .setVideoCodec("libx264").done());
        // execute synchronized
        try {
            job.run();
            // clean file
            Files.delete(path);
            return dest;
        } catch (Exception e) {
            log.error("转换视频错误", e);
            return filePath;
        }
    }

}
