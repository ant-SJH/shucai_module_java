package com.shenhaoinfo.shucai_module_java.bean;

public enum ApiParamNameEnum {
	/**
	 * 文件或附件上传平台
	 */
	A01_UpLoadFile("0"),
	/**
	 * 质控状态抓拍上传平台
	 */
	T01_CAPTURE_QC_STATUS("1"),
	/**
	 * 数采屏幕抓拍上传平台
	 */
	T01_CAPTURE_DATA_SCREEN("2"),
	/**
	 * 自动校准抓拍上传平台
	 */
	T01_CAPTURE_AUTO_CALIBRATION("3"),
	/**
	 * 实样比对图片上传平台
	 */
	T01_CAPTURE_ACTUAL_SAMPLE_COMPARE("4"),
	/**
	 * 试剂抽样判断上传平台
	 */
	T01_CAPTURE_REAGENT_SAMPLING("5"),
	/**
	 * 水样管脏判断结果上传平台
	 */
	T01_CAPTURE_WATER_SAMPLE_PIPE_DIRTY_JUDGE("6"),
	/**
	 * AB罐脏判断结果上传平台
	 */
	T01_CAPTURE_AB_TANK_DIRTY_JUDGE("7"),
	/**
	 * 供样罐脏判断结果上传平台
	 */
	T01_CAPTURE_SAMPLE_TANK_DIRTY_JUDGE("8"),
	/**
	 * 蠕动泵异常上传平台
	 */
	T01_CAPTURE_ABNORMAL_PERISTALTIC_PUMP("9"),
	/**
	 * 设备识别上传平台
	 */
	T01_CAPTURE_EQUIP_IDENTIFY("10"),
	/**
	 * 现场检查结果上传平台
	 */
	T01_CAPTURE_SITE_INSPECT("11"),
	/**
	 * 入侵检测告警信息上传平台
	 */
	T01_CHECK_ALARM("12"),

	/**
	 * 发送仪器锁屏或解锁反控指令
	 */
	A01_A041CD("13"),

	/**
	 * 人员入侵结束
	 */
	T01_OVER_ALARM("14");
	
	private final String deviceType;
	
	ApiParamNameEnum(String type) {
		this.deviceType = type;
	}
	
	public String getDeviceType() {
		return deviceType;
	}
	
	public static ApiParamNameEnum getByDeviceType(String type) {
		for (ApiParamNameEnum param : ApiParamNameEnum.values()) {
			if (param.getDeviceType().equals(type)) {
				return param;
			}
		}
		return null;
	}

}
