/*
 * $Id: FormBean.java,v 1.5 2006/10/19 17:07:20 gediminas Exp $
 * Created on Aug 22, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.bean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.util.WebdavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * <p>
 * A form document which loads itself from slide and parses the XML
 * </p>
 *  Last modified: $Date: 2006/10/19 17:07:20 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.5 $
 */
public class FormBean implements Serializable {

	private static final long serialVersionUID = -3316649519515911534L;

	private static final Logger log = Logger.getLogger(FormBean.class.getName());

	private String resourcePath;

	private String name;

	private Document document;

	private boolean loaded = false;

	public FormBean() {
		// nothing
	}

	public FormBean(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/**
	 * Clears all attributes for this bean.
	 */
	public void clear() {
		this.loaded = false;
		this.resourcePath = null;
		this.name = null;
		this.document = null;
	}

	/**
	 * <p>
	 * Loads this resource from the folder set by setResourcepath();
	 * </p>
	 * 
	 * @throws IOException
	 * @throws Exception
	 *             If there is an exception loading
	 */
	public void load() throws IOException {
		if (!isLoaded()) {
			String resourcePath = getResourcePath();
			if (resourcePath == null) {
				throw new FileNotFoundException("Error loading content Item. No resourcePath set");
			}
			boolean loaded = load(resourcePath);
			setLoaded(loaded);
		}
	}

	public void reload() throws IOException {
		setLoaded(false);
		load();
	}

	/**
	 * Loads all xml files in the given folder
	 * 
	 * @param folder
	 * @return List containing ArticleItemBean
	 * @throws IOException
	 * @throws XmlException
	 * @throws IOException
	 */
	protected boolean load(String path) throws IOException {
		// System.out.print("["+this.toString()+"]:");
		// System.out.println("Attempting to load path "+path);
		clear();
		IWUserContext iwuc = IWContext.getInstance();
		boolean returner = true;
		try {
			IWSlideSession session = getIWSlideSession(iwuc);
			WebdavExtendedResource webdavResource = session.getWebdavResource(path);
			webdavResource.setProperties();
			// here I don't use the varible 'path' since it can actually be the
			// URI
			setResourcePath(webdavResource.getPath());
			setName(webdavResource.getDisplayName());
			returner = load(webdavResource);
		}
		catch (HttpException e) {
			if (e.getReasonCode() == WebdavStatus.SC_NOT_FOUND) {
				return false;
			}
			else {
				throw e;
			}
		}
		return returner;
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
			 * TODO split the xml into parts - description, schema, localized
			 * strings, xf:model, xf:form?
			 */
			String name = null;
			Node title = doc.getElementsByTagName("title").item(0);
			if (title != null) {
				Node child = title.getFirstChild();
				if (child != null && child.getNodeType() == Node.TEXT_NODE) {
					name = child.getNodeValue().trim();
				}
			}
			if (name == null || name.equals("")) {
				name = webdavResource.getDisplayName();
			}
			setName(name);
			setDocument(doc);
		}
		else {
			// article not found
			log.warning("Form xml file was not found");
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
	 * @return Returns the name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
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

	protected IWSlideSession getIWSlideSession(IWUserContext iwuc) {
		IWSlideSession session = null;
		try {
			session = (IWSlideSession) IBOLookup.getSessionInstance(iwuc, IWSlideSession.class);
		}
		catch (IBOLookupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}

	/**
	 * @return Returns the loaded.
	 */
	public boolean isLoaded() {
		return this.loaded;
	}

	/**
	 * @param loaded
	 *            The loaded to set.
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	/**
	 * @return Returns the resourcePath.
	 */
	public String getResourcePath() {
		return this.resourcePath;
	}

	/**
	 * @param resourcePath
	 *            The resourcePath to set.
	 */
	public void setResourcePath(String resourcePath) {
		if (this.resourcePath != null && !this.resourcePath.equals(resourcePath)) {
			clear();
		}
		this.resourcePath = resourcePath;
	}
}
