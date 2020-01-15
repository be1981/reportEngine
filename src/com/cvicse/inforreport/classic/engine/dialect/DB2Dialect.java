package com.cvicse.inforreport.classic.engine.dialect;

public class DB2Dialect  extends Dialect {

	public DB2Dialect() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getLimitString(String sql, String fstResult, String step) {
		sql = sql.trim().toLowerCase();
		int startOfSelect = sql.indexOf("select");	

		StringBuffer pagingSelect = new StringBuffer(sql.length() + 100)
				.append(sql.substring(0, startOfSelect)) //add the comment
				.append("select * from ( select "); //nest the main query in an outer select
				//070720,bug2642,delete
				//.append(getRowNumber(sql)); //add the rownnumber bit into the outer query select list

		if (hasDistinct(sql)) {
			pagingSelect.append(" row_.* ") 
					.append(getRowNumber(sql)) //070720,bug2642,add
					.append(" from ( ") //add another (inner) nested select
					.append(sql.substring(startOfSelect)) //add the main query
					.append(" ) as row_"); //close off the inner nested select
		} else {
			int starIndex = sql.indexOf("*");
			
			if(starIndex!=-1){		
				int whereIndex = sql.toLowerCase().indexOf("where");
				//070720,bug2642,delete and add
				//pagingSelect.append("temp1.*");			
				pagingSelect.append(sql.substring(startOfSelect + 6));
				pagingSelect.insert(pagingSelect.lastIndexOf("*"),"temp1.");
				pagingSelect.insert(pagingSelect.lastIndexOf("*")+1,getRowNumber(sql));
				if(whereIndex!=-1)
					pagingSelect.insert(pagingSelect.indexOf("where")," as temp1 ");
				else
					pagingSelect.append(" as temp1 ");
				/* old code,not simple enough
				pagingSelect.append("temp1.*").append(getRowNumber(sql)); 				
				if(whereIndex!=-1){
					pagingSelect.append(sql.substring(starIndex+1,whereIndex));
				}else{
					pagingSelect.append(sql.substring(starIndex+1));
				}				
				pagingSelect.append(" as temp1 ");
				if(whereIndex!=-1)
					pagingSelect.append(sql.substring(whereIndex));
				*/
			}
			else{
				//070720,bug2642,delete and add
				//pagingSelect.append(sql.substring(startOfSelect + 6)); //add the main query
				pagingSelect.append(sql.substring(startOfSelect + 6)); 
				pagingSelect.insert(pagingSelect.lastIndexOf("from")-1,getRowNumber(sql));

			}
		}

		pagingSelect.append(" ) as temp_ where rownumber_ ");

		//add the restriction to the outer select
		int f = new Integer(fstResult).intValue();
		int s = new Integer(step).intValue();
		if (!fstResult.equals("0")) {
			pagingSelect.append("between "+f+" and "+(f+s));
		} else {
			pagingSelect.append("<= "+step);
		}

		return pagingSelect.toString();
	}

	public String getLimitString(String sql, boolean hasOffset) {
		sql = sql.trim().toLowerCase();
		int startOfSelect = sql.indexOf("select");

		StringBuffer pagingSelect = new StringBuffer(sql.length() + 100)
				.append(sql.substring(0, startOfSelect)) //add the comment
				.append("select * from ( select ") //nest the main query in an outer select
				.append(getRowNumber(sql)); //add the rownnumber bit into the outer query select list

		if (hasDistinct(sql)) {
			pagingSelect.append(" row_.* from ( ") //add another (inner) nested select
					.append(sql.substring(startOfSelect)) //add the main query
					.append(" ) as row_"); //close off the inner nested select
		} else {
			pagingSelect.append(sql.substring(startOfSelect + 6)); //add the main query
		}

		pagingSelect.append(" ) as temp_ where rownumber_ ");

		//add the restriction to the outer select
		if (hasOffset) {
			pagingSelect.append("between ?+1 and ?");
		} else {
			pagingSelect.append("<= ?");
		}

		return pagingSelect.toString();
	}

	/**
	 * Render the <tt>rownumber() over ( .... ) as rownumber_,</tt> 
	 * bit, that goes in the select list
	 */
	private String getRowNumber(String sql) {
		StringBuffer rownumber = new StringBuffer(50)
				//.append("rownumber() over(");
				.append(",rownumber() over("); //070720,bug2642

		int orderByIndex = sql.indexOf("order by");

		if (orderByIndex > 0 && !hasDistinct(sql)) {
			rownumber.append(sql.substring(orderByIndex));
		}

		//rownumber.append(") as rownumber_,");
		rownumber.append(") as rownumber_");  //070720,bug2642

		return rownumber.toString();
	}

	private static boolean hasDistinct(String sql) {
		return sql.indexOf("select distinct") >= 0;
	}

}
