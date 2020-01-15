package com.cvicse.inforreport.classic.engine.datasource;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cvicse.inforreport.model.Cell;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.OBJDataDefine;
import com.cvicse.inforreport.model.Row;
import com.cvicse.inforreport.model.TXTDataDefine;
import com.cvicse.inforreport.model.XMLDataDefine;

import junit.framework.TestCase;

public class DatasetProcessorTest extends TestCase {
	
	InforReport report;
	DatasetProcessor dp;

	protected void setUp() throws Exception {
		super.setUp();
		report = new InforReport();
	}

	public void testGetAllDataDataset1() throws Exception {
		report.init("E:\\CVICSE\\InforReport\\demo\\testxml.ipr");
		List data = new ArrayList();
		List cells1 = new ArrayList();
		cells1.add(new Cell("aaa"));
		cells1.add(new Cell("bbb"));
		cells1.add(new Cell("ccc"));
		data.add(new Row(cells1));
		List cells2 = new ArrayList();
		cells2.add(new Cell("ddd"));
		cells2.add(new Cell("eee"));
		cells2.add(new Cell("fff"));
		data.add(new Row(cells2));
		XMLDataDefine xdd =(XMLDataDefine)report.getDataset().get("dd");
		//xdd.setData(data);
		xdd.setData(new FileInputStream("E:/CVICSE/InforReport/server/deploy/reportservice/WEB-INF/data/ds.xml"));
		dp = new DatasetProcessor(report.getDataset());
		Map map = dp.getAllData(null, null);
		List list = (List)map.get("dd");
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}		
		System.out.println("");

		List list1 = (List)map.get("d");
		for(int i=0;i<list1.size();i++){
			List cells = ((Row)list1.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}

	public void testGetAllDataDataset2() throws Exception {
		report.init("E:\\CVICSE\\InforReport\\demo\\testtxt.ipr");
		List data = new ArrayList();
		List cells1 = new ArrayList();
		cells1.add(new Cell("aaa"));
		cells1.add(new Cell("bbb"));
		cells1.add(new Cell("ccc"));
		data.add(new Row(cells1));
		List cells2 = new ArrayList();
		cells2.add(new Cell("ddd"));
		cells2.add(new Cell("eee"));
		cells2.add(new Cell("fff"));
		data.add(new Row(cells2));
		TXTDataDefine xdd =(TXTDataDefine)report.getDataset().get("txt");
		xdd.setData(data);
		//xdd.setData(new FileInputStream("E:/CVICSE/InforReport/server/deploy/reportservice/WEB-INF/data/ds.xml"));
		dp = new DatasetProcessor(report.getDataset());
		Map map = dp.getAllData(null, null);
		List list = (List)map.get("txt");
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}		
		System.out.println("");

		List list1 = (List)map.get("d");
		for(int i=0;i<list1.size();i++){
			List cells = ((Row)list1.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}
	}
	
	public void testGetAllDataDataset3() throws Exception {
		System.out.println("");
		System.out.println("testGetAllDataDataset3:");
		report.init("E:\\CVICSE\\InforReport\\demo\\4.2\\obj.ipr");
		List data = new ArrayList();
		List cells1 = new ArrayList();
		cells1.add(new Cell("aaa"));
		cells1.add(new Cell("bbb"));
		cells1.add(new Cell("ccc"));
		data.add(new Row(cells1));
		List cells2 = new ArrayList();
		cells2.add(new Cell("ddd"));
		cells2.add(new Cell("eee"));
		cells2.add(new Cell("fff"));
		data.add(new Row(cells2));
		OBJDataDefine xdd =(OBJDataDefine)report.getDataset().get("obj");
		xdd.setData(data);
		//xdd.setData(new FileInputStream("E:/CVICSE/InforReport/server/deploy/reportservice/WEB-INF/data/ds.xml"));
		dp = new DatasetProcessor(report.getDataset());
		Map map = dp.getAllData(null, null);
		List list = (List)map.get("obj");
		for(int i=0;i<list.size();i++){
			List cells = ((Row)list.get(i)).getCells();
			System.out.println("");
			for(int j=0;j<cells.size();j++){
				System.out.print(((Cell)cells.get(j)).getCellContent()+";");
			}
		}		
		System.out.println("");
	}
}
