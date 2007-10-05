/*
 * $Id: FormViewer.java,v 1.30 2007/10/05 11:44:19 civilis Exp $ Created on
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

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
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.jbpm.exe.VariablesHandler;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Script;
import com.idega.util.xml.NamespaceContextImpl;
import com.idega.webface.WFUtil;

/**
 * TODO: remake this component completely
 * 
 * Last modified: $Date: 2007/10/05 11:44:19 $ by $Author: civilis $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.30 $
 */
public class FormViewer extends IWBaseComponent {

	protected static final Logger log =   Logger.getLogger(FormViewer.class.getName());
	private static final String taskInstanceIdParameter = "taskInstanceId";

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
	
	private void initializeXForms(FacesContext context) {
		Document document = xDoc;
		
		if(document == null) {
			
			String formId = getFormId() != null ? getFormId() : getValueBinding("formId") != null ? (String)getValueBinding("formId").getValue(context) : (String)context.getExternalContext().getRequestParameterMap().get("formId");
			
			if(formId == null || formId.equals(""))
				return;
				
			setFormId(formId);

			PersistenceManager persistenceManager = (PersistenceManager) WFUtil.getBeanInstance("xformsPersistenceManager");
			document = persistenceManager.loadFormNoLock(formId);
			
			if (document == null) {
				log.log(Level.SEVERE, "Could not load the form for id: " + formId);
				return;
			}
		}
		
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
		
		XFormsSessionManager sessionManager = getXFormsSessionManager();
		XFormsSession xformsSession = sessionManager.createXFormsSession();
		
		/*
        the XFormsSessionManager is kept in the http-session though it is accessible as singleton. Subsequent
        servlets should access the manager through the http-session attribute as below to ensure the http-session
        is refreshed.
		 */
		HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
		session.setAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER, sessionManager);

		WebAdapter adapter = new FluxAdapter();
		xformsSession.setAdapter(adapter);
		try {
			setupDocument(context, document);
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
				
				UIGenerator uiGenerator = createUIGenerator(context, request, xformsSession);
				// store WebAdapter in XFormsSession
				xformsSession.setAdapter(adapter);
				// store UIGenerator in XFormsSession as property
				xformsSession.setProperty(XFormsSession.UIGENERATOR, uiGenerator);
				// store queryString as 'referer' in XFormsSession
				xformsSession.setProperty(XFormsSession.REFERER, request.getQueryString());
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

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		
		if(xDoc == null) {
			
			String formId = getFormId();
			String newFormId = getValueBinding("formId") != null ? (String)getValueBinding("formId").getValue(context) : (String)context.getExternalContext().getRequestParameterMap().get("formId");
			
			if(newFormId != null && (formId == null || !formId.equals(newFormId))) {
				
				setFormId(null);
				initializeXForms(context);
			}
		}
		
		if (getFormId() != null || xDoc != null) {
			
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
	
	protected void setupDocument(FacesContext ctx, Document document) {

//		TODO: move this to specific form viewer. don't worry about the mess for now
		String tiIdPar = getTaskInstanceId() != null ? getTaskInstanceId() : getValueBinding("taskInstanceId") != null ? (String)getValueBinding("taskInstanceId").getValue(ctx) : (String)ctx.getExternalContext().getRequestParameterMap().get(taskInstanceIdParameter);
		
		if(tiIdPar == null || tiIdPar.equals(""))
			return;
		
//		check if task id parses as long
		long tiId = Long.parseLong(tiIdPar);
		
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			
			NamespaceContextImpl nmspCtx = new NamespaceContextImpl();
			nmspCtx.addPrefix("xf", "http://www.w3.org/2002/xforms");
			xpath.setNamespaceContext(nmspCtx);
			
			XPathExpression exp = xpath.compile("//xf:instance[@id='data-instance']");
			
			Node instance = (Node)exp.evaluate(document, XPathConstants.NODE);
			VariablesHandler vh = (VariablesHandler)WFUtil.getBeanInstance("process_xforms_variablesHandler");
			vh.populate(tiId, instance);
			
		} catch (XPathException e) {
			throw new RuntimeException("Could not compile XPath expression: " + e.getMessage(), e);
		}
		
		BlockFormUtil.appendToSubmissionsActions(document, "?taskId="+tiIdPar);
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
		generator.setParameter("imagesPath", "/idegaweb/bundles/" + IWBundleStarter.BUNDLE_IDENTIFIER + ".bundle/resources/style/images/");
		
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

	public void setXFormsDocument(Document xDoc) {
		this.xDoc = xDoc;
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

	public String getTaskInstanceId() {
		return taskInstanceId;
	}

	public void setTaskInstanceId(String taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}
}