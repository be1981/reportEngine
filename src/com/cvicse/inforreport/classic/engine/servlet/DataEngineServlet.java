package com.cvicse.inforreport.classic.engine.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.cvicse.inforreport.classic.engine.ConfigurationManager;
import com.cvicse.inforreport.classic.engine.LargeData;
import com.cvicse.inforreport.classic.engine.StandardReportEngine;
import com.cvicse.inforreport.engine.ReportManager;
import com.cvicse.inforreport.handler.HandlerChain;
import com.cvicse.inforreport.handler.HandlerChainFactory;
import com.cvicse.inforreport.model.DataDefine;
import com.cvicse.inforreport.model.Dataset;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.Parameter;
import com.cvicse.inforreport.model.SQLDataDefine;
import com.cvicse.inforreport.util.DBConnection;
import com.cvicse.inforreport.util.EntityResolverImpl;
import com.cvicse.inforreport.util.Utils;

public class DataEngineServlet extends HttpServlet {
	
	private static final Log log = LogFactory.getLog(DataEngineServlet.class);
	
	//private static final String CONTENT_TYPE = "text/plain; charset=ISO-8859-1";
	private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

	private StandardReportEngine engine;

	private HandlerChain handlerChain;

	/**
	 * Initialize global variables
	 */
	public void init() throws ServletException {
		/*
		 * try { engine = StandardReportEngine.getInstance(); } catch (Throwable
		 * ex) { Utils.fatal(ex.getMessage(), ex); throw new
		 * ServletException(ex.getMessage(), ex); }
		 */
		try {
			handlerChain = HandlerChainFactory.createHandlerChain("LargeDataChain");
		} catch (Throwable t) {
			log.error(t.getMessage(),t);
			throw new ServletException(t);
		}
	}

	/**
	 * Process the HTTP Post request
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Enumeration names = request.getParameterNames();
		String name = null;
		String value = null;
		
		while (names.hasMoreElements()) {
			name = (String) names.nextElement();
			value = request.getParameter(name);
			log.debug("    " + names + ": " + value);
		}

		try {
			engine = StandardReportEngine.getInstance();
		} catch (Throwable t) {
			String error = t.getMessage();
			response.sendError(500, Utils.encodeBase64(error));
			return;
		}

		Enumeration headerNames = request.getHeaderNames();
		String headerName = null;
		String headerValue = null;
		log.debug("");
		log.debug("DataEngineServlet HTTP Headers: ");
		while (headerNames.hasMoreElements()) {
			headerName = (String) headerNames.nextElement();
			headerValue = request.getHeader(headerName);
			log.debug("    " + headerName + ": " + headerValue);
		}

		//request.setCharacterEncoding("ISO-8859-1");
		response.setContentType(CONTENT_TYPE);
		String action = request.getParameter("action");

		if (action == null || action.trim().equals("")) {
			log.info(Utils.getResourceValue("ActionisNull"));			
			throw new ServletException(Utils.getResourceValue("ActionisNull"));
		}
		if (action.trim().startsWith("largedata")) {
			doFirstLargeData(request, response, action);
		} else if (action.trim().startsWith("getdata")) {
			doAfterLargeData(request, response, action);
		}

	}

	/**
	 * 处理大数据模板请求
	 * 
	 * @param request
	 * @param response
	 * @param action
	 * @throws IOException
	 * @throws ServletException
	 */
	private void doFirstLargeData(HttpServletRequest request,
			HttpServletResponse response, String action) throws IOException,
			ServletException {
		log.debug("Data Action: " + action);
		PrintWriter pw = response.getWriter();		
		try {
			/* 取得模板名 */
			String name = request.getParameter("template");
			if (name == null || name.trim().equals("")) {
				log.info(Utils.getResourceValue("NullTemplate"));
				throw new ServletException(Utils.getResourceValue("NullTemplate"));
			}

			/* 取得外部参数 */
			String vpara = request.getParameter("vpara");
			Map conditions = this.enStringToMap(vpara,"vpara");

			/* 初始化模板 */
			File file = new File(Utils.decodeHttpBase64(name));
			if (!file.isAbsolute()) {
				file = new File(ReportManager.getInstance().getReportTemplatesHome(), file.toString());

			}
			InforReport template = new InforReport();
			template.init(file.toString());

			String report = null;
			Dataset ds = template.getDataset();
			String[] page = {"false"};	
			if (ds != null) { //如果模板中有数据集
				LargeData data = new LargeData();
				
				log.debug("set vpara");
				data.setConditions(conditions);
				String step = request.getParameter("step");
				data.setStep((step != null && !step.equals("")) ? step : "100");
				log.debug("set step: " + step);
				log.debug("set cpara");
				Map parameters = template.getParameters();
				data.setParameters(parameters);

				// 遍历dataset中是否有pages，并获得totalcount 
				Map datas = ds.getDataDefines();
				int totalcount = this.getTotalCount(data,datas,page,conditions,parameters);					
				
				// 如果有pages=true,调用LargeData的方法，并传入第一个dataDefine 
				if (page[0].equals("true")) {
					data.setDataDefine(this.getFirstDataDefine(datas));
					report = this.getFirstLargeData(data,template.getDocument(),totalcount);
				}
			}
			if (ds == null || page[0].equals("false")) { //如果无数据集或不需要分页，调用以前的getReport()方法
//				Utils.debug("call StandardEngine");
				log.debug("call StandardEngine");
				report = new StringBuffer("template=")
						.append(engine.getReport(template, conditions))
						.append("&")
						.toString();
			} 
			outputReportForDebug(report, "report.xml");
//			Utils.debug("response to client");
			log.debug("response to client");
			pw.print(report);

		} catch (Throwable ex) {
//			Utils.error("Error in getFirstLargeData: "+ex.getMessage(), ex);
			log.error("Error in getFirstLargeData: "+ex.getMessage(), ex);
			String err = ex.getMessage();
	    	if(err!=null)
	    		err = new String(err.getBytes(),"ISO-8859-1");
			response.sendError(500, "Error in getFirstLargeData: "+err);
			return;
			//throw new ServletException(ex);
		} 
		pw.flush();
		pw.close();

	}
	
