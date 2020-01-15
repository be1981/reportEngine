package com.cvicse.inforreport.exporter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFTextbox;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;

import com.cvicse.inforreport.api.IReportExporter;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.exporter.chart.ChartExporter;
import com.cvicse.inforreport.model.Body;
import com.cvicse.inforreport.model.Cell;
import com.cvicse.inforreport.model.DynamicChart;
import com.cvicse.inforreport.model.Format;
import com.cvicse.inforreport.model.Grid;
import com.cvicse.inforreport.model.GridCell;
import com.cvicse.inforreport.model.GridRow;
import com.cvicse.inforreport.model.IRColor;
import com.cvicse.inforreport.model.IRFont;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.PageSetup;
import com.cvicse.inforreport.model.ReportData;
import com.cvicse.inforreport.model.Row;
import com.cvicse.inforreport.model.Style;
import com.cvicse.inforreport.model.Summary;
import com.cvicse.inforreport.model.TBorder;
import com.cvicse.inforreport.model.Table;
import com.cvicse.inforreport.model.Title;
import com.cvicse.inforreport.util.JDataType;
import com.lowagie.text.Image;

public class XLSReportExporter implements IReportExporter {

	private static final long serialVersionUID = 1L;

	private static int IsForeColor = 0;

	private static int IsBackColor = 1;

	private HSSFWorkbook wb;

	private Hashtable hssfFontHash;

	private short colorIndex;

	public XLSReportExporter() {
		hssfFontHash = new Hashtable();
		colorIndex = 8;
	}

	public void exportReport(InforReport report, OutputStream result)
			throws ReportException {
		exportReport(null, report, result);
	}

	public void exportReport(PageSetup pagesetup, InforReport report,
			OutputStream result) throws ReportException {
		try {
			// 创建工作簿
			wb = new HSSFWorkbook();
			// 创建工作表
			HSSFSheet sheet = wb.createSheet();
			wb.setSheetName(0, "sheet1");
			String reportTitle = report.getReportTitle();
			if ((reportTitle != null) && (!"".equals(reportTitle))) {
				wb.setSheetName(0, reportTitle);
			}

			// 2009.03.25重写，用grid来导出
			ReportData wrapper = report.getWrapper();
			if (wrapper == null) {
				wrapper = new ReportData();
				wrapper.setGrid(report);
			}
			Grid grid = wrapper.getGrid();
			// EngineUtils.Doc2XmlFile(wrapper.getGridDocument(), "GBK",
			// "C:/xx.xml");

			// 2009.06.03创建一个全局patriarch，导图片和画线用
			HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

			// 创建行，并设置行高
			List rowsHeightList = report.getRowsHeight();
			for (int i = 0; i < rowsHeightList.size(); i++) {
				String rowHeightS = String.valueOf(rowsHeightList.get(i));
				int rowHeight = Integer.parseInt(rowHeightS);
				HSSFRow row = sheet.createRow(i);
				// row.setHeightInPoints((short)(int)(rowHeight * 72 / 96));
				row.setHeight((short) (int) (rowHeight * 15.207675329298683D));
			}

			// 设置列宽
			List colsWidthList = report.getColsWidth();
			for (short j = 0; j < colsWidthList.size(); j++) {
				int colWidth = Integer.parseInt(String.valueOf(colsWidthList
						.get(j)));
				// sheet.setColumnWidth(j, (int)(colWidth *
				// 34.600000000000001D));
				sheet.setColumnWidth(j, (int) (colWidth * 36.534161490683231D));
			}

			// 先把所有xlscell全创建出来 避免处理合并时出错
			HSSFRow xlsrow;
			HSSFCell xlscell;
			for (int i = 0; i < grid.totalrow; i++) {
				xlsrow = sheet.getRow(i);
				for (int j = 0; j < grid.totalcol; j++) {
					xlscell = xlsrow.createCell(j);
				}
			}
			
			//存储模板中的cellstyle，给扩展出的cell用，减少HSSFCellStyle数量
			List<HSSFCellStyle[]> styleList = new ArrayList();
			List<Row> srcrows = report.getSrcRows();
			HSSFCellStyle[] stylerow;
			for(Row srcrow:srcrows){
				List<Cell> cells = null;
				if(null!=srcrow) //有图表时会出现空行和空cell的情况，所以这里要加判断20101214
					cells= srcrow.getCells();
				stylerow = new HSSFCellStyle[report.getColsCount()];
				int colno=0;
				//System.out.println("~~~~");
				if(null!=cells){ //同上20101214
					for(int i =0;i<cells.size();i++){
						Cell cell = cells.get(i);
						//System.out.print(i+"===: "+cell.getCellTop()+","+cell.getCellLeft()+";/");
						
						Style cellStyle = cell.getCellStyle();
						// 处理数字格式
						HSSFCellStyle style = wb.createCellStyle();
						Format cellFormat = cell.getCellFormat();
						exportStyle(cellStyle, cellFormat, style);
						if(cell.getCellLeft()>0)						
							stylerow[cell.getCellLeft()-1]=style;
						
					}
				}
				styleList.add(stylerow);
			}

			// 逐行导出
			GridRow[] arrGridRows = grid.arrGridRows;
			for (int i = 0; i < grid.totalrow; i++) {
				GridRow gridrow = arrGridRows[i];
				GridCell[] arrGridCells = gridrow.arrGridCells;
				xlsrow = sheet.getRow(i);
				for (int j = 0; j < grid.totalcol; j++) {
					GridCell gridcell = arrGridCells[j];
					xlscell = xlsrow.getCell(j);
					// report的cell和Excel的cell数组下标都是从0开始的，调用的方法参数是实际坐标，所以要+1
					// 2009.06.03斜线cell分开处理
					Style gcstyle = gridcell.style;
					boolean flagslash = false;
					if (gcstyle != null) {
						TBorder gcsb = gcstyle.getBorder("slash");
						if (gcsb != null
								&& !gcsb.borderType.equalsIgnoreCase("none")
								&& !gcsb.borderWeight.equals("0")) {
							flagslash = true;
						}
					}
					if (!flagslash) {
						HSSFCellStyle style =null;
						if(styleList.size()>0 && gridcell.rowInTemp>0){
							if(gridcell.colInTemp>report.getColsCount())
								gridcell.colInTemp=report.getColsCount();
							int gridcellrowindex = gridcell.rowInTemp-1;
							if(gridcellrowindex>=styleList.size())
								gridcellrowindex = styleList.size()-1;
							HSSFCellStyle[] row = styleList.get(gridcellrowindex);
							int gridcellindex = gridcell.colInTemp-1;
							if(gridcell.colInTemp>=row.length)
								gridcellindex = row.length-1;
							style = row[gridcellindex];
						
						
							exportCell(gridcell, i + 1, j + 1, sheet, xlscell,style);
						}
					} else {
						exportSlashCell(gridcell, i + 1, j + 1, sheet, xlscell,
								patriarch);
					}
				}
			}

			// 处理image
			exportImages(report, grid, patriarch);

			// 图表
			exportCharts(report, patriarch);

			// 页面设置
			exportPageSetup(pagesetup == null ? report.getPagesetup()
					: pagesetup, sheet);
			wb.write(result);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ReportException(e.getMessage());
		}

	}

