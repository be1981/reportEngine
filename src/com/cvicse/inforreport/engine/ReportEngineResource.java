/*

Copyright (C) 2008, CVICSE Inc. (http://www.inforbus.com)

*/
package com.cvicse.inforreport.engine;

import com.cvicse.inforreport.util.ResourceUtil;

import java.util.ResourceBundle;


public class ReportEngineResource {



    public static final String getResourcesName(String key) {
        try {
            ResourceBundle bundle = ResourceUtil.findBundle(ReportEngineResource.class, "report_engine", key);
            if (null != bundle) {
                return bundle.getString(key);
            }
        } catch (Exception exE) {
        }
        return key;

    }

    public static void main(String[] args) {
        System.out.println(ReportEngineResource.getResourcesName("mVersion") + " Build: " + ReportEngineResource.getResourcesName("mBuild"));
        System.out.println(ReportEngineResource.getResourcesName("mBuildDate"));
        System.out.println(ReportEngineResource.getResourcesName("mName"));

    }
}