/*
 * $Id: FormListViewer.java,v 1.3 2006/10/19 17:02:11 gediminas Exp $ Created on
 * 24.1.2005
 * 
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import com.idega.block.form.bean.FormBean;
import com.idega.business.IBOLookup;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.slide.business.IWSlideSession;

/**
 * <p>
 * Displays a list of available XForms
 * </p>
 * 
 * Last modified: $Date: 2006/10/19 17:02:11 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.3 $
 */
public class FormListViewer extends IWBaseComponent {

	private static final Logger log = Logger.getLogger(FormListViewer.class.getName());

	protected static final String FORMS_PATH = "/files/forms";
	protected static final String FORMS_FILE_EXTENSION = ".xhtml";
	
	private String detailsViewerPath = "#";
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.IWBaseComponent#initializeComponent(javax.faces.context.FacesContext)
	 */
	protected void initializeComponent(FacesContext context) {
		Lists ul = new Lists();
		
		List<FormBean> forms = getForms();
		for (FormBean f : forms) {
			ListItem li = new ListItem();
			Link a = new Link(f.getName());
			a.setURL(getDetailsViewerPath());
			a.addParameter("resourcePath", f.getResourcePath());
			li.add(a);
			ul.add(li);
		}
		
		getChildren().add(ul);
	}

	public List<FormBean> getForms() {
		List<FormBean> list = new ArrayList();
		try {			
			IWContext iwc = IWContext.getInstance();
			IWSlideSession session = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
			WebdavResource root = session.getWebdavResource(FORMS_PATH);
			for (WebdavResource folder : root.listWebdavResources()) {
				if (folder.isCollection()) {
					String formId = folder.getDisplayName();
					WebdavResources rs = folder.getChildResources();
					WebdavResource r = rs.getResource(folder.getName() + "/" + formId + FORMS_FILE_EXTENSION);
					if (r == null) {
						continue;
					}
					
					// need to load for title
					FormBean form = new FormBean();
					form.setResourcePath(r.getPath());
					form.load();
					
					list.add(form);
				}
			}
		}
		catch (IOException e) {
			log.log(Level.WARNING, "Error listing forms", e);
		}
		return list;
	}

	
	public String getDetailsViewerPath() {
		return this.detailsViewerPath;
	}

	public void setDetailsViewerPath(String detailsViewerPath) {
		this.detailsViewerPath = detailsViewerPath;
	}

}
