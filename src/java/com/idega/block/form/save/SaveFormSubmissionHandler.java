package com.idega.block.form.save;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.chiba.adapter.ChibaAdapter;
import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.presentation.FormViewer;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.file.tmp.TmpFileResolver;
import com.idega.core.file.tmp.TmpFileResolverType;
import com.idega.core.file.tmp.TmpFilesModifyStrategy;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.SendMail;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.xml.XPathUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.8 $
 *
 * Last modified: $Date: 2008/06/28 18:59:19 $ by $Author: civilis $
 */
public class SaveFormSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    
	public static final String SUBMITTED_DATA_PATH = "/files/forms/submissions";
	private static final String sendFormLinkEmailParam = "SEND_SAVED_FORM_LINK";
	
	private PersistenceManager persistenceManager;
	@Autowired @TmpFileResolverType("xformVariables")
	private TmpFileResolver tmpFileResolver;
	@Autowired @TmpFileResolverType("xformSlide")
	private TmpFilesModifyStrategy tmpFilesModifyStrategy;
	
	public Map<String, Object> submit(Submission submission, Node instance) throws XFormsException {
    	
    	checkSubmissionActions(submission);
    	
    	DOMUtil.prettyPrintDOM(instance);
    	
    	if(submission.getReplace().equals("instance")) {
    		
//    		stage 1 -> save submission and generate link
    		
    		String formId = FormManagerUtil.getFormId(instance);
        	
        	if(formId != null && !CoreConstants.EMPTY.equals(formId)) {
        		
        		ELUtil.getInstance().autowire(this);
        		Long fid = new Long(formId);
        		
//        		TODO: check, if form is in firm state, if not - take it
        		
    			String[] submissionMeta = saveSubmission(fid, instance);
    			
    			InputStream is = getSavedResponse(submissionMeta[0], submissionMeta[1], instance);
    			HashMap<String, Object> response = new HashMap<String, Object>(1);

//    			TODO: what's the non-deprecated way of doing it?
    	        response.put(ChibaAdapter.SUBMISSION_RESPONSE_STREAM, is);
                return response;
        	}
    		
    	} else {
    		
    		String action = submission.getElement().getAttribute(FormManagerUtil.action_att);
        	Map<String, String> parameters = new URIUtil(action).getParameters();
        	
        	if(parameters.containsKey(sendFormLinkEmailParam)) {

//        		stage 2 -> send email with the link
        		sendSavedFormLink(instance);
        	}
    	}

        return null;
    }
    
    protected String[] saveSubmission(Long fid, Node instance) {
		
//		TODO: check, if form is in firm state, if not - take it
		
		try {

//			trying to find form identifier from standard node (@nodeType='formIdentifier')
			XPathUtil u = new XPathUtil(".//*[@nodeType='formIdentifier']");
			Element el = (Element)u.getNode(instance);
			
			final String submissionIdentifier;
			
			if(el != null)
				submissionIdentifier = el.getTextContent();
			else {
				submissionIdentifier = String.valueOf(System.currentTimeMillis());
			}

//			checking if submission already contains submissionId - therefore we're reusing existing submissionId
			u = new XPathUtil(".//saveFormData/submissionId");
			el = (Element)u.getNode(instance);
			String submissionIdStr = el != null ? el.getTextContent() : null;
			
			if(StringUtil.isEmpty(submissionIdStr))
				submissionIdStr = null;

//			TODO: works incorrectly now, should store submission id, probably create submission instance before saving or smth
			Long submissionId = submissionIdStr != null ? new Long(submissionIdStr) : null;
			
//			storing uploaded files to persistent location
			getTmpFileResolver().replaceAllFiles(instance, getTmpFilesModifyStrategy());
			
			InputStream is = getISFromXML(instance);
			
			if(submissionId != null) {
				submissionId = getPersistenceManager().saveSubmittedDataByExistingSubmission(submissionId, fid, is, submissionIdentifier);
			} else {
				
				submissionId = getPersistenceManager().saveSubmittedData(fid, is, submissionIdentifier);
				
				
				el.setTextContent(submissionId.toString());
				is = getISFromXML(instance);
				
//				restore with new modified submission data
				submissionId = getPersistenceManager().saveSubmittedDataByExistingSubmission(submissionId, fid, is, submissionIdentifier);
			}
			
			return new String[] {submissionId.toString(), submissionIdentifier};
			
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while saving submission", e);
			throw new RuntimeException(e);
		}
	}
    
    protected InputStream getSavedResponse(String submissionId, String submissionIdentifier, Node instance) {
		
    	try {
//    		resolving url to formviewer and setting submission param
    		IWContext iwc = IWContext.getCurrentInstance();
    		BuilderService bs = getBuilderService(iwc);
    		
    		String url = bs.getFullPageUrlByPageType(iwc, FormViewer.formviewerPageType, true);
    		final URIUtil uriUtil = new URIUtil(url);
    		uriUtil.setParameter(FormViewer.submissionIdParam, submissionId);
    		url = uriUtil.getUri();
    		
//    		placing link and saved form identifier in response
    		
    		XPathUtil u = new XPathUtil(".//saveFormData/formIdentifier");
    		Element el = (Element)u.getNode(instance);
    		el.setTextContent(submissionIdentifier);
    		u = new XPathUtil(".//saveFormData/linkToForm");
    		el = (Element)u.getNode(instance);
    		el.setTextContent(url);
    		
    		InputStream is = getISFromXML(instance);
    		return is;
			
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while creating response input stream", e);
			throw new RuntimeException(e);
		}
	}
    
    protected void sendSavedFormLink(Node instance) {
		
    	XPathUtil u = new XPathUtil(".//email");
		Element el = (Element)u.getNode(instance);
		String email = el != null ? el.getTextContent() : null;
		
		u = new XPathUtil(".//link");
		el = (Element)u.getNode(instance);
		String link = el != null ? el.getTextContent() : null;
		
		if(!StringUtil.isEmpty(email) && !StringUtil.isEmpty(link)) {
			
			IWContext iwc = IWContext.getCurrentInstance();
			IWResourceBundle iwrb = getResourceBundle(iwc);
			
			String from = iwc.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_MAIL_FROM_ADDRESS, "staff@idega.is");
			String host = iwc.getApplicationSettings().getProperty(CoreConstants.PROP_SYSTEM_SMTP_MAILSERVER, "mail.idega.is");
			String subject = iwrb.getLocalizedString("save_form.linkToForm.subject", "Link to your saved form");
			String text = iwrb.getLocalizedAndFormattedString("save_form.linkToForm.text", "Link to your saved form: {0}", new Object[] {link});
			
			try {
				SendMail.send(from, email, null, null, host, subject, text);
			} catch (javax.mail.MessagingException me) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while sending participant invitation message", me);
			}
		} else {
			throw new IllegalArgumentException("Either is not provided: email="+email+", link="+link);
		}
	}
    
    private InputStream getISFromXML(Node node) throws TransformerException {
    	
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtil.prettyPrintDOM(node, out);
		return new ByteArrayInputStream(out.toByteArray());
    }
    
    private void checkSubmissionActions(Submission submission) throws XFormsException {
    	if(!submission.getMethod().equalsIgnoreCase("post"))
    		throw new XFormsException("submission method '" + submission.getMethod() + "' not supported");
    	
    	if (!submission.getReplace().equals("instance") && !submission.getReplace().equals("none"))
            throw new XFormsException("submission mode '" + submission.getReplace() + "' not supported");
    }
    
	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	@Autowired
	@XFormPersistenceType("slide")
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}
	
	protected BuilderService getBuilderService(IWApplicationContext iwc) {
		try {
			return BuilderServiceFactory.getBuilderService(iwc);
			
		} catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}
	
	protected IWResourceBundle getResourceBundle(IWContext iwc) {
		IWMainApplication app = iwc.getIWMainApplication();
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		
		if(bundle != null) {
			return bundle.getResourceBundle(iwc);
		} else {
			return null;
		}
	}
	
	public TmpFileResolver getTmpFileResolver() {
		return tmpFileResolver;
	}

	public TmpFilesModifyStrategy getTmpFilesModifyStrategy() {
		return tmpFilesModifyStrategy;
	}
}