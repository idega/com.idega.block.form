package com.idega.block.form.save;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.xforms.action.AbstractBoundAction;
import org.chiba.xml.xforms.core.Instance;
import org.chiba.xml.xforms.core.Model;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsBindingException;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.presentation.FormViewer;
import com.idega.block.form.submission.XFormSubmission;
import com.idega.block.form.submission.XFormSubmissionInstance;
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
import com.idega.util.CoreUtil;
import com.idega.util.IOUtil;
import com.idega.util.SendMail;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.business.XFormPersistenceType;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.11 $ Last modified: $Date: 2009/05/05 14:11:44 $ by $Author: civilis $
 */
public class SaveFormAction extends AbstractBoundAction {
	
	private Long formId;
	private String instanceId;
	private Instance instance;
	private String submissionRepresentationExp;
	private String submissionIdExp;
	private String action;
	private String submissionElementId;
	
	private String emailExp;
	private String linkExp;
	
	private String submissionUUID;
	
	private boolean overWrite;
	
	@Autowired
	@XFormPersistenceType("slide")
	private PersistenceManager persistenceManager;
	@Autowired
	@TmpFileResolverType("xformVariables")
	private TmpFileResolver tmpFileResolver;
	@Autowired
	@TmpFileResolverType("xformSlide")
	private TmpFilesModifyStrategy tmpFilesModifyStrategy;
	@Autowired
	private FormSavePhasePluginFactory formSavePhasePluginFactory;
	
	private static final String actionSave = "save",
								actionSubmissionComplete = "submissionComplete",
								actionSendLink = "sendLink",
								actionSaveByOverwritingStopOrContinue = "saveOverwriteFormStopOrContinue";
	
	public SaveFormAction(Element element, Model model) {
		super(element, model);
	}
	
	@Override
	public void init() throws XFormsException {
		super.init();
		
		final String action = getXFormsAttribute("action");
		setOverWrite(actionSaveByOverwritingStopOrContinue.equals(action));
		
		if (actionSave.equals(action) || actionSaveByOverwritingStopOrContinue.equals(action)) {
			final String instanceId = getXFormsAttribute("instanceId");
			final String linkExp = getXFormsAttribute("linkLocation");
			final String submissionRepresentationExp = getXFormsAttribute("submissionIdentifier");
			final String submissionIdExp = getXFormsAttribute("submissionId");
			String submissionElementId = getXFormsAttribute("submission");
			final String formIdExp = getXFormsAttribute("formId");
			String formIdStr = null;
			if (!StringUtil.isEmpty(formIdExp))
				formIdStr = XFormsUtil.getValueFromExpression(formIdExp, this);
			
			if (StringUtil.isEmpty(formIdStr))
				throw new XFormsException("No form id resolved from the expression provided=" + formIdExp);
			
			if (StringUtil.isEmpty(submissionIdExp))
				throw new XFormsException("No submissionIdExp location provided, we need to save saved submission id somewhere");
			
			if (StringUtil.isEmpty(submissionElementId))
				submissionElementId = "submit_data_submission";
			
			setLinkExp(linkExp);
			
			setSubmissionRepresentationExp(submissionRepresentationExp);
			
			setSubmissionIdExp(submissionIdExp);
			
			setFormId(new Long(formIdStr));
			setInstanceId(StringUtil.isEmpty(instanceId) ? null : instanceId);
			setSubmissionElementId(submissionElementId);
		} else if (actionSubmissionComplete.equals(action)) {
			final String instanceId = getXFormsAttribute("instanceId");
			final String submissionIdExp = getXFormsAttribute("submissionId");
			
			if (StringUtil.isEmpty(submissionIdExp))
				throw new XFormsException("No submissionIdExp location provided, we need to resolve saved submission id somewhere");
			
			setSubmissionIdExp(submissionIdExp);
			setInstanceId(StringUtil.isEmpty(instanceId) ? null : instanceId);
		} else if (actionSendLink.equals(action)) {
			final String emailExp = getXFormsAttribute("email");
			final String linkExp = getXFormsAttribute("link");
			
			if (StringUtil.isEmpty(emailExp) || StringUtil.isEmpty(linkExp))
				throw new XFormsException("Either expression is not provided. emailExp=" + emailExp + ", linkExp=" + linkExp);
			
			setEmailExp(emailExp);
			setLinkExp(linkExp);
		} else {
			throw new XFormsException("No action specified");
		}
		
		setAction(action);
	}
	
	protected void save() throws XFormsException {
		
		storeSubmission();
		afterSave();
		
		doRebuild(true);
		doRecalculate(true);
		doRevalidate(true);
		doRefresh(true);
	}
	
