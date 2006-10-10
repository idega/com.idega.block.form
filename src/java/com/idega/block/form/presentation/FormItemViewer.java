/*
 * $Id: FormItemViewer.java,v 1.1 2006/10/10 15:58:11 gediminas Exp $ Created on Aug
 * 17, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.el.ValueBinding;
import com.idega.block.form.bean.FormBean;
import com.idega.content.bean.ContentItem;
import com.idega.content.bean.ContentItemBean;
import com.idega.content.presentation.ContentItemViewer;

/**
 * 
 * Last modified: $Date: 2006/10/10 15:58:11 $ by $Name:  $
 * 
 * @name <a href="mailto:gediminas@idega.com">gediminas</a>
 * @version $Revision: 1.1 $
 */
public class FormItemViewer extends ContentItemViewer {
	private static final Logger log = Logger.getLogger(FormItemViewer.class.getName());

	private final static String ATTRIBUTE_NAME = "name";
	private final static String[] ATTRIBUTE_ARRAY = new String[] { ATTRIBUTE_NAME };

	private String name;
	private String description;

	protected UIComponent createFieldComponent(String attribute) {
		if (attribute.equals("name")) {
			UIComponent link = getEmptyMoreLink();
			HtmlOutputText text = new HtmlOutputText();
			link.getChildren().add(text);
			return link;
		}
		return super.createFieldComponent(attribute);
	}

	public String[] getViewerFieldNames(){
		return ATTRIBUTE_ARRAY;
	}
	
	public ContentItem loadContentItem(String itemResourcePath) {
		try {
			FormBean bean = new FormBean();
			bean.setResourcePath(itemResourcePath);
			log.info("Loading form bean from " + itemResourcePath);
			bean.load();
			return bean;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		log.entering("FormItemViewer", "getName");
		if (this.name != null) {
			return this.name;
		}
		ContentItem item = getContentItem();
		if (item != null) {
			return ((ContentItemBean)item).getName();
		}
		ValueBinding vb = getValueBinding("name");
		return vb != null ? (String) vb.getValue(getFacesContext()) : null;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		log.entering("FormItemViewer", "getDescription");
		if (this.description != null) {
			return this.description;
		}
		ContentItem item = getContentItem();
		if (item != null) {
			return ((ContentItemBean)item).getDescription();
		}
		ValueBinding vb = getValueBinding("description");
		return vb != null ? (String) vb.getValue(getFacesContext()) : null;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}


}
