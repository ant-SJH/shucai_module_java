package com.shenhaoinfo.shucai_module_java;

import cn.hutool.core.io.checksum.crc16.CRC16Modbus;
import cn.hutool.core.util.HexUtil;
import com.shenhaoinfo.shucai_module_java.bean.ApiParamNameEnum;
import com.shenhaoinfo.shucai_module_java.bean.GetParam;
import com.shenhaoinfo.shucai_module_java.service.UploadService;
import com.shenhaoinfo.shucai_module_java.util.MqttSend;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;

@SpringBootTest
@Slf4j
class ShucaiModuleJavaApplicationTests {
    @Resource
    private UploadService uploadService;

    @Resource
    private SlaveStationState slaveStationState;

    @Resource
    private MqttSend mqttSend;

    public static int twoByteToUnsignedInt(byte high, byte low) {
        return ((high << 8) & 0xffff) | (low & 0x00ff);
    }

    public static byte[] intToTwoByte(int numInt) {
        byte[] rest = new byte[2];
        if (numInt < -32768 || numInt > 32767) {
            return null;
        }
        rest[0] = (byte) (numInt >> 8);//高8位
        rest[1] = (byte) (numInt & 0x00ff);//低8位

        return rest;
    }

    @Test
    void contextLoads() {
    }

    @Test
    public void crcCalTest() {
        byte[] source = new byte[]{0x06, 0x03, 0x00, 0x42, 0x00, 0x1D};
        CRC16Modbus crc16Modbus = new CRC16Modbus();
        crc16Modbus.update(source);
        String hexValue = crc16Modbus.getHexValue();
        byte[] b = HexUtil.decodeHex(hexValue);
        System.out.println(Arrays.toString(b));
    }

    @Test
    public void getSlaveStationState() {
        byte[] b = new byte[]{0x01, 0x03, 0x00, 0x01, 0x00, 0x02, (byte) 0x95, (byte) 0xCB};
        byte[] data = slaveStationState.readStationState(b);
        System.out.println(Arrays.toString(data));
    }

    @Test
    public void intToByte() {
        int a = 1;
        byte[] b = intToTwoByte(a);
        assert b != null;
        twoByteToUnsignedInt(b[0], b[1]);
    }

    @Test
    public void testRtuJava() {
    }

    @Test
    public void uploadTest() throws FileNotFoundException {
        String filePath = "C:\\Users\\songj\\Desktop\\upload.jpg";
        uploadService.uploadFile(filePath);
    }

    @Test
    public void uploadTest2() {
        GetParam param = GetParam.builder().fileName("57B58BCB4B5246989402441D431DCAEB.jpg").deviceCode("0001").time(new Date()).desc("").result(1).build();
        uploadService.uploadData(ApiParamNameEnum.T01_CAPTURE_QC_STATUS, param);
    }

    @Test
    public void mqttSend() {
        for (int i = 0; i < 5000; i++) {
            mqttSend.sendMessage("123");
        }
    }
}
