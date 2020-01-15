/*
 * 创建日期 2005-9-27
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.cvicse.inforreport.util.ReportModelResource;

/**
 * @author zhou_fbo
 * 
 *         TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class MetaDataImpl {
	private static final Log log = LogFactory.getLog(MetaDataImpl.class);

	private List fields = new ArrayList();

	public MetaDataImpl(Element metaDataElem, ResultSetMetaData rsmd)
			throws SQLException {
		List fieldElements = metaDataElem.elements("field");
		log.debug("- - there is " + fieldElements.size()
				+ " field Element nested in metadata Element");
		for (int i = 0; i < fieldElements.size(); i++) {

			Element fieldElement = (Element) fieldElements.get(i);
			String fieldName = fieldElement.getTextTrim();
			String isKey = fieldElement.valueOf("@iskey");
			if (isKey == null) {
				isKey = "false";
			}
			if ((i + 1) > rsmd.getColumnCount()) {
				log.fatal("MDCountInvalid");
				throw new RuntimeException(ReportModelResource
						.getResourcesName("MDCountInvalid"));
			}
			int fieldType = rsmd.getColumnType(i + 1);

			MetaDataFieldImpl mdf = new MetaDataFieldImpl(fieldName, isKey,
					fieldType);
			fields.add(i, mdf);
		}
	}

	public String getFieldName(int index) {
		if (index < 1) {
			log.error("FieldIndexOutofBound: [" + index + "<1]");
			throw new RuntimeException(ReportModelResource
					.getResourcesName("FieldIndexOutofBound")
					+ ": [" + index + "<1]");
		}
		if (index > fields.size()) {
			log.error("FieldIndexOutofBound: [" + index + ">"
					+ fields.size() + "]");
			throw new RuntimeException(ReportModelResource
					.getResourcesName("FieldIndexOutofBound")
					+ ": [" + index + ">" + fields.size() + "]");
		}

		MetaDataFieldImpl mdf = (MetaDataFieldImpl) fields.get(index - 1);
		return mdf.getFieldName();
	}

	public int getFieldType(int index) {
		if (index < 1) {
			log.error("FieldIndexOutofBound: [" + index + "<1]");
			throw new RuntimeException(ReportModelResource
					.getResourcesName("FieldIndexOutofBound")
					+ ": [" + index + "<1]");
		}
		if (index > fields.size()) {
			log.error("FieldIndexOutofBound: [" + index + ">"
					+ fields.size() + "]");
			throw new RuntimeException(ReportModelResource
					.getResourcesName("FieldIndexOutofBound")
					+ ": [" + index + ">" + fields.size() + "]");
		}

		MetaDataFieldImpl mdf = (MetaDataFieldImpl) fields.get(index - 1);
		return mdf.getFieldType();

	}

	public boolean isKey(int index) {
		if (index < 1) {
			log.error("FieldIndexOutofBound: [" + index + "<1]");
			throw new RuntimeException(ReportModelResource
					.getResourcesName("FieldIndexOutofBound")
					+ ": [" + index + "<1]");
		}
		if (index > fields.size()) {
			log.error("FieldIndexOutofBound: [" + index + ">"
					+ fields.size() + "]");
			throw new RuntimeException(ReportModelResource
					.getResourcesName("FieldIndexOutofBound")
					+ ": [" + index + ">" + fields.size() + "]");
		}
		MetaDataFieldImpl mdf = (MetaDataFieldImpl) fields.get(index - 1);
		return mdf.isKey();
	}

	public List getFields() {
		return fields;
	}

	public void setFields(List fields) {
		this.fields = fields;
	}

}