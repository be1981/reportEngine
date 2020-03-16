/**
 * 
 */
package com.cvicse.inforreport.exporter;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author li_zhi
 *
 */
public class PdfHeaderFooterEvent extends PdfPageEventHelper {
	/**
	 * 页眉
	 */
	private String header = "";

	/**
	 * 页脚
	 */
	private String footer = "";

	/**
	 * 页眉对齐方式
	 */
	private int headerAlignment = Element.ALIGN_LEFT;

	/**
	 * 页脚对齐方式
	 */
	private int footerAlignment = Element.ALIGN_LEFT;

	/**
	 * 页眉边距
	 */
	private float marginHeader = 0.0f;

	/**
	 * 页脚边距
	 */
	private float marginFooter = 0.0f;

	/**
	 * 文档页面大小，最好前面传入，否则默认为A4纸张
	 */
	private Rectangle pageSize = PageSize.A4;

	/**
	 * 模板
	 */
	private PdfTemplate total;

	/**
	 * 页眉字体对象
	 */
	private Font headerFont = null;

	/**
	 * 页脚字体对象
	 */
	private Font footerFont = null;

	/**
	 * 构造函数
	 * 
	 * @param pageSize
	 *            文档格式
	 * @param headerFont
	 *            页眉字体对象
	 * @param footerFont
	 *            页脚字体对象
	 */
	public PdfHeaderFooterEvent(Rectangle pageSize, Font headerFont, Font footerFont) {
		this.pageSize = pageSize;
		this.headerFont = headerFont;
		this.footerFont = footerFont;
	}

