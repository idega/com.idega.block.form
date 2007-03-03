package com.idega.block.form.business.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class BlockFormUtil {
	
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
	public static final String form_id_tag = "form_id";
	public static final String ref_s_att = "ref";
	public static final String lang_att = "lang";
	public static final String data_instance_id = "data-instance";
	public static final String id_att = "id";
	public static final String src_att = "src";
	public static final String submit_button_class = "fbcomp_button_submit";
	public static final String trigger_tag = "xf:trigger";
	public static final String name_att = "name";
	public static final String relevant_att = "relevant";
	public static final String xpath_false = "false()";

	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		
		if(factory == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			factory.setAttribute("http://apache.org/xml/properties/dom/document-class-name",
					"org.apache.xerces.dom.DocumentImpl");
			
			BlockFormUtil.factory = factory;
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
		
		String ref = element.getAttribute(ref_s_att);
		
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
			lang = getElementsTextNodeValue(default_language_node_list.item(0));
		}		
		if(lang == null)
			lang = "en";			
		
		return new Locale(lang);
	}

	public static String getDefaultFormTitle(Document document) {
		LocalizedStringBean strings = getTitleLocalizedStrings(document);
		Locale default_form_locale = getDefaultFormLocale(document);
		
		String title = strings.getString(default_form_locale);
		return title;
	}
	
	public static String getFormIdFromSubmissionInstance(Node instance) {
		
		Element form_id = DOMUtil.getChildElement(instance, form_id_tag);
		
        if (form_id != null) {
        	return DOMUtil.getElementValue((Element) form_id);
        }
        return null;
	}
	
	/**
	 * 
	 * Copied from com.idega.formbuilder.business.form.manager.util.FormManagerUtil
	 * 
	 * Puts localized text on element. Localization is saved on the xforms document.
	 * 
	 * @param element - element, to change or put localization message
	 * @param xforms - xforms document
	 * @param loc_string - localized message
	 * @throws NullPointerException - something necessary not provided
	 */
	public static void putLocalizedText(Element element, Document xforms, LocalizedStringBean loc_string) {
		if(xforms == null)
			throw new NullPointerException("XForms document not provided");
		
		String ref = element.getAttribute(ref_s_att);
		
		if(ref == null || !isRefFormCorrect(ref))
			throw new NullPointerException("Ref and key not specified or ref has incorrect format");
		
		String key = getKeyFromRef(ref);
		
		Element loc_model = getElementByIdFromDocument(xforms, head_tag, data_mod);
		
		Element loc_strings = (Element)loc_model.getElementsByTagName(loc_tag).item(0);
		
		NodeList loc_tags = loc_strings.getElementsByTagName(key);
		
		Collection<Locale> lang_key_set = loc_string.getLanguagesKeySet();
		
		Collection<String> lang_strings = new ArrayList<String>();
		
		for (Iterator<Locale> iter = lang_key_set.iterator(); iter.hasNext();) {
			
			lang_strings.add(iter.next().getLanguage());
		}
		
		for (int i = 0; i < loc_tags.getLength(); i++) {
			
			Element loc_tag = (Element)loc_tags.item(i);
			
			if(!lang_strings.contains(loc_tag.getAttribute(lang_att))) {
				
				loc_strings.removeChild(loc_tag);
			}
		}
		lang_strings = null;
		
		for (Iterator<Locale> iter = lang_key_set.iterator(); iter.hasNext();) {
			Locale locale = iter.next();
			
			boolean val_set = false;
			
			if(loc_tags != null) {
				
				for (int i = 0; i < loc_tags.getLength(); i++) {
					
					Element loc_tag = (Element)loc_tags.item(i);
					
					if(loc_tag.getAttribute(lang_att).equals(locale.getLanguage())) {
						
						if(loc_string.getString(locale) != null)
							setElementsTextNodeValue(loc_tag, loc_string.getString(locale));
						
						val_set = true;
						break;
					}
				}
			}
			
			if(loc_tags == null || !val_set) {
				
//				create new localization element
				Element new_loc_el = xforms.createElement(key);
				new_loc_el.setAttribute(lang_att, locale.getLanguage());
				new_loc_el.appendChild(xforms.createTextNode(""));
				setElementsTextNodeValue(new_loc_el, loc_string.getString(locale) == null ? "" : loc_string.getString(locale));
				loc_strings.appendChild(new_loc_el);
			}
		}
	}
	
	public static void setElementsTextNodeValue(Node element, String value) {
		
		NodeList children = element.getChildNodes();
		List<Node> childs_to_remove = new ArrayList<Node>();
		
		for (int i = 0; i < children.getLength(); i++) {
			
			Node child = children.item(i);
			
			if(child != null && child.getNodeType() == Node.TEXT_NODE)
				childs_to_remove.add(child);
		}
		
		for (Iterator<Node> iter = childs_to_remove.iterator(); iter.hasNext();)
			element.removeChild(iter.next());
		
		Node text_node = element.getOwnerDocument().createTextNode(value);
		element.appendChild(text_node);
	}
	
	public static void putDefaultTitle(Document xforms_doc, String new_title_for_default_locale) {
		Locale default_locale = BlockFormUtil.getDefaultFormLocale(xforms_doc);
		LocalizedStringBean loc_bean = new LocalizedStringBean();
		loc_bean.setString(default_locale, new_title_for_default_locale);
		Element title_element = (Element)((Element)xforms_doc.getDocumentElement().getElementsByTagName(title_tag).item(0)).getElementsByTagName("*").item(0);
		putLocalizedText(title_element, xforms_doc, loc_bean);
	}
}