	/**
	 * 导出单元格
	 * 
	 * @param srcCell
	 *            扩展后的report实际cell
	 * @param top
	 *            report cell的行，相当于list序号+1
	 * @param left
	 *            report cell的列，相当于list序号+1
	 * @param sheet
	 *            已经定义好的sheet
	 * @param cell
	 *            已经创建好的Excel的cell
	 * @param rowStyle
	 *            行风格
	 */
	private void exportCell(GridCell gridcell, int top, int left,
			HSSFSheet sheet, HSSFCell xlscell, HSSFCellStyle style) {

		// 如果是被合并的，跳出
		if (gridcell.merged)
			return;

		// 设置单元格风格 //2010-9-3注释掉，style提到循环外处理
//		Style cellStyle = (Style) gridcell.style;

		// 处理数字格式
//		HSSFCellStyle style = wb.createCellStyle();
//		Format cellFormat = gridcell.cellformat;
//		exportStyle(cellStyle, cellFormat, style);
		if(null!=style)
			xlscell.setCellStyle(style);
		

		// 设置单元格内容
		String cellContent = gridcell.cellText;
		String ct = gridcell.dataType;

		// 单元格的类型
		if ((ct == null)
				|| (ct != null && !ct.equals("image") && !ct.equals("Dyimage"))) { // 非pic的cell
			// CELL_TYPE_BLANK 空值 CELL_TYPE_BOOLEAN 布尔型 CELL_TYPE_ERROR 错误
			// CELL_TYPE_FORMULA 公式型
			// CELL_TYPE_STRING 字符串型 CELL_TYPE_NUMERIC 数值型
			if (cellContent != null && !cellContent.equals("")) {
				if (JDataType.isNumericEx(cellContent) == true) {
					if(cellContent.length()>11){ //大于11位excel会默认显示成科学计数法，需要转换为string类型且设置style---2016.11.14
						HSSFCellStyle cellstyle = wb.createCellStyle();
						HSSFDataFormat format = wb.createDataFormat();
						cellstyle.setDataFormat(format.getFormat("@"));
						xlscell.setCellStyle(cellstyle);						
					    xlscell.setCellType(HSSFCell.CELL_TYPE_STRING);
					    xlscell.setCellValue(new HSSFRichTextString(cellContent));					    
					}else{
						xlscell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						xlscell.setCellValue(Double.parseDouble(cellContent));
					}
				} else if (JDataType.isDate(cellContent)) {
					xlscell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					xlscell
							.setCellValue(new Date(cellContent
									.replace("-", "/")));
				} else if (JDataType.isTime(cellContent)) { // TODO
					StringTokenizer st = new StringTokenizer(cellContent, ":");
					int c = st.countTokens();
					String s = "";
					if (c == 2) {
						s = "HH:mm";
					} else if (c == 3) {
						s = "HH:mm:ss";
					}
					try {
						SimpleDateFormat sdf = new SimpleDateFormat(s);
						Date d = sdf.parse(cellContent);
						xlscell.setCellValue(d);
					} catch (Exception ex) {
					}
					// cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					// cell.setCellValue(cellContent);
				} else if (JDataType.isBool(cellContent) == true) {
					xlscell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
					xlscell.setCellValue(Boolean.parseBoolean(cellContent));
				} else {
					xlscell.setCellType(HSSFCell.CELL_TYPE_STRING);
					xlscell.setCellValue(new HSSFRichTextString(cellContent));
				}
			} else {
				xlscell.setCellType(HSSFCell.CELL_TYPE_BLANK);
				xlscell.setCellValue(new HSSFRichTextString(cellContent));
			}
		}

		// 处理合并单元格
		int colspan = gridcell.colSpan;
		int rowspan = gridcell.rowSpan;
		if ((colspan > 1) || (rowspan > 1)) {
			// 创建合并区域
			Region region = new Region((top - 1), (short) (left - 1), (top
					+ rowspan - 2), (short) (left + colspan - 2));
			sheet.addMergedRegion(region);
			// 把被合并的xlscell都设上style
			if(null!=style){
				for (int i = top; i <= top + rowspan - 1; i++) {
					HSSFRow xlsrow = sheet.getRow(i - 1);
					for (int j = left; j <= left + colspan - 1; j++) {
						HSSFCell cell = xlsrow.getCell(j - 1);
						cell.setCellStyle(style);
					}
				}
			}
		}
	}

