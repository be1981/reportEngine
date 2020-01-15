/**
 * 
 */
package com.cvicse.inforreport.submit;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.handler.Handler;
import com.cvicse.inforreport.handler.HandlerChain;
import com.cvicse.inforreport.handler.HandlerConfig;
import com.cvicse.inforreport.handler.Request;
import com.cvicse.inforreport.handler.Response;
import com.cvicse.inforreport.util.ReportModelResource;

/**
 * @author qiao_lu1
 *
 */
public class MessageHandler implements Handler {
	
	private Map initparams;

	public void handleRequest(Request request, Response response, HandlerChain chain) throws ReportException {
		// TODO Auto-generated method stub
		Map map = (Map)request.getParameter("submitMessage");
		String show = (String)initparams.get("showMessage");
		//如果是detail,显示 "数据提交成功"+成功的详细信息,或 "数据提交失败"+成功和失败的详细信息.
		//如果是note,显示 "数据提交成功",或 "数据提交失败"+失败的详细信息.
		
		StringBuffer buffer = new StringBuffer();
		boolean hasError = false;
		for(Iterator it = map.keySet().iterator();it.hasNext();){
			String tableName = (String)it.next();
			buffer.append(ReportModelResource.getResourcesName("TableName")+": "+tableName);
			buffer.append("\n");
        	List msgList = (List)map.get(tableName);
        	for (int i = 0; i < msgList.size(); i++) {
				String[] s = (String[]) msgList.get(i);
				if(!s[0].equals("-1")){ //success
					if(show.equals("detail"))
						showSuccess(s, buffer);
				}else{  //fail
					showError(s, buffer);
					hasError = true;
				}
			}
		}
		
		if(!hasError){
			String message = ReportModelResource.getResourcesName("SubmitSuccess");
			if(show.equals("detail"))
				message += "\n" + buffer.toString();
			response.setParameter("message", message);
		}
		else{
			response.setParameter("message", buffer.toString());
//			throw new ReportException(buffer.toString());
		}
		
			
	}
	
	private void showSuccess(String[] s, StringBuffer msg){
		initMessage(s,msg);
		msg.append(ReportModelResource.getResourcesName("Success"));
		msg.append("\n");
	}
	
	private void showError(String[] s, StringBuffer msg){		
		initMessage(s,msg);
		msg.append(ReportModelResource.getResourcesName("Fail"));
		if(s[0].equals("0"))
			msg.append(ReportModelResource.getResourcesName("KeyNotMatch"));
		else if(s[0].equals("-1"))
			msg.append(ReportModelResource.getResourcesName("Reason")+s[3]);
		else
			msg.append("Error!");
		msg.append("\n");

	}
	private void initMessage(String[] s, StringBuffer msg){
		msg.append("    "+ReportModelResource.getResourcesName("Key")+s[2]+"  ");
		if(s[1].equals("a")) msg.append(ReportModelResource.getResourcesName("Add"));
		else if(s[1].equals("m")) msg.append(ReportModelResource.getResourcesName("Update"));
		else if(s[1].equals("d")) msg.append(ReportModelResource.getResourcesName("Delete"));
		msg.append(ReportModelResource.getResourcesName("Execute"));
	}

	public void init(HandlerConfig config) throws ReportException {
		// TODO Auto-generated method stub
		initparams = config.getParameters();
		
	}

}
