/*
 * $Id: FormViewerTag.java,v 1.1 2006/09/08 14:03:14 gediminas Exp $
 * Created on Aug 17, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.form.presentation;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;


public class FormViewerTag extends UIComponentTag {
	
	private String uri;

	@Override
	public String getComponentType() {
		return "FormViewer";
	}

	@Override
	public String getRendererType() {
		return null;
	}

	@Override
	protected void setProperties(UIComponent component) {
		super.setProperties(component);
		if (component != null) {
			component.getAttributes().put("uri", this.uri);     
		}
	}
	
	public String getUri() {
		return uri;
	}

	
	public void setUri(String uri) {
		this.uri = uri;
	}	
	
}
