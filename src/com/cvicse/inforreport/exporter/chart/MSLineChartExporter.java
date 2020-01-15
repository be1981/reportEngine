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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.ui.TextAnchor;

public class MSLineChartExporter implements IChartExporter {

	String type ="2D";

	public JFreeChart createChart(Document data) {
		Element root = data.getRootElement();
		JFreeChart chart = null;
		if("2D".equals(type)){
		chart = ChartFactory.createLineChart(
				root.attributeValue("caption"), 
				root.attributeValue("xAxisName"), 
				root.attributeValue("yAxisName"), 
				(CategoryDataset)getDataset(data), 
				PlotOrientation.VERTICAL, 
				root.attributeValue("showLegend").equals("1")?true:false, 
				true, 
				false);
		}else if("3D".equals(type)){
			chart = ChartFactory.createLineChart3D(
					root.attributeValue("caption"), 
					root.attributeValue("xAxisName"), 
					root.attributeValue("yAxisName"), 
					(CategoryDataset)getDataset(data), 
					PlotOrientation.VERTICAL, 
					root.attributeValue("showLegend").equals("1")?true:false, 
					true, 
					false);
		}
		
		//背景色
		if(!"".equals(root.attributeValue("bgColor"))){
			Color bgcolor = new Color(Integer.parseInt(root.attributeValue("bgColor"), 16));
			if(!"".equals(root.attributeValue("bgAlpha")))
				chart.setBackgroundPaint(new Color(bgcolor.getRed(), bgcolor
						.getGreen(), bgcolor.getBlue(), Float.parseFloat(root
						.attributeValue("bgAlpha")) / 100));
			else
				chart.setBackgroundPaint(bgcolor);
		}
		//此方法设置背景透明不生效
		//if(!"".equals(root.attributeValue("bgAlpha")))
		//	chart.setBackgroundImageAlpha(Float.parseFloat(root.attributeValue("bgAlpha"))/100);
		
		//布景的背景色，网格线色
		CategoryPlot plot = (CategoryPlot) chart.getPlot();		
		if(!"".equals(root.attributeValue("canvasBgColor")))
			plot.setBackgroundPaint(new Color(Integer.parseInt(root.attributeValue("canvasBgColor"),16)));
		if(!"".equals(root.attributeValue("canvasBgAlpha")))
			plot.setBackgroundAlpha(Float.parseFloat(root.attributeValue("canvasBgAlpha"))/100);
		if(!"".equals(root.attributeValue("divlinecolor")))
			plot.setRangeGridlinePaint(new Color(Integer.parseInt(root.attributeValue("divlinecolor"),16)));
		//plot.setRangeGridlinePaint(new Color(Integer.parseInt("C0C0C0",16)));
		
		
		//折线上显示数值
		LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
		renderer.setBaseItemLabelsVisible("1".equals(root.attributeValue("showValues"))?true:false);
		StandardCategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator();
		if("1".equals(root.attributeValue("formatNumber"))){
			generator = new StandardCategoryItemLabelGenerator(
					"{2}", new DecimalFormat(root.attributeValue("numberPrefix")
							+Format.getNumber(Integer.parseInt(root.attributeValue("decimalPrecision")))
							+ root.attributeValue("numberSuffix")),
					NumberFormat.getInstance());
		}
		renderer.setBaseItemLabelGenerator(generator);
		ItemLabelPosition position = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER);
		renderer.setBasePositiveItemLabelPosition(position);
		renderer.setBaseNegativeItemLabelPosition(position);
		
		//折线上显示数值的字体、颜色
		Font seriesfont = new Font(root.attributeValue("baseFont"),
				Font.PLAIN, Integer.parseInt(root.attributeValue("baseFontSize")));
		Color seriesfontcolor = new Color(Integer.parseInt(
				"".equals(root.attributeValue("baseFontColor"))?"000000":root.attributeValue("baseFontColor"),16));
		String anchorSides = root.attributeValue("anchorSides");
		List<Element> serieslist =  root.selectNodes("dataset");
		int count = serieslist.size();
		for(int i =0;i<count;i++){
			renderer.setSeriesItemLabelFont(i, seriesfont);
			renderer.setSeriesItemLabelPaint(i, seriesfontcolor);
			renderer.setSeriesPaint(i, new Color(Integer.parseInt(serieslist.get(i).attributeValue("color"),16)));			

		}
		
		//折线的透明度
		if(serieslist.get(0).attributeValue("lineAlpha")!=null)
			plot.setForegroundAlpha(Float.parseFloat(serieslist.get(0).attributeValue("lineAlpha"))/100);
		
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
		if(!"".equals(root.attributeValue("yAxisMaxValue")))
			rangeAxis.setUpperBound(Double.parseDouble(root.attributeValue("yAxisMaxValue")));
		if(!"".equals(root.attributeValue("yAxisMinValue")))
			rangeAxis.setLowerBound(Double.parseDouble(root.attributeValue("yAxisMinValue")));		
		
		//图例
		if (chart.getLegend() != null) {
			chart.getLegend().setItemFont(basefont);
			chart.getLegend().setItemPaint(basefontcolor);
		}
		
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
