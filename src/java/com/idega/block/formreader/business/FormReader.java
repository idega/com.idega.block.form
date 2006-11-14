package com.idega.block.formreader.business;

import javax.faces.context.FacesContext;

import org.chiba.adapter.ui.XSLTGenerator;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xslt.TransformerService;
import org.w3c.dom.Document;

import com.idega.block.form.IWBundleStarter;
import com.idega.block.form.bean.FormBean;
import com.idega.block.formreader.business.util.FormReaderUtil;
import com.idega.idegaweb.IWMainApplication;
import com.idega.webface.WFUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 */
public class FormReader {
	
	private ChibaBean chiba;
	private XSLTGenerator ui_generator;
	private String formId;
	private FormBean form_bean;
	private boolean inited = false;
	private Object output;
	
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
		
		if(form_bean == null)
			form_bean = (FormBean) WFUtil.getBeanInstance(FormReaderUtil.form_bean_name);
		
		form_bean.setFormId(formId);
		
		form_bean.load();
		Document doc = form_bean.getDocument();
		
		if (doc == null) {
			throw new NullPointerException("Document was not found by provided resource path: "+formId);
		}
		
		chiba = new ChibaBean();
		chiba.setXMLContainer(doc);
		chiba.init();
	}
	
	public void setFormId(String formId) throws NullPointerException, Exception {
		
		if(!inited)
			throw new NullPointerException("FormParser not initialized");
		
		this.formId = formId;
		loadDynamicResources();
	}
	
	public void setFormDocument(Document form_document) throws NullPointerException, Exception {
		
		if(!inited)
			throw new NullPointerException("FormParser not initialized");
		
		chiba = new ChibaBean();
		chiba.setXMLContainer(form_document);
		chiba.init();
	}
	
	public void setOutput(Object output) {
		
		this.output = output;
	}
	
	public void generate() throws NullPointerException, Exception {
		
		if(!inited)
			throw new NullPointerException("FormParser not initialized");
		
		if(output == null)
			throw new NullPointerException("Output not set");
		
		ui_generator.setOutput(output);
		ui_generator.setInput(chiba.getXMLContainer());
		ui_generator.generate();
	}
	
}