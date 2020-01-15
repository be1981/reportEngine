package com.cvicse.inforreport.classic.engine.servlet;

import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.classic.engine.ConfigurationManager;
import com.cvicse.inforreport.util.EngineUtils;

public class InitialServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(InitialServlet.class);

	public void init(ServletConfig servletconfig) throws ServletException {
		super.init(servletconfig);

		String cf = servletconfig.getInitParameter("ConfigFilePath");
		String cfAbsolutePath = servletconfig.getServletContext().getRealPath(cf);
		log.debug("ConfigFilePath: " + cfAbsolutePath);

		String hf = cf.substring(0, cf.lastIndexOf("/")) + "/"
					+ "inforreport-handler.xml";
		String hfAbsolutePath = servletconfig.getServletContext().getRealPath(hf);
		log.debug("HandlerFilePath: " + hfAbsolutePath);

		// String[] files = null;
		if (cfAbsolutePath != null && hfAbsolutePath != null) {
			// files = new String[] { cfAbsolutePath, hfAbsolutePath };
			try {
				// ConfigurationManager.getInstance().initProperties(files);
				ConfigurationManager.getInstance().initProperties(
						cfAbsolutePath, hfAbsolutePath);
			} catch (Throwable e) {
				log.error(EngineUtils.getResourceValue("InitServletError") + ": "
						+ e.getMessage());
				throw new ServletException(e);
			}
		} else { // war
			InputStream cfIn = servletconfig.getServletContext()
					.getResourceAsStream(cf);
			// InputStream mfIn = servletconfig.getServletContext()
			// .getResourceAsStream(mf);
			InputStream hfIn = servletconfig.getServletContext()
					.getResourceAsStream(hf);
			try {
				ConfigurationManager config = ConfigurationManager
						.getInstance();
				config.initProperties(cfIn);
				// if (mfIn != null)
				// config.initPortalXml(mfIn);
				if (hfIn != null)
					config.initHandlerXml(hfIn);

			} catch (Throwable e) {
				//log.error(Utils.getResourceValue("InitServletError") + ": "
				//		+ e.getMessage());
				throw new ServletException(e);
			}

		}
	}
}