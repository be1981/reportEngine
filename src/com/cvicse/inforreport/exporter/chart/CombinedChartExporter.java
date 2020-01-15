package com.cvicse.inforreport.exporter.chart;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.CombinedRangeCategoryPlot;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import com.cvicse.inforreport.exporter.chart.renderer.CylinderRenderer;

public class CombinedChartExporter implements IChartExporter {
	
	public JFreeChart createChart(Document data) {
		Element root = data.getRootElement();
		CategoryDataset categorydataset = (CategoryDataset)getDataset(data);
		
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
		Color cnvfontcolor = new Color(Integer.parseInt(root.attributeValue("outCnvBaseFontColor"),16));
		
		//标题字体
		Font titlefont = new Font(root.attributeValue("outCnvBaseFont"),
				"1".equals(root.attributeValue("outCnvBaseFontIsBold"))?Font.BOLD:Font.PLAIN, 
						Integer.parseInt(root.attributeValue("captionFontSize")));
		
		
        NumberAxis numberaxis = new NumberAxis(root.attributeValue("yAxisName"));
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setLabelFont(basefont);
        numberaxis.setLabelPaint(basefontcolor);
        numberaxis.setTickLabelFont(basefont);
        numberaxis.setTickLabelPaint(basefontcolor);
		if(!"".equals(root.attributeValue("yAxisMaxValue")))
			numberaxis.setUpperBound(Double.parseDouble(root.attributeValue("yAxisMaxValue")));
		if(!"".equals(root.attributeValue("yAxisMinValue")))
			numberaxis.setLowerBound(Double.parseDouble(root.attributeValue("yAxisMinValue")));
        
        CategoryAxis cateAxis = new CategoryAxis(root.attributeValue("xAxisName"));
		cateAxis.setTickLabelFont(cnvfont);
		cateAxis.setTickLabelPaint(cnvfontcolor);
		cateAxis.setLabelFont(basefont);
		cateAxis.setLabelPaint(basefontcolor);
        
        List<Element> datasets = root.selectNodes("dataset");
        int count = datasets.size();
        CategoryPlot categoryplot = null;      
        CombinedDomainCategoryPlot combineddomaincategoryplot = new CombinedDomainCategoryPlot(cateAxis);
        for(int i =0;i<count;i++){
        	Element dataset = datasets.get(i);
        	String renderas = dataset.attributeValue("renderAs");
        	AbstractCategoryItemRenderer renderer = null;
        	if("Column".equals(renderas)){
        		renderer = new BarRenderer();
        	}else if("Area".equals(renderas)){
        		renderer = new AreaRenderer();
        	}else if("Cylinder".equals(renderas)){
        		renderer = new CylinderRenderer();
        	}else if("Line".equals(renderas)){
        		renderer = new LineAndShapeRenderer();
        	}

//			renderer.setSeriesItemLabelFont(i, basefont);
//			renderer.setSeriesItemLabelPaint(i, basefontcolor);
//			renderer.setSeriesPaint(i, new Color(Integer.parseInt(datasets.get(i).attributeValue("color"),16)));	
 
			StandardCategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator();
    		if("1".equals(root.attributeValue("formatNumber"))){
    			generator = new StandardCategoryItemLabelGenerator(
    					"{2}", 
    					new DecimalFormat(root.attributeValue("numberPrefix")
    							+Format.getNumber(Integer.parseInt(root.attributeValue("decimalPrecision")))
    							+ root.attributeValue("numberSuffix")),
    					NumberFormat.getInstance());
    		}
    		renderer.setBaseItemLabelGenerator(generator);
    		categoryplot = new CategoryPlot(categorydataset, null, numberaxis, renderer);
    		combineddomaincategoryplot.add(categoryplot, 1);
        }
        
        JFreeChart jfreechart = new JFreeChart(
        		root.attributeValue("caption"),
        		titlefont, 
        		combineddomaincategoryplot, 
        		true);
        
//        LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
//        lineandshaperenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
//        CategoryPlot categoryplot = new CategoryPlot(categorydataset, null, numberaxis, lineandshaperenderer);
//        categoryplot.setDomainGridlinesVisible(true);
//        //CategoryDataset categorydataset1 = createDataset2();
//        NumberAxis numberaxis1 = new NumberAxis("Value");
//        numberaxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        BarRenderer barrenderer = new BarRenderer();
//        barrenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
//        CategoryPlot categoryplot1 = new CategoryPlot(categorydataset, null, numberaxis1, barrenderer);
//        categoryplot1.setDomainGridlinesVisible(true);
//        CategoryAxis categoryaxis = new CategoryAxis("Category");
//        CombinedDomainCategoryPlot combineddomaincategoryplot = new CombinedDomainCategoryPlot(categoryaxis);
//        combineddomaincategoryplot.add(categoryplot, 2);
//        combineddomaincategoryplot.add(categoryplot1, 1);
//        JFreeChart jfreechart = new JFreeChart("Combined Domain Category Plot Demo",new Font("SansSerif", 1, 12), combineddomaincategoryplot, true);
        return jfreechart;
	}
	
