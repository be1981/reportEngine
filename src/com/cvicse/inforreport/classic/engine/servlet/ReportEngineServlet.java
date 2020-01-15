package com.cvicse.inforreport.classic.engine.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.api.IReportEngine;
import com.cvicse.inforreport.classic.engine.StandardReportEngine;
import com.cvicse.inforreport.engine.ReportEngineFactory;
import com.cvicse.inforreport.util.EngineUtils;

public class ReportEngineServlet extends HttpServlet {
	
	private static final Log log = LogFactory.getLog(ReportEngineServlet.class);
	
	private static final String CONTENT_TYPE = "text/plain; charset=ISO-8859-1";

	private IReportEngine engine;

	/** @link dependency */
	/*# StandardReportEngine lnkStandardReportEngine; */
	//Initialize global variables
	public void init() throws ServletException {
		/*
		try {
			engine = StandardReportEngine.getInstance();
		} catch (Throwable ex) {
			Utils.fatal(ex.getMessage(), ex);
			throw new ServletException(ex.getMessage(), ex);
		}*/
	}

	//Process the HTTP Post request
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
  		String templateName = request.getParameter("templateName");
		if (templateName == null || templateName.trim().equals("")) {
			log.error(EngineUtils.getResourceValue("NullTemplate"));
			throw new ServletException(EngineUtils
					.getResourceValue("NullTemplate"));
		}
		templateName = new String(templateName.getBytes("ISO-8859-1"));
		log.debug("Template Name: " + templateName);

		try {
			//engine = ReportEngineFactory.getReportEngine(templateName);
			engine = StandardReportEngine.getInstance();  //4.2引擎
		} catch (Exception e) {
			String error = e.getMessage();
			error = EngineUtils.encodeBase64(error);
			response.sendError(500, error);
			return;

		}
		 
		Enumeration headerNames = request.getHeaderNames();
		String headerName = null;
		String headerValue = null;

		log.debug(""); 
		log.debug("");
		log.debug("HTTP Headers: ");
		while (headerNames.hasMoreElements()) {
			headerName = (String) headerNames.nextElement();
			headerValue = request.getHeader(headerName);
			log.debug("    " + headerName + ": " + headerValue);
		}
		log.debug(""); 

		request.setCharacterEncoding("ISO-8859-1");
		response.setContentType(CONTENT_TYPE);
		String action = request.getParameter("action");

		if (action == null || action.trim().equals("")) {
			log.error(EngineUtils.getResourceValue("ActionisNull"));			
			throw new ServletException(EngineUtils.getResourceValue("ActionisNull"));
		}
		if (action.trim().startsWith("getReport")) {
			getReport(request, response, templateName,action);
		} 

	}
	

	/**
	 * 处理展示报表请求
	 * @param request
	 * @param response
	 * @param action
	 * @throws IOException
	 * @throws ServletException
	 */
	private void getReport(HttpServletRequest request,
			HttpServletResponse response, String templateName,String action) throws IOException,
			ServletException {
		log.debug("Report Action: " + action);

//		String templateName = request.getParameter("templateName");
//		if (templateName == null || templateName.trim().equals("")) {
//			log.error(EngineUtils.getResourceValue("NullTemplate"));
//			throw new ServletException(EngineUtils.getResourceValue("NullTemplate"));
//		}
//		templateName = new String(templateName.getBytes("ISO-8859-1"));
//		log.debug("Template Name: " + templateName);

		Map parameters = new HashMap();
		Enumeration keys = request.getParameterNames();
		String report = null;

		String key = null;
		String value = null;
		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			if ((!key.trim().equals(""))
					&& (!key.trim().equalsIgnoreCase("action"))
					&& (!key.trim().equalsIgnoreCase("templateName"))) {
				value = request.getParameter(key);
				log.debug(key + ": " + value);
				value = new String(value.getBytes("ISO-8859-1"));
				parameters.put(key, value);
				log.debug(key + ": " + value);
			}
		}

		if (action.trim().equalsIgnoreCase("getReport1")) {

			try {
				report = engine.getReport(templateName, parameters);
			} catch (Throwable t) {
				String error = t.getMessage();
				//        Utils.fatal(t.getMessage(), t);
				error = EngineUtils.encodeBase64(error);
				//        System.out.println("servlet1: "+error);
				//        error = new String(error.getBytes("GBK"));
				//        System.out.println("servlet2: "+error);

				//        response.setHeader("1001",error);
				//        response.setHeader("Content-Type","test/plain");
				response.sendError(500, error);
				return;
				//        response.sendError(500,error);

				//        String error = t.getMessage();
				//        error = new String(error.getBytes("GBK"));
				//        System.out.println("error: "+error);
				//        response.setContentType("text/plain; charset=gb2312");
				//        response.setHeader("1001",error);
				//        response.setStatus(1001,error);
				//        PrintWriter out = response.getWriter();
				//        out.write(error);
				//        Utils.fatal(error, t);
				//        out.close();
				//        return;

				//        Utils.fatal(t.getMessage(), t);
				//        throw new ServletException(t.getMessage(), t);
			}

			try {
				byte[] reportContent = report.getBytes();
				response.setContentLength(reportContent.length);
				OutputStream out = response.getOutputStream();
				out.write(reportContent);
				out.close();
			} catch (Throwable ex) {
				log.error(ex.getMessage(),ex);
				response.sendError(500, EngineUtils.encodeBase64(ex.getMessage()));
			}

		}
	}



}
