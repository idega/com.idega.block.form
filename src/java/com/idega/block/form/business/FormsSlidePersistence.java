package com.idega.block.form.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.chiba.xml.dom.DOMUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import com.idega.block.form.data.XForm;
import com.idega.block.form.data.XFormSubmission;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.persistence.Param;
import com.idega.xformsmanager.business.DocumentManager;
import com.idega.xformsmanager.business.DocumentManagerFactory;
import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.FormLockException;
import com.idega.xformsmanager.business.PersistedFormDocument;
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.business.Submission;
import com.idega.xformsmanager.business.SubmittedDataBean;
import com.idega.xformsmanager.business.XFormPersistenceType;
import com.idega.xformsmanager.business.XFormState;
import com.idega.xformsmanager.component.FormDocument;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.util.CoreConstants;
import com.idega.util.xml.XmlUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.23 $
 *
 * Last modified: $Date: 2008/11/05 08:50:34 $ by $Author: civilis $
 */
@Scope("singleton")
@XFormPersistenceType("slide")
@Repository("xformsPersistenceManager")
public class FormsSlidePersistence implements PersistenceManager {

	private static final long serialVersionUID = 1790429880309352062L;
	
	private static final String slideStorageType = "slide";
	private static final String standaloneFormType = "standalone";
	private static final String submissionFileName = "submission.xml";

	private final Logger logger;
	private IWApplicationContext iwac;
	private XFormsDAO xformsDAO;
	private DocumentManagerFactory documentManagerFactory;

	public static final String FORMS_PATH = "/files/forms";
	public static final String STANDALONE_FORMS_PATH = FORMS_PATH+"/standalone";
	public static final String FORMS_FILE_EXTENSION = ".xhtml";
	public static final String SUBMITTED_DATA_PATH = "/files/forms/submissions";

