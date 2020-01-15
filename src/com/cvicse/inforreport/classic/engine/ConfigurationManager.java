package com.cvicse.inforreport.classic.engine;

import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.util.EngineUtils;
import com.cvicse.inforreport.util.EntityResolverImpl;
import com.cvicse.inforreport.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置文件inforreport.properties管理类
 * @author qiao_lu1
 *
 */
public class ConfigurationManager {

	private static final Log log = LogFactory.getLog(ConfigurationManager.class);

	public String TEMPLATE_REPOSITORY_PATH = "TemplateRepositoryPath";

	public String ENGINE_TYPE = "EngineType"; //local,remote

	public String SERVICE_TYPE = "ServiceType"; //HTTP,Web Service

	public String SERVICE_URL = "ServiceURL";

	public String MAX_RECORD_NUMBER = "MaxRecordNumber";

	public String INITIAL_CONTEXT_FACTORY = "InitialContextFactory";

	public String PROVIDER_URL = "ProviderURL";

	public String OUTPUT_REPORT_FOR_DEBUG = "OutputReportForDebug";

	public String EXPORT_SERVICE_URL = "ExportServiceURL";

	public String DATABASE_CHARSET = "Database_Charset";

	public String FILE_ENCODING = "FileEncoding";

	public String SERVER = "Server";

	public String STEP = "Step";

	public String CODEBASE = "codebase";

	public String CLASSID="classid";

	private Properties props;

	private Map parameters = new HashMap();

	private String configFilePath;

	private String manageFilePath;

	private String handlerFilePath;

	private Document portalDom;

	private Document handlerDom;

	public static ConfigurationManager instance = new ConfigurationManager();

	private ConfigurationManager() {
		super();
	}

	public static ConfigurationManager getInstance() {
		//log.debug("---ConfigurationManager.getInstance()..."+instance.toString());
		return instance;
	}

	public void initProperties(String path)throws ReportException {
		try {
			File file = new File(path);
			if (!file.isAbsolute()) {
				file = file.getAbsoluteFile();
			}
			configFilePath = file.getCanonicalPath();
			log.debug(configFilePath);
			InputStream in = null;
			in = new FileInputStream(file);
			log.debug(file.getName()+":");
			initProperties(in);
		} catch (Throwable e) {
//			Utils.error(e.getMessage(), e);
			log.error(e.getMessage(),e);
			throw new ReportException(e.getMessage());
		}
	}

	public void initProperties(String configPath,String handlerPath)throws ReportException {
		try {
			File handlerFile = new File(handlerPath);
			if (!handlerFile.isAbsolute()) {
				handlerFile = handlerFile.getAbsoluteFile();
			}
			handlerFilePath = handlerFile.getCanonicalPath();

			File file = new File(configPath);
			if (!file.isAbsolute()) {
				file = file.getAbsoluteFile();
			}
			configFilePath = file.getCanonicalPath();

			log.debug(configFilePath);
			log.debug(handlerFilePath);
			InputStream in = null;
			in = new FileInputStream(file);
			log.debug(file.getName()+":");
			initProperties(in);
		} catch (Throwable e) {
//			Utils.error(e.getMessage(), e);
			log.error(e.getMessage(),e);
			throw new ReportException(e.getMessage());
		}
	}

	/**
	 * 使用路径的方式初始化配置文件
	 *
	 * @param files
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void initProperties(String[] files) throws ReportException{
		configFilePath = files[0];
		manageFilePath = files[1];
		handlerFilePath = files[2];

		try {
			File configFile = new File(configFilePath);
			File manageFile = new File(manageFilePath);
			File handlerFile = new File(handlerFilePath);
			if (!configFile.isAbsolute()) {
				configFile = configFile.getAbsoluteFile();
				configFilePath = configFile.toString();
			}
			if (!manageFile.isAbsolute()) {
				manageFile = manageFile.getAbsoluteFile();
				manageFilePath = manageFile.toString();
			}
			if (!handlerFile.isAbsolute()) {
				handlerFile = handlerFile.getAbsoluteFile();
				handlerFilePath = handlerFile.toString();
			}
			InputStream in = null;
			in = new FileInputStream(configFile);
			//		Utils.debug(configFile.getName() + ":");
			log.debug(configFile.getName() + ":");
			this.initProperties(in);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new ReportException(e.getMessage());
		}		

	}

	/**
	 * 使用流的方式初始化配置文件inforreport.properties
	 * @param in 使用getResourceAsStream获得的inforreport.properties的输入流
	 * @throws IOException
	 */
	public void initProperties(InputStream in) throws Exception {
		props = new Properties();
		try {
			props.load(in);
		} catch (IOException e) {
//			Utils.error("ManageFileNotFound", e);
			log.error(Utils.getResourceValue("ManageFileNotFound"),e);
			throw e;
		} finally {
			if (in != null)
				in.close();
		}

		String key = null;
		String value = null;
		Enumeration namesEnumeration = props.propertyNames();

		while (namesEnumeration.hasMoreElements()) {
			key = (String) namesEnumeration.nextElement();
			value = props.getProperty(key);

			parameters.put(key, value);
//			Utils.debug("    " + key + ": " + value);
			log.debug("    " + key + ": " + value);
		}

	}

