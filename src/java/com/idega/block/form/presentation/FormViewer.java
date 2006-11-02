/*
 * $Id: FormViewer.java,v 1.8 2006/11/02 14:39:43 gediminas Exp $ 
 * Created on Aug 17, 2006
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
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xslt.TransformerService;
import org.w3c.dom.Document;
import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.bean.FormBean;
import com.idega.presentation.IWBaseComponent;
import com.idega.webface.WFUtil;

/**
 * 
 * Last modified: $Date: 2006/11/02 14:39:43 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version $Revision: 1.8 $
 */
public class FormViewer extends IWBaseComponent {

	private static final Logger log = Logger.getLogger(FormViewer.class.getName());

	private String formId;

	private XFormsSession xFormsSession;

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
		
		log.info("formId = " + form.getFormId());

		if (form.getFormId() == null) {
			log.warning("formId not defined");
			return;
		}
		
        WebAdapter adapter = null;
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        HttpSession session = request.getSession(true);

        XFormsSessionManager sessionManager = getXFormsSessionManager();
        xFormsSession = sessionManager.createXFormsSession();
        
		/*
        the XFormsSessionManager is kept in the http-session though it is accessible as singleton. Subsequent
        servlets should access the manager through the http-session attribute as below to ensure the http-session
        is refreshed.
        */
        session.setAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER,sessionManager);

        response.setHeader("Cache-Control","private, no-store,  no-cache, must-revalidate");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader("Expires",-1);

		try {
	        adapter = new FluxAdapter();
	        
	        adapter.setXFormsSession(xFormsSession);

			// load form
			form.load();
			Document doc = form.getDocument();
			if (doc == null) {
				log.warning("Could not load the form from " + getFormId());
				return;
			}
			
			// setup Adapter
			adapter = setupAdapter(adapter, xFormsSession.getKey(), form);
			storeCookies(request, adapter);
			adapter.init();
			XMLEvent exitEvent = adapter.checkForExitEvent();
			if (exitEvent != null) {
				handleExit(exitEvent, xFormsSession, session, request, response);
			}
			else {
				UIGenerator uiGenerator = createUIGenerator(context, request, xFormsSession.getKey());
				// store WebAdapter in XFormsSession
				xFormsSession.setAdapter(adapter);
				// store UIGenerator in XFormsSession as property
				xFormsSession.setProperty(XFormsSession.UIGENERATOR, uiGenerator);
				// store queryString as 'referer' in XFormsSession
				xFormsSession.setProperty(XFormsSession.REFERER, request.getQueryString());
				// actually add the XFormsSession ot the manager
				sessionManager.addXFormsSession(xFormsSession);
			}
        
		}
		catch (IOException e) {
			log.log(Level.WARNING, "Error reading document from Slide", e);
            shutdown(adapter, session, e, response, request, xFormsSession.getKey());
			return;
		}
		catch (XFormsException e) {
			log.log(Level.WARNING, "Could not set XML container", e);
            shutdown(adapter, session, e, response, request, xFormsSession.getKey());
			return;
		}
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String key = request.getParameter("sessionKey");
		
		log.info("sessionKey = " + key + ", initialized = " + isInitialized());

		if (isInitialized()/* && key != null*/) {
	        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
	        HttpSession session = request.getSession(true);
	        WebAdapter webAdapter = null;
	
	        try {
	            webAdapter = xFormsSession.getAdapter();
	            if (webAdapter == null) {
	                throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
	            }

	            // POST (from FluxHelperServlet)
	            /*
	            ChibaEvent chibaEvent = new DefaultChibaEventImpl();
	            chibaEvent.initEvent("http-request", null, request);
	            webAdapter.dispatch(chibaEvent);
	
	            boolean isUpload = FileUpload.isMultipartContent(request);
	
	            if (isUpload) {
	                ServletOutputStream out = response.getOutputStream();
	                out.println("<html><head><title>status</title></head><body></body></html>");
	                out.close();
	            }
	            */
	            
	            // GET (from ViewServlet)
                UIGenerator uiGenerator = (UIGenerator) xFormsSession.getProperty(XFormsSession.UIGENERATOR);
				uiGenerator.setInput(webAdapter.getXForms());
				uiGenerator.setOutput(context.getResponseWriter());
				uiGenerator.generate();
	            
	        } catch (Exception e) {
	            shutdown(webAdapter, session, e, response, request, key);
	        }
		}
		
		super.encodeEnd(context);
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
		getFormBean().setFormId(formId);
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
    protected XFormsSessionManager getXFormsSessionManager(){
       return DefaultXFormsSessionManagerImpl.getInstance();
    }
    
    protected void handleExit(XMLEvent exitEvent, XFormsSession xFormsSession, HttpSession session,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (ChibaEventNames.REPLACE_ALL.equals(exitEvent.getType())) {
//			response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
//					+ "/SubmissionResponse?sessionKey=" + xFormsSession.getKey()));
		}
		else if (ChibaEventNames.LOAD_URI.equals(exitEvent.getType())) {
			if (exitEvent.getContextInfo("show") != null) {
				String loadURI = (String) exitEvent.getContextInfo("uri");
				// kill XFormsSession
				xFormsSession.getManager().deleteXFormsSession(xFormsSession.getKey());
//				response.sendRedirect(response.encodeRedirectURL(loadURI));
			}
		}
		log.fine("Exited during XForms model init");
	}

	/**
	 * configures the an Adapter for interacting with the XForms processor (ChibaBean). The Adapter itself
	 * will create the XFormsProcessor (ChibaBean) and configure it for processing.
	 * <p/>
	 * If you'd like to use a different source of XForms documents e.g. DOM you should extend this class and
	 * overwrite this method. Please take care to also set the baseURI of the processor to a reasonable value
	 * cause this will be the fundament for all URI resolutions taking place.
	 *
	 * @param adapter  the WebAdapter implementation to setup
	 * @param formPath - the relative location where forms are stored
	 * @return ServletAdapter
	 */
	protected WebAdapter setupAdapter(WebAdapter adapter, String sessionKey, FormBean form) throws XFormsException {
		adapter.setXForms(form.getDocument());

// if (processorBase == null || processorBase.equalsIgnoreCase("remote") ) {
//	        adapter.setBaseURI(formPath);
//	    }
//	    else {
//	        adapter.setBaseURI(new File(contextRoot, formsDir).toURI().toString());
//	    }

//	    adapter.setUploadDestination(new File(contextRoot, uploadDir).getAbsolutePath());
	
	    Map servletMap = new HashMap();
	    servletMap.put(WebAdapter.SESSION_ID, sessionKey);
	    adapter.setContextParam(ChibaAdapter.SUBMISSION_RESPONSE, servletMap);
	
	    return adapter;
	}

    /**
	 * stores cookies that may exist in request and passes them on to processor
	 * for usage in HTTPConnectors. Instance loading and submission then uses
	 * these cookies. Important for applications using auth.
	 * 
	 * @param request
	 *            the servlet request
	 * @param adapter
	 *            the WebAdapter instance
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

	protected UIGenerator createUIGenerator(FacesContext context, HttpServletRequest request, String sessionKey)
			throws XFormsConfigException {
		TransformerService transformerService = (TransformerService) getIWMainApplication(context).getAttribute(
				IWBundleStarter.TRANSFORMER_SERVICE);

		XSLTGenerator generator = new XSLTGenerator();
		generator.setTransformerService(transformerService);
		generator.setStylesheetURI(IWBundleStarter.XSLT_URI);
		
		// todo: unify and extract parameter names
		generator.setParameter("contextroot", context.getExternalContext().getRequestContextPath());
		generator.setParameter("scriptPath", "/idegaweb/bundles/" + IWBundleStarter.BUNDLE_IDENTIFIER + "/resources/javascript");
		generator.setParameter("sessionKey", sessionKey);
		
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
		
		return generator;
	}

    protected void shutdown(WebAdapter webAdapter, HttpSession session, Exception e, HttpServletResponse response,
			HttpServletRequest request, String key) {
		// attempt to shutdown processor
		if (webAdapter != null) {
			try {
				webAdapter.shutdown();
			}
			catch (XFormsException xfe) {
				xfe.printStackTrace();
			}
		}

		session.removeAttribute(key);
		// redirect to error page (after encoding session id if required)
		//response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/"
		//		+ request.getSession().getServletContext().getInitParameter("error.page")));
	}


}
