package com.cvicse.inforreport.handler;

import com.cvicse.inforreport.exceptions.ReportException;
/**
 * 基于职责链模式处理请求的处理器接口。
 * @author shi_hang
 *
 */
public interface Handler {

	/**
	 *  本方法采用职责链模式处理请求。当前对象可以决定是否对请求对象进行处理，
	 *  或者将请求转给职责链继续处理。
	 * @param request 请求对象
	 * @param chain 当前处理器所在的职责链实例
	 * @throws ReportException
	 */
	public void handleRequest(Request request,Response response, HandlerChain chain)throws ReportException;

	/**
	 * 本方法用于初始化当前处理器。
	 * @param config 存放配置信息的对象
	 * @throws ReportException
	 */
	public void init(HandlerConfig config)throws ReportException;
}
