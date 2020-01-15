/*
 * 创建日期 2005-9-27
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;


/**
 * @author zhou_fbo
 * 
 *         TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class UpdateImpl {
	private static final Log log = LogFactory.getLog(UpdateImpl.class);
	private String name;
	private String dataSourceName;
	private Connection conn;
	private List tableElements;
	private boolean autoCommit;

	public UpdateImpl(Element updateElement) throws Exception {
		this.init(updateElement);
	}

	protected void init(Element updateElement) throws Exception {
		String name = updateElement.valueOf("@name");
		String dataSourceName = updateElement.valueOf("@dbcon");
		this.setName(name);
		this.SetDataSourceName(dataSourceName);
		InitialContext ic = new InitialContext();
		DataSource ds = (DataSource) ic.lookup(this.getDataSourceName());
		conn = ds.getConnection();
		autoCommit = conn.getAutoCommit();
		if (autoCommit)
			conn.setAutoCommit(false);
		conn.setTransactionIsolation(conn.getTransactionIsolation());

		tableElements = updateElement.elements("table");
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataSourceName() {
		return this.dataSourceName;
	}

	public void SetDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public Map execute() throws Exception {
		log.debug("- there is " + tableElements.size()
				+ " Table Element nested in Updata Element");
		Map submitMsg = new HashMap();
		try {
			for (int i = 0; i < tableElements.size(); i++) {
				log.debug("- table Element [" + (i + 1) + "]:");
				Element tableElement = (Element) tableElements.get(i);
				TableImpl table = new TableImpl(tableElement, conn);
				List msg = table.doModify();
				submitMsg.put(table.getTableName(), msg);
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			this.release();
		}
		return submitMsg;
	}

	public void release() throws SQLException {
		// Utils.debug("updateImpl.release()");
		if (conn != null)
			// Utils.debug("updateImpl.release()如果连接不为空：");
			conn.setAutoCommit(autoCommit);
		conn.close();
	}

}