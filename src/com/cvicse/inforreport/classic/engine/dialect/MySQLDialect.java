package com.cvicse.inforreport.classic.engine.dialect;

public class MySQLDialect extends Dialect {

	public MySQLDialect() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getLimitString(String sql, boolean hasOffset) {
		return new StringBuffer( sql.length()+20 )
			.append(sql)
			.append( hasOffset ? " limit ?, ?" : " limit ?")
			.toString();
	}
	
	public String getLimitString(String sql, String fstResult, String step) {
		if(fstResult.equals("0"))
			return new StringBuffer( sql.length()+20 )
			.append(sql)
			.append(" limit ")
			.append(step)
			.toString();
		else
			return new StringBuffer( sql.length()+20 )
			.append(sql)
			.append(" limit "+fstResult+", "+step+"")
			.toString();
	}

}
