package com.idega.block.form.business;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;
import javax.faces.model.SelectItem;
import org.w3c.dom.Document;
import com.idega.block.form.bean.SubmittedDataBean;
import com.idega.business.IBOService;
import com.idega.documentmanager.business.FormLockException;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.slide.business.IWSlideChangeListener;

/**
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version 1.0
 * 
 */
public interface FormsService extends IBOService, IWSlideChangeListener, PersistenceManager {

	public abstract Document loadFormAndLock(String formId) throws FormLockException, RemoteException;
	
	public abstract Document loadFormNoLock(String formId) throws RemoteException;

	public abstract void saveForm(String formId, Document document) throws RemoteException, Exception;

	public abstract List<SelectItem> getForms() throws RemoteException;

	/**
	 * Save submitted form's instance
	 * 
	 * @param formId
	 * @param is instance input stream to save
	 * @throws IOException 
	 */
	public abstract void saveSubmittedData(String formId, InputStream is) throws RemoteException, IOException;

	public abstract Document loadSubmittedData(String formId, String submittedDataFilename) throws RemoteException, Exception;

	public abstract List<SubmittedDataBean> listSubmittedData(String formId) throws RemoteException, Exception;

	/**
	 * 
	 * @param form_id - form id to remove
	 * @param remove_submitted_data - remove submitted data for this form
	 * @throws Exception
	 */
	public abstract void removeForm(String form_id, boolean remove_submitted_data) throws FormLockException, Exception;
	
	public abstract void duplicateForm(String form_id, String new_title_for_default_locale) throws Exception;
	
	public abstract String generateFormId(String name);
	
	public abstract String getSubmittedDataResourcePath(String formId, String submittedDataFilename);
	
	public abstract void unlockForm(String form_id);
}