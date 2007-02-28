/*
 * $Id: FormViewer.java,v 1.19 2007/02/28 08:56:50 civilis Exp $ Created on
 * Aug 17, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.chiba.adapter.ui.UIGenerator;
import org.chiba.adapter.ui.XSLTGenerator;
import org.chiba.web.WebAdapter;
import org.chiba.web.flux.FluxAdapter;
import org.chiba.web.servlet.HttpRequestHandler;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.XFormsEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xslt.TransformerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.business.FormsService;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;

/**
 * 
 * Last modified: $Date: 2007/02/28 08:56:50 $ by $Author: civilis $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.19 $
 */
public class FormViewer extends IWBaseComponent {

	protected static final Logger log = Logger.getLogger(FormViewer.class.getName());

	private String formId;
	private Document xforms_document;

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
		
		Document document = xforms_document;
		if(document == null) {
			
			String form_id = (String) context.getExternalContext().getRequestParameterMap().get("formId");
			if (form_id != null && !form_id.equals("")) {
				setFormId(form_id);
			} else {
				log.warning("formId not defined");
				return;
			}
			
			try {
				document = getFormsService(context).loadForm(getFormId());
				if (document == null) {
					log.warning("Could not load the form for id: " + getFormId());
					return;
				}
			} catch (IOException e) {
				log.log(Level.SEVERE, "Could not load the form for id: " + getFormId(), e);
				return;
			}
		}
		
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(true);

		WebAdapter adapter = new FluxAdapter();
		session.setAttribute(WebAdapter.WEB_ADAPTER, adapter);
		try {
			setupAdapter(adapter, document, context);
			adapter.init();
			
			EventTarget eventTarget = (EventTarget) ((Document)adapter.getXForms()).getDocumentElement();
			
			EventListener eventListener = new EventListener() {

				public void handleEvent(Event event) {
					String id = "";
			        if (event.getTarget() instanceof Element) {
			            id = ((Element) event.getTarget()).getAttribute("id");
			        }

					log.info("Got event, type=" + event.getType() + ", id=" + id);
					
				}};
			
			eventTarget.addEventListener(XFormsEventNames.SUBMIT_DONE, eventListener, true);
	        eventTarget.addEventListener(XFormsEventNames.SUBMIT_ERROR, eventListener, true);
			
			XMLEvent exitEvent = adapter.checkForExitEvent();
			if (exitEvent != null) {
				handleExit(exitEvent, session, request, response);
			}
			else {
				UIGenerator uiGenerator = createUIGenerator(context);
				adapter.setUIGenerator(uiGenerator);

				// store queryString as 'referer' in XFormsSession
				//xFormsSession.setProperty(XFormsSession.REFERER, request.getQueryString());
			}
		}
		catch (IOException e) {
			log.log(Level.WARNING, "handleExit failed", e);
			return;
		}
		catch (XFormsException e) {
			log.log(Level.WARNING, "Could not set XML container", e);
			shutdown(adapter, session);
			return;
		}
		
