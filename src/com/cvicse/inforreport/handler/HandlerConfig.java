package com.cvicse.inforreport.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理处理器实例的配置信息。
 *
 * @author shi_hang
 *
 */
public class HandlerConfig {
	/**
	 * 处理器的名称，保留参数，当前未使用。
	 */
	private String name;

	/**
	 * 处理器的类全名，用于构造实例
	 */
	private String handlerClass;

	/**
	 * 描述信息，保留参数，当前未使用
	 */
	private String description;

	/**
	 * 根据inforreport-handler.xml形成的该处理器的参数Map
	 */
	private Map parameters = new HashMap();

	public HandlerConfig() {
		// TODO:根据xml节点进行初始化
	}

	/**
	 *
	 * @param param_name
	 * @return param_value
	 */
	public String getParameter(String param_name) {
		return (String) parameters.get(param_name);
	}

	/**
	 *
	 * @param param_name
	 * @param param_value
	 */
	public void setParameter(String param_name,String param_value) {
		parameters.put(param_name,param_value);
	}

	protected Handler getHandler() throws ClassNotFoundException,InstantiationException,IllegalAccessException{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Handler handler = null;

		Class clazz = loader.loadClass(handlerClass);
	    handler = (Handler) clazz.newInstance();

		return handler;
	}


	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * @return Returns the handlerClass.
	 */
	public String getHandlerClass() {
		return handlerClass;
	}


	/**
	 * @param handlerClass The handlerClass to set.
	 */
	public void setHandlerClass(String handlerClass) {
		this.handlerClass = handlerClass;
	}


	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return Returns the parameters.
	 */
	public Map getParameters() {
		return parameters;
	}


	/**
	 * @param parameters The parameters to set.
	 */
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

}
