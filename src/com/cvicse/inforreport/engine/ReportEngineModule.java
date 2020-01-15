package com.cvicse.inforreport.engine;

import com.cvicse.inforreport.api.IReportModule;

/**
 * @Target:
 * @Author: Administrator
 * @version: 1.0
 * Date: 2009-3-18
 * Time: 11:32:50
 */
public class ReportEngineModule implements IReportModule {
      public String getVersion() {
        return  ReportEngineResource.getResourcesName("mVersion") +" Build:"+  ReportEngineResource.getResourcesName("mBuild");
    }

    public String getName() {
        return ReportEngineResource.getResourcesName("mName");

    }

    public String getID() {
        return ReportEngineResource.getResourcesName("mID");
    }

    public String getBuildDate() {
        return ReportEngineResource.getResourcesName("mBuildDate");
    }

    public String getDependencies() {
        return ReportEngineResource.getResourcesName("mDependencie");
    }

    public String getDescription() {
        return ReportEngineResource.getResourcesName("mDescription");
    }
}
