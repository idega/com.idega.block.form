package com.idega.block.form.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.chiba.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.idega.block.form.bean.AvailableFormBean;
import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.chiba.web.xml.xforms.connector.webdav.WebdavSubmissionHandler;
import com.idega.documentmanager.business.FormLockException;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.SubmittedDataBean;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version 1.0
 * 
 */
public class FormsSlidePersistence implements PersistenceManager {

	private static final long serialVersionUID = 1790429880309352062L;

	private static final Logger logger = Logger.getLogger(FormsSlidePersistence.class.getName());
	private IWApplicationContext iwac;

	public static final String FORMS_PATH = "/files/forms";
	public static final String FORMS_FILE_EXTENSION = ".xhtml";
	public static final String SUBMITTED_DATA_PATH = WebdavSubmissionHandler.SUBMITTED_DATA_PATH;

	public static final PropertyName FORM_NAME_PROPERTY_NAME = new PropertyName("FB:", "FormName");

	private static List<SelectItem> formNames;
	private DocumentBuilder doc_builder;
	
	public Document loadFormNoLock(String form_id) {
		
		try {
			return loadForm(form_id, false);
			
		} catch (FormLockException e) {
			logger.warning("Form Lock exception caught");
			return null;
		}
	}
	
	public Document loadFormAndLock(String form_id) throws FormLockException {
		
		return loadForm(form_id, true);
	}
	
	protected Document loadForm(String formId, boolean lock_relevant) throws FormLockException {
		Document document = null;
		try {
			
			WebdavExtendedResource webdav_resource = loadFormResource(formId, lock_relevant);
			
			if(webdav_resource == null)
				return null;

			InputStream is = webdav_resource.getMethodData();
			
			if(doc_builder == null)
				doc_builder = BlockFormUtil.getDocumentBuilder();
			
			document = doc_builder.parse(is);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading form from Webdav: " + formId, e);
		}
		catch (SAXException e) {
			logger.warning("Could not parse form document: " + formId);
		}
		catch (ParserConfigurationException e) {
			logger.severe("Could not create Xerces document builder");
		}
		return document;
	}
	
