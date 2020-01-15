/*	
 * 
 * ISReport Copyright 2008-2009 CVICSE, Co.ltd . 
 * All rights reserved.
 *			 
 * Package:  com.cvicse.inforreport.engine
 * FileName: ReportEngineFactory.java
 * 
 */
package com.cvicse.inforreport.engine;

import com.cvicse.inforreport.api.IReportBusiModel;
import com.cvicse.inforreport.api.IReportDatasource;
import com.cvicse.inforreport.api.IReportEngine;
import com.cvicse.inforreport.business.ReportBusiModel;
import com.cvicse.inforreport.classic.engine.StandardReportEngine;
import com.cvicse.inforreport.classic.engine.Version;
import com.cvicse.inforreport.datasource.ReportDatasource;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.model.InforReport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openide.util.Lookup;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 创建人 li_zhi1
 * @version 1.0
 * @功能
 *          <p>
 *          报表引擎工厂
 *          </p>
 * @date 创建日期 2009-9-23
 */

public class ReportEngineFactory {
	
	private static final Log log = LogFactory.getLog(ReportEngineFactory.class);
	
	private static Map engineProviderMap = new HashMap();

	private static boolean isInit = false;
	
	private static boolean register; // 产品注册标志

	/**
	 * 根据目前的Class加载，初始化所有的报表引擎 针对4.2的StandardReportEngine类构造方式为私有做了修改
	 */

	private static void initProvides() throws ReportException{
		//判断版权
		try {
			register = Version.checkLicense();
		} catch (Exception e1) {
			log.error(e1);
			throw new ReportException(e1.getMessage());
		}

		Lookup.Result result = Lookup.getDefault().lookupResult(
				IReportEngine.class);
		Collection<? extends Lookup.Item> instances = result.allItems();// allInstances();

		for (Iterator<? extends Lookup.Item> iter = instances.iterator(); iter
				.hasNext();) {
			Lookup.Item item = iter.next();
			if (item.getType().equals(StandardReportEngine.class)) {
				try {
					IReportEngine reportEngine = StandardReportEngine
							.getInstance();
					engineProviderMap.put(reportEngine.getEngineType(),
							reportEngine);
				} catch (ReportException e) {
					log.error(e);
				}
				// engineProvideMap.put(StandardReportEngine.getInstance().getEngineType(),)
			} else {
				IReportEngine provider = (IReportEngine) item.getInstance();
				engineProviderMap.put(provider.getEngineType(), provider);
			}
		}

		isInit = true;
	}

	public static void initDatasource(String file) throws ReportException{
		if (!isInit)
			initProvides();
		ReportDatasource rds = ReportDatasource.getInstance();
		rds.init(file);
		engineProviderMap.put("datasource", rds);
	}

	public static void initBusinessModel(String file) throws ReportException{
		if (!isInit)
			initProvides();
		engineProviderMap.put("businessmodel", new ReportBusiModel(file));
	}

	/*
	 * public static void initDatasource(InputStream in){ if (!isInit)
	 * initProvides(); engineProviderMap.put("datasource", new
	 * ReportDatasource(in)); } public static void initBusinessModel(InputStream
	 * in){ if (!isInit) initProvides(); engineProviderMap.put("businessmodel",
	 * new ReportBusiModel(in)); }
	 */

	/**
	 * 取得某一类型的报表引擎。类型 ：groovy(groovy脚本),classic(原报表引擎),suite(新报表引擎),等等。
	 * 
	 * @param file
	 *            模板名
	 * @return
	 */
	public static IReportEngine getReportEngine(String file)
			throws ReportException {
		log.debug("");
		log.debug("ReportEngineFactory getReportEngine");
		if (!isInit)
			initProvides();
		IReportEngine reportEngine = null;
		if (file.endsWith(".groovy")) {
			reportEngine = (IReportEngine) engineProviderMap.get("groovy");
		} else if (file.endsWith(".ipr") || file.endsWith(".xml")) {
			try {
				//福建邮政局
				/*
				file = file.replace("file:" + File.separator, "");
				if(file.startsWith("/"))
					file = file.replaceFirst("/", "");
				*/
				File file1 = new File(file);
				if (!file1.isAbsolute()) {
					file1 = new File(ReportManager.getInstance()
							.getReportTemplatesHome(), file1.toString());

				}
				InforReport report = new InforReport();
				// report.init(file1);
				String type = report.getReportType(file1);
				if ("fixed".equals(type) || "extend".equals(type)
				//		|| "cross".equals(type) //交叉报表表头中有写死内容时扩展报错 20141016
						|| "chart".equals(type)
						|| "composite".equals(type)
						|| "newmainsub".equals(type)) {
					reportEngine = (IReportEngine) engineProviderMap
							.get("suite");
					log.debug("New Suite Engine");
				} else {
					reportEngine = (IReportEngine) engineProviderMap
							.get("classic");
					log.debug("New Classic Engine");
				}
			} catch (Exception e) {
				log.error(e);
				throw new ReportException(e.getMessage());
			}

		}
		return reportEngine;
	}

	public static IReportDatasource getReportDatasource() {
		return (IReportDatasource) engineProviderMap.get("datasource");
	}

	public static IReportBusiModel getReportBusiModel() {
		return (IReportBusiModel) engineProviderMap.get("businessmodel");
	}

	public static boolean isRegister() {
		return register;
	}
}
