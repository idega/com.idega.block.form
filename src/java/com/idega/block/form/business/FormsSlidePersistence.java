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

import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.block.form.data.XForm;
import com.idega.block.form.data.dao.XFormsDAO;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.chiba.web.xml.xforms.connector.webdav.WebdavSubmissionHandler;
import com.idega.documentmanager.business.FormLockException;
import com.idega.documentmanager.business.PersistedForm;
import com.idega.documentmanager.business.PersistedFormDocument;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.SubmittedDataBean;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.documentmanager.business.XFormState;
import com.idega.documentmanager.component.FormDocument;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.util.CoreConstants;
import com.idega.util.xml.XmlUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.10 $
 *
 * Last modified: $Date: 2008/04/12 01:51:01 $ by $Author: civilis $
 */
@Scope("singleton")
@XFormPersistenceType("slide")
@Repository("xformsPersistenceManager")
public class FormsSlidePersistence implements PersistenceManager {

	private static final long serialVersionUID = 1790429880309352062L;
	
	private static final String slideStorageType = "slide";
	private static final String standaloneFormType = "standalone";

	private final Logger logger;
	private IWApplicationContext iwac;
	private XFormsDAO xformsDAO;

	public static final String FORMS_PATH = "/files/forms";
	public static final String STANDALONE_FORMS_PATH = FORMS_PATH+"/standalone";
	public static final String FORMS_FILE_EXTENSION = ".xhtml";
	public static final String SUBMITTED_DATA_PATH = WebdavSubmissionHandler.SUBMITTED_DATA_PATH;

	public FormsSlidePersistence() {
		logger = Logger.getLogger(getClass().getName());
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
	protected String getFormResourcePath(String formType, String formIdentifier, boolean withFile) {
		
		StringBuilder b = new StringBuilder(FORMS_PATH).append(BlockFormUtil.slash).
		append(formType).append(BlockFormUtil.slash);
		
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
		
		Document xformsDoc = loadXFormsDocument(formPath);
		
		PersistedFormDocument formDoc = new PersistedFormDocument();
		formDoc.setFormId(formId);
		formDoc.setFormType(xform.getFormType());
		formDoc.setXformsDocument(xformsDoc);
		
		return formDoc;
	}
	
	protected Document loadXFormsDocument(String formPath) {
	
		try {
			WebdavExtendedResource xformRes = getWebdavExtendedResource(formPath);

			if(!xformRes.exists())
				throw new IllegalArgumentException("Expected webdav resource doesn't exist. Path provided: "+formPath);
			
			InputStream is = xformRes.getMethodData();
			DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
			Document xformsDoc = docBuilder.parse(is);
			return xformsDoc;
			
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected WebdavExtendedResource loadSubmittedDataFolderResource(String form_id) {
		
		try {
			WebdavExtendedResource webdav_resource = getWebdavExtendedResource(getSubmittedDataResourcePath(form_id, ""));

			return !webdav_resource.exists() ? null : webdav_resource;
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading form submitted data folder from Webdav: " + form_id, e);
			return null;
		}
	}
	
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
			
			Document xformsDocument = document.getContext().getXformsXmlDoc();
			
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
				throw new IllegalAccessException("Tried to save firm form. Once form made simple, it cannot be modified. Form id: "+formId);
			
			String formPath = xform.getFormStorageIdentifier();
			Document xformsDocument = document.getContext().getXformsXmlDoc();
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
			
			xformsDocument = loadXFormsDocument(xform.getFormStorageIdentifier());
			
		} else if(xform.getFormState() == XFormState.FLUX) {
		
//			making firm
			
			XForm existingFirmXForm = getXformsDAO().getXFormByParentVersion(xform.getFormId(), xform.getVersion(), XFormState.FIRM);
			
			if(existingFirmXForm != null) {
				
				xformsDocument = loadXFormsDocument(existingFirmXForm.getFormStorageIdentifier());
				xform = existingFirmXForm;
				
			} else {
				
				xformsDocument = loadXFormsDocument(xform.getFormStorageIdentifier());
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
//		com.idega.documentmanager.business.Document document = getDocumentManagerFactory().newDocumentManager(iwma).openForm(xformsDoc);
		
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

	public List<PersistedForm> getStandaloneForms() {

		String formType = standaloneFormType;
		String formStorageType = slideStorageType;
		
		List<XForm> xforms = getXformsDAO().getAllXFormsByTypeAndStorageType(formType, formStorageType);
		
		ArrayList<PersistedForm> forms = new ArrayList<PersistedForm>(xforms.size());
		
		for (XForm form : xforms) {
			
			PersistedForm pform = new PersistedForm();
			pform.setDateCreated(form.getDateCreated());
			pform.setFormId(form.getFormId());
			pform.setDisplayName(form.getDisplayName());
			forms.add(pform);
		}
		
		return forms;
	}

	public String getSubmittedDataResourcePath(String formId, String submittedDataFilename) {
		return 
			new StringBuilder(SUBMITTED_DATA_PATH)
			.append(BlockFormUtil.slash)
			.append(formId)
			.append(BlockFormUtil.slash)
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
	public synchronized IWApplicationContext getIWApplicationContext(){
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
		
		if(formId == null)
			throw new NullPointerException("Form identifier is not set");
		
		WebdavResource form_folder = getWebdavExtendedResource(
				new StringBuilder(SUBMITTED_DATA_PATH)
				.append(BlockFormUtil.slash)
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
	
	protected String generateFormId(String name) {
		name = name.replaceAll("-", CoreConstants.UNDER);
		String result = name+CoreConstants.MINUS+new Date();
		String formId = result.replaceAll(" |:|\n", CoreConstants.UNDER).toLowerCase();
		
		char[] chars = formId.toCharArray();
		StringBuilder correctFormId = new StringBuilder(formId.length());
		
		for (int i = 0; i < chars.length; i++) {
			
			int charId = (int)chars[i];
			
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
}