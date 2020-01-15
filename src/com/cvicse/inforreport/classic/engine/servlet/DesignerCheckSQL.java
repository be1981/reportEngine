package com.cvicse.inforreport.classic.engine.servlet;

import java.io.CharArrayWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.cvicse.inforreport.datasource.DBPool;
import com.cvicse.inforreport.util.DBConnection;
import com.cvicse.inforreport.util.EntityResolverImpl;
import com.cvicse.inforreport.util.Utils;

import sun.jdbc.rowset.WebRowSet;

public class DesignerCheckSQL{

	public DesignerCheckSQL() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 处理sql，获得元数据
	 * @param cdata
	 * @return
	 * @throws Throwable
	 */
	public String check(String cdata)throws Throwable {
		Connection con = null;
		Statement stat = null;
		ResultSet rs = null;
		CharArrayWriter writer = null;
		
		String checkInfo = null;		
		try{	
			
			//解析设计器发送的数据源名和sql			
			SAXReader builder = new SAXReader();
			builder.setEntityResolver(new EntityResolverImpl());
			StringReader reader = new StringReader(cdata);
			Element datadefine = builder.read(reader).getRootElement();
			String conName = datadefine.elementText("connection");// sql校验信息中的DB连接串
			String sql = datadefine.elementText("sql");// sql校验信息中的sql语句
			reader.close();	
			
			//连接数据库
			if(datadefine.element("connection").valueOf("@type")!=null && "1".equals(datadefine.element("connection").valueOf("@type"))){
				con = DBPool.getInstance().getConnection(conName);
			}else{
				con = DBConnection.getInstance().getConnection(conName);
			}
			//查询数据
			stat = con.createStatement();
			rs = stat.executeQuery(sql);	
			writer = new CharArrayWriter();
			WebRowSet.writeXml(rs,writer);
			checkInfo = getCheckSQLInfo(writer.toString()); //从行集信息中获得元数据，作为校验信息
		}catch(DocumentException ex){
//			log.error(Utils.getResourceValue("CDataDefineParseError"),ex);
			ex.printStackTrace();
			throw new Exception(getErrorInfo("ParseError: "+ex.getMessage()));
		}catch(NamingException ex){
//			log.error(Utils.getResourceValue("ConnectDBFailed"),ex);
			ex.printStackTrace();
			throw new Exception(getErrorInfo("ConnectDBFailed: "+ex.getMessage()));
		}catch(Throwable ex){
//			log.error(Utils.getResourceValue("CheckSQLFailed"),ex);
			ex.printStackTrace();
			throw new Exception(getErrorInfo(ex.getMessage()));
		}finally{
			try {
				if (rs != null) 
					rs.close();				
				if (stat != null) 
					stat.close();				
				if (con != null) 
					con.close();				
				if (writer != null) 
					writer.close();						
			} catch (SQLException ex) {
//				log.error(Utils.getResourceValue("ConnectionCloseError"),ex);
				throw new Exception(getErrorInfo("ConnectionCloseError: "+ex.getMessage()));
			}
		}
		
		return checkInfo;
	}

	
	/**
	 * 获得sql校验信息
	 * @param webrs 执行sql得到的行集格式字符串
	 * @return 返回给查看器的xml格式校验信息
	 * @throws DocumentException
	 */
	private String getCheckSQLInfo(String webrs)
			throws Throwable {

		SAXReader builder = new SAXReader();
		builder.setEntityResolver(new EntityResolverImpl());
		StringReader reader = new StringReader(webrs);
		Element metadata = (Element) builder.read(reader).getRootElement()
				.element("metadata").detach();
		metadata.remove(metadata.element("auto-increment"));
		metadata.remove(metadata.element("case-sensitive"));
		metadata.remove(metadata.element("currency"));
		metadata.remove(metadata.element("signed"));
		metadata.remove(metadata.element("searchable"));
		metadata.remove(metadata.element("column-display-size"));
		metadata.remove(metadata.element("schema-name"));
		metadata.remove(metadata.element("column-precision"));
		metadata.remove(metadata.element("column-scale"));
		metadata.remove(metadata.element("catalog-name"));
		reader.close();
//		log.debug(Utils.getResourceValue("CheckSQLInfo")+": "+metadata.asXML());
		
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("checkSQLInfo");
		root.addElement("statuscode").addText("OK");
		root.add(metadata);
		//2007-2-12修改，Utils.encodeBase64中的getBytes时不强制使用GBK，适应繁体系统
		return Utils.encodeBase64ForCheckSQL(Utils.convertToString(document,"", false, "UTF-8"));
	}

	/**
	 * 获得sql校验错误信息
	 * @param error 出错原因
	 * @return 返回给查看器的xml格式校验信息
	 */
	public String getErrorInfo(String error) throws Throwable {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("checkSQLInfo");
		root.addElement("statuscode").addText(error);
		root.addElement("metadata");
//		2007-2-12修改，Utils.encodeBase64中的getBytes时不强制使用GBK，适应繁体系统
		return Utils.encodeBase64ForCheckSQL(Utils.convertToString(document,"", false, "UTF-8"));
		//return Utils.encodeBase64(dom.asXML()); //用dom.asXML得到的字符串中文是乱码
	}
}
