/*
 * $Id: FormViewer.java,v 1.15 2006/11/28 18:27:42 laddi Exp $ Created on
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
import org.apache.commons.httpclient.Cookie;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.adapter.ui.UIGenerator;
import org.chiba.adapter.ui.XSLTGenerator;
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
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xslt.TransformerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.bean.FormBean;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.webface.WFUtil;

/**
 * 
 * Last modified: $Date: 2006/11/28 18:27:42 $ by $Author: laddi $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.15 $
 */
public class FormViewer extends IWBaseComponent {

	protected static final Logger log = Logger.getLogger(FormViewer.class.getName());

	private String formId;

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
		FormBean form = getFormBean();
		String param = (String) context.getExternalContext().getRequestParameterMap().get("formId");
		if (param != null && !param.equals("")) {
			log.info("Setting component's formId from request parameter");
			setFormId(param);
		}
		else {
			form.setFormId(getFormId());
		}
		if (form.getFormId() == null) {
			log.warning("formId not defined");
			return;
		}
		// load form
		try {
			form.load();
			Document doc = form.getDocument();
			if (doc == null) {
				log.warning("Could not load the form from " + getFormId());
				return;
			}
		}
		catch (IOException e) {
			log.log(Level.WARNING, "Could not load the form from " + getFormId(), e);
			return;
		}
		WebAdapter adapter = null;
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
		XFormsSessionManager sessionManager = getXFormsSessionManager();
		XFormsSession xFormsSession = sessionManager.createXFormsSession();
		/*
        the XFormsSessionManager is kept in the http-session though it is accessible as singleton. Subsequent
        servlets should access the manager through the http-session attribute as below to ensure the http-session
        is refreshed.
		 */
		session.setAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER, sessionManager);
		try {
			adapter = new FluxAdapter();
			setupAdapter(adapter, form, xFormsSession, context);
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
				handleExit(exitEvent, xFormsSession, session, request, response);
			}
			else {
				UIGenerator uiGenerator = createUIGenerator(context, request, xFormsSession);
				// store WebAdapter in XFormsSession
				xFormsSession.setAdapter(adapter);
				// store UIGenerator in XFormsSession as property
				xFormsSession.setProperty(XFormsSession.UIGENERATOR, uiGenerator);
				// store queryString as 'referer' in XFormsSession
				xFormsSession.setProperty(XFormsSession.REFERER, request.getQueryString());
				// actually add the XFormsSession ot the manager
				sessionManager.addXFormsSession(xFormsSession);
				setSessionKey(xFormsSession.getKey());
			}
		}
		catch (IOException e) {
			log.log(Level.WARNING, "handleExit failed", e);
			return;
		}
		catch (XFormsException e) {
			log.log(Level.WARNING, "Could not set XML container", e);
			shutdown(adapter, session, xFormsSession.getKey());
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
		if (isInitialized()) {
//	        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
//	        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
			// HttpSession session = request.getSession(true);
			HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
			WebAdapter webAdapter = null;
			try {
				XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
				XFormsSession xFormsSession = manager.getXFormsSession(sessionKey);
				webAdapter = xFormsSession.getAdapter();
				if (webAdapter == null) {
					throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
				}
				UIGenerator uiGenerator = (UIGenerator) xFormsSession.getProperty(XFormsSession.UIGENERATOR);
				uiGenerator.setInput(webAdapter.getXForms());
				uiGenerator.setOutput(context.getResponseWriter());
				uiGenerator.generate();
	        } catch (Exception e) {
				shutdown(webAdapter, session, sessionKey);
			}
		}
		super.encodeEnd(context);
	}

	/* (non-Javadoc)
	 * @see com.idega.presentation.IWBaseComponent#decode(javax.faces.context.FacesContext)
	 */
	public void decode(FacesContext context) {
		log.info("decode!");
		super.decode(context);
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
		getFormBean().setFormId(formId);
	}

	public String getSessionKey() {
		return this.sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[3];
		values[0] = super.saveState(ctx);
		values[1] = this.formId;
		values[2] = this.sessionKey;
		return values;
	}

	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		this.formId = (String) values[1];
		this.sessionKey = (String) values[2];
	}

	/**
	 * Get managed form bean
	 * @return
	 */
	protected FormBean getFormBean() {
		return (FormBean) WFUtil.getBeanInstance("formBean");
	}

	/**
     * returns a specific implementation of XFormsSessionManager. Plugin your own implementations here if needed.
	 * 
     * @return a specific implementation of XFormsSessionManager (defaults to DefaultXFormsSessionManagerImpl)
	 */
	protected XFormsSessionManager getXFormsSessionManager() {
		return DefaultXFormsSessionManagerImpl.getInstance();
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
	protected void setupAdapter(WebAdapter adapter, FormBean form, XFormsSession xFormsSession, FacesContext context) throws XFormsException {
		adapter.setXFormsSession(xFormsSession);
		adapter.setXForms(form.getDocument());
		
		Map servletMap = new HashMap();
		servletMap.put(WebAdapter.SESSION_ID, xFormsSession.getKey());
		adapter.setContextParam(ChibaAdapter.SUBMISSION_RESPONSE, servletMap);

		IWMainApplication app = IWMainApplication.getIWMainApplication(context);
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		adapter.setBaseURI(bundle.getResourcesVirtualPath());
		adapter.setUploadDestination(bundle.getBundleBaseRealPath() + "/upload");
		// storeCookies(request, adapter);
	}

	/**
     * stores cookies that may exist in request and passes them on to processor for usage in
     * HTTPConnectors. Instance loading and submission then uses these cookies. Important for
     * applications using auth.
	 * 
     * @param request the servlet request
     * @param adapter the WebAdapter instance
	 */
	protected void storeCookies(HttpServletRequest request, WebAdapter adapter) {
		javax.servlet.http.Cookie[] cookiesIn = request.getCookies();
		if (cookiesIn != null) {
			Cookie[] commonsCookies = new org.apache.commons.httpclient.Cookie[cookiesIn.length];
			for (int i = 0; i < cookiesIn.length; i += 1) {
				javax.servlet.http.Cookie c = cookiesIn[i];
                commonsCookies[i] = new Cookie(c.getDomain(),
                        c.getName(),
                        c.getValue(),
                        c.getPath(),
                        c.getMaxAge(),
						c.getSecure());
			}
			adapter.setContextParam(AbstractHTTPConnector.REQUEST_COOKIE, commonsCookies);
		}
	}

	protected UIGenerator createUIGenerator(FacesContext context, HttpServletRequest request, XFormsSession session)
			throws XFormsConfigException {
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
		generator.setParameter("debug-enabled", String.valueOf(log.isLoggable(Level.FINE)));
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
		session.removeAttribute(key);
		setSessionKey(null);
		// redirect to error page (after encoding session id if required)
		//response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/"
		//		+ request.getSession().getServletContext().getInitParameter("error.page")));
	}
}
