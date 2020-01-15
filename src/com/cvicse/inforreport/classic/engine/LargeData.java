package com.cvicse.inforreport.classic.engine;

import com.cvicse.inforreport.api.IRDataProcessor;
import com.cvicse.inforreport.classic.engine.dialect.*;
import com.cvicse.inforreport.dataset.BusinessData;
import com.cvicse.inforreport.dataset.DatasetProcessor;
import com.cvicse.inforreport.dataset.SQLDataProcessor;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.handler.HandlerChain;
import com.cvicse.inforreport.model.DataDefine;
import com.cvicse.inforreport.model.Dataset;
import com.cvicse.inforreport.model.SQLDataDefine;
import com.cvicse.inforreport.util.DBConnection;
import com.cvicse.inforreport.util.EngineUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class LargeData {
	
	private static final Log log = LogFactory.getLog(LargeData.class);

	private Map conditions;

	private Map parameters;

	private DataDefine dataDefine;

	private String step;

	private String fstRecord = "0";

	// private Template template;

	private boolean finished = true;

	public LargeData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String processToString(HandlerChain handlerChain, String encoding)
			throws Throwable {
		Node node = this.process(handlerChain);
		Document document = DocumentHelper.createDocument();
		document.addElement("report_data").add(node.detach());
		String data = EngineUtils.convertToString(document, "", false, encoding);
		return data;

	}

	public Node process(HandlerChain handlerChain) throws Throwable {
//		Utils.debug("processing largedata");
		log.debug("processing largedata");
		Node dataNode = null;
		Connection conn = null;
		try {
			/*
			 * createHandler
			 * 
			 * 如果数据不为空，{
			 */
			Object data = this.getHandlerData(handlerChain);
			if (data != null) {
//				Utils.debug("set handler data");
				log.debug("set handler data");
				//因还没加入处理数据快照的方法，此处编译不能通过，暂时注释
				//dataDefine.setData(data);
			} else {
				if (dataDefine instanceof SQLDataDefine) {
//					Utils.debug("SQLDataDefine Id:" + dataDefine.getId());
					log.debug("SQLDataDefine Id:" + dataDefine.getId());

					conn = dataDefine.getCon();
					if(conn==null||conn.isClosed()){
						conn = DBConnection.getInstance().getConnection(dataDefine.getSource());
						dataDefine.setCon(conn);
					}
					

					String sql = ((SQLDataDefine) dataDefine).getSql();
					int count = ((SQLDataDefine) dataDefine).getCount();
					if(count==0){
						count = getCount(dataDefine,conn);
					}

					// 组织分页sql
					String limitSql = this.getLimitSql(conn,sql,fstRecord,step);
					((SQLDataDefine) dataDefine).setSql(limitSql);

					// 判断是否是最后一页
					if ((new Integer(fstRecord).intValue()
							+ new Integer(step).intValue() )< count) {
						finished = false;
					}
//					Utils.debug("finished: " + finished);
					log.debug("finished: " + finished);
				}
			}

			Dataset ds = new Dataset();
			Map dataDefines = new HashMap();
			dataDefines.put(dataDefine.getId(), dataDefine);
			ds.setDataDefines(dataDefines);
			// ds.setDataDefines((Map)new HashMap(1).put(dataDefine.getId(),dataDefine));
			//dataNode = ds.getAllData(conditions, parameters).getRootElement();
			DatasetProcessor dp = new DatasetProcessor(ds);
			//dp.getAllData(conditions, parameters);
			dataNode = dp.getDomData(ds,conditions, parameters).getRootElement();

		} catch (Throwable ex) {
//			Utils.fatal("Processing large data error.", ex);
			log.error("Processing large data error.", ex);
			throw ex;
		} finally {
			if (!(conn == null || conn.isClosed()))
				conn.close();

		}

		return dataNode;
	}

	private Dialect getDialect(String db) {
		db = db.toLowerCase();
		Dialect dialect = null;
		if (db.indexOf("hsql") >=0) {
			dialect = new HSQLDialect();
		} else if (db.indexOf("oracle9") >=0) {
			dialect = new Oracle9Dialect();
		} else if (db.indexOf("db2") >=0) {
			dialect = new DB2Dialect();
		} else if(db.indexOf("sql server")>=0){
			dialect = new SQLServerDialect();
		}
		else{
//			Utils.error("No such dialect: "+db);
			log.error("No such dialect: "+db);
			throw new RuntimeException("No such dialect:"+db);
		}
		return dialect;
	}
	
	public String getLimitSql(Connection con,String sql,String fstrow,String pagesize) throws Throwable {
		sql =sql.toLowerCase();
		String db = con.getMetaData().getDatabaseProductName() + " "
				+ con.getMetaData().getDatabaseProductVersion();
//		Utils.debug("Database: " + db);
		log.debug("Database: " + db);
		Dialect dialect = this.getDialect(db);
		String s = null;
		if(dialect instanceof SQLServerDialect){
			String key = ((SQLServerDialect)dialect).getKeyColumn(con,sql);
			s = dialect.getLimitString(sql,key,fstrow,pagesize);
		}else{
			s = dialect.getLimitString(sql, fstrow, pagesize);
		}
		//2012.4.20中软冠群(4.2源码同步过来的)
		if(s.indexOf("$v")>-1)
			s= s.replaceAll("\\$v", "\\$V");
//		Utils.debug("limit sql: " + s);
		log.debug("limit sql: " + s);
		return s;
	}

	public int getCount(DataDefine dataDefine) throws Throwable {
		String source = dataDefine.getSource();
		int count = 0;
		Connection conn = null;
		try {
			conn = DBConnection.getInstance().getConnection(source);
			count = this.getCount(dataDefine, conn,
					((SQLDataDefine) dataDefine).getSql());
		} catch (Throwable t) {
			throw t;
		} finally {
			if (conn != null)
				conn.close();
		}

		return count;
	}

	public int getCount(DataDefine dataDefine, Connection conn)
			throws Throwable {

		int count = this.getCount(dataDefine, conn,
				((SQLDataDefine) dataDefine).getSql());

		return count;
	}

	/**
	 * 获得sql的count值
	 * @param dataDefine
	 * @param conn
	 * @param sql
	 * @return
	 * @throws Throwable
	 */
	public int getCount(DataDefine dataDefine, Connection conn, String sql)
			throws Throwable {
		//获得count sql
		String countSql = this.getCountSQL(sql);
//		Utils.debug("count sql: " + countSql);
		log.debug("count sql: " + countSql);

		//得到count数
		int count = 0;
		if (countSql.indexOf("$V") == -1) { //无参数引用时直接查询数据库
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(countSql);
			while(rs.next())
				count = rs.getInt(1);
			//count = new Integer().intValue();
			rs.close();
			stat.close();
		} else { //有参数引用时调用businessdata获取结果集
			((SQLDataDefine) dataDefine).setSql(countSql);
			BusinessData busi = new BusinessData();
//			Node countNode = busi.processBusiData(dataDefine.processBasicData(
//					conn, conditions, parameters));
			IRDataProcessor idp = new SQLDataProcessor();
			String s = idp.processDataStr(dataDefine, conditions, parameters);
			Node countNode = busi.processBasicData(s);
			
			count = new Integer(countNode.selectSingleNode("data")
					.selectSingleNode("row").selectSingleNode("col").getText())
					.intValue();
		}
//		Utils.debug("count: " + count);
		log.debug("count: " + count);
		return count;

	}

	/**
	 * 组织计算count的sql
	 * @param sql 原sql
	 * @return 计算count的sql
	 * @throws Exception
	 */
	public String getCountSQL(String sql) throws Exception{
		//sql =sql.toLowerCase();

		//for中软冠群2012.4.20(4.2源码同步过来的)
		if(sql.toLowerCase().indexOf("order by")!=-1){
			sql = sql.substring(0,sql.lastIndexOf("order by"));
		}
		
		if (sql.toLowerCase().indexOf(" from ") != -1) {
//			if(sql.indexOf("union")!=-1){
//				sql = sql.substring(0,sql.indexOf("union"));
//			}
//			return new StringBuffer(sql.length() + 10)
//					.append("select count(*) ")
//					.append(sql.substring(sql.toLowerCase().indexOf(" from ")))
//					.toString();
			String sql11 = new StringBuffer(sql.length() + 10)
				.append("select count(*) ")
				.append(sql.substring(sql.toLowerCase().indexOf(" from ")))
				.toString();
			return sql11;
		}else{
//			Utils.error("'from' not found in sql: '"+sql+"'");
			log.error("'from' not found in sql: '"+sql+"'");
			throw new ReportException("'from' not found in sql: '"+sql+"'");
		}
	}

	private Object getHandlerData(HandlerChain handlerChain) throws Throwable {
		if (handlerChain == null)
			return null;
		com.cvicse.inforreport.handler.Request req = new com.cvicse.inforreport.handler.Request();
		req.setParameter("vpara", conditions);
		req.setParameter("cpara", parameters);
		req.setParameter("fstRecord", fstRecord);
		req.setParameter("step", step);
		req.setParameter("dataDefine", dataDefine);
		req.setParameter("page", String.valueOf(new Integer(fstRecord)
				.intValue()
				/ new Integer(step).intValue() + 1));
		com.cvicse.inforreport.handler.Response res = new com.cvicse.inforreport.handler.Response();

		handlerChain.handlerRequest(req, res);

		return res.getParameter("data");
	}

	/**
	 * @return Returns the step.
	 */
	public String getStep() {
		return step;
	}

	/**
	 * @param step
	 *            The step to set.
	 */
	public void setStep(String step) {
		this.step = step;
	}

	/**
	 * @return Returns the fstRecord.
	 */
	public String getFstRecord() {
		return fstRecord;
	}

	/**
	 * @param fstResult
	 *            The fstRecord to set.
	 */
	public void setFstRecord(String fstRecord) {
		this.fstRecord = fstRecord;
	}

	/**
	 * @return Returns the parameters.
	 */
	public Map getConditions() {
		return conditions;
	}

	/**
	 * @param conditions
	 *            The conditions to set.
	 */
	public void setConditions(Map conditions) {
		this.conditions = conditions;
	}

	/**
	 * @param parameters
	 *            The parameters to set.
	 */
	// public void setConditions(String conditions) {
	// Document dom = null;
	// StringReader reader = null;
	// Map map = null;
	// try {
	// reader = new StringReader(conditions);
	// SAXReader saxReader = new SAXReader();
	// saxReader.setEntityResolver(new EntityResolverImpl());
	// dom = saxReader.read(reader);
	// List paras = dom.getRootElement().elements();
	// map = new HashMap();
	// Utils.debug("Conditions: ");
	// for(int i=0;i<paras.size();i++){
	// Element para = (Element)paras.get(i);
	// String name = para.attributeValue("name");
	// String value = para.attributeValue("value");
	// map.put(name,value);
	// Utils.debug(" "+name+": "+value);
	// }
	// } catch (DocumentException ex) {
	// throw new RuntimeException(ex);
	// } finally {
	// reader.close();
	// }
	// this.conditions = map;
	// }
	/**
	 * @return Returns the dataDefineObj.
	 */
	public DataDefine getDataDefine() {
		return dataDefine;
	}

	/**
	 * @param dataDefineObj
	 *            The dataDefineObj to set.
	 */
	public void setDataDefine(DataDefine dataDefine) {
		this.dataDefine = dataDefine;
	}

	/**
	 * @return Returns the parameters.
	 */
	public Map getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            The parameters to set.
	 */
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return Returns the finished.
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @param finished
	 *            The finished to set.
	 */
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

}
