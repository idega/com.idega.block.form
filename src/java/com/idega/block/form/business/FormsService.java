package com.idega.block.form.business;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;
import javax.faces.model.SelectItem;
import org.w3c.dom.Document;
import com.idega.block.form.bean.SubmittedDataBean;
import com.idega.business.IBOService;
import com.idega.slide.business.IWSlideChangeListener;

/**
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version 1.0
 * 
 */
public interface FormsService extends IBOService, IWSlideChangeListener {

	public Document loadForm(String formId) throws RemoteException;

	public void saveForm(String formId, Document document) throws RemoteException, Exception;

	public List<SelectItem> getForms() throws RemoteException;

	/**
	 * Save submitted form's instance
	 * 
	 * @param formId
	 * @param is instance input stream to save
	 * @throws IOException 
	 */
	public void saveSubmittedData(String formId, InputStream is) throws RemoteException, IOException;

	public Document loadSubmittedData(String formId, String submittedDataFilename) throws RemoteException, Exception;

	public List<SubmittedDataBean> listSubmittedData(String formId) throws RemoteException, Exception;

	/**
	 * 
	 * @param form_id - form id to remove
	 * @param remove_submitted_data - remove submitted data for this form
	 * @throws Exception
	 */
	public void removeForm(String form_id, boolean remove_submitted_data) throws Exception;
}