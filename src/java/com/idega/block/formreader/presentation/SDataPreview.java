package com.idega.block.formreader.presentation;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idega.block.form.business.FormsService;
import com.idega.block.form.presentation.FormViewer;
import com.idega.block.formreader.business.FormReader;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

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
	
	private static final String FORM_READER = "com.idega.block.formreader.presentation.FormReaderBlock.FORM_READER";
	public static final String form_identifier = "form_identifier";
	public static final String submitted_data_identifier = "submitted_data_identifier";
	public static final String FORM_VIEWER = "FORM_VIEWER";
	public static final String title_tag = "title";
	
	private String formid_provided;
	private String submitted_data_id_provided;
		
	@Override
	public void initializeComponent(FacesContext ctx) {
//		Map session_map = ctx.getExternalContext().getSessionMap();
//		FormReader form_reader = (FormReader)session_map.get(FORM_READER);
//		if(form_reader == null) {
//			form_reader = new FormReader();
//			session_map.put(FORM_READER, form_reader);
//		}
		System.out.println("initalizing ....... " + formid_provided);
		try {
			
//			Document doc = getFormsService(ctx).loadForm(formid_provided);
//			adjustDocumentForPreview(doc);
//			FormViewer form_viewer = new FormViewer();
//			form_viewer.setXFormsDocument(doc);
//			getFacets().put(FORM_VIEWER, form_viewer);
			
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public boolean getRendersChildren() {
		return true;
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
		System.out.println("enc child........");
		
		Document doc = getFormsService(context).loadForm(formid_provided);
		adjustDocumentForPreview(doc);
		FormViewer form_viewer = new FormViewer();
		form_viewer.setXFormsDocument(doc);
		form_viewer.setRendered(true);
		renderChild(context, form_viewer);
		
//		UIComponent form_viewer = getFacet(FORM_VIEWER);
		
//		if(form_viewer != null) {
//			
//			form_viewer.setRendered(true);
//			renderChild(context, form_viewer);
//		}
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
		System.out.println("is rendered ............");
		return true;
	}
	
	@Override
	public void encodeEnd(FacesContext ctx) throws IOException {
		
//		Map session_map = ctx.getExternalContext().getSessionMap();
//		FormReader form_reader = (FormReader)session_map.get(FORM_READER);
//		
//		if(form_reader != null) {
//			
//			try {
//				Document doc = getFormsService(ctx).loadForm(formid_provided);
//				
//				FormViewer form_viewer = new FormViewer();
//				form_viewer.setXFormsDocument(doc);
//				getFacets().put(FORM_VIEWER, form_viewer);
//				
//				if (doc == null) {
//					throw new NullPointerException("Document was not found by provided resource path: "+formid_provided);
//				}
//				
//				form_reader.setFormDocument(doc);
//				
//				Document document_output = BlockFormUtil.getDocumentBuilder().newDocument();
//				
//				form_reader.setOutput(document_output);
//				form_reader.generate();
//				
//				FormParser form_parser = new FormParser();
//				form_parser.setHtmlForm(document_output);
//				
//				Document submitted_data_doc = getFormsService(ctx).loadSubmittedData(formid_provided, submitted_data_id_provided);
//				Element submittedData = submitted_data_doc.getDocumentElement();
//				
//				form_parser.setOutput(ctx.getResponseWriter());
//				form_parser.setXmlToFetch(submittedData);
//				form_parser.parse();
//				
//			} catch (Exception e) {
//				logger.error(e);
//			}
//		}
//		super.encodeEnd(ctx);
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
	
	protected void adjustDocumentForPreview(Document xforms_doc) {
		Element title = (Element)xforms_doc.getElementsByTagName(title_tag).item(0);
		title.getParentNode().removeChild(title);
	}
}