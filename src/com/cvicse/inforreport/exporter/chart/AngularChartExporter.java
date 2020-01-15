package com.cvicse.inforreport.exporter.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultValueDataset;

public class AngularChartExporter implements IChartExporter {

	public JFreeChart createChart(Document data) {
		Element root = data.getRootElement();
		DefaultValueDataset dataset = (DefaultValueDataset) getDataset(data);
		MeterPlot plot = new MeterPlot(dataset);
		//背景色
		if(!"".equals(root.attributeValue("bgColor")))
			plot.setBackgroundPaint(new Color(Integer.parseInt(root.attributeValue("bgColor"), 16)));
		if(!"".equals(root.attributeValue("bgAlpha")))
			plot.setBackgroundAlpha(Float.parseFloat(root.attributeValue("bgAlpha"))/100);
		
		//图表label字体
		Font basefont = new Font(root.attributeValue("baseFont"),
				Font.PLAIN, Integer.parseInt(root.attributeValue("baseFontSize")));
		Color basefontcolor = new Color(Integer.parseInt(
				"".equals(root.attributeValue("baseFontColor")) ? "000000" : root
				.attributeValue("baseFontColor"), 16));
		//指针属性
		Element dail = (Element)root.selectSingleNode("dials/dial");
		plot.setNeedlePaint(new Color(Integer.parseInt(dail.attributeValue("bgColor"), 16)));
		//plot.setUnits("");
		
		plot.setDialBackgroundPaint(Color.white); 
		plot.setDialShape(DialShape.CHORD);
		
		//表盘显示的角度，如未设置显示180度（半圆）
		if (!"".equals(root.attributeValue("gaugeScaleAngle"))
				&& !"".equals(root.attributeValue("gaugeStartAngle")))
			plot.setMeterAngle(Integer.parseInt(root
					.attributeValue("gaugeScaleAngle"))
					- Integer.parseInt(root.attributeValue("gaugeStartAngle")));
		else
			plot.setMeterAngle(180);

		//显示刻度
		plot.setTickLabelsVisible("1".equals(root.attributeValue("showTickMarks"))?true:false);
		plot.setTickLabelFont(basefont);
		plot.setTickLabelPaint(basefontcolor);
		if(!"".equals(root.attributeValue("majorTMColor")))
			plot.setTickPaint(new Color(Integer.parseInt(root.attributeValue("majorTMColor"), 16)));
		if("1".equals(root.attributeValue("formatNumber"))){
			NumberFormat format = new DecimalFormat(root.attributeValue("numberPrefix")
					+ Format.getNumber(Integer.parseInt(root.attributeValue("decimalPrecision")))
					+ root.attributeValue("numberSuffix"));
			plot.setTickLabelFormat(format);
		}
		if("1".equals(root.attributeValue("formatNumberScale"))){
			plot.setUnits(root.attributeValue("defaultNumberScale"));			
		}
		plot.setValuePaint(basefontcolor);
		plot.setValueFont(basefont);
		
		//显示的刻度个数
		if (!"".equals(root.attributeValue("lowerLimit"))
				&& !"".equals(root.attributeValue("upperLimit"))
				&& !"".equals(root.attributeValue("majorTMNumber"))) {
			double l = Double.parseDouble(root.attributeValue("lowerLimit"));
			double u = Double.parseDouble(root.attributeValue("upperLimit"));
			double n = Double.parseDouble(root.attributeValue("majorTMNumber"));
			plot.setTickSize((u-l)/(n-1));
		}
		
		//表盘显示范围
		if (!"".equals(root.attributeValue("lowerLimit"))
				&& !"".equals(root.attributeValue("upperLimit")))
			plot.setRange(new Range(Double.parseDouble(root
					.attributeValue("lowerLimit")), Double.parseDouble(root
					.attributeValue("upperLimit"))));
		Stroke stroke  = null;
		if(!"".equals(root.attributeValue("borderThickness")))
			stroke = new BasicStroke(Float.parseFloat(root.attributeValue("borderThickness")));
		List<Element> colors = root.selectNodes("colorRange/color");
		for(Element color:colors){
			String c = "".equals(color.attributeValue("borderColor"))?root.attributeValue("borderColor"):color.attributeValue("borderColor");
			Color bordercolor = null;			
			if(!"".equals(c))			
				bordercolor = new Color(Integer.parseInt(c, 16));				
	
			plot.addInterval(new MeterInterval(
					color.attributeValue("name"),
					new Range(Double.parseDouble(color.attributeValue("minValue")), Double.parseDouble(color.attributeValue("maxValue"))), 
					bordercolor, 
					stroke,
					new Color(Integer.parseInt(color.attributeValue("code"),16))));
		}
		//range透明度
		if(!"".equals(colors.get(0).attributeValue("alpha")))
			plot.setForegroundAlpha(Float.parseFloat(colors.get(0).attributeValue("alpha"))/100);
		JFreeChart jfreechart = new JFreeChart(
				"",JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		return jfreechart;
	}
	
	public Dataset getDataset(Document data) {
		String value = data.getRootElement().selectSingleNode("dials/dial").valueOf("@value");
		DefaultValueDataset dataset = new DefaultValueDataset(Double.parseDouble(value));
		return dataset;	
		
	}

}
