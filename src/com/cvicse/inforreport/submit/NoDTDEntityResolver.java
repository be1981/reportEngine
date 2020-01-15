/*
 * 创建日期 2005-9-27
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

import java.io.StringBufferInputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author zhou_fbo
 *
 * TODO 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class NoDTDEntityResolver implements EntityResolver {
	  
	  /** ××××
	   * @param publicId_ ××××
	   * @param systemId_ ××××
	   * @return InputSource ××××
	   */
	  public InputSource resolveEntity(String publicId, String systemId) {
	    return new InputSource(new StringBufferInputStream(""));
	  }
	}