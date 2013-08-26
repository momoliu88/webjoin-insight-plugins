package com.ebupt.webjoin.insight.LZW;

public class Code {
	Integer code;
	Boolean flag = false;
	public Code(Integer code,Boolean flag){
		this.code = code;
		this.flag= flag;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public Boolean getFlag() {
		return flag;
	}
	public void setFlag(Boolean flag) {
		this.flag = flag;
	}
	
}
