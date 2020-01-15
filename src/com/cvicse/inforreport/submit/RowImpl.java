/*
 * 创建日期 2005-9-27
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 * @author zhou_fbo
 * 
 *         TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class RowImpl {
	private static final Log log = LogFactory.getLog(RowImpl.class);
	private List keys = new ArrayList();
	private List upds = new ArrayList();

	public RowImpl(Element row, MetaDataImpl md) {
		List cols = row.elements("col");
		log.debug("- - - there is " + cols.size()
				+ " col Element nested in row Element");
		for (int i = 0; i < cols.size(); i++) {
			Element colElement = (Element) cols.get(i);
			// String fieldValue = colElement.getTextTrim();
			String fieldValue = colElement.getText();
			String fieldName = md.getFieldName(i + 1);
			int fieldType = md.getFieldType(i + 1);

			boolean isKey = md.isKey(i + 1);
			RowFieldImpl rfl = new RowFieldImpl(fieldValue, fieldName,
					fieldType, new Boolean(isKey).toString());
			if (isKey) {
				keys.add(rfl);
			} else {
				upds.add(rfl);
			}
		}
	}

	protected String buildUpdateClause() {
		String updateCmd = " SET ";
		for (int i = 0; i < upds.size(); i++) {
			RowFieldImpl rfl = (RowFieldImpl) upds.get(i);
			String fieldName = rfl.getMetaDataFieldImpl().getFieldName();
			fieldName += "= ?, ";
			updateCmd += fieldName;
		}
		int index = updateCmd.lastIndexOf(",");
		if (index > 0) {
			updateCmd = updateCmd.substring(0, index);
			updateCmd += " ";
		}
		return updateCmd;

	}

	protected String buildInsertClause() {
		String insertCmd = "(";
		List insertRange = new ArrayList();
		insertRange.addAll(keys);
		insertRange.addAll(upds);
		// keys.addAll(upds);
		RowFieldImpl rfl = null;
		for (int i = 0; i < insertRange.size(); i++) {
			if (i > 0)
				insertCmd += ",";
			rfl = (RowFieldImpl) insertRange.get(i);
			String fieldName = rfl.getMetaDataFieldImpl().getFieldName();
			insertCmd += fieldName;
		}
		insertCmd += ") VALUES (";
		for (int i = 0; i < insertRange.size(); i++) {
			if (i > 0)
				insertCmd += ",";
			insertCmd += "?";
		}
		insertCmd += ")";
		// for (int k = 1; k <= keys.size(); k++) {
		// insertCmd += "?";
		// if (k < keys.size())
		// insertCmd += ", ";
		// else
		// insertCmd += ")";
		// }
		insertRange.clear();
		insertRange = null;
		return insertCmd;

	}

	protected String buildWhereClause() {
		String whereClause = "";
		if (keys.size() == 0)
			return whereClause;
		whereClause = " WHERE ";
		for (int i = 0; i < keys.size(); i++) {
			RowFieldImpl rfl = (RowFieldImpl) keys.get(i);
			String fieldName = rfl.getMetaDataFieldImpl().getFieldName();
			fieldName += "= ? and ";
			whereClause += fieldName;
		}
		int index = whereClause.lastIndexOf("and");
		if (index > 0) {
			whereClause = whereClause.substring(0, index);
			// whereClause += " ";
		}
		return whereClause;
	}

	// public List getCols() {
	// upds.addAll(keys);
	// return upds;
	// }
	public List getKeysList() {
		return keys;
	}

	public List getUpdsList() {
		return upds;
	}

	public boolean isUpdsEmpty() {
		return upds.isEmpty();
	}

	public void release() {
		if (null != keys)
			keys.clear();
		keys = null;
		if (null != upds)
			upds.clear();
		upds = null;
	}

}