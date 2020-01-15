package com.cvicse.inforreport.engine;

import com.cvicse.inforreport.api.IReportManager;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.manage.model.ReportInfo;
import com.cvicse.inforreport.manage.service.XMLManagerService;
import com.cvicse.inforreport.util.IRportHomeLocator;
import com.cvicse.inforreport.util.ReportHomeLocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ReportManager implements IReportManager {
	
	private static IReportManager manager = new ReportManager();

	static IRportHomeLocator locator = new ReportHomeLocator();
	static File config = new File(locator.locateReportHome(), "config");
	static File templates = new File(locator.locateReportHome(), "templates");
	static XMLManagerService xms = new XMLManagerService(new File(config,"reportInfo.xml").toString());
	
	private ReportManager(){		
	}
	
	public static IReportManager getInstance(){
		return manager;
	}
	
	public File download(String fileName, String path) throws ReportException{
		File reportfile = new File(path);
		if (!reportfile.isAbsolute()) {
			reportfile = new File(templates, path);
			//reportfile = new File(reportfile, fileName);
		}
		//File reportfile = new File(templates, path);
		reportfile = new File(reportfile, fileName);
		if(reportfile.isDirectory()||!reportfile.exists() )
			throw new ReportException("文件不存在! ");
		
		return reportfile;
	}
	
	public void upload(String reportName, File file, String path) throws ReportException {
		try {
			File reportfile = file;
			if (!file.isAbsolute()) {
				reportfile = new File(templates, path);
				reportfile = new File(reportfile, file.toString());
			}
			boolean bool= false;
			if (reportfile.isFile() && reportfile.exists())
				bool=true;
			//	throw new ReportException("文件已存在! ");
			if(xms.exists(reportName))
				throw new ReportException("报表标题已存在.");
			byte[] buffer = new byte[1024];
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(reportfile);
			int length = -1;
			while ((length = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, length);
			}
			fis.close();
			fos.close();
			
			if (bool)
				editReportInfo(new String[] {
						reportName,
						file.getName(),
						path,
						file.getName().substring(
								file.getName().lastIndexOf(".") + 1),
						reportName, "" });
			else
				addReportInfo(new String[] {
						reportName,
						file.getName(),
						path,
						file.getName().substring(
								file.getName().lastIndexOf(".") + 1),
						reportName, "" });
		} catch (Exception e) {
			e.printStackTrace();
			throw new ReportException(e.getMessage());
		}

	}

	public void upload(String reportName, String fileName, String content,
			String path) throws ReportException {
		try {
			File file = new File(fileName);
			if (!file.isAbsolute()) {
				file = new File(templates, path);
				file = new File(file, fileName);
			}
			boolean bool= false;
			if (file.isFile() && file.exists())
				bool=true;
			//	throw new ReportException("文件已存在! ");
			//if(xms.exists(reportName))
			//	throw new ReportException("报表标题已存在.");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.close();
			
			if (bool)
				editReportInfo(new String[] { 
						reportName, 
						file.getName(), 
						path,
						fileName.substring(fileName.lastIndexOf(".") + 1),
						reportName, 
						"" });
			else
				addReportInfo(new String[] { 
						reportName, 
						file.getName(), 
						path,
						fileName.substring(fileName.lastIndexOf(".") + 1),
						reportName, 
						"" });
				

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ReportException(e.getMessage());
		}
	}

	public void delete(String fileName, String path) throws ReportException {
		File file = new File(fileName);
		if(!file.isAbsolute()){
			file = new File(templates, path);
			file = new File(file, fileName);
		}
		if(file.isDirectory()||!file.exists())
			throw new ReportException("删除失败, 文件不存在.");
		file.delete();
		
		delReportInfo(fileName, path);
	}

	public File getReportHome() {
		return locator.locateReportHome();
	}

	public File getReportConfigHome() {
		File config = new File(locator.locateReportHome(), "config");
		return config;
	}

	public File getReportTemplatesHome() {
		return templates;
	}
	
	public void addReportInfo(String[] reportInfoAttrs){
//		XMLManagerService xms = new XMLManagerService(new File(config,
//				"reportInfo.xml").toString());
		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setName(reportInfoAttrs[0]);
		reportInfo.setFileName(reportInfoAttrs[1]);
		reportInfo.setDir(reportInfoAttrs[2]);
		reportInfo.setType(reportInfoAttrs[3]);
		reportInfo.setRemark(reportInfoAttrs[4]);
		reportInfo.setUrl(reportInfoAttrs[5]);
		xms.add(reportInfo);
		
	}
	
	public void editReportInfo(String[] reportInfoAttrs){
//		XMLManagerService xms = new XMLManagerService(new File(config,
//				"reportInfo.xml").toString());
		ReportInfo reportInfo = new ReportInfo();
		reportInfo.setName(reportInfoAttrs[0]);
		reportInfo.setFileName(reportInfoAttrs[1]);
		reportInfo.setDir(reportInfoAttrs[2]);
		reportInfo.setType(reportInfoAttrs[3]);
		reportInfo.setRemark(reportInfoAttrs[4]);
		reportInfo.setUrl(reportInfoAttrs[5]);
		xms.edit(reportInfo);
		
	}
	
	public void delReportInfo(String fileName,String path){
		ReportInfo info = new ReportInfo();
		info.setFileName(fileName);
		info.setDir(path);
//		XMLManagerService xms = new XMLManagerService(new File(config,
//				"reportInfo.xml").toString());
		xms.delete(info);
	}
}
