package com.cvicse.inforreport.classic.engine;


import com.cvicse.inforreport.exceptions.ReportException;
import com.cvicse.inforreport.util.EngineUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;

import java.util.List;

/**
 * 判断模板类型  ---修改了内容,不再校验
 * @author qiao_lu1
 *
 */
public class TemplateType {

	private static final Log log = LogFactory.getLog(TemplateType.class);
	/**
	 * 模板标记
	 */
	private String edition;

	/**
	 * <connection>节点列表
	 */
	private List conElems;

	/**
	 * 产品类型
	 */
	//private static String product = VerInfo.getProductType();


	private TemplateType(){

	}

	public TemplateType(Document dom) {

//		edition = dom.getRootElement().attributeValue("edition");
//		if(product.equals("standard")) {
//			Namespace ns = dom.getRootElement().getNamespace();
//			Element datasetElem = dom.getRootElement().element(new QName("dataset", ns));
//			if (datasetElem != null){
//				conElems = datasetElem.selectNodes("dataDefine/connection");
//			}
//		}
	}

	/**
	 * 判断模板是否符合要求
	 * @return true 是, false 不是
	 */
	public boolean match() throws ReportException{
//		boolean match = this.match(product);
//		if(match&&(conElems!=null)) this.matchStandard(conElems);
//		return match;
        return true;
	}

	/**
	 * 判断模板是否匹配指定产品类型
	 * @param product 产品类型
	 * @return true 是, false 不是
	 */
	public boolean match(String product){
		boolean bool = true;
		if ((product.equals("enterprise"))&&
				(edition == null||edition.equals("")
						|| edition.startsWith("e")|| edition.equals("oem2"))) {
			bool = true;
		}
		else if(product.equals("professional")&&
				(edition == null||edition.equals("")|| edition.indexOf("p")!=-1)){
			bool = true;
		}
		else if(product.equals("standard")&&
				(edition == null||edition.equals("")|| edition.indexOf("s")!=-1)){
			bool = true;
		}
		else if(product.equals("oem")&& edition != null&&
				(edition.equals("oem")|| edition.equals("oem_yk")|| edition.equals("oem_xsd"))){
			bool = true;
		}
		else if(product.equals("official")){
			bool=true;
		}
		else{
//			Utils.error("TemplateNotMatch",": "+product+", "+edition);
			log.error(EngineUtils.getResourceValue("TemplateNotMatch")+": "+product+", "+edition);
		}
		return bool;
	}

	/**
	 * 判断标准版模板是否连接了多数据库
	 * @param cons <connection>节点列表
	 */
	protected void matchStandard(List cons) throws ReportException{
		String firstCon = ((Node) cons.get(0)).getText();
		for (int i = 0; i < cons.size(); i++) {
			if (!((Node) cons.get(i)).getText().equals(firstCon)) {
//				Utils.error("StandardNotSupportMultiDB");
				log.error(EngineUtils.getResourceValue("StandardNotSupportMultiDB"));
				throw new ReportException(EngineUtils
						.getResourceValue("StandardNotSupportMultiDB"));
			}
		}
	}


}