	public FormsSlidePersistence() {
		logger = Logger.getLogger(getClass().getName());
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
	protected String getFormResourcePath(String formType, String formIdentifier, boolean withFile) {
		
		StringBuilder b = new StringBuilder(FORMS_PATH).append(CoreConstants.SLASH).
		append(formType).append(CoreConstants.SLASH);
		
		if(withFile) {
			b = b
			.append(formIdentifier)
			.append(FORMS_FILE_EXTENSION);
		}
		return b.toString();
	}
	
	
	@Transactional(readOnly=true)
	public PersistedFormDocument loadForm(Long formId) {
		
		XForm xform = getXformsDAO().find(XForm.class, formId);
		
		String formPath = xform.getFormStorageIdentifier();
		Document xformsDoc = loadXMLResourceFromSlide(formPath);
		
		PersistedFormDocument formDoc = new PersistedFormDocument();
		formDoc.setFormId(formId);
		formDoc.setFormType(xform.getFormType());
		formDoc.setXformsDocument(xformsDoc);
		
		return formDoc;
	}
	
	@Transactional(readOnly=true)
	public PersistedFormDocument loadPopulatedForm(Long submissionId) {
		
		XFormSubmission xformSubmission = getXformsDAO().find(XFormSubmission.class, submissionId);
		final PersistedFormDocument formDoc;
		
		if(xformSubmission != null) {
			
			XForm xform = xformSubmission.getXform();
			
			String formPath = xform.getFormStorageIdentifier();
			Document xformsDoc = loadXMLResourceFromSlide(formPath);
			
			Document submissionDoc = xformSubmission.getSubmissionDocument();
			
//			TODO: load with submitted data
			
			DocumentManager documentManager = getDocumentManagerFactory().newDocumentManager(null);
			com.idega.xformsmanager.business.Document form = documentManager.openForm(xformsDoc);
			
			form.populateSubmissionDataWithXML(submissionDoc, true);
			form.setReadonly(true);
			xformsDoc = form.getXformsDocument();
			
			formDoc = new PersistedFormDocument();
			formDoc.setFormId(xform.getFormId());
			formDoc.setFormType(xform.getFormType());
			formDoc.setXformsDocument(xformsDoc);
			
		} else {
			logger.log(Level.WARNING, "No submission found by submissionId provided="+submissionId);
			formDoc = null;
		}
		
		return formDoc;
	}
	
	protected Document loadXMLResourceFromSlide(String resourcePath) {
	
		try {
			WebdavExtendedResource resource = getWebdavExtendedResource(resourcePath);

			if(!resource.exists())
				throw new IllegalArgumentException("Expected webdav resource doesn't exist. Path provided: "+resourcePath);
			
			InputStream is = resource.getMethodData();
			DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
			Document resourceDocument = docBuilder.parse(is);
			return resourceDocument;
			
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
//	protected WebdavExtendedResource loadSubmittedDataFolderResource(String form_id) {
//		
//		try {
//			WebdavExtendedResource webdav_resource = getWebdavExtendedResource(getSubmittedDataResourcePath(form_id, ""));
//
//			return !webdav_resource.exists() ? null : webdav_resource;
//			
//		} catch (IOException e) {
//			logger.log(Level.SEVERE, "Error loading form submitted data folder from Webdav: " + form_id, e);
//			return null;
//		}
//	}
	
	protected void saveExistingXFormsDocumentToSlide(Document xformsDoc, String path) {

		try {
			WebdavExtendedResource xformRes = getWebdavExtendedResource(path);

			if(!xformRes.exists())
				throw new IllegalArgumentException("Expected webdav resource doesn't exist. Path provided: "+path);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtil.prettyPrintDOM(xformsDoc, out);
			InputStream is = new ByteArrayInputStream(out.toByteArray());
			xformRes.putMethod(is);
			is.close();
		
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String saveXFormsDocumentToSlide(Document xformsDoc, String formIdentifier, String formType) {
		
		try {
			String formResourcePath = getFormResourcePath(formType, formIdentifier, true);
			
			String pathToFileFolder = getFormResourcePath(formType, formIdentifier, false);
			String fileName = formIdentifier + FORMS_FILE_EXTENSION;
			
			IWSlideService slideService = getIWSlideService();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtil.prettyPrintDOM(xformsDoc, out);
			InputStream is = new ByteArrayInputStream(out.toByteArray());
			slideService.uploadFileAndCreateFoldersFromStringAsRoot(pathToFileFolder, fileName, is, "text/xml", false);
			is.close();
			
			return formResourcePath;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Transactional(readOnly=false)
	public PersistedFormDocument saveForm(FormDocument document) throws IllegalAccessException {

		if(document == null)
			throw new NullPointerException("FormDocument not provided");
		
		Long formId = document.getFormId();
		String defaultFormName = document.getFormTitle().getString(document.getDefaultLocale());
		PersistedFormDocument formDocument = new PersistedFormDocument();
		
		if(formId == null) {
			
			String formSlideId = generateFormId(defaultFormName);
			String formType = document.getFormType() == null ? standaloneFormType : document.getFormType();
			
			Document xformsDocument = document.getXformsDocument();
			
			String formPath = saveXFormsDocumentToSlide(xformsDocument, formSlideId, formType);
			
			Integer version = 1;
			
			XForm xform = new XForm();
			xform.setDisplayName(defaultFormName);
			xform.setDateCreated(new Date());
			xform.setFormState(XFormState.FLUX);
			xform.setFormStorageIdentifier(formPath);
			xform.setFormStorageType(slideStorageType);
			xform.setFormType(formType);
			xform.setVersion(version);
			
			getXformsDAO().persist(xform);
			formId = xform.getFormId();
			
			formDocument.setFormId(formId);
			formDocument.setFormType(formType);
			formDocument.setXformsDocument(xformsDocument);
			
		} else {
			
			XForm xform = getXformsDAO().find(XForm.class, formId);
			
			if(xform.getFormState() == XFormState.FIRM)
				throw new IllegalAccessException("Tried to save firm form. Once form made firm, it cannot be modified. Form id: "+formId);
			
			String formPath = xform.getFormStorageIdentifier();
			Document xformsDocument = document.getXformsDocument();
			saveExistingXFormsDocumentToSlide(xformsDocument, formPath);
			
			xform.setDisplayName(defaultFormName);
			xform.setVersion(xform.getVersion()+1);
			getXformsDAO().merge(xform);
			
			formDocument.setFormId(formId);
			formDocument.setFormType(xform.getFormType());
			formDocument.setXformsDocument(xformsDocument);
		}
		
		return formDocument;
	}
	
	@Transactional(readOnly=false)
	public PersistedFormDocument takeForm(Long formId) {
		
		XForm xform = getXformsDAO().find(XForm.class, formId);
		PersistedFormDocument formDocument = new PersistedFormDocument();
		
		Document xformsDocument;
		
		if(xform.getFormState() == XFormState.FIRM) {
		
			getLogger().log(Level.WARNING, "TakeForm on firm form. Is this the expected behavior?");
			
			xformsDocument = loadXMLResourceFromSlide(xform.getFormStorageIdentifier());
			
		} else if(xform.getFormState() == XFormState.FLUX) {
		
//			making firm
			
			XForm existingFirmXForm = getXformsDAO().getXFormByParentVersion(xform.getFormId(), xform.getVersion(), XFormState.FIRM);
			
			if(existingFirmXForm != null) {
				
				xformsDocument = loadXMLResourceFromSlide(existingFirmXForm.getFormStorageIdentifier());
				xform = existingFirmXForm;
				
			} else {
				
				xformsDocument = loadXMLResourceFromSlide(xform.getFormStorageIdentifier());
				String formSlideId = generateFormId(xform.getDisplayName());
				String formType = xform.getFormType();
				
				String formPath = saveXFormsDocumentToSlide(xformsDocument, formSlideId, formType);
				
				XForm newFirmForm = new XForm();
				newFirmForm.setDateCreated(xform.getDateCreated());
				newFirmForm.setDisplayName(xform.getDisplayName());
				newFirmForm.setFormParent(xform.getFormId());
				newFirmForm.setFormState(XFormState.FIRM);
				newFirmForm.setFormStorageType(slideStorageType);
				newFirmForm.setFormStorageIdentifier(formPath);
				newFirmForm.setFormType(formType);
				newFirmForm.setVersion(xform.getVersion());
				
				getXformsDAO().persist(newFirmForm);
				
				xform = newFirmForm;
				
			}
		} else
			throw new IllegalStateException("XForm state not supported by slide persistence manager. State: "+xform.getFormState());
		
		
		formDocument.setFormId(xform.getFormId());
		formDocument.setFormType(xform.getFormType());
		formDocument.setXformsDocument(xformsDocument);
		
		return formDocument;
	}
	
	public void duplicateForm(String formId, String new_title_for_default_locale) throws Exception {
		
		if(true)
			throw new UnsupportedOperationException("Not supported yet, make this call from document manager");

//		Document xformsDoc = loadFormNoLock(formId);
//		com.idega.xformsmanager.business.Document document = getDocumentManagerFactory().newDocumentManager(iwma).openForm(xformsDoc);
		
//		if(newTitle == null)
//			newTitle = new LocalizedStringBean("copy_"+document.getFormTitle().getString(new Locale("en")));
		
//		BlockFormUtil.putDefaultTitle(xformsDoc, newTitleForDefaultLocale);
//		
//		formId = generateFormId(newTitleForDefaultLocale);
//		saveForm(formId, xformsDoc, false);
	}
	
	public void removeForm(String form_id, boolean remove_submitted_data) throws FormLockException, Exception {
		
		if(true)
			throw new UnsupportedOperationException();
		
		/*
		if(form_id == null || form_id.equals(""))
			throw new NullPointerException("Form id not provided");
		
		WebdavResource simpleResource = loadFormResource(FORMS_PATH, form_id, true);
		WebdavExtendedResource resource = getWebdavExtendedResource(simpleResource.getPath());
		
		if(resource == null)
			throw new Exception("Form with id: "+form_id+" couldn't be loaded from webdav");
		
		Exception e = null;
		try {
			resource.deleteMethod(resource.getParentPath());
			
		} catch (Exception some_e) {
			logger.log(Level.SEVERE, "Exception occured while deleting form document: "+form_id, some_e);
			e = some_e;
		}
		
		if(remove_submitted_data) {
			
			try {
				resource = loadSubmittedDataFolderResource(form_id);
				
				if(resource == null)
					return;
				
				resource.deleteMethod();
				
				
			} catch (Exception some_e) {
				
				logger.log(Level.SEVERE, "Exception occured while deleting form's submitted data for form document: "+form_id, some_e);

//				more imporant to display first error
				if(e == null)
					e = some_e;
			}
		}
		
		if(e != null)
			throw e;
			*/
	}

	public List<Form> getStandaloneForms() {

		String formType = standaloneFormType;
		String formStorageType = slideStorageType;
		
		List<Form> xforms = getXformsDAO().getAllXFormsByTypeAndStorageType(formType, formStorageType);
		return xforms;
	}
	
	public List<Submission> getAllStandaloneFormsSubmissions() {
		
		List<Submission> submissions = 
			getXformsDAO().getResultListByInlineQuery("select submissions from " +
					"com.idega.block.form.data.XForm xforms inner join xforms."+XForm.xformSubmissionsProperty+" submissions " +
							"where xforms."+XForm.formTypeProperty+" = :"+XForm.formTypeProperty+" and " +
									"submissions."+XFormSubmission.isFinalSubmissionProperty+" = :"+XFormSubmission.isFinalSubmissionProperty, Submission.class,
									new Param(XForm.formTypeProperty, standaloneFormType),
									new Param(XFormSubmission.isFinalSubmissionProperty, true)
			);
		
		return submissions;
	}
	
	public List<Submission> getFormsSubmissions(long formId) {
		
		List<Submission> submissions = 
			getXformsDAO().getResultListByInlineQuery("select submissions from " +
					"com.idega.block.form.data.XForm xforms inner join xforms."+XForm.xformSubmissionsProperty+" submissions " +
							"where xforms."+XForm.formIdProperty+" = :"+XForm.formIdProperty+" and " +
									"submissions."+XFormSubmission.isFinalSubmissionProperty+" = :"+XFormSubmission.isFinalSubmissionProperty, Submission.class,
									new Param(XForm.formIdProperty, formId),
									new Param(XFormSubmission.isFinalSubmissionProperty, true)
			);
		
		return submissions;
	}
	
	public Submission getSubmission(long submissionId) {
		
		XFormSubmission submission = getXformsDAO().find(XFormSubmission.class, submissionId);
		return submission;
	}

	public String getSubmittedDataResourcePath(String formId, String submittedDataFilename) {
		return 
			new StringBuilder(SUBMITTED_DATA_PATH)
			.append(CoreConstants.SLASH)
			.append(formId)
			.append(CoreConstants.SLASH)
			.append(submittedDataFilename)
			.toString();
	}

	protected IWSlideService getIWSlideService() throws IBOLookupException {
		
		try {
			return (IWSlideService) IBOLookup.getServiceInstance(getIWApplicationContext(), IWSlideService.class);
		} catch (IBOLookupException e) {
			logger.log(Level.SEVERE, "Error getting IWSlideService");
			throw e;
		}
	}
	
	private synchronized IWApplicationContext getIWApplicationContext() {
		
		if(iwac == null)
			iwac = IWMainApplication.getDefaultIWApplicationContext();
		
	    return iwac;
	}

	private WebdavExtendedResource getWebdavExtendedResource(String path) throws HttpException, IOException, RemoteException, IBOLookupException {
		IWSlideService service = getIWSlideService();
		return service.getWebdavExtendedResource(path, service.getRootUserCredentials());
	}

	public Document loadSubmittedData(String formId, String submittedDataFilename) throws Exception {
		
		if(submittedDataFilename == null || formId == null)
			throw new NullPointerException("submitted_data_id or formId is not provided");
	
		String resource_path = getSubmittedDataResourcePath(formId, submittedDataFilename);

		WebdavExtendedResource webdav_resource = getWebdavExtendedResource(resource_path);
		
		if(webdav_resource == null)
			throw new NullPointerException("Submitted data document was not found by provided resource path: "+resource_path);
		
		InputStream is = webdav_resource.getMethodData();
		
		DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
		Document submitted_data = docBuilder.parse(is);
		
		return submitted_data;
	}

	public List<SubmittedDataBean> listSubmittedData(String formId) throws Exception {
		
		if(true)
			throw new UnsupportedOperationException("Not supported yet, implement with new submission storage");
		if(formId == null)
			throw new NullPointerException("Form identifier is not set");
		
		WebdavResource form_folder = getWebdavExtendedResource(
				new StringBuilder(SUBMITTED_DATA_PATH)
				.append(CoreConstants.SLASH)
				.append(formId)
				.toString()
		);
		
		if(form_folder == null)
			throw new NullPointerException("Error during form submissions folder retrieve");
		
		if(!form_folder.exists())
			return new ArrayList<SubmittedDataBean>();
		
		WebdavResources child_resources = form_folder.getChildResources();
		
		@SuppressWarnings("unchecked")
		Enumeration<WebdavResource> resources = child_resources.getResources();
		
		DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
		
		List<SubmittedDataBean> submitted_data = new ArrayList<SubmittedDataBean>();
		
		while (resources.hasMoreElements()) {
			WebdavResource webdav_resource = resources.nextElement();

			final String displayName = webdav_resource.getDisplayName();
			if (displayName.startsWith("."))
				continue; // skip .DS_Store and other junk files
			
			try {
				
				InputStream is = webdav_resource.getMethodData();
				Document submitted_data_doc = docBuilder.parse(is);
				
				SubmittedDataBean data_bean = new SubmittedDataBean();
				data_bean.setSubmittedDataElement(submitted_data_doc.getDocumentElement());
				data_bean.setId(displayName);
				
				submitted_data.add(data_bean);
				
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error when retrieving/parsing submitted data file", e);
			}
		}
		return submitted_data;
	}
	
	/**
	 * Stores submitted data: inputStream file and attachments in slide, and meta data in XFORMS_SUBMISSIONS
	 * 
	 * @param formId - not null
	 * @param is - not null
	 * @param identifier - could be null, some random identifier would be generated
	 * @return submitted data id
	 * @throws IOException
	 * 
	 */
	@Transactional(readOnly=false)
	public Long saveSubmittedData(Long formId, InputStream is, String identifier, boolean finalSubmission) throws IOException {

		if(formId == null || is == null)
			throw new IllegalArgumentException("Not enough arguments. FormId="+formId+", is="+is);
		
		if(identifier == null || identifier.length() == 0) {
			identifier = String.valueOf(System
					.currentTimeMillis());
		}
		
		String path = storeSubmissionData(formId.toString(), identifier, is);
		
		XForm xform = getXformsDAO().find(XForm.class, formId);
		
		if(xform == null)
			throw new RuntimeException("No xform found for formId provided="+formId);
		
		XFormSubmission xformSubmission = new XFormSubmission();
		xformSubmission.setDateSubmitted(new Date());
		xformSubmission.setSubmissionIdentifier(identifier);
		xformSubmission.setSubmissionStorageIdentifier(path);
		xformSubmission.setSubmissionStorageType(slideStorageType);
		xformSubmission.setIsFinalSubmission(finalSubmission);
		xformSubmission.setXform(xform);
		
		getXformsDAO().persist(xformSubmission);
		
		return xformSubmission.getSubmissionId();
	}
	
	private String storeSubmissionData(String formId, String identifier, InputStream is) throws IOException {

//		path equals SUBMITTED_DATA_PATH + formId + identifier, 
//		so we get something similar like /files/forms/submissions/123/P-xx/submission.xml, 
//		and files at /files/forms/submissions/123/P-xx/uploads/file1.doc
		String path = 
			new StringBuilder(SUBMITTED_DATA_PATH)
			.append(CoreConstants.SLASH)
			.append(formId)
			.append(CoreConstants.SLASH)
			.append(identifier)
			.append(CoreConstants.SLASH)
			.toString();
			
		IWSlideService service = getIWSlideService();
		service.uploadFileAndCreateFoldersFromStringAsRoot(path, submissionFileName, is, "text/xml", false);
		
		return path;
	}
	
	public Long saveSubmittedDataByExistingSubmission(Long submissionId, Long formId, InputStream is, String identifier) throws IOException {
		
		
		if(identifier == null || identifier.length() == 0) {
			identifier = String.valueOf(System
					.currentTimeMillis());
		}

		boolean isFinalSubmission = false;
		XFormSubmission xformSubmission;
		
		if(submissionId != null)
			xformSubmission = getXformsDAO().find(XFormSubmission.class, submissionId);
		else
			xformSubmission = null;
		
		if(xformSubmission == null) {
			
			submissionId = saveSubmittedData(formId, is, identifier, isFinalSubmission);
		} else {
		
			String path = storeSubmissionData(formId.toString(), identifier, is);
			xformSubmission.setDateSubmitted(new Date());
			xformSubmission.setSubmissionStorageIdentifier(path);
			xformSubmission.setIsFinalSubmission(isFinalSubmission);
			xformSubmission = getXformsDAO().merge(xformSubmission);
			submissionId = xformSubmission.getSubmissionId();
		}
		
		return submissionId;
	}
	
	protected String generateFormId(String name) {
		name = name.replaceAll("-", CoreConstants.UNDER);
		String result = name+CoreConstants.MINUS+new Date();
		String formId = result.replaceAll(" |:|\n", CoreConstants.UNDER).toLowerCase();
		
		char[] chars = formId.toCharArray();
		StringBuilder correctFormId = new StringBuilder(formId.length());
		
		for (int i = 0; i < chars.length; i++) {
			int charId = chars[i];
			
			if((charId > 47 && charId < 58) || charId == 45 || charId == 95 || (charId > 96 && charId < 123))
				correctFormId.append(chars[i]);
		}
		
		return correctFormId.toString();
	}
	
	public void unlockForm(String form_id) {
		
		if(true)
			return;
		/*
		try {
			WebdavResource webdavResource = loadFormResource(FORMS_PATH, form_id, false);
			
			if(webdavResource == null || !webdavResource.isLocked())
				return;
			
			webdavResource.unlockMethod();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading form from Webdav: " + form_id, e);
		} catch (FormLockException e) {
			logger.log(Level.WARNING, "FormLockException caught while loading form when lock is irrelevant for form id: "+form_id);
		}
		*/
	}

	public XFormsDAO getXformsDAO() {
		return xformsDAO;
	}

	@Autowired
	public void setXformsDAO(XFormsDAO xformsDAO) {
		this.xformsDAO = xformsDAO;
	}

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	@Autowired
	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}
}