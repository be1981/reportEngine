/*

Copyright (C) 2008, CVICSE Inc. (http://www.inforbus.com)

*/
package com.cvicse.inforreport.util;

import java.util.ResourceBundle;


public class ReportModelResource {


    public static final String getResourcesName(String key) {
        try {
            ResourceBundle bundle = ResourceUtil.findBundle(ResourceUtil.class, "report_model", key);
            if (null != bundle) {
                return bundle.getString(key);
            }
        } catch (Exception exE) {
        }
        return key;

    }


}
