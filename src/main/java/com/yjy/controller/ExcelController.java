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
	
	@PostMapping("/a")
	public void readExcel(@RequestParam("file") MultipartFile file) {
		excelService.readExcel(file);
	}
	
	@GetMapping("/b")
	public void exportExcel(HttpServletResponse response) {
		excelService.exportExcel(response);
	}

}
