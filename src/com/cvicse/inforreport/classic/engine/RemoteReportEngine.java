package com.cvicse.inforreport.classic.engine;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.api.IReportEngine;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.model.InforReport;
import com.cvicse.inforreport.model.ReportData;
import com.cvicse.inforreport.util.EngineUtils;

public class RemoteReportEngine implements IReportEngine {

	private static final Log log = LogFactory.getLog(RemoteReportEngine.class);

	/**
	 * 报表服务引擎URL
	 */
	private URL serviceURL;

	/**
	 * 访问报表服务使用的用户名
	 */
	private String userName;

	/**
	 * 访问报表服务使用的密码
	 */
	private String passWord;

	/**
	 * 报表服务引擎实例
	 */
	private static IReportEngine instance;
	
	public synchronized static IReportEngine getInstance()
			throws ReportException {
		IReportEngine newInstance = null;
		if (instance == null) {
			newInstance = new RemoteReportEngine();
			instance = newInstance;
			log.debug(EngineUtils.getResourceValue("NewReportEngine"));
		} else {
			newInstance = instance;
		}
		return newInstance;
	}

	private RemoteReportEngine() throws ReportException {
		try {
			serviceURL = new URL(ConfigurationManager.getInstance()
					.getServiceURL());
			userName = ConfigurationManager.getInstance().getValueByName(
					"UserName");
			if (userName == null || "".equals(userName.trim())) {
				throw new ReportException("NullUserName");
			}

			passWord = ConfigurationManager.getInstance().getValueByName(
					"PassWord");
			if (passWord == null || "".equals(passWord.trim())) {
				throw new ReportException("NullPassWord");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ReportException(e.getMessage());
		}
	}

	public String getReport(String file, Map conditions) throws ReportException {
		if (file == null)
			throw new ReportException("null templatename");

		String report = "";

		HttpClient client = new HttpClient();
		client.getState().setCredentials(null, serviceURL.getHost(),
				new UsernamePasswordCredentials(userName, passWord));

		PostMethod post = new PostMethod(serviceURL.toString());
		log.debug("Request CharSet: " + post.getRequestCharSet());
		int parameterNumber = 0;
		if (conditions != null) {
			parameterNumber = conditions.size();
		}

		post.addParameter("action", "getReport1");
		log.debug("Report Action: getReport1");

		try {
			post.addParameter("templateName", new String(file.getBytes(),
					"ISO-8859-1"));
		} catch (UnsupportedEncodingException ex) {
			log.error("UnsupportedEncoding: GBK.",ex);
			throw new ReportException("UnsupportedEncoding: GBK.");
		} catch (IllegalArgumentException ex) {
			log.error(ex.getMessage(), ex);
			throw new ReportException("IllegalArgumentException.");
		}
		log.debug("Template Name: " + file);

		if (parameterNumber != 0) {
			Iterator keyIterator = conditions.keySet().iterator();
			String key = null;
			String value = null;
			try {
				for (int j = 2; keyIterator.hasNext(); j++) {
					key = (String) keyIterator.next();
					value = (String) conditions.get(key);
					log.debug(key + ": " + value);
					post.addParameter(key, new String(value.getBytes(),
							"ISO-8859-1"));

				}
			} catch (UnsupportedEncodingException ex) {
				log.error("UnsupportedEncoding: GBK.", ex);
				throw new ReportException("UnsupportedEncoding: GBK.");
			}
		}

		try {
			try {
				client.executeMethod(post);
				log.debug("Post Request Body: " + post.getQueryString());
			} catch (IOException ex) {
				String err = ex.getMessage();
				if (err.indexOf("Software caused connection abort") != -1)
					err = EngineUtils.getResourceValue("ConnectEngineError1");
				else
					err = EngineUtils.getResourceValue("ConnectEngineError");
				log.error(err + ": " + ex.getMessage(), ex);
				throw new ReportException(err + ": " + ex.getMessage());
			}

			int statusCode = post.getStatusCode();
			if (statusCode == 200) {
				try {
					report = new String(post.getResponseBody());
				} catch (Exception e) {
					log.error("IOException", e);
					throw new ReportException("IOException: " + e.getMessage());
				}

			} else if (statusCode == 500) {
				String error = null;
				error = post.getStatusText();
				if (!error.equals("Internal Server Error")) {
					error = EngineUtils.decodeBase64(error);
				}
				log.error(EngineUtils.getResourceValue("ConnectEngine500") + ": "
						+ error);
				throw new ReportException(EngineUtils
						.getResourceValue("ConnectEngine500")
						+ ": " + error);
			} else if (statusCode == 404) {
				log.error(EngineUtils.getResourceValue("ConnectEngine404"));
				throw new ReportException(EngineUtils
						.getResourceValue("ConnectEngine404"));
			} else if (statusCode == 401) {
				log.error(EngineUtils.getResourceValue("ConnectEngine401"));
				throw new ReportException(EngineUtils
						.getResourceValue("ConnectEngine401"));
			} else {
				String error = post.getStatusText();
				log.error(EngineUtils.getResourceValue("ConnectEngineOther") + ": "
						+ statusCode + ": " + error);
				throw new ReportException(EngineUtils
						.getResourceValue("ConnectEngineOther")
						+ ": " + statusCode + ": " + error);

			}
		} finally {
			post.releaseConnection();
			log.debug("released connection to report service");
		}
		return report;
	}

	public String getReport(File file, Map conditions) throws ReportException {
		return getReport(file.toString(), conditions);
	}

	public String getEngineType() {
		return "classic";
	}

	public ReportData getReportData(String file, Map conditions)
			throws ReportException {
		// TODO Auto-generated method stub
		return null;
	}

	public ReportData getReportData(File file, Map conditions)
			throws ReportException {
		// TODO Auto-generated method stub
		return null;
	}

	public ReportData getReportData(InforReport report, Map conditions)
			throws ReportException {
		// TODO Auto-generated method stub
		return null;
	}

}