	/**
	 * 
	 * 导出斜线单元格
	 * 
	 * @param report
	 * @param sheet
	 */
	private void exportSlashCell(GridCell gridcell, int top, int left,
			HSSFSheet sheet, HSSFCell xlscell, HSSFPatriarch patriarch) {
		// 2009.06.03 专门处理斜线cell TODO
		// 设置单元格风格
		Style cellStyle = (Style) gridcell.style;

		// 处理数字格式
		HSSFCellStyle style = wb.createCellStyle();
		Format cellFormat = gridcell.cellformat;
		exportStyle(cellStyle, cellFormat, style);
		xlscell.setCellStyle(style);

		// 设置单元格内容
		String cellContent = gridcell.cellValue;
		xlscell.setCellType(HSSFCell.CELL_TYPE_STRING);
		xlscell.setCellValue(new HSSFRichTextString());

		// 处理合并单元格
		int colspan = gridcell.colSpan;
		int rowspan = gridcell.rowSpan;
		if ((colspan > 1) || (rowspan > 1)) {
			// 创建合并区域
			Region region = new Region((top - 1), (short) (left - 1), (top
					+ rowspan - 2), (short) (left + colspan - 2));
			sheet.addMergedRegion(region);
			// 把被合并的xlscell都设上style
			for (int i = top; i <= top + rowspan - 1; i++) {
				HSSFRow xlsrow = sheet.getRow(i - 1);
				for (int j = left; j <= left + colspan - 1; j++) {
					HSSFCell cell = xlsrow.getCell(j - 1);
					cell.setCellStyle(style);
				}
			}
		}

		// 处理斜线
		String allBorderColor = cellStyle.getallBorderColor();
		TBorder slashBorder = cellStyle.getBorder("slash");
		String borderPosition = slashBorder.borderPosition;
		String borderType = slashBorder.borderType;
		String borderColor = slashBorder.borderColor;
		String borderWeight = slashBorder.borderWeight;
		int weight = Integer.parseInt(borderWeight);

		// 线型
		int lineType = HSSFSimpleShape.LINESTYLE_NONE;
		if (borderType.equalsIgnoreCase("solid")) {
			lineType = HSSFSimpleShape.LINESTYLE_SOLID;
		} else if (borderType.equalsIgnoreCase("dash")) {
			lineType = HSSFSimpleShape.LINESTYLE_DASHSYS;
		} else if (borderType.equalsIgnoreCase("dot")) {
			lineType = HSSFSimpleShape.LINESTYLE_DOTSYS;
		} else if (borderType.equalsIgnoreCase("dashdot")) {
			lineType = HSSFSimpleShape.LINESTYLE_DASHDOTSYS;
		}

		// 颜色
		if (!(borderColor != null && !borderColor.equals(""))) {
			borderColor = allBorderColor;
		}
		Color color = IRColor.getColorByNodeValue(borderColor);

		HSSFClientAnchor anchor = new HSSFClientAnchor();
		HSSFClientAnchor anchor1 = new HSSFClientAnchor();
		HSSFClientAnchor anchor2 = new HSSFClientAnchor();
		HSSFSimpleShape shape;
		HSSFSimpleShape shape1;
		HSSFTextbox textbox;
		
		
		anchor.setAnchor((short) (left - 1), (top - 1), 0, 0, (short) (left
				+ colspan - 2), (top + rowspan - 2), 1023, 255);
		String[] subcontent = (null==cellContent)?new String[]{"","",""}:cellContent.split("/");
		if (borderPosition.equalsIgnoreCase("topDown")) {
			// 参数的范围是dx2 0-1023 dy2 0-255
//			anchor.setAnchor((short) (left - 1), (top - 1), 0, 0, (short) (left
//					+ colspan - 2), (top + rowspan - 2), 1023, 255);
			shape = patriarch.createSimpleShape(anchor);
			shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
			shape.setLineStyle(lineType);
			shape.setLineStyleColor(color.getRed(), color.getGreen(), color
					.getBlue());
			shape.setLineWidth(HSSFSimpleShape.LINEWIDTH_ONE_PT * weight);

			// 左下角内容
			HSSFRichTextString s = new HSSFRichTextString(subcontent[0]);
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_BOTTOM);
			
			//右上角内容
			s = new HSSFRichTextString(subcontent.length>1?subcontent[1]:"");
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setHorizontalAlignment(HSSFTextbox.HORIZONTAL_ALIGNMENT_RIGHT);
			
		} else if (borderPosition.equalsIgnoreCase("aggregation")) { // 聚合			
			//斜线
			anchor2.setAnchor((short) (left - 1), (top - 1), 1023 / 2, 0,
					(short) (left + colspan - 2), (top + rowspan - 2), 1023,
					255);
			shape = patriarch.createSimpleShape(anchor2);
			shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
			shape.setLineStyle(lineType);
			shape.setLineStyleColor(color.getRed(), color.getGreen(), color
					.getBlue());
			shape.setLineWidth(HSSFSimpleShape.LINEWIDTH_ONE_PT * weight);

			anchor1.setAnchor((short) (left - 1), (top - 1), 0, 255 / 2,
					(short) (left + colspan - 2), (top + rowspan - 2), 1023,
					255);
			shape1 = patriarch.createSimpleShape(anchor1);
			shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
			shape1.setLineStyle(lineType);
			shape1.setLineStyleColor(color.getRed(), color.getGreen(), color
					.getBlue());
			shape1.setLineWidth(HSSFSimpleShape.LINEWIDTH_ONE_PT * weight);	

			// 左下角内容
			HSSFRichTextString s = new HSSFRichTextString(subcontent[0]);
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_BOTTOM);
			
