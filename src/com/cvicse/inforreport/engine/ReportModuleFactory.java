package com.cvicse.inforreport.engine;

import com.cvicse.inforreport.api.IReportExporter;
import com.cvicse.inforreport.api.IReportModule;

import org.openide.util.Lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Target:
 * @Author: Administrator
 * @version: 1.0
 * Date: 2009-3-18
 * Time: 11:36:55
 */
public class ReportModuleFactory {
    private static Map moduleProvideMap = new HashMap();
    private static boolean isInit = false;

    /**
     * 根据目前的Class加载，初始化所有的导出程序
     */
    private static void initProvides() {

        Lookup.Result result = Lookup.getDefault().lookupResult(IReportModule.class);
        Collection<? extends IReportModule> instances = result.allInstances();
        for (IReportModule provider : instances) {
            //
            moduleProvideMap.put(provider.getID(), provider);
        }
        isInit = true;
    }

    /**
     * 返回模块信息Map
     *
     * @return
     */
    public static Map getReportModuleMap() {
        if (!isInit) initProvides();
        return moduleProvideMap;
    }

}
