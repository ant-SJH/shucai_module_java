package com.shenhaoinfo.shucai_module_java.util;

import cn.hutool.core.io.checksum.crc16.CRC16Modbus;
import cn.hutool.core.util.HexUtil;

import java.util.Arrays;

/**
 * @author jinhang
 * @date 2022/10/26
 */
public class ModbusUtils {

    /**
     * 功能码读取
     */
    public static final byte FUN_READ = 0x03;
    /**
     * 功能码写入
     */
    public static final byte FUN_WRITE = 0x10;

    /**
     * 将传入的order最后两位增加crc校验
     * @param order 需要增加crc校验的指令
     */
    public static void addCrc(byte[] order) {
        if (order == null || order.length < 2) {
            return;
        }
        int length = order.length;
        byte[] o = Arrays.copyOf(order, (length-2));
        CRC16Modbus crc16Modbus = new CRC16Modbus();
        crc16Modbus.update(o);
        String hexValue = crc16Modbus.getHexValue();
        byte[] b = HexUtil.decodeHex(hexValue);
        order[length-2] = b[1];
        order[length-1] = b[0];
    }
}
