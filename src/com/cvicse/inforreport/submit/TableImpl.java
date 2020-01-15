/*
 * 创建日期 2005-9-27
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 * @author zhou_fbo
 * 
 *         TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class TableImpl {

	private static final Log log = LogFactory.getLog(TableImpl.class);
	private String tableName;
	private MetaDataImpl md;
	private Connection conn;
	private List rowElems;

	public TableImpl(Element table, Connection conn) throws SQLException {
		String tableName = table.valueOf("@name");
		this.setTableName(tableName);
		this.conn = conn;
		rowElems = table.elements("row");
		init(table.element("metadata"));
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	protected void init(Element mdElem) throws SQLException {
		List fields = mdElem.elements("field");
		String selectCmd = " SELECT ";
		for (int i = 0; i < fields.size(); i++) {
			Element field = (Element) fields.get(i);
			String fieldContent = field.getTextTrim();
			fieldContent += ", ";
			selectCmd += fieldContent;
		}

		int index = selectCmd.lastIndexOf(",");
		if (index > 0) {
			selectCmd = selectCmd.substring(0, index);
			selectCmd += " ";
		}

		selectCmd += " FROM ";
		selectCmd += this.getTableName() + " WHERE 1=2";

		Statement state = conn.createStatement();
		log.debug("sql: " + selectCmd);
		ResultSet rs = state.executeQuery(selectCmd);
		ResultSetMetaData rsmd = rs.getMetaData();

		md = new MetaDataImpl(mdElem, rsmd);
		rs.close();
		state.close();
	}

	public List doModify() throws SQLException {
		log.debug("- - there is " + rowElems.size()
				+ " row Element nested in table Element");
		List submitMsg = new ArrayList();
		String[] keys = null;
		for (int i = 0; i < rowElems.size(); i++) {

			log.debug("- - row Element [" + (i + 1) + "]:");
			Element rowElement = (Element) rowElems.get(i);
			RowImpl rl = new RowImpl(rowElement, md);

			keys = new String[4]; // 第一个是0、1或-1,第二个是a、m或d,第三个是关键字段信息,第四个是错误信息(只有第一个是-1时此项才有)
			int updateCount = -1;

			String operateType = rowElement.valueOf("@type");
			try {
				if (operateType == null || operateType.equals("")
						|| operateType.equals("AddWhenKeyMismatch")) {
					updateCount = updateOriginalRow(rl, keys);
					if (updateCount == 0)
						updateCount = insertNewRow(rl, keys);
				} else if (operateType.equals("modify")) {
					updateCount = updateOriginalRow(rl, keys);
				} else if (operateType.equals("delete")) {
					updateCount = deleteOriginalRow(rl, keys);
				} else if (operateType.equals("add")) {
					updateCount = insertNewRow(rl, keys);
				} else {
					log.error("operateTypeError: [" + operateType
							+ "]");
				}
				keys[0] = new Integer(updateCount).toString();
			} catch (Throwable ex) {
				keys[0] = "-1";
				keys[3] = ex.getMessage();
				log.fatal(ex.getMessage(), ex);
			}

			submitMsg.add(keys);
			rl.release();

		}
		return submitMsg;
	}

	/*
	 * public List doModify(String transactScope) throws SQLException { List
	 * list = null; if (transactScope.equals("single")) list = doSingleModify();
	 * else if (transactScope.equals("batch")) list = doBatchModify();
	 * 
	 * return list; } private List doBatchModify()throws SQLException{ List
	 * updates = new ArrayList(); for (int i = 0; i < rowElems.size(); i++) {
	 * Element rowElement = (Element) rowElems.get(i); RowImpl rl = new
	 * RowImpl(rowElement, md); String operateType =
	 * rowElement.valueOf("@type");
	 * if(operateType==null||operateType.equals("")||
	 * operateType.equals("AddWhenKeyMismatch")){ String cmd =
	 * buildUpdateClause()+buildWhereClause(); updates.add(rl); } } return null;
	 * }
	 * 
	 * 
	 * private String buildUpdateClause(){ List metaFields = md.getFields();
	 * String updateCmd = "UPDATE "+tableName+" SET "; for (int i = 0; i <
	 * metaFields.size(); i++) { MetaDataFieldImpl metaField =
	 * (MetaDataFieldImpl) metaFields.get(i); String fieldName =
	 * metaField.getFieldName(); if(!metaField.isKey()){ fieldName += "= ?, ";
	 * updateCmd += fieldName; } } int index = updateCmd.lastIndexOf(","); if
	 * (index > 0) { updateCmd = updateCmd.substring(0, index); updateCmd +=
	 * " "; } return updateCmd; }
	 * 
	 * private String buildWhereClause() { String whereClause = ""; List
	 * metaFields = md.getFields(); for (int i = 0; i < metaFields.size(); i++)
	 * { MetaDataFieldImpl metaField = (MetaDataFieldImpl) metaFields.get(i);
	 * String fieldName = metaField.getFieldName(); if(metaField.isKey()){
	 * fieldName += "= ?, "; whereClause += fieldName; } } //如果有关键字段
	 * if(!whereClause.equals("")){ whereClause=" WHERE "+whereClause;
	 * whereClause = whereClause.substring(0, whereClause.lastIndexOf(",")); }
	 * 
	 * return whereClause; }
	 */

	private int updateOriginalRow(RowImpl rowImpl, String[] keys)
			throws SQLException {
		keys[1] = "m";
		String updateCmd = null;
		int flag = -1;
		if (!rowImpl.isUpdsEmpty()) {
			updateCmd = "UPDATE ";
			updateCmd += tableName;
			// updateCmd += " ";
			updateCmd += rowImpl.buildUpdateClause();
			// updateCmd += " ";
			updateCmd += rowImpl.buildWhereClause();
			// updateCmd += " ";

			List updateRange = new ArrayList();
			updateRange.addAll(rowImpl.getUpdsList());
			updateRange.addAll(rowImpl.getKeysList());

			flag = this.executeCmd(updateCmd, updateRange, keys);
			updateRange.clear();
			updateRange = null;
		}
		return flag;
	}

	// ?方法未实现
	private int insertNewRow(RowImpl rowImpl, String[] keys)
			throws SQLException {
		// Utils.info("插入操作暂不支持");
		keys[1] = "a";
		String insertCmd = null;
		int flag = -1;
		insertCmd = "INSERT INTO ";
		insertCmd += tableName;
		insertCmd += rowImpl.buildInsertClause();

		List insertRange = new ArrayList();
		insertRange.addAll(rowImpl.getKeysList());
		insertRange.addAll(rowImpl.getUpdsList());
		flag = this.executeCmd(insertCmd, insertRange, keys);
		insertRange.clear();
		insertRange = null;

		return flag;

	}

	// ?方法未实现
	private int deleteOriginalRow(RowImpl rowImpl, String[] keys)
			throws SQLException {
		// Utils.info("删除操作暂不支持");
		keys[1] = "d";
		String deleteCmd = null;
		deleteCmd = "DELETE FROM ";
		deleteCmd += tableName;
		deleteCmd += rowImpl.buildWhereClause();

		int flag = this.executeCmd(deleteCmd, rowImpl.getKeysList(), keys);
		return flag;

	}

	// private int executeCmd(String cmd, RowImpl rowImpl) throws SQLException {
	private int executeCmd(String cmd, List cols, String[] keysMsg)
			throws SQLException {

		int flag = -1;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(cmd);
			log.debug("- - - sql: " + cmd);
			// List cols = rowImpl.getCols();
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < cols.size(); i++) {
				RowFieldImpl rfl = (RowFieldImpl) cols.get(i);
				String fieldValue = rfl.getFieldValue();
				// log.debug("fieldValue: " + fieldValue);
				int fieldType = rfl.getMetaDataFieldImpl().getFieldType();
				// log.debug("fieldType: " + fieldType);

				// 构造返回信息,格式为[<keyname1>=<keyvalue1>][<keyname2>=<keyvalue2>]...
				if (rfl.getMetaDataFieldImpl().isKey()) {
					String value = fieldValue;
					if (fieldType == -1 || fieldType == 1 || fieldType == 12)
						value = "'" + value + "'";
					buff.append("[" + rfl.getMetaDataFieldImpl().getFieldName()
							+ "=" + value + "]");
				}

				// ？类型匹配
				if (!(fieldType == -1 || fieldType == 1 || fieldType == 12)
						&& fieldValue.trim().equals(""))
					ps.setNull(i + 1, fieldType);
				else {
					switch (fieldType) {
					case -1:
						ps.setString(i + 1, fieldValue);
						break;
					case 1:
						ps.setString(i + 1, fieldValue);
						break;
					case 12:
						ps.setString(i + 1, fieldValue);
						break;
					case 4: // '\004'
						ps.setInt(i + 1, new Integer(fieldValue).intValue());
						break;

					case 16:
					case -7:
						ps.setBoolean(i + 1, new Boolean(fieldValue)
								.booleanValue());
						break;

					case -6: // tinyint
						ps.setByte(i + 1, new Byte(fieldValue).byteValue());
						break;

					case 5: // '\005'
						ps.setShort(i + 1, new Short(fieldValue).shortValue());
						break;

					case -5:
						ps.setLong(i + 1, new Long(fieldValue).longValue());
						break;

					case 6: // '\006'
					case 7: // '\007'
						ps.setFloat(i + 1, new Float(fieldValue).floatValue());
						break;

					case 8: // '\b'
						ps.setDouble(i + 1, new Double(fieldValue)
								.doubleValue());
						break;

					case 2: // '\002'
					case 3: // '\003'
						ps.setBigDecimal(i + 1, new java.math.BigDecimal(
								fieldValue));
						break;

					case 91: // '['
					// if(conn.getMetaData().getDatabaseProductName().toLowerCase().equals("oracle"))
					// ps.setTimestamp(i + 1, Timestamp.valueOf(fieldValue));
					// else
						ps.setDate(i + 1, java.sql.Date.valueOf(fieldValue));
						break;

					case 92: // '\\'
						ps.setTime(i + 1, Time.valueOf(fieldValue));
						break;

					case 93: // ']'
					// try {
					// java.util.Date d = new
					// java.text.SimpleDateFormat(fieldValue).
					// parse(
					// fieldValue);
					// fieldValue = new java.text.SimpleDateFormat(
					// "yyyy-MM-dd HH:mm:ss").format(d);
					// }
					// catch (Exception ex) {
					// Utils.error("Timestamp格式转换出错。",ex);
					// }
					// if(conn.getMetaData().getDatabaseProductName().toLowerCase().equals("oracle"))
					// ps.setDate(i + 1, java.sql.Date.valueOf(fieldValue));
					// else

						// 2010-11-17:如果提交yyyy-m(m)-d(d)格式也可以提交成功，不需必须是yyyy-MM-dd
						// HH:mm:ss
						boolean boo = fieldValue
								.matches("[1-9][0-9]{3}\\-([1-9]|0[1-9]|1[0-2])\\-([1-9]|0[1-9]|1[0-9]|2[0-9]|3[0-1])");
						if (boo) {
							fieldValue = fieldValue + " 00:00:00";
						}
						ps.setTimestamp(i + 1, Timestamp.valueOf(fieldValue));
						break;

					default:
						break;
					}
				}
			}
			keysMsg[2] = buff.toString();
			flag = ps.executeUpdate();
			log.debug("- - - update count: " + flag);

			// ？事务提交
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				log.fatal("RollBackError: " + ex.getMessage() + ".", ex);
				throw new SQLException("RollBackError: " + ex.getMessage() + ".");
			}
			// finally{
			// conn.close();
			// }
			log.fatal(e.getMessage(), e);
			throw e;
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				log.fatal("PreparedStatementCloseFailed", ex);
				throw new SQLException("PreparedStatementCloseFailed");
			}
		}
		return flag;

	}

}