/*
 * $Id: FormListViewer.java,v 1.6 2006/11/28 18:27:42 laddi Exp $ Created on
 * 24.1.2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.util.List;

import javax.faces.context.FacesContext;

import com.idega.block.form.business.AvailableFormBean;
import com.idega.block.form.business.AvailableFormsLister;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;

/**
 * <p>
 * Displays a list of available XForms
 * </p>
 * 
 * Last modified: $Date: 2006/11/28 18:27:42 $ by $Author: laddi $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.6 $
 */
public class FormListViewer extends IWBaseComponent {

	//private static final Logger log = Logger.getLogger(FormListViewer.class.getName());

	private String detailsViewerPath = "#";
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.IWBaseComponent#initializeComponent(javax.faces.context.FacesContext)
	 */
	protected void initializeComponent(FacesContext context) {
		Lists ul = new Lists();
		
		List<AvailableFormBean> forms = AvailableFormsLister.getInstance().getAvailableForms();
		for (AvailableFormBean f : forms) {
			ListItem li = new ListItem();
			Link a = new Link(f.getFormTitle());
			a.setURL(getDetailsViewerPath());
			a.addParameter("formId", f.getId());
			li.add(a);
			ul.add(li);
		}
		
		getChildren().add(ul);
	}

	public String getDetailsViewerPath() {
		return this.detailsViewerPath;
	}

	public void setDetailsViewerPath(String detailsViewerPath) {
		this.detailsViewerPath = detailsViewerPath;
	}

}
