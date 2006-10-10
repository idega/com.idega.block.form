/*
 * $Id: FormListManagedBean.java,v 1.1 2006/10/10 15:29:16 gediminas Exp $
 * Created on 27.9.2006
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.form.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import org.apache.webdav.lib.WebdavResource;
import com.idega.block.form.presentation.FormItemViewer;
import com.idega.business.IBOLookup;
import com.idega.content.bean.ContentListViewerManagedBean;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;

/**
 * 
 *  Last modified: $Date: 2006/10/10 15:29:16 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.1 $
 */
public class FormListManagedBean implements ContentListViewerManagedBean {
	
	private static final Logger log = Logger.getLogger(FormListManagedBean.class.getName());

	private String detailsViewerPath;
	private String resourcePath;
	
	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentItems()
	 */
	public List getContentItems() {
		List list = new ArrayList();
		try {			
			IWContext iwc = IWContext.getInstance();
			IWSlideSession session = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
			WebdavResource root = session.getWebdavResource(getBaseFolderPath());
			WebdavResource[] resources = root.listWebdavResources();
			for (WebdavResource r : resources) {
				FormBean form = new FormBean();
				form.setResourcePath(r.getPath());
				log.info("Loading form bean from " + r.getPath());
				form.load();
				list.add(form);
			}
		}
		catch (IOException e) {
			log.throwing(FormListManagedBean.class.getName(), "getContentItems", e);
		}
		return list;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getContentViewer()
	 */
	public ContentItemViewer getContentViewer() {
		FormItemViewer viewer = new FormItemViewer();		

		if(this.detailsViewerPath != null){
			viewer.setDetailsViewerPath(this.detailsViewerPath);
			HtmlOutputLink moreLink = viewer.getEmptyMoreLink();
			HtmlOutputText moreText = new HtmlOutputText();
			moreText.setValue("view form...");
			moreLink.getChildren().add(moreText);
			viewer.setDetailsCommand(moreLink);
		}
		
		return viewer;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getAttachmentViewers()
	 */
	public List getAttachmentViewers() {
		return null;
	}

	/**
	 * @return Returns the detailsViewerPath.
	 */
	public String getDetailsViewerPath() {
		return this.detailsViewerPath;
	}

	/**
	 * @param path the detailsViewerPath.
	 */
	public void setDetailsViewerPath(String path) {
		this.detailsViewerPath = path;
	}
	
	/**
	 * @param categories The categories to set.
	 */
	public void setCategories(List categories) {
		// no categories for forms
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getIWActionURIHandlerIdentifier()
	 */
	public String getIWActionURIHandlerIdentifier() {
		// use default
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#setResourcePath(java.lang.String)
	 */
	public void setBaseFolderPath(String path) {
		this.resourcePath=path;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getResourcePath()
	 */
	public String getBaseFolderPath() {
		return this.resourcePath;
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#setMaxNumberOfDisplayed(int)
	 */
	public void setMaxNumberOfDisplayed(int maxItems) {
		// ignore, always displays all
	}

	/* (non-Javadoc)
	 * @see com.idega.content.bean.ContentListViewerManagedBean#getMaxNumberOfDisplayed()
	 */
	public int getMaxNumberOfDisplayed() {
		return -1;
	}

}