	/**
	 * 使用流的方式初始化配置文件inforreport-handler.xml
	 * @param in 使用getResourceAsStream获得的inforreport-handler.xml的输入流
	 * @throws Exception
	 */
	public void initHandlerXml(InputStream in) throws Exception {
		handlerDom = this.stream2Dom(in);

	}
	/**
	 * 将xml输入流转化为Document对象
	 * @param in
	 * @return
	 * @throws Exception
	 */
	private Document stream2Dom(InputStream in) throws Exception{
		SAXReader saxReader = new SAXReader();
		saxReader.setEntityResolver(new EntityResolverImpl());
		Document dom = saxReader.read(in);
		return dom;
	}

	public void storeProps() throws Exception{
	    FileOutputStream fos = null;
	    try{
	      fos = new FileOutputStream(configFilePath);
	      props.store(fos, " defined for Report");
	    }catch(Exception e){
	      throw new ReportException(e.getMessage());
	    }
	    finally{
	      try{
	        if(fos!=null)
	          fos.close();
	      }
	      catch(IOException ex){
	        throw new ReportException(ex.getMessage());
	      }
	    }
	  }

	public String getValueByName(String key) {
		String value = (String) parameters.get(key);
		try {
			if (value != null && !value.trim().equals("")) {
				//value = new String(value.getBytes("ISO-8859-1"), "GBK");
				value = new String(value.getBytes("ISO-8859-1"));
			}
		} catch (UnsupportedEncodingException e) {
//			Utils.error(e.getMessage(), e);
			log.error(e.getMessage(),e);
			//throw new ReportException(e.getMessage());
		}
		return value;
	}

	public String getTemplateRepositoryPath()  throws ReportException{
		String value =null;
		try {
			value = this.getValueByName(this.TEMPLATE_REPOSITORY_PATH);
			if(value==null || "".equals(value.trim())){
				log.error(Utils.getResourceValue("NullTempatePath"));
				throw new ReportException(Utils.getResourceValue("NullTempatePath")
						+ ": " + this.TEMPLATE_REPOSITORY_PATH + ".");
			}

			File templatePath = new File(value);
			if (!templatePath.isAbsolute()) {
				// value = new File(new
				// File(configFilePath).getParent(),value).getCanonicalPath();
				if (configFilePath == null) // weblogic+war
					value = new File(new File("."), value).getCanonicalPath();
				else
					value = new File(new File(configFilePath).getParent(),
							value).getCanonicalPath();
			}

		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new ReportException(e.getMessage());
		}
//		Utils.debug("TemplateRepositoryPath :[" + value + "]");
		log.debug("TemplateRepositoryPath :[" + value + "]");
		return value;
	}

	public String getEngineType(){
		String value = this.getValueByName(this.ENGINE_TYPE);
		if ((value == null) || value.trim().equals("")) {
			return "local";
		}
		return value;
	}

	public String getServiceURL() throws ReportException{
		String value = this.getValueByName(this.SERVICE_URL);
		if(value==null || "".equals(value.trim())){
			log.error(Utils.getResourceValue("NullServiceURL1"+": " + this.SERVICE_URL));
			throw new ReportException(Utils.getResourceValue("NullServiceURL1")
					+ ": "+ this.SERVICE_URL + ", "
					+ Utils.getResourceValue("NullServiceURL2"));
		}
		log.debug(this.SERVICE_URL + ": [" + value + "]");

		return value;
	}

	public String getServiceType() {
		String value = this.getValueByName(this.SERVICE_TYPE);
		if ((value == null) || value.trim().equals("")) {
			value = "HTTP";
		}
//		Utils.debug(this.SERVICE_TYPE + ": [" + value + "]");
		log.debug(this.SERVICE_TYPE + ": [" + value + "]");
		return value;
	}

