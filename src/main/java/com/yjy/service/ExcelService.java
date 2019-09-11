package com.yjy.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {

	void exportExcel(HttpServletResponse response);

	void readExcel(MultipartFile file);

	void readJiluExcel(MultipartFile record,MultipartFile user,HttpServletResponse response,String type);

	void readJiluTxt(MultipartFile txt, MultipartFile user, HttpServletResponse response, String type);

}
