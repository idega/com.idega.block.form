package com.idega.block.form.bean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import com.idega.block.form.business.FormsService;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 * 
 */
public class FormList {
	private static final Logger log = Logger.getLogger(FormList.class.getName()); 

	public List<SelectItem> getForms() {
		
		List<SelectItem> forms = new ArrayList<SelectItem>();
		try {
			forms = getFormsService().getForms();
		}
		catch (RemoteException e) {
			log.warning("Error getting available forms");
		}
		
		return forms;
	}

	private FormsService getFormsService() {
		FormsService service = null;
		try {
			IWApplicationContext iwc = IWMainApplication.getDefaultIWApplicationContext();
			service = (FormsService) IBOLookup.getServiceInstance(iwc, FormsService.class);
		}
		catch (IBOLookupException e) {
			log.severe("Could not find FormsService");
		}
		return service;
	}
}