	public int getMaxRecordNumber() {
		String number = this.getValueByName(this.MAX_RECORD_NUMBER);
		if ((number == null) || number.trim().equals("")) {
			return 5000;
		} else {
			return Integer.parseInt(number);
		}
	}

	public String getNamingInitialContextFactory(){
		String factory = null;
		factory = this.getValueByName(this.INITIAL_CONTEXT_FACTORY);
		return factory;
	}

	public String getNamingProviderURL() {
		String url = null;
		url = this.getValueByName(this.PROVIDER_URL);
		return url;
	}

	public String getExportServiceURL(){
		String url = null;
		url = this.getValueByName(this.EXPORT_SERVICE_URL);
		return url;
	}

	public String getDatabaseCharset(){
		String charset = null;
		charset = this.getValueByName(this.DATABASE_CHARSET);
		if (charset == null || charset.equals("")) {
			// charset="GBK";
			return null;
		}
		return charset.trim().toUpperCase();
	}

	public String getFileEncoding(){
		String encoding = null;
		encoding = this.getValueByName(this.FILE_ENCODING);
		String sysEncoding = System.getProperty("file.encoding");
		EngineUtils.debug("File Encoding: "+sysEncoding);
		if (encoding == null || encoding.equals(""))
			encoding = "GBK";

//		if (sysEncoding.toLowerCase().equals("gb2312"))
//			encoding = "GBK";

		return encoding;
	}

	public String getCodeBase(){
		String base = this.getValueByName(this.CODEBASE);
		base = base.substring(0,base.indexOf("#version="));
		return base;
	}

	public String getViewerVersion(){
		String value = null;
		value = this.getValueByName(this.CODEBASE);
		String version = value.substring(value.indexOf("=") + 1);
		return version.trim();
	}
	public String getClassID() {
		String classid = null;
		classid = this.getValueByName(this.CLASSID);
		return classid;
	}
	/**
	 * @return Returns the Server.
	 */
	public String getServer() {
		String server =null;
		server = this.getValueByName(this.SERVER);
		return server;
	}

	/**
	 * @return Returns the Step.
	 */
	public String getStep() {
		String step =null;
		step = this.getValueByName(this.STEP);
		return step;
	}

	public String getManageFilePath() {
		return manageFilePath;
	}

	/**
	 * @return Returns the configPath.
	 */
	public String getConfigFilePath() {
		return configFilePath;
	}

	/**
	 * @return Returns the handlerFilePath.
	 */
	public String getHandlerFilePath() {
		return handlerFilePath;
	}

	/**
	 * @return Returns the handlerDom.
	 */
	public Document getHandlerDom() {
		return handlerDom;
	}

	/**
	 * @return Returns the portalDom.
	 */
	public Document getPortalDom() {
		return portalDom;
	}

	public void setValue(String key, String value) {
        parameters.put(key, value);
        if(value==null)
        	props.remove(key);
        else
        	props.setProperty(key, value);
    }

    public void setTemplateRepositoryPath(String path) {
        setValue(TEMPLATE_REPOSITORY_PATH, path);
    }

    public void setExportServiceURL(String exportPdfUrl) {
        setValue(EXPORT_SERVICE_URL, exportPdfUrl);
    }

    public void setCodeBase(String codebase){
    	String version = this.getViewerVersion();
    	if(version==null)
    		version = "4,2,0,0";
    	String base = codebase+"#version="+version;
    	setValue(CODEBASE,base);
    }

    public void setViewerVersion(String version){
    	String base = this.getCodeBase();
    	if(base ==null)
    		base = "InforReportViewer.CAB";
    	else if(base.indexOf("#version=")>0)
    		base = base.substring(0,base.indexOf("#version="));

    	base+="#version="+version;
        setValue(CODEBASE, base);
    }

    public void setClassID(String classId){
        setValue(CLASSID, classId);
    }

    public void setDatabaseCharset(String charset) {
        setValue(DATABASE_CHARSET, charset);
    }

    public void setNamingInitialContextFactory(String initContext) {
        setValue(INITIAL_CONTEXT_FACTORY, initContext);
    }

    public void setNamingProviderURL(String providerUrl) {
        setValue(PROVIDER_URL, providerUrl);
    }

    public void setServer(String server){
        setValue(SERVER, server);
    }

    public void setStep(String step){
        setValue(STEP, step);
    }

    public void setConfigFilePath(String configFilePath){
        this.configFilePath = configFilePath;
    }

	public void setHandlerFilePath(String handlerFilePath) {
		this.handlerFilePath = handlerFilePath;
	}


}