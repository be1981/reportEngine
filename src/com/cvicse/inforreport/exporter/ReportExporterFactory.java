package com.cvicse.inforreport.exporter;

import com.cvicse.inforreport.api.IReportExporter;
import org.openide.util.Lookup;

import java.util.*;

/**
 * @Target: 报表导出服务的工厂类，单例模式，负责创建各种类型的报表导出类
 * @Author: shuaiche
 * @version: 1.0
 * Date: 2009-2-20
 * Time: 11:15:25
 */
public class ReportExporterFactory {

    private static Map exporterProvideMap = new HashMap();
    private static List typeList = new ArrayList();
    private static boolean isInit = false;

    /**
     * 根据目前的Class加载，初始化所有的导出程序
     */
    private static void initProvides() {

        Lookup.Result result = Lookup.getDefault().lookupResult(IReportExporter.class);
        Collection<? extends IReportExporter> instances = result.allInstances();
        for (IReportExporter provider : instances) {
            //
            typeList.add(provider.getExportType());
            exporterProvideMap.put(provider.getExportType(), provider);
        }
        isInit = true;
    }

    /**
     * 取得某一类型的导出程序
     *
     * @param type 类型 ：CSV，EXCEL，PDF，WORD。。。。
     * @return
     */
    public static IReportExporter getReportExporter(String type) {
        if (!isInit) initProvides();
        return (IReportExporter) exporterProvideMap.get(type.toUpperCase());
    }

    /**
     * 取得当前的支持的导出类型
     *
     * @return List（String）
     */
    public static List getExportertype() {
        if (!isInit) initProvides();
        return typeList;
    }

    public static void main(String[] args) {
        for (int i = 0; i < ReportExporterFactory.getExportertype().size(); i++) {
            String s = (String) ReportExporterFactory.getExportertype().get(i);
        }
    }
}
