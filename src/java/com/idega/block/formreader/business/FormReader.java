package com.idega.block.formreader.business;

import javax.faces.context.FacesContext;
import org.chiba.adapter.ui.XSLTGenerator;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xslt.TransformerService;
import org.w3c.dom.Document;
import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.business.FormsService;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 */
public class FormReader {
	
	private ChibaBean chiba;
	private XSLTGenerator ui_generator;
	private String formId;
	private boolean inited = false;
	private Object output;
	private String base_uri; 
	
	private FormReader() { 	}
	
	public static FormReader getInstance() {
		
		return new FormReader();
	}
	
	public void init() throws NullPointerException, Exception {
		
		FacesContext ctx = FacesContext.getCurrentInstance();
		
		TransformerService transformer_service = (TransformerService) IWMainApplication.getIWMainApplication(ctx).getAttribute(IWBundleStarter.TRANSFORMER_SERVICE);
		ui_generator = new XSLTGenerator();
		ui_generator.setTransformerService(transformer_service);
		ui_generator.setStylesheetURI(IWBundleStarter.XSLT_URI);
		
		inited = true;
	}
	
	protected void loadDynamicResources() throws Exception {
		Document doc = getFormsService().loadForm(formId);
		
		if (doc == null) {
			throw new NullPointerException("Document was not found by provided resource path: "+formId);
		}
		
		chiba = new ChibaBean();
		chiba.setXMLContainer(doc);
		chiba.init();
	}
	
	public void setFormId(String formId) throws NullPointerException, Exception {
		
		if(!inited)
			throw new NullPointerException("FormReader not initialized");
		
		this.formId = formId;
		loadDynamicResources();
	}
	
	public void setFormDocument(Document form_document) throws NullPointerException, Exception {
		
		if(!inited)
			throw new NullPointerException("FormReader not initialized");
		
		chiba = new ChibaBean();
		chiba.setXMLContainer(form_document);
		chiba.setBaseURI(base_uri == null ? "http://localhost:8080/content/files/forms/" : base_uri);
		chiba.init();
	}
	
	public void setBaseFormURI(String base_uri) {
		this.base_uri = base_uri;
	}
	
	public void setOutput(Object output) {
		
		this.output = output;
	}
	
	public void generate() throws NullPointerException, Exception {
		
		if(!inited)
			throw new NullPointerException("FormReader not initialized");
		
		if(output == null)
			throw new NullPointerException("Output not set");
		
		ui_generator.setOutput(output);
		ui_generator.setInput(chiba.getXMLContainer());
		ui_generator.generate();
	}
	
	private FormsService getFormsService() {
		FormsService service = null;
		try {
			IWApplicationContext iwc = IWMainApplication.getDefaultIWApplicationContext();
			service = (FormsService) IBOLookup.getServiceInstance(iwc, FormsService.class);
		}
		catch (IBOLookupException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return service;
	}

}