package com.idega.block.form.save;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.xforms.action.AbstractBoundAction;
import org.chiba.xml.xforms.core.Instance;
import org.chiba.xml.xforms.core.Model;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.presentation.FormViewer;
import com.idega.business.IBORuntimeException;
import com.idega.chiba.web.xml.xforms.util.XFormsUtil;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.file.tmp.TmpFileResolver;
import com.idega.core.file.tmp.TmpFileResolverType;
import com.idega.core.file.tmp.TmpFilesModifyStrategy;
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
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.business.XFormPersistenceType;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.8 $ Last modified: $Date: 2009/01/20 17:53:54 $ by $Author: civilis $
 */
public class SaveFormAction extends AbstractBoundAction {
	
	private String formId;
	private String instanceId;
	private Instance instance;
	private String submissionIdentifier;
	private String submissionIdExp;
	private String action;
	
	private String emailExp;
	private String linkExp;
	
	@Autowired
	@XFormPersistenceType("slide")
	private PersistenceManager persistenceManager;
	@Autowired
	@TmpFileResolverType("xformVariables")
	private TmpFileResolver tmpFileResolver;
	@Autowired
	@TmpFileResolverType("xformSlide")
	private TmpFilesModifyStrategy tmpFilesModifyStrategy;
	
	private static final String actionSave = "save";
	private static final String actionSubmissionComplete = "submissionComplete";
	private static final String actionSendLink = "sendLink";
	
	public SaveFormAction(Element element, Model model) {
		super(element, model);
	}
	
	@Override
	public void init() throws XFormsException {
		super.init();
		
		final String action = getXFormsAttribute("action");
		
		if (actionSave.equals(action)) {
			
			final String instanceId = getXFormsAttribute("instanceId");
			final String linkExp = getXFormsAttribute("linkLocation");
			final String submissionIdentifier = getXFormsAttribute("submissionIdentifier");
			final String submissionIdExp = getXFormsAttribute("submissionId");
			final String formIdExp = getXFormsAttribute("formId");
			final Object formIdVal = formIdExp != null
			        && formIdExp.length() != 0 ? XFormsUtil
			        .getValueFromExpression(formIdExp, this) : null;
			
			if (formIdVal == null)
				throw new XFormsException(
				        "No form id resolved from the expression provided="
				                + formIdExp);
			
			if (submissionIdentifier == null
			        || submissionIdentifier.length() == 0)
				throw new XFormsException(
				        "No submissionIdentifier location provided, we need to save submission identifier somewhere");
			
			if (submissionIdExp == null || submissionIdExp.length() == 0)
				throw new XFormsException(
				        "No submissionIdExp location provided, we need to save saved submission id somewhere");
			
			setLinkExp(linkExp);
			setSubmissionIdentifier(submissionIdentifier);
			setSubmissionIdExp(submissionIdExp);
			
			setFormId(formIdVal.toString());
			setInstanceId(instanceId == null || instanceId.length() == 0 ? null
			        : instanceId);
			
		} else if (actionSubmissionComplete.equals(action)) {
			
			final String instanceId = getXFormsAttribute("instanceId");
			final String submissionIdExp = getXFormsAttribute("submissionId");
			
			if (submissionIdExp == null || submissionIdExp.length() == 0)
				throw new XFormsException(
				        "No submissionIdExp location provided, we need to resolve saved submission id somewhere");
			
			setSubmissionIdExp(submissionIdExp);
			setInstanceId(instanceId == null || instanceId.length() == 0 ? null
			        : instanceId);
			
		} else if (actionSendLink.equals(action)) {
			
			final String emailExp = getXFormsAttribute("email");
			final String linkExp = getXFormsAttribute("link");
			
			if (emailExp == null || emailExp.length() == 0 || linkExp == null
			        || linkExp.length() == 0)
				throw new XFormsException(
				        "Either expression is not provided. emailExp="
				                + emailExp + ", linkExp=" + linkExp);
			
			setEmailExp(emailExp);
			setLinkExp(linkExp);
			
		} else {
			throw new XFormsException("No action specified");
		}
		
		setAction(action);
	}
	
	public void perform() throws XFormsException {
		super.perform();
		
		if (actionSave.equals(action)) {
			
			storeSubmission();
			
			doRebuild(true);
			doRecalculate(true);
			doRevalidate(true);
			doRefresh(true);
			
		} else if (actionSubmissionComplete.equals(action)) {
			removeSubmittedData();
			
		} else if (actionSendLink.equals(action)) {
			
			try {
				String email = (String) XFormsUtil.getValueFromExpression(
				    getEmailExp(), this);
				String link = (String) XFormsUtil.getValueFromExpression(
				    getLinkExp(), this);
				
				if (email == null || email.length() == 0 || link == null
				        || link.length() == 0) {
					
					Logger.getLogger(getClass().getName()).log(
					    Level.WARNING,
					    "No link or email were resolved from expressions: emailExp="
					            + getEmailExp() + ", linkExp=" + getLinkExp()
					            + ". Email=" + email + ", link=" + link
					            + ". Skipping sending email.");
					// TODO: we could send event of the error here, and form would inform user about
					// this
					
				} else {
					sendSavedFormLink(email, link);
				}
				
			} catch (XFormsException e) {
				Logger.getLogger(getClass().getName()).log(
				    Level.SEVERE,
				    "Exception while resolving email or link from expressions: emailExp="
				            + getEmailExp() + ", linkExp=" + getLinkExp());
			}
		}
	}
	
