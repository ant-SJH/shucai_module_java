package com.shenhaoinfo.shucai_module_java;

import cn.hutool.core.io.checksum.crc16.CRC16Modbus;
import cn.hutool.core.util.HexUtil;
import com.shenhaoinfo.shucai_module_java.bean.ApiParamNameEnum;
import com.shenhaoinfo.shucai_module_java.bean.GetParam;
import com.shenhaoinfo.shucai_module_java.service.UploadService;
import gnu.io.NRSerialPort;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

@SpringBootTest
@Slf4j
class ShucaiModuleJavaApplicationTests {
	@Resource
	private UploadService uploadService;

	@Resource
	private SlaveStationState slaveStationState;

	@Test
	void contextLoads() {
	}

	@Test
	public void getSerialPortName() {
		Set<String> serialPortNames = NRSerialPort.getAvailableSerialPorts();
		log.info(serialPortNames.toString());
	}

	@Test
	public void connectSerialPort() {
		String serialPortName = "COM4";
		int baudRate = 9600;
		NRSerialPort serialPort = new NRSerialPort(serialPortName, baudRate);
		serialPort.connect();

		DataInputStream in = new DataInputStream(serialPort.getInputStream());
		DataOutputStream out = new DataOutputStream(serialPort.getOutputStream());

		try {
			while (!Thread.interrupted()) {
				if (in.available() > 0) {
					byte[] b = new byte[in.available()];
					int len = in.read(b);
					log.info("len={}，b={}", len, HexUtil.format(HexUtil.encodeHexStr(b, false)));
					out.write(b);
					out.flush();
				}
			}
		} catch (Exception e) {
			log.error("" , e);
		}
	}

	@Test
	public void crcCalTest() {
		byte[] source = new byte[]{0x06, 0x03 ,0x00 ,0x42, 0x00, 0x1D};
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
		a = twoByteToUnsignedInt(b[0], b[1]);
	}

	@Test
	public void testRtuJava() {
	}

	public static int twoByteToUnsignedInt(byte high,byte low){
		return ((high << 8) & 0xffff) | (low & 0x00ff);
	}

	public static byte[] intToTwoByte(int numInt){
		byte[] rest = new byte[2];
		if(numInt < -32768 || numInt > 32767){
			return null;
		}
		rest[0] = (byte)(numInt >> 8);//高8位
		rest[1] = (byte)(numInt & 0x00ff);//低8位

		return rest;
	}

	@Test
	public void uploadTest() {
		String filePath = "C:\\Users\\songj\\Desktop\\upload.jpg";
		uploadService.uploadFile(filePath);
	}

	@Test
	public void uploadTest2() {
		GetParam param = new GetParam();
		param.setResult(1);
		param.setDeviceCode("0001");
		param.setDesc("");
		param.setFileName("57B58BCB4B5246989402441D431DCAEB.jpg");
		param.setTime(new Date());
		uploadService.uploadData(ApiParamNameEnum.T01_CAPTURE_QC_STATUS, param);
	}
}
