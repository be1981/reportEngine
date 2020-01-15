package com.cvicse.inforreport.classic.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FieldTypes {
	
	private static final Log log = LogFactory.getLog(FieldTypes.class);
	
	
	public FieldTypes() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public int getFieldType(String name){
		if(name==null){
			return 0;
		}
		name = name.toUpperCase();
		if(name.equals("ARRAY")){
			return 2003;
		}
		else if(name.equals("BIGINT")||name.equals("UNSIGNEDBIGINT")){
			return -5;
		}
		else if(name.equals("BINARY")){
			return -2;
		}
		else if(name.equals("BIT")){
			return -7;
		}
		else if(name.equals("BLOB")){
			return 2004;
		}
		else if(name.equals("BOOLEAN")){
			return 16;
		}
		else if(name.equals("CHAR")||name.equals("WCHAR")){
			return 1;
		}
		else if(name.equals("CLOB")){
			return 2005;
		}
		else if(name.equals("DATALINK")){
			return 70;
		}
		else if(name.equals("DATE")||name.equals("DBDATE")){
			return 91;
		}
		else if(name.equals("DECIMAL")){
			return 3;
		}
		else if(name.equals("DISTINCT")){
			return 2001;
		}
		else if(name.equals("DOUBLE")||name.equals("CURRENCY")){
			return 8;
		}
		else if(name.equals("FLOAT")||name.equals("SINGLE")){
			return 6;
		}
		else if(name.equals("INTEGER")||name.equals("UNSIGNEDINT")){
			return 4;
		}
		else if(name.equals("JAVA_OBJECT")){
			return 2000;
		}
		else if(name.equals("LONGVARBINARY")){
			return -4;
		}
		else if(name.equals("LONGVARCHAR")||name.equals("LONGVARWCHAR")){
			return -1;
		}
		else if(name.equals("NUMERIC")||name.equals("VARNUMERIC")){
			return 2;
		}
		else if(name.equals("OTHER")){
			return 1111;
		}
		else if(name.equals("REAL")){
			return 7;
		}
		else if(name.equals("REF")){
			return 2006;
		}
		else if(name.equals("SMALLINT")||name.equals("UNSIGNEDSMALLINT")){
			return 5;
		}
		else if(name.equals("STRUCT")){
			return 2002;
		}
		else if(name.equals("TIME")||name.equals("DBTIME")){
			return 92;
		}
		else if(name.equals("TIMESTAMP")||name.equals("DBTIMESTAMP")){
			return 93;
		}
		else if(name.equals("TINYINT")||name.equals("UNSIGNEDTINYINT")){
			return -6;
		}
		else if(name.equals("VARBINARY")){
			return -3;
		}
		else if(name.equals("VARCHAR")||name.equals("VARWCHAR")){
			return 12;
		}
		else{
//			Utils.debug(" - - - column-type-name: "+name);
			log.debug(" - - - column-type-name: "+name);
			return 12;
		}
	}

}
