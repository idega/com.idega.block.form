/*
 * $Id: FormViewer.java,v 1.3 2006/10/10 15:47:48 gediminas Exp $ Created on Aug
 * 17, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.chiba.adapter.ui.XSLTGenerator;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xslt.TransformerService;
import org.w3c.dom.Document;
import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.bean.FormBean;
import com.idega.content.bean.ManagedContentBeans;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFUtil;

/**
 * 
 * Last modified: $Date: 2006/10/10 15:47:48 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">gediminas</a>
 * @version $Revision: 1.3 $
 */
public class FormViewer extends IWBaseComponent implements ManagedContentBeans {

	private static final Logger log = Logger.getLogger(FormViewer.class.getName());

	private static final String BUNDLE_IDENTIFIER = "com.idega.block.form";

	// private static String RENDERER_TYPE = "xforms";
	private static final URI XSLT_URI = URI.create("bundle://" + BUNDLE_IDENTIFIER + "/resources/xslt/html4.xsl");

	private ChibaBean chiba;

	private XSLTGenerator uiGenerator;

	private String resourcePath;

	public FormViewer() {
		super();
	}

	public String getRendererType() {
		return null;
		// return RENDERER_TYPE;
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		TransformerService transformerService = (TransformerService) getIWMainApplication(context).getAttribute(IWBundleStarter.TRANSFORMER_SERVICE);
		uiGenerator = new XSLTGenerator();
		uiGenerator.setTransformerService(transformerService);
		uiGenerator.setStylesheetURI(XSLT_URI);
	}

	@Override
	public void encodeEnd(FacesContext ctx) throws IOException {
		ResponseWriter out = ctx.getResponseWriter();
		
		FormBean form = (FormBean) WFUtil.getBeanInstance(FORM_ITEM_BEAN_ID);
		
		if (form.getResourcePath() == null) {
			log.fine("form.resourcePath is null");
			form.setResourcePath(getResourcePath());			
			super.encodeEnd(ctx);
			return;
		}
		try {
			form.load();
			Document doc = form.getDocument();
			
			chiba = new ChibaBean();
			chiba.setXMLContainer(doc);
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
			uiGenerator.setInput(chiba.getXMLContainer());
			uiGenerator.setOutput(out);
			uiGenerator.generate();
		}
		catch (XFormsException e) {
			log.log(Level.WARNING, "Could not generate HTML from XForms document", e);
		}
		super.encodeEnd(ctx);
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = this.resourcePath;
		return values;
	}

	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.resourcePath = (String) values[1];
	}

}
