/*
 * $Id: FormBean.java,v 1.8 2006/11/10 09:29:58 civilis Exp $
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
import java.util.Locale;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.util.WebdavStatus;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.formbuilder.business.form.beans.LocalizedStringBean;
import com.idega.formbuilder.business.form.manager.util.FormManagerUtil;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * <p>
 * A form document which loads itself from slide and parses the XML
 * </p>
 *  Last modified: $Date: 2006/11/10 09:29:58 $ by $Author: civilis $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.8 $
 */
public class FormBean implements Serializable {

	private static final long serialVersionUID = -3316649519515911534L;

	private static final Logger log = Logger.getLogger(FormBean.class.getName());

	public static final String FORMS_PATH = "/files/forms";
	public static final String FORMS_FILE_EXTENSION = ".xhtml";

	private String formId;

	private String name;

	private Document form_xforms;

	private boolean loaded = false;

	public FormBean() {
		// nothing
	}

	public FormBean(String formId) {
		this.formId = formId;
	}

	/**
	 * Clears all attributes for this bean.
	 */
	public void clear() {
		this.loaded = false;
		this.formId = null;
		this.name = null;
		this.form_xforms = null;
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
			if (getFormId() == null) {
				throw new FileNotFoundException("Error loading content Item. No formId set");
			}
			String resourcePath = getResourcePath();
			log.info("Loading form from " + resourcePath);
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
		IWUserContext iwuc = IWContext.getInstance();
		boolean returner = true;
		try {
			IWSlideSession session = getIWSlideSession(iwuc);
			WebdavExtendedResource webdavResource = session.getWebdavResource(path);
			webdavResource.setProperties();
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
			
			setDocument(doc);
			loadName();
		}
		else {
			// article not found
			log.warning("Form xml file was not found");
			return false;
		}
		return true;
	}
	
	/**
	 * load form title from form document using default form locale
	 * 
	 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
	 * @version 1.0
	 */
	protected void loadName() {

		Document form_xforms = getDocument();
		
		if(form_xforms == null)
			throw new NullPointerException("Form document is not loaded");
		
		LocalizedStringBean title = FormManagerUtil.getTitleLocalizedStrings(form_xforms);
		
		if(title == null) {

			log.warning("Could not find form title. Setting empty string.");
			setName("");
			return;
		}
		
		Locale default_form_locale = FormManagerUtil.getDefaultFormLocale(form_xforms);
		
		if(default_form_locale == null)
			default_form_locale = new Locale("en");
		
		String default_title = title.getString(default_form_locale);
		
		if(default_title == null) {

			log.warning("Could not find form title by default locale: "+default_form_locale.getLanguage()+". Setting empty string.");
			default_title = "";
		}
		
		setName(default_title);
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
		return this.form_xforms;
	}

	/**
	 * @param document
	 *            The document to set.
	 */
	public void setDocument(Document document) {
		this.form_xforms = document;
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
	 * @return Returns the formId.
	 */
	public String getFormId() {
		return this.formId;
	}

	/**
	 * @param formId
	 *            The formId to set.
	 */
	public void setFormId(String formId) {
		if (this.formId != null && !this.formId.equals(formId)) {
			clear();
		}
		this.formId = formId;
	}
	
	/**
	 * Constructs a path from bean's formId
	 * @return
	 */
	public String getResourcePath() {
		return getResourcePath(getFormId());
	}

	/**
	 * Constructs a path from given formId
	 * @return A string like /files/forms/f1/f1.xhtml
	 */
	public static String getResourcePath(String formId) {
		return FORMS_PATH + "/" + formId + "/" + formId + FORMS_FILE_EXTENSION;
	}

}
