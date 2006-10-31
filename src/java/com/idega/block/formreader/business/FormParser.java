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

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 */
public class FormParser {
	
	private Document form_document;
	private OutputStream output_stream;
	private Writer output_writer;
	private Element xml_to_fetch;
	
	private static final String body_tag = "body";
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
	
	private FormParser() { 	}
	
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
		
		Element body_element = (Element)form_document.getElementsByTagName(body_tag).item(0);
		
		Element form_element = (Element)DOMUtil.getFirstChildByTagName(body_element, form_tag);
		
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
		
		OutputFormat output_format = new OutputFormat();
		XMLSerializer serializer;
		
		if(output_stream != null)
			serializer = new XMLSerializer(output_stream, output_format);
		else
			serializer = new XMLSerializer(output_writer, output_format);
		
		serializer.serialize(form_element);
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
	
	public static FormParser getInstance() {
		
		return new FormParser();
	}
}