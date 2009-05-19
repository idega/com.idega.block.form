/*
 * $Id: FormViewerTag.java,v 1.4 2009/05/19 07:22:58 valdas Exp $
 * Created on Aug 17, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.form.presentation;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;


public class FormViewerTag extends UIComponentELTag {
	
	private String formId;

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
