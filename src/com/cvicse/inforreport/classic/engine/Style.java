package com.cvicse.inforreport.classic.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;

/**
 * 维护静态样式的数据，提供取得各种静态样式元素的方法
 * @author qiao_lu1
 * @version 1.00, 2005/11/18
 * @since 1.00
 */
public class Style implements Cloneable {

	private static final Log log = LogFactory.getLog(Style.class);


    private String foreColor; // 前景色
    private String backColor; // 背景色
    private String borderColor; // 边框颜色
    private String hAlign; // 水平对齐方式
    private String vAlign; // 垂直对齐方式
    private String fontName; // 字体名字
    private String fontSize; // 字体大小
    private String isBold; // 字体是否加粗
    private String isItalic; // 字体是否是斜体
    private String isUnderline; // 字体是否有下划线
    private String isStrikeThrough; // 字体是否有删除线
    private String borderWeight; // 边框宽度
    private String autoWrap; // 自动换行
    private String readOnly;  //是否只读
    private String attr;  //属性，1：可写，0：只读，-1：不可见
    private String popMenu; //是否可以弹出右键菜单，0，不可；1，可以

    /**
     * 获得Style对象
     * @param styleNode style节点对象
     */
    public void init(Node styleNode) {
        foreColor = styleNode.valueOf("@foreColor");
        backColor = styleNode.valueOf("@backColor");
        hAlign = styleNode.valueOf("@hAlign");
        vAlign = styleNode.valueOf("@vAlign");
        autoWrap = styleNode.valueOf("@autoWrap");
        String readOnly = styleNode.valueOf("@readOnly");
        if(readOnly.equals("")||readOnly.equals("false"))
        	attr = "0";
        else if(readOnly.equals("true"))
        	attr = "1";

        Node font = styleNode.selectSingleNode("font");
        if (font != null) {
            fontName = font.valueOf("@name");
            fontSize = font.valueOf("@size");
            isBold = font.valueOf("@isBold");
            isItalic = font.valueOf("@isItalic");
            isUnderline = font.valueOf("@isUnderline");
            isStrikeThrough = font.valueOf("@isStrikeThrough");
        }

        Node borders = styleNode.selectSingleNode("borders");
        if (borders != null) {
            borderColor = borders.valueOf("@borderColor");
            Node border = borders.selectSingleNode("border");
            if (border != null) {
                borderWeight = border.valueOf("@weight");
            }
        }
    }

    public Object clone() {
        Style style = null;
        try {
            style = (Style) super.clone();
        }
        catch (CloneNotSupportedException e) {
            // e.printStackTrace();
//            Utils.error(Style.class + " not support exception");
            log.error(Style.class + " not support exception");
        }

        return style;
    }

    /**
     * @return Returns the backColor.
     */
    public String getBackColor() {
        return backColor;
    }

    /**
     * @param backColor The backColor to set.
     */
    public void setBackColor(String backColor) {
        this.backColor = backColor;
    }

    /**
     * @return Returns the borderColor.
     */
    public String getBorderColor() {
        return borderColor;
    }

    /**
     * @param borderColor The borderColor to set.
     */
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @return Returns the borderWeight.
     */
    public String getBorderWeight() {
        return borderWeight;
    }

    /**
     * @param borderWeight The borderWeight to set.
     */
    public void setBorderWeight(String borderWeight) {
        this.borderWeight = borderWeight;
    }

    /**
     * @return Returns the fontName.
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * @param fontName The fontName to set.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /**
     * @return Returns the fontSize.
     */
    public String getFontSize() {
        return fontSize;
    }

    /**
     * @param fontSize The fontSize to set.
     */
    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * @return Returns the foreColor.
     */
    public String getForeColor() {
        return foreColor;
    }

    /**
     * @param foreColor The foreColor to set.
     */
    public void setForeColor(String foreColor) {
        this.foreColor = foreColor;
    }

    /**
     * @return Returns the hAlign.
     */
    public String getHAlign() {
        return hAlign;
    }

    /**
     * @param align The hAlign to set.
     */
    public void setHAlign(String align) {
        hAlign = align;
    }

    /**
     * @return Returns the isBold.
     */
    public String getIsBold() {
        return isBold;
    }

    /**
     * @param isBold The isBold to set.
     */
    public void setIsBold(String isBold) {
        this.isBold = isBold;
    }

    /**
     * @return Returns the isItalic.
     */
    public String getIsItalic() {
        return isItalic;
    }

    /**
     * @param isItalic The isItalic to set.
     */
    public void setIsItalic(String isItalic) {
        this.isItalic = isItalic;
    }

    /**
     * @return Returns the isStrikeThrough.
     */
    public String getIsStrikeThrough() {
        return isStrikeThrough;
    }

    /**
     * @param isStrikeThrough The isStrikeThrough to set.
     */
    public void setIsStrikeThrough(String isStrikeThrough) {
        this.isStrikeThrough = isStrikeThrough;
    }

    /**
     * @return Returns the isUnderline.
     */
    public String getIsUnderline() {
        return isUnderline;
    }

    /**
     * @param isUnderline The isUnderline to set.
     */
    public void setIsUnderline(String isUnderline) {
        this.isUnderline = isUnderline;
    }


    /**
     * @return Returns the vAlign.
     */
    public String getVAlign() {
        return vAlign;
    }

    /**
     * @param align The vAlign to set.
     */
    public void setVAlign(String align) {
        vAlign = align;
    }

    /**
     * @return Returns the autoWrap.
     */
    public String getAutoWrap() {
        return autoWrap;
    }

    /**
     * @param autoWrap The autoWrap to set.
     */
    public void setAutoWrap(String autoWrap) {
        this.autoWrap = autoWrap;
    }

	/**
	 * @return Returns the attr.
	 */
	public String getAttr() {
		return attr;
	}

	/**
	 * @param attr The attr to set.
	 */
	public void setAttr(String attr) {
		this.attr = attr;
	}

	/**
	 * @return Returns the readOnly.
	 */
	public String getReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly The readOnly to set.
	 */
	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @return Returns the popMenu.
	 */
	public String getPopMenu() {
		return popMenu;
	}

	/**
	 * @param popMenu The popMenu to set.
	 */
	public void setPopMenu(String popMenu) {
		this.popMenu = popMenu;
	}
}
