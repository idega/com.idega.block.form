/*
 * $Id: FormViewer.java,v 1.80 2009/06/19 11:27:16 valdas Exp $ Created on
 * Aug 17, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.form.presentation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.chiba.web.IWBundleStarter;
import org.chiba.web.WebAdapter;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;
import org.chiba.web.session.impl.DefaultXFormsSessionManagerImpl;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.XFormsEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.XFormsConstants;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.JQueryPlugin;
import com.idega.block.web2.business.Web2Business;
import com.idega.chiba.web.session.impl.IdegaXFormSessionManagerImpl;
import com.idega.chiba.web.upload.XFormTmpFileResolverImpl;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.PDFRenderedComponent;
import com.idega.presentation.text.Text;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xformsmanager.business.DocumentManager;
import com.idega.xformsmanager.business.DocumentManagerFactory;
import com.idega.xformsmanager.business.InvalidSubmissionException;
import com.idega.xformsmanager.business.PersistedFormDocument;
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.business.XFormPersistenceType;

/**
 * Last modified: $Date: 2009/06/19 11:27:16 $ by $Author: valdas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.80 $
 */
public class FormViewer extends IWBaseComponent implements PDFRenderedComponent {
	
	public static final String COMPONENT_TYPE = "FormViewer";
	public static final String formIdParam = "formId";
	public static final String submissionIdParam = "submissionId";
	public static final String formviewerPageType = "formsviewer";
	
	private static final String invalidSubmissionFacet = "InvalidSubmission";
	
	protected static final Logger log = Logger.getLogger(FormViewer.class
	        .getName());
	
	private PersistenceManager persistenceManager;
	private String formId;
	private String submissionId;
	private DocumentManagerFactory documentManagerFactory;
	
	private Document xDoc;
	private String sessionKey;
	
	private boolean pdfViewer;
	
	@Autowired
	private JQuery jQuery;
	@Autowired
	private Web2Business web2;
	
	public FormViewer() {
		super();
		ELUtil.getInstance().autowire(this);
	}
	
	@Override
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
		
		if (document == null) {
			
			String formId = getFormId(context);
			
			if (!StringUtil.isEmpty(formId)) {
				
				PersistenceManager persistenceManager = getPersistenceManager();
				PersistedFormDocument formDocument = persistenceManager
				        .loadForm(new Long(formId));
				document = formDocument.getXformsDocument();
				
			} else {
				
				String submissionId = getSubmissionId(context);
				
				if (!StringUtil.isEmpty(submissionId)) {
					
					PersistenceManager persistenceManager = getPersistenceManager();
					
					String uniqueSubmissionId = null;
					try {
						uniqueSubmissionId = persistenceManager.getSubmission(Long.valueOf(submissionId)).getSubmissionUUID();
					} catch(NumberFormatException e) {
						uniqueSubmissionId = submissionId;
					} catch(Exception e) {
						log.log(Level.WARNING, "Error resolving unique submission id from submission id: " + submissionId, e);
					}
					
					if (StringUtil.isEmpty(uniqueSubmissionId)) {
						log.warning("Unique submission ID was not resolved by submission id: " + submissionId);
						return null;
					}
					
					try {
						PersistedFormDocument formDocument = persistenceManager.loadPopulatedForm(uniqueSubmissionId, isPdfViewer());
						document = formDocument.getXformsDocument();
					
					} catch (InvalidSubmissionException e) {
						Text text = new Text("The form was already submitted");
						getFacets().put(invalidSubmissionFacet, text);
						
					} catch(Exception e) {
						log.log(Level.SEVERE, "Error loading form by unique submission ID: " + uniqueSubmissionId, e);
					}
				}
			}
		}
		
