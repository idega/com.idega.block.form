/*
 * $Id: FormViewer.java,v 1.2 2006/09/27 10:01:44 gediminas Exp $ Created on Aug
 * 17, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.exception.XFormsException;
import com.idega.block.form.business.FBXSLTGenerator;
import com.idega.block.form.business.TransformerCache;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;

/**
 * 
 * Last modified: $Date: 2006/09/27 10:01:44 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">gediminas</a>
 * @version $Revision: 1.2 $
 */
public class FormViewer extends IWBaseComponent {

	private static final Logger log = Logger.getLogger(FormViewer.class.getName());

	private static final String BUNDLE_IDENTIFIER = "com.idega.block.form";
	//private static String RENDERER_TYPE = "xforms";
	
	private static final URI XSLT_URI = URI.create("bundle://" + BUNDLE_IDENTIFIER + "/resources/xslt/html4.xsl");

	private ChibaBean chiba;
	
	private String uri;
	private FBXSLTGenerator uiGenerator;

	public FormViewer() {
		super();
	}
	
	public String getRendererType() {
		return null;
		//return RENDERER_TYPE;
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		uiGenerator = new FBXSLTGenerator(TransformerCache.getInstance(context));
		uiGenerator.setStylesheetURI(XSLT_URI);
	}

	@Override
	public void encodeEnd(FacesContext ctx) throws IOException {
		ResponseWriter out = ctx.getResponseWriter();
		
		if (uri == null) {
			super.encodeEnd(ctx);
			return;
		}

		try {
			InputStream stream = getIWSlideSession().getInputStream(uri);
			chiba = new ChibaBean();
			chiba.setXMLContainer(stream);
			chiba.init();
		}
		catch (RemoteException e) {
			log.log(Level.WARNING, "Error reading document from Slide", e);
			return;
		}
		catch (IOException e) {
			log.log(Level.WARNING, "Error reading document from Slide", e);
			return;
		}
		catch (XFormsException e) {
			log.log(Level.WARNING, "Could not set XML container", e);
			return;
		}
				
		try {
			uiGenerator.setInputNode(chiba.getXMLContainer());
			uiGenerator.setOutput(out);
			uiGenerator.generate();
		}
		catch (XFormsException e) {
			log.log(Level.WARNING, "Could not generate HTML from XForms document", e);
		}

		super.encodeEnd(ctx);
	}

	protected IWSlideSession getIWSlideSession() {
		IWUserContext iwuc = IWContext.getInstance();
		IWSlideSession session = null;
		try {
			session = (IWSlideSession) IBOLookup.getSessionInstance(iwuc, IWSlideSession.class);
		}
		catch (IBOLookupException e) {
			log.log(Level.SEVERE, "Error getting Slide session", e);
		}
		return session;
	}
	
    public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
