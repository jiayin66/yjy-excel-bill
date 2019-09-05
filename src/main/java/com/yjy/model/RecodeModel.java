package com.yjy.model;

import java.math.BigDecimal;

import com.alibaba.fastjson.annotation.JSONField;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecodeModel {
	@JSONField(ordinal=1)
	@Excel(name = "姓名", orderNum = "0",width = 15)
	private String name;
	
	@JSONField(ordinal=2)
	@Excel(name = "金额", orderNum = "1",width = 15)
	private BigDecimal money;
	
	@JSONField(ordinal=3)
	@Excel(name = "余额", orderNum = "2",width = 15)
	private BigDecimal allMoney;
	
	@JSONField(ordinal=4)
	@Excel(name = "下一位", orderNum = "3",width = 30, type = 1)
	private String next;
	public RecodeModel() {
		super();
		// TODO Auto-generated constructor stub
	}
	public RecodeModel(String name, BigDecimal money, BigDecimal allMoney, String next) {
		super();
		this.name = name;
		this.money = money.multiply(new BigDecimal(-1));
		this.allMoney = allMoney;
		this.next = next;
	}
	
	
}