	private void saveByOverwritting() throws XFormsException {
		boolean success = true;
		try {
			saveSubmission();
			
			doRebuild(true);
			doRecalculate(true);
			doRevalidate(true);
			doRefresh(true);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		success = !StringUtil.isEmpty(getSubmissionUUID());
		
		Instance instance = getInstance();
		Node message = getElement().getParentNode().getNextSibling().getNextSibling();
		if (!(message instanceof Element)  || !message.getNodeName().endsWith(":message"))
			throw new XFormsException("Message element (<xf:message>) can not be found in XForm!");

		String path = "instance('control-instance')/saveForm";
		instance.setNodeValue(path, String.valueOf(success));
	}
	
	protected void afterSave() {
		
		executeAfterSavePluginBasedOnSubmission();
	}
	
	protected void executeAfterSavePluginBasedOnSubmission() {
		
		try {
			XFormSubmission submission = getXFormSubmission();
			String scheme = submission.getScheme();
			Collection<FormSavePhasePlugin> plugins = getFormSavePhasePluginFactory().getPlugins(scheme);
			
			Element instanceElement = getInstance().getElement();
			
			for (FormSavePhasePlugin plugin : plugins) {
				
				FormSavePhasePluginParams params = new FormSavePhasePluginParams();
				params.submissionInstance = new XFormSubmissionInstance(instanceElement);
				params.submissionUUID = getSubmissionUUID();
				plugin.afterSave(params);
			}
		} catch (XFormsException e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while resolving and executing after save plugin", e);
		}
	}
	
	@Override
	public void perform() throws XFormsException {
		super.perform();
		
		if (actionSave.equals(action)) {
			save();
		} else if (actionSubmissionComplete.equals(action)) {
			submissionComplete();
		} else if (actionSendLink.equals(action)) {
			sendLink();
		} else if (actionSaveByOverwritingStopOrContinue.equals(action)) {
			saveByOverwritting();
		}
	}
	
	protected void sendLink() throws XFormsException {
		String email = null;
		String link = null;
		try {
			email = (String) XFormsUtil.getValueFromExpression(getEmailExp(), this);
			link = (String) XFormsUtil.getValueFromExpression(getLinkExp(), this);
		} catch (XFormsException e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while resolving email or link from expressions: emailExp=" + getEmailExp() + ", linkExp=" +
					getLinkExp());
		}
		
		if (StringUtil.isEmpty(email) || StringUtil.isEmpty(link)) {
			String message = "No link or email were resolved from expressions: emailExp=" + getEmailExp() + ", linkExp=" + getLinkExp() + ". Email=" + email + ", link=" + link +
				". Skipping sending email.";
			Logger.getLogger(getClass().getName()).warning(message);
			throw new XFormsException(message);
		} else {
			sendSavedFormLink(email, link);
		}
	}
	
	protected void submissionComplete() {
		
		removeSubmittedData();
	}
	
	protected void removeSubmittedData() {
		
		String submissionUUID = getSubmissionUUID();
		getPersistenceManager().invalidateSubmittedDataByExistingSubmission(
		    submissionUUID);
	}
	
	protected void storeSubmission() {
		
		saveSubmission();
		setSavedFormLink();
	}
	
	private void setSavedFormLink() {
		
		Instance instance = getInstance();
		
		final String submissionUUID = getSubmissionUUID();
		URIUtil uriUtil = getSavedFormPageURI();
		uriUtil.setParameter(FormViewer.submissionIdParam, submissionUUID);
		String url = uriUtil.getUri();
		
		ModelItem mi = instance.getModelItem(getLinkExp());
		mi.setValue(url);
	}
	
	private URIUtil getSavedFormPageURI() {
		
		final IWContext iwc = IWContext.getCurrentInstance();
		BuilderService bs = getBuilderService(iwc);
		String url = bs.getFullPageUrlByPageType(iwc,
		    FormViewer.formviewerPageType, true);
		final URIUtil uriUtil = new URIUtil(url);
		return uriUtil;
	}
	
	private String getSubmissionRepresentationValue() {
		
		Instance instance = getInstance();
		ModelItem submissionRepresentationMI = !StringUtil
		        .isEmpty(getSubmissionRepresentationExp()) ? instance
		        .getModelItem(getSubmissionRepresentationExp()) : null;
		
		String submissionIdentifier = null;
		
		if (submissionRepresentationMI != null) {
			
			submissionIdentifier = submissionRepresentationMI.getValue();
		}
		
		if (StringUtil.isEmpty(submissionIdentifier)) {
			
			submissionIdentifier = String.valueOf(System.currentTimeMillis());
		}
		
		return submissionIdentifier;
	}
	
