package com.cvicse.inforreport.exporter.chart;

public class Format {
	
	public static final String DECIMAL0 = "0";
	
	public static final String DECIMAL1 = "0.0";
	
	public static final String DECIMAL2 = "0.00";
	
	public static String getPercent(int precision){
		return getNumber(precision)+"%";
	}
	
	public static String getNumber(int precision){
		String s = null;
		switch(precision){
		case 0:
			s = DECIMAL0;
			break;
		case 1:
			s = DECIMAL1;
			break;
		default:
			s = DECIMAL2;
			break;
			
		}
		return s;
	}
}
