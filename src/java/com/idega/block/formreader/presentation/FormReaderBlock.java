package com.idega.block.formreader.presentation;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.idega.block.formreader.business.FormParser;
import com.idega.block.formreader.business.FormReader;
import com.idega.block.formreader.business.SubmittedDataReader;
import com.idega.block.formreader.business.util.FormReaderUtil;
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
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 */
public class FormReaderBlock extends IWBaseComponent {
	
	private static Log logger = LogFactory.getLog(FormReaderBlock.class);
	
	private static final String FORM_READER = "com.idega.block.formreader.presentation.FormReaderBlock.FORM_READER";
	
	@Override
	public void initializeComponent(FacesContext ctx) {
		
		Map session_map = ctx.getExternalContext().getSessionMap();
		FormReader form_reader = (FormReader)session_map.get(FORM_READER);
		
		if(form_reader == null) {
			
			form_reader = FormReader.getInstance();
			
			try {
				
				form_reader.init();
				session_map.put(FORM_READER, form_reader);
				
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public static final String form_identifier = "form_identifier";
	public static final String submitted_data_identifier = "submitted_data_identifier";
	
	@Override
	public boolean isRendered() {
		
		boolean is_rendered = super.isRendered();
		
		if(!is_rendered)
			return false;
		
		FacesContext ctx = FacesContext.getCurrentInstance();
		
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
	
	private String formid_provided;
	private String submitted_data_id_provided;
	
	@Override
	public void encodeBegin(FacesContext ctx) throws IOException {

		
		super.encodeBegin(ctx);
	}
	
	@Override
	public void encodeEnd(FacesContext ctx) throws IOException {
		
		Map session_map = ctx.getExternalContext().getSessionMap();
		FormReader form_reader = (FormReader)session_map.get(FORM_READER);
		
		if(form_reader != null) {
			
			try {
				form_reader.setFormId(formid_provided);
				
				Document document_output = FormReaderUtil.getDocumentBuilder().newDocument();
				
				form_reader.setOutput(document_output);
				form_reader.generate();
				
				SubmittedDataReader submitted_data_reader = SubmittedDataReader.getInstance();
				submitted_data_reader.setFormIdentifier(formid_provided);
				submitted_data_reader.setSubmittedDataId(submitted_data_id_provided);
				
				FormParser form_parser = FormParser.getInstance();
				form_parser.setHtmlForm(document_output);
				
				form_parser.setOutput(ctx.getResponseWriter());
				form_parser.setXmlToFetch(submitted_data_reader.getSubmittedData());
				form_parser.parse();
				
			} catch (Exception e) {
				logger.error(e);
			}
		}
		super.encodeEnd(ctx);
	}
}