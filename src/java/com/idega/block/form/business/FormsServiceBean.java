package com.idega.block.form.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.HttpException;
import org.apache.slide.event.ContentEvent;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.chiba.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import com.idega.block.form.bean.AvailableFormBean;
import com.idega.block.form.bean.SubmittedDataBean;
import com.idega.block.formreader.business.util.FormReaderUtil;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOServiceBean;
import com.idega.slide.business.IWContentEvent;
import com.idega.slide.business.IWSlideChangeListener;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version 1.0
 * 
 */
public class FormsServiceBean extends IBOServiceBean implements FormsService, IWSlideChangeListener {

	private static final Logger logger = Logger.getLogger(FormsServiceBean.class.getName());

	public static final String FORMS_PATH = "/files/forms";
	public static final String FORMS_FILE_EXTENSION = ".xhtml";
	public static final String SUBMITTED_DATA_PATH = "/files/forms/submissions";

	public static final PropertyName FORM_NAME_PROPERTY_NAME = new PropertyName("FB:", "FormName");

	private List<SelectItem> formNames;

	public Document loadForm(String formId) {
		Document document = null;
		try {
			WebdavExtendedResource webdav_resource = getWebdavExtendedResource(getResourcePath(formId));

			if (!webdav_resource.exists()) {
				logger.log(Level.INFO, "Form " + formId + " does not exist");
				return null;
			}
			InputStream is = webdav_resource.getMethodData();
			document = FormReaderUtil.getDocumentBuilder().parse(is);
		}
		catch (IOException e) {
			logger.log(Level.WARNING, "Error loading form from Webdav: " + formId, e);
		}
		catch (SAXException e) {
			logger.warning("Could not parse form document: " + formId);
		}
		catch (ParserConfigurationException e) {
			logger.severe("Could not create Xerces document builder");
		}
		return document;
	}

