package com.yjy.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@Component
public class ExcelTemplateExporter {

    public static void export(HttpServletResponse response, String tplName, String templateName) {

        OutputStream out = null;
        BufferedInputStream in = null;
        try {
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(templateName + ".xlsx", "UTF-8"));


            out = response.getOutputStream();

            Resource resource = new ClassPathResource(tplName +".xlsx");
            in = new BufferedInputStream(resource.getInputStream());

            byte[] buff = new byte[1024];
            int i = -1;
            while((i = in.read(buff)) != -1) {
                out.write(buff, 0, i);
            }

            out.flush();

            out.close();
            in.close();

        } catch (Exception e) {
            throw new RuntimeException("鑾峰彇" + templateName +"鍑洪敊锛�" + e.getMessage());
        }
    }

    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass, String fileName,
            HttpServletResponse response) {

        defaultExport(list, pojoClass, fileName, response, new ExportParams(title, sheetName));
    }

    private static void defaultExport(List<?> list, Class<?> pojoClass, String fileName, HttpServletResponse response,
            ExportParams exportParams) {
        exportParams.setStyle(ExcelStyleUtil.class);
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
        if (workbook != null){
            downLoadExcel(fileName, response, workbook);

        }

    }

    private static void downLoadExcel(String fileName, HttpServletResponse response, Workbook workbook) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // response.setHeader("Content-Disposition", "attachment;filename=" +
            // URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Content-Disposition",
                    "attachment;filename*= UTF-8''" + URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
