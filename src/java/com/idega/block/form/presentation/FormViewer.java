/*
 * $Id: FormViewer.java,v 1.23 2007/04/06 20:13:00 civilis Exp $ Created on
 * Aug 17, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.chiba.adapter.ui.UIGenerator;
import org.chiba.adapter.ui.XSLTGenerator;
import org.chiba.web.IWBundleStarter;
import org.chiba.web.WebAdapter;
import org.chiba.web.flux.FluxAdapter;
import org.chiba.web.servlet.HttpRequestHandler;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;
import org.chiba.web.session.impl.DefaultXFormsSessionManagerImpl;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.XFormsEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xslt.TransformerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.webface.WFUtil;

/**
 * 
 * Last modified: $Date: 2007/04/06 20:13:00 $ by $Author: civilis $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.23 $
 */
public class FormViewer extends IWBaseComponent {

	protected static final Logger log = Logger.getLogger(FormViewer.class.getName());

	private String formId;
	private Document xforms_document;
	private String sessionKey;

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
		HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
		
		if(document == null) {
			
			if(getFormId() == null) {
				
				String form_id = (String) context.getExternalContext().getRequestParameterMap().get("formId");
				if (form_id != null && !form_id.equals("")) {
					setFormId(form_id);
				} else {
					log.warning("formId not defined");
					return;
				}
			}
			
			PersistenceManager persistence_manager = (PersistenceManager) WFUtil.getBeanInstance("persistenceManager");
			document = persistence_manager.loadFormNoLock(getFormId());
			
			if (document == null) {
				log.warning("Could not load the form for id: " + getFormId());
				return;
			}
		}
		
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
		
		XFormsSessionManager session_manager = getXFormsSessionManager();
		XFormsSession xforms_session = session_manager.createXFormsSession();
		/*
        the XFormsSessionManager is kept in the http-session though it is accessible as singleton. Subsequent
        servlets should access the manager through the http-session attribute as below to ensure the http-session
        is refreshed.
		 */
		session.setAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER, session_manager);

		WebAdapter adapter = new FluxAdapter();
		session.setAttribute(WebAdapter.WEB_ADAPTER, adapter);
		try {
			setupAdapter(adapter, document, xforms_session, context);
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
				handleExit(exitEvent, xforms_session, session, request, response);
			} else {
				
				UIGenerator uiGenerator = createUIGenerator(context, request, xforms_session);
				// store WebAdapter in XFormsSession
				xforms_session.setAdapter(adapter);
				// store UIGenerator in XFormsSession as property
				xforms_session.setProperty(XFormsSession.UIGENERATOR, uiGenerator);
				// store queryString as 'referer' in XFormsSession
				xforms_session.setProperty(XFormsSession.REFERER, request.getQueryString());
				// actually add the XFormsSession ot the manager
				session_manager.addXFormsSession(xforms_session);
				setSessionKey(xforms_session.getKey());

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
			shutdown(adapter, session, xforms_session.getKey());
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

			HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
			WebAdapter webAdapter = null;
			try {
				XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
				XFormsSession xFormsSession = manager.getXFormsSession(getSessionKey());
				webAdapter = xFormsSession.getAdapter();
				
				if (webAdapter == null)
					throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
				
				UIGenerator uiGenerator = (UIGenerator) xFormsSession.getProperty(XFormsSession.UIGENERATOR);
				uiGenerator.setInput(webAdapter.getXForms());
				uiGenerator.setOutput(context.getResponseWriter());
				uiGenerator.generate();
				
	        } catch (Exception e) {
	        	log.log(Level.WARNING, "Error rendering form", e);
	        	shutdown(webAdapter, session, getSessionKey());
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
		Object values[] = new Object[3];
		values[0] = super.saveState(ctx);
		values[1] = formId;
		values[2] = sessionKey;
		
		return values;
	}

	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		formId = (String) values[1];
		sessionKey = (String) values[2];
	}

	protected void handleExit(XMLEvent exitEvent, XFormsSession xFormsSession, HttpSession session,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (ChibaEventNames.REPLACE_ALL.equals(exitEvent.getType())) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/SubmissionResponse?sessionKey=" + xFormsSession.getKey()));
        } else if (ChibaEventNames.LOAD_URI.equals(exitEvent.getType())) {
			if (exitEvent.getContextInfo("show") != null) {
				String loadURI = (String) exitEvent.getContextInfo("uri");
				// kill XFormsSession
				xFormsSession.getManager().deleteXFormsSession(xFormsSession.getKey());
				setSessionKey(null);
				response.sendRedirect(response.encodeRedirectURL(loadURI));
			}
		}
		log.fine("Exited during XForms model init");
	}

	protected void setupAdapter(WebAdapter adapter, Document document, XFormsSession xforms_session, FacesContext context) throws XFormsException {
		adapter.setXFormsSession(xforms_session);
		adapter.setXForms(document);
		
		Map servletMap = new HashMap();
		servletMap.put(WebAdapter.SESSION_ID, xforms_session.getKey());
		adapter.setContextParam(Submission.SUBMISSION, servletMap);

		IWMainApplication app = IWMainApplication.getIWMainApplication(context);
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		adapter.setBaseURI(bundle.getResourcesVirtualPath());
		adapter.setUploadDestination(bundle.getBundleBaseRealPath() + "/upload");
		// storeCookies(request, adapter);
	}

	protected UIGenerator createUIGenerator(FacesContext context, HttpServletRequest request, XFormsSession session) throws XFormsConfigException {
		TransformerService transformerService = (TransformerService) getIWMainApplication(context).getAttribute(
				IWBundleStarter.TRANSFORMER_SERVICE);
		XSLTGenerator generator = new XSLTGenerator();
		generator.setTransformerService(transformerService);
		generator.setStylesheetURI(IWBundleStarter.XSLT_URI);
		// todo: unify and extract parameter names
		generator.setParameter("contextroot", context.getExternalContext().getRequestContextPath());
		generator.setParameter("sessionKey", session.getKey());
		if (session.getProperty(XFormsSession.KEEPALIVE_PULSE) != null) {
			generator.setParameter("keepalive-pulse", session.getProperty(XFormsSession.KEEPALIVE_PULSE));
		}
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
		generator.setParameter("user-agent", request.getHeader("User-Agent"));
		generator.setParameter("scripted", true);
		generator.setParameter("scriptPath", "/idegaweb/bundles/" + IWBundleStarter.BUNDLE_IDENTIFIER + ".bundle/resources/javascript/");
		
		return generator;
	}

	protected void shutdown(WebAdapter webAdapter, HttpSession session, String key) {
		// attempt to shutdown processor
		if (webAdapter != null) {
			try {
				webAdapter.shutdown();
			}
			catch (XFormsException xfe) {
				xfe.printStackTrace();
			}
		}
		// remove xformssession from httpsession
		if (key != null) {
			session.removeAttribute(key);
		}
		setSessionKey(null);
		// redirect to error page (after encoding session id if required)
		//response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/"
		//		+ request.getSession().getServletContext().getInitParameter("error.page")));
	}

	public void setXFormsDocument(Document xforms_document) {
		this.xforms_document = xforms_document;
	}
	
	/**
     * returns a specific implementation of XFormsSessionManager. Plugin your own implementations here if needed.
	 * 
     * @return a specific implementation of XFormsSessionManager (defaults to DefaultXFormsSessionManagerImpl)
	 */
	protected XFormsSessionManager getXFormsSessionManager() {
		return DefaultXFormsSessionManagerImpl.getInstance();
	}
	
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public String getSessionKey() {
		return sessionKey;
	}
}