	public void saveForm(String formId, Document document) {
		String path_to_file = FORMS_PATH + "/" + formId + "/";
		String file_name = formId + FORMS_FILE_EXTENSION;
		IWSlideService service_bean = getIWSlideService();

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtil.prettyPrintDOM(document, out);
			InputStream is = new ByteArrayInputStream(out.toByteArray());
			service_bean.uploadFileAndCreateFoldersFromStringAsRoot(path_to_file, file_name, is, "text/xml", false);
			
			WebdavResource webdav_res = service_bean.getWebdavResourceAuthenticatedAsRoot(path_to_file);
			String formTitle = FormReaderUtil.getDefaultFormTitle(document);
			
			// ensure formNames is initialized
			listForms();
			
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
				formNames.add(f);
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Exception occured while saving document to webdav dir: ", e);
		}
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
		resource.proppatchMethod(FORM_NAME_PROPERTY_NAME, formTitle, (formTitle != null));
	}

	public List<SelectItem> listForms() {
		
		if(formNames == null) {
			synchronized (this) {
				if(formNames == null) {
					formNames = loadAvailableForms();
				}
			}
		}
	
		return formNames;
	}

	protected List<SelectItem> loadAvailableForms() {
		
		long start = System.currentTimeMillis();
		
		List<SelectItem> forms = new ArrayList<SelectItem>();
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
				WebdavResource r = rs.getResource(folder.getName() + "/" + formId + FORMS_FILE_EXTENSION);
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
					Document document = loadForm(formId);
					if (document == null) {
						continue;
					}
					formTitle = FormReaderUtil.getDefaultFormTitle(document);

					if (formTitle == null) {
						// if this form is not built with formbuilder, just take text content of title
						Node title = document.getElementsByTagName("title").item(0);
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

	public void setAvailableFormsChanged() {		
		cleanup();
	}

	protected void cleanup() {
		formNames = null;
	}

	/**
	 * Constructs a path from given formId
	 * @return A string like /files/forms/f1/f1.xhtml
	 */
	public static String getResourcePath(String formId) {
		return FORMS_PATH + "/" + formId + "/" + formId + FORMS_FILE_EXTENSION;
	}

	public static String getResourcePath(String formId, String submittedDataFilename) {
		return SUBMITTED_DATA_PATH + "/" + formId + "/" + submittedDataFilename;
	}

	protected IWSlideService getIWSlideService() {
		IWSlideService service = null;
		try {
			service = (IWSlideService) IBOLookup.getServiceInstance(getIWApplicationContext(), IWSlideService.class);
		}
		catch (IBOLookupException e) {
			logger.log(Level.SEVERE, "Error getting IWSlideService", e);
		}
		return service;
	}

	private WebdavExtendedResource getWebdavExtendedResource(String path) throws HttpException, IOException, RemoteException, IBOLookupException {
		IWSlideService service = getIWSlideService();
		return service.getWebdavExtendedResource(path, service.getRootUserCredentials());
	}

	/**
	 * Save submitted form's instance
	 * 
	 * @param path "/files/forms/submissions/$formId/"
	 * @param is instance input stream to save
	 * @throws IOException 
	 */
	public void saveSubmittedData(String path, InputStream is) throws IOException {
		IWSlideService service = getIWSlideService();
		
		String fileName = System.currentTimeMillis() + ".xml";
		
		logger.info("Saving submitted instance to webdav path: " + fileName);

		service.uploadFileAndCreateFoldersFromStringAsRoot(path, fileName, is, "text/xml", false);
	}

	public Document loadSubmittedData(String formId, String submittedDataFilename) throws Exception {
		
		if(submittedDataFilename == null || formId == null)
			throw new NullPointerException("submitted_data_id or formId is not provided");
	
		String resource_path = getResourcePath(formId, submittedDataFilename);

		WebdavExtendedResource webdav_resource = getWebdavExtendedResource(resource_path);
		
		if(webdav_resource == null)
			throw new NullPointerException("Submitted data document was not found by provided resource path: "+resource_path);
		
		InputStream is = webdav_resource.getMethodData();
		
		Document submitted_data = FormReaderUtil.getDocumentBuilder().parse(is);
		
		return submitted_data;
	}

	public List<SubmittedDataBean> listSubmittedData(String formId) throws Exception {
		
		if(formId == null)
			throw new NullPointerException("Form identifier is not set");
		
		WebdavResource form_folder = getWebdavExtendedResource(SUBMITTED_DATA_PATH + "/" + formId);
		
		if(form_folder == null)
			throw new NullPointerException("Form submitted data not found");
		
		WebdavResources child_resources = form_folder.getChildResources();
		Enumeration<WebdavResource> resources = child_resources.getResources();
		
		DocumentBuilder doc_builder = FormReaderUtil.getDocumentBuilder();
		List<SubmittedDataBean> submitted_data = new ArrayList<SubmittedDataBean>();
		
		while (resources.hasMoreElements()) {
			WebdavResource webdav_resource = resources.nextElement();
			
			InputStream is = webdav_resource.getMethodData();
			Document submitted_data_doc = doc_builder.parse(is);
			
			SubmittedDataBean data_bean = new SubmittedDataBean();
			data_bean.setSubmittedDataElement(submitted_data_doc.getDocumentElement());
			data_bean.setId(webdav_resource.getDisplayName());
			
			submitted_data.add(data_bean);
		}
		
		return submitted_data;
	}
	
	private SelectItem findFormName(String formId) {
		for (SelectItem f : formNames) {
			if (formId.equals(f.getValue())) {
				return f;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.idega.slide.business.IWSlideChangeListener#onSlideChange(com.idega.slide.business.IWContentEvent)
	 */
	public void onSlideChange(IWContentEvent contentEvent) {
		String uri = contentEvent.getContentEvent().getUri();
		if (!uri.startsWith(FORMS_PATH)) { // If not proccesing a form
			return;
		}
		String remainder = uri.substring(FORMS_PATH.length() + 1);
		int slashIndex = remainder.indexOf("/");
		if (slashIndex == -1) {
			return;
		}
		String formId = remainder.substring(0, slashIndex);
		if (!remainder.equals(formId + "/" + formId + FORMS_FILE_EXTENSION)) {
			return;
		}
		
		if (ContentEvent.REMOVE.equals(contentEvent.getMethod())) {
			SelectItem f = findFormName(formId);
			formNames.remove(f);
		}
		else if (ContentEvent.CREATE.equals(contentEvent.getMethod())) {
			// handle manually uploaded forms, saveForm handles the rest
		}
	}

}