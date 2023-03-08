package com.shenhaoinfo.shucai_module_java.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

@Data
public class GetDataResult implements Serializable {
	
	@JSONField(name = "s_result")
	private Integer sResult;
	
	@JSONField(name = "error_desc")
	private String errorDesc;

}
