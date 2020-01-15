package com.cvicse.inforreport.exporter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import com.cvicse.inforreport.api.IReportExporter;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.exporter.CellEvent;
import com.cvicse.inforreport.exporter.chart.ChartExporter;
import com.cvicse.inforreport.model.Body;
import com.cvicse.inforreport.model.DynamicChart;
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
import com.cvicse.inforreport.model.Title;
import com.cvicse.inforreport.util.IReportConfiger;
import com.cvicse.inforreport.util.ReportConfiger;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PDFReportExporter implements IReportExporter {
	
	private Document pdfDocument;
	
	private List colsWidth;
	
	private List rowsHeight;
	
	private InforReport report;
	
	private PdfWriter pdfWriter;

	public void exportReport(InforReport report, OutputStream result)
			throws ReportException {
		exportReport(null, report, result);

	}

	public void exportReport(PageSetup pagesetup, InforReport report,
			OutputStream result) throws ReportException {
		this.report = report; 
		colsWidth = report.getColsWidth();
		rowsHeight = report.getRowsHeight();
		
		if(pagesetup==null)
			pagesetup = report.getPagesetup();
		String pageHeader = null;
		String pageFooter = null;
		if (pagesetup != null) {
			float marginLeft = Math.round(Float.parseFloat(pagesetup.getLeftMargin()==null?"0":pagesetup.getLeftMargin())*28.35);
			float marginRight = Math.round(Float.parseFloat(pagesetup.getRightMargin()==null?"0":pagesetup.getRightMargin())*28.35);
			float marginTop = Math.round(Float.parseFloat(pagesetup.getTopMargin()==null?"0":pagesetup.getTopMargin())*28.35);
			float marginBottom = Math.round(Float.parseFloat(pagesetup.getBottomMargin()==null?"0":pagesetup.getBottomMargin())*28.35);
			Rectangle pageSize = PageSize.A4;
			if("landscape".equalsIgnoreCase(pagesetup.getOrientation())){
				pageSize = pageSize.rotate();

			}
			pdfDocument = new Document(pageSize);
			pdfDocument.setMargins(marginLeft, marginRight,marginTop, marginBottom);	
			pageHeader = pagesetup.getPageHeaderContent();
			pageFooter = pagesetup.getPageFooterContent();
		} else {
			pdfDocument = new Document(PageSize.A4);
		}		
		

		try {
			pdfWriter = PdfWriter.getInstance(pdfDocument, result);
			
			String reportTitle = report.getReportTitle();
			if ((reportTitle != null) && (!reportTitle.equals("")))
				pdfDocument.addTitle(reportTitle);
			if (pageHeader != null) {
				HeaderFooter header = new HeaderFooter(new Phrase(pageHeader),
						false);
				header.setBorder(HeaderFooter.NO_BORDER);
				pdfDocument.setHeader(header);
			}

			pdfDocument.open();
			
			if (pageFooter != null) {
				HeaderFooter footer = new HeaderFooter(new Phrase(pageFooter),
						true);
				footer.setBorder(HeaderFooter.NO_BORDER);
				pdfDocument.setFooter(footer);
			}
			
			/*			
			//export title
			Title title = report.getTitle();
			if (title != null) {
				exportTitle(title);
			}
			// export body
			exportBody(report.getBody());
			// export summary
			Summary summary = report.getSummary();
			if (summary != null) {
				exportSummary(summary);
			}
*/

			
			// 2009.07.13重写，用grid来导出
			ReportData wrapper = report.getWrapper();
			if (wrapper == null) {
				wrapper = new ReportData();
				wrapper.setGrid(report);
			}
			Grid grid = wrapper.getGrid();
			// EngineUtils.Doc2XmlFile(wrapper.getGridDocument(), "GBK", "C:/xx.xml");
		
			float[] rows = new float[rowsHeight.size()];
			for (int i = 0; i < rowsHeight.size(); i++) {
				rows[i] = Float.parseFloat(String.valueOf(rowsHeight.get(i)));
			}

			float[] cols= new float[colsWidth.size()];
			for (int i = 0; i < colsWidth.size(); i++){
				cols[i] = Float.parseFloat(String.valueOf(colsWidth.get(i)));			
			}			
			PdfPTable pTable = new PdfPTable(cols);
			
			// 逐行导出
			GridRow[] arrGridRows = grid.arrGridRows;
			for (int i = 0; i < grid.totalrow; i++) {
				GridRow gridrow = arrGridRows[i];
				GridCell[] arrGridCells = gridrow.arrGridCells;
				for (int j = 0; j < grid.totalcol; j++) {
					if (arrGridCells[j].merged != true) {
						PdfPCell pCell = createPdfpCell(arrGridCells[j],rows[i]);
						pTable.addCell(pCell);
					}
				}
				pTable.completeRow();
			}
			pdfDocument.add(pTable);
			
			// 导出图表
			DynamicChart[] charts = report.getBody().getCharts();
			for(DynamicChart chart: charts){
				exportChart(chart);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ReportException(e.getMessage());
		}		
		pdfDocument.close();		

	}
	
	
	/**
	 * 导出标题区
	 * 
	 * @param titleNode
	 */
	private void exportTitle(Title title) throws Exception {
		exportTable(title, null);
	}

	/**
	 * 导出汇总区
	 * 
	 * @param summaryNode
	 */
	private void exportSummary(Summary summary) throws Exception {
		exportTable(summary, null);
	}

	/**
	 * 导出报表body区域
	 * 
	 * @param bodyNode
	 */
	private void exportBody(Body body) throws Exception {

		// body区风格
		Style bodyStyle = body.getStyle();

		// 遍历所有子表		
		com.cvicse.inforreport.model.Table[] tables = body.getTables();
		for (int i = 0; i < tables.length; i++) {
			com.cvicse.inforreport.model.Table modelTable = tables[i];
			exportTable(modelTable, bodyStyle);
		}
		
		DynamicChart[] charts = body.getCharts();
		for(DynamicChart chart: charts){
			exportChart(chart);
		}
	}
	
	private void exportChart(DynamicChart chart) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ChartExporter.exportChart(chart,output);
		byte[] b = output.toByteArray();
		if(b!=null){
			Image image = Image.getInstance(b);
			float width = Float.parseFloat(String.valueOf(chart.getWidthPx()));
			float height = Float.parseFloat(String.valueOf(chart.getHeightPx()));
			//System.out.println("scale: "+width+"--"+height);
			//image.scaleAbsolute(chart.getWidthPx(),chart.getHeightPx());
			//image.scalePercent(50);
			image.scaleAbsolute(width,height);
//			System.out.println("position: " + chart.getLeftPx() + "--"
//					+  (pdfDocument.getPageSize().getHeight() -chart.getTopPx()-chart.getHeightPx()));
//			System.out.println("pageHeight: "+pdfDocument.getPageSize().getHeight());
			//image.setAbsolutePosition(chart.getLeftPx(), pdfDocument
			//		.getPageSize().getHeight()
			//		- chart.getTopPx()-chart.getHeightPx());
			PdfPTable table = new PdfPTable(1);
			//table.setTotalWidth(width);
			//table.setWidths(new float[]{width});
			//table.setLockedWidth(true);
			
			//此处若使用PdfPCell cell= new PdfPCell(image)则图片会超出上面设定的width和height，原因不知
			PdfPCell cell= new PdfPCell();
			cell.setImage(image);
			table.addCell(cell);
			pdfDocument.add(table);
			
		}
		output.close();
		
	}

	/**
	 * 处理table
	 * 
	 * @param tableNode
	 * @param bodyStyle
	 */
	private void exportTable(com.cvicse.inforreport.model.Table table, Style bodyStyle) throws Exception {

		// 表风格
		Style tableStyle = table.getStyle();
		if (bodyStyle != null && tableStyle==null)
			tableStyle = (Style) bodyStyle.clone();
		

		// 位置
		//int rowsTop = Integer.parseInt(table.getTableTop());
		//int colsLeft = Integer.parseInt(table.getTableLeft());

		int rowCount = table.getRealHeight();
		int colCount = table.getRealWidth();
		
		float[] cols= new float[colsWidth.size()];
		for(int i=0;i<colsWidth.size();i++){
			cols[i] = Float.parseFloat(String.valueOf(colsWidth.get(i)));			
			//System.out.println(cols[i]);
		}
		
//		float widths = 0.0f;
//		for(int i=0;i<colsWidth.size();i++){
//			widths += Float.parseFloat(String.valueOf(colsWidth.get(i)));	
//		}
//		float[] cols = new float[colsWidth.size()];
//		for(int i=0;i<colsWidth.size();i++){
//			cols[i] = Float.parseFloat(String.valueOf(colsWidth.get(i)))/widths;
//			System.out.println(cols[i]);
//		}
		
		
		//Table aTable = new Table(colCount, rowCount);
		//aTable.setWidth(widths/PageSize.A4.getWidth());
		//aTable.setWidths(cols);
		//aTable.setLocked(true);
		//setTableStyle(aTable, tableStyle);
		//System.out.println(colsWidth);
		
		//PdfPTable pTable = new PdfPTable(colCount);	
		//pTable.setTotalWidth(cols);
		PdfPTable pTable = new PdfPTable(cols);
		//pTable.setLockedWidth(true);

		// 遍历所有行		
		List rows = table.getRows();
		for (int i = 0; i < rows.size(); i++) {
			//System.out.println("row"+(i+1));
			Row row =(Row)rows.get(i);
			if(row==null) continue;
			Style rowStyle = row.getStyle();
			if(rowStyle==null && tableStyle!=null)
				rowStyle = tableStyle;
			
			List cells = row.getCells();
			for (int j = 0; j < cells.size(); j++) {				
				com.cvicse.inforreport.model.Cell modelCell = (com.cvicse.inforreport.model.Cell)cells.get(j);
				PdfPCell pCell = createPdfpCell(modelCell, rowStyle,i+(report.getTitle()==null?0:report.getTitle().getHeight()));
				pTable.addCell(pCell);		
			}
			pTable.completeRow();

		}

		pdfDocument.add(pTable);
		//pdfDocument.add(aTable);
		
	}	

	/**
	 * @author qiao_lu1 
	 * export cell using report but no border
	 * @param modelCell
	 * @param rowStyle
	 * @param rowNo
	 * @return PdfPCell
	 * @throws Exception
	 */	
	private PdfPCell createPdfpCell(com.cvicse.inforreport.model.Cell modelCell, Style rowStyle, int rowNo) throws Exception {
		PdfPCell cell;		
		
		Style cellStyle = modelCell.getCellStyle();
		// 2009.03.25 修改处理为4条边框

		// 设置单元格内容
		String cellContent = modelCell.getCellContent();
		//System.out.println(cellContent);
		CellEvent event = new CellEvent();		
		event.setModelCell(modelCell);
		if (cellStyle != null) {
			Font cellFont = convertFont(cellStyle.getFont());
			cell = new PdfPCell(new Phrase(cellContent, cellFont));
			event.setPhrase(cell.getPhrase());

			// 处理单元格风格
			if (cellStyle.getHalign().equalsIgnoreCase("right")) {
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			} else if (cellStyle.getHalign().equalsIgnoreCase("center")) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			}

			if (cellStyle.getValign().equalsIgnoreCase("bottom")) {
				cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
			} else if (cellStyle.getValign().equalsIgnoreCase("middle")) {
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			} else {
				cell.setVerticalAlignment(Element.ALIGN_TOP);
			}

			String backColor = cellStyle.getBackColor();
			Color color = IRColor.getColorByNodeValue(backColor);
			cell.setBackgroundColor(color);

			// 边框
			// 2009.03.25 修改处理为4条边框

		} else {
			Font cellFont = createFontName("");
			cell = new PdfPCell(new Phrase(cellContent,cellFont));
			cell.setBorderWidth(0);
			event.setPhrase(cell.getPhrase());
		}
		cell.setCellEvent(event);
		
		if(modelCell.getRowspan()==1)
			cell.setFixedHeight(modelCell.getHeight());
		
		if("image".equals(modelCell.getType())||"Dyimage".equals(modelCell.getType())){
			byte[] b = modelCell.getPic();
			Image image = null;
			if(b!=null){ //db pic
				image = Image.getInstance(b);	
				cell.setImage(image);				
			} else {
				b = (byte[]) report.getImages().get(cellContent);
				if (b != null) { // template pic
					image = Image.getInstance(b);					
					cell.setImage(image);
				} else { //url
					String content = modelCell.getCellContent();
					if (content != null) {
						if (content.startsWith("<img")){
							//System.out.println("!!!!!"+content);
							content = content.substring(content.indexOf("\"") + 1, 
									content.indexOf("\"", content.indexOf("http")));
						}
						try {
							IReportConfiger configer = ReportConfiger.getInstance();
							System.setProperty("http.proxyHost", configer.getProxyHost());
							System.setProperty("http.proxyPort", configer.getProxyPort());
							//System.out.println(InetAddress.getLocalHost().getHostAddress());							
							image = Image.getInstance(new URL(content));
							cell.setImage(image);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
			
		}
		
		//cell.setFixedHeight(Float.parseFloat(String.valueOf(rowsHeight.get(rowNo+1))));

		// 处理合并单元格
		int colspan = modelCell.getColspan();
		int rowspan = modelCell.getRowspan();
		if (colspan > 1)
			cell.setColspan(colspan);
		//if (rowspan > 1)
			//cell.setRowspan(rowspan);

		return cell;
	}

	/**
	 * 处理单元格字体
	 * 
	 * @param cellFont
	 * @return
	 * @throws Exception
	 */
	private Font convertFont(IRFont cellFont) throws Exception {
		// 解决中文问题
		//BaseFont bfChinese = BaseFont.createFont("STSong-Light",
		//		"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);	
		
		Font font = createFontName(cellFont.getFontName());

		String fontColor = cellFont.getForeColor();
		Color color = IRColor.getColorByNodeValue(fontColor);
		font.setColor(color);

		if ("true".equals(cellFont.getIsBold())) {
			font.setStyle(Font.BOLD);
		}

		if ("true".equals(cellFont.getIsItalic())) {
			font.setStyle(Font.ITALIC);
		}

		if ("true".equals(cellFont.getIsStrikeThrough())) {
			font.setStyle(Font.STRIKETHRU);
		}

		if ("true".equals(cellFont.getIsUnderline())) {
			font.setStyle(Font.UNDERLINE);
		}

		if ((cellFont.getFontName() == null)
				|| ("".equals(cellFont.getFontName()))) {
			font.setFamily("宋体");
		} else {
			font.setFamily(cellFont.getFontName());
		}

		if ((cellFont.getFontSize() != null)
				&& (!"".equals(cellFont.getFontSize()))) {
			font.setSize(Float.parseFloat(cellFont.getFontSize()));
		} else {
			font.setSize(12);
		}

		return font;
	}
	
	private Font createFontName(String fontName) throws Exception {
		String osname = System.getProperty("os.name").toUpperCase();
		Font font = null;
		if (osname.startsWith("WIN")) {

			String fontFile = null;
			if (fontName == null || "".equals(fontName)) {
				// fontName= "宋体";
				fontFile = "simsun.ttc,1";
			} else if ("楷体_GB2312".equals(fontName)) {
				fontFile = "simkai.ttf";
			} else if ("方正舒体".equals(fontName)) {
				fontFile = "FZSTK.ttf";
			} else if ("方正姚体".equals(fontName)) {
				fontFile = "FZYTK.ttf";
			} else if ("仿宋_GB2312".equals(fontName)) {
				fontFile = "SIMFANG.ttf";
			} else if ("黑体".equals(fontName)) {
				fontFile = "SIMHEI.ttf";
			} else if ("华文彩云".equals(fontName)) {
				fontFile = "STCAIYUN.ttf";
			} else if ("华文仿宋".equals(fontName)) {
				fontFile = "STFANGSO.ttf";
			} else if ("华文细黑".equals(fontName)) {
				fontFile = "STXIHEI.ttf";
			} else if ("华文新魏".equals(fontName)) {
				fontFile = "STXINWEI.ttf";
			} else if ("华文行楷".equals(fontName)) {
				fontFile = "STXINGKA.ttf";
			} else if ("华文中宋".equals(fontName)) {
				fontFile = "STZHONGS.ttf";
			} else if ("隶书".equals(fontName)) {
				fontFile = "SIMLI.ttf";
			} else if ("宋体-方正超大字符集".equals(fontName)) {
				fontFile = "SURSONG.ttf";
			} else if ("幼圆".equals(fontName)) {
				fontFile = "SIMYOU.ttf";
			} else { // 宋体
				fontFile = "simsun.ttc,1";
			}
			BaseFont bfChinese = BaseFont.createFont(System.getenv("SystemRoot")
					+ "\\fonts\\" + fontFile, BaseFont.IDENTITY_H,
					BaseFont.NOT_EMBEDDED);
			font = new Font(bfChinese);

		} else if("linux".equalsIgnoreCase(osname)){
			String fontFile ="/usr/share/fonts/zh_CN/TrueType/zysong.ttf";
			if(new File(fontFile).exists()){
				BaseFont bfChinese = BaseFont.createFont(fontFile, BaseFont.IDENTITY_H,
						BaseFont.NOT_EMBEDDED);
				font = new Font(bfChinese);
			}else{
				font = new Font();
			}
				
		}else if("aix".equalsIgnoreCase(osname)){
			String javahome = System.getenv("JAVA_HOME");
			String fontFile =javahome+"/jre/lib/fonts/zysong.ttf";
			if(new File(fontFile).exists()){
				BaseFont bfChinese = BaseFont.createFont(fontFile, BaseFont.IDENTITY_H,
						BaseFont.NOT_EMBEDDED);
				font = new Font(bfChinese);
			}else{
				font = new Font();
			}
		}
		else {
			//BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
			//		BaseFont.EMBEDDED);
			font = new Font();
			
			//font = FontFactory.getFont("Arial");
		}
		
		return font;
	}
	
	// 2009.07.13
	/**
	 * @author li_feng2 
	 * export cell using grid with borders
	 *  
	 */
	private PdfPCell createPdfpCell(GridCell gridcell,float rowHeight) throws Exception {
		PdfPCell pcell;
		Font font;
		Color color;
		
		Style style = gridcell.style;
		String content = gridcell.cellText;
		CellEvent event = new CellEvent();
		event.setGridCell(gridcell);
		
		// 处理单元格风格
		if (style == null) {
			font = createFontName("");
			pcell = new PdfPCell(new Phrase(content,font));
		}else {
			font = convertFont(style.getFont());
			pcell = new PdfPCell(new Phrase(content,font));
		}

		// 处理合并单元格
		int colspan = gridcell.colSpan;
		int rowspan = gridcell.rowSpan;
		if (colspan > 1) pcell.setColspan(colspan);
		//if (rowspan > 1) cell.setRowspan(rowspan);
		
		pcell.setBorderWidth(0);
		event.setPhrase(pcell.getPhrase());

		if (style != null) {
			// 居中
			if (style.getHalign().equalsIgnoreCase("right")) {
				pcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			} else if (style.getHalign().equalsIgnoreCase("center")) {
				pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				pcell.setHorizontalAlignment(Element.ALIGN_LEFT);
			}

			if (style.getValign().equalsIgnoreCase("bottom")) {
				pcell.setVerticalAlignment(Element.ALIGN_BOTTOM);
			} else if (style.getValign().equalsIgnoreCase("middle")) {
				pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			} else {
				pcell.setVerticalAlignment(Element.ALIGN_TOP);
			}

			// 背景色
			String backColor = style.getBackColor();
			color = IRColor.getColorByNodeValue(backColor);
			pcell.setBackgroundColor(color);
			
			// 边框
			TBorder[] borders = style.getBorders();
			if (borders != null) {
				String allBorderColor = style.getallBorderColor();
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
						color = IRColor.getColorByNodeValue(borderColor);
						
						float weight = Float.parseFloat(borderWeight);
						if (weight > 0) {
							// 感觉pdf的线性粗细是正常看到的一半
							float f = (int)Math.round(weight/2*10)/10f;
							
							if (i==0) {//top
								pcell.setBorderColorTop(color);
								pcell.setBorderWidthTop(f);								
							}
							if (i==1) {//bottom
								pcell.setBorderColorBottom(color);
								pcell.setBorderWidthBottom(f);
							}
							if (i==2) {//left
								pcell.setBorderColorLeft(color);
								pcell.setBorderWidthLeft(f);
							}
							if (i==3) {//right
								pcell.setBorderColorRight(color);
								pcell.setBorderWidthRight(f);
							}
						}//if weight
					}//if bodertype
				}//for
			}
		}
		pcell.setCellEvent(event);
		
		// 行高
		if (gridcell.rowSpan == 1) {
			pcell.setFixedHeight(rowHeight);
		}
		
		// 图片
		byte[] b = null;
		Image image = null;
		if ("image".equals(gridcell.dataType)) {
			// template pic
			b = (byte[]) report.getImages().get(gridcell.cellValue);
		}else if ("Dyimage".equals(gridcell.dataType)) {
			b = (byte[]) gridcell.Pic;
		}
		if (b != null) {
			image = Image.getInstance(b);					
			pcell.setImage(image);
		}

		return pcell;
	}
	

	public String getExportType() {
		return "PDF";
	}

}
