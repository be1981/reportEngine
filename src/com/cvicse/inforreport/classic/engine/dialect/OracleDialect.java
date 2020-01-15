package com.cvicse.inforreport.classic.engine.dialect;

public class OracleDialect extends Dialect {

	public OracleDialect() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String getLimitString(String sql, boolean hasOffset) {

		sql = sql.trim();
		boolean isForUpdate = false;
		if ( sql.toLowerCase().endsWith(" for update") ) {
			sql = sql.substring( 0, sql.length()-11 );
			isForUpdate = true;
		}
		
		StringBuffer pagingSelect = new StringBuffer( sql.length()+100 );
		if (hasOffset) {
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		}
		else {
			pagingSelect.append("select * from ( ");
		}
		pagingSelect.append(sql);
		if (hasOffset) {
			pagingSelect.append(" ) row_ ) where rownum_ <= ? and rownum_ > ?");
		}
		else {
			pagingSelect.append(" ) where rownum <= ?");
		}

		if (isForUpdate) pagingSelect.append(" for update");
		
		return pagingSelect.toString();
	}
	
	public String getLimitString(String sql, String fstResult, String step) {
		sql = sql.trim();
		boolean isForUpdate = false;
		if ( sql.toLowerCase().endsWith(" for update") ) {
			sql = sql.substring( 0, sql.length()-11 );
			isForUpdate = true;
		}
		
		StringBuffer pagingSelect = new StringBuffer( sql.length()+100 );
		if (!fstResult.equals("0")) {
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		}
		else {
			pagingSelect.append("select * from ( ");
		}
		pagingSelect.append(sql);
		
		int f = new Integer(fstResult).intValue();
		int s = new Integer(step).intValue();
		if (!fstResult.equals("0")) {
			pagingSelect.append(" ) row_ ) where rownum_ <= "+(f+s-1)+" and rownum_ > "+(f-1));
		}
		else {
			pagingSelect.append(" ) where rownum <= "+(f+s));
		}

		if (isForUpdate) pagingSelect.append(" for update");
		
		return pagingSelect.toString();
	}

}
