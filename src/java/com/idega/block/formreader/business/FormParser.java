package com.idega.block.formreader.business;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.chiba.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.idega.block.formreader.business.util.BlockFormUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 */
public class FormParser {
	
	private Document form_document;
	private OutputStream output_stream;
	private Writer output_writer;
	private Element xml_to_fetch;
	
	private static final String form_tag = "form";
	private static final String input_tag = "input";
	private static final String type_att = "type";
	private static final String submit_val = "submit";
	private static final String id_att = "id";
	private static final String value_ending = "-value";
	private static final String all_elements = "*";
	private static final String value_att = "value";
	private static final String disabled_att = "disabled";
	private static final String bind_ending = "bind";
	private static final String span_tag = "span";
	private static final String class_att = "class";
	private static final String alert_val = "alert";
	private static final String invalid_val = "invalid";
	private static final String div_tag = "div";
	private static final String empty_str = "";
	private static final String refresh_butt_att_val = "refresh-button";
	
	
	public FormParser() { 	}
	
	/**
	 * <i>WARNING!</i> This document will be modified during parse phase.
	 * Make deep clone of it if don't want to lose it
	 * 
	 * @param form_document - document to fetch data to
	 */
	public void setHtmlForm(Document form_document) {
		
		if(form_document == null)
			throw new NullPointerException("form document cannot be null");
		
		this.form_document = form_document;
	}
	
	public void setXmlToFetch(Element xml_to_fetch) {
		
		if(xml_to_fetch == null)
			throw new NullPointerException("xml document cannot be null");
		
		this.xml_to_fetch = xml_to_fetch;
	}
	
	/**
	 * Currently supported outputs - OutputStream and Writer
	 * 
	 * @param output - data will be parsed to
	 */
	public void setOutput(Object output) {
		
		if(output == null)
			throw new NullPointerException("output cannot be null");
		
		if(output instanceof OutputStream) {
			output_stream = (OutputStream)output;
			output_writer = null;
			
		} else if(output instanceof Writer) {
			this.output_writer = (Writer)output;
			output_stream = null;
		} else
			throw new IllegalArgumentException("output type not supported, provided: "+output.getClass().getName());
	}
	
	public void parse() throws IOException {
		
		if(output_stream == null && output_writer == null)
			throw new NullPointerException("Output is not set");
		
		if(form_document == null)
			throw new NullPointerException("Html form document is not set");
		
		if(xml_to_fetch == null)
			throw new NullPointerException("Submitted XML document is not set");
		
		Element form_element = (Element)form_document.getElementsByTagName(form_tag).item(0);
		
//		remove submit button
		
		NodeList inputs = form_element.getElementsByTagName(input_tag);
		
		for (int i = 0; i < inputs.getLength(); i++) {
			
			Element input = (Element)inputs.item(i);
			
			String type_value = input.getAttribute(type_att); 
			
			if(type_value != null && type_value.equals(submit_val)) {
				
				Element input_container = (Element)input.getParentNode();
				input_container.getParentNode().removeChild(input_container);
				break;
			}
		}
		fetchData(form_element);
		removeErrorMessages(form_element);
		
		OutputFormat output_format = new OutputFormat();
		output_format.setOmitXMLDeclaration(true);
		
		XMLSerializer serializer;
		
		if(output_stream != null)
			serializer = new XMLSerializer(output_stream, output_format);
		else
			serializer = new XMLSerializer(output_writer, output_format);
		
		serializer.asDOMSerializer();
		serializer.serialize(form_element);
	}
	
	private void removeErrorMessages(Element form_element) {
		
		List<Element> refresh_buttons = BlockFormUtil.getElementsByAttributeValue(form_element, input_tag, class_att, refresh_butt_att_val);
		
		if(refresh_buttons != null) {
			
			for (Iterator<Element> iter = refresh_buttons.iterator(); iter.hasNext();) {
				Element refresh_button = iter.next();
				refresh_button.getParentNode().removeChild(refresh_button);
			}
		}
		
		List<Element> err_messages = BlockFormUtil.getElementsByAttributeValue(form_element, span_tag, class_att, alert_val);
		
		if(err_messages != null) {
			
			for (Iterator<Element> iter = err_messages.iterator(); iter.hasNext();) {
				Element element = iter.next();
				element.getParentNode().removeChild(element);
			}
		}
		
		err_messages = BlockFormUtil.getElementsByAttributeValueContained(form_element, div_tag, class_att, invalid_val);
		
		if(err_messages != null) {
			
			for (Iterator<Element> iter = err_messages.iterator(); iter.hasNext();) {
				Element element = iter.next();
				String class_attribute = element.getAttribute(class_att);
				
				element.setAttribute(class_att, class_attribute.replace(invalid_val, empty_str));
			}
		}
	}
	
	/**
	 * currently supports elements with value attribute (like text type)
	 * @param form_element - form element to fetch data to
	 */
	protected void fetchData(Element form_element) {
		
		List<Element> submitted_data_elements = DOMUtil.getChildElements(xml_to_fetch);
		
		for (Iterator<Element> iter = submitted_data_elements.iterator(); iter.hasNext();) {
			Element submitted_data_element = iter.next();
			
			String tag_name = submitted_data_element.getTagName();
			
			int bind_start = tag_name.lastIndexOf(bind_ending);
			
			if(bind_start < 0)
//				smth bad - all should contain bind at the end
				continue;
			
			String submitted_to_id = tag_name.substring(0, bind_start)+value_ending;
			Element input_element = DOMUtil.getElementByAttributeValue(form_element, all_elements, id_att, submitted_to_id);
			
			if(input_element.getTagName().equals("textarea")) {
				
				String submitted_data = DOMUtil.getTextNodeAsString(submitted_data_element);
				
				input_element.appendChild(
						input_element.getOwnerDocument().createTextNode(submitted_data)
				);
				
			} else {
				
				String submitted_data = DOMUtil.getTextNodeAsString(submitted_data_element);
				input_element.setAttribute(value_att, submitted_data);
				
			}
			
			
			input_element.setAttribute(disabled_att, disabled_att);
		}
	}
}