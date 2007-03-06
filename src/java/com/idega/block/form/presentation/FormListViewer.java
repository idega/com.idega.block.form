/*
 * $Id: FormListViewer.java,v 1.9 2007/03/06 08:58:49 civilis Exp $ Created on
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.idega.block.form.bean.FormList;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.builder.business.BuilderService;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.webface.WFUtil;

/**
 * <p>
 * Displays a list of available XForms
 * </p>
 * 
 * Last modified: $Date: 2007/03/06 08:58:49 $ by $Author: civilis $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.9 $
 */
public class FormListViewer extends IWBaseComponent {

	private BuilderService builder_service;
	private String detailsViewerPath;
	private static Log logger = LogFactory.getLog(FormListViewer.class);
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.IWBaseComponent#initializeComponent(javax.faces.context.FacesContext)
	 */
	protected void initializeComponent(FacesContext context) {
		
		Lists ul = new Lists();

		try {
			
			FormList bean = (FormList) WFUtil.getBeanInstance("formList");
			List<SelectItem> forms = bean.getForms();
			IWContext iw_ctx = IWContext.getInstance();
			
			for (SelectItem f : forms) {
				ListItem li = new ListItem();
				Link a = new Link(f.getLabel());
				a.setURL(getDetailsViewerPath(iw_ctx));
				a.addParameter("formId", (String) f.getValue());
				li.add(a);
				ul.add(li);
			}
			
		} catch (Exception e) {
			logger.error("Error while loading form list", e);
		}
		
		getChildren().add(ul);
	}

	public String getDetailsViewerPath(IWContext iw_ctx) throws Exception {
		
		return detailsViewerPath != null ? detailsViewerPath : getBuilderService() == null ? null : getBuilderService().getCurrentPageURI(iw_ctx);
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

	protected BuilderService getBuilderService() {

		if(builder_service == null) {
			try {
				IWApplicationContext iwc = IWMainApplication.getDefaultIWApplicationContext();
				builder_service = (BuilderService)IBOLookup.getServiceInstance(iwc, BuilderService.class);
			} catch (IBOLookupException e) {
				logger.error("BuilderService could not be found", e);
			}
		}
		return builder_service;
	}
}