	protected WebdavExtendedResource loadFormResource(String form_id, boolean lock_relevant) throws FormLockException {
		
		try {
			WebdavExtendedResource webdav_resource = getWebdavExtendedResource(getFormResourcePath(form_id));
			
			if (!webdav_resource.exists()) {
				logger.log(Level.WARNING, "Form " + form_id + " does not exist");
				return null;
			}
			
//			if(lock_relevant && webdav_resource.isLocked())
//				throw new FormLockException(form_id, "Form with an id: "+form_id+" is locked");
//			
//			if(lock_relevant)
//				webdav_resource.lockMethodNoTimeout();
			
			return webdav_resource;
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading form from Webdav: " + form_id, e);
			return null;
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
	
	public void saveForm(String form_id, Document document) throws Exception {
		saveForm(form_id, document, true);
	}

	protected void saveForm(String formId, Document document, boolean lock_relevant) throws Exception {

		if(formId == null || document == null)
			throw new NullPointerException("formId or document not provided");
		
		String path_to_file = new StringBuilder(FORMS_PATH).append(BlockFormUtil.slash).append(formId).append(BlockFormUtil.slash).toString();
		String file_name = formId + FORMS_FILE_EXTENSION;

		try {
			IWSlideService service_bean = getIWSlideService();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtil.prettyPrintDOM(document, out);
			InputStream is = new ByteArrayInputStream(out.toByteArray());
			service_bean.uploadFileAndCreateFoldersFromStringAsRoot(path_to_file, file_name, is, "text/xml", false);
			
//			if(lock_relevant) {
//				
//				WebdavExtendedResource wextr = getWebdavExtendedResource(path_to_file+file_name);
//				
//				if(!wextr.isLocked()) {
//					logger.info("form is not locked");
//					wextr.lockMethodNoTimeout();
//				} else
//					logger.info("form is locked");
//			}
			
			WebdavResource webdav_res = service_bean.getWebdavResourceAuthenticatedAsRoot(path_to_file);
			String formTitle = BlockFormUtil.getDefaultFormTitle(document);
			
			// ensure formNames is initialized
			List<SelectItem> loaded_form_names = getForms();
			
			SelectItem f = findFormName(formId);
			if (f != null) {				
				if (!formTitle.equals(f.getLabel())) {
					f.setLabel(formTitle);
					setFormTitleProperty(webdav_res, formTitle);
				}
			}
			else {
				setFormTitleProperty(webdav_res, formTitle);
				f = new AvailableFormBean(formId, formTitle);
				loaded_form_names.add(f);
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Exception occured while saving document to webdav directory: "+formId, e);
			throw e;
		}
	}
	
	public void duplicateForm(String form_id, String new_title_for_default_locale) throws Exception {
		
		Document xforms_doc = loadFormNoLock(form_id);
		
		if(new_title_for_default_locale == null)
			new_title_for_default_locale = "copy_"+BlockFormUtil.getDefaultFormTitle(xforms_doc);
		
		BlockFormUtil.putDefaultTitle(xforms_doc, new_title_for_default_locale);
		
		form_id = generateFormId(new_title_for_default_locale);
		saveForm(form_id, xforms_doc, false);
	}
	
	public void removeForm(String form_id, boolean remove_submitted_data) throws FormLockException, Exception {
		
		if(form_id == null || form_id.equals(""))
			throw new NullPointerException("Form id not provided");
		
		WebdavExtendedResource resource = loadFormResource(form_id, true);
		
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
	}

	/**
	 * Sets form title as webdav resource's property
	 * 
	 * @param resource
	 * @param formTitle
	 * @throws HttpException
	 * @throws IOException
	 */
	private void setFormTitleProperty(WebdavResource resource, String formTitle) throws HttpException, IOException {
		if (formTitle != null) {
			resource.proppatchMethod(FORM_NAME_PROPERTY_NAME, formTitle, true);
		}
	}

	public List<SelectItem> getForms() {
		
		if(formNames == null) {
			synchronized (FormsSlidePersistence.class) {
				if(formNames == null) {
					formNames = loadAvailableForms();
				}
			}
		}
		
		return formNames;
	}

	protected List<SelectItem> loadAvailableForms() {

		long start = System.currentTimeMillis();
		
		List<SelectItem> forms = Collections.synchronizedList(new ArrayList<SelectItem>());
		try {
			IWSlideService service = getIWSlideService();

			WebdavResource root = service.getWebdavResourceAuthenticatedAsRoot(FORMS_PATH);
			WebdavResources form_folders = root.getChildResources();
			Enumeration<WebdavResource> folders = form_folders.getResources();
			
			Vector<PropertyName> props = new Vector<PropertyName>(1);
			props.add(FORM_NAME_PROPERTY_NAME);
			
			while (folders.hasMoreElements()) {
				WebdavResource folder = folders.nextElement();
				
				String formId = folder.getDisplayName();
				
				WebdavResources rs = folder.getChildResources();
				WebdavResource r = rs.getResource(folder.getName() + BlockFormUtil.slash + formId + FORMS_FILE_EXTENSION);
				if (r == null) {
					continue;
				}
				
				Enumeration prop_values = folder.propfindMethod(folder.getPath(), props);
				
				String formTitle = null;
				
				if(prop_values.hasMoreElements()) {					
					formTitle = (String)prop_values.nextElement();
					
					if(formTitle.equals("")) {
						formTitle = null;
					}
				}
				
				if (formTitle == null) {
					Document document = loadFormAndLock(formId);
					if (document == null) {
						continue;
					}
					formTitle = BlockFormUtil.getDefaultFormTitle(document);

					if (formTitle == null) {
						// if this form is not built with formbuilder, just take text content of title
						Node title = document.getElementsByTagName(BlockFormUtil.title_tag).item(0);
						if (title != null) {
							formTitle = DOMUtil.getTextNodeAsString(title);
						}
					}
			
					if (formTitle != null) {
						// set title for future
						setFormTitleProperty(r, formTitle);
					}
					else {
						formTitle = formId;
					}
				}

				AvailableFormBean f = new AvailableFormBean(formId, formTitle);
				forms.add(f);
			}
			
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error during loading available forms", e);
		}
		
		long end = System.currentTimeMillis();
		logger.info("Available forms list loaded in: "+(end-start));
		
		return forms;
	}

	/**
	 * Constructs a path from given formId
	 * @return A string like /files/forms/f1/f1.xhtml
	 */
	protected String getFormResourcePath(String formId) {
		return 
			new StringBuilder(FORMS_PATH)
			.append(BlockFormUtil.slash)
			.append(formId)
			.append(BlockFormUtil.slash)
			.append(formId)
			.append(FORMS_FILE_EXTENSION)
			.toString();
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
	public IWApplicationContext getIWApplicationContext(){
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
		
		if(doc_builder == null)
			doc_builder = BlockFormUtil.getDocumentBuilder();
		
		Document submitted_data = doc_builder.parse(is);
		
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
		Enumeration<WebdavResource> resources = child_resources.getResources();
		
		if(doc_builder == null)
			doc_builder = BlockFormUtil.getDocumentBuilder();
		
		List<SubmittedDataBean> submitted_data = new ArrayList<SubmittedDataBean>();
		
		while (resources.hasMoreElements()) {
			WebdavResource webdav_resource = resources.nextElement();

			final String displayName = webdav_resource.getDisplayName();
			if (displayName.startsWith("."))
				continue; // skip .DS_Store and other junk files
			
			try {
				
				InputStream is = webdav_resource.getMethodData();
				Document submitted_data_doc = doc_builder.parse(is);
				
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
	
	public SelectItem findFormName(String formId) {
		for (SelectItem f : getForms()) {
			if (formId.equals(f.getValue())) {
				return f;
			}
		}
		return null;
	}

	public String generateFormId(String name) {
		
		String result = name+"-"+ new Date();
		return result.replace(' ', '_').replace(':', '_');
	}
	
	public void unlockForm(String form_id) {
		
		if(true)
			return;
		try {
			WebdavExtendedResource webdav_resource = loadFormResource(form_id, false);
			
			if(webdav_resource == null || !webdav_resource.isLocked())
				return;
			
			webdav_resource.unlockMethod();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error loading form from Webdav: " + form_id, e);
		} catch (FormLockException e) {
			logger.log(Level.WARNING, "FormLockException caught while loading form when lock is irrelevant for form id: "+form_id);
		}
	}
}