	/**
	 * 构造方法
	 * 
	 * @param header
	 *            页眉字符串
	 * @param footer
	 *            页脚字符串
	 * @param headerAlignment
	 *            页眉对齐方式
	 * @param footerAlignment
	 *            页脚对齐方式
	 * @param marginHeader
	 *            页眉边距
	 * @param marginFooter
	 *            页脚边距
	 * @param pageSize
	 *            页面文档大小，A4，A5，A6横转翻转等Rectangle对象
	 * @param headerFont
	 *            页眉字体对象
	 * @param footerFont
	 *            页脚字体对象
	 */
	public PdfHeaderFooterEvent(String header, String footer, int headerAlignment, int footerAlignment,
			float marginHeader, float marginFooter, Rectangle pageSize, Font headerFont, Font footerFont) {
		this.header = header;
		this.footer = footer;
		this.headerAlignment = headerAlignment;
		this.footerAlignment = footerAlignment;
		this.marginHeader = marginHeader;
		this.marginFooter = marginFooter;
		this.pageSize = pageSize;
		this.headerFont = headerFont;
		this.footerFont = footerFont;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public void setHeaderAlignment(int headerAlignment) {
		this.headerAlignment = headerAlignment;
	}

	public void setFooterAlignment(int footerAlignment) {
		this.footerAlignment = footerAlignment;
	}

	public void setMarginHeader(float marginHeader) {
		this.marginHeader = marginHeader;
	}

	public void setMarginFooter(float marginFooter) {
		this.marginFooter = marginFooter;
	}

	/**
	 *
	 * 文档打开时创建模板
	 *
	 */
	public void onOpenDocument(PdfWriter writer, Document document) {
		int index = footer.indexOf("$V{N}");
		if (index > 0) {
			BaseFont footerBF = footerFont.getBaseFont();
			float footerSize = footerFont.getSize();
			float len0 = footerBF.getWidthPoint("20", footerSize);
			total = writer.getDirectContent().createTemplate(len0 + 10.0f, footerSize * 2.0f);// 共 页 的矩形的长宽高
		}
	}

	/**
	 * 关闭每页的时候，写入页眉页脚
	 */
	public void onEndPage(PdfWriter writer, Document document) {
		// 1.写入页眉
		int pageS = writer.getPageNumber();
		String realHeader = convertPageParams(pageS, header);
		// System.out.println("页眉为:" + realHeader);
		BaseFont headerBF = headerFont.getBaseFont();
		float headerSize = headerFont.getSize();
		float headerLen = headerBF.getWidthPoint(realHeader, headerSize);
		float headerX = getXPosition(headerAlignment, document, headerLen);
		ColumnText.showTextAligned(writer.getDirectContent(), headerAlignment, new Phrase(realHeader, headerFont),
				headerX, document.top() + marginHeader, 0);

		// 2.拿到当前的PdfContentByte
		PdfContentByte cb = writer.getDirectContent();

		// 3.写入页脚
		BaseFont footerBF = footerFont.getBaseFont();
		float footerSize = footerFont.getSize();
		String realFooter = convertPageParams(pageS, footer);
		float footerLen = footerBF.getWidthPoint(realFooter, footerSize);
		float footerX = getXPosition(footerAlignment, document, footerLen);
		ColumnText.showTextAligned(cb, footerAlignment, new Phrase(realFooter, footerFont), footerX,
				document.bottom() - marginFooter, 0);

		// 4.写入页脚总页数
		if (total != null) {
			int index = footer.indexOf("$V{N}");
			float len = footerBF.getWidthPoint(realFooter, footerSize);
			float lenf = 0.0f;
			float lena = 0.0f;
			if (index > 0) {
				String front = footer.substring(0, index);
				String realFront = convertPageParams(pageS, front);
				lenf = footerBF.getWidthPoint(realFront, footerSize);
				String after = footer.substring(index + 5, footer.length());
				String realAfter = convertPageParams(pageS, after);
				lena = footerBF.getWidthPoint(realAfter, footerSize);
			}
			float footerPagePos = getPageTotalPosition(footerAlignment, footerX, len, lenf, lena);
			cb.addTemplate(total, footerPagePos, document.bottom() - marginFooter);
		}
	}

	/**
	 *
	 * 关闭文档时，替换模板，完成整个页眉页脚组件
	 *
	 */
	public void onCloseDocument(PdfWriter writer, Document document) {
		if (total != null) {
			// 7.最后一步了，就是关闭文档的时候，将模板替换成实际的 Y 值,至此，page x of y 制作完毕，完美兼容各种文档size。
			total.beginText();
			BaseFont footerBF = footerFont.getBaseFont();
			float footerSize = footerFont.getSize();
			total.setFontAndSize(footerBF, footerSize);// 生成的模版的字体、颜色
			String foot2 = "" + (writer.getPageNumber() - 1);
			// String foot2 = "";
			total.showText(foot2);// 模版显示的内容
			total.endText();
			total.closePath();
		}
	}

	/**
	 * 转换参数值
	 * 
	 * @param pageS
	 * @param src
	 * @return
	 */
	private String convertPageParams(int pageS, String src) {
		String result = src.replace("$V{P}", String.valueOf(pageS));
		result = result.replace("$V{N}", "  ");

		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		String curDate = sdfDate.format(new Date());
		result = result.replace("$V{D}", String.valueOf(curDate));

		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
		String curTime = sdfTime.format(new Date());
		result = result.replace("$V{T}", String.valueOf(curTime));

		result = result.replace("\"", "");
		result = result.replace("&", "");

		return result;
	}

	private float getXPosition(int alignment, Document document, float len) {
		// 左对齐取最左侧坐标点
		float result = document.left() + document.leftMargin();
		if (alignment == Element.ALIGN_CENTER) {
			// 居中对齐取中线坐标点
			result = document.left() + document.leftMargin() + (document.right() - document.left()) / 2.0F;
		} else if (alignment == Element.ALIGN_RIGHT) {
			// 左对齐取最右侧坐标点
			result = document.right() - document.rightMargin();
		}

		return result;
	}

	private float getPageTotalPosition(int alignment, float xposition, float len, float lenf, float lena) {
		BaseFont footerBF = footerFont.getBaseFont();
		float footerSize = footerFont.getSize();
		float len0 = footerBF.getWidthPoint("20", footerSize);
		
		// 左对齐取最左侧坐标点
		float result = xposition + lenf;
		if (alignment == Element.ALIGN_CENTER) {
			// 居中对齐取中线坐标点
			result = xposition - len / 2.0F + lenf;
		} else if (alignment == Element.ALIGN_RIGHT) {
			// 左对齐取最右侧坐标点
			result = xposition - lena - len0;
		}

		return result;
	}
}