	protected void saveSubmission() {
		// TODO: check, if form is in firm state, if not - take it
		// TODO: refactor this method (split)
		
		InputStream stream = null;
		try {
			Long formId = getFormId();
			Instance instance = getInstance();
			String submissionRepresentation = getSubmissionRepresentationValue();
			
			// checking if submission already contains submissionId - therefore we're reusing
			// existing submissionId
			
			ModelItem submissionIdMI = instance.getModelItem(getSubmissionIdExp());
			String submissionUUID = getSubmissionUUID();
			
			if (StringUtil.isEmpty(submissionUUID))
				submissionUUID = null;
			
			Element instanceEl = ((Document) instance.getPointer(".").getNode()).getDocumentElement();
			
			// storing uploaded files to persistent location
			getTmpFileResolver().replaceAllFiles(instanceEl, getTmpFilesModifyStrategy());
			
			stream = getISFromXML(instanceEl);
			
			if (submissionUUID != null) {
				boolean old = false;
				if (submissionUUID.length() != 36) {
					old = true;
				}
				
				submissionUUID = getPersistenceManager().saveSubmittedDataByExistingSubmission(submissionUUID, formId, stream, submissionRepresentation, getFormSubmitterId());
				
				if (old) {
					submissionIdMI.setValue(submissionUUID);
					
					instanceEl = ((Document) instance.getPointer(".").getNode()).getDocumentElement();
					
					stream = getISFromXML(instanceEl);
					
					// restore with new modified submission data
					submissionUUID = getPersistenceManager().saveSubmittedDataByExistingSubmission(submissionUUID, formId, stream, submissionRepresentation, getFormSubmitterId());
				}
			} else {
				submissionUUID = getPersistenceManager().saveSubmittedData(formId, stream, submissionRepresentation, false, getFormSubmitterId());
				
				submissionIdMI.setValue(submissionUUID);
				
				instanceEl = ((Document) instance.getPointer(".").getNode()).getDocumentElement();
				
				stream = getISFromXML(instanceEl);
				
				// restore with new modified submission data
				submissionUUID = getPersistenceManager().saveSubmittedDataByExistingSubmission(submissionUUID, formId, stream, submissionRepresentation, getFormSubmitterId());
			}
			
			setSubmissionUUID(submissionUUID);
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Exception while saving submission", e);
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(stream);
		}
	}
	
	private Integer getFormSubmitterId() {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc != null && iwc.isLoggedOn()) {
			return iwc.getCurrentUserId();
		}
		return null;
	}
	
	protected String getSubmissionUUID() {
		
		if (submissionUUID == null) {
			
			ModelItem submissionIdMI = getInstance().getModelItem(
			    getSubmissionIdExp());
			submissionUUID = submissionIdMI.getValue();
		}
		
		return submissionUUID;
	}
	
	protected void sendSavedFormLink(String email, String url) {
		
		if (!StringUtil.isEmpty(email) && !StringUtil.isEmpty(url)) {
			
			IWContext iwc = IWContext.getCurrentInstance();
			IWResourceBundle iwrb = getResourceBundle(iwc);
			
			String from = iwc.getApplicationSettings().getProperty(
			    CoreConstants.PROP_SYSTEM_MAIL_FROM_ADDRESS, CoreConstants.EMAIL_DEFAULT_FROM);
			String host = iwc.getApplicationSettings().getProperty(
			    CoreConstants.PROP_SYSTEM_SMTP_MAILSERVER, CoreConstants.EMAIL_DEFAULT_HOST);
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
	
	Long getFormId() {
		return formId;
	}
	
	void setFormId(Long formId) {
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
	
	protected Instance getInstance() {
		
		if (instance == null) {
			
			if (getInstanceId() != null) {
				instance = getModel().getInstance(getInstanceId());
			} else
				instance = getModel().getDefaultInstance();
		}
		
		return instance;
	}
	
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
	
	protected XFormSubmission getXFormSubmission() throws XFormsException {
		
		String submissionElementId = getSubmissionElementId();
		Object submissionObject = this.container.lookup(submissionElementId);
		
		if (submissionObject == null
		        || !(submissionObject instanceof Submission)) {
			throw new XFormsBindingException(
			        "invalid submission id at " + this, this.target,
			        submissionElementId);
		}
		
		return new XFormSubmission((Submission) submissionObject);
	}
	
	String getSubmissionElementId() {
		return submissionElementId;
	}
	
	void setSubmissionElementId(String submissionElementId) {
		this.submissionElementId = submissionElementId;
	}
	
	FormSavePhasePluginFactory getFormSavePhasePluginFactory() {
		
		if (formSavePhasePluginFactory == null)
			ELUtil.getInstance().autowire(this);
		
		return formSavePhasePluginFactory;
	}
	
	String getSubmissionRepresentationExp() {
		return submissionRepresentationExp;
	}
	
	void setSubmissionRepresentationExp(String submissionRepresentationExp) {
		this.submissionRepresentationExp = submissionRepresentationExp;
	}
	
	void setSubmissionUUID(String submissionUUID) {
		this.submissionUUID = submissionUUID;
	}

	protected boolean isOverWrite() {
		return overWrite;
	}

	protected void setOverWrite(boolean overWrite) {
		this.overWrite = overWrite;
	}
	
}