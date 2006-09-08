/*
 * $Id: FormViewer.java,v 1.1 2006/09/08 14:03:14 gediminas Exp $ Created on Aug
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
import java.rmi.RemoteException;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.exception.XFormsException;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.webface.WFUtil;

/**
 * 
 * Last modified: $Date: 2006/09/08 14:03:14 $ by $Author: gediminas $
 * 
 * @author <a href="mailto:gediminas@idega.com">gediminas</a>
 * @version $Revision: 1.1 $
 */
public class FormViewer extends IWBaseComponent {

	//private static String RENDERER_TYPE = "xforms";
		
	private ChibaBean chiba;
	
	private String uri;
	
	public String getRendererType() {
		return null;
		//return RENDERER_TYPE;
	}

	@Override
	protected void initializeComponent(FacesContext context) {
		
		InputStream stream = null;

		try {
			stream = getIWSlideSession().getInputStream(uri);
		}
		catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		chiba = new ChibaBean();
		try {
			chiba.setXMLContainer(stream);
			chiba.setBaseURI(uri);
			chiba.init();
		}
		catch (XFormsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void encodeEnd(FacesContext ctx) throws IOException {
		ResponseWriter out = ctx.getResponseWriter();
		
		out.startElement("p", this);
		out.write("uri = ");
		out.startElement("code", this);
		out.write(uri != null ? uri : "(null)");
		out.endElement("code");
		out.endElement("p");
		
		if (chiba != null) {
			out.startElement("pre", this);
			out.write(chiba.toString());
			out.endElement("pre");
		}
		else {
			out.write("Could not load xml document from slide");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}

    /**
     *
     * creates and configures the UI generating component.
     * @param request
     * @param sessionKey
     * @param actionURL
     * @param xslFile
     * @param javascriptPresent
     * @return
     * @throws XFormsException
     */
     /*
    protected UIGenerator createUIGenerator(HttpServletRequest request,
                                            String sessionKey,
                                            String actionURL,
                                            String xslFile) throws XFormsException {
        StylesheetLoader stylesheetLoader = new StylesheetLoader(stylesPath);
        if (xslFile != null){
            stylesheetLoader.setStylesheetFile(xslFile);
        }
        UIGenerator uiGenerator = new XSLTGenerator(stylesheetLoader);

        //set parameters
        uiGenerator.setParameter("contextroot",request.getContextPath());
        uiGenerator.setParameter("sessionKey",sessionKey);
        uiGenerator.setParameter("action-url",actionURL);
        uiGenerator.setParameter("debug-enabled",String.valueOf(LOGGER.isDebugEnabled()));
        String selectorPrefix = Config.getInstance().getProperty(HttpRequestHandler.SELECTOR_PREFIX_PROPERTY,
                                                                 HttpRequestHandler.SELECTOR_PREFIX_DEFAULT);
        uiGenerator.setParameter("selector-prefix", selectorPrefix);
        String removeUploadPrefix = Config.getInstance().getProperty(HttpRequestHandler.REMOVE_UPLOAD_PREFIX_PROPERTY,
                                                                     HttpRequestHandler.REMOVE_UPLOAD_PREFIX_DEFAULT);
        uiGenerator.setParameter("remove-upload-prefix", removeUploadPrefix);
        String dataPrefix = Config.getInstance().getProperty("chiba.web.dataPrefix");
        uiGenerator.setParameter("data-prefix", dataPrefix);

        String triggerPrefix = Config.getInstance().getProperty("chiba.web.triggerPrefix");
        uiGenerator.setParameter("trigger-prefix", triggerPrefix);

        uiGenerator.setParameter("user-agent", request.getHeader("User-Agent"));

        uiGenerator.setParameter("scripted","true");
        
        if(scriptPath != null){
            uiGenerator.setParameter("scriptPath",scriptPath);
            LOGGER.warn("Script path not configured");
        }
        if(cssPath != null){
            uiGenerator.setParameter("CSSPath",cssPath);
            LOGGER.warn("CSS path not configured");
        }

        return uiGenerator;
    }
    */
	
	public String getUri() {
		return uri;
	}

	
	public void setUri(String uri) {
		this.uri = uri;
	}
}
