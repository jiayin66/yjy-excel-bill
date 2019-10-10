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
	 * ����excle��ȡ
	 * @param file
	 */
	@PostMapping("/a")
	public void readExcel(@RequestParam("file") MultipartFile file) {
		excelService.readExcel(file);
	}
	
	/**
	 * ����excle����
	 * @param response
	 */
	@GetMapping("/b")
	public void exportExcel(HttpServletResponse response) {
		excelService.exportExcel(response);
	}
	
	/**
	 * 20190903�����
	 * �����ʽ�� ����� 3 ��� 917 ��һλ��־��|��־����һλ/�� ����|����� 3  ��һλ/�� ��־�� ��� 917|����� 3 
	 * ƥ�����������2�����֣�ȡ������ͷ�����ݣ����ƥ���û����ұ�ȥ����һλ��
	 * 
	 * 
	 * ÿ�ε��������¼���û����������ͣ�����֧�����ɼ��˼�¼
	 * @param record �����¼������ȥ�����ʺ������
	 * @param user �û�����������
	 * @param type ��1��ʾ����0��ʾ������2��ʾ������������
	 * @param response
	 */
	@PostMapping("/j")
	public void readJiluExcel(@RequestParam("record") MultipartFile record,@RequestParam("user") MultipartFile user,
			@RequestParam("type") String type,HttpServletResponse response) {
		excelService.readJiluExcel(record,user,response,type);
	}
	//���ı���ʽ
	@PostMapping("/z")
	public void readJiluTxt(@RequestParam("txt") MultipartFile txt,@RequestParam("user") MultipartFile user,
			@RequestParam("type") String type,HttpServletResponse response) {
		excelService.readJiluTxt(txt,user,response,type);
	}

	
}
