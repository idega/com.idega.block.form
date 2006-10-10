/*
 * $Id: FormBean.java,v 1.3 2006/10/10 15:29:16 gediminas Exp $ Created on Aug
 * 22, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.idega.content.bean.ContentItemBean;
import com.idega.slide.util.WebdavExtendedResource;

public class FormBean extends ContentItemBean {

	private static final long serialVersionUID = -3316649519515911534L;

	private static final Logger log = Logger.getLogger(FormBean.class.getName());

	private Document document;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.content.bean.ContentItemBean#getContentFieldNames()
	 */
	public String[] getContentFieldNames() {
		// no content fields
		return new String[] { "name" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.content.bean.ContentItem#getContentItemPrefix()
	 */
	public String getContentItemPrefix() {
		return "xforms_";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.content.bean.ContentItemBean#getToolbarActions()
	 */
	public String[] getToolbarActions() {
		// no toolbar
		return new String[0];
	}

	/**
	 * Loads an xml file specified by the webdav resource The beans atributes
	 * are then set according to the information in the XML file
	 */
	protected boolean load(WebdavExtendedResource webdavResource) throws IOException {
		Document doc = null;
		try {
			InputStream inStream = webdavResource.getMethodData();
			doc = getDocumentBuilder().parse(inStream);
		}
		catch (SAXException e) {
			log.warning("Could not parse xforms document");
		}
		catch (ParserConfigurationException e) {
			log.config("Could not create Xerces document builder");
		}
		if (doc != null) {
			/*
			 * TODO split the xml into parts - description, schema, localized strings,
			 * xf:model, xf:form?
			 */
			Node title = doc.getElementsByTagName("title").item(0);
			if (title != null) {
				String name = null;
				if (title.getNodeType() == Node.TEXT_NODE) {
					name = title.getNodeValue();
				}
				else {
					name = title.getFirstChild().getNodeValue();
				}
				setName(name);
			}
			setDocument(doc);
		}
		else {
			// article not found
			log.warning("Form xml file was not found");
			setRendered(false);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.content.bean.ContentItem#store()
	 */
	public void store() throws Exception {
		// TODO Auto-generated method stub
		throw new RuntimeException("store() not implemented for FormBean");
	}

	/**
	 * @return Returns the document.
	 */
	public Document getDocument() {
		return this.document;
	}

	/**
	 * @param document
	 *            The document to set.
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

	private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		// ensure xerces dom
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		factory.setAttribute("http://apache.org/xml/properties/dom/document-class-name",
				"org.apache.xerces.dom.DocumentImpl");
		return factory.newDocumentBuilder();
	}
}
