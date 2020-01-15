package com.cvicse.inforreport.classic.engine.servlet;

import java.io.File;
import java.io.FileOutputStream;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.api.IReportEngine;
import com.cvicse.inforreport.classic.engine.ConfigurationManager;
import com.cvicse.inforreport.classic.engine.StandardReportEngine;
import com.cvicse.inforreport.engine.ReportEngineFactory;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.util.StrUtil;
import com.cvicse.inforreport.util.EngineUtils;

public class DesignerShow {
	//private static final Log log = LogFactory.getLog(DesignerShow.class);

	public DesignerShow() {
		// TODO Auto-generated constructor stub
	}

	public String showReport(String template) throws ReportException {
		File tmpFile = null;
		File tmpDir = null;
		StringBuffer buffer = new StringBuffer();
		try {
			String dir = System.getProperty("user.home");
			dir += File.separator + "." + EngineUtils.getResourceValue("Product")
					+ File.separator;
			tmpDir = new File(dir);
			if (!(tmpDir.exists() && tmpDir.isDirectory())) {
				tmpDir.mkdirs();
			}

			tmpFile = new File(dir, StrUtil.getTempFileName() + ".ipr");
			FileOutputStream fos = null;
			fos = new FileOutputStream(tmpFile);
			fos.write(template.getBytes());
			fos.close();
			String reportContent = "";
			// 2009.07.28 设计器支持新主从的预览
			// StandardReportEngine re = StandardReportEngine.getInstance();
			IReportEngine engine = ReportEngineFactory.getReportEngine(tmpFile.toString());
			reportContent = engine.getReport(tmpFile.toString(), null);

			String classid = ConfigurationManager.getInstance().getClassID();
			String pdfUrl = ConfigurationManager.getInstance()
					.getExportServiceURL();
			String version = ConfigurationManager.getInstance()
					.getViewerVersion();
			if (version == null || version.trim().equals("")) {
				version = "3,2,0,0";
			}
			buffer
					.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
			buffer.append("<html>\n");
			buffer.append("<head>\n");
			buffer
					.append("      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
			buffer.append("      <title>Preview</title>\n");
			buffer.append("      </head>\n");
			buffer.append("      <body>\n");
			buffer.append("      <p>\n");
			buffer
					.append("        <OBJECT ID=\"IRVCtl\" style=\"WIDTH: 100%; HEIGHT: 100%\"\n");
			buffer.append("      CLASSID=\"" + classid + "\"\n");
			buffer.append(" CODEBASE=\"../InforReportViewer.CAB"
					//+ EngineUtils.getResourceValue("ViewerName") 
					+ "#version="
					+ version + "\">\n");

			buffer.append("          <PARAM Name=\"report\" VALUE=\""
					+ reportContent + "\">\n");
			buffer
					.append("          <PARAM Name=\"showtoolbar\" VALUE=\"true\">\n");
			buffer
					.append("          <PARAM Name=\"showErrLevel\" VALUE=\"0\">\n");

			buffer
					.append("          <PARAM Name=\"ExportServiceURL\" VALUE=\"");
			buffer.append(pdfUrl);
			buffer.append("\">\n");
			buffer.append("        </OBJECT>\n");
			buffer.append("      </p>\n");
			buffer.append("      </body>\n");
			buffer.append("      </html>\n");

		} catch (Throwable ex) {
			//log.error(Utils.getResourceValue("ShowReportError"), ex);
			ex.printStackTrace();
			throw new ReportException(ex.getMessage());
		} finally {
			if (tmpFile != null) {
				if (tmpFile.exists() && tmpFile.isFile()) {
					tmpFile.delete();
					tmpFile.deleteOnExit();
				}
			}
			if (tmpDir != null) {
				File[] files = tmpDir.listFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						files[i].delete();
						files[i].deleteOnExit();
					}
				}
			}
		}
		return buffer.toString();
	}
}
