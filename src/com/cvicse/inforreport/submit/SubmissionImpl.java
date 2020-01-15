/*
 * 创建日期 2005-9-27
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author zhou_fbo
 * 
 *         TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class SubmissionImpl {

	private static final Log log = LogFactory.getLog(SubmissionImpl.class);

	private List updateElements;

	public SubmissionImpl(String str) throws Exception {
		this.init(str);
	}

	public SubmissionImpl(InputStream in) throws Exception {
		this.init(in);
	}

	protected void init(String str) throws Exception {
		SAXReader builder = new SAXReader();
		StringReader in = new StringReader(str);

		Element root = builder.read(in).getRootElement();
		updateElements = root.elements("update");

		in.close();

	}

	protected void init(InputStream in) throws Exception {
		SAXReader builder = new SAXReader();
		builder.setEntityResolver(new NoDTDEntityResolver());

		// Reader reader = new InputStreamReader(in, "GBK");
		Reader reader = new InputStreamReader(in);
		Document document = builder.read(reader);
		Element root = document.getRootElement();
		updateElements = root.elements("update");
	}

	public Map doSubmit() throws Exception {
		log.debug("there is " + updateElements.size()
				+ " Update Element nested in Submission Element");
		Map submitMsg = new HashMap();
		for (int i = 0; i < updateElements.size(); i++) {
			log.debug("Update Element [" + (i + 1) + "]:");
			Element updateElement = (Element) updateElements.get(i);
			UpdateImpl update = new UpdateImpl(updateElement);
			Map map = update.execute();
			submitMsg.putAll(map);
			// update.release();
		}
		return submitMsg;
	}

}