	/**
	 * 获取totalcount，判断是否需要分页处理
	 * @param data LargeData对象
	 * @param dataDefines 所有DataDefine对象的Map映射
	 * @param page 分页标识
	 * @param conditions
	 * @param parameters
	 * @return
	 * @throws Throwable
	 */
	public int getTotalCount(LargeData data,Map dataDefines,String[] page,Map conditions,Map parameters)throws Throwable{
		int totalcount = 0; // SQL数据集的总count数
		Iterator it = dataDefines.keySet().iterator();
		DataDefine dataDefine = null;
		int i=0;
		log.debug("compute total record: ");
		while (it.hasNext()) {
			dataDefine = (DataDefine) dataDefines.get((String) it.next());
			log.debug("- - datadefine id: " + dataDefine.getId());
			if ("sql".equals(dataDefine.getType()) ){
				String sql = ((SQLDataDefine) dataDefine).replaceVmark(conditions,parameters);//处理$V!
				Connection conn = DBConnection.getInstance().getConnection(dataDefine.getSource());
				int count = data.getCount(dataDefine,conn,sql); //计算count
//				if (count >= 10000)   //记录数大于10K自动按照分页处理?
//					page[0] = "true";
				totalcount += count;
				
				((SQLDataDefine) dataDefine).setCount(count);				
				((SQLDataDefine) dataDefine).setSql(sql); // 原sql设置为替换完$V!的sql
				if(i==0)
					((SQLDataDefine) dataDefine).setCon(conn);
				else{
					conn.close();
				}
				
				if (dataDefine.getPages() != null
						&& !dataDefine.getPages().equals("")
						&& !dataDefine.getPages().equals("false")) 
					page[0] = "true";
					// break;	
				i++;
			}		
			
		}
//		Utils.debug("total record is: "+totalcount);
		log.debug("total record is: "+totalcount);
		return totalcount;
	}
	
