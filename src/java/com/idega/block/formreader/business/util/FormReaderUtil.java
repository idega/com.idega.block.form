package com.idega.block.formreader.business.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.chiba.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idega.block.form.bean.LocalizedStringBean;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas �ivilis</a>
 * @version 1.0
 */
public class FormReaderUtil {
	
	private static DocumentBuilderFactory factory;
	
	public static final String slash = "/";
	public static final String dot_xhtml = ".xhtml";
	public static final String form_bean_name = "formBean";
	public static final String output_tag = "xf:output";
	public static final String title_tag = "title";
	public static final String loc_ref_part1 = "instance('localized_strings')/";
	public static final String loc_ref_part2 = "[@lang=instance('localized_strings')/current_language]";
	public static final String head_tag = "head";
	public static final String loc_tag = "localized_strings";
	public static final String data_mod = "data_model";
	public static final String default_language_tag = "default_language";

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
	
	public static LocalizedStringBean getTitleLocalizedStrings(Document xforms_doc) {
		
		Element title = (Element)xforms_doc.getElementsByTagName(title_tag).item(0);
		Element output = (Element)title.getElementsByTagName(output_tag).item(0);
		
		return getElementLocalizedStrings(output, xforms_doc);
	}
	
	public static LocalizedStringBean getElementLocalizedStrings(Element element, Document xforms_doc) {
		
		String ref = element.getAttribute("ref");
		
		if(!isRefFormCorrect(ref))
			return new LocalizedStringBean();
		
		String key = getKeyFromRef(ref);
		
		return getLocalizedStrings(key, xforms_doc);
	}
	
	public static boolean isRefFormCorrect(String ref) {

		return ref != null && ref.startsWith(loc_ref_part1) && ref.endsWith(loc_ref_part2) && !ref.contains(" "); 
	}
	
	public static String getKeyFromRef(String ref) {
		return ref.substring(ref.indexOf(slash)+1, ref.indexOf("["));
	}
	
	public static LocalizedStringBean getLocalizedStrings(String key, Document xforms_doc) {

		Element loc_model = getElementByIdFromDocument(xforms_doc, head_tag, data_mod);
		Element loc_strings = (Element)loc_model.getElementsByTagName(loc_tag).item(0);
		
		NodeList key_elements = loc_strings.getElementsByTagName(key);
		LocalizedStringBean loc_str_bean = new LocalizedStringBean();
		
		for (int i = 0; i < key_elements.getLength(); i++) {
			
			Element key_element = (Element)key_elements.item(i);
			
			String lang_code = key_element.getAttribute("lang");
			
			if(lang_code != null) {
				
				String content = getElementsTextNodeValue(key_element);
				loc_str_bean.setString(new Locale(lang_code), content == null ? "" : content);
			}
		}
		
		return loc_str_bean;
	}
	
	public static Element getElementByIdFromDocument(Document doc, String start_tag, String id_value) {
		
		return getElementByAttributeFromDocument(doc, start_tag, "id", id_value);
	}
	
	/**
	 * 
	 * @param doc - document, to search for an element
	 * @param start_tag - where to start. Could be just null, then document root element is taken.
	 * @param attribute_name - what name attribute should be searched for
	 * @param attribute_value
	 * @return - Reference to element in document
	 */
	public static Element getElementByAttributeFromDocument(Document doc, String start_tag, String attribute_name, String attribute_value) {
		
		Element start_element;
		
		if(start_tag != null)
			start_element = (Element)doc.getElementsByTagName(start_tag).item(0);
		else
			start_element = doc.getDocumentElement();
		
		return DOMUtil.getElementByAttributeValue(start_element, "*", attribute_name, attribute_value);
	}
	
	public static String getElementsTextNodeValue(Node element) {
		
		Node txt_node = element.getFirstChild();
		
		if(txt_node == null || txt_node.getNodeType() != Node.TEXT_NODE) {
			return null;
		}
		String node_value = txt_node.getNodeValue();
		
		return node_value == null ? "" : node_value.trim();
	}
	
	public static Locale getDefaultFormLocale(Document form_xforms) {
		
		Element loc_model = getElementByIdFromDocument(form_xforms, head_tag, data_mod);
		Element loc_strings = (Element)loc_model.getElementsByTagName(loc_tag).item(0);
		NodeList default_language_node_list = loc_strings.getElementsByTagName(default_language_tag);
		
		String lang = null;
		if(default_language_node_list != null && default_language_node_list.getLength() != 0) {
			lang = getElementsTextNodeValue((Element)default_language_node_list.item(0));
		}		
		if(lang == null)
			lang = "en";			
		
		return new Locale(lang);
	}

	public static String getDefaultFormTitle(Document document) {
		LocalizedStringBean strings = getTitleLocalizedStrings(document);
		Locale default_form_locale = getDefaultFormLocale(document);
		
		String title = strings.getString(default_form_locale);
		return title == null ? "" : title;
	}

}