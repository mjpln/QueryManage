package com.knowology.common.render;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.Render;
import com.knowology.common.controller.FileController;

@SuppressWarnings("serial")
public class ExcelRender extends Render {

	@SuppressWarnings("unchecked")
	public ExcelRender(List _list, String _filename, String _exportId) {
		list = _list;
		exportId = _exportId;
		filename = _filename;
	}

	@SuppressWarnings("unchecked")
	HSSFWorkbook export(List list) {
		System.out.println("查询数据完成");
		@SuppressWarnings("unused")
		JSONObject result = FileController.exportMap.get(exportId);
		HSSFWorkbook wb = new HSSFWorkbook();

		int sheetI = 0;
		HSSFSheet sheet = wb.createSheet("Campaign");
		HSSFRow row = sheet.createRow((int) 0);
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 头部样式
		HSSFFont fontBlod = wb.createFont();// 项目名称一栏
		fontBlod.setFontName("微软雅黑");
		fontBlod.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		fontBlod.setFontHeightInPoints((short) 11);// 设置字体大小
		style.setFont(fontBlod);

		int len = list.size();
		if (len > 0) {
			Record rec = (Record) list.get(0);
			String[] columnnames = rec.getColumnNames();
			for (int j = 0; j < columnnames.length - 1; j++) {
				HSSFCell cell = row.createCell(j);
				cell.setCellValue(columnnames[j]);
				cell.setCellStyle(style);
				sheet.autoSizeColumn(j);
			}
			int rowindex = 0;
			for (int i = 0; i < len; i++) {
				if (i % 65535 == 0 && i > 0) {
					rowindex = 0;
					sheetI++;
					sheet = wb.createSheet("Campaign" + sheetI);
					row = sheet.createRow((int) 0);
					row.setRowStyle(style);
					// 新sheet重新生成头
					for (int j = 0; j < columnnames.length - 1; j++) {
						HSSFCell cell = row.createCell(j);
						cell.setCellValue(columnnames[j]);
						cell.setCellStyle(style);
						sheet.autoSizeColumn(j);
					}
				}
				row = sheet.createRow(rowindex + 1);
				rowindex++;
				rec = (Record) list.get(i);
				for (int j = 0; j < columnnames.length - 1; j++) {
					Object obj = rec.get(columnnames[j]);
					String str = "";
					if (obj != null) {
						str = obj.toString();
					}
					row.createCell(j).setCellValue(str);
				}
			}
		}
		System.out.println("生成数据完成！");
		return wb;
	}

	@SuppressWarnings("unchecked")
	List list;
	String filename;
	String exportId;

	@Override
	public void render() {
		synchronized (this) {
			HSSFWorkbook wb = export(list);
			JSONObject result = FileController.exportMap.get(exportId);
			int use = result.getIntValue("use");
			System.out.println(use + "use");
			if (use < 2) {
				list = null;
				System.gc();
				// FileController.exportMap.remove(exportId);
			} else {
				System.out.println("不能清除！" + use);
				result.put("use", use - 1);
			}
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-disposition", "attachment;filename="
					+ filename);
			OutputStream ouputStream = null;
			try {
				System.out.println("数据开始写入流...");
				ouputStream = response.getOutputStream();
				wb.write(ouputStream);
				result.put("state", 4);

				System.out.println("数据写入流完成");
				wb = null;
				System.gc();
				ouputStream.flush();
				System.out.println("流下载中...");
				ouputStream.close();//
				System.out.println("关闭请求和流文件");
				if (use < 2) {
					FileController.exportMap.remove(exportId);
				}
			} catch (IOException e) {
				try {
					wb = null;
					System.gc();
					ouputStream.close();
				} catch (IOException e1) {
					System.out.println(filename + "导出过程被终止！" + exportId);
				}
				e.printStackTrace();
			} finally {
				result.put("state", 4);
				if (use < 2) {
					FileController.exportMap.remove(exportId);
				}
			}
		}
	}
}