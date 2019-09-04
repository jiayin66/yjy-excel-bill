package com.yjy.model;

import java.math.BigDecimal;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecodeModel {
	@Excel(name = "����", orderNum = "0")
	private String name;
	@Excel(name = "���", orderNum = "1")
	private BigDecimal money;
	@Excel(name = "���", orderNum = "2")
	private BigDecimal allMoney;
	@Excel(name = "��һλ", orderNum = "3")
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