	/**
	 * 获得dataDefines中第一个DataDefine对象的第一页数据
	 * @param data LargeData对象
	 * @param templateDom 模板Dom对象
	 * @param totalcount SQL数据集的总记录条数
	 * @return
	 * @throws Throwable
	 */
	public String getFirstLargeData(LargeData data,Document templateDom,int totalcount) throws Throwable{
//		Utils.debug("call LargeDataEngine");	
		log.debug("call LargeDataEngine");

		/* 构造返回的字符串 */
		String encoding = templateDom.getRootElement().attributeValue(
				"encoding");
		if (encoding == null || encoding.equals(""))
			encoding = "GBK";
		String content = Utils.encodeBase64ForCheckSQL(Utils
				.convertToString(templateDom, "", false, encoding));

		String reportData = Utils.encodeBase64ForCheckSQL(data
				.processToString(handlerChain, encoding)); // 数据

		String finished = new Boolean(data.isFinished()).toString();// 完成标识
		String dsId = data.getDataDefine().getId(); // dsId

//		Utils.debug("build response content");
		log.debug("build response content");
		String report = new StringBuffer("template=" + content).append(
				"&data=" + reportData)
				.append("&finish=" + finished).append(
						"&dsid=" + dsId).append("&count=" + totalcount)
				.append("&").toString();
		return report;		
	}
	
	/**
	 * 从DataDefine对象列表中获取第一个对象
	 * @param dataDefines
	 * @return
	 * @throws Throwable
	 */
	private DataDefine getFirstDataDefine(Map dataDefines) throws Throwable{
		DataDefine dataDefine = null;
		Iterator it = dataDefines.keySet().iterator();
		while (it.hasNext()) {
//			Utils.debug("set first dataDefine");
			log.debug("set first dataDefine");
			String id = (String) it.next();
			dataDefine = (DataDefine) dataDefines.get(id);
			break;
		}
		return dataDefine;
	}

	/**
	 * 处理大数据集请求
	 * 
	 * @param request
	 * @param response
	 * @param action
	 * @throws IOException
	 * @throws ServletException
	 */
	private void doAfterLargeData(HttpServletRequest request,
			HttpServletResponse response, String action) throws IOException,
			ServletException {
//		Utils.debug("Data Action: " + action);
		log.debug("Data Action: " + action);

		PrintWriter pw = response.getWriter();

		// 单个数据集
		String ds = request.getParameter("ds");
		if (ds == null || ds.trim().equals("")) {
//			Utils.info("[ds] is null");
			log.info("[ds] is null");
			throw new ServletException("[ds] is null");
		}
		// 起始数
		String firstRow = request.getParameter("fstRecord");
		if (firstRow == null || firstRow.trim().equals("")) {
//			Utils.info("[fstRecord] is null");
			log.info("[fstRecord] is null");
			throw new ServletException("[fstRecord] is null");
		}

		try {
			LargeData data = new LargeData();			

			// 起始数firstRow
//			Utils.debug("fstRecord: " + firstRow);
			log.debug("fstRecord: " + firstRow);
			data.setFstRecord(firstRow);
			// 每页行数step
			String step = request.getParameter("step");
			data.setStep((step != null && !step.equals("")) ? step : "100");
//			Utils.debug("step: " + step);
			log.debug("step: " + step);
			// 外部参数conditions
			String vpara = request.getParameter("vpara");
			Map conditions = this.enStringToMap(vpara,"vpara");
			data.setConditions(conditions);

			// 模板参数parameters
			String cpara = request.getParameter("cpara");
			Map parameters = this.enStringToMap(cpara,"cpara");
			data.setParameters(parameters);

			//单个数据集ds
			String ds1 = this.decode(ds);
//			Utils.debug("ds: " + ds1);
			log.debug("ds: " + ds1);
			data.setDataDefine(this.getDataDefine(ds1,conditions,parameters));

			// 构造返回的字符串
			String res = this.getAfterLargeData(data);
			outputReportForDebug(res, "data.xml");
//			Utils.debug("response to client");
			log.debug("response to client");
			pw.write(res);

		} catch (Throwable ex) {			
//			Utils.error("Error in getAfterLargeData: "+ex.getMessage(), ex);
			log.error("Error in getAfterLargeData: "+ex.getMessage(), ex);
			String err = ex.getMessage();
	    	if(err!=null)
	    		err = new String(err.getBytes(),"ISO-8859-1");
			response.sendError(500, "Error in getAfterLargeData: "+err);
			return;
			//response.sendError(500, "Error in getAfterLargeData: "+ex.getMessage());
			//throw new ServletException(ex);
		} 
		pw.flush();
		pw.close();

	}
	
