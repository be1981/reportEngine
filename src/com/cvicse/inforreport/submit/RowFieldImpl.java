/*
 * 创建日期 2005-9-29
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
public class RowFieldImpl {
	String fieldValue;
//	String fieldName;
//	int fieldType;
//	boolean isKey;
	MetaDataFieldImpl metaDataFieldImpl;
	
	public RowFieldImpl(String fieldValue, MetaDataFieldImpl metaDataFieldImpl){
		this.fieldValue=fieldValue;
		this.metaDataFieldImpl= metaDataFieldImpl;
	}
	public RowFieldImpl(String fieldValue, String fieldName, int fieldType, String isKey) {
		this.fieldValue=fieldValue;
		this.metaDataFieldImpl=new MetaDataFieldImpl(fieldName, isKey, fieldType);
	}
	
//	public RowFieldImpl(String fieldValue, String fieldName, int fieldType) {
//		this.fieldValue = fieldValue;
//		this.fieldName = fieldName;
//		this.fieldType = fieldType;
//	}
//	
//	public String getFieldName() {
//		return this.fieldName;
//	}
	
	public String getFieldValue() {
		return this.fieldValue;
	}

	public MetaDataFieldImpl getMetaDataFieldImpl() {
		return metaDataFieldImpl;
	}




	
//	public int getFieldType() {
//		return this.fieldType;
//	}
}
