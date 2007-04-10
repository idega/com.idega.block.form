package com.idega.block.formpreview.presentation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.block.form.presentation.FormViewer;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.documentmanager.business.DocumentManagerService;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.form.ButtonArea;
import com.idega.documentmanager.business.form.DocumentManager;
import com.idega.documentmanager.business.form.Page;
import com.idega.documentmanager.business.form.beans.LocalizedStringBean;
import com.idega.documentmanager.business.form.manager.util.InitializationException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.webface.WFUtil;

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
	
	private DocumentManager doc_man;
	
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
		
		PersistenceManager persistence_manager = (PersistenceManager) WFUtil.getBeanInstance("formbuilderPersistenceManager");
		Document doc = persistence_manager.loadFormNoLock(formid_provided);
		
		String resource_path = "webdav:"+
			persistence_manager.getSubmittedDataResourcePath(formid_provided, submitted_data_id_provided);
		
		try {
			doc = adjustDocumentForPreview(resource_path, doc, getDocumentManager(context));
			FormViewer form_viewer = new FormViewer();
			form_viewer.setXFormsDocument(doc);
			form_viewer.setRendered(true);
			renderChild(context, form_viewer);
			
		} catch (Exception e) {
			logger.error("Error while adjusting document for preview", e);
		}
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
	
	private DocumentManager getDocumentManager(FacesContext context) {
		
		if(doc_man == null) {
			try {
				IWApplicationContext iwc = IWContext.getIWContext(context);
				doc_man = ((DocumentManagerService) IBOLookup.getServiceInstance(iwc, DocumentManagerService.class)).newDocumentManager(context);
			} catch (IBOLookupException e) {
				logger.error("Could not find DocumentManagerService", e);
			} catch (InitializationException e) {
				logger.error("Document manager failed to initialize", e);
			}
		}
		
		return doc_man;
	}
	
	public Document adjustDocumentForPreview(String resource_path, Document xforms_doc, DocumentManager doc_man) throws Exception {
		
		com.idega.documentmanager.business.form.Document doc = doc_man.openForm(xforms_doc);
		Page c_page = doc.getConfirmationPage();
		if(c_page == null)
			c_page = doc.addConfirmationPage(null);
		else {
			
			ButtonArea b_area = c_page.getButtonArea();
			
			if(b_area != null)
				b_area.remove();
		}
		
//		todo: change lang to current
		
		LocalizedStringBean label = new LocalizedStringBean();
		label.setString(new Locale("en"), "Submitted data");
		c_page.getProperties().setLabel(label);
				
		List<String> p_list = doc.getContainedPagesIdList();
		
		for (int i = 0; i < p_list.size(); i++) {
			
			if(doc.getComponent(p_list.get(i)).getId().equals(c_page.getId())) {
				
				p_list.set(i, p_list.get(0));
				p_list.set(0, c_page.getId());
				break;
			}
		}
		
		doc.rearrangeDocument();

		xforms_doc = doc.getXformsDocument();
		
		Element data_instance = BlockFormUtil.getElementByIdFromDocument(xforms_doc, BlockFormUtil.head_tag, BlockFormUtil.data_instance_id);
		data_instance.setAttribute(BlockFormUtil.src_att, resource_path);

		return doc.getXformsDocument();
	}
}