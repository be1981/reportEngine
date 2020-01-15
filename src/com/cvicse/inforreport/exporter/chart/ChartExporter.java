package com.cvicse.inforreport.exporter.chart;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.cvicse.inforreport.model.DynamicChart;

public class ChartExporter{
	
	public ChartExporter(){		
		
	}	

	public static void exportChart(DynamicChart dchart, File file) {

		try {
			SAXReader reader = new SAXReader();
			Document dom = reader.read(new StringReader(dchart.getDataxml()));
			JFreeChart chart = getExporter(dchart.getChartStyle().getId()).createChart(dom);
			OutputStream output = new FileOutputStream(file);
			ChartUtilities.writeChartAsJPEG(output, 1.0f, chart, dchart
					.getWidthPx(), dchart.getHeightPx(), null);
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void exportChart(DynamicChart dchart,OutputStream output){
		
		try {
			SAXReader reader = new SAXReader();
			Document dom = reader.read(new StringReader(dchart.getDataxml()));
			JFreeChart chart = getExporter(dchart.getChartStyle().getId()).createChart(dom);		
  
			//ChartUtilities.writeChartAsJPEG(output,1.0f,chart,dchart.getWidthPx(),dchart.getHeightPx(),null);    
			//ChartUtilities.writeChartAsPNG(new FileOutputStream(new File("h:/test.PNG")),chart,dchart.getWidthPx(),dchart.getHeightPx(),null);
			ChartUtilities.writeChartAsPNG(output,chart,dchart.getWidthPx(),dchart.getHeightPx(),null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void exportChart(String chartId,String data,int width,int height,OutputStream output){
		File file = new File("e:\\testchart.jpg");
		
		try {
			SAXReader reader = new SAXReader();
			Document dom = reader.read(new StringReader(data));
			JFreeChart chart = getExporter(chartId).createChart(dom);		
  
			ChartUtilities.writeChartAsJPEG(output,1.0f,chart,width,height,null);    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static IChartExporter getExporter(String chartId){
		IChartExporter exporter = null;
		if(chartId.equals("PSPie")|| chartId.equals("PSDoughnut"))
			exporter = new PieChartExporter();
		else if(chartId.equals("MSColumn3D"))
			exporter = new MSColumnChartExporter();
		else if(chartId.equals("MSLine2D"))
			exporter = new MSLineChartExporter();
//		else if(chartId.equals("MSRadar"))
//			exporter = new RadarChartExporter();
		else if(chartId.equals("PSCombo"))
			exporter = new CombinedChartExporter();
		else if(chartId.equals("PSArea"))
			exporter = new AreaChartExporter();
		else if(chartId.equals("PSCylinder"))
			exporter = new CylinderChartExporter();
		else if(chartId.equals("PSColumn"))
			exporter = new MSColumnChartExporter();
		else if(chartId.equals("PSLine")){
			exporter = new MSLineChartExporter();
			((MSLineChartExporter)exporter).setType("3D");
		}
		else if (chartId.equals("MSFI2Angular"))
			exporter = new AngularChartExporter();
		else if (chartId.equals("MSFI2Thermometer"))
			exporter = new ThermoChartExporter();
		return exporter;
	}
	
	public static void main(String[] args) throws Exception{
		String data = "<graph caption='caption' subCaption='subcaption' bgColor='FFFFFF,BBDFFF,FFFFFF' bgRatio='0,35,65' bgFillType='radial' bgAlpha='' bgDegree='' bgSWF='' fgSWF='' sndMP3='' showNames='1' showValues='1' showPercentageValues='0' showPosition='0' nameTbDistance='15' slicingDistance='' radius='150' innerRadius='' brightnessFill='' brightnessLine='' showLegend='1' showLegendValues='1' showLegendName='1' showLegendPercentage='1' showLegendPosition='0' showLegendColumn='1' legendMax='5' legendPos='3' legendAlign='2' legendNameAlign='2' legendHorPadding='5' legendValuesHDText='值' legendNameHDText='名称' legendPositionHDText='序号' legendPercentageHDText='百分比' legendSoDatasetsHDText='of' legendSumOfValuesHDText='总计' animationWall='1' fadeIn='1' fadeInFromBgColor='' startMode2D3D='3' initialXgrow='1' wobble='' playMode='forward' wallThickness='' startRotationAngle='' endRotationAngle='' cameraY='' bevel='8' baseFont='宋体' baseFontSize='12' baseFontColor='' baseFontIsBold='1' outCnvBaseFont='宋体' outCnvBaseFontSize='12' outCnvBaseFontColor='333333' outCnvBaseFontIsBold='1' captionFontSize='14' formatNumber='0' numberPrefix='' numberSuffix='' formatNumberScale='0' decimalSeparator='.' thousandSeparator=',' decimalPrecision='2' percentageDecimalPrecision='2' cornerMask='0' maskBevel='30' maskCornerRadius='70' maskColor='bbddff' maskAlpha='' maskInnerColor='' maskInnerAlpha='' navIntro='0' nav3D2D='1' navJog='0' navRefresh='0' navPrint='0' navColor='' btnValuesText='显示值,隐藏值' btnIntroText='重播' btn3D2DText='3D, 2D' btnRefreshText='刷新' btnPrintText='打印' chartLeftMargin='' chartRightMargin='' chartTopMargin='' chartBottomMargin='' exportEnabled='1' exportAtClient='0' exportAction='save' exportHandler='http://127.0.0.1:8080/ReportService/FCExporter.jsp'><set name='赵纯勇' value='92' color='AFD8F8' alpha='50' isSliced='1' link=''/><set name='李文宁' value='86' color='F6BD0F' alpha='50' isSliced='1' link=''/><set name='钱芬' value='89' color='8BBA00' alpha='50' isSliced='' link=''/><set name='赵晓慧' value='76' color='F984A1' alpha='80' isSliced='' link=''/><set name='吴顺娴' value='93' color='9999CC' alpha='80' isSliced='' link=''/><set name='周单' value='78' color='00cc00' alpha='80' isSliced='' link=''/><set name='王静严' value='83' color='aa00aa' alpha='50' isSliced='' link=''/></graph>";
			
		FileOutputStream fos = new FileOutputStream(new File("e:\\testchart.jpg"));
		exportChart("PSPie",data,640,480,fos);
		fos.close();
	}

}
