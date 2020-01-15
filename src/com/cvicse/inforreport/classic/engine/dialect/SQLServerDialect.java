package com.cvicse.inforreport.classic.engine.dialect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLServerDialect extends Dialect {

	public SQLServerDialect() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	static int getAfterSelectInsertPoint(String sql) {
		sql = sql.trim().toLowerCase();
		final int selectDistinctIndex = sql.indexOf( "select distinct" );
		if ( selectDistinctIndex >= 0 ) {
			return selectDistinctIndex + 15;
		}
		else {
			return sql.indexOf( "select" ) + 6;
		}
	}
	
	static int getAfterWhereInsertPoint(String sql){
		sql = sql.trim().toLowerCase();
		final int whereIndex = sql.indexOf( "where" );
		if(whereIndex>=0){
			return sql.indexOf("where")+5;
		}else{
			return -1;
		}
		
	}

	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset>0) throw new UnsupportedOperationException("sql server has no offset");
		return new StringBuffer( querySelect.length()+8 )
			.append(querySelect)
			.insert( getAfterSelectInsertPoint(querySelect), " top " + limit )
			.toString();
	}
	
	public String getLimitString(String sql,String key, String fstResult, String step) {
		sql = sql.trim().toLowerCase();
		/*
		int topNum = 0;
		if(sql.indexOf("top")!=-1){
			int begin = sql.indexOf("top")+3;
			String space = String.valueOf(sql.charAt(begin));
			while(space.equals(" ")){
				begin ++;
				space = String.valueOf(sql.charAt(begin));				
			}
			int end = sql.indexOf(" ",begin);
			String topValue = sql.substring(begin,end).trim();
			sql = new StringBuffer(sql.substring(0,sql.indexOf("top")))
					.append(sql.substring(end)).toString();
			topNum = Integer.parseInt(topValue);
		}*/		
		
		final int whereIndex = sql.indexOf("where");
		StringBuffer buff = new StringBuffer(sql.length()+80);
		if(whereIndex>=0)
			buff.append(sql.substring(0,whereIndex));
		else
			buff.append(sql);		

		buff.insert(getAfterSelectInsertPoint(sql), " top " + step);
		if(!fstResult.equals("0")){
			buff.append(" where (").append(key).append(" NOT IN (SELECT TOP ")
			.append((new Integer(fstResult).intValue() - 1) + " " + key)
			.append(" FROM ")
			.append(this.getFirstTableName(sql))
			.append("))");
			if(whereIndex>=0)
				buff.append(" and");
		}else{
			if(whereIndex>=0)
				buff.append(" where");
		}
		
		
		if(whereIndex>=0)
			buff.append(sql.substring(getAfterWhereInsertPoint(sql)));
		
		return buff.toString();
	}
	
	/**
	 * �������.���û������,��ѯ�����ֶ�,ȡ������ĸ�������еĵ�һ���ֶ�
	 * @param con
	 * @param sql
	 * @return
	 * @throws Throwable
	 */
	public String getKeyColumn(Connection con,String sql)throws Throwable{
		String tableName = this.getFirstTableName(sql);
		String key = null;
		Statement stat = con.createStatement();
		ResultSet rs = stat.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME='"+tableName+"'");
		while(rs.next())
			key = rs.getString(1);
		
		if(key==null||key.equals("")){
			stat.clearBatch();
			rs = stat.executeQuery("SELECT name FROM syscolumns WHERE (id= OBJECT_ID('"+tableName+"'))");
			while(rs.next()){
				key = rs.getString(1);
				break;
			}
		}
		rs.close();
		stat.close();

		
		return key;
	}
	
	/**
	 * ��ñ���.���ж���ѯʱ,ȡ��һ������
	 * @param sql
	 * @return
	 */
	String getFirstTableName(String sql){
		sql = sql.toLowerCase();
		String tableName = null;
		int index = sql.indexOf("where");
		if(index>=0)
			tableName = sql.substring(sql.indexOf("from")+4,index);
		else
			tableName = sql.substring(sql.indexOf("from")+4);
		
		tableName = tableName.trim();
		index = tableName.indexOf(",");
		if(index>=0){
			tableName = tableName.substring(0,index);
		}
		index = tableName.indexOf(" ");
		if(index >=0){
			tableName = tableName.substring(0,index);
		}
		return tableName.trim();
	}

}
