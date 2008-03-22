/*
 * $Id: FormViewer.java,v 1.39 2008/03/22 11:27:23 civilis Exp $ Created on
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.chiba.web.IWBundleStarter;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.XFormsEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.idega.chiba.web.session.impl.IdegaXFormSessionManagerImpl;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.webface.WFUtil;

import org.chiba.web.WebAdapter;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;
import org.chiba.web.session.impl.DefaultXFormsSessionManagerImpl;

/**
 * TODO: remake this component completely
 * 
 * Last modified: $Date: 2008/03/22 11:27:23 $ by $Author: civilis $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.39 $
 */
public class FormViewer extends IWBaseComponent {

	public static final String COMPONENT_TYPE = "FormViewer";
	
	protected static final Logger log =   Logger.getLogger(FormViewer.class.getName());
//	private static final String taskInstanceIdParameter = "taskInstanceId";

	private String formId;
	private Document xDoc;
	private String sessionKey;
	private String taskInstanceId;

	public FormViewer() {
		super();
	}

	public String getRendererType() {
		return null;
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		
		super.initializeComponent(context);
		initializeXForms(context);
	}
	
	protected Document resolveXFormsDocument(FacesContext context) {
		
		Document document = xDoc;
		
		if(document == null) {
			
			String formId = getFormId(context);
			
			if(formId == null || formId.equals(CoreConstants.EMPTY))
				return null;
				
			PersistenceManager persistenceManager = (PersistenceManager) WFUtil.getBeanInstance("xformsPersistenceManager");
			document = persistenceManager.loadFormNoLock(formId);
			
			if (document == null) {
				log.log(Level.SEVERE, "Could not load the form for id: " + formId);
				return null;
			}
		}
		
		return document;
	}
	
	protected void initializeXForms(FacesContext context) {
		
		Document document = resolveXFormsDocument(context);
		
		if(document == null)
			return;
		
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
		
		XFormsSessionManager sessionManager = null;
		XFormsSession xformsSession = null;
		
		try {
			sessionManager = getXFormsSessionManager(session);
			//get IdegaXFormsSessionBase instance
			xformsSession = sessionManager.createXFormsSession(request, response, session);
		}catch (XFormsConfigException e2) {
			e2.printStackTrace();
		} catch (XFormsException e1) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e1);
		}
		
		WebAdapter adapter = xformsSession.getAdapter();
		//xformsSession.setAdapter(adapter);
		try {
//			setupDocument(context, document);
			setupAdapter(adapter, document, xformsSession, context);
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
				handleExit(exitEvent, xformsSession, session, request, response);
			} else {

				// actually add the XFormsSession ot the manager
				sessionManager.addXFormsSession(xformsSession);
				setSessionKey(xformsSession.getKey());

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
			shutdown(adapter, session, xformsSession.getKey());
			return;
		}
	}
	
//	AddResource resource = AddResourceFactory.getInstance(context);
//	resource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, CoreUtil.getCoreBundle().getVirtualPathWithFileNameString(CACHE_JS_SRC));
//	resource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, web2_business.getBundleURIToMootoolsLib());
//	resource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, web2_business.getBundleURIToJQueryLib());
//	resource.addInlineScriptAtPosition(context, AddResource.HEADER_BEGIN, "jQuery.noConflict();");
//	
//	IWBundle bundle = iwma.getBundle(IW_BUNDLE_IDENTIFIER);
//	resource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, bundle.getVirtualPathWithFileNameString(ELIGHT_JS_SRC));

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		
		if (getFormId(context) != null || xDoc != null) {
			
			HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
			WebAdapter webAdapter = null;
			try {
				XFormsSessionManager manager = getXFormsSessionManager(session);
				XFormsSession xFormsSession = manager.getXFormsSession(getSessionKey());
				
				HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

		        xFormsSession.setRequest(request);
		        xFormsSession.setBaseURI(request.getRequestURL().toString());

                xFormsSession.handleRequest();
				
           } catch (Exception e) {
	        	log.log(Level.SEVERE, "Error rendering form", e);
	        	shutdown(webAdapter, session, getSessionKey());
			}
		}
		super.encodeEnd(context);
	}

	public String getFormId() {
		
		return formId;
	}
	
	public String getFormId(FacesContext context) {

		String formId = getFormId();
		
		if(formId == null) {
			
			formId = getValueBinding("formId") != null ? (String)getValueBinding("formId").getValue(context) : (String)context.getExternalContext().getRequestParameterMap().get("formId");
			formId = CoreConstants.EMPTY.equals(formId) ? null : formId;
			setFormId(formId);
		}
		
		return formId;
	}

	public void setFormId(String formId) {

		this.formId = formId;
	}

	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[4];
		values[0] = super.saveState(ctx);
		values[1] = formId;
		values[2] = sessionKey;
		values[3] = taskInstanceId;
		
		return values;
	}

	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		formId = (String) values[1];
		sessionKey = (String) values[2];
		taskInstanceId = (String) values[3];
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
		
		Map<String, String> servletMap = new HashMap<String, String>();
		servletMap.put(WebAdapter.SESSION_ID, xforms_session.getKey());
		adapter.setContextParam(Submission.SUBMISSION, servletMap);

		IWMainApplication app = IWMainApplication.getIWMainApplication(context);
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		adapter.setBaseURI(bundle.getResourcesVirtualPath());
		adapter.setUploadDestination(bundle.getBundleBaseRealPath() + "/upload/" + xforms_session.getKey());
		// storeCookies(request, adapter);
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

	public void setXFormsDocument(Document xDoc) {
		this.xDoc = xDoc;
	}
	
	protected XFormsSessionManager getXFormsSessionManager(HttpSession session) throws XFormsConfigException {
		
		XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
		
		if(manager == null) {
		
			manager = DefaultXFormsSessionManagerImpl.createXFormsSessionManager(IdegaXFormSessionManagerImpl.class.getName());
			session.setAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER, manager);
		}
			
		return manager;
	}
	
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public String getSessionKey() {
		return sessionKey;
	}

	public String getTaskInstanceId() {
		return taskInstanceId;
	}

	public void setTaskInstanceId(String taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}
}