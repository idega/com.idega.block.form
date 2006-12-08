package com.idega.block.form.business;


import java.rmi.RemoteException;
import java.util.List;
import javax.faces.model.SelectItem;
import org.w3c.dom.Document;
import com.idega.block.form.bean.SubmittedDataBean;
import com.idega.business.IBOService;
import com.idega.slide.business.IWContentEvent;
import com.idega.slide.business.IWSlideChangeListener;

public interface FormsService extends IBOService, IWSlideChangeListener {

	/**
	 * @see com.idega.block.form.business.FormsServiceBean#loadForm
	 */
	public Document loadForm(String formId) throws RemoteException;

	/**
	 * @see com.idega.block.form.business.FormsServiceBean#saveForm
	 */
	public void saveForm(String formId, Document document) throws RemoteException;

	/**
	 * @see com.idega.block.form.business.FormsServiceBean#listForms
	 */
	public List<SelectItem> listForms() throws RemoteException;

	/**
	 * @see com.idega.block.form.business.FormsServiceBean#setAvailableFormsChanged
	 */
	public void setAvailableFormsChanged() throws RemoteException;

	/**
	 * @see com.idega.block.form.business.FormsServiceBean#getSubmittedData
	 */
	public Document getSubmittedData(String formId, String submittedDataFilename) throws Exception, RemoteException;

	/**
	 * @see com.idega.block.form.business.FormsServiceBean#listSubmittedData
	 */
	public List<SubmittedDataBean> listSubmittedData(String formId) throws Exception, RemoteException;

	/**
	 * @see com.idega.block.form.business.FormsServiceBean#onSlideChange
	 */
	public void onSlideChange(IWContentEvent contentEvent);
}