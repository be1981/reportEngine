package com.cvicse.inforreport.manage.service;

import java.io.IOException;
import java.util.List;

import com.cvicse.inforreport.manage.model.ReportInfo;
import com.cvicse.inforreport.manage.parser.XMLConfigHandle;

public class XMLManagerService {
	private XMLConfigHandle configHandle;

	public XMLManagerService(String path) {
		configHandle = new XMLConfigHandle(path);
	}

	public void add(ReportInfo reportInfo) {
		configHandle.add(reportInfo);
	}

	public void delete(ReportInfo reportInfo) {
		String filename = reportInfo.getFileName();
		String dir = reportInfo.getDir();
		configHandle.delete(filename, dir);
	}

	public void edit(ReportInfo reportInfo) {
		configHandle.edit(reportInfo);
	}

	public ReportInfo get(String filename, String dir) {
		return configHandle.get(filename, dir);
	}

	public List query(String dir) {
		List reportInfos = null;
		reportInfos = configHandle.query(dir);
		return reportInfos;
	}
	
	public boolean exists(String name){
		return configHandle.exists(name);
	}
	

	public static void main(String args[]) {
		XMLManagerService XMLManagerHandle = new XMLManagerService(
				"c:/reportInfo.xml");
		List list = XMLManagerHandle.query("a");
		for (int i = 0; i < list.size(); i++) {
			ReportInfo reportInfo = (ReportInfo) list.get(i);
			System.out.println(reportInfo.getFileName());
		}
		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setFileName("test4.ipr");
		reportInfo.setDir("b");
		reportInfo.setName("测试报表111");
		reportInfo.setType("ipr");
//		XMLManagerHandle.add(reportInfo);
		// XMLManagerHandle.delete(reportInfo);
	}
}
