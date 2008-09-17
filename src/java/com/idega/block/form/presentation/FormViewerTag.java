/*
 * $Id: FormViewerTag.java,v 1.3 2008/09/17 13:07:52 civilis Exp $
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
	
	private String formId;

	@Override
	public String getComponentType() {
		return "FormViewer";
	}

	@Override
	public String getRendererType() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setProperties(UIComponent component) {
		super.setProperties(component);
		if (component != null) {
			component.getAttributes().put("formId", this.formId);     
		}
	}
	
	public String getFormId() {
		return formId;
	}

	
	public void setFormId(String uri) {
		this.formId = uri;
	}	
	
}
