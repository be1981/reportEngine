package com.cvicse.inforreport.classic.engine;

import com.cvicse.inforreport.api.IReportEngine;
import com.cvicse.inforreport.exceptions.ReportException;

public class ReportService {

	/**
	 * 取得ReportEngine对象
	 * @return ReportEngine对象
	 * @throws com.cvicse.inforreport.exception.ReportException
	 */
	public static IReportEngine getReportEngine() throws ReportException {
		
		IReportEngine engine = null;
		String engineType = null;
		engineType = ConfigurationManager.getInstance().getEngineType();
		
		if (engineType.trim().equalsIgnoreCase("local")){
		      engine = StandardReportEngine.getInstance();		      
		}else{
			engine = RemoteReportEngine.getInstance();
		}
		
		return engine;

	}

}
