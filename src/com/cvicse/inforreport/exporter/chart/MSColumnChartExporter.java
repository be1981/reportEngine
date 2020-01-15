package com.cvicse.inforreport.exporter.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.ui.TextAnchor;

public class MSColumnChartExporter implements IChartExporter {

	public JFreeChart createChart(Document data) {
		Element root = data.getRootElement();
		JFreeChart chart = ChartFactory.createBarChart3D(
				root.attributeValue("caption"), 
				root.attributeValue("xAxisName"), 
				root.attributeValue("yAxisName"), 
				(CategoryDataset)getDataset(data), 
				PlotOrientation.VERTICAL, 
				root.attributeValue("showLegend").equals("1")?true:false, 
				false, 
				false);
		//背景色
		if(!"".equals(root.attributeValue("bgColor"))){
			Color bgcolor = new Color(Integer.parseInt(root.attributeValue("bgColor"), 16));
			if(!"".equals(root.attributeValue("bgAlpha")))
				chart.setBackgroundPaint(new Color(bgcolor.getRed(), bgcolor
						.getGreen(), bgcolor.getBlue(), Integer.parseInt(root
						.attributeValue("bgAlpha"))));
			else
				chart.setBackgroundPaint(bgcolor);
		}
		//此方法设置背景透明不生效
		//if(!"".equals(root.attributeValue("bgAlpha")))
		//	chart.setBackgroundImageAlpha(Float.parseFloat(root.attributeValue("bgAlpha"))/100);
		
		//布景的背景色，网格线色
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(new Color(Integer.parseInt(root.attributeValue("canvasBgColor"),16)));
		plot.setRangeGridlinePaint(new Color(Integer.parseInt(
				null == root.attributeValue("canvasBaseColor")
				|| "".equals(root.attributeValue("canvasBaseColor")) ? "000000"
				: root.attributeValue("canvasBaseColor"), 16)));
		plot.setRangeZeroBaselineVisible(true);
		plot.setRangeZeroBaselinePaint(new Color(Integer.parseInt(
				null == root.attributeValue("zeroPlaneBorderColor")
				|| "".equals(root.attributeValue("zeroPlaneBorderColor")) ? "000000"
				: root.attributeValue("zeroPlaneBorderColor"),16)));
		//plot.setRangeZeroBaselineStroke(new BasicStroke(1.23f));
		
		//布景的地面和侧面的颜色
		BarRenderer3D renderer = (BarRenderer3D)plot.getRenderer();
		String canvasBaseColorStr = root.attributeValue("canvasBaseColor");
		if(null!=canvasBaseColorStr&&!"".equals(canvasBaseColorStr)){
			Color canvasBaseColor = new Color(Integer.parseInt(canvasBaseColorStr,16));
			String canvasBgAlphaStr = root.attributeValue("canvasBgAlpha");
			if(null!=canvasBgAlphaStr&&!"".equals(canvasBgAlphaStr)){
				((BarRenderer3D)renderer).setWallPaint(new Color(canvasBaseColor.getRed(), canvasBaseColor
						.getGreen(), canvasBaseColor.getBlue(),Integer.parseInt(canvasBgAlphaStr)));
			}else {
				((BarRenderer3D)renderer).setWallPaint(canvasBaseColor);
			}
		}

		//图柱上显示数值
		renderer.setBaseItemLabelsVisible("1".equals(root.attributeValue("shownames"))?true:false);
		StandardCategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator();
		if("1".equals(root.attributeValue("formatNumber"))){
			generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat(Format.getNumber(Integer
							.parseInt(root.attributeValue("decimalPrecision")))),
					NumberFormat.getInstance());
		}
		renderer.setBaseItemLabelGenerator(generator);
		renderer.setBaseItemLabelsVisible(true);
		ItemLabelPosition position = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER);
		renderer.setBasePositiveItemLabelPosition(position);
		renderer.setBaseNegativeItemLabelPosition(position);
		
		//图柱上显示数值的字体、颜色
		Font seriesfont = new Font(root.attributeValue("baseFont"),
				Font.PLAIN, Integer.parseInt(root.attributeValue("baseFontSize")));
		Color seriesfontcolor = new Color(Integer.parseInt(
				"".equals(root.attributeValue("baseFontColor")) ? "000000"
				: root.attributeValue("baseFontColor"),16));
		List<Element> serieslist =  root.selectNodes("dataset");
		int count = serieslist.size();
		for(int i =0;i<count;i++){
			renderer.setSeriesItemLabelFont(i, seriesfont);
			renderer.setSeriesItemLabelPaint(i, seriesfontcolor);
			renderer.setSeriesPaint(i, new Color(Integer.parseInt(serieslist.get(i).attributeValue("color"),16)));			
		}
		
		//图柱的透明度
		if(serieslist.get(0).attributeValue("alpha")!=null &&
				!"".equals(serieslist.get(0).attributeValue("alpha")))
			plot.setForegroundAlpha(Float.parseFloat(serieslist.get(0).attributeValue("alpha"))/100);
		else
			plot.setForegroundAlpha(1f);
		
		//分类的字体、颜色
		Element cateElem = root.element("categories");
		String font = cateElem.attributeValue("font")==null?"宋体":cateElem.attributeValue("font");
		String fontsize = cateElem.attributeValue("fontSize")==null?"12":cateElem.attributeValue("fontSize");
		Font catefont = new Font(font, Font.PLAIN, Integer.parseInt(fontsize));
		String catefontcolor = cateElem.attributeValue("fontColor")==null?"000000": cateElem.attributeValue("fontColor");
		Color catecolor = new Color(Integer.parseInt(catefontcolor, 16));

		CategoryAxis cateAxis = plot.getDomainAxis();
		cateAxis.setTickLabelFont(catefont);
		cateAxis.setTickLabelPaint(catecolor);
		//分类名称旋转
		if("1".equals(root.attributeValue("rotateNames")))
			cateAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		
		
		//面板字体
		Font basefont = new Font(root.attributeValue("outCnvBaseFont"),
				Font.PLAIN, Integer.parseInt(root.attributeValue("outCnvBaseFontSize")));
		Color basefontcolor = new Color(Integer.parseInt("".equals(root
				.attributeValue("outCnvBaseFontColor")) ? "000000" : root
				.attributeValue("outCnvBaseFontColor"), 16));
		
		cateAxis.setLabelFont(basefont);
		cateAxis.setLabelPaint(basefontcolor);
		ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setLabelFont(basefont) ;
		rangeAxis.setLabelPaint(basefontcolor);
		rangeAxis.setTickLabelFont(basefont);
		rangeAxis.setTickLabelPaint(basefontcolor);
		if(!"".equals(root.attributeValue("yaxismaxvalue"))&&null!=root.attributeValue("yaxismaxvalue"))
			rangeAxis.setUpperBound(Double.parseDouble(root.attributeValue("yaxismaxvalue")));
		if(!"".equals(root.attributeValue("yaxisminvalue"))&&null!=root.attributeValue("yaxisminvalue"))
			rangeAxis.setLowerBound(Double.parseDouble(root.attributeValue("yaxisminvalue")));		
		
		//图例
		chart.getLegend().setItemFont(basefont);
		chart.getLegend().setItemPaint(basefontcolor);
		
		//标题
		chart.getTitle().setFont(basefont);
		chart.getTitle().setPaint(basefontcolor);
		TextTitle subtitle = new TextTitle(root.attributeValue("subCaption"));
		subtitle.setFont(basefont);
		subtitle.setPaint(basefontcolor);
		chart.addSubtitle(subtitle);
		return chart;
	}
	
	public Dataset getDataset(Document data) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		List<Element> cates = data.getRootElement().selectNodes("categories/category");
		List<Element> serieslist = data.getRootElement().selectNodes("dataset");
		for(int i =0;i<cates.size();i++){
			String cateName = cates.get(i).attributeValue("name");
			for(Element series:serieslist){
				String seriesName = series.attributeValue("seriesname");
				List<Element> sets = series.selectNodes("set");
				if(sets!=null && i<sets.size()){
					String value = sets.get(i).attributeValue("value");
					dataset.addValue(Double.parseDouble(value), seriesName, cateName);
					//System.out.println(Double.parseDouble(value)+"--"+cateName+"--"+seriesName);
				}
			}
		}
		return dataset;
	}

}
