package com.yjy.model;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserRecord {
	
	public UserRecord() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserRecord(String txtRecord2) {
		this.txtRecord=txtRecord2;
	}

	@Excel(name = "¼ÇÂ¼", orderNum = "0")
    private String txtRecord;
	
	@Excel(name = "ÐòºÅ", orderNum = "1")
	private Integer id;
}