	/**
	 * 组织第2+次的返回数据
	 * @param data
	 * @return
	 * @throws Throwable
	 */
	private String getAfterLargeData(LargeData data) throws Throwable{
		String reportData = Utils.encodeBase64ForCheckSQL(data
				.processToString(handlerChain, "UTF-8")); // 数据
		String finished = new Boolean(data.isFinished()).toString();// 完成标识
		String dsId = data.getDataDefine().getId(); // dsId
		
//		Utils.debug("build response content");
		log.debug("build response content");
		String res = new StringBuffer("data=" + reportData).append(
				"&finish=" + finished).append("&dsid=" + dsId).append("&")
				.toString();
		return res;
	}
	
	/**
	 * 由dataDefine的xml字符串获得DataDefine对象
	 * @param ds
	 * @param vpara
	 * @param cpara
	 * @return
	 * @throws Throwable
	 */
	private DataDefine getDataDefine(String ds,Map vpara,Map cpara) throws Throwable{
		StringReader sr = null;
		DataDefine dataDefine = null;
		try {
			sr = new StringReader(ds);
			Element root = new SAXReader().read(sr).getRootElement();
			dataDefine = new SQLDataDefine(root);
			String sql = ((SQLDataDefine) dataDefine).replaceVmark(vpara, cpara);
			((SQLDataDefine) dataDefine).setSql(sql);
		} catch (Throwable ex) {
//			Utils.error(ex.getMessage(), ex);
			log.error(ex.getMessage(),ex);
			throw ex;
		} finally {
			if (sr != null)
				sr.close();
		}
		return dataDefine;
	}
	
	private Map enStringToMap(String s,String type) throws Throwable{
		Map map = null;
		if (s != null && !s.equals("")) {
			map = this.stringToMap(this.decode(s), type);			
		}
		return map;
	}
	
	private String decode(String s) throws Throwable{
		s = s.replace(' ', '+');
		s = s.replace('`', '=');
		s = Utils.decodeBase64(s);
		return s;
	}

	private Map stringToMap(String s, String type) throws Throwable {
//		Utils.debug(type + ": " + s);
		log.debug(type + ": " + s);
		StringReader reader = null;
		Map map = null;
		try {
			reader = new StringReader(s);
			SAXReader saxReader = new SAXReader();
			saxReader.setEntityResolver(new EntityResolverImpl());
			List list = saxReader.read(reader).getRootElement().elements();
			map = new HashMap();
			// Utils.debug(type + ": ");
			for (int i = 0; i < list.size(); i++) {
				Element elem = (Element) list.get(i);
				if (type.equals("vpara")) {
					String name = elem.attributeValue("name");
					String value = elem.attributeValue("value");
					map.put(name, value);
//					Utils.debug("  " + name + ": " + value);
					log.debug("  " + name + ": " + value);
				} else if (type.equals("cpara")) {
					Parameter p = new Parameter(elem);
					map.put(p.getName(), p);
				}
			}
		} catch (DocumentException ex) {
			throw ex;
		} finally {
			reader.close();
		}
		return map;
	}

	private void outputReportForDebug(String data, String fileName) {
		try {
			String output = ConfigurationManager.getInstance().getValueByName(
					ConfigurationManager.getInstance().OUTPUT_REPORT_FOR_DEBUG);
			if (output != null && output.trim().equalsIgnoreCase("true")) {
//				Utils.debug("Output response data");
				log.debug("Output response data");
				FileWriter fw = new FileWriter(new File(fileName));
				StringReader sr = new StringReader(data);
				BufferedReader reader = new BufferedReader(sr);
				BufferedWriter writer = new BufferedWriter(fw);
				char[] c = new char[1024];
				int len;
				while ((len = reader.read(c)) > 0)
					writer.write(c, 0, len);

				reader.close();
				writer.flush();
				writer.close();
				fw.close();
				sr.close();
			}
		} catch (IOException e) {
//			Utils.error("OutputDataForDebugError", ": " + "report.xml");
			log.error("OutputDataForDebugError"+": " + "report.xml");
		}
	}

}
