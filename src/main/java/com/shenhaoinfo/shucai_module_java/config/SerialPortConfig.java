package com.shenhaoinfo.shucai_module_java.config;

import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Bytes;
import com.shenhaoinfo.shucai_module_java.handler.ModbusHandler;
import gnu.io.NRSerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

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

    private long firstByteTime;

    private final List<Byte> orders = new ArrayList<>();

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                NRSerialPort serialPort = new NRSerialPort(serialPortName, baudRate);
                serialPort.connect();

                DataInputStream in = new DataInputStream(serialPort.getInputStream());
                DataOutputStream out = new DataOutputStream(serialPort.getOutputStream());

                while (!Thread.interrupted()) {
                    if (in.available() > 0) {
                        try {
                            byte b = (byte) in.read();
                            if (checkOrderValid(b)) {
                                byte[] bytes = Bytes.toArray(orders);
                                // 清空之前接收的信息
                                orders.clear();
                                log.info("读取到数据为：{}", HexUtil.format(HexUtil.encodeHexStr(bytes, false)));
                                byte[] reply = modbusHandler.handler(bytes);
                                log.info("返回给主站信息：{}", HexUtil.format(HexUtil.encodeHexStr(reply, false)));
                                out.write(reply);
                                out.flush();
                            }
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

    public boolean checkOrderValid(Byte b) {
        if (orders.isEmpty()) {
            firstByteTime = System.currentTimeMillis();
        } else if (orders.size() > 6){
            long gap = firstByteTime - System.currentTimeMillis();
            if (gap > 1000) {
                orders.clear();
            } else {
                // 当接收到的字符间隔时间小于1s，说明是同一条信令，检查其合法性
                orders.add(b);
                byte[] bytes = Bytes.toArray(orders);
                return modbusHandler.checkDataLegal(bytes);
            }
        }
        orders.add(b);
        return false;
    }
}
