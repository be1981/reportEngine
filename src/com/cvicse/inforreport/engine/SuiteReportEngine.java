/*	
 * 
 * ISReport Copyright 2008-2009 CVICSE, Co.ltd . 
 * All rights reserved.
 *			 
 * Package:  com.cvicse.inforreport.engine
 * FileName: SuiteReportEngine.java
 * 
 */
package com.cvicse.inforreport.engine;

import java.io.File;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import com.cvicse.inforreport.api.IReportEngine;
//import com.cvicse.inforreport.classic.engine.ConfigurationManager;
import com.cvicse.inforreport.core.ExtendReportImpl;
import com.cvicse.inforreport.core.ExtendReportService;
import com.cvicse.inforreport.dataset.DatasetProcessor;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.ReportData;
import com.cvicse.inforreport.util.EngineUtils;

/**
 * 
 * @功能
 * <p>
 * SuiteReport引擎接口实现类
 * </p>
 * @author 创建人 li_zhi1
 * @date 创建日期 2009-9-24
 * @version 1.0
 * 
 */
public class SuiteReportEngine implements IReportEngine {

	private static final Log log = LogFactory.getLog(SuiteReportEngine.class);

	private static boolean register; // 产品注册标志

	private String version = "6.0";

	public String getEngineType() {
		return "suite";
	}
	
	public SuiteReportEngine() throws Exception{
		register = ReportEngineFactory.isRegister();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cvicse.inforreport.api.IReportEngine#getReport(java.lang.String,
	 *      java.util.Map)
	 */
	public String getReport(String file, Map conditions) throws ReportException {
		//福建邮政局
		/*
		file = file.replace("file:" + File.separator, "");
		if(file.startsWith("/")){
			File f = new File(file);
			if(!f.exists()){
				file = file.replaceFirst("/", "");
			}
		}*/
		
		return getReport(new File(file), conditions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cvicse.inforreport.api.IReportEngine#getReport(java.io.File,
	 *      java.util.Map)
	 */
	public String getReport(File file, Map conditions) throws ReportException {
		String reportXml = null;
		InforReport report = new InforReport();
		try {
			if (!file.isAbsolute()) {
				file = new File(ReportManager.getInstance().getReportTemplatesHome(), file.toString());

			}
			report.init(file.toString());			
			reportXml = getReport(report, conditions);
		} catch (Exception e) {
			log.error(e);
			//System.err.println(e);
			e.printStackTrace();
			return null;
		}		
		return reportXml;
	}

	/**
	 * 获得返回查看器的XML格式字符串
	 * 
	 * @param report
	 *            未处理扩展的模板对象
	 * @param conditions
	 *            外部传入参数
	 * @return XML格式字符串，由模板、结果网格和外部传入参数组成
	 * @throws ReportException
	 */
	public String getReport(InforReport report, Map conditions)
			throws ReportException {
		log.debug("SuiteReportEngine getReport");
		String reportXml = null;
		try {
			//Map datas = report.getDatas(conditions);
			DatasetProcessor dp = new DatasetProcessor();
			Map datas = dp.getAllData(report.getDataset(), conditions, report.getParameters());
			ExtendReportService res = new ExtendReportImpl();
			report=res.process(report, datas, conditions);

			ReportData wr = new ReportData(report);
			wr.setGrid(report);
			// EngineUtils.Doc2XmlFile(wr.getGridDocument(), "GBK", "C:/xx.xml");
	
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("inforreport_transport_content");
			QName qName = new QName("noNamespaceSchemaLocation", new Namespace(
					"xsi", "http://www.w3.org/2001/XMLSchema-instance"));
			root.addAttribute(qName, "inforreport_transport_content.xsd");
			root.addElement("version").setText(version);
	
			// 加入模板节点
			Element rootTemplate = root.addElement("report_template");
			rootTemplate.add(report.getDocument().getRootElement());
	
			// 加入grid节点
			root.add(wr.getGridDocument().getRootElement());
	
			// 加入外部参数节点
			Element rootpara = root.addElement("report_parameters");
			if (conditions != null) {
				log.debug("conditions:");
				for (Iterator it = conditions.keySet().iterator(); it.hasNext();) {
					String key = (String) it.next();
					String value = conditions.get(key).toString();
					log.debug("  "+key+": "+value);
					rootpara.addElement("report_parameter").addAttribute("name",
							key).addAttribute("value", value);
	
				}
			}
	
			// 获取服务器属性
			Element server_properties = root.addElement("server_properties");
			Element property = server_properties.addElement("property");
			property.addElement("name").addText("ServerTimezoneOffset");
			property.addElement("value")
					.addText(
							Integer.toString(new Date(1970, 1, 1)
									.getTimezoneOffset() * 60));
	
			if (register) {
				Element property1 = server_properties.addElement("property");
				property1.addElement("name").addText("IR_Register");
				property1.addElement("value").addText("true");
			}
	
			String encoding = report.getDocument().getRootElement().attributeValue(
					"encoding");
			if (encoding == null || encoding.equals(""))
				encoding = "GBK";
	
			reportXml = EngineUtils.convertToString(document, "", false, encoding);
			/*
			try {
				SAXReader saxReader = new SAXReader();
				Document dom = saxReader.read(new StringReader(reportXml));
				List<Node> ps = dom.getRootElement().selectNodes("report_parameters/report_parameter");
				log.debug("reportparas:");
				for(Node node:ps){
					log.debug("  "+node.valueOf("@name")+":"+node.valueOf("@value"));
				}
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			*/
			
			// EngineUtils.Doc2XmlFile(document, "GBK", "C:/xxx.xml");
			reportXml = EngineUtils.encodeBase64(reportXml,encoding);
	
			log.debug("End getReport");

		} catch (Exception e) {
			log.error(e);
			//System.err.println(e);
			e.printStackTrace();
			throw new ReportException(e.getMessage());
		}
		return reportXml;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cvicse.inforreport.api.IReportEngine#getReportData(java.lang.String,
	 *      java.util.Map)
	 */
	public ReportData getReportData(String file, Map conditions)
			throws ReportException {
		//福建邮政局
		/*
		file = file.replace("file:" + File.separator, "");
		if(file.startsWith("/"))
			file = file.replaceFirst("/", "");
		*/
		return getReportData(new File(file), conditions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cvicse.inforreport.api.IReportEngine#getReportData(java.io.File,
	 *      java.util.Map)
	 */
	public ReportData getReportData(File file, Map conditions) throws ReportException {
		InforReport report = new InforReport();
		try {
			if (!file.isAbsolute()) {
				file = new File(ReportManager.getInstance().getReportTemplatesHome(), file.toString());

			}
			report.init(file.toString());
			return getReportData(report, conditions);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cvicse.inforreport.api.IReportEngine#getReportData(com.cvicse.inforreport.model.InforReport,
	 *      java.util.Map)
	 */
	public ReportData getReportData(InforReport report, Map conditions)
			throws ReportException {
		log.debug("SuiteReportEngine getReportData");
		InforReport ifReport=null;
		try {
			DatasetProcessor dp = new DatasetProcessor();
			Map datas = dp.getAllData(report.getDataset(), conditions, report.getParameters());
			ExtendReportService res = new ExtendReportImpl();
			ifReport=res.process(report, datas, conditions);
		} catch (Exception e) {
			e.printStackTrace();
			//return null;
			throw new ReportException(e.getMessage());
		}
		ReportData wr = new ReportData(ifReport);
		if (report.getDesignFlag() != 2) {
			wr.setGrid(ifReport);
		}

		log.debug("End getReportData");
		return wr;
	}

}
