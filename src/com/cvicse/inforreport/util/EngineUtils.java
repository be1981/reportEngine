package com.cvicse.inforreport.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

//import com.cvicse.inforreport.classic.engine.EntityResolverImpl;
import com.cvicse.inforreport.model.Cell;
import com.cvicse.inforreport.model.Dataset;
import com.cvicse.inforreport.model.Row;


/**
 * 工具类，用于日志输出、编码转换等
 * @author qiao_lu1
 *
 */
public class EngineUtils {
	/**
	 * 输出到控制台的appender
	 */
	private static Log console;
	/**
	 * 输出到文件的appender
	 */
	private static Log logfile;
	/**
	 * lineSeparator
	 */
	public static final String lineSeparator = System.getProperty("line.separator");
	/**
	 * 国际化处理ResourceBundle
	 */
	//private static ResourceBundle resoueceBundle = ResourceBundle.getBundle(
	//		"com.cvicse.inforreport.resource.AppResource", Locale.getDefault());

	static {
		console = LogFactory.getLog(getResourceValue("Product")+".console");
		logfile = LogFactory.getLog(getResourceValue("Product")+".logfile");
	}
	/**
	 * 对http传输的字符串进行解码
	 * @param report Base64编码的字符串
	 * @return 解码后的字符串
	 */
	public static String decodeHttpBase64(String report) {
		if (!(report == null || report.trim().equals(""))) {
			report = report.replace(' ', '+');
			report = report.replace('`', '=');
			report = decodeBase64(report);
		}
		return report;
	}
	/**
	 * 对指定字符串进行Base64编码，字符串getBytes时的charset是GBK
	 * @param report 需要编码的字符串
	 * @return 经过Base64编码的字符串
	 */
	public static String encodeBase64(String report) {
		String encodedStr = null;
		try {
			encodedStr = new String(
					Base64.encodeBase64(report.getBytes("GBK")), "ISO-8859-1");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("", ex);
		}
		return encodedStr;
	}
	/**
	 * For checkSQL
	 * @param report
	 * @return
	 */
	public static String encodeBase64ForCheckSQL(String report) {
		String encodedStr = null;
		try {
			encodedStr = new String(
					Base64.encodeBase64(report.getBytes()), "ISO-8859-1");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("", ex);
		}
		return encodedStr;
	}
	/**
	 * 对指定字符串进行Base64编码，并指定字符串getBytes时的charset
	 * @param report 需要编码的字符串
	 * @param encoding 字符串getBytes时的charset
	 * @return 经过Base64编码的字符串
	 */
	public static String encodeBase64(String report, String encoding) {
		String encodedStr = null;
		try {
			encodedStr = new String(Base64.encodeBase64(report
					.getBytes(encoding)), "ISO-8859-1");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("", ex);
		}
		return encodedStr;
	}
	/**
	 * 对指定字符串进行Base64解码，构造字符串的charset是GBK
	 * @param report Base64编码的字符串
	 * @return 解码后的字符串
	 */
	public static String decodeBase64(String report) {
		byte[] reportBytes = null;
		String decodedStr = null;

		try {
			reportBytes = report.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("", ex);
		}

		if (Base64.isArrayByteBase64(reportBytes)) {
			try {
				decodedStr = new String(Base64.decodeBase64(reportBytes), "GBK");
			} catch (UnsupportedEncodingException ex1) {
				throw new RuntimeException("", ex1);
			}
			return decodedStr;
		} else {
			throw new IllegalArgumentException("IllegalBase64Char");
			//throw new IllegalArgumentException(Utils
			//		.getResourceValue("IllegalBase64Char"));
		}
	}
	/**
	 * 对指定字符串进行Base64解码，并指定构造字符串的charset
	 * @param report Base64编码的字符串
	 * @param encoding 构造字符串的charset
	 * @return 解码后的字符串
	 */
	public static String decodeBase64(String report, String encoding) {
		byte[] reportBytes = null;
		String decodedStr = null;

		try {
			reportBytes = report.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("", ex);
		}

		if (Base64.isArrayByteBase64(reportBytes)) {
			try {
				decodedStr = new String(Base64.decodeBase64(reportBytes),
						encoding);
			} catch (UnsupportedEncodingException ex1) {
				throw new RuntimeException("", ex1);
			}
			return decodedStr;
		} else {
			throw new IllegalArgumentException(Utils
					.getResourceValue("IllegalBase64Char"));
		}
	}
	/**
	 * 对byte数组进行Base64编码
	 * @param b byte数组
	 * @return Base64编码后的byte数组
	 */
	public static byte[] encodeBase64(byte[] b) {
		return Base64.encodeBase64(b);
	}
	/**
	 * 对byte数组进行Base64解码
	 * @param b byte数组
	 * @return Base64解码后的byte数组
	 */
	public static byte[] decodeBase64(byte[] b) {
		return Base64.decodeBase64(b);
	}
	/**
	 * 对指定字符串进行解压缩
	 * @param zipTemplate 需要解压缩的字符串
	 * @return 解压缩后的字符串
	 * @throws IOException
	 */
	public static String unZip(String zipTemplate) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(zipTemplate
				.getBytes());
		ZipInputStream in = new ZipInputStream(bais);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String template = null;
		byte[] buf = new byte[1024];
		int len = in.read(buf);

