package com.cvicse.inforreport;

import com.cvicse.inforreport.api.IReportManager;
import com.cvicse.inforreport.engine.ReportEngineFactory;
import com.cvicse.inforreport.engine.ReportEngineResource;
import com.cvicse.inforreport.engine.ReportManager;
import com.cvicse.inforreport.engine.ReportModuleFactory;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.exporter.ReportExporterFactory;

import java.util.logging.Logger;

/**
 * User: Administrator
 * Date: 2009-3-30
 * Time: 3:05:38
 */
public class ReportServer {
    private Logger log = Logger.getLogger("ReportServer.class");

    IReportManager reportManager;
    ReportEngineFactory engineFactory;
    ReportExporterFactory exporterFactory;
    ReportModuleFactory moduleFactory;
    /**
     * 报表服务引擎实例
     */
    private static ReportServer instance;

    public synchronized static ReportServer getInstance()
            throws ReportException {
        ReportServer newInstance = null;
        if (instance == null) {
            newInstance = new ReportServer();
            newInstance.init();
            instance = newInstance;

        } else {
            newInstance = instance;
        }
        return newInstance;
    }

    /**
     * 初始化引擎西信息
     */
    public void init() {
        log.info(ReportEngineResource.getResourcesName("ReportServerInit"));
        reportManager = ReportManager.getInstance();
        engineFactory = new ReportEngineFactory();
        exporterFactory = new ReportExporterFactory();
        moduleFactory = new ReportModuleFactory();
    }

    /**
     * 报表管理
     * @return
     */
    public IReportManager getReportManager() {
        return reportManager;
    }

    /**
     * 获取报表引擎工厂类
     * @return
     */

    public ReportEngineFactory getEngineFactory() {
        return engineFactory;
    }

    /**
     * 获取报表导出工厂类
     * @return
     */
    public ReportExporterFactory getExporterFactory() {
        return exporterFactory;
    }

    /**
     * 获取报表模块工厂类
     * @return
     */
    public ReportModuleFactory getModuleFactory() {
        return moduleFactory;
    }
}
