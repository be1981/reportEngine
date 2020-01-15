/*
 * 创建日期 2005-9-28
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

/**
 * @author zhou_fbo
 *
 * TODO 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class MetaDataFieldImpl {
	String fieldName;
	String isKey;
	int fieldType;

	public MetaDataFieldImpl(String fieldName, String isKey, int fieldType) {
		this.fieldName = fieldName;
		this.isKey = isKey;
		this.fieldType = fieldType;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public boolean isKey() {
		return (isKey!=null&&isKey.equals("true"));
	}

	public int getFieldType() {
		return this.fieldType;
	}

}