		try {
			IWContext iwc = IWContext.getIWContext(context);
			Web2Business business = (Web2Business) IBOLookup.getServiceInstance(iwc, Web2Business.class);
			
			Script s = new Script();
			s.addScriptSource(business.getBundleURIToPrototypeLib());
			s.addScriptSource(business.getBundleURIToScriptaculousLib());
			this.getChildren().add(s);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		if (getFormId() != null || xforms_document != null) {
//	        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//	        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
			// HttpSession session = request.getSession(true);
			HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
			WebAdapter webAdapter = null;
			try {
				webAdapter = (WebAdapter) session.getAttribute(WebAdapter.WEB_ADAPTER);
				if (webAdapter == null) {
					throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
				}
				
				UIGenerator uiGenerator = webAdapter.getUIGenerator();
				uiGenerator.setInput(webAdapter.getXForms());
				uiGenerator.setOutput(context.getResponseWriter());
				uiGenerator.generate();
	        } catch (Exception e) {
	        	log.log(Level.WARNING, "Error rendering form", e);
				shutdown(webAdapter, session);
			}
		}
		super.encodeEnd(context);
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[2];
		values[0] = super.saveState(ctx);
		values[1] = this.formId;
		return values;
	}

	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.formId = (String) values[1];
	}

	protected void handleExit(XMLEvent exitEvent, HttpSession session,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (ChibaEventNames.REPLACE_ALL.equals(exitEvent.getType())) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/SubmissionResponse"));
        } else if (ChibaEventNames.LOAD_URI.equals(exitEvent.getType())) {
			if (exitEvent.getContextInfo("show") != null) {
				String loadURI = (String) exitEvent.getContextInfo("uri");
			    session.removeAttribute(WebAdapter.WEB_ADAPTER);
				response.sendRedirect(response.encodeRedirectURL(loadURI));
			}
		}
		log.fine("Exited during XForms model init");
	}

	/**
	 * <p>
	 * TODO menesis describe method setupAdapter
	 * </p>
	 * @param adapter
	 * @param form
	 * @param xFormsSession
	 * @param context
	 * @return
	 * @throws XFormsException
	 */
	protected void setupAdapter(WebAdapter adapter, Document document, FacesContext context) throws XFormsException {
		adapter.setXForms(document);

		IWMainApplication app = IWMainApplication.getIWMainApplication(context);
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		adapter.setBaseURI(bundle.getResourcesVirtualPath());
		adapter.setUploadDestination(bundle.getBundleBaseRealPath() + "/upload");
	}

	protected UIGenerator createUIGenerator(FacesContext context) throws XFormsConfigException {
		TransformerService transformerService = (TransformerService) getIWMainApplication(context).getAttribute(
				IWBundleStarter.TRANSFORMER_SERVICE);
		XSLTGenerator generator = new XSLTGenerator();
		generator.setTransformerService(transformerService);
		generator.setStylesheetURI(IWBundleStarter.XSLT_URI);
		// todo: unify and extract parameter names
		generator.setParameter("contextroot", context.getExternalContext().getRequestContextPath());
		//if (session.getProperty(XFormsSession.KEEPALIVE_PULSE) != null) {
		//	generator.setParameter("keepalive-pulse", session.getProperty(XFormsSession.KEEPALIVE_PULSE));
		//}
        generator.setParameter("action-url", context.getExternalContext().encodeActionURL(context.getExternalContext().getRequestContextPath() + "/FluxHelper"));
		generator.setParameter("debug-enabled", false);
		String selectorPrefix = Config.getInstance().getProperty(HttpRequestHandler.SELECTOR_PREFIX_PROPERTY,
				HttpRequestHandler.SELECTOR_PREFIX_DEFAULT);
		generator.setParameter("selector-prefix", selectorPrefix);
		String removeUploadPrefix = Config.getInstance().getProperty(HttpRequestHandler.REMOVE_UPLOAD_PREFIX_PROPERTY,
				HttpRequestHandler.REMOVE_UPLOAD_PREFIX_DEFAULT);
		generator.setParameter("remove-upload-prefix", removeUploadPrefix);
		String dataPrefix = Config.getInstance().getProperty("chiba.web.dataPrefix");
		generator.setParameter("data-prefix", dataPrefix);
		String triggerPrefix = Config.getInstance().getProperty("chiba.web.triggerPrefix");
		generator.setParameter("trigger-prefix", triggerPrefix);
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		generator.setParameter("user-agent", request.getHeader("User-Agent"));
		generator.setParameter("scripted", true);
		generator.setParameter("scriptPath", "/idegaweb/bundles/" + IWBundleStarter.BUNDLE_IDENTIFIER + ".bundle/resources/javascript/");
		
		return generator;
	}

	protected void shutdown(WebAdapter webAdapter, HttpSession session) {
		// attempt to shutdown processor
		if (webAdapter != null) {
			try {
				webAdapter.shutdown();
			}
			catch (XFormsException xfe) {
				xfe.printStackTrace();
			}
		}
		
	    session.removeAttribute(WebAdapter.WEB_ADAPTER);

		// redirect to error page (after encoding session id if required)
		//response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/"
		//		+ request.getSession().getServletContext().getInitParameter("error.page")));
	}
	
	private FormsService getFormsService(FacesContext context) {
		FormsService service = null;
		try {
			IWApplicationContext iwc = IWContext.getIWContext(context);
			service = (FormsService) IBOLookup.getServiceInstance(iwc, FormsService.class);
		}
		catch (IBOLookupException e) {
			log.severe("Could not find FormsService");
		}
		return service;
	}

	public void setXFormsDocument(Document xforms_document) {
		this.xforms_document = xforms_document;
	}
}