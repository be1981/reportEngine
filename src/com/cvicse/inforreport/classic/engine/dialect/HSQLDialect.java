package com.cvicse.inforreport.classic.engine.dialect;

public class HSQLDialect extends Dialect {

	public HSQLDialect() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getLimitString(String sql, boolean hasOffset) {
		return new StringBuffer( sql.length() + 10 )
				.append( sql )
				.insert( sql.indexOf( "select" ) + 6, hasOffset ? " limit ? ?" : " top ?" )
				.toString();
	}
	
	public String getLimitString(String sql, String fstResult, String step) {
		if(fstResult.equals("0"))
			return new StringBuffer( sql.length() + 10 )
				.append( sql )
				.insert( sql.indexOf( "select" ) + 6, " top "+step )
				.toString();
		else
			return new StringBuffer( sql.length() + 10 )
			.append( sql )
			.insert( sql.indexOf( "select" ) + 6, " limit "+(new Integer(fstResult).intValue()-1)+" "+step )
			.toString();
	}


}
