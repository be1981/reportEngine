package com.cvicse.inforreport.exporter;

import java.awt.Color;
import java.awt.Graphics2D;

import com.cvicse.inforreport.model.Cell;
import com.cvicse.inforreport.model.GridCell;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;

public class CellEvent implements PdfPCellEvent {
	
	private String slash;
	
	private Phrase phrase;
	
	private Cell modelCell;
	
	private GridCell gridcell;
	
	public CellEvent(){
		
	}
	
	public CellEvent(String slash,Phrase phrase){
		this.slash = slash;
		this.phrase = phrase;
	}

	public void cellLayout(PdfPCell cell, Rectangle position,
			PdfContentByte[] canvases){
		PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
		float width = cell.getWidth();
		float height = cell.getHeight();
		float left = position.getLeft();
		float right = position.getRight();
		float top = position.getTop();
		float bottom =  position.getBottom();		
		
		String content = phrase.getContent();
		if(slash!=null){
			// System.out.println(width+"--"+height+"--"+left+"--"+right+"--"+top+"--"+bottom);
		
			String str1 = content.substring(content.indexOf("str1")+5,content.indexOf("&", content.indexOf("str1")));
			String str2 = content.substring(content.indexOf("str2")+5,content.indexOf("&", content.indexOf("str2")));
			String str3 = null;
			if(content.indexOf("str3")>0){
				str3 = content.substring(content.indexOf("str3")+5,content.indexOf("&", content.indexOf("str3")));
			}
			
			
			
			Font font = phrase.getFont();
			cb.setFontAndSize(font.getBaseFont(), font.getSize());
			if("topDown".equals(slash)){
				cb.moveTo(left,top );
				cb.lineTo(right,bottom);
				cb.stroke();
/*				
				ColumnText coltext = new ColumnText(cb);
				coltext.setSimpleColumn(new Phrase(str1,phrase.getFont()), 
					left, left+width, top, top+height, font.getSize(), PdfContentByte.ALIGN_LEFT);
				try{
					coltext.go();
				}catch(Exception ex){
					ex.printStackTrace();
				}
*/				
				cb.beginText();		
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, str1, 
						left, bottom+BaseFont.ASCENT+BaseFont.DESCENT, 0);	
				cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, str2, 
						right, top-phrase.getLeading(), 0);
				cb.endText();   
				
			}else if("aggregation".equals(slash)){
				cb.moveTo(left+width/2, top);
				cb.lineTo(right, bottom);
				cb.moveTo(left, bottom+height/2);
				cb.lineTo(right, bottom);
				cb.stroke();
				
				cb.beginText();		
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, str1, 
						left, bottom+BaseFont.ASCENT+BaseFont.DESCENT, 0);	
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, str2, 
						left, top-(BaseFont.ASCENT+BaseFont.DESCENT)*2, 0);
				cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, str3, 
						right, top-phrase.getLeading(), 0);
				cb.endText(); 
			}else{
				cb.moveTo(left, top);
				cb.lineTo(right, bottom+height/2);
				cb.moveTo(left, top);
				cb.lineTo(left+width/2, bottom);
				cb.stroke();
					
				cb.beginText();		
				cb.showTextAligned(PdfContentByte.ALIGN_LEFT, str1, 
						left, bottom+BaseFont.ASCENT+BaseFont.DESCENT, 0);	
				cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, str2, 
						right, bottom, 0);
				cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, str3, 
						right, top-phrase.getLeading(), 0);
				cb.endText(); 
			}	
		}
		
		// if(modelCell.getRowspan()>1){
		if (gridcell.rowSpan > 1) {
			// System.out.println(width+"--"+height+"--"+left+"--"+right+"--"+top+"--"+bottom);
			cb.rectangle(position);
			cb.setColorFill(new Color(Integer.parseInt("000000",16)));
			cb.fill();
/*			
			Graphics2D g = cb.createGraphics(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			g.clearRect((int)left, (int)top, (int)right, (int)bottom);
			g.drawLine((int)right, (int)top,(int)left,(int) bottom);
			g.setColor(new Color(Integer.parseInt("000000",16)));
			g.dispose();
*/
		}

/*		
		byte[] b = modelCell.getPic();
		try{
		if(b!=null){
			Image image = Image.getInstance(b);
			image.scaleAbsolute(width, height);
			image.setAbsolutePosition(left, top);
			cb.addImage(image);
			
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
*/		
	}

	public Cell getModelCell() {
		return modelCell;
	}

	public void setModelCell(Cell modelCell) {
		this.modelCell = modelCell;
	}

	public Phrase getPhrase() {
		return phrase;
	}

	public void setPhrase(Phrase phrase) {
		this.phrase = phrase;
	}

	public String getSlash() {
		return slash;
	}

	public void setSlash(String slash) {
		this.slash = slash;
	}
	
	// 2009.07.13
	public void setGridCell(GridCell gridcell) {
		this.gridcell = gridcell;
	}
	
	public GridCell getGridCell() {
		return gridcell;
	}

}