	public JFreeChart createChart2(Document data) {
		CategoryDataset categorydataset = (CategoryDataset)getDataset(data);
        NumberAxis numberaxis = new NumberAxis("Value");
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
        lineandshaperenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        CategoryPlot categoryplot = new CategoryPlot(categorydataset, null, numberaxis, lineandshaperenderer);
        categoryplot.setDomainGridlinesVisible(true);
        //CategoryDataset categorydataset1 = createDataset2();
        NumberAxis numberaxis1 = new NumberAxis("Value");
        numberaxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        BarRenderer barrenderer = new BarRenderer();
        barrenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        CategoryPlot categoryplot1 = new CategoryPlot(categorydataset, null, numberaxis, barrenderer);
        categoryplot1.setDomainGridlinesVisible(true);
        CategoryAxis categoryaxis = new CategoryAxis("Category");
        CombinedDomainCategoryPlot combineddomaincategoryplot = new CombinedDomainCategoryPlot(categoryaxis);
        combineddomaincategoryplot.add(categoryplot, 2);
        combineddomaincategoryplot.add(categoryplot1, 1);
        combineddomaincategoryplot.add(categoryplot1, 3);
        JFreeChart jfreechart = new JFreeChart("Combined Domain Category Plot Demo", new Font("SansSerif", 1, 12), combineddomaincategoryplot, true);
        return jfreechart;
	}

	public JFreeChart createChart1(Document data) {
		CategoryDataset categorydataset = (CategoryDataset)getDataset(data);
        CategoryAxis categoryaxis = new CategoryAxis("Class 1");
        categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        categoryaxis.setMaximumCategoryLabelWidthRatio(5F);
        LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
        lineandshaperenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        CategoryPlot categoryplot = new CategoryPlot(categorydataset, categoryaxis, null, lineandshaperenderer);
        categoryplot.setDomainGridlinesVisible(true);
        //CategoryDataset categorydataset1 = createDataset2();
        CategoryAxis categoryaxis1 = new CategoryAxis("Class 2");
        categoryaxis1.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        categoryaxis1.setMaximumCategoryLabelWidthRatio(5F);
        BarRenderer3D barrenderer = new BarRenderer3D();
        barrenderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        CategoryPlot categoryplot1 = new CategoryPlot(categorydataset, categoryaxis1, null, barrenderer);
        categoryplot1.setDomainGridlinesVisible(true);
        NumberAxis numberaxis = new NumberAxis("Value");
        CombinedRangeCategoryPlot combinedrangecategoryplot = new CombinedRangeCategoryPlot(numberaxis);
        combinedrangecategoryplot.setRangePannable(true);
        combinedrangecategoryplot.add(categoryplot, 3);
        combinedrangecategoryplot.add(categoryplot1, 2);
       // combinedrangecategoryplot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart jfreechart = new JFreeChart(
        		"Combined Range Category Plot Demo", 
        		new Font("SansSerif", 1, 12), 
        		combinedrangecategoryplot, 
        		true);
        return jfreechart;
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
