package com.yjy.util;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.params.ExcelForEachParams;
import cn.afterturn.easypoi.excel.export.styler.IExcelExportStyler;

import org.apache.poi.ss.usermodel.*;

/**
 * 閲嶅啓IExcelExportStyler
 * @author xiongbin
 * @since 2018-11-20
 */
public class ExcelStyleUtil implements IExcelExportStyler {
	private static final short STRING_FORMAT = (short) BuiltinFormats.getBuiltinFormat("TEXT");
	private static final short FONT_SIZE_TEN = 10;
	private static final short FONT_SIZE_ELEVEN = 11;
	private static final short FONT_SIZE_TWELVE = 12;
	/**
	 * 澶ф爣棰樻牱寮�
	 */
	private CellStyle headerStyle;
	/**
	 * 姣忓垪鏍囬鏍峰紡
	 */
	private CellStyle titleStyle;
	/**
	 * 鏁版嵁琛屾牱寮�
	 */
	private CellStyle styles;

	public ExcelStyleUtil(Workbook workbook) {
		this.init(workbook);
	}

	/**
	 * 鍒濆鍖栨牱寮�
	 *
	 * @param workbook
	 */
	private void init(Workbook workbook) {
		this.headerStyle = initHeaderStyle(workbook);
		this.titleStyle = initTitleStyle(workbook);
		this.styles = initStyles(workbook);
	}

	/**
	 * 澶ф爣棰樻牱寮�
	 *
	 * @param color
	 * @return
	 */
	public CellStyle getHeaderStyle(short color) {
		return headerStyle;
	}

	/**
	 * 姣忓垪鏍囬鏍峰紡
	 *
	 * @param color
	 * @return
	 */
	public CellStyle getTitleStyle(short color) {
		return titleStyle;
	}

	/**
	 * 鏁版嵁琛屾牱寮�
	 *
	 * @param parity 鍙互鐢ㄦ潵琛ㄧず濂囧伓琛�
	 * @param entity 鏁版嵁鍐呭
	 * @return 鏍峰紡
	 */
	public CellStyle getStyles(boolean parity, ExcelExportEntity entity) {
		return styles;
	}

	/**
	 * 鑾峰彇鏍峰紡鏂规硶
	 *
	 * @param dataRow 鏁版嵁琛�
	 * @param obj     瀵硅薄
	 * @param data    鏁版嵁
	 */
	public CellStyle getStyles(Cell cell, int dataRow, ExcelExportEntity entity, Object obj, Object data) {
		return getStyles(true, entity);
	}

	/**
	 * 妯℃澘浣跨敤鐨勬牱寮忚缃�
	 */
	public CellStyle getTemplateStyles(boolean isSingle, ExcelForEachParams excelForEachParams) {
		return null;
	}

	/**
	 * 鍒濆鍖�--澶ф爣棰樻牱寮�
	 *
	 * @param workbook
	 * @return
	 */
	private CellStyle initHeaderStyle(Workbook workbook) {
		CellStyle style = getBaseCellStyle(workbook);
		style.setFont(getFont(workbook, FONT_SIZE_TWELVE, true));
		return style;
	}

	/**
	 * 鍒濆鍖�--姣忓垪鏍囬鏍峰紡
	 *
	 * @param workbook
	 * @return
	 */
	private CellStyle initTitleStyle(Workbook workbook) {
		CellStyle style = getBaseCellStyle(workbook);
		style.setFont(getFont(workbook, FONT_SIZE_ELEVEN, true));
		//鑳屾櫙鑹�
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}

	/**
	 * 鍒濆鍖�--鏁版嵁琛屾牱寮�
	 *
	 * @param workbook
	 * @return
	 */
	private CellStyle initStyles(Workbook workbook) {
		CellStyle style = getBaseCellStyle(workbook);
		style.setFont(getFont(workbook, FONT_SIZE_TEN, false));
		style.setDataFormat(STRING_FORMAT);
		return style;
	}

	/**
	 * 鍩虹鏍峰紡
	 *
	 * @return
	 */
	private CellStyle getBaseCellStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		//涓嬭竟妗�
		style.setBorderBottom(BorderStyle.THIN);
		//宸﹁竟妗�
		style.setBorderLeft(BorderStyle.THIN);
		//涓婅竟妗�
		style.setBorderTop(BorderStyle.THIN);
		//鍙宠竟妗�
		style.setBorderRight(BorderStyle.THIN);
		//姘村钩灞呬腑
		style.setAlignment(HorizontalAlignment.CENTER);
		//涓婁笅灞呬腑
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		//璁剧疆鑷姩鎹㈣
		style.setWrapText(true);
		return style;
	}

	/**
	 * 瀛椾綋鏍峰紡
	 *
	 * @param size   瀛椾綋澶у皬
	 * @param isBold 鏄惁鍔犵矖
	 * @return
	 */
	private Font getFont(Workbook workbook, short size, boolean isBold) {
		Font font = workbook.createFont();
		//瀛椾綋鏍峰紡
		font.setFontName("瀹嬩綋");
		//鏄惁鍔犵矖
		font.setBold(isBold);
		//瀛椾綋澶у皬
		font.setFontHeightInPoints(size);
		return font;
	}
}
