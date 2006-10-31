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
	private String resource_path;
	private FormBean form_bean;
	private boolean inited = false;
	private Object output;
	
	private static final String form_path = "/files/forms/";
	
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
		
		form_bean.setResourcePath(resource_path);
		
		form_bean.load();
		Document doc = form_bean.getDocument();
		
		if (doc == null) {
			throw new NullPointerException("Document was not found by provided resource path: "+resource_path);
		}
		
		chiba = new ChibaBean();
		chiba.setXMLContainer(doc);
		chiba.init();
	}
	
	public void setResourcePath(String resource_path) throws NullPointerException, Exception {
		
		if(!inited)
			throw new NullPointerException("FormParser not initialized");
		
		this.resource_path = resource_path;
		loadDynamicResources();
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
	
	
	
	public static String getResourcePath(String form_identifier) {
		
		return new StringBuffer(form_path)
		.append(FormReaderUtil.slash)
		.append(form_identifier)
		.append(FormReaderUtil.slash)
		.append(form_identifier)
		.append(FormReaderUtil.dot_xhtml)
		.toString();
	}
}