		while (len > 0) {
			out.write(buf, 0, len);
		}
		template = out.toString();

		if (out != null) {
			out.flush();
			out.close();
		}
		if (in != null) {
			in.close();
		}
		return template;
	}
	/**
	 * Translates a string into application/x-www-form-urlencoded format using UTF-8.
	 * @param url String to be translated
	 * @return the translated String
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeURL(String url)
			throws UnsupportedEncodingException {

		return URLEncoder.encode(url, "UTF-8");

	}
	/**
	 * 调用dom4j接口将Document对象转换为字符串
	 * @param dom Document to format
	 * @param indent is the indent string to be used for indentation (usually a number of spaces
	 * @param newlines whether new lines are added to layout
	 * @param encoding is the text encoding to use for writing the XML
	 * @return dom转换的字符串
	 * @throws IOException
	 */
	public static String convertToString(Document dom, String indent,
			boolean newlines, String encoding) {
		// XMLOutputter writer = new XMLOutputter();
		// ByteArrayOutputStream out = new ByteArrayOutputStream();
		// writer.output(dom, out);
		// return out.toString();
		OutputFormat format = new OutputFormat(indent, newlines, encoding);
		StringWriter sw = new StringWriter();
		XMLWriter writer = new XMLWriter(sw, format);
		try {
			writer.write(dom);
		} catch (IOException ex) {
			EngineUtils.fatal(ex.getMessage(),ex);
			throw new RuntimeException(ex.getMessage(), ex);
		} finally {
			try {
				sw.close();
				writer.close();
			} catch (IOException ex) {
				EngineUtils.fatal(ex.getMessage(),ex);
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
		return sw.toString();
	}
	/**
	 * 调用dom4j接口将xml格式字符串转换为Document对象
	 * @param str xml格式字符串
	 * @return Document对象
	 * @throws DocumentException
	 */
	public static Document convertToDocument(String str)
			throws DocumentException {
		StringReader reader = new StringReader(str);
		SAXReader builder = new SAXReader();
		builder.setEntityResolver(new EntityResolverImpl());
		Document dom = builder.read(reader);
		reader.close();
		return dom;
	}
	/**
	 * logger.info
	 * @param message 需要输出的日志信息，必须是资源文件中的key值
	 */
	public static void info(String message) {
		console.info(getResourceValue(message));
		logfile.info(getResourceValue(message));
	}
	/**
	 * logger.debug
	 * @param message 需要输出的日志信息的key值
	 */
	public static void debug(String message) {
		console.debug(getResourceValue(message));
		logfile.debug(getResourceValue(message));
	}
	/**
	 * logger.debug
	 * @param message 需要输出的日志信息的key值
	 * @param variable 需要输出的日志信息
	 */
	public static void debug(String message, String variable) {
		console.debug(getResourceValue(message) + variable);
		logfile.debug(getResourceValue(message) + variable);
	}
	/**
	 * logger.error
	 * @param message 需要输出的日志信息的key值
	 */
	public static void error(String message) {
		console.error(getResourceValue(message));
		logfile.error(getResourceValue(message));
	}
	/**
	 * logger.error
	 * @param message 需要输出的日志信息的key值
	 * @param variable 需要输出的日志信息
	 */
	public static void error(String message, String variable) {
		console.error(getResourceValue(message) + variable);
		logfile.error(getResourceValue(message) + variable);
	}
	/**
	 * logger.error
	 * @param message 需要输出的日志信息的key值
	 * @param exception 需要输出的异常
	 */
	public static void error(String message, Throwable exception) {
		if (message!=null&&!message.endsWith("."))
			message = getResourceValue(message) + ". ";
		console.error(message + getResourceValue("ViewLog"));
		logfile.error(message, exception);
	}
	/**
	 * logger.fatal
	 * @param message 需要输出的日志信息的key值
	 */
	public static void fatal(String message) {
		console.fatal(getResourceValue(message));
		logfile.fatal(getResourceValue(message));
	}
	/**
	 * logger.fatal
	 * @param message 需要输出的日志信息的key值
	 * @param variable 需要输出的日志信息
	 */
	public static void fatal(String message, String variable) {
		console.fatal(getResourceValue(message) + variable);
		logfile.fatal(getResourceValue(message) + variable);
	}
	/**
	 * logger.fatal
	 * @param message 需要输出的日志信息的key值
	 * @param exception 需要输出的异常
	 */
	public static void fatal(String message, Throwable exception) {
		if (message!=null&&!message.endsWith("."))
			message = getResourceValue(message) + ". ";
		console.fatal(message + getResourceValue("ViewLog"));
		logfile.fatal(message, exception);
	}
	/**
	 * logger.fatal
	 * @param message 需要输出的日志信息的key值
	 * @param variable 需要输出的日志信息
	 * @param exception 需要输出的异常
	 */
	public static void fatal(String message, String variable,
			Throwable exception) {
		if (!variable.endsWith("."))
			variable += ". ";
		console.fatal(getResourceValue(message) + variable
				+ getResourceValue("ViewLog"));
		logfile.fatal(getResourceValue(message) + variable, exception);
	}
	/**
	 * Gets a string for the given key from this resource bundle or one of its parents
	 * @param resource the key for the desired string
	 * @return the string for the given key
	 */
	public static String getResourceValue(String resource) {
		String value = resource;
		try {
			//value = resoueceBundle.getString(resource);
		} catch (Exception ex) {
			value = resource;
		}
		return value;
	}
	
	/**
	 * 调用dom4j接口将Document对象保存为文件
	 * @param doc
	 * @param encoding
	 * @param file
	 */
	public static void Doc2XmlFile(Document doc, String encoding, String file) {
		try {			
			FileOutputStream fos = new FileOutputStream(new File(file));
			OutputFormat format = new OutputFormat("", false, encoding);
			XMLWriter writer = new XMLWriter(fos, format);
			writer.write(doc);
			if (fos != null)
				fos.close();
			writer.close();
		}catch (IOException e) {
			//	Utils.error("OutputReportForDebugError" ,": "+ "report.xml");
		}
	}
		
	// 2009.03.25
	/**
	 * 从类似$F{1.name}里面分离出1.name
	 *
	 */
	public static String getField(String value) {
		return value.substring(value.indexOf("{")+1,value.indexOf("}"));
	}
	
	/**
	 * 获取来自结果集中的数据
	 * @param dsfield
	 * @param datas
	 * @return
	 */
	public static String[] getDatasetValue(String dsfield, Dataset dataset, Map datas) {
		if(dsfield==null||"".equals(dsfield))
			return null;		

		int index = 0;		
		index = dataset.getIndex(dsfield);
		
		String dataId = dsfield.substring(0,dsfield.indexOf("."));
		// field = dsfield.substring(dsfield.indexOf(".")+1);
		List data = (List)datas.get(dataId);
		String[] values = new String[data.size()];
		
		for(int i=0;i<data.size();i++){
			List cells = ((Row)data.get(i)).getCells();
			values[i]=((Cell)cells.get(index)).getCellContent();			
		}		
		return values;
	}
}
