package com.idega.block.formreader.business.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
}