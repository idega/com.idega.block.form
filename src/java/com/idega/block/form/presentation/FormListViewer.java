/*
 * $Id: FormListViewer.java,v 1.7 2006/12/08 15:37:41 gediminas Exp $ Created on
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
import javax.faces.model.SelectItem;
import com.idega.block.form.bean.FormList;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.webface.WFUtil;

/**
 * <p>
 * Displays a list of available XForms
 * </p>
 * 
 * Last modified: $Date: 2006/12/08 15:37:41 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.7 $
 */
public class FormListViewer extends IWBaseComponent {

	private String detailsViewerPath = "#";
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.IWBaseComponent#initializeComponent(javax.faces.context.FacesContext)
	 */
	protected void initializeComponent(FacesContext context) {
		Lists ul = new Lists();

		FormList bean = (FormList) WFUtil.getBeanInstance("formList");
		List<SelectItem> forms = bean.getForms();
		
		for (SelectItem f : forms) {
			ListItem li = new ListItem();
			Link a = new Link(f.getLabel());
			a.setURL(getDetailsViewerPath());
			a.addParameter("formId", (String) f.getValue());
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

	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = this.detailsViewerPath;
		return values;
	}

	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.detailsViewerPath = (String) values[1];
	}

}
