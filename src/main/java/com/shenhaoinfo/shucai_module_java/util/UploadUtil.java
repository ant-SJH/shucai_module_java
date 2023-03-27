package com.shenhaoinfo.shucai_module_java.util;

import com.shenhaoinfo.shucai_module_java.bean.PatrolResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jinhang
 * @date 2023/3/27
 */
@Slf4j
public class UploadUtil {
    public static List<PatrolResult> needUploadList = new ArrayList<>();

    public static String getDeviceCode(PatrolResult result) {
        switch (result.getDevicename()) {
            case "数采机柜#数采显示屏":
                return "001";
            case "COD分析仪#采样仪显示屏":
                return "002";
            case "COD分析仪#TN管":
                return "003";
            case "COD分析仪#水样管":
                return "004";
            case "COD分析仪#蠕动泵":
                return "005";
            case "氨氮分析仪#采样仪显示屏":
                return "006";
            case "氨氮分析仪#TN管":
                return "007";
            case "氨氮分析仪#水样管":
                return "008";
            case "氨氮分析仪#蠕动泵":
                return "009";
            case "总磷分析仪#采样仪显示屏":
                return "010";
            case "总磷分析仪#TN管":
                return "011";
            case "总磷分析仪#水样管":
                return "012";
            case "总磷分析仪#蠕动泵":
                return "013";
            case "总氮分析仪#采样仪显示屏":
                return "014";
            case "总氮分析仪#TN管":
                return "015";
            case "总氮分析仪#水样管":
                return "016";
            case "总氮分析仪#蠕动泵":
                return "017";
            case "水质等比例采样器#人工取样位置":
                return "018";
            case "水质等比例采样器#AB罐":
                return "019";
            case "COD分析仪#供样罐":
                return "020";
            case "氨氮分析仪#供样罐":
                return "021";
            case "总磷分析仪#供样罐":
                return "022";
            case "总氮分析仪#供样罐":
                return "023";
            default:
                return "";
        }
    }

    public static int getResult(PatrolResult result) {
        String[] deviceName = result.getDevicename().split("#");
        if (deviceName[1].equals("TN管")) {
            switch (deviceName[0]) {
                case "COD分析仪":
                    return "4".equals(result.getResultstr()) ? 1 : 0;
                case "氨氮分析仪":
                    return "5".equals(result.getResultstr()) ? 1 : 0;
                case "总磷分析仪":
                case "总氮分析仪":
                    return "6".equals(result.getResultstr()) ? 1 : 0;
                default:
                    return 0;
            }
        } else {
            return result.getData1() == 1 ? 1 : 0;
        }
    }
}
