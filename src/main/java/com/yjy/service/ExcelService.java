package com.yjy.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {

	void exportExcel(HttpServletResponse response);

	void readExcel(MultipartFile file);

}
