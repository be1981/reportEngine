package com.cvicse.inforreport.exporter.chart;

import org.dom4j.Document;
import org.jfree.chart.JFreeChart;

public interface IChartExporter {
	
	public JFreeChart createChart(Document data);

}
