package com.cvicse.inforreport.classic.engine.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.cvicse.inforreport.api.IReportDatasource;
import com.cvicse.inforreport.dataset.webservice.WSClient;
import com.cvicse.inforreport.engine.ReportEngineFactory;
import com.cvicse.inforreport.model.datasource.WSDatasource;
import com.cvicse.inforreport.util.EngineUtils;
import com.cvicse.inforreport.util.Utils;

public class DesignerServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(DesignerServlet.class);

	private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

	public void init(ServletConfig config) throws ServletException {

	}

	/**
	 * 发布模板权限校验
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doDeployValidate(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("doDeployValidate");
		PrintWriter out = response.getWriter();
		boolean bool = false;
		DesignerDeploy deploy = new DesignerDeploy();
		try {
			bool = deploy.hasValidation();
		} catch (Throwable ex) {
			// log.error(ex.getMessage(),ex);
			response.sendError(-1, ex.getMessage());
		}
		out.write(Boolean.toString(bool));

		out.close();
	}

	/**
	 * 处理模板发布和文件(xls/pdf/dat)发布
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doDeploy(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("doDeploy");
		response.setContentType("text/plain; charset=ISO-8859-1");
		PrintWriter out = response.getWriter();
		long resLength = -1;
		int status = 0;
		try {
			DesignerDeploy deploy = new DesignerDeploy();
			if(request.getParameter("type")!=null&&request.getParameter("type").equals("file")){
				log.debug("deploy file...");
				status = deploy.deployFile(request);
			}else{
				log.debug("deploy template...");
				String path = EngineUtils.decodeHttpBase64(request.getParameter("path"));				
				
				String username = request.getParameter("username");
				if (username != null) {
					username = EngineUtils.decodeHttpBase64(username);
				}
				String password = request.getParameter("password");
				if (password != null) {
					password = EngineUtils.decodeHttpBase64(password);
				}		
				// 权限校验
				deploy.deployValidate(username, password, path);
				
	
				// 模板内容
				String tempContent = request.getParameter("template");
				if (tempContent == null) {
					throw new Exception("CannotGetTemplate");
				}
				resLength = tempContent.length();
				tempContent = tempContent.replace(' ', '+');
				tempContent = tempContent.replace('`', '=');
				// tempContent = Utils.decodeBase64(tempContent);
	
				// 模板名
				String rptName = request.getParameter("rptName");
				// rptName = rptName.replaceAll("\\ ", "\\+");
				// rptName = rptName.replaceAll("\\`", "\\=");
				rptName = EngineUtils.decodeHttpBase64(rptName);
	
				// 是否覆盖
				String cover = request.getParameter("cover");
	
				status = deploy.deploy(path, tempContent, rptName, cover);
			}
			response.setStatus(status);
			// 返回发布成功标志
			response.setHeader("Content-Length", Long.toString(resLength));
			// response.setContentLength((int) resLength);
			out.println("Return");
			return;
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			response.setContentLength(0);
			String err = "DeployFailed" + ": " + e.getMessage();
			response.sendError(500, new String(err.getBytes(), "ISO-8859-1"));

		} finally {
			out.flush();
			out.close();
		}
	}

	/**
	 * 校验sql
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doCheckSql(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("doCheckSql");
		response.setContentType("text/plain; charset=ISO-8859-1");

		PrintWriter pw = response.getWriter();
		try {
			String cdata = request.getParameter("cdatadefine");
			if (cdata == null)
				throw new Exception("Cannot Get CDataDefine");
			cdata = cdata.replace(' ', '+');
			cdata = cdata.replace('`', '=');
			cdata = EngineUtils.decodeBase64(cdata);
			// log.debug("cdatadefine:" + cdata);

			String checkInfo = new DesignerCheckSQL().check(cdata);
			//System.out.println("!!!!!"+checkInfo);
			pw.print(checkInfo); // 返回给查看器

		} catch (Throwable ex) {
			// log.error(Utils.getResourceValue("CheckSQLFailed"));
			pw.print(ex.getMessage());
		}
		pw.flush();
		pw.close();
	}

	/**
	 * 查看报表
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doShow(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("doPreview");
		PrintWriter out = response.getWriter();
		// ServletOutputStream out = response.getOutputStream();

		try {
			String var0 = request.getParameter("template");

			if (var0 == null) {
				// log.error(Utils.getResourceValue("CannotGetTemplate"));
				throw new ServletException(EngineUtils.getResourceValue("CannotGetTemplate"));
			}

			// var0 = var0.replaceAll("\\ ", "\\+");
			// var0 = var0.replaceAll("\\`", "\\=");

			var0 = var0.replace(' ', '+');
			var0 = var0.replace('`', '=');
			
			DesignerShow show = new DesignerShow();
			out.print(show.showReport(var0));
			
			out.flush();
			out.close();
		}catch (Throwable e) {
            out.println("<p>");
            out.println(EngineUtils.getResourceValue("ShowReportError")+":");
            out.println("</p>");
            out.println(e.getMessage());
            out.println("<br><pre>");
            e.printStackTrace(out);
            out.println("</pre>");
// out.println("</body></html>");
            out.flush();
            out.close();
		}
	}

	/**
	 * 根据设计器发送的wsdl返回service、operaion、request组成的xml字符串
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doGetWS(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("doGetWS");
		response.setContentType("text/plain; charset=ISO-8859-1");

		PrintWriter pw = response.getWriter();
		try {
			String source = request.getParameter("source");
			String wsdl = null;
			if (source != null) {
				source = source.replace(' ', '+');
				source = source.replace('`', '=');
				source = EngineUtils.decodeBase64(source);
				IReportDatasource ids = ReportEngineFactory
						.getReportDatasource();
				wsdl = ((WSDatasource) ids.getDS(source)).getUrl();
			} else {
				wsdl = request.getParameter("wsdl");
				if (wsdl == null)
					throw new Exception("Cannot get wsdl");
				wsdl = wsdl.replace(' ', '+');
				wsdl = wsdl.replace('`', '=');
				wsdl = EngineUtils.decodeBase64(wsdl);
			}
			log.debug("wsdl:" + wsdl);

			WSClient client = new WSClient();
			String s = client.getServices(wsdl);
			s = Utils.encodeBase64ForCheckSQL(s);
			pw.print(s); // 返回给查看器

		} catch (Throwable ex) {
			log.error("doGetWS error.", ex);
			pw.print(ex.getMessage());
		}
		pw.flush();
		pw.close();
	}

	/**
	 * 解析设计器发送的cdatadefine串，返回response
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doCheckWS(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("doCheckWS");
		response.setContentType("text/plain; charset=ISO-8859-1");

		PrintWriter pw = response.getWriter();
		try {
			String cdata = request.getParameter("cdatadefine");
			cdata = cdata.replace(' ', '+');
			cdata = cdata.replace('`', '=');
			cdata = EngineUtils.decodeBase64(cdata);

			SAXReader reader = new SAXReader();
			StringReader sr = new StringReader(cdata);
			Element root = reader.read(sr).getRootElement();
			String wsdl = root.elementText("wsdl");
			String serviceName = root.elementText("service");
			String operationName = root.elementText("operation");
			String requeststr = root.elementText("request");
			// requeststr = EngineUtils.decodeBase64(requeststr);
			sr.close();

			String responsestr = WSClient.invokeOperation(wsdl, serviceName,
					operationName, requeststr);
			responsestr = Utils.encodeBase64ForCheckSQL(responsestr);
			pw.print(responsestr); // 返回给查看器

		} catch (Throwable ex) {
			log.error("doCheckWS error.", ex);
			pw.print(ex.getMessage());
		}
		pw.flush();
		pw.close();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType(CONTENT_TYPE);		
		String action = null;
		action = request.getParameter("action");
		if (action == null) {
			response.setContentType("text/plain; charset=ISO-8859-1");
			String err = "Null action!";
			response.sendError(-1, new String(err.getBytes(), "ISO-8859-1"));
		} else if (action.equals("deploy")) {
			this.doDeploy(request, response);
		} else if (action.equals("publishvalidate")) {
			this.doDeployValidate(request, response);
		} else if (action.equals("checkSQL")) {
			this.doCheckSql(request, response);
		} else if (action.equals("show")) {
			this.doShow(request, response);
		} else if (action.equals("doCheckWS")) { // 返回 response
			this.doCheckWS(request, response);
			// action=doCheckWS&cdatadefine=
		} else if (action.equals("doGetWS")) { // 返回service、operation、request
			this.doGetWS(request, response);
			// action=doGetWS & source=xxxx 或
			// action=doGetWS & wsdl=xxxx
		} else {
			response.setContentType("text/plain; charset=ISO-8859-1");
			// Utils.error("ErrorActionForDeployservlet",": "+action);
			String err = "Error Action" + action;

			response.sendError(-1, new String(err.getBytes(), "ISO-8859-1"));
		}

	}

	// Process the HTTP Post request
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	// Clean up resources
	public void destroy() {
	}

}