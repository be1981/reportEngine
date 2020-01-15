package com.cvicse.inforreport.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.util.EntityResolverImpl;
import com.cvicse.inforreport.util.Utils;
import com.cvicse.inforreport.classic.engine.ConfigurationManager;


/**
 * 采用单件设计模式的职责链工厂
 * @author shi_hang
 *
 */
public class HandlerChainFactory {

	private static final Log log = LogFactory.getLog(HandlerChainFactory.class);
	/**
	 * 存放了所有职责链的配置信息。以配置文件中的chain_name为名，以HandlerConfigs对象为值
	 */
	private static HashMap handlerChainConfigs = new HashMap();

	/**
	 * 采用单件模式
	 */
	private static HandlerChainFactory instance=null;

	private static Integer lock = new Integer(1);

	/**
	 * 由于采用单件模式且通过静态方法引用，因此禁止使用构造函数，保证通过统一的静态方法引用。
	 */
	private HandlerChainFactory(){}

	/**
	 * 根据职责链的名称获取一个职责链实例，即使对于同一名称，每次调用都会返回一个全新的职责链实例
	 * @param chain_name 职责链的名称，对应于在inforreport-handler.xml中的chain-name元素值
	 * @return 全新构造的职责链实例
	 * @throws ReportException
	 */
	public static HandlerChain createHandlerChain(String chain_name)
	{
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new HandlerChainFactory();
					instance.init();
				}
			}
		}

		/*
		 * TODO: 根据HandlerConfig的配置构造一个新的HandlerChain实例返回，参考Tomcat
		 */

		List handlerConfigs = (List) handlerChainConfigs.get(chain_name);
		if (chain_name == null||handlerConfigs == null) {
//			Utils.debug("<Handler-chain> not found.The <chain_name> is: "+chain_name);
			log.debug("Handler-chain not found: "+chain_name);
			return null;
		}
		//构造HandlerChain对象
		HandlerChain handlerChain = new HandlerChain();

		// 遍历List对象
		for (int i = 0; i < handlerConfigs.size(); i++) {
			// 依次取出HandlerConfig对象，调用HandlerChain的addHandler(HandlerConfig)方法
			HandlerConfig handlerConfig = (HandlerConfig) handlerConfigs.get(i);
			handlerChain.addHandler(handlerConfig);
		}

		return handlerChain;
	}

	/**
	 * 在classpath下读取inforreport-handler.xml，初始化handlerChainConfigs对象。
	 * @throws ReportException
	 */
	private void init()
	{
		// TODO:reference to tomcat
		try {
/*			String configFilePath = ConfigurationManager.getInstance()
					.getConfigFilePath();
			File configFile = new File(configFilePath).getParentFile();
			//读取inforreport-handler.xml，
			SAXReader saxReader = new SAXReader();
			saxReader.setEntityResolver(new EntityResolverImpl());
			Document dom = saxReader.read(new File(configFile,Utils.getResourceValue("Product")+"-handler.xml"));*/
			Document dom = null;
			String path = ConfigurationManager.getInstance().getHandlerFilePath();
			if (path != null) {
				File configFile = new File(path).getParentFile();
				// 读取inforreport-handler.xml，
				SAXReader saxReader = new SAXReader();
				saxReader.setEntityResolver(new EntityResolverImpl());
				//dom = saxReader.read(new File(configFile,Utils.getResourceValue("Product")+"-handler.xml"));
				dom = saxReader.read(new File(configFile,"inforreport-handler.xml"));
			} else { //weblogic
				dom = ConfigurationManager.getInstance().getHandlerDom();
			}

			//遍历<handler-chain>列表
			List chainElems = dom.getRootElement().elements("handler-chain");
			for(int i=0;i<chainElems.size();i++){
			    //依次获得<handler-chain>，获取它的<handler>列表；构造List对象handlerConfigList
				Element chainElem = (Element)chainElems.get(i);
				String chainName = chainElem.elementText("chain-name");
				String des = chainElem.elementText("description");
				List handlerConfigList = new ArrayList();
				List handlerElems = chainElem.elements("handler");
			    //遍历<handler>列表
				for(int j=0;j<handlerElems.size();j++){
					Element handlerElem=(Element)handlerElems.get(j);
					//依次构造HandlerConfig对象
					HandlerConfig handlerConfig = new HandlerConfig();
					handlerConfig.setName(handlerElem.elementText("handler-name"));
					String handlerClass =handlerElem.elementText("handler-class");
					if(handlerClass==null||handlerClass.trim().equals("")){
						throw new ReportException("Null value of <handler-class>.The <handler-name> is: "
								+handlerElem.elementText("handler-name"));
					}
					handlerConfig.setHandlerClass(handlerClass);

					handlerConfig.setDescription(handlerElem.elementText("description"));
					List initparaElems = handlerElem.elements("init-param");
					for(int k=0;k<initparaElems.size();k++){
						Element initparaElem = (Element)initparaElems.get(k);
						handlerConfig.setParameter(
								initparaElem.elementText("param-name"),
								initparaElem.elementText("param-value"));
					}
				    //将HandlerConfig对象加入List对象
					handlerConfigList.add(handlerConfig);
				}
				handlerChainConfigs.put(chainName,handlerConfigList);
			}
		//End
		} catch(Throwable ex){
//			Utils.fatal(ex.getMessage(),ex);
			log.error(ex.getMessage(),ex);
			throw new RuntimeException(ex.getMessage());
		}

	}
}
