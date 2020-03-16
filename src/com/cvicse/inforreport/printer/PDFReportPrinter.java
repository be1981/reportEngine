package com.cvicse.inforreport.printer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.api.IReportEngine;
import com.cvicse.inforreport.api.IReportExporter;
import com.cvicse.inforreport.engine.ReportEngineFactory;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.exporter.ReportExporterFactory;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.ReportData;

public class PDFReportPrinter {
	private static final Log log = LogFactory.getLog(PDFReportPrinter.class);

	public String createPrintFile(String fileName, String rootPath, String parameterString, String paralistcode) {
		Date datetime = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		String path = "pdfoutput" + File.separator + sdf1.format(datetime);
		int pathEndIndex = rootPath.lastIndexOf(File.separator);
		String file = rootPath.substring(0, pathEndIndex) + File.separator + path;

		try {
			File dir = new File(file);
			if (!dir.exists()) {// 判断目录是否存在
				dir.mkdirs(); // 多层目录需要调用mkdirs
				System.out.println("创建目录" + file);
				log.info("创建目录" + file);
			}
			int startIndex = fileName.lastIndexOf("/") + 1;
			int endIndex = fileName.lastIndexOf(".");
			path = path + File.separator + fileName.substring(startIndex, endIndex) + datetime.getTime() + ".pdf";
			file = rootPath.substring(0, pathEndIndex) + File.separator  + path;

			Map tempParameters = new HashMap();
			if (parameterString != null && !"".equals(parameterString.trim())) {
				tempParameters = convertStringtoMap(parameterString);
			}

			// 初始化引擎
			IReportEngine engine = ReportEngineFactory.getReportEngine(fileName);
			ReportData reportData = engine.getReportData(fileName, tempParameters);
			if (reportData != null) {
				InforReport report = reportData.getReport();
				FileOutputStream fos = new FileOutputStream(new File(file));
				IReportExporter reportExporter = ReportExporterFactory.getReportExporter("PDF");
				reportExporter.exportReport(report, fos);
				fos.flush();
				fos.close();
				path = path.replace("\\", "/");
				return path;
			}
		} catch (FileNotFoundException e) {
			log.error("文件未找到:" + file);
			e.printStackTrace();
		} catch (ReportException e) {
			log.error("导出PDF文件失败!" + file);
			e.printStackTrace();
		} catch (IOException e) {
			log.error("写文件失败!" + file);
			e.printStackTrace();
		}

		return "";
	}

	public String createPrintFile(String fileName, String rootPath, Map tempParameters, String paralistcode) {
		Date datetime = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		String path = "pdfoutput" + File.separator + sdf1.format(datetime);
		int pathEndIndex = rootPath.lastIndexOf(File.separator);
		String file = rootPath.substring(0, pathEndIndex) + File.separator + path;

		try {
			File dir = new File(file);
			if (!dir.exists()) {// 判断目录是否存在
				dir.mkdirs(); // 多层目录需要调用mkdirs
				System.out.println("创建目录" + file);
				log.info("创建目录" + file);
			}
			int startIndex = fileName.lastIndexOf("/") + 1;
			int endIndex = fileName.lastIndexOf(".");
			path = path + File.separator + fileName.substring(startIndex, endIndex) + datetime.getTime() + ".pdf";
			file = rootPath.substring(0, pathEndIndex) + File.separator  + path;

			if (tempParameters == null) {
				tempParameters = new HashMap();
			}

			// 初始化引擎
			IReportEngine engine = ReportEngineFactory.getReportEngine(fileName);
			ReportData reportData = engine.getReportData(fileName, tempParameters);
			if (reportData != null) {
				InforReport report = reportData.getReport();
				FileOutputStream fos = new FileOutputStream(new File(file));
				IReportExporter reportExporter = ReportExporterFactory.getReportExporter("PDF");
				reportExporter.exportReport(report, fos);
				fos.flush();
				fos.close();
				path = path.replace("\\", "/");
				return path;
			}
		} catch (FileNotFoundException e) {
			log.error("文件未找到:" + file);
			e.printStackTrace();
		} catch (ReportException e) {
			log.error("导出PDF文件失败!" + file);
			e.printStackTrace();
		} catch (IOException e) {
			log.error("写文件失败!" + file);
			e.printStackTrace();
		}

		return "";
	}
	
	/**
	 * String串转化为Map
	 * 
	 * @param paramerterStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map convertStringtoMap(String paramerterStr) {
		Map tempMap = new HashMap();
		if (paramerterStr != null && !"".equals(paramerterStr.trim())) {
			try {
				String[] paramsArray = paramerterStr.split(";/");
				if (paramsArray != null && paramsArray.length > 0) {
					for (int i = 0; i < paramsArray.length; i++) {
						tempMap.put(paramsArray[i].substring(0, paramsArray[i].indexOf(":")),
								paramsArray[i].substring(paramsArray[i].indexOf(":") + 1));
					}
				}
			} catch (Exception e) {
				return null;
			}
		}
		return tempMap;
	}
}
