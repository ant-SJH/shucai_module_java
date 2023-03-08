package com.shenhaoinfo.shucai_module_java.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadResult implements Serializable {
	
	private String newFileName;
	
	private String oldFileName;

}
