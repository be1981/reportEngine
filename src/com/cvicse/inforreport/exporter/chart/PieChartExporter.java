package com.cvicse.inforreport.exporter.chart;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class PieChartExporter implements IChartExporter {

	public JFreeChart createChart(Document data) {
		Element root = data.getRootElement();
		boolean showlegend =false;
		if ("1".equals(root.attributeValue("showLegend")))
			showlegend = true;
		JFreeChart chart = null;
		if("0".equals(root.attributeValue("isPie"))){
			chart = ChartFactory.createRingChart(
					root.attributeValue("caption"), 
					(PieDataset) getDataset(data),
					showlegend, 
					true, 
					true);
		}
		else if ("3".equals(root.attributeValue("startMode2D3D"))) {
				chart = ChartFactory.createPieChart3D(
						root.attributeValue("caption"), 
						(PieDataset) getDataset(data),
						showlegend, 
						true, 
						true);
		} else if ("2".equals(root.attributeValue("startMode2D3D"))) {
			chart = ChartFactory.createPieChart(
					root.attributeValue("caption"),
					(PieDataset) getDataset(data), 
					showlegend, 
					true, 
					true);
		}
		//图表label字体
		Font basefont = new Font(root.attributeValue("baseFont"),
				"1".equals(root.attributeValue("baseFontIsBold"))?Font.BOLD:Font.PLAIN, 
				Integer.parseInt(root.attributeValue("baseFontSize")));
		Color basefontcolor = new Color(Integer.parseInt(
				"".equals(root.attributeValue("baseFontColor")) ? "000000" : root
				.attributeValue("baseFontColor"), 16));

		//面板字体
		Font cnvfont = new Font(root.attributeValue("outCnvBaseFont"),
				"1".equals(root.attributeValue("outCnvBaseFontIsBold"))?Font.BOLD:Font.PLAIN, 
						Integer.parseInt(root.attributeValue("outCnvBaseFontSize")));
		Color cnvfontcolor = new Color(Integer.parseInt("".equals(root
				.attributeValue("outCnvBaseFontColor")) ? "000000" : root
						.attributeValue("outCnvBaseFontColor"), 16));
		
		//标题字体，实际测试captionFontSize无效，outCnvBaseFontSize生效
		Font titlefont = new Font(root.attributeValue("outCnvBaseFont"),
				"1".equals(root.attributeValue("outCnvBaseFontIsBold"))?Font.BOLD:Font.PLAIN, 
						Integer.parseInt(root.attributeValue("captionFontSize")));
		
		
		//标题，实际测试basefontcolor对title、subtitle、legend生效
		chart.getTitle().setFont(titlefont);
		chart.getTitle().setPaint(cnvfontcolor);
		//chart.getTitle().setPaint(basefontcolor);
		TextTitle subtitle = new TextTitle(root.attributeValue("subCaption"));
		subtitle.setFont(cnvfont);
		subtitle.setPaint(cnvfontcolor);
		//subtitle.setPaint(basefontcolor);
		chart.addSubtitle(subtitle);
		
		//背景色
		PiePlot  plot=(PiePlot)chart.getPlot();
		String bgColor = root.attributeValue("bgColor");
		if(bgColor.indexOf(",")>-1)
			bgColor = bgColor.substring(0,bgColor.indexOf(","));
		plot.setBackgroundPaint(new Color(Integer.parseInt(bgColor,16)));
		plot.setOutlinePaint(new Color(Integer.parseInt(bgColor,16)));
		if(!"".equals(root.attributeValue("bgAlpha")))
			plot.setBackgroundAlpha(Integer.parseInt(root.attributeValue("bgAlpha")));
		//plot.setBackgroundImage(image)

		//环形图的半径
		if("0".equals(root.attributeValue("isPie")) 
				&& !"".equals(root.attributeValue("radius"))
				&& !"".equals(root.attributeValue("innerRadius"))){
			((RingPlot)plot).setSectionDepth(Double.parseDouble(root.attributeValue("innerRadius"))/
					Double.parseDouble(root.attributeValue("radius")));
		}
		plot.setCircular(true);

		//图表label
		List<Element> sets = root.elements("set");
		for(Element set:sets){
			if(set.attributeValue("color")!=null)
				plot.setSectionPaint(set.attributeValue("name"), new Color(Integer.parseInt(set.attributeValue("color"),16)));
			if("1".equals(set.attributeValue("isSliced")))
				plot.setExplodePercent(set.attributeValue("name"), 0.2);
		}
		if(sets.get(0).attributeValue("alpha")!=null)
			plot.setForegroundAlpha(Float.parseFloat((sets.get(0).attributeValue("alpha")))/100);
		String format = null;
		if ("1".equals(root.attributeValue("showNames")))
			format = "{0}";
		if ("1".equals(root.attributeValue("showValues")))
			format += "={1}";
		if ("1".equals(root.attributeValue("showPercentageValues")))
			format += "({2})";
		
		NumberFormat numberformat = NumberFormat.getInstance();
		NumberFormat percentformat = new DecimalFormat("0.00%");
		if("1".equals(root.attributeValue("formatNumber"))){
			numberformat = new DecimalFormat(root.attributeValue("numberPrefix")
					+ Format.getNumber(Integer.parseInt(root.attributeValue("decimalPrecision")))
					+ root.attributeValue("numberSuffix"));
			percentformat = new DecimalFormat(Format.getPercent(
					Integer.parseInt(root.attributeValue("percentageDecimalPrecision"))));
		}
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator(format,
				numberformat, percentformat));
		plot.setLabelFont(basefont);
		plot.setLabelPaint(basefontcolor);

		//图例
		if ("1".equals(root.attributeValue("showLegend"))) {
			format = null;
			if("1".equals(root.attributeValue("showLegendName")))
				format = "{0}";
			if("1".equals(root.attributeValue("showLegendValues")))
				format += "={1}";
			if("1".equals(root.attributeValue("showLegendPercentage")))
				format += "({2})";				
			plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(format,
					numberformat, percentformat));  
			chart.getLegend().setItemFont(cnvfont);
			chart.getLegend().setItemPaint(cnvfontcolor);
			//chart.getLegend().setItemPaint(basefontcolor);
		}
		
		return chart;
	}
	
	public Dataset getDataset(Document data) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		List<Element> elems = data.getRootElement().elements();
		for (Element elem : elems) {
			dataset.setValue(elem.attributeValue("name"), new Double(elem
					.attributeValue("value")));
			//System.out.println(elem.attributeValue("name")+"--"+elem.attributeValue("value"));
		}
		return dataset;
	}

}