			//左上角内容
			s = new HSSFRichTextString(subcontent.length>1?subcontent[1]:"");
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_TOP);
			
			//右上角内容
			s = new HSSFRichTextString(subcontent.length>2?subcontent[2]:"");
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setHorizontalAlignment(HSSFTextbox.HORIZONTAL_ALIGNMENT_RIGHT);
			
		} else if (borderPosition.equalsIgnoreCase("radiation")) { // 发散			
			//斜线
			anchor2.setAnchor((short) (left - 1), (top - 1), 0, 0, (short) (left
					+ colspan - 2), (top + rowspan - 2), 1023, 255 / 2);
			shape = patriarch.createSimpleShape(anchor2);
			shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
			shape.setLineStyle(lineType);
			shape.setLineStyleColor(color.getRed(), color.getGreen(), color
					.getBlue());
			shape.setLineWidth(HSSFSimpleShape.LINEWIDTH_ONE_PT * weight);

			anchor1.setAnchor((short) (left - 1), (top - 1), 0, 0,
					(short) (left + colspan - 2), (top + rowspan - 2),
					1023 / 2, 255);
			shape1 = patriarch.createSimpleShape(anchor1);
			shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
			shape1.setLineStyle(lineType);
			shape1.setLineStyleColor(color.getRed(), color.getGreen(), color
					.getBlue());
			shape1.setLineWidth(HSSFSimpleShape.LINEWIDTH_ONE_PT * weight);
			
			// 左下角内容
			HSSFRichTextString s = new HSSFRichTextString(subcontent[0]);
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_BOTTOM);
			
			//右下角内容
			s = new HSSFRichTextString(subcontent.length>1?subcontent[1]:"");
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setVerticalAlignment(HSSFTextbox.VERTICAL_ALIGNMENT_BOTTOM);
			textbox.setHorizontalAlignment(HSSFTextbox.HORIZONTAL_ALIGNMENT_RIGHT);
			
			//右上角内容
			s = new HSSFRichTextString(subcontent.length>2?subcontent[2]:"");
			s.applyFont(style.getFont(wb));
			textbox = patriarch.createTextbox(anchor);
			textbox.setString(s);
			textbox.setNoFill(true);
			textbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
			textbox.setHorizontalAlignment(HSSFTextbox.HORIZONTAL_ALIGNMENT_RIGHT);
			
		}
	}

	/**
	 * 
	 * 导出报表image区域
	 * 
	 * @param report
	 * @param grid
	 * @param excel
	 *            patriarch
	 */
	private void exportImages(InforReport report, Grid grid,
			HSSFPatriarch patriarch) {
		try {
			GridRow[] arrGridRows = grid.arrGridRows;
			for (int i = 0; i < grid.totalrow; i++) {
				GridRow gridrow = arrGridRows[i];
				GridCell[] arrGridCells = gridrow.arrGridCells;
				for (int j = 0; j < grid.totalcol; j++) {
					GridCell gridcell = arrGridCells[j];
					String ct = gridcell.dataType;
					byte[] b = null;
					if (ct != null && ct.equals("image")) {
						b = (byte[]) report.getImages().get(gridcell.cellValue);
					} else if (ct != null && ct.equals("Dyimage")) {
						b = (byte[]) gridcell.Pic;
					}
					//System.out.println(b);
					if (b != null) {
						int rowspan = gridcell.rowSpan;
						int colspan = gridcell.colSpan;
						HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0,
								1023, 255, (short) j, i,
								(short) (j + colspan - 1), (i + rowspan - 1));
						// 参数的范围是dx2 0-1023 dy2 0-255
						anchor.setAnchorType(2);
						// 插入图片
						patriarch.createPicture(anchor, wb.addPicture(b,
								HSSFWorkbook.PICTURE_TYPE_JPEG));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 导出图表区域
	 * 
	 * @param report
	 * @param patriarch
	 */
	private void exportCharts(InforReport report, HSSFPatriarch patriarch) {
		try {
			// 图表导出图片
			DynamicChart[] charts = report.getBody().getCharts();
			//ByteArrayOutputStream output = new ByteArrayOutputStream();
			ByteArrayOutputStream output;
			for (DynamicChart chart : charts) {
				int height = chart.getHeight();
				int width = chart.getWidth();
				int top = chart.getRealTop() - 1;
				int left = chart.getRealLeft() - 1;
				output = new ByteArrayOutputStream();
				ChartExporter.exportChart(chart, output);
				byte[] b = output.toByteArray();
				if (b != null) {
					HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023,
							255, (short) left, top, (short) (left + width - 1),
							(top + height - 1));
					// 参数的范围是dx2 0-1023 dy2 0-255
					anchor.setAnchorType(2); //2=ClientAnchor.MOVE_DONT_RESIZE
					// 插入图片
					patriarch.createPicture(anchor, wb.addPicture(b,
							HSSFWorkbook.PICTURE_TYPE_PNG));
				}
				output.flush();
				output.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 处理单元格风格
	 * 
	 * @param cellStyle
	 * @param cellFormat
	 * @param style
	 */
	private void exportStyle(Style cellStyle, Format cellFormat,
			HSSFCellStyle style) {
		Color color;

		if (cellStyle != null) {
			 // 2010.05.20无论是否自适应行高都按照自动换行导出
			String autoWrap = cellStyle.getAutoWrap();
			String fixCellHeight = cellStyle.getFixCellHeight();
			if (("true".equalsIgnoreCase(autoWrap))
					|| ("true".equalsIgnoreCase(fixCellHeight))) {
				style.setWrapText(true);
			} else {
				style.setWrapText(false);
			}

			// 背景色
			String backColor = cellStyle.getBackColor();
			color = IRColor.getColorByNodeValue(backColor);
			short cIndex = getNearestColor(color, IsBackColor);
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			style.setFillForegroundColor(cIndex);
			style.setFillBackgroundColor(cIndex);

			// 边框
			exportBorder(cellStyle, style);

			// 水平对齐
			String hAlign = cellStyle.getHalign().trim();
			if (hAlign.equals("left")) {
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			} else if (hAlign.equals("center")) {
				style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			} else if (hAlign.equals("right")) {
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			} else {
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			}

			// 垂直对齐
			String vAlign = cellStyle.getValign().trim();
			if (vAlign.equals("top")) {
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
			} else if (vAlign.equals("middle")) {
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			} else if (vAlign.equals("bottom")) {
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
			} else {
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
			}

			// 创建字体
			IRFont irFont = cellStyle.getFont();
			if (irFont != null) {
				HSSFFont font = createHSSFFont(wb, irFont);
				style.setFont(font);
			}

		} else {
			// 默认风格
			setDefaultStyle(style);
		}

		// 数字显示格式
		if (cellFormat != null) {
			exportCellFormat(style, cellFormat);
		}
	}

	/**
	 * 设置默认风格
	 * 
	 * @param style
	 */
	private void setDefaultStyle(HSSFCellStyle style) {
		style.setWrapText(false);
		style.setFillPattern(HSSFCellStyle.NO_FILL);
		style.setFillForegroundColor(HSSFColor.AUTOMATIC.index);
		style.setFillBackgroundColor(HSSFColor.AUTOMATIC.index);
		style.setBorderBottom(HSSFCellStyle.BORDER_NONE);
		style.setBorderLeft(HSSFCellStyle.BORDER_NONE);
		style.setBorderRight(HSSFCellStyle.BORDER_NONE);
		style.setBorderTop(HSSFCellStyle.BORDER_NONE);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		IRFont irFont = new IRFont();
		HSSFFont font = createHSSFFont(wb, irFont);
		style.setFont(font);
	}

	/**
	 * 处理字体
	 * 
	 * @param wb
	 * @param font
	 * @return
	 */
	private HSSFFont createHSSFFont(HSSFWorkbook wb, IRFont font) {
		if (font == null)
			return null;

		// 由于HSSFWorkbook中最多只能创建32767种不同字体，所以对于已存在的字体，不能重新创建
		HSSFFont hssffont = null;
		Enumeration enumeration = hssfFontHash.keys();
		do {
			if (!enumeration.hasMoreElements())
				break;
			Object obj = enumeration.nextElement();
			if (!font.equals(obj))
				continue;
			hssffont = (HSSFFont) hssfFontHash.get(obj);
			break;
		} while (true);

		// font不存在，则创建新的字体
		if (hssffont == null) {
			hssffont = wb.createFont();
			hssfFontHash.put(font, hssffont);

			// fontsize
			hssffont.setFontHeightInPoints((short) (Float.parseFloat(font
					.getFontSize())));

			// fontname
			String fn = font.getFontName();
			hssffont.setFontName(fn);

			// charset
			// 如果是中文字体必须把charset设为134 英文的设为默认
			if (fn.getBytes().length != fn.length()) {
				hssffont.setCharSet((byte) 134);
			} else {
				hssffont.setCharSet(HSSFFont.DEFAULT_CHARSET);
			}

			// 粗体
			if (font.getIsBold() == null) {
				hssffont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			} else {
				if (font.getIsBold().equals("true")) {
					hssffont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				} else {
					hssffont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
				}
			}
			// 删除线
			if (font.getIsStrikeThrough() == null) {
				hssffont.setStrikeout(false);
			} else {
				if (font.getIsStrikeThrough().equals("true")) {
					hssffont.setStrikeout(true);
				} else {
					hssffont.setStrikeout(false);
				}
			}
			// 斜体
			if (font.getIsItalic() == null) {
				hssffont.setItalic(false);
			} else {
				if (font.getIsItalic().equals("true")) {
					hssffont.setItalic(true);
				} else {
					hssffont.setItalic(false);
				}
			}
			// 下划线
			if (font.getIsUnderline() == null) {
				hssffont.setUnderline(HSSFFont.U_NONE);
			} else {
				if (font.getIsUnderline().equals("true")) {
					hssffont.setUnderline(HSSFFont.U_SINGLE);
				} else {
					hssffont.setUnderline(HSSFFont.U_NONE);
				}
			}

			// 前景色
			String fontColor = font.getForeColor();
			Color color = IRColor.getColorByNodeValue(fontColor);
			short cIndex = getNearestColor(color, IsForeColor);
			hssffont.setColor(cIndex);
		}

		return hssffont;
	}

	/**
	 * 从颜色画板寻找匹配的颜色，不存在则添加新的颜色
	 * 
	 * @param color
	 * @param type
	 * @return
	 */
	public short getNearestColor(Color color, int type) {
		if (color == null) {
			color = Color.BLACK;
			if (type == IsBackColor) {
				color = Color.WHITE;
			}
		}

		HSSFPalette hssfpalette = wb.getCustomPalette();
		HSSFColor hssfcolor = hssfpalette.findColor((byte) color.getRed(),
				(byte) color.getGreen(), (byte) color.getBlue());
		if ((hssfcolor == null) || (hssfcolor.getIndex() > colorIndex)) {
			colorIndex++;
			hssfpalette.setColorAtIndex(colorIndex, (byte) color.getRed(),
					(byte) color.getGreen(), (byte) color.getBlue());
			hssfcolor = hssfpalette.findColor((byte) color.getRed(),
					(byte) color.getGreen(), (byte) color.getBlue());
			return colorIndex;
		}
		return hssfcolor.getIndex();
	}

	/**
	 * 处理边框
	 * 
	 * @param cellStyle
	 * @param style
	 */
	private void exportBorder(Style cellStyle, HSSFCellStyle style) {

		// 边框大小
		// 2009.03.25 修改处理为4条边框
		TBorder[] borders = cellStyle.getBorders();
		if (borders != null) {
			String allBorderColor = cellStyle.getallBorderColor();
			for (int i = 0; i < 5; i++) {
				TBorder border = borders[i];
				String borderType = border.borderType;
				String borderPosition = border.borderPosition;
				String borderWeight = border.borderWeight;
				String borderColor = border.borderColor;
				if (!borderType.equalsIgnoreCase("none")) {
					if (!(borderColor != null && !borderColor.equals(""))) {
						borderColor = allBorderColor;
					}
					Color color = IRColor.getColorByNodeValue(borderColor);
					short cIndex = getNearestColor(color, IsForeColor);

					int weight = Integer.parseInt(borderWeight);
					if (weight > 0) {
						if (i == 0) {// top
							style.setTopBorderColor(cIndex);
							if (borderType.equalsIgnoreCase("solid")) {
								if (weight < 2) {
									style
											.setBorderTop(HSSFCellStyle.BORDER_THIN);
								} else if (weight < 3) {
									style
											.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
								} else {
									style
											.setBorderTop(HSSFCellStyle.BORDER_THICK);
								}
							} else if (borderType.equalsIgnoreCase("dot")) {
								style.setBorderTop(HSSFCellStyle.BORDER_DOTTED);
							} else if (borderType.equalsIgnoreCase("dash")) {
								if (weight < 2) {
									style
											.setBorderTop(HSSFCellStyle.BORDER_DASHED);
								} else if (weight < 3) {
									style
											.setBorderTop(HSSFCellStyle.BORDER_MEDIUM_DASHED);
								}
							}
						}
						if (i == 1) {// bottom
							style.setBottomBorderColor(cIndex);
							if (borderType.equalsIgnoreCase("solid")) {
								if (weight < 2) {
									style
											.setBorderBottom(HSSFCellStyle.BORDER_THIN);
								} else if (weight < 3) {
									style
											.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
								} else {
									style
											.setBorderBottom(HSSFCellStyle.BORDER_THICK);
								}
							} else if (borderType.equalsIgnoreCase("dot")) {
								style
										.setBorderBottom(HSSFCellStyle.BORDER_DOTTED);
							} else if (borderType.equalsIgnoreCase("dash")) {
								if (weight < 2) {
									style
											.setBorderBottom(HSSFCellStyle.BORDER_DASHED);
								} else if (weight < 3) {
									style
											.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM_DASHED);
								}
							}
						}
						if (i == 2) {// left
							style.setLeftBorderColor(cIndex);
							if (borderType.equalsIgnoreCase("solid")) {
								if (weight < 2) {
									style
											.setBorderLeft(HSSFCellStyle.BORDER_THIN);
								} else if (weight < 3) {
									style
											.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
								} else {
									style
											.setBorderLeft(HSSFCellStyle.BORDER_THICK);
								}
							} else if (borderType.equalsIgnoreCase("dot")) {
								style
										.setBorderLeft(HSSFCellStyle.BORDER_DOTTED);
							} else if (borderType.equalsIgnoreCase("dash")) {
								if (weight < 2) {
									style
											.setBorderLeft(HSSFCellStyle.BORDER_DASHED);
								} else if (weight < 3) {
									style
											.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM_DASHED);
								}
							}
						}
						if (i == 3) {// right
							style.setRightBorderColor(cIndex);
							if (borderType.equalsIgnoreCase("solid")) {
								if (weight < 2) {
									style
											.setBorderRight(HSSFCellStyle.BORDER_THIN);
								} else if (weight < 3) {
									style
											.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
								} else {
									style
											.setBorderRight(HSSFCellStyle.BORDER_THICK);
								}
							} else if (borderType.equalsIgnoreCase("dot")) {
								style
										.setBorderRight(HSSFCellStyle.BORDER_DOTTED);
							} else if (borderType.equalsIgnoreCase("dash")) {
								if (weight < 2) {
									style
											.setBorderRight(HSSFCellStyle.BORDER_DASHED);
								} else if (weight < 3) {
									style
											.setBorderRight(HSSFCellStyle.BORDER_MEDIUM_DASHED);
								}
							}
						}
						if (i == 4) {
						}// slash 单独处理

					}
				}
			}
		}
	}

	/**
	 * 处理页面设置
	 * 
	 * @param pagesetup
	 * @param sheet
	 */
	private void exportPageSetup(PageSetup pagesetup, HSSFSheet sheet) {
		// PageSetup pagesetup = report.getPagesetup();

		if (pagesetup == null)
			return;

		// 打印设置
		HSSFPrintSetup hps = sheet.getPrintSetup();

		// 方向
		String orientation = pagesetup.getOrientation();
		if (orientation == null || orientation.equals("")) {
			hps.setLandscape(false);
		} else if (orientation.equals("landscape")) {
			hps.setLandscape(true);
		}

		// 纸张
		String pagesize = pagesetup.getPageSize();
		if (pagesize == null || pagesize.equals("")) { // 默认A4
			hps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("A4")) { // 9
			hps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("A5")) { // 11
			hps.setPaperSize(HSSFPrintSetup.A5_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("Letter")) { // 1
			hps.setPaperSize(HSSFPrintSetup.LETTER_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("Legal")) { // 5
			hps.setPaperSize(HSSFPrintSetup.LEGAL_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("Executive")) { // 7
			hps.setPaperSize(HSSFPrintSetup.EXECUTIVE_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("Env10")) { // 20
			hps.setPaperSize(HSSFPrintSetup.ENVELOPE_10_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("EnvDL")) { // 27
			hps.setPaperSize(HSSFPrintSetup.ENVELOPE_DL_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("EnvC5")) { // 28
			hps.setPaperSize(HSSFPrintSetup.ENVELOPE_CS_PAPERSIZE);
		} else if (pagesize.equalsIgnoreCase("EnvMonarch")) { // 37
			hps.setPaperSize(HSSFPrintSetup.ENVELOPE_MONARCH_PAPERSIZE);
		}

		// 缩放
		hps.setScale((short) 100);

		// 调整
		// hps.setFitWidth((short)1);
		// hps.setFitHeight((short)1);

		// 边距
		if (pagesetup.getLeftMargin() != null) {
			double marginLeft = Double.parseDouble(pagesetup.getLeftMargin());
			sheet.setMargin(HSSFSheet.LeftMargin, marginLeft * 0.3937); // 不知道为啥*0.3937就对了
		}
		if (pagesetup.getRightMargin() != null) {
			double marginRight = Double.parseDouble(pagesetup.getRightMargin());
			sheet.setMargin(HSSFSheet.RightMargin, marginRight * 0.3937);
		}
		if (pagesetup.getTopMargin() != null) {
			double marginTop = Double.parseDouble(pagesetup.getTopMargin());
			sheet.setMargin(HSSFSheet.TopMargin, marginTop * 0.3937);
		}
		if (pagesetup.getBottomMargin() != null) {
			double marginBottom = Double.parseDouble(pagesetup
					.getBottomMargin());
			sheet.setMargin(HSSFSheet.BottomMargin, marginBottom * 0.3937);
		}
		if (pagesetup.getHeaderMargin() != null
				&& !"".equals(pagesetup.getHeaderMargin())) {
			double marginHeader = Double.parseDouble(pagesetup
					.getHeaderMargin());
			hps.setHeaderMargin(marginHeader * 0.3937);
		}
		if (pagesetup.getFooterMargin() != null
				&& !"".equals(pagesetup.getFooterMargin())) {
			double marginFooter = Double.parseDouble(pagesetup
					.getFooterMargin());
			hps.setFooterMargin(marginFooter * 0.3937);
		}

		// 居中
		boolean isHCenter = pagesetup.getIsHCenter();
		boolean isVCenter = pagesetup.getIsVCenter();
		sheet.setHorizontallyCenter(isHCenter);
		sheet.setVerticallyCenter(isVCenter);

		// 页眉页脚
		HSSFHeader xlsheader = sheet.getHeader();
		HSSFFooter xlsfooter = sheet.getFooter();
		String header = pagesetup.getPageHeaderContent();
		String footer = pagesetup.getPageFooterContent();

		if (header != null && !header.equals("")) {
			Style hs = pagesetup.getPageHeaderStyle();
			if (hs != null) {
				// IRFont font = hs.getFont();
				String align = hs.getHalign();
				if (align.equalsIgnoreCase("left")) {
					xlsheader.setLeft(header);
				} else if (align.equalsIgnoreCase("center")) {
					xlsheader.setCenter(header);
				} else if (align.equalsIgnoreCase("right")) {
					xlsheader.setRight(header);
				}
			}
		}

		if (footer != null && !footer.equals("")) {
			Style fs = pagesetup.getPageFooterStyle();
			if (fs != null) {
				// IRFont font = fs.getFont();
				String align = fs.getHalign();
				if (align.equalsIgnoreCase("left")) {
					xlsfooter.setLeft(footer);
				} else if (align.equalsIgnoreCase("center")) {
					xlsfooter.setCenter(footer);
				} else if (align.equalsIgnoreCase("right")) {
					xlsfooter.setRight(footer);
				}
			}
		}
	}

	/**
	 * 处理数字显示格式
	 * 
	 * @param style
	 * @param cellFormat
	 */
	/**
	 * Utility to identify builtin formats. Now can handle user defined data
	 * formats also. The following is a list of the formats as returned by this
	 * class.
	 * <P>
	 *<P>
	 * 0, "General"<br>
	 * 1, "0"<br>
	 * 2, "0.00"<br>
	 * 3, "#,##0"<br>
	 * 4, "#,##0.00"<br>
	 * 5, "($#,##0_);($#,##0)"<br>
	 * 6, "($#,##0_);[Red]($#,##0)"<br>
	 * 7, "($#,##0.00);($#,##0.00)"<br>
	 * 8, "($#,##0.00_);[Red]($#,##0.00)"<br>
	 * 9, "0%"<br>
	 * 0xa, "0.00%"<br>
	 * 0xb, "0.00E+00"<br>
	 * 0xc, "# ?/?"<br>
	 * 0xd, "# ??/??"<br>
	 * 0xe, "m/d/yy"<br>
	 * 0xf, "d-mmm-yy"<br>
	 * 0x10, "d-mmm"<br>
	 * 0x11, "mmm-yy"<br>
	 * 0x12, "h:mm AM/PM"<br>
	 * 0x13, "h:mm:ss AM/PM"<br>
	 * 0x14, "h:mm"<br>
	 * 0x15, "h:mm:ss"<br>
	 * 0x16, "m/d/yy h:mm"<br>
	 *<P>
	 * // 0x17 - 0x24 reserved for international and undocumented 0x25,
	 * "(#,##0_);(#,##0)"
	 * <P>
	 * 0x26, "(#,##0_);[Red](#,##0)"
	 * <P>
	 * 0x27, "(#,##0.00_);(#,##0.00)"
	 * <P>
	 * 0x28, "(#,##0.00_);[Red](#,##0.00)"
	 * <P>
	 * 0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)"
	 * <P>
	 * 0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)"
	 * <P>
	 * 0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)"
	 * <P>
	 * 0x2c, "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)"
	 * <P>
	 * 0x2d, "mm:ss"
	 * <P>
	 * 0x2e, "[h]:mm:ss"
	 * <P>
	 * 0x2f, "mm:ss.0"
	 * <P>
	 * 0x30, "##0.0E+0"
	 * <P>
	 * 0x31, "@" - This is text format.
	 * <P>
	 * 0x31 "text" - Alias for "@"
	 * <P>
	 * 
	 * @author Andrew C. Oliver (acoliver at apache dot org)
	 * @author Shawn M. Laubach (slaubach at apache dot org)
	 */
	private void exportCellFormat(HSSFCellStyle style, Format cellFormat) {
		HSSFDataFormat format = wb.createDataFormat();
		String formatType = cellFormat.getType();
		String formatString = cellFormat.getFormatString();
		String point = cellFormat.getPoint();
		String isThousand = cellFormat.getIsThousand(); // 1使用 0不用
		int pt = 0;
		String numstr = "";
		String pointstr = "";
		String tmpstr = "";

		// 对fnumber/fcurrency/fpercent的小数位做预处理
		if (formatType.equals("fnumber") || formatType.equals("fcurrency")
				|| formatType.equals("fpercent")) {
			if (point != null && !point.equals("")) {
				pt = Integer.parseInt(point);
				if (pt > 0) {
					pointstr = "0.";
					for (int i = 1; i <= pt; i++) {
						pointstr = pointstr + "0";
					}
				} else if (pt == 0) {
					pointstr = "0";
				}
			}
		}

		if (formatType.equals("fnumber")) {
			if ((isThousand != null) && isThousand.equals("1"))
				numstr = "#,##";
			// if (!pointstr.equals("")) pointstr = pointstr + "_";
			style.setDataFormat(format.getFormat(numstr + pointstr));
		} else if (formatType.equals("fcurrency")) {
			String currency = cellFormat.getCurrency();
			if (currency != null && !currency.equals("")) {
				tmpstr = "\"" + currency + "\"" + pointstr;
			} else {
				tmpstr = pointstr;
			}
			style.setDataFormat(format.getFormat(tmpstr));
		} else if (formatType.equals("fpercent")) {
			style.setDataFormat(format.getFormat(pointstr + "%"));
		} else if (formatType.equals("fdate")) {
			if (formatString.equals("vbShortDate")) {
				style.setDataFormat(format.getFormat("yyyy-m-d"));
			} else if (formatString.equals("vbLongDate")) {
				style.setDataFormat(format
						.getFormat("[$-F800]dddd, mmmm dd, yyyy"));
			} else if (formatString.equals("=chn_YMD")) { // [DBNum1][$-804]yyyy"年"m"月"d"日";@
				tmpstr = "[DBNum1][$-804]yyyy\"年\"m\"月\"d\"日\";@";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("=chn_YM")) { // [DBNum1][$-804]yyyy"年"m"月";@
				tmpstr = "[DBNum1][$-804]yyyy\"年\"m\"月\";@";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("=chn_MD")) { // [DBNum1][$-804]m"月"d"日";@
				tmpstr = "[DBNum1][$-804]m\"月\"d\"日\";@";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("=chn_Y")) { // [DBNum1][$-804]yyyy"年";@
				tmpstr = "[DBNum1][$-804]yyyy\"年\";@";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("yyyy年m月d日")) { // yyyy"年"m"月"d"日"
				tmpstr = "yyyy\"年\"m\"月\"d\"日\"";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("yyyy年m月")) { // yyyy"年"m"月"
				tmpstr = "yyyy\"年\"m\"月\"";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("m月d日")) { // m"月"d"日"
				tmpstr = "m\"月\"d\"日\"";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("yyyy-m-d")) {
				style.setDataFormat(format.getFormat("yyyy-m-d"));
			} else if (formatString.equals("yy-m-d")) {
				style.setDataFormat(format.getFormat("yy-m-d"));
			} else if (formatString.equals("m-d")) {
				style.setDataFormat(format.getFormat("m-d"));
			}
		} else if (formatType.equals("ftime")) {
			if (formatString.equals("vbLongTime")) {
				style.setDataFormat(format.getFormat("[$-F400]h:mm:ss AM/PM"));
			} else if (formatString.equals("HH:mm")) {
				style.setDataFormat(format.getFormat("h:mm"));
			} else if (formatString.equals("h:mm AM/PM")) {
				style.setDataFormat(format.getFormat("h:mm AM/PM"));
			} else if (formatString.equals("HH:mm:ss")) {
				style.setDataFormat(format.getFormat("h:mm:ss"));
			} else if (formatString.equals("h:mm:ss AM/PM")) {
				style.setDataFormat(format.getFormat("h:mm:ss AM/PM"));
			} else if (formatString.equals("HH时mm分")) { // h"时"mm"分"
				tmpstr = "h\"时\"mm\"分\"";
				style.setDataFormat(format.getFormat(tmpstr));
			} else if (formatString.equals("HH时mm分ss秒")) { // h"时"mm"分"ss"秒"
				tmpstr = "h\"时\"mm\"分\"ss\"秒\"";
				style.setDataFormat(format.getFormat(tmpstr));
			}
		}
	}

	public String getExportType() {
		// TODO Auto-generated method stub
		return "EXCEL";
	}

}