	protected void removeSubmittedData() {
		
		String submissionUUID = getSubmissionUUID();
		getPersistenceManager().invalidateSubmittedDataByExistingSubmission(
		    submissionUUID);
	}
	
	protected void storeSubmission() {
		
		String formId = getFormId();
		
		Long fid = new Long(formId);
		
		Instance instance = getInstance();
		
		final String[] submissionMeta = saveSubmission(fid, instance);
		final String submissionUUID = submissionMeta[0];
		final String submissionIdentifier = submissionMeta[1];
		
		final IWContext iwc = IWContext.getCurrentInstance();
		
		BuilderService bs = getBuilderService(iwc);
		String url = bs.getFullPageUrlByPageType(iwc,
		    FormViewer.formviewerPageType, true);
		final URIUtil uriUtil = new URIUtil(url);
		uriUtil.setParameter(FormViewer.submissionIdParam, submissionUUID);
		url = uriUtil.getUri();
		
		ModelItem mi = instance.getModelItem(getLinkExp());
		
		mi.setValue(url);
		
		mi = instance.getModelItem(getSubmissionIdentifier());
		
		mi.setValue(submissionIdentifier);
	}
	
	protected String[] saveSubmission(Long fid, Instance instance) {
		
		// TODO: check, if form is in firm state, if not - take it
		
		try {
			ModelItem submissionIdentifierMI = instance
			        .getModelItem(getSubmissionIdentifier());
			
			String submissionIdentifier = null;
			
			if (submissionIdentifierMI != null)
				submissionIdentifier = submissionIdentifierMI.getValue();
			
			if (submissionIdentifier == null
			        || submissionIdentifier.length() == 0) {
				
				submissionIdentifier = String.valueOf(System
				        .currentTimeMillis());
				submissionIdentifierMI.setValue(submissionIdentifier);
			}
			
			// checking if submission already contains submissionId - therefore we're reusing
			// existing submissionId
			
			ModelItem submissionIdMI = instance
			        .getModelItem(getSubmissionIdExp());
			String submissionUUID = getSubmissionUUID();
			
			if (StringUtil.isEmpty(submissionUUID))
				submissionUUID = null;
			
			Element instanceEl = ((Document) instance.getPointer(".").getNode())
			        .getDocumentElement();
			
			// storing uploaded files to persistent location
			getTmpFileResolver().replaceAllFiles(instanceEl,
			    getTmpFilesModifyStrategy());
			
			InputStream is = getISFromXML(instanceEl);
			
			if (submissionUUID != null) {
				
				boolean old = false;
				if (submissionUUID.length() != 36) {
					old = true;
				}
				
				submissionUUID = getPersistenceManager()
				        .saveSubmittedDataByExistingSubmission(submissionUUID,
				            fid, is, submissionIdentifier);
				
				if (old) {
					
					submissionIdMI.setValue(submissionUUID);
					
					instanceEl = ((Document) instance.getPointer(".").getNode())
					        .getDocumentElement();
					
					is = getISFromXML(instanceEl);
					
					// restore with new modified submission data
					submissionUUID = getPersistenceManager()
					        .saveSubmittedDataByExistingSubmission(
					            submissionUUID, fid, is, submissionIdentifier);
				}
				
			} else {
				
				submissionUUID = getPersistenceManager().saveSubmittedData(fid,
				    is, submissionIdentifier, false);
				
				submissionIdMI.setValue(submissionUUID);
				
				instanceEl = ((Document) instance.getPointer(".").getNode())
				        .getDocumentElement();
				
				is = getISFromXML(instanceEl);
				
				// restore with new modified submission data
				submissionUUID = getPersistenceManager()
				        .saveSubmittedDataByExistingSubmission(submissionUUID,
				            fid, is, submissionIdentifier);
			}
			
			return new String[] { submissionUUID, submissionIdentifier };
			
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE,
			    "Exception while saving submission", e);
			throw new RuntimeException(e);
		}
	}
	
	private String getSubmissionUUID() {
		
		ModelItem submissionIdMI = getInstance().getModelItem(
		    getSubmissionIdExp());
		String submissionUUID = submissionIdMI.getValue();
		
		return submissionUUID;
	}
	
	protected void sendSavedFormLink(String email, String url) {
		
		if (!StringUtil.isEmpty(email) && !StringUtil.isEmpty(url)) {
			
			IWContext iwc = IWContext.getCurrentInstance();
			IWResourceBundle iwrb = getResourceBundle(iwc);
			
			String from = iwc.getApplicationSettings().getProperty(
			    CoreConstants.PROP_SYSTEM_MAIL_FROM_ADDRESS, "staff@idega.is");
			String host = iwc.getApplicationSettings().getProperty(
			    CoreConstants.PROP_SYSTEM_SMTP_MAILSERVER, "mail.idega.is");
			String subject = iwrb.getLocalizedString(
			    "save_form.linkToForm.subject", "Link to your saved form");
			String text = iwrb.getLocalizedAndFormattedString(
			    "save_form.linkToForm.text", "Link to your saved form: {0}",
			    new Object[] { url });
			
			try {
				SendMail.send(from, email, null, null, host, subject, text);
			} catch (javax.mail.MessagingException me) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE,
				    "Exception while sending participant invitation message",
				    me);
			}
		} else {
			throw new IllegalArgumentException("Either is not provided: email="
			        + email + ", link=" + url);
		}
	}
	
	String getFormId() {
		return formId;
	}
	
	void setFormId(String formId) {
		this.formId = formId;
	}
	
	private InputStream getISFromXML(Node node) throws TransformerException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtil.prettyPrintDOM(node, out);
		return new ByteArrayInputStream(out.toByteArray());
	}
	
	private IWResourceBundle getResourceBundle(IWContext iwc) {
		IWMainApplication app = iwc.getIWMainApplication();
		IWBundle bundle = app.getBundle(IWBundleStarter.BUNDLE_IDENTIFIER);
		
		if (bundle != null) {
			return bundle.getResourceBundle(iwc);
		} else {
			return null;
		}
	}
	
	private Instance getInstance() {
		
		if (instance == null) {
			
			if (getInstanceId() != null) {
				instance = getModel().getInstance(getInstanceId());
			} else
				instance = getModel().getDefaultInstance();
		}
		
		return instance;
	}
	
	/*
	private Element getLinkLocationElement() {
		
		if(linkLocationElement == null && getLinkExp() != null) {
			
			try {
				Pointer p = XFormsUtil.getPointerFromExpression(getLinkExp(), this);
				linkLocationElement = (Element)p.getNode();
				
			} catch (XFormsException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception while resolving linkLocationElement from the expression="+getLinkExp(), e);
			}
		}
		
		return linkLocationElement;
	}
	*/

	PersistenceManager getPersistenceManager() {
		
		if (persistenceManager == null)
			ELUtil.getInstance().autowire(this);
		
		return persistenceManager;
	}
	
	TmpFileResolver getTmpFileResolver() {
		
		if (tmpFileResolver == null)
			ELUtil.getInstance().autowire(this);
		
		return tmpFileResolver;
	}
	
	TmpFilesModifyStrategy getTmpFilesModifyStrategy() {
		
		if (tmpFilesModifyStrategy == null)
			ELUtil.getInstance().autowire(this);
		
		return tmpFilesModifyStrategy;
	}
	
	@Override
	public String getInstanceId() {
		return instanceId;
	}
	
	void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	private BuilderService getBuilderService(IWApplicationContext iwc) {
		try {
			return BuilderServiceFactory.getBuilderService(iwc);
			
		} catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}
	
	String getSubmissionIdentifier() {
		return submissionIdentifier;
	}
	
	/*
	Element getSubmissionIdentifierElement() {
		
		if(submissionIdentifierElement == null) {
			
			try {
				Pointer p = XFormsUtil.getPointerFromExpression(getSubmissionIdentifier(), this);
				submissionIdentifierElement = (Element)p.getNode();
				
	//				submissionIdentifierElement = (Element)XFormsUtil.getValueFromExpression(getSubmissionIdentifier(), this);
				
			} catch (XFormsException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception while resolving submissionIdentifierElement from the expression="+getSubmissionIdentifier(), e);
			}
		}
		
		return submissionIdentifierElement;
	}
	*/

	void setSubmissionIdentifier(String submissionIdentifier) {
		this.submissionIdentifier = submissionIdentifier;
	}
	
	String getAction() {
		return action;
	}
	
	void setAction(String action) {
		this.action = action;
	}
	
	String getEmailExp() {
		return emailExp;
	}
	
	String getLinkExp() {
		return linkExp;
	}
	
	void setEmailExp(String emailExp) {
		this.emailExp = emailExp;
	}
	
	void setLinkExp(String linkExp) {
		this.linkExp = linkExp;
	}
	
	String getSubmissionIdExp() {
		return submissionIdExp;
	}
	
	void setSubmissionIdExp(String submissionIdExp) {
		this.submissionIdExp = submissionIdExp;
	}
}