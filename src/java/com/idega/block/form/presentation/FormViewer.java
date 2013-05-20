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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
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
import com.idega.chiba.ChibaUtils;
import com.idega.chiba.event.SubmissionEvent;
import com.idega.chiba.web.exception.IdegaChibaException;
import com.idega.chiba.web.session.impl.IdegaXFormSessionManagerImpl;
import com.idega.chiba.web.session.impl.IdegaXFormsSessionBase;
import com.idega.chiba.web.upload.XFormTmpFileResolverImpl;
import com.idega.chiba.web.xml.xforms.util.XFormsUtil;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.PDFRenderedComponent;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.OutdatedBrowserInformation;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xformsmanager.business.DocumentManager;
import com.idega.xformsmanager.business.DocumentManagerFactory;
import com.idega.xformsmanager.business.InvalidSubmissionException;
import com.idega.xformsmanager.business.PersistedFormDocument;
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.business.Submission;
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
	public static final String DISPLAY_FULL_FORM = "display_full_form";

	private static final String invalidSubmissionFacet = "InvalidSubmission";

	protected static final Logger LOGGER = Logger.getLogger(FormViewer.class.getName());

	private PersistenceManager persistenceManager;
	private String formId;
	private String submissionId;
	private DocumentManagerFactory documentManagerFactory;

	private Document xDoc;
	private String sessionKey;

	private boolean pdfViewer, validBrowser = Boolean.TRUE, submitted;

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

		IWContext iwc = IWContext.getIWContext(context);
		if (!isPdfViewer()) {
			if (iwc.isIE() && iwc.getBrowserVersion() < 7) {
				getChildren().add(new OutdatedBrowserInformation(Boolean.FALSE));
				validBrowser = Boolean.FALSE;
				return;
			}
		}

		initializeXForms(iwc);
	}

	protected Document resolveXFormsDocument(FacesContext context) {
		Document document = xDoc;
		if (document == null) {
			String formId = getFormId(context);
			if (!StringUtil.isEmpty(formId)) {
				PersistenceManager persistenceManager = getPersistenceManager();
				PersistedFormDocument formDocument = persistenceManager.loadForm(new Long(formId));
				document = formDocument.getXformsDocument();
			} else {
				String submissionId = getSubmissionId(context);
				if (!StringUtil.isEmpty(submissionId)) {
					PersistenceManager persistenceManager = getPersistenceManager();
					String uniqueSubmissionId = null;
					try {
						Submission submission = persistenceManager.getSubmission(Long.valueOf(submissionId));
						uniqueSubmissionId = submission.getSubmissionUUID();
					} catch(NumberFormatException e) {
						uniqueSubmissionId = submissionId;
					} catch(Exception e) {
						LOGGER.log(Level.WARNING, "Error resolving unique submission id from submission id: " + submissionId, e);
					}

					if (StringUtil.isEmpty(uniqueSubmissionId)) {
						LOGGER.warning("Unique submission ID was not resolved by submission id: " + submissionId);
						return null;
					}

					try {
						PersistedFormDocument formDocument = persistenceManager.loadPopulatedForm(uniqueSubmissionId, isPdfViewer());
						document = formDocument.getXformsDocument();
					} catch (InvalidSubmissionException e) {
						Text text = new Text("The form was already submitted");
						getFacets().put(invalidSubmissionFacet, text);
					} catch(Exception e) {
						LOGGER.log(Level.SEVERE, "Error loading form by unique submission ID: " + uniqueSubmissionId, e);
					}
				}
			}
		}

		return document;
	}

	private void addResources(IWContext iwc) {
		String styleSheet = new StringBuilder().append(CoreConstants.WEBDAV_SERVLET_URI).append(IWBundleStarter.REPOSITORY_STYLES_PATH)
			.append(IWBundleStarter.CHIBA_CSS).toString();
		PresentationUtil.addStyleSheetToHeader(iwc, styleSheet);

		List<String> scriptsUris = new ArrayList<String>();

		boolean addTestScript = false;

		IWBundle chibaBundle = iwc.getIWMainApplication().getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		try {
			// scripts for XForms - DO NOT change order of scripts!
			scriptsUris.add(jQuery.getBundleURIToJQueryLib());
			scriptsUris.add(jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.MASKED_INPUT));
			scriptsUris.add(jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.URL_PARSER));

			scriptsUris.add(web2.getBundleURIToPrototypeLib());
			scriptsUris.add(web2.getBundleURIToScriptaculousLib() + "?load=builder,effects,dragdrop,controls,slider");

			scriptsUris.add(CoreConstants.DWR_ENGINE_SCRIPT);
			scriptsUris.add("/dwr/interface/Flux.js");
			scriptsUris.add(CoreConstants.DWR_UTIL_SCRIPT);

			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/xformsConfig.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/dojo-0.4.4/dojo.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/xforms-util.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/FluxInterface.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/PresentationContext.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/htmltext.js"));
			scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/fckeditor/fckeditor.js"));

			scriptsUris.add(web2.getBundleUriToHumanizedMessagesScript());
			scriptsUris.add(jQuery.getBundleURIToJQueryPlugin(JQueryPlugin.TEXT_AREA_AUTO_GROW));

			//	TinyMCE
			scriptsUris.addAll(web2.getScriptsForTinyMCE());

			//	Test script
			IWMainApplicationSettings settings = iwc.getApplicationSettings();
			if (settings.getBoolean("load_xforms_test_script", Boolean.FALSE))
				scriptsUris.add(chibaBundle.getVirtualPathWithFileNameString("javascript/XFormsTester.js"));

			// Fancybox
			scriptsUris.addAll(web2.getBundleURIsToFancyBoxScriptFiles());

			//	Firefox Lite for IE
			if (settings.getBoolean("load_firebug_ie", Boolean.FALSE) && iwc.isIE())
				scriptsUris.add("https://getfirebug.com/firebug-lite.js");

			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scriptsUris);

			//	CSS
			PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
					web2.getBundleUriToHumanizedMessagesStyleSheet(),
					web2.getBundleURIToFancyBoxStyleFile()
			));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		String locale = iwc.getCurrentLocale().toString();
		IWResourceBundle iwrb = chibaBundle.getResourceBundle(iwc);
		String initScript = null;
		try {
			initScript = new StringBuilder("XFormsConfig.setConfiguration({")
				.append("baseScriptUri: '").append(chibaBundle.getVirtualPathWithFileNameString("javascript/dojo-0.4.4/', "))
				.append("locale: '").append(locale).append("', ")
				.append("displayFullForm: '").append(doDisplayFullForm(iwc.getIWMainApplication())).append("', ")
				.append("maxStringValueLength: ").append(XFormsUtil.getBPMStringVariableMaxLength())
				.append("}); ")
				.append("XFormsConfig.locale = '").append(locale).append("'; ")
				.append("Localization.CONFIRM_TO_SAVE_FORM = '").append(iwrb.getLocalizedString("save_form_before_exit", "Save form before exit?")).append("'; ")
				.append("Localization.CONFIRM_TO_LEAVE_NOT_SUBMITTED_FORM = '")
				.append(iwrb.getLocalizedString("confirm_to_leave_unfinished_xform", "Are you sure you want to navigate from unfinished form?"))
				.append("'; Localization.CONFIRM_TO_LEAVE_WHILE_UPLOAD_IN_PROGRESS = '")
				.append(iwrb.getLocalizedString("confirm_to_leave_xform_while_upload_in_progress",
						"Are you sure you want to navigate from this page while upload is in progress?")).append("';")
				.append("FluxInterfaceHelper.SUBMITTED = ").append(isSubmitted()).append(";").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (initScript != null)
			PresentationUtil.addJavaScriptActionToBody(iwc, initScript);

		if (addTestScript) {
			int openedSessions = 0;
			if (iwc.isLoggedOn()) {
				String httpSessionId = iwc.getSessionId();
				openedSessions = ChibaUtils.getInstance().getNumberOfXFormSessionsForHttpSession(httpSessionId) + 1;
			} else {
				openedSessions = IdegaXFormSessionManagerImpl.getXFormsSessionManager().getSessionCount() + 1;
			}
			String amount = iwc.getApplicationSettings().getProperty("open_test_sessions", String.valueOf(10));
			String action = "jQuery(window).load(function() {XFormsTester.OPENED_SESSIONS = " + openedSessions +"; XFormsTester.openSessions(" + amount + ");});";
			PresentationUtil.addJavaScriptActionToBody(iwc, action);
		}
	}

	protected void initializeXForms(IWContext iwc) {
		addResources(iwc);

		Document document = resolveXFormsDocument(iwc);

		if (document == null)
			return;

		HttpServletRequest request = iwc.getRequest();
		HttpServletResponse response = iwc.getResponse();
		HttpSession session = iwc.getSession();

		XFormsSessionManager sessionManager = null;
		XFormsSession xformsSession = null;

		try {
			sessionManager = getXFormsSessionManager(session);
			// get IdegaXFormsSessionBase instance
			xformsSession = sessionManager.createXFormsSession(request, response, session);
		} catch (XFormsConfigException e) {
			LOGGER.log(Level.WARNING, "Error creating XFormsSession", e);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error creating XFormsSession", e);
			CoreUtil.sendExceptionNotification(e);
		}

		Throwable exception = null;
		WebAdapter adapter = xformsSession.getAdapter();
		try {
			setupAdapter(adapter, document, xformsSession, iwc);
			adapter.init();

			EventTarget eventTarget = (EventTarget) ((Document) adapter.getXForms()).getDocumentElement();

			final WebAdapter eventAdapter = adapter;
			EventListener eventListener = new EventListener() {
				@Override
				public void handleEvent(Event event) {
					String id = CoreConstants.EMPTY;
					if (event.getTarget() instanceof Element) {
						id = ((Element) event.getTarget()).getAttribute("id");
					}

					if (XFormsEventNames.SUBMIT_DONE.equals(event.getType())) {
						ELUtil.getInstance().publishEvent(new SubmissionEvent(eventAdapter, event));
					}

					LOGGER.info("Got event, type=" + event.getType() + ", id=" + id);
				}
			};

			eventTarget.addEventListener(XFormsEventNames.SUBMIT_DONE, eventListener, true);
			eventTarget.addEventListener(XFormsEventNames.SUBMIT_ERROR, eventListener, true);

			XMLEvent exitEvent = adapter.checkForExitEvent();
			if (exitEvent != null) {
				handleExit(exitEvent, xformsSession, session, request, response);
			} else {
				// actually add the XFormsSession at the manager
				sessionManager.addXFormsSession(xformsSession);
				setSessionKey(xformsSession.getKey());
			}
		} catch (IOException e) {
			exception = e;
			LOGGER.log(Level.WARNING, "handleExit failed", e);
		} catch (XFormsException e) {
			exception = e;
			LOGGER.log(Level.WARNING, "Could not set XML container", e);
			shutdown(adapter, session, xformsSession.getKey());
		} catch (IdegaChibaException e) {
			exception = e;
			LOGGER.log(Level.WARNING, "Chiba exception", e);
		} finally {
			if (exception != null) {
				String messageToClient = exception instanceof IdegaChibaException ? ((IdegaChibaException) exception).getMessageToClient() : null;
				if (StringUtil.isEmpty(messageToClient)) {
					IWResourceBundle iwrb = getIWResourceBundle(iwc, com.idega.block.form.IWBundleStarter.BUNDLE_IDENTIFIER);
					messageToClient = iwrb.getLocalizedString("chiba_error_rendering_form",
							"We are very sorry, an error occurred... We are working on it. Please, try later.");
				}
				getChildren().add(new Heading1(messageToClient));

				shutdown(adapter, session, xformsSession.getKey());

				String identifier = getFormId(iwc);
				identifier = StringUtil.isEmpty(identifier) ? getSubmissionId(iwc) : identifier;
				CoreUtil.sendExceptionNotification("Error opening XForm: " + identifier, exception);
			}
		}
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		if (validBrowser) {
			if (getFacets().containsKey(invalidSubmissionFacet)) {
				renderChild(context, getFacet(invalidSubmissionFacet));
			} else {
				if (getFormId(context) != null || getSubmissionId(context) != null || xDoc != null) {
					IWContext iwc = IWContext.getIWContext(context);
					HttpSession session = iwc.getSession();
					WebAdapter webAdapter = null;
					try {
						XFormsSessionManager manager = getXFormsSessionManager(session);
						XFormsSession xFormsSession = manager.getXFormsSession(getSessionKey());

						if (xFormsSession == null) {
							initializeXForms(iwc);
							xFormsSession = manager.getXFormsSession(getSessionKey());
						}

						HttpServletRequest request = iwc.getRequest();

						xFormsSession.setRequest(request);
						xFormsSession.setBaseURI(request.getRequestURL().toString());

						if (xFormsSession instanceof IdegaXFormsSessionBase)
							((IdegaXFormsSessionBase) xFormsSession).handleRequest(iwc);
						else
							xFormsSession.handleRequest();
					} catch (Exception e) {
						LOGGER.log(Level.SEVERE, "Error rendering form", e);
						shutdown(webAdapter, session, getSessionKey());
					}
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

			if (formId == null) {
				ExternalContext externalContext = context.getExternalContext();
				formId = externalContext == null ? null : externalContext.getRequestParameterMap().get(formIdParam);
			}

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
		Object values[] = new Object[5];
		values[0] = super.saveState(ctx);
		values[1] = formId;
		values[2] = sessionKey;
		values[3] = Boolean.valueOf(isPdfViewer());
		values[4] = Boolean.valueOf(validBrowser);

		return values;
	}

	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		formId = (String) values[1];
		sessionKey = (String) values[2];
		pdfViewer = values[3] instanceof Boolean ? (Boolean) values[3] : Boolean.FALSE;
		validBrowser = values[4] instanceof Boolean ? (Boolean) values[3] : Boolean.TRUE;
	}

	protected void handleExit(XMLEvent exitEvent, XFormsSession xFormsSession, HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (ChibaEventNames.REPLACE_ALL.equals(exitEvent.getType())) {
			response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/SubmissionResponse?sessionKey=" + xFormsSession.getKey()));
		} else if (ChibaEventNames.LOAD_URI.equals(exitEvent.getType())) {
			Object showContextInfo = exitEvent.getContextInfo("show");
			if (showContextInfo != null) {
				String sessionId = xFormsSession.getKey();
				String explanation = "Killing XForm session '".concat(sessionId).concat("', because context info 'show' is: ")
					.concat(showContextInfo.toString()).concat(", expected value: 'replace'");

				ChibaUtils.getInstance().markXFormSessionFinished(sessionId, Boolean.TRUE);

				XFormsSessionManager manager = xFormsSession.getManager();
				if (manager instanceof IdegaXFormSessionManagerImpl) {
					((IdegaXFormSessionManagerImpl) manager).deleteXFormsSession(sessionId, explanation);
				} else {
					LOGGER.warning("Using not standard XForm manager to delete XForm session: ".concat(manager.toString()).concat(" ").concat(explanation));
					manager.deleteXFormsSession(sessionId);
				}
				setSessionKey(null);

				String loadURI = (String) exitEvent.getContextInfo("uri");
				response.sendRedirect(response.encodeRedirectURL(loadURI));
			}
		}
		LOGGER.fine("Exited during XForms model init");
	}

	protected void setupAdapter(WebAdapter adapter, Document document, XFormsSession xforms_session, FacesContext context) throws XFormsException {
		adapter.setXFormsSession(xforms_session);
		adapter.setXForms(document);

		Map<String, String> servletMap = new HashMap<String, String>();
		servletMap.put(WebAdapter.SESSION_ID, xforms_session.getKey());
		adapter.setContextParam(XFormsConstants.SUBMISSION, servletMap);

		IWMainApplication app = context instanceof IWContext ?
				((IWContext) context).getIWMainApplication() :
				IWMainApplication.getDefaultIWMainApplication();
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		adapter.setBaseURI(bundle.getResourcesVirtualPath());
		adapter.setUploadDestination(XFormTmpFileResolverImpl.UPLOADS_PATH);
		// storeCookies(request, adapter);
	}

	protected void shutdown(WebAdapter webAdapter, HttpSession session, String key) {
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

	protected XFormsSessionManager getXFormsSessionManager(HttpSession session) throws XFormsConfigException {
		XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);

		if (manager == null) {
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

	private String getSubmissionId(FacesContext context) {
		String submissionId = getSubmissionId();

		if (submissionId == null) {
			submissionId = getExpressionValue(context, submissionIdParam);

			if (submissionId == null) {
				ExternalContext externalContext = context.getExternalContext();
				submissionId = externalContext == null ? null : externalContext.getRequestParameterMap().get(submissionIdParam);
			}

			submissionId = StringUtil.isEmpty(submissionId) ? null : submissionId;
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
	@XFormPersistenceType(CoreConstants.REPOSITORY)
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	@Override
	public boolean isPdfViewer() {
		return pdfViewer;
	}

	@Override
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
			IWMainApplication iwma = fctx == null ?
					IWMainApplication.getDefaultIWMainApplication() :
					IWMainApplication.getIWMainApplication(fctx);

			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(iwma);

			if (xDoc == null)
				xDoc = resolveXFormsDocument(fctx);

			com.idega.xformsmanager.business.Document form = documentManager.openFormLazy(xDoc);

			return form;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * <p>Checks if forms should be should full or in traditional way.</p>
	 * @param iwc - current context;
	 * @return <code>true</code> if form should be shown full or
	 * <code>false</code> if in traditional way.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	private boolean doDisplayFullForm(IWMainApplication iwma) {
		if (iwma == null) {
			iwma = IWMainApplication.getDefaultIWMainApplication();
		}
		if (!iwma.getSettings().getBoolean(DISPLAY_FULL_FORM, Boolean.FALSE)) {
			return Boolean.FALSE;
		}

		return isSubmitted();
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

}