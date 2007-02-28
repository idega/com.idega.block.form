package com.idega.block.formpreview.presentation;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chiba.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idega.block.form.business.FormsService;
import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.block.form.presentation.FormViewer;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;

/**
 * Component's responsibility is to provide view form's of submitted data in readonly format.<br /><br /> 
 * 
 * To set form id and submitted data id, which You want to view, use value binding's
 * attributes form_identifier and submitted_data_identifier.
 * 
 * The component is rendered if:
 * - rendered attribute (also using value binding) is set to true 
 * - both form_identifier and submitted_data_identifier attributes are provided
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version 1.0
 */
public class SDataPreview extends IWBaseComponent {
	
	private static Log logger = LogFactory.getLog(SDataPreview.class);
	
	public static final String FORM_VIEWER = "FORM_VIEWER";
	public static final String form_identifier = "form_identifier";
	public static final String submitted_data_identifier = "submitted_data_identifier";
	
	private String formid_provided;
	private String submitted_data_id_provided;
		
	@Override
	public boolean getRendersChildren() {
		return true;
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
		
		if(formid_provided == null || submitted_data_id_provided == null)
			return;
		
		Document doc = getFormsService(context).loadForm(formid_provided);
		
		String resource_path = "webdav:"+
				getFormsService(context).getSubmittedDataResourcePath(formid_provided, submitted_data_id_provided);
		
		BlockFormUtil.adjustDocumentForPreview(resource_path, doc);
		FormViewer form_viewer = new FormViewer();
		form_viewer.setXFormsDocument(doc);
		form_viewer.setRendered(true);
		renderChild(context, form_viewer);
	}
	
	@Override
	public boolean isRendered() {
		
		boolean is_rendered = super.isRendered();
		
		if(!is_rendered)
			return false;
		
		FacesContext ctx = getFacesContext();
		
		ValueBinding vb = getValueBinding(form_identifier);
		
		String form_identifier = null;
		
		if(vb != null) {
			form_identifier = (String) vb.getValue(ctx);
		}
		
		if(form_identifier == null)
			return false;
		
		vb = getValueBinding(submitted_data_identifier);
		
		String submitted_data_identifier = null;
		
		if(vb != null) {
			submitted_data_identifier = (String) vb.getValue(ctx);
		}
		
		if(submitted_data_identifier == null)
			return false;
		
		formid_provided = form_identifier;
		submitted_data_id_provided = submitted_data_identifier;
		return true;
	}
	
	private FormsService getFormsService(FacesContext context) {
		FormsService service = null;
		try {
			IWApplicationContext iwc = IWContext.getIWContext(context);
			service = (FormsService) IBOLookup.getServiceInstance(iwc, FormsService.class);
		}
		catch (IBOLookupException e) {
			logger.error("Could not find FormsService");
		}
		return service;
	}
}