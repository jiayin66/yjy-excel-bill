package com.yjy.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yjy.service.ExcelService;

@RestController
@RequestMapping("/e")
public class ExcelController {
	@Autowired
	private ExcelService excelService;
	
	/**
	 * 测试excle读取
	 * @param file
	 */
	@PostMapping("/a")
	public void readExcel(@RequestParam("file") MultipartFile file) {
		excelService.readExcel(file);
	}
	
	/**
	 * 测试excle导出
	 * @param response
	 */
	@GetMapping("/b")
	public void exportExcel(HttpServletResponse response) {
		excelService.exportExcel(response);
	}
	
	/**
	 * 20190903日完成
	 * 建议格式： 李谨延 3 余额 917 下一位张志龙|严志凌下一位/个 镇阳|李谨延 3  下一位/个 张志龙 余额 917|李谨延 3 
	 * 匹配规则：整行有2个数字，取数字两头的数据，左边匹配用户，右边去掉下一位。
	 * 
	 * 
	 * 每次导入聊天记录，用户基础表，类型，即可支持生成记账记录
	 * @param record 聊天记录，包括去除杂质后的聊天
	 * @param user 用户基础表，更新
	 * @param type ：1表示排序，0表示不排序，2表示挑出基础数据
	 * @param response
	 */
	@PostMapping("/j")
	public void readJiluExcel(@RequestParam("record") MultipartFile record,@RequestParam("user") MultipartFile user,
			@RequestParam("type") String type,HttpServletResponse response) {
		excelService.readJiluExcel(record,user,response,type);
	}
	//用文本格式
	@PostMapping("/z")
	public void readJiluTxt(@RequestParam("txt") MultipartFile txt,@RequestParam("user") MultipartFile user,
			@RequestParam("type") String type,HttpServletResponse response) {
		excelService.readJiluTxt(txt,user,response,type);
	}

	
}
