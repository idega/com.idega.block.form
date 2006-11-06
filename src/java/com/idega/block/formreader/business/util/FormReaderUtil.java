package com.idega.block.formreader.business.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 */
public class FormReaderUtil {
	
	private static DocumentBuilderFactory factory;
	
	public static final String slash = "/";
	public static final String dot_xhtml = ".xhtml";
	public static final String form_bean_name = "formBean";

	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		
		if(factory == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			factory.setAttribute("http://apache.org/xml/properties/dom/document-class-name",
					"org.apache.xerces.dom.DocumentImpl");
			
			FormReaderUtil.factory = factory;
		}
		
		return factory.newDocumentBuilder();
	}
	
	public static List<Element> getElementsByAttributeValue(Node start, String tag_name, String attr_name, String attr_value) {
        NodeList nl = ((Element)start).getElementsByTagName(tag_name);
        int l = nl.getLength();
        if(l == 0)
            return null;
        Element e = null;
        String compareValue = null;
        
        List<Element> elements = new ArrayList<Element>();
        
        for(int i = 0; i < l; i++) {
            e = (Element)nl.item(i);
            if(e.getNodeType() != 1)
                continue;
            compareValue = e.getAttribute(attr_name);
            if(compareValue.equals(attr_value))
            	elements.add(e);
        }

        return elements;
    }
	
	public static List<Element> getElementsByAttributeValueContained(Node start, String tag_name, String attr_name, String attr_value) {
        NodeList nl = ((Element)start).getElementsByTagName(tag_name);
        int l = nl.getLength();
        if(l == 0)
            return null;
        Element e = null;
        String compareValue = null;
        
        List<Element> elements = new ArrayList<Element>();
        
        for(int i = 0; i < l; i++) {
            e = (Element)nl.item(i);
            if(e.getNodeType() != 1)
                continue;
            compareValue = e.getAttribute(attr_name);
            if(compareValue.contains(attr_value))
            	elements.add(e);
        }

        return elements;
    }
}