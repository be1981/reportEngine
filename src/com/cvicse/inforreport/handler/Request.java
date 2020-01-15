package com.cvicse.inforreport.handler;

import com.cvicse.inforreport.exceptions.ExistedParameterException;
import com.cvicse.inforreport.exceptions.ReportException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 封装了请求参数，职责链上的处理对象可以通过该对象传递参数
 * @author shi_hang
 *
 */
public class Request {
	private Map parameters = new HashMap();

	public Object getParameter(Object key) {
		return parameters.get(key);
	}

	/**
	 * 获取参数列表的名
	 * @return 存放了参数key的Set对象
	 */
	public Set entrySet() {
		return parameters.entrySet();
	}

	/**
	 * 增加一个参数，如该参数已存在则覆盖
	 * @param key
	 * @param value
	 */
	public void setParameter(Object key, Object value) {
		parameters.put(key, value);
	}

	/**
	 * 增加一个参数，该参数不得与已存在参数重名
	 * @param key
	 * @param value
	 * @throws ExistedParameterException
	 */
	public void addParameter(Object key,Object value)throws ExistedParameterException, ReportException
	{
		//TODO:检查是否已存在
		String v = (String)parameters.get(key);
		if(v!=null)
			throw new ExistedParameterException("Existed Parameter: "+key);
		parameters.put(key,value);
	}
}
