package com.knowology.km.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.knowology.km.dal.Database;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ReadExcel {
	@SuppressWarnings("unchecked")
	public static boolean writeExcel(String dirPath, String fileName,
			String sheetTitle, String title, List columnTitle, List text) {
		WritableWorkbook workBook = null;
		WritableSheet sheet = null;
		if (dirPath == null || "".equals(dirPath) || fileName == null
				|| "".equals(fileName)) {
			System.out.println("建立excel文件失败：路径或文件名为空");
			return false;
		}
		File filePath = new File(dirPath);
		{
			if (!filePath.exists()) {
				// 如果文件要保存的目录不存在则产生该目录
				if (!filePath.mkdirs()) {
					System.out.println("建立excel文件失败：无法建立该目录");
					return false;
				}
			}
		}
		// 在该目录下产生要保存的文件名
		String excelPath = dirPath + "/" + fileName + ".xls";
		File excelFile = new File(excelPath);
		// 以下开始输出到EXCEL
		try {
			if (!excelFile.exists()) {
				if (!excelFile.createNewFile()) {
					System.out.println("建立excel文件失败：建立excel文件发生异常");
					return false;
				}
			}
			/** **********创建工作簿************ */
			workBook = Workbook.createWorkbook(excelFile);
			/** **********创建工作表************ */
			if ("".equals(sheetTitle) || sheetTitle == null) {
				sheet = workBook.createSheet("Sheet1", 0);
			} else {
				sheet = workBook.createSheet(sheetTitle, 0);
			}
			// SheetSettings sheetSet = sheet.getSettings();
			// sheetSet.setProtected(false);
			/** ************设置单元格字体************** */
			WritableFont headFont = new WritableFont(WritableFont.ARIAL, 14);
			WritableFont normalFont = new WritableFont(WritableFont.ARIAL, 12);
			/** ************以下设置几种格式的单元格************ */
			// 用于表头
			WritableCellFormat wcf_head = new WritableCellFormat(headFont);
			wcf_head.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
			wcf_head.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
			wcf_head.setAlignment(Alignment.CENTRE); // 文字水平对齐
			wcf_head.setWrap(false); // 文字是否换行

			// 用于正文居中
			WritableCellFormat wcf_center = new WritableCellFormat(normalFont);
			wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
			wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
			wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
			wcf_center.setWrap(false); // 文字是否换行

			/** ************单元格格式设置完成****************** */

			/** ***************以下是报表的内容********************* */
			// 合并单元格设置excel的题目
			int x = 0;
			if ("".equals(title) || title == null) {
				title = "";
				x = 0;
			} else {
				if (columnTitle != null && columnTitle.size() > 0) {
					sheet.mergeCells(0, 0, columnTitle.size() - 1, 0);
					sheet.addCell(new Label(0, 0, title, wcf_head));
					x = 1;
				} else if (text != null && text.size() > 0
						&& ((List) text.get(0)).size() > 0) {
					sheet.addCell(new Label(0, ((List) text.get(0)).size() - 1,
							title, wcf_head));
					x = 1;
				} else {
					sheet.addCell(new Label(0, 0, title, wcf_head));
					x = 1;
				}
			}
			// 设置列名
			if (columnTitle != null && columnTitle.size() > 0) {
				for (int i = 0; i < columnTitle.size(); i++) {
					sheet.addCell(new Label(i, x, String.valueOf(columnTitle
							.get(i)), wcf_center));
				}
				x = x + 1;
			}

			// 写入正文数据
			if (text != null && text.size() > 0) {
				for (int i = 0; i < text.size(); i++) {
					List content = (List) text.get(i);
					if (content != null && content.size() > 0) {
						for (int j = 0; j < content.size(); j++) {
							String data = String.valueOf(content.get(j));
							// 判断数据类型
							if (content.get(j) instanceof String) {
								sheet.addCell(new Label(j, x + i, data,
										wcf_center));
							} else if (content.get(j) instanceof Integer) {
								Number numberLabel = new Number(j, x + i,
										Integer.parseInt(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Float) {
								Number numberLabel = new Number(j, x + i, Float
										.parseFloat(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Double) {
								Number numberLabel = new Number(j, x + i,
										Double.parseDouble(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Long) {
								Number numberLabel = new Number(j, x + i, Long
										.parseLong(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Short) {
								Number numberLabel = new Number(j, x + i, Short
										.parseShort(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Boolean) {
								sheet.addCell(new Label(j, x + i, data,
										wcf_center));
							} else if (content.get(j) instanceof Byte) {
								sheet.addCell(new Label(j, x + i, data,
										wcf_center));
							} else {
								try {
									Number numberLabel = new Number(j, x + i,
											Double.parseDouble(data),
											wcf_center);
									sheet.addCell(numberLabel);
								} catch (Exception ex) {
									sheet.addCell(new Label(j, x + i, data,
											wcf_center));
								}
							}
							sheet.setColumnView(j, data.length() + 10);
						}
					}
				}
			}
			/** **********以上所写的内容都是写在缓存中的，下一句将缓存的内容写到文件中******** */
			workBook.write();
		} catch (Exception e) {
			System.out.println("建立excel文件失败：" + e.getMessage());
			return false;
		} finally {
			/** *********关闭文件************* */
			try {
				if (workBook != null) {
					workBook.close();
				}
			} catch (Exception ex) {
				System.out.println("关闭文件流失败：" + ex.getMessage());
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static boolean writeExcelBySheet(String dirPath, String fileName,
			Map<String,List<String>> columnTitleMap ,Map<String,List<List<Object>>> textMap) {
		WritableWorkbook workBook = null;
		WritableSheet sheet = null;
		if (dirPath == null || "".equals(dirPath) || fileName == null
				|| "".equals(fileName)) {
			System.out.println("建立excel文件失败：路径或文件名为空");
			return false;
		}
		File filePath = new File(dirPath);
		{
			if (!filePath.exists()) {
				// 如果文件要保存的目录不存在则产生该目录
				if (!filePath.mkdirs()) {
					System.out.println("建立excel文件失败：无法建立该目录");
					return false;
				}
			}
		}
		// 在该目录下产生要保存的文件名
		String excelPath = dirPath + "/" + fileName + ".xls";
		File excelFile = new File(excelPath);
		// 以下开始输出到EXCEL
		try {
			if (!excelFile.exists()) {
				if (!excelFile.createNewFile()) {
					System.out.println("建立excel文件失败：建立excel文件发生异常");
					return false;
				}
			}
			/** **********创建工作簿************ */
			workBook = Workbook.createWorkbook(excelFile);
			/** **********创建工作表************ */
			int i = 0;
			for(Map.Entry<String, List<String>> entry : columnTitleMap.entrySet()){
				String sheetTitle = entry.getKey();
				if ("".equals(sheetTitle) || sheetTitle == null) {
					sheet = workBook.createSheet("Sheet1", i);
				} else {
					sheet = workBook.createSheet(sheetTitle, i);
				}
				writeSheet(sheet,null,entry.getValue(),textMap.get(sheetTitle));
				i++;
			}
			
			/** **********以上所写的内容都是写在缓存中的，下一句将缓存的内容写到文件中******** */
			workBook.write();
		} catch (Exception e) {
			System.out.println("建立excel文件失败：" + e.getMessage());
			return false;
		} finally {
			/** *********关闭文件************* */
			try {
				if (workBook != null) {
					workBook.close();
				}
			} catch (Exception ex) {
				System.out.println("关闭文件流失败：" + ex.getMessage());
				return false;
			}
		}
		return true;
	}
	
	private static void writeSheet(WritableSheet sheet,String title,List<String> columnTitle,List<List<Object>> text) throws WriteException {
		
		// SheetSettings sheetSet = sheet.getSettings();
		// sheetSet.setProtected(false);
		/** ************设置单元格字体************** */
		WritableFont headFont = new WritableFont(WritableFont.ARIAL, 14);
		WritableFont normalFont = new WritableFont(WritableFont.ARIAL, 12);
		/** ************以下设置几种格式的单元格************ */
		// 用于表头
		WritableCellFormat wcf_head = new WritableCellFormat(headFont);
		wcf_head.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
		wcf_head.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_head.setAlignment(Alignment.CENTRE); // 文字水平对齐
		wcf_head.setWrap(false); // 文字是否换行

		// 用于正文居中
		WritableCellFormat wcf_center = new WritableCellFormat(normalFont);
		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
		wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
		wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
		wcf_center.setWrap(false); // 文字是否换行

		/** ************单元格格式设置完成****************** */

		/** ***************以下是报表的内容********************* */
		// 合并单元格设置excel的题目
		int x = 0;
		if ("".equals(title) || title == null) {
			title = "";
			x = 0;
		} else {
			if (columnTitle != null && columnTitle.size() > 0) {
				sheet.mergeCells(0, 0, columnTitle.size() - 1, 0);
				sheet.addCell(new Label(0, 0, title, wcf_head));
				x = 1;
			} else if (text != null && text.size() > 0
					&& ((List) text.get(0)).size() > 0) {
				sheet.addCell(new Label(0, ((List) text.get(0)).size() - 1,
						title, wcf_head));
				x = 1;
			} else {
				sheet.addCell(new Label(0, 0, title, wcf_head));
				x = 1;
			}
		}
		// 设置列名
		if (columnTitle != null && columnTitle.size() > 0) {
			for (int i = 0; i < columnTitle.size(); i++) {
				sheet.addCell(new Label(i, x, String.valueOf(columnTitle
						.get(i)), wcf_center));
			}
			x = x + 1;
		}

		// 写入正文数据
		if (text != null && text.size() > 0) {
			for (int i = 0; i < text.size(); i++) {
				List content = (List) text.get(i);
				if (content != null && content.size() > 0) {
					for (int j = 0; j < content.size(); j++) {
						String data = Objects.toString(content.get(j),"");
						// 判断数据类型
						if (content.get(j) instanceof String) {
							sheet.addCell(new Label(j, x + i, data,
									wcf_center));
						} else if (content.get(j) instanceof Integer) {
							Number numberLabel = new Number(j, x + i,
									Integer.parseInt(data), wcf_center);
							sheet.addCell(numberLabel);
						} else if (content.get(j) instanceof Float) {
							Number numberLabel = new Number(j, x + i, Float
									.parseFloat(data), wcf_center);
							sheet.addCell(numberLabel);
						} else if (content.get(j) instanceof Double) {
							Number numberLabel = new Number(j, x + i,
									Double.parseDouble(data), wcf_center);
							sheet.addCell(numberLabel);
						} else if (content.get(j) instanceof Long) {
							Number numberLabel = new Number(j, x + i, Long
									.parseLong(data), wcf_center);
							sheet.addCell(numberLabel);
						} else if (content.get(j) instanceof Short) {
							Number numberLabel = new Number(j, x + i, Short
									.parseShort(data), wcf_center);
							sheet.addCell(numberLabel);
						} else if (content.get(j) instanceof Boolean) {
							sheet.addCell(new Label(j, x + i, data,
									wcf_center));
						} else if (content.get(j) instanceof Byte) {
							sheet.addCell(new Label(j, x + i, data,
									wcf_center));
						} else {
							try {
								Number numberLabel = new Number(j, x + i,
										Double.parseDouble(data),
										wcf_center);
								sheet.addCell(numberLabel);
							} catch (Exception ex) {
								sheet.addCell(new Label(j, x + i, data,
										wcf_center));
							}
						}
						sheet.setColumnView(j, data.length() + 10);
					}
				}
			}
		}
	}

	/**
	 * @param excelFileName
	 *            :excel文件的具体路径+文件名 return List(类型为两级嵌套List)
	 */
	@SuppressWarnings("unchecked")
	public static List<List<Object>> readExcel(File excelFileName) {
		InputStream stream = null;
		Workbook workBook = null;
		List data = new ArrayList();
		if (excelFileName == null || "".equals(excelFileName)) {
			System.out.println("读取excel文件失败：路径或文件名为空");
			return null;
		}
		// File file = new File(excelFileName);
		// if (!file.exists()) {
		// System.out.println("读取excel文件失败：路径或文件名不存在");
		// return null;
		// }
		try {
			stream = new FileInputStream(excelFileName);
			workBook = Workbook.getWorkbook(stream);
			Sheet sheet = workBook.getSheet(0);
			int columns = sheet.getColumns();
			int rows = sheet.getRows();
			for (int i = 0; i < rows; i++) {
				List<Object> row = new ArrayList();
				for (int j = 0; j < columns; j++) {
					Cell cell = sheet.getCell(j, i);
					String cellValue = cell.getContents();
					if (cell.getType() == CellType.NUMBER) {
						try {
							Integer intCell = Integer.valueOf(cellValue);
							row.add(intCell);
						} catch (Exception ex) {
							Double doubleCell = Double.valueOf(cellValue);
							row.add(doubleCell);
						}
					} else {
						row.add(cellValue);
					}
				}
				data.add(row);
			}
		} catch (Exception e) {
			System.out.println("读取excel文件失败：" + e.getMessage());
			return null;
		} finally {
			/** *********关闭流和工作簿************* */
			try {
				if (workBook != null) {
					workBook.close();
				}
				if (stream != null) {
					stream.close();
				}
			} catch (Exception ex) {
				System.out.println("关闭文件流失败：" + ex.getMessage());
				return null;
			}
		}
		return data;
	}

	/**
	 * 读取 office 2003 excel
	 * 
	 * @param file参数导入文件
	 * @param count 读取列数
	 * @return 读取文件后的集合
	 */
	public static List<List<List<Object>>> read2003ExcelBySheet(File file) {
		List<List<List<Object>>> result = new ArrayList<List<List<Object>>>();
		try {
			// 将导入的文件变成工作簿对象
			HSSFWorkbook hwb = new HSSFWorkbook(new FileInputStream(file));
			int sheets = hwb.getNumberOfSheets();
			for(int s = 0 ;s<sheets;s++){
				// 定义返回的集合
				List<List<Object>> list = new ArrayList<List<Object>>();
				// 定义每一行组成的集合
				List<Object> param = new ArrayList<Object>();
				// 获取工作簿的第一个sheet
				HSSFSheet sheet = hwb.getSheetAt(s);
				// 定义每一个单元格的值变量
				Object value = null;
				// 定义每一行的变量
				HSSFRow row = null;
				// 定义每一个单元格变量
				HSSFCell cell = null;
				// 读取第一行
				row = sheet.getRow(0);
				// 判断第一行是否为null
				if (row != null) {
					// 第一行不为null，循环变量第一行的每个单元格
					for (int j = 0; j <= row.getLastCellNum(); j++) {
						// 获取第j个单元格
						cell = row.getCell(j);
						// 判断第j个单元格是否为null
						if (cell == null) {
							// 为null，继续下一个单元格
							continue;
						}
						// 获取第j个单元格的值
						value = cell.getStringCellValue().trim();
						// 将值放入集合中
						param.add(value);
					}
					// 将读取的第一列组成的集合放入集合中
					list.add(param);
				}
				// 读取第一行以下的部分
				// 循环遍历当前sheet的除第一行以下的行数
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					// 获取第i行，赋值给行变量
					row = sheet.getRow(i);
					// 判断第i行是否为null
					if (row == null) {
						// 第i行为null，继续读取下一行
						continue;
					}
					int count = row.getLastCellNum();
					// 定义每一行组成的集合
					param = new ArrayList<Object>();
					int flag = 0;
					// 循环遍历每一行的列数
					for (int j = 0; j < count; j++) {
						// 获取第j个单元格
						cell = row.getCell(j);
						// 判断第j个单元格是否为null
						if (cell == null) {
							// 将null放入集合中
							param.add(null);
						} else {
							// 第j个单元格不为null，判断当前单元格的类型是什么
							switch (cell.getCellType()) {
							case XSSFCell.CELL_TYPE_STRING:// 字符串单元格
								// 获取当前单元格的值
								value = cell.getStringCellValue().trim();
								flag = 1;
								break;
							case XSSFCell.CELL_TYPE_BLANK:// 空单元格
								// 将null赋值给当前单元格的值变量
								value = null;
								break;
							default:// 缺省类型
								// 直接转换为字符串
								value = cell.toString();
								flag = 1;
							}
							// 将当前单元格的值放入集合中
							param.add(value);
						}
					}
					// 将读取的每一列组成的集合放入集合中
					if (flag == 1){
						list.add(param);
					}
				}
				result.add(list);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	/**
	 * 读取Office 2007 excel
	 * 
	 * @param file参数导入的文件
	 * @param count 读取列数
	 * @return 读取文件后的集合
	 */
	public static List<List<List<Object>>> read2007ExcelBySheet(File file) {
		List<List<List<Object>>> result = new ArrayList<List<List<Object>>>();
		try {
			// 将导入的文件变成工作簿对象
			XSSFWorkbook xwb = new XSSFWorkbook(new FileInputStream(file));
			int sheets = xwb.getNumberOfSheets();
			for(int s = 0 ;s<sheets;s++){
				// 定义返回的集合
				List<List<Object>> list = new ArrayList<List<Object>>();
				// 定义每一行组成的集合
				List<Object> param = new ArrayList<Object>();
				// 获取工作簿的第一个sheet
				XSSFSheet sheet = xwb.getSheetAt(s);
				// 定义每一个单元格的值变量
				Object value = null;
				// 定义每一行的变量
				XSSFRow row = null;
				// 定义每一个单元格变量
				XSSFCell cell = null;
				// 读取第一行
				row = sheet.getRow(0);
				// 判断第一行是否为null
				if (row != null) {
					// 第一行不为null，循环变量第一行的每个单元格
					for (int j = 0; j <= row.getLastCellNum(); j++) {
						// 获取第j个单元格
						cell = row.getCell(j);
						// 判断第j个单元格是否为null
						if (cell == null) {
							// 为null，继续下一个单元格
							continue;
						}
						// 获取第j个单元格的值
						value = cell.getStringCellValue().trim();
						// 将值放入集合中
						param.add(value);
					}
					// 将读取的第一列组成的集合放入集合中
					list.add(param);
				}
				// 读取第一行以下的部分
				// 循环遍历当前sheet的除第一行以下的行数
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					// 获取第i行，赋值给行变量
					row = sheet.getRow(i);
					// 判断第i行是否为null
					if (row == null) {
						// 第i行为null，继续读取下一行
						continue;
					}
					int count = row.getLastCellNum();
					// 定义每一行组成的集合
					param = new ArrayList<Object>();
					int flag = 0;
					// 循环遍历每一行的列数
					for (int j = 0; j < count; j++) {
						// 获取第j个单元格
						cell = row.getCell(j);
						// 判断第j个单元格是否为null
						if (cell == null) {
							// 将null放入集合中
							param.add(null);
						} else {
							// 第j个单元格不为null，判断当前单元格的类型是什么
							switch (cell.getCellType()) {
							case XSSFCell.CELL_TYPE_STRING:// 字符串单元格
								// 获取当前单元格的值
								value = cell.getStringCellValue().trim();
								flag = 1;
								break;
							case XSSFCell.CELL_TYPE_BLANK:// 空单元格
								// 将null赋值给当前单元格的值变量
								value = null;
								break;
							default:// 缺省类型
								// 直接转换为字符串
								value = cell.toString();
								flag = 1;
							}
							// 将当前单元格的值放入集合中
							param.add(value);
						}
					}
					// 将读取的每一列组成的集合放入集合中
					if (flag == 1){
						list.add(param);
					}
				}
				result.add(list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	public static List<List<Object>> readTxt(File excelFileName) {
		List<List<Object>> list = new ArrayList<List<Object>>();
		List<Object> linelist = null;
		String encoding = Database.getJDBCValues("regressqueryreadencoding");
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					excelFileName), encoding);// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			String str[];

			while ((lineTxt = bufferedReader.readLine()) != null) {
				linelist = new ArrayList<Object>();
				try {
					if ("".equals(lineTxt)) {
						continue;
					}
					if (lineTxt.indexOf("\t") != -1) {
						str = lineTxt.split("\t");
						if (str.length == 1) {
							linelist.add(str[0]);
							linelist.add("");

						} else {
							linelist.add(str[0]);
							linelist.add(str[1]);
						}
						list.add(linelist);
					}
					// System.out.println(lineTxt);
				} catch (Exception e) {
					continue;
				}

			}

			read.close();
		} catch (Exception ex) {

			return null;
		}

		return list;

	}

}
