package com.shenhaoinfo.shucai_module_java.config;

import cn.hutool.core.util.HexUtil;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.google.common.primitives.Bytes;
import com.shenhaoinfo.shucai_module_java.handler.ModbusHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private SerialPort serialPort;

    private final List<Byte> orders = new ArrayList<>();

    private long firstByteTime = 0;

    @PostConstruct
    public void init() {
        SerialPort[] ports = SerialPort.getCommPorts();
        log.info("可用的串口：{}", Arrays.toString(Arrays.stream(ports).map(SerialPort::getSystemPortName).toArray()));
        int portToUse = -1;
        for (int i = 0; i < ports.length; ++i) {
            if (ports[i].getSystemPortName().contains(serialPortName)) {
                log.info("使用的串口：{}：{}，{}", ports[i].getSystemPortName(), ports[i].getDescriptivePortName(),
                        ports[i].getPortDescription());
                portToUse = i;
                break;
            }
        }
        if (portToUse < 0) {
            log.info("没有找到可用的串口！");
            return;
        }
        serialPort = ports[portToUse];
        if (!serialPort.openPort()) {
            log.info("打开串口异常！");
            return;
        } else {
            log.info("打开串口成功！");
        }
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.TWO_STOP_BITS, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 10000);
        if (serialPort.isOpen()) {
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent serialPortEvent) {
                    if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        return;//判断事件的类型
                    }
                    while (serialPort.bytesAvailable() != 0) {
                        byte[] data = new byte[1];
                        serialPort.readBytes(data, 1);
                        log.info("读取到的数据为：{}", HexUtil.format(HexUtil.encodeHexStr(data, false)));
                        if (checkOrderValid(data[0])) {
                            byte[] order = Bytes.toArray(orders);
                            orders.clear();
                            log.info("通过校验的数据为：{}", HexUtil.format(HexUtil.encodeHexStr(order, false)));
                            byte[] reply = modbusHandler.handler(order);
                            serialPort.writeBytes(reply, reply.length);
                            log.info("返回给主站信息：{}", HexUtil.format(HexUtil.encodeHexStr(reply, false)));
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public boolean checkOrderValid(Byte b) {
        if (orders.isEmpty()) {
            log.info("order为空，接收第一个字符");
            firstByteTime = System.currentTimeMillis();
            if (b != 0x01) {
                return false;
            }
        } else {
            long gap = System.currentTimeMillis() - firstByteTime;
            if (gap > 1000) {
                orders.clear();
                return checkOrderValid(b);
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
