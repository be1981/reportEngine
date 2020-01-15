package com.cvicse.inforreport.classic.engine;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class XMLHandler extends DefaultHandler{
//  private String localname;
  private String qname;
//  private String uri;
  private String title;
//  private int i = 0;
  private Map map = null;
  private boolean parasExist = false;
  public XMLHandler() {
  }

  public void startElement(String uri, String localName, String qName,
                           Attributes atts) {
    if (qName.equals("inforreport")){
//      this.uri = uri;
//      localname = localName;
      title = atts.getValue("title");
      map = new HashMap();
      for(int j = 0;j<atts.getLength();j++){
        map.put(atts.getQName(j),atts.getValue(j));
      }
    }
    if (qName.equals("parameter")){
      parasExist = true;
//      System.out.println("!!!parameter");
//      this.uri = uri;
//      localname = localName;
//      String type = atts.getValue("name");
//      System.out.println(uri+","+type+","+localname+","+atts.getLength());
    }

//      localname = localName;
//      qname = qName;
//      i = i+1;

//      title = atts.getValue("title");
//      title = qName;
  }

  protected boolean parasExist(){
    return parasExist;
  }

  protected String getAttributeValue(String attrName){
    String value = (String)map.get(attrName);
//    Utils.debug("===begin getAttributeValue");
    return value;
  }

  protected String getTitle(){
//    Utils.debug("===begin getTitle");
    return title;

  }



  public static void main(String[] args) {
    XMLHandler xmlHandler1 = new XMLHandler();
  }

}