package com.cvicse.inforreport.classic.engine.datasource;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.cvicse.inforreport.model.Cell;
import com.cvicse.inforreport.model.DataDefine;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.Row;
import com.cvicse.inforreport.model.XMLDataDefine;

import junit.framework.TestCase;

public class XMLDefineProcessorTest extends TestCase {
	
	XMLDefineProcessor xml;
	InforReport report;

	protected void setUp() throws Exception {
		super.setUp();
		xml = new XMLDefineProcessor();
		report = new InforReport();
		report.init("E:\\CVICSE\\InforReport\\demo\\testxml.ipr");
	}

	public void testProcessData1()throws Exception {
		XMLDataDefine xdd =(XMLDataDefine)report.getDataset().get("dd");
		List list = xml.processData(xdd, null, null);
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}
	public void testProcessData2()throws Exception {
		XMLDataDefine xdd =(XMLDataDefine)report.getDataset().get("dd");
		xdd.setData(new FileInputStream("E:/CVICSE/InforReport/server/deploy/reportservice/WEB-INF/data/ds.xml"));
		List list = xml.processData(xdd, null, null);
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}
	public void testProcessData3()throws Exception {
		String data = "<?xml version=\"1.0\" encoding=\"GB2312\"?><employees><employee><id>4</id><name first=\"L\">Linda</name><date>2005-11-30</date><cost>960.01</cost></employee><employee><id>5</id><name first=\"S\">Sunny</name><date>2005-12-31</date><cost>1560.41</cost></employee></employees>";
		XMLDataDefine xdd =(XMLDataDefine)report.getDataset().get("dd");		
		xdd.setData(data);
		List list = xml.processData(xdd, null, null);
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}

}
