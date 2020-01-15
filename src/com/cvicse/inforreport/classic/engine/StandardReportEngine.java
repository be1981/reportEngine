package com.cvicse.inforreport.classic.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.cvicse.inforreport.api.IReportEngine;
import com.cvicse.inforreport.dataset.DatasetProcessor;
import com.cvicse.inforreport.engine.ReportEngineFactory;
import com.cvicse.inforreport.engine.ReportManager;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.model.Dataset;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.ReportData;
import com.cvicse.inforreport.util.EngineUtils;

public class StandardReportEngine implements IReportEngine {

	private static final Log log = LogFactory.getLog(StandardReportEngine.class);

	private static boolean register; // 产品注册标志

	private String version = "6.1";
	
	private static StandardReportEngine instance = null;

	private static Integer lock = new Integer(1);
	
	public String getEngineType() {
		return "classic";
	}

	private StandardReportEngine() throws Exception{
		//register = Version.checkLicense();
		register = ReportEngineFactory.isRegister();
	}
	
	public static StandardReportEngine getInstance() throws ReportException {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					try {
						instance = new StandardReportEngine();
						log.debug("NewReportEngine");
					} catch (Throwable t) {
						String msg = t.getMessage();
						if (!(msg == null || msg.indexOf("cpr/cviccpr") == -1)) {
							log.error("VerifyCodeJarNotFound",t);
							throw new ReportException("VerifyCodeJarNotFound");

						} else {
							log.error(t.getMessage(), t);
							throw new ReportException(t.getMessage());
						}
					}
				}
			}
		}
		return instance;
	}

	public String getReport(String file, Map conditions) throws ReportException {		
		return getReport(new File(file), conditions,null);
	}
	
	public String getReport(File file, Map conditions) throws ReportException {	
		return getReport(file, conditions, null);
	}

    public String getReport(String file, Map conditions, Dataset dataset) throws ReportException {
		return getReport(new File(file), conditions, dataset);
	}
	
	public String getReport(File file, Map conditions, Dataset dataset) throws ReportException {
		InforReport report;
		try {
			if (!file.isAbsolute()) {
//				file = new File(ConfigurationManager.getInstance()
//						.getTemplateRepositoryPath(), file.toString());
				file = new File(ReportManager.getInstance()
						.getReportTemplatesHome(), file.toString());
				
			}
			report = new InforReport();
			report.init(file.toString());
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			throw new ReportException("");
		}
		return getReport(report, conditions, dataset);
	}	
	
	public String getReport(InforReport report, Map conditions)
			throws ReportException {
		 return getReport(report, conditions, null);
	}
	/**
	 * 获得返回查看器的XML格式字符串
	 * @param report 模板对象
	 * @param conditions 外部传入参数
	 * @param dataset 外部传入数据集
	 * @return XML格式字符串，由模板、数据和外部传入参数组成
	 * @throws ReportException
	 */
	public String getReport(InforReport report, Map conditions, Dataset dataset)
			throws ReportException {
		log.debug("StandardReportEngine getReport()");

		Map parameters = report.getParameters();
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("inforreport_transport_content");
		QName qName = new QName("noNamespaceSchemaLocation", new Namespace(
				"xsi", "http://www.w3.org/2001/XMLSchema-instance"));
		root.addAttribute(qName, "inforreport_transport_content.xsd");
		root.addElement("version").setText(version);

		// 加入模板节点
		log.debug("Add element template");
		root.addElement("report_template").add(
				report.getDocument().getRootElement());

		// 加入数据节点
		log.debug("Add element data");
		Element rootdata = root.addElement("report_data");
		Document data = null;
		Dataset ds = (dataset != null) ? dataset : report.getDataset();
		if (ds != null) {
			try {
				//data = ds.getAllData(conditions, parameters);
				DatasetProcessor dp = new DatasetProcessor();
				data = dp.getDomData(ds, conditions, parameters);
			} catch (Throwable ex) {
				log.error(ex.getMessage(), ex);
				throw new ReportException(ex.getMessage());
			}
			rootdata.add(data.getRootElement());
		}

		// 加入外部参数节点
		// Utils.debug("Add element conditions");
		log.debug("Add element conditions");
		Element rootpara = root.addElement("report_parameters");
		if (conditions != null) {
			for (Iterator it = conditions.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				String value = (String) conditions.get(key);
				rootpara.addElement("report_parameter").addAttribute("name",
						key).addAttribute("value", value);

			}
		}

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
		String encoding = report.getDocument().getRootElement()
				.attributeValue("encoding");
		if (encoding == null || encoding.equals(""))
			encoding = "GBK";
		OutputFormat format = new OutputFormat("", false, encoding);

		outputReportForDebug(document, format);

		StringWriter sw = new StringWriter();
		XMLWriter writer = new XMLWriter(sw, format);
		try {
			writer.write(document);
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw new ReportException(ex.getMessage());
		} finally {
			try {
				sw.close();
				writer.close();
			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
				throw new ReportException(ex.getMessage());
			}
		}
		String s = sw.toString();
		log.debug("encode report");
		s = EngineUtils.encodeBase64(s, encoding);
		log.debug("end getReport");
		return s;
	}

	private void outputReportForDebug(Document doc, OutputFormat format) {

		try {
			String output = ConfigurationManager.getInstance().getValueByName(
					ConfigurationManager.getInstance().OUTPUT_REPORT_FOR_DEBUG);
			if (output != null && output.trim().equalsIgnoreCase("true")) {
				log.debug("Output XML file");
				FileOutputStream fos = new FileOutputStream(new File(
						"report.xml"));
				XMLWriter writer = new XMLWriter(fos, format);
				writer.write(doc);
				if (fos != null)
					fos.close();
				writer.close();
			}
		} catch (IOException e) {
			log.error(EngineUtils.getResourceValue("OutputReportForDebugError")
					+ ": " + "report.xml");
		}

	}

	

	public ReportData getReportData(String file, Map conditions)
			throws ReportException {
		// ZK方式使用，本类无需实现
		return null;
	}

	public ReportData getReportData(File file, Map conditions) throws ReportException {
		// ZK方式使用，本类无需实现
		return null;
	}

	public ReportData getReportData(InforReport report, Map conditions)
			throws ReportException {
		// ZK方式使用，本类无需实现
		return null;
	}

}
