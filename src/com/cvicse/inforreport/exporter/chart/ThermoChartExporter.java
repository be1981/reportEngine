package com.cvicse.inforreport.exporter.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultValueDataset;

public class ThermoChartExporter implements IChartExporter {

	public JFreeChart createChart(Document data) {
		Element root = data.getRootElement();
		DefaultValueDataset dataset = (DefaultValueDataset) getDataset(data);
		ThermometerPlot plot = new ThermometerPlot(dataset);
		JFreeChart jfreechart = new JFreeChart(
				"",JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		//背景色
		if(!"".equals(root.attributeValue("bgColor")))
			plot.setBackgroundPaint(new Color(Integer.parseInt(root.attributeValue("bgColor"), 16)));
		if(!"".equals(root.attributeValue("bgAlpha")))
			plot.setBackgroundAlpha(Float.parseFloat(root.attributeValue("bgAlpha"))/100);
		
		//边框色
		if(!"".equals(root.attributeValue("borderColor")))
			plot.setThermometerPaint(new Color(Integer.parseInt(root.attributeValue("borderColor"), 16)));
		if(!"".equals(root.attributeValue("borderThickness")))
			plot.setThermometerStroke(new BasicStroke(Float.parseFloat(root.attributeValue("borderThickness"))));
		plot.setGap(0);
		
		//前景透明度
		if(!"".equals(root.attributeValue("thmFillAlpha")))
			plot.setForegroundAlpha(Float.parseFloat(root.attributeValue("thmFillAlpha"))/100);
		if(!"".equals(root.attributeValue("lowerLimit")))
			plot.setLowerBound(Double.parseDouble(root.attributeValue("lowerLimit")));
		if(!"".equals(root.attributeValue("upperLimit")))
			plot.setUpperBound(Double.parseDouble(root.attributeValue("upperLimit")));
		
		//显示的刻度个数
//		if (!"".equals(root.attributeValue("lowerLimit"))
//				&& !"".equals(root.attributeValue("upperLimit"))
//				&& !"".equals(root.attributeValue("majorTMNumber"))) {
//			int l = Integer.parseInt(root.attributeValue("lowerLimit"));
//			int u = Integer.parseInt(root.attributeValue("upperLimit"));
//			int n = Integer.parseInt(root.attributeValue("majorTMNumber"));
//			plot.setUnits((u-l)/(n-1));
//		}

		plot.setSubrange(0, plot.getLowerBound(), plot.getUpperBound());
		if(!"".equals(root.attributeValue("thmFillColor")))
			plot.setSubrangePaint(0, new Color(Integer.parseInt(root.attributeValue("thmFillColor"), 16)));

		//plot.setRange(Double.parseDouble(root.attributeValue("lowerLimit")), Double.parseDouble(root.attributeValue("upperLimit")));
		
		//图表label字体
		Font basefont = new Font(root.attributeValue("baseFont"),
				Font.PLAIN, Integer.parseInt(root.attributeValue("baseFontSize")));
		Color basefontcolor = new Color(Integer.parseInt(
				"".equals(root.attributeValue("baseFontColor")) ? "000000" : root
				.attributeValue("baseFontColor"), 16));
		plot.setValueFont(basefont);
		plot.setValuePaint(basefontcolor);
		if("1".equals(root.attributeValue("formatNumber")))
			plot.setValueFormat(new DecimalFormat(root.attributeValue("numberPrefix")
					+ Format.getNumber(Integer.parseInt(root.attributeValue("decimalPrecision")))
					+ root.attributeValue("numberSuffix")));
		//plot.setValueLocation(ThermometerPlot.RIGHT);
		//if(!"".equals(root.attributeValue("thmBulbRadius")))
		//	plot.setBulbRadius(Integer.parseInt(root.attributeValue("thmBulbRadius")));
	
		return jfreechart;
	}

	
	public Dataset getDataset(Document data) {
		String value = data.getRootElement().elementText("value");
		DefaultValueDataset dataset = new DefaultValueDataset(Double.parseDouble(value));
		return dataset;	
		
	}
}
