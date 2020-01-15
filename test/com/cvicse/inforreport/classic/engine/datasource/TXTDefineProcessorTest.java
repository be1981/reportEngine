package com.cvicse.inforreport.classic.engine.datasource;

import java.io.FileInputStream;
import java.util.List;

import com.cvicse.inforreport.model.Cell;
import com.cvicse.inforreport.model.DataDefine;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.Row;
import com.cvicse.inforreport.model.TXTDataDefine;

import junit.framework.TestCase;

public class TXTDefineProcessorTest extends TestCase {
	
	InforReport report;
	TXTDefineProcessor txt;

	protected void setUp() throws Exception {
		super.setUp();
		txt = new TXTDefineProcessor();
		report = new InforReport();
		report.init("E:\\CVICSE\\InforReport\\demo\\testtxt.ipr");
	}

	public void testProcessData1() throws Exception{
		DataDefine tdd =(DataDefine)report.getDataset().get("d");
		List list = txt.processData(tdd,null, null);
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}
	
	public void testProcessData2() throws Exception{
		TXTDataDefine tdd =(TXTDataDefine)report.getDataset().get("d");
		tdd.setData(new FileInputStream("E:/CVICSE/InforReport/server/deploy/reportservice/WEB-INF/data/test1.txt"));
		List list = txt.processData(tdd,null, null);
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}
	
	public void testProcessData3() throws Exception{
		TXTDataDefine tdd =(TXTDataDefine)report.getDataset().get("txt");
		tdd.setData(new FileInputStream("E:/CVICSE/InforReport/server/deploy/reportservice/WEB-INF/data/test1.csv"));
		List list = txt.processData(tdd,null, null);
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}
}