		return document;
	}
	
	
	private void addResources(IWContext iwc) {
		String styleSheet = new StringBuilder().append("/content").append(IWBundleStarter.SLIDE_STYLES_PATH).append(IWBundleStarter.CHIBA_CSS).toString();
		PresentationUtil.addStyleSheetToHeader(iwc, styleSheet);
		
		List<String> scriptsUris = new ArrayList<String>();
		
		IWBundle chibaBundle = iwc.getIWMainApplication().getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);	
		try {
			// scripts for xforms - DO NOT change order of scripts!
			scriptsUris.add(jQuery.getBundleURIToJQueryLib());
			
			scriptsUris.add(web2.getBundleURIToPrototypeLib());
			scriptsUris.add(web2.getBundleURIToScriptaculousLib());
			
			scriptsUris.add(CoreConstants.DWR_ENGINE_SCRIPT);
			scriptsUris.add("/dwr/interface/Flux.js");
			scriptsUris.add(CoreConstants.DWR_UTIL_SCRIPT);
			
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/xformsConfig.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/dojo-0.4.3/dojo.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/xforms-util.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/FluxInterface.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/PresentationContext.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/htmltext.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/fckeditor/fckeditor.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/dojo-0.4.3/dojoSetup.js"));
			
			scriptsUris.add(web2.getBundleUriToHumanizedMessagesScript());
			scriptsUris.add(jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.AUTO_RESIZE));
			
			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scriptsUris);
			
			PresentationUtil.addStyleSheetToHeader(iwc, web2.getBundleUriToHumanizedMessagesStyleSheet());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		String initScript = new StringBuilder("XFormsConfig.setConfiguration({baseScriptUri: '")
			.append(chibaBundle.getVirtualPathWithFileNameString("javascript/dojo-0.4.3/")).append("', locale: '").append(iwc.getCurrentLocale().toString())
		.append("'});").toString();
		PresentationUtil.addJavaScriptActionToBody(iwc, initScript);
	}
	
	protected void initializeXForms(FacesContext context) {
		addResources(IWContext.getIWContext(context));
		
		Document document = resolveXFormsDocument(context);
		
		if (document == null)
			return;
		
		HttpServletRequest request = (HttpServletRequest) context
		        .getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) context
		        .getExternalContext().getResponse();
		HttpSession session = (HttpSession) context.getExternalContext()
		        .getSession(true);
		
		XFormsSessionManager sessionManager = null;
		XFormsSession xformsSession = null;
		
		try {
			sessionManager = getXFormsSessionManager(session);
			// get IdegaXFormsSessionBase instance
			xformsSession = sessionManager.createXFormsSession(request,
			    response, session);
		} catch (XFormsConfigException e2) {
			e2.printStackTrace();
		} catch (XFormsException e1) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e1);
		}
		
		WebAdapter adapter = xformsSession.getAdapter();
		// xformsSession.setAdapter(adapter);
		try {
			// setupDocument(context, document);
			setupAdapter(adapter, document, xformsSession, context);
			adapter.init();
			
			EventTarget eventTarget = (EventTarget) ((Document) adapter
			        .getXForms()).getDocumentElement();
			
			EventListener eventListener = new EventListener() {
				
				public void handleEvent(Event event) {
					String id = "";
					if (event.getTarget() instanceof Element) {
						id = ((Element) event.getTarget()).getAttribute("id");
					}
					
					log.info("Got event, type=" + event.getType() + ", id="
					        + id);
					
				}
			};
			
			eventTarget.addEventListener(XFormsEventNames.SUBMIT_DONE,
			    eventListener, true);
			eventTarget.addEventListener(XFormsEventNames.SUBMIT_ERROR,
			    eventListener, true);
			
			XMLEvent exitEvent = adapter.checkForExitEvent();
			if (exitEvent != null) {
				handleExit(exitEvent, xformsSession, session, request, response);
			} else {
				
				// actually add the XFormsSession ot the manager
				sessionManager.addXFormsSession(xformsSession);
				setSessionKey(xformsSession.getKey());
				
				// store queryString as 'referer' in XFormsSession
				// xFormsSession.setProperty(XFormsSession.REFERER, request.getQueryString());
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "handleExit failed", e);
			return;
		} catch (XFormsException e) {
			log.log(Level.WARNING, "Could not set XML container", e);
			shutdown(adapter, session, xformsSession.getKey());
			return;
		}
	}
	
	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		
		if (getFacets().containsKey(invalidSubmissionFacet)) {
			
			renderChild(context, getFacet(invalidSubmissionFacet));
			
		} else {
			
			if (getFormId(context) != null || getSubmissionId(context) != null
			        || xDoc != null) {
				
				HttpSession session = (HttpSession) context
				        .getExternalContext().getSession(true);
				WebAdapter webAdapter = null;
				try {
					XFormsSessionManager manager = getXFormsSessionManager(session);
					XFormsSession xFormsSession = manager
					        .getXFormsSession(getSessionKey());
					
					if (xFormsSession == null) {
						initializeXForms(context);
						xFormsSession = manager
						        .getXFormsSession(getSessionKey());
					}
					
					HttpServletRequest request = (HttpServletRequest) context
					        .getExternalContext().getRequest();
					
					xFormsSession.setRequest(request);
					xFormsSession
					        .setBaseURI(request.getRequestURL().toString());
					
					xFormsSession.handleRequest();
					
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error rendering form", e);
					shutdown(webAdapter, session, getSessionKey());
				}
			}
		}
		
		super.encodeEnd(context);
	}
	
	public String getFormId() {
		
		return formId;
	}
	
	public String getFormId(FacesContext context) {
		
		String formId = getFormId();
		
		if (formId == null) {
			
			formId = getExpressionValue(context, formIdParam);
			
			if (formId == null)
				formId = context.getExternalContext().getRequestParameterMap()
				        .get(formIdParam);
			
			formId = StringUtil.isEmpty(formId) ? null : formId;
			setFormId(formId);
		}
		
		return formId;
	}
	
	public void setFormId(String formId) {
		
		this.formId = formId;
	}
	
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[4];
		values[0] = super.saveState(ctx);
		values[1] = formId;
		values[2] = sessionKey;
		values[3] = Boolean.valueOf(isPdfViewer());
		
		return values;
	}
	
	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		formId = (String) values[1];
		sessionKey = (String) values[2];
		pdfViewer = values[3] instanceof Boolean ? (Boolean) values[3] : false;
	}
	
	protected void handleExit(XMLEvent exitEvent, XFormsSession xFormsSession,
	        HttpSession session, HttpServletRequest request,
	        HttpServletResponse response) throws IOException {
		if (ChibaEventNames.REPLACE_ALL.equals(exitEvent.getType())) {
			response.sendRedirect(response.encodeRedirectURL(request
			        .getContextPath()
			        + "/SubmissionResponse?sessionKey="
			        + xFormsSession.getKey()));
		} else if (ChibaEventNames.LOAD_URI.equals(exitEvent.getType())) {
			if (exitEvent.getContextInfo("show") != null) {
				String loadURI = (String) exitEvent.getContextInfo("uri");
				// kill XFormsSession
				xFormsSession.getManager().deleteXFormsSession(
				    xFormsSession.getKey());
				setSessionKey(null);
				response.sendRedirect(response.encodeRedirectURL(loadURI));
			}
		}
		log.fine("Exited during XForms model init");
	}
	
	protected void setupAdapter(WebAdapter adapter, Document document,
	        XFormsSession xforms_session, FacesContext context)
	        throws XFormsException {
		adapter.setXFormsSession(xforms_session);
		adapter.setXForms(document);
		
		Map<String, String> servletMap = new HashMap<String, String>();
		servletMap.put(WebAdapter.SESSION_ID, xforms_session.getKey());
		adapter.setContextParam(XFormsConstants.SUBMISSION, servletMap);
		
		IWMainApplication app = IWMainApplication.getIWMainApplication(context);
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		adapter.setBaseURI(bundle.getResourcesVirtualPath());
		adapter.setUploadDestination(XFormTmpFileResolverImpl.UPLOADS_PATH);
		// storeCookies(request, adapter);
	}
	
	protected void shutdown(WebAdapter webAdapter, HttpSession session,
	        String key) {
		// attempt to shutdown processor
		if (webAdapter != null) {
			try {
				webAdapter.shutdown();
			} catch (XFormsException xfe) {
				xfe.printStackTrace();
			}
		}
		// remove xformssession from httpsession
		if (key != null) {
			session.removeAttribute(key);
		}
		setSessionKey(null);
		// redirect to error page (after encoding session id if required)
		// response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/"
		// + request.getSession().getServletContext().getInitParameter("error.page")));
	}
	
	public void setXFormsDocument(Document xDoc) {
		this.xDoc = xDoc;
	}
	
	protected XFormsSessionManager getXFormsSessionManager(HttpSession session)
	        throws XFormsConfigException {
		
		XFormsSessionManager manager = (XFormsSessionManager) session
		        .getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
		
		if (manager == null) {
			
			manager = DefaultXFormsSessionManagerImpl
			        .createXFormsSessionManager(IdegaXFormSessionManagerImpl.class
			                .getName());
			session.setAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER,
			    manager);
		}
		
		return manager;
	}
	
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	
	public String getSessionKey() {
		return sessionKey;
	}
	
	private String getSubmissionId(FacesContext context) {
		
		String submissionId = getSubmissionId();
		
		if (submissionId == null) {
			
			submissionId = getExpressionValue(context, submissionIdParam);
			
			if (submissionId == null)
				submissionId = context.getExternalContext()
				        .getRequestParameterMap().get(submissionIdParam);
			
			submissionId = StringUtil.isEmpty(submissionId) ? null
			        : submissionId;
			setSubmissionId(submissionId);
		}
		
		return submissionId;
	}
	
	public String getSubmissionId() {
		return submissionId;
	}
	
	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}
	
	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}
	
	@Autowired
	@XFormPersistenceType("slide")
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}
	
	public boolean isPdfViewer() {
		return pdfViewer;
	}
	
	public void setPdfViewer(boolean pdfViewer) {
		if (pdfViewer)
			getFormDocument().setPdfForm(pdfViewer);
		
		this.pdfViewer = pdfViewer;
	}
	
	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}
	
	@Autowired
	public void setDocumentManagerFactory(
	        DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}
	
	protected com.idega.xformsmanager.business.Document getFormDocument() {
		
		try {
			FacesContext fctx = FacesContext.getCurrentInstance();
			IWMainApplication iwma = fctx == null ? IWMainApplication
			        .getDefaultIWMainApplication() : IWMainApplication
			        .getIWMainApplication(fctx);
			
			DocumentManager documentManager = getDocumentManagerFactory()
			        .newDocumentManager(iwma);
			
			if (xDoc == null) {
				xDoc = resolveXFormsDocument(fctx);
			}
			
			com.idega.xformsmanager.business.Document form = documentManager
			        .openFormLazy(xDoc);
			
			return form;
			
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}