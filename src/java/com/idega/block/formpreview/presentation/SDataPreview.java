package com.idega.block.formpreview.presentation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idega.block.form.business.util.BlockFormUtil;
import com.idega.xformsmanager.business.DocumentManager;
import com.idega.xformsmanager.business.component.ButtonArea;
import com.idega.xformsmanager.business.component.Page;
import com.idega.xformsmanager.component.beans.LocalizedStringBean;
import com.idega.presentation.IWBaseComponent;

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
	
	//private static Log logger = LogFactory.getLog(SDataPreview.class);
	
	public static final String FORM_VIEWER = "FORM_VIEWER";
	public static final String form_identifier = "form_identifier";
	public static final String submitted_data_identifier = "submitted_data_identifier";
	
	//private String formid_provided;
	//private String submitted_data_id_provided;
		
	@Override
	public boolean getRendersChildren() {
		return true;
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
		
		/*
		if(formid_provided == null || submitted_data_id_provided == null)
			return;
		
		PersistenceManager persistence_manager = (PersistenceManager) WFUtil.getBeanInstance("xformsPersistenceManager");
		Document doc = persistence_manager.loadFormNoLock(formid_provided);
		
		String resource_path = "webdav:"+
			persistence_manager.getSubmittedDataResourcePath(formid_provided, submitted_data_id_provided);
		
		try {
			
			DocumentManagerFactory docManagerFact = (DocumentManagerFactory)WFUtil.getBeanInstance("xformsDocumentManagerFact");
			doc = adjustDocumentForPreview(resource_path, doc, docManagerFact.newDocumentManager(IWMainApplication.getIWMainApplication(context)));
			FormViewer form_viewer = new FormViewer();
			form_viewer.setXFormsDocument(doc);
			form_viewer.setRendered(true);
			renderChild(context, form_viewer);
			
		} catch (Exception e) {
			logger.error("Error while adjusting document for preview", e);
		}
		*/
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
		
		//formid_provided = form_identifier;
		//submitted_data_id_provided = submitted_data_identifier;
		return true;
	}
	
	public Document adjustDocumentForPreview(String resource_path, Document xforms_doc, DocumentManager doc_man) throws Exception {
		
		com.idega.xformsmanager.business.Document doc = doc_man.openForm(xforms_doc);
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