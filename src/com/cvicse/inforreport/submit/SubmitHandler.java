package com.cvicse.inforreport.submit;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.handler.Handler;
import com.cvicse.inforreport.handler.HandlerChain;
import com.cvicse.inforreport.handler.HandlerConfig;
import com.cvicse.inforreport.handler.Request;
import com.cvicse.inforreport.handler.Response;

public class SubmitHandler implements Handler {
	
	private static final Log log = LogFactory.getLog(SubmitHandler.class);
	/**
	 * 本方法采用职责链模式处理请求。当前对象可以决定是否对请求对象进行处理， 或者将请求转给职责链继续处理。
	 * 
	 * @param request 请求对象
	 * @param chain 当前处理器所在的职责链实例
	 * @throws ReportException
	 */
	public void handleRequest(Request request,Response response, HandlerChain chain)
			throws ReportException{
		String data =(String)request.getParameter("submitData");
	    try {
	    	log.debug("获取数据提交信息: " + data);
	        SubmissionImpl helper = new SubmissionImpl(data);
	        Map submitMsg = helper.doSubmit();
	        /*
	        StringBuffer msg = new StringBuffer();
	        for(Iterator it = submitMsg.keySet().iterator();it.hasNext();){
	        	String tableName = (String)it.next();
	        	msg.append("表名: "+tableName);
	        	msg.append("\n");
	        	List msgList = (List)submitMsg.get(tableName);
	        	for(int i=0;i<msgList.size();i++){
	        		String[] s = (String[])msgList.get(i);
	        		msg.append("    关键字段: "+s[2]+"  ");
	        		if(s[1].equals("a")) msg.append("插入");
	        		else if(s[1].equals("m")) msg.append("更新");
	        		else if(s[1].equals("d")) msg.append("删除");
	        		msg.append("操作"+(s[0].equals("0")?"失败。可能原因是表中不存在相匹配的关键字段的值。":"成功"));
	        		msg.append("\n");
	        			
	        	}	        	
	        }
	        response.addParameter("message",msg.toString());
	        */
	        request.setParameter("submitMessage",submitMsg);
	        chain.handlerRequest(request, response);
	        
			//response.addParameter("message","数据提交成功!");
	        
			
		} catch (Throwable ex) {
			response.addParameter("message",ex.getMessage());
			log.error(ex.getMessage(),ex);
			throw new ReportException(ex.getMessage() + ".");
		}
		chain.handlerRequest(request,response);
	}
	

	/**
	 * 本方法用于初始化当前处理器。
	 * 
	 * @param config
	 *            存放配置信息的对象
	 * @throws ReportException
	 */
	public void init(HandlerConfig config) throws ReportException{

	}

}
