package com.idega.block.form.business;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;

import com.idega.business.IBOLookup;
import com.idega.core.cache.IWCacheManager2;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.repository.data.Singleton;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas �ivilis</a>
 * @version 1.0
 */
public class AvailableFormsLister implements Singleton {
	
	private static Log logger = LogFactory.getLog(AvailableFormsLister.class);
	
	private Map<Integer, List> iw_cache;
	private static final String cache_identfier = "com.idega.block.formadmin.business.beans.AvailableFormsLister.cache_identfier";
	private static final Integer form_names_key = 1;
	private static final Integer form_names_select_key = 2;
	
	protected AvailableFormsLister() { 
		
		FacesContext ctx = FacesContext.getCurrentInstance();
		IWMainApplication iwma = IWMainApplication.getIWMainApplication(ctx);
		iw_cache = IWCacheManager2.getInstance(iwma).getCache(cache_identfier);
	}
	
	private static AvailableFormsLister me;
	
	public static AvailableFormsLister getInstance() {
		
		if (me == null) {
			
			synchronized (AvailableFormsLister.class) {
				if (me == null) {
					me = new AvailableFormsLister();
				}
			}
		}

		return me;
	}
	
	public void setAvailableFormsChanged() {
		
		cleanup();
	}
	
	public List<AvailableFormBean> getAvailableForms() {
		
		List<AvailableFormBean> form_names = iw_cache.get(form_names_key);
		
		if(form_names == null) {
			
			synchronized (this) {
				
				form_names = iw_cache.get(form_names_key);
				
				if(form_names == null) {
					
					cleanup();
					form_names = loadAvailableForms();
					iw_cache.put(form_names_key, form_names);
					
				}
			}
		}

		return form_names;
	}
	
	public List<SelectItem> getAvailableFormsAsSelectItems() {
		
		List<SelectItem> form_names_select = (List<SelectItem>)iw_cache.get(form_names_select_key);
		
		if(form_names_select == null) {
			
			synchronized (this) {
				
				form_names_select = iw_cache.get(form_names_select_key);
				
				if(form_names_select == null) {
					
					List<AvailableFormBean> form_names = iw_cache.get(form_names_key);
					
					if(form_names == null)
						form_names = getAvailableForms();
					
					form_names_select = new ArrayList<SelectItem>();
					
					for (Iterator<AvailableFormBean> iter = form_names.iterator(); iter.hasNext();) {
						
						AvailableFormBean available_form = iter.next();
						form_names_select.add(new SelectItem(available_form.getId(), available_form.getFormTitle()));
					}
					
					iw_cache.put(form_names_select_key, form_names_select);
				}
			}
		}
		
		return form_names_select;
	}
	
	PropertyName prop_name;
	private static final String empty_str = "";
	
	public PropertyName getFormPropertyName() {
		
		if(prop_name == null)
			prop_name = new PropertyName("FB:", "FormName");
		
		return prop_name;
	}
	
	protected List<AvailableFormBean> loadAvailableForms() {
		
		List<AvailableFormBean> available_forms = new ArrayList<AvailableFormBean>();
		
		try {
			
			WebdavExtendedResource forms_folder_webdav_resource = getFormsFolderWebdavResource();
			
			WebdavResources form_folders = forms_folder_webdav_resource.getChildResources();
			Enumeration<WebdavResource> folders = form_folders.getResources();
			
			Vector<PropertyName> props = new Vector<PropertyName>(1);
			props.add(getFormPropertyName());
			
			while (folders.hasMoreElements()) {
				WebdavResource webdav_resource = folders.nextElement();
				
				Enumeration prop_values = 
					webdav_resource.propfindMethod(webdav_resource.getPath(), props);
				
				String form_title = null;
				
				if(prop_values.hasMoreElements()) {
					
					form_title = (String)prop_values.nextElement();
					
					if(form_title.equals(empty_str))
						form_title = null;
				}
				
				if(form_title != null) {
					
					String form_id = webdav_resource.getDisplayName();
					
					AvailableFormBean available_form = new AvailableFormBean();
					available_form.setId(form_id);
					available_form.setFormTitle(form_title);
					available_forms.add(available_form);
				}
			}
			
		} catch (Exception e) {
			logger.error("Error during loading available forms", e);
		}
		
		return available_forms;
	}
	
	protected void cleanup() {
		
		iw_cache.remove(form_names_key);
		iw_cache.remove(form_names_select_key);
	}
	
	protected static final String FORMS_PATH = "/files/forms";
	
	protected WebdavExtendedResource getFormsFolderWebdavResource() {
	
		try {
			IWSlideSession session = (IWSlideSession) IBOLookup.getSessionInstance(IWContext.getInstance(), IWSlideSession.class);
			WebdavExtendedResource webdav_resource = session.getWebdavResource(FORMS_PATH);
			
			return webdav_resource;
			
		} catch (Exception e) {
			
			logger.error("Error getting WebdavExtendedResource", e);
		}
		return null;
	}
}
