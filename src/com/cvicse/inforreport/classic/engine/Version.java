package com.cvicse.inforreport.classic.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.cviccpr.exception.LicenseCommonException;
import com.cvicse.cviccpr.exception.LicenseExtendsException;
import com.cvicse.cviccpr.license.LicenseManager;
import com.cvicse.cviccpr.license.LicenseQO;
import com.cvicse.cviccpr.license.SignatureManager;
import com.cvicse.cviccpr.util.LicenseConstant;
import com.cvicse.inforreport.util.EngineUtils;

public class Version {
	
	private static final Log log = LogFactory.getLog(Version.class);
	
	private static String label = "Report";
	
	private static String expiration;
	
	private static Properties props;

	static {
		props = new Properties();

		try {
			InputStream in = Version.class.getResourceAsStream("version.properties");
			props.load(in);
			in.close();
		} catch (IOException e) {
			throw new Error("Missing version.properties", e);
		}	
	}
	
	public static boolean checkLicense() throws Exception{
		LicenseQO licenseQo=new LicenseQO(label,"V6");
		String formal = null;
		try {
			SignatureManager.verify(licenseQo);
			formal = LicenseManager.getInstance(licenseQo).getFeature(LicenseConstant.XML_ELEM_ATTR_FORMAL);
			expiration = LicenseManager.getInstance(licenseQo).getFeature(LicenseConstant.XML_ELEM_ATTR_EXPIRATION);
		} catch (LicenseCommonException e) {
			log.error("Copyright error. Error code: "+ e.getCode(),e);
			throw e;
		}catch(LicenseExtendsException e){
			log.error("Copyright error. Error code: "+ e.getCode(),e);
			throw e;
		}
		return Boolean.parseBoolean(formal);
	}
	/**
	 * ��ʽ��/���ð�
	 * @return
	 */
	public static String getVersionType() {
		//return "0".equals(versionType) ? "1":versionType ;
		String versionType= EngineUtils.getResourceValue("Trial");
		try {
			boolean active = checkLicense();
			if(active==true)
				versionType = EngineUtils.getResourceValue("Official");
			
		} catch (Exception ex) {
			log.error(ex.getMessage());
			versionType = ex.getMessage();
		}
		return versionType;
	}
	
	private static String getProperty(final String name) {
		return props.getProperty(name);
	}
	
	public static String getVersion() {
		return getProperty("version");
		
	}

	public static String getLabel() {
		return label;
	}	
	
	public static String getExpiration() {
		return expiration;
	}	
	
	public static String getBuildNo() {
		return getProperty("build.date") + "." + getProperty("build.count");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("");
	    System.out.println(getLabel()+getVersion()+" "+getVersionType());
	    System.out.println("");

	}

}
