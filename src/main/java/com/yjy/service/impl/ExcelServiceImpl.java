package com.yjy.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.yjy.model.User;
import com.yjy.service.ExcelService;
import com.yjy.util.ExcelTemplateExporter;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
@Service
public class ExcelServiceImpl implements ExcelService{
	@Autowired
	private ExcelTemplateExporter excelTemplateExporter;

	public Map<String, String> parseExcel()  {
//		File file=new File("user.xls");
//		InputStream is=new FileInputStream(file);
//		ImportParams params = new ImportParams();
//		params.setNeedVerfiy(true);
//		ExcelImportResult<Object> userList = ExcelImportUtil.importExcelMore(is, User.class, params);
		return null;
	}

	public void exportExcel(HttpServletResponse response) {
		List<User> userList=new ArrayList<User>();
		userList.add(new User(1,"张三"));
		//第二个参数是抬头名称
		excelTemplateExporter.exportExcel(userList, "炊事班-记账", "记账", User.class, "炊事班-记账.xls", response);
		
	}

	public void readExcel(MultipartFile file) {
		try {
			ImportParams params = new ImportParams();
			params.setNeedVerfiy(true);
			ExcelImportResult<User> resul = ExcelImportUtil.importExcelMore(file.getInputStream(), User.class,
					params);
			List<User> list = resul.getList();
			System.out.println(JSON.toJSONString(list));
		} catch (Exception e) {
			
		}
		
	}

}
