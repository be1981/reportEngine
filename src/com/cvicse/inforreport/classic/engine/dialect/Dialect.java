package com.cvicse.inforreport.classic.engine.dialect;

public abstract class Dialect {

	public Dialect() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getLimitString(String querySelect, boolean hasOffset) {
		throw new UnsupportedOperationException( "paged queries not supported" );
	}

	public String getLimitString(String querySelect, int offset, int limit) {
		return getLimitString( querySelect, offset>0 );
	}
	
	public String getLimitString(String sql, String fstResult, String step) {
		throw new UnsupportedOperationException( "paged queries not supported" );
	}
	
	public String getLimitString(String sql,String key, String fstResult, String step) {
		throw new UnsupportedOperationException( "paged queries not supported" );
	}
	
	public String getCountSQL(String sql){
//		if(sql.indexOf("union")==-1&&sql.indexOf("from")!=-1){
			return new StringBuffer(sql.length()+10)
			.append("select count(*) ")
			.append(sql.substring(sql.indexOf("from")))
			.toString();
//		}
//		else
//		return null;		;
	}

}
