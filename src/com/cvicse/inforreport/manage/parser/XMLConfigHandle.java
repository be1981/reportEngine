package com.cvicse.inforreport.manage.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.cvicse.inforreport.manage.model.ReportInfo;

public class XMLConfigHandle {

	String path = "";

	SAXReader saxReader = new SAXReader();

	static Document document = null;

	public XMLConfigHandle(String path) {
		try {
			this.path = path;
			document = saxReader.read(new File(path));
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 修改报表信息，发现有报表模板文件名称和目录一样的时候修改报表信息
	public void edit(ReportInfo reportInfo) {
		String filename = reportInfo.getFileName();
		String name = reportInfo.getName();
		String dir = reportInfo.getDir();
		String type = reportInfo.getType();
		String remark = reportInfo.getRemark();
		List list = document.selectNodes("reports/report");
		for (int i = 0; i < list.size(); i++) {
			Element ele = (Element) list.get(i);
			String domfilename = ele.attributeValue("filename");
			String domdir = ele.attributeValue("dir");
			if (dir.equals(domdir) && filename.equals(domfilename)) {
				ele.setAttributeValue("name", name);
				ele.setAttributeValue("type", type);
				ele.setAttributeValue("remark", remark);
				break;
			}
		}
		save();
	}

	// 获取指定目录名和文件名的报表模板信息
	public ReportInfo get(String filename, String dir) {
		ReportInfo reportInfo = null;
		List list = document.selectNodes("reports/report");
		for (int i = 0; i < list.size(); i++) {
			Element ele = (Element) list.get(i);
			String domfilename = ele.attributeValue("filename");
			String name = ele.attributeValue("name");
			String domdir = ele.attributeValue("dir");
			String type = ele.attributeValue("type");
			String remark = ele.attributeValue("remark");
			if (dir.equals(domdir) && filename.equals(domfilename)) {
				reportInfo = new ReportInfo();
				reportInfo.setFileName(filename);
				reportInfo.setName(name);
				reportInfo.setDir(dir);
				reportInfo.setType(type);
				reportInfo.setRemark(remark);
				break;
			}
		}
		return reportInfo;
	}

	// 获取所有报表信息
	public List query(String dirpath) {
		try {
			List list = document.selectNodes("reports/report");
			List reportInfos = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				Element ele = (Element) list.get(i);
				String filename = ele.attributeValue("filename");
				String name = ele.attributeValue("name");
				String dir = ele.attributeValue("dir");
				String type = ele.attributeValue("type");
				String remark = ele.attributeValue("remark");
				if (dirpath == null) {
					ReportInfo reportInfo = new ReportInfo();
					reportInfo.setFileName(filename);
					reportInfo.setName(name);
					reportInfo.setDir(dir);
					reportInfo.setType(type);
					reportInfo.setRemark(remark);
					reportInfos.add(reportInfo);
				} else if (dirpath != null && dirpath.equals(dir)) {
					ReportInfo reportInfo = new ReportInfo();
					reportInfo.setFileName(filename);
					reportInfo.setName(name);
					reportInfo.setDir(dir);
					reportInfo.setType(type);
					reportInfo.setRemark(remark);
					reportInfos.add(reportInfo);
				}
			}
			return reportInfos;
		} catch (Exception e) {
		}
		return null;
	}

	// 根据报表名称和目录名称删除配置文件中的某张报表的信息,做完操作后保存�������󱣴�
	public void delete(String filename, String dir) {
		List list = document.selectNodes("reports");
		Element element = (Element) list.iterator().next();
		List childList = document.selectNodes("reports/report");
		Element childElement = null;
		for (int i = 0; i < childList.size(); i++) {
			childElement = (Element) childList.get(i);
			String domfilename = childElement.attributeValue("filename");
			String domdir = childElement.attributeValue("dir");
			if (dir.equals(domdir) && filename.equals(domfilename)) {
				element.remove(childElement);
				break;
			}
		}
		save();
	}

	// 添加一个报表时候，向配置文件添加配置信息
	public void add(ReportInfo reportInfo) {
		Element reports = null;
		String filename = reportInfo.getFileName();
		String name = reportInfo.getName();
		String dir = reportInfo.getDir();
		String type = reportInfo.getType();
		String remark = reportInfo.getRemark();
		try {
			List list = document.selectNodes("reports");
			reports = (Element) list.iterator().next();
			Element report = reports.addElement("report");
			report.addAttribute("name", name);
			report.addAttribute("filename", filename);
			report.addAttribute("dir", dir);
			report.addAttribute("type", type);
			report.addAttribute("remark", remark);
		} catch (Exception e) {
			e.printStackTrace();
		}
		save();
	}
	
	/**
	 * 判断报表标题是否已存在
	 * @param name
	 * @return
	 */
	public boolean exists(String name){
		Node node = document.getRootElement().selectSingleNode("report[@name='"+name+"']");
		if(node!=null)
			return true;
		return false;
	}

	/**
	 * @param key
	 * @param stringArray
	 * @param isAttribute
	 * @throws IOException
	 */

	public Element insert(String key, String[] stringArray, Boolean isAttribute) {
		Element element = null;
		try {
			List list = document.selectNodes(key);
			element = (Element) list.iterator().next();
			if (isAttribute) {
				element = element.addAttribute(stringArray[0], stringArray[1]);
			} else {
				element = element.addElement(stringArray[0]);
				element.setText(stringArray[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return element;
	}

	public void treeWalk(Element element) {
		for (int i = 0, size = element.nodeCount(); i < size; i++) {
			Node node = element.node(i);
			if (node instanceof Element) {
				treeWalk((Element) node);
			} else { // do something....
			}
		}
	}

	public String getStringFromDocument() {
		String text = document.asXML();
		return text;
	}

	private void save() {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,
					"utf-8"));
			XMLWriter writer = new XMLWriter(bw);
			writer.write(document);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
