package com.cvicse.inforreport.handler;

import java.util.ArrayList;
import java.util.Iterator;

import com.cvicse.inforreport.exceptions.ReportException;

/**
 * 基于职责链模式的职责链类定义。
 * @author shi_hang
 *
 */
public class HandlerChain {

    /**
     * 职责链上的处理器对象列表
     */
    private ArrayList handlers = new ArrayList();


    /**
     * 用于维护职责链当前位置的迭代器，该迭代器仅应在handlerRequest方法被调用时使用
     */
    private Iterator iterator = null;

    /**
	 * 基于职责链模式处理请求，该方法会通过iterator调用职责链上的下一处理器对象
	 * @param request
	 * @throws ReportException
	 */
	public void handlerRequest(Request request,Response response) throws ReportException
	{
		// TODO:参考tomcat的实现方式
		if (this.iterator == null)
            this.iterator = handlers.iterator();

		if (this.iterator.hasNext()) {
			HandlerConfig handlerConfig = (HandlerConfig) iterator.next();
			Handler handler = null;
			try {
				handler = handlerConfig.getHandler();
				handler.init(handlerConfig);
				handler.handleRequest(request,response, this);
			} catch (ClassNotFoundException ex) {
				//Utils.fatal(ex.getMessage(),ex);
				throw new ReportException("ClassNotFoundException：" + ex.getMessage() + ".");
			} catch (InstantiationException ex) {
				//Utils.fatal(ex.getMessage(),ex);
				throw new ReportException("InstantiationException：" + ex.getMessage() + ".");
			} catch (IllegalAccessException ex) {
				//Utils.fatal(ex.getMessage(),ex);
				throw new ReportException("IllegalAccessException：" + ex.getMessage() + ".");
			} catch(ReportException ex){
				//Utils.fatal(ex.getMessage(),ex);
				throw ex;
			}
		}
	}

	/**
	 *
	 * @param handlerConfig
	 */
	void addHandler(HandlerConfig handlerConfig){
		this.handlers.add(handlerConfig);
	}
}
