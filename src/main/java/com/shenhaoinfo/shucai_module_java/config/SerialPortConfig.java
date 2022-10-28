package com.shenhaoinfo.shucai_module_java.config;

import cn.hutool.core.util.HexUtil;
import com.shenhaoinfo.shucai_module_java.handler.ModbusHandler;
import gnu.io.NRSerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author jinhang
 * @date 2022/10/26
 */
@Configuration
@Slf4j
public class SerialPortConfig {
    @Value("${serialPort.name:COM4}")
    private String serialPortName;

    @Value("${serialPort.baudRate:9600}")
    private Integer baudRate;

    @Resource
    private ModbusHandler modbusHandler;

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                NRSerialPort serialPort = new NRSerialPort(serialPortName, baudRate);
                serialPort.connect();

                DataInputStream in = new DataInputStream(serialPort.getInputStream());
                DataOutputStream out = new DataOutputStream(serialPort.getOutputStream());

                while (!Thread.interrupted()) {
                    if (in.available() > 6) {
                        try {
                            byte[] b = new byte[in.available()];
                            int len = in.read(b);
                            log.info("读取到数据长度：{}，数据为：{}", len, HexUtil.format(HexUtil.encodeHexStr(b, false)));
                            byte[] reply = modbusHandler.handler(b);
                            log.info("返回给主站信息：{}", HexUtil.format(HexUtil.encodeHexStr(reply, false)));
                            out.write(reply);
                            out.flush();
                        } catch (Exception e) {
                            log.error("解析信令异常！", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("接收serialPort异常！" , e);
                try {
                    Thread.sleep(3000);
                } catch (Exception ee) {
                    log.error("", ee);
                }
                init();
            }
        }).start();
    }
}
