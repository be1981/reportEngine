package com.cvicse.inforreport.classic.engine.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.classic.engine.ConfigurationManager;
import com.cvicse.inforreport.engine.ReportManager;
import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.handler.HandlerChain;
import com.cvicse.inforreport.handler.HandlerChainFactory;
import com.cvicse.inforreport.util.EngineUtils;

public class DesignerDeploy {

	private static final Log log = LogFactory.getLog(DesignerDeploy.class);

	private HandlerChain handlerChain = HandlerChainFactory
			.createHandlerChain("DeployChain");

	public DesignerDeploy() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean hasValidation() {
		// handlerChain = HandlerChainFactory.createHandlerChain("DeployChain");
		if (handlerChain == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 发布模板权限校验
	 * @param username
	 * @param password
	 * @param path
	 * @throws ReportException
	 */
	public void deployValidate(String username, String password, String path)
			throws ReportException {
		try {
			// handlerChain =
			// HandlerChainFactory.createHandlerChain("DeployChain");

			if (handlerChain != null) {
				com.cvicse.inforreport.handler.Request req = new com.cvicse.inforreport.handler.Request();
				req.setParameter("deployPath", path);
				req.setParameter("username", username);
				req.setParameter("password", password);
				com.cvicse.inforreport.handler.Response res = new com.cvicse.inforreport.handler.Response();

				handlerChain.handlerRequest(req, res);

			}
		} catch (ReportException ex) {
			log.error(ex.getMessage(), ex);
			throw new ReportException(ex.getMessage());
		}
	}

	/**
	 * 发布模板ipr文件
	 * @param path 子路径
	 * @param template 模板内容
	 * @param rptName 模板名称
	 * @param cover 是否覆盖
	 * @return
	 * @throws ReportException
	 */
	public int deploy(String path, String template, String rptName, String cover)
			throws Exception {
		File tmpFile = null;
		FileOutputStream fos = null;
		int status = 200;
		try {
			// 发布路径
//			String dir = ConfigurationManager.getInstance().getTemplateRepositoryPath();
			String dir = ReportManager.getInstance().getReportTemplatesHome().toString();
			//dir += File.separator + path;
			//tmpFile = new File(dir);
			tmpFile = new File(dir,path);
			log.debug("deploy Path: " + tmpFile.toString());
			if (!(tmpFile.exists() && tmpFile.isDirectory())) {
				tmpFile.mkdirs();
			}

			// 文件全路径
			tmpFile = new File(tmpFile, rptName+".ipr");
			log.debug("deploy template Name: "+ rptName+".ipr");

			//询问设计器是够覆盖
			if (tmpFile.exists()&& tmpFile.isFile()
					&& (cover == null || cover.trim().equals("") || cover
							.equals("false"))) {
				// response.sendError(-2,"cover?");
				return 100;
			}
			
			// 发布模板(生成新模板或覆盖旧模板)
			fos = new FileOutputStream(tmpFile);
			fos.write(template.getBytes());
			fos.close();

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw ex;

		} finally {
			if (fos != null)
				fos.close();
		}
		return status;
	}
	
	/**
	 * 发布xls/pdf/dat文件
	 * @param request
	 * @throws Exception
	 */
	public int deployFile(HttpServletRequest request) throws Exception{
		int status = 200;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(4096); // 设置缓冲区大小，这里是4kb
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(request);// 得到所有的文件
		Iterator<FileItem> i = items.iterator();
		String path = null;
		while (i.hasNext()) {
			FileItem fi = (FileItem) i.next();
			if(fi.getFieldName().equals("path")){
				path = ReportManager.getInstance().getReportTemplatesHome().toString()+
					File.separator+EngineUtils.decodeHttpBase64(fi.getString());
				log.debug("deploy path: "+path);
				File pathfile = new File(path);
				if(!pathfile.exists() || !pathfile.isDirectory()){
					boolean bool = pathfile.mkdir();
					if(!bool) log.error("create dir error: "+pathfile);
				}
			}else {
				String fileName = fi.getName();		
				log.debug("deploy file name: "+fileName);
				if (fileName != null) {
					File fullFile = new File(fi.getName());
					File savedFile = new File(path, fullFile.getName());
					fi.write(savedFile);
				}
			}
		}
		return status;

	}

}
