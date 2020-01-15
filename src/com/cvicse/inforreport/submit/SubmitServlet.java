/*
 * 创建日期 2005-9-27
 *
 * TODO 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.cvicse.inforreport.submit;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.handler.HandlerChain;
import com.cvicse.inforreport.handler.HandlerChainFactory;
import com.cvicse.inforreport.util.EngineUtils;
import com.cvicse.inforreport.util.ReportModelResource;

/**
 * @author zhou_fbo
 * 
 * TODO 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class SubmitServlet extends HttpServlet {
	
	private static final Log log = LogFactory.getLog(SubmitServlet.class);
	// private static String processerName;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		// 从web.xml中读取安全验证类名
		// processerName = config.getInitParameter("ProcesserImplClass");
	}

	public void destroy() {
		super.destroy();
	}

	public String getServletInfo() {
		return super.getServletInfo();
	}
	
	private void detachSameRow(List rows,int rownum){
		String fst = ((Node) rows.get(rownum)).getStringValue();
		for (int j = rownum+1; j < rows.size(); j++) {
			String sec = ((Node) rows.get(j)).getStringValue();
			if (sec.equals(fst)) {
				((Node) rows.get(j)).detach();
				//Utils.debug("detach row" + (j + 1));
			}
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// response.setContentType("text/html; charset=GBK");
		response.setContentType("text/html; charset=ISO-8859-1");

		PrintWriter out = response.getWriter();
		String data = null;
		Reader reader = null;
		try {
			// 获取提交信息转化为字符串
			// Reader reader = new InputStreamReader(request.getInputStream(),
			// "GBK");
			reader = new InputStreamReader(request.getInputStream());

			// InputStream reader = new FileInputStream("E:/submit.txt");

			// if (reader != null) {
			// //FileWriter sw = new FileWriter(new File("E:/submit.txt"));
			// StringWriter sw = new StringWriter();
			// char[] c = new char[1024];
			// int length;
			// while ( (length = reader.read(c)) > 0)
			// sw.write(c, 0, length);
			//
			// data = sw.toString();
			// System.out.println(data);
			// reader.close();
			// sw.flush();
			// sw.close();
			//
			// }

			// 解析查看器端提交的字节流,组成username、password、data
			SAXReader saxReader = new SAXReader();
			saxReader.setEntityResolver(new NoDTDEntityResolver());
			Document dom = saxReader.read(reader);
			Element root = dom.getRootElement();
			Element authElem = root.element("authentication");
			String username = null;
			String password = null;
			if (authElem != null) {
				username = authElem.elementText("username");
				password = authElem.elementText("password");
				root.remove(authElem);
			}

			List tables = root.selectNodes(".//table");
			for (int i = 0; i < tables.size(); i++) {
				Node table = (Node) tables.get(i);
				List rows = table.selectNodes(".//row");
				if (rows.size() == 0)
					continue;
				for(int j=0;j<rows.size();j++){
					detachSameRow(rows,j);
				}

			}

			data = EngineUtils.convertToString(dom, "", false, "UTF-8");
			//System.out.println("submit data: "+data);

			// 构造Request、Response对象
			com.cvicse.inforreport.handler.Request req = new com.cvicse.inforreport.handler.Request();
			req.setParameter("submitData", data);
			req.setParameter("username", username);
			req.setParameter("password", password);
			com.cvicse.inforreport.handler.Response res = new com.cvicse.inforreport.handler.Response();

			// 调用职责链
			HandlerChain handlerChain = HandlerChainFactory
					.createHandlerChain("SubmitChain");
			if (handlerChain == null)
				throw new ReportException(
						"[SubmitChain] was not found. No data updated");
			handlerChain.handlerRequest(req, res);

			// 如果职责链执行无误，从res中取得正确信息，返回查看器端
			String msg = (String) res.getParameter("message");
			log.debug("Submit Success: "+msg);
			if (msg == null)
				msg = ReportModelResource.getResourcesName("SubmitSuccess");
			out.print("success:" + new String(msg.getBytes(), "ISO-8859-1")); // send
																				// to
																				// Viewer
			// response.setStatus(200);

		} catch (ReportException ex) {
			log.fatal(ex.getMessage(), ex);
			response.sendError(-1, new String(ex.getMessage().getBytes(),
					"ISO-8859-1"));
		} catch (Throwable ex) {
			log.fatal(ReportModelResource.getResourcesName("SubmitFailed") + ": "
					+ ex.getMessage() + ".", ex);
			// String error = new
			// String(ReportModelResource.getResourcesName("SubmitFailed").getBytes(),"ISO-8859-1");
			String error = ReportModelResource.getResourcesName("SubmitFailed");
			String err = ex.getMessage();
			if (err != null)
				err = new String(err.getBytes(), "ISO-8859-1");
			response.sendError(-2, error + ": " + err);
		} finally {
			reader.close();
			out.close();
		}

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}