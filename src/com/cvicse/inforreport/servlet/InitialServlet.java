package com.cvicse.inforreport.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.engine.ReportEngineFactory;
import com.cvicse.inforreport.engine.ReportManager;

public class InitialServlet extends HttpServlet {
	
	private static final Log log = LogFactory.getLog(InitialServlet.class);
	
	public void init(ServletConfig servletconfig) throws ServletException {
		super.init(servletconfig);
		
		//ReportEngineFactory.initProvides(); //init engine

		//String filePath = servletconfig.getInitParameter("TemplatePath");
		
		//String configPath = servletconfig.getInitParameter("ConfigPath");
		File configPath = ReportManager.getInstance().getReportConfigHome();
		
		InputStream in = null;
		try {			
			//String path = getFilePath(servletconfig,new File(configPath,"datasource.xml"));
			String path = new File(configPath,"datasource.xml").toString();
			ReportEngineFactory.initDatasource(path);
			//path = getFilePath(servletconfig,new File(configPath,"businessmodel.xml"));
			File busifile = new File(configPath,"businessmodel.xml");
			path = busifile.toString();
			if(busifile.exists())
				ReportEngineFactory.initBusinessModel(path);
			
//			in = getFilePathInputStream(servletconfig,new File(configPath,"datasource.xml"));
//			ReportEngineFactory.initDatasource(in); //init datasource
//			in = getFilePathInputStream(servletconfig,new File(configPath,"businessmodel.xml"));
//			ReportEngineFactory.initBusinessModel(in); //init businessmodel
		} catch (Exception e) {
			log.error(e);
		}finally{
			try {
				if(in!=null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private String getFilePath(ServletConfig servletconfig, File file) throws Exception{
		String path = null;
		if(!file.isAbsolute()){
			path = servletconfig.getServletContext().getRealPath(file.toString());
		}else {
			path = file.toString();
		}
		return path;
	}
	
	private InputStream getFilePathInputStream(ServletConfig servletconfig, File file) throws Exception{
		InputStream in = null;
		if (!file.isAbsolute()) {
			in = servletconfig.getServletContext().getResourceAsStream(
					file.toString());
		} else {
			in = new FileInputStream(file);
		}

		return in;
	}

}
