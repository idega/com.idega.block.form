package com.idega.block.formreader.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idega.block.form.bean.FormBean;
import com.idega.block.formreader.business.beans.SubmittedDataBean;
import com.idega.block.formreader.business.util.FormReaderUtil;
import com.idega.business.IBOLookup;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.webface.WFUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 */
public class SubmittedDataReader {
	
	private String resource_path;
	private FormBean form_bean;
	private String form_identifier;
	
	private static final String submitted_data_path = "/files/forms/submitted_data_fake/"; 
	
	private SubmittedDataReader() { }
	
	public static SubmittedDataReader getInstance() {
		
		return new SubmittedDataReader();
	}
	
	public Element getSubmittedData() throws IOException {
		
		if(resource_path == null)
			throw new NullPointerException("Resourse path is not set");
		
		if(form_bean == null)
			form_bean = (FormBean) WFUtil.getBeanInstance(FormReaderUtil.form_bean_name);
		
//		form_bean.setResourcePath(resource_path);
		// FIXME: this loads default form
		form_bean.setFormId(form_identifier);
		
		form_bean.load();
		Document submitted_data = form_bean.getDocument();
		
		if(submitted_data == null)
			throw new NullPointerException("Submitted data document was not found by provided resource path: "+resource_path);
		
		return submitted_data.getDocumentElement();
	}
	
	public void setResourcePath(String resource_path) {
		
		this.resource_path = resource_path;
	}
	
	public void setFormIdentifier(String form_identifier) {
		
		this.form_identifier = form_identifier;
	}
	
	public List<SubmittedDataBean> getFormAllSubmittedData() throws Exception {
		
		if(form_identifier == null)
			throw new NullPointerException("Form identifier is not set");
		
		IWContext iwc = IWContext.getInstance();
		IWSlideSession session = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
		WebdavResource form_folder = session.getWebdavResource(submitted_data_path+form_identifier);
		
		if(form_folder == null)
			throw new NullPointerException("Form submitted data not found");
		
		WebdavResources child_resources = form_folder.getChildResources();
		Enumeration<WebdavResource> resources = child_resources.getResources();
		
		DocumentBuilder doc_builder = FormReaderUtil.getDocumentBuilder();
		List<SubmittedDataBean> submitted_data = new ArrayList<SubmittedDataBean>();
		
		while (resources.hasMoreElements()) {
			WebdavResource webdav_resource = resources.nextElement();
			
			InputStream is = webdav_resource.getMethodData();
			Document submitted_data_doc = doc_builder.parse(is);
			
			SubmittedDataBean data_bean = new SubmittedDataBean();
			data_bean.setSubmittedDataElement(submitted_data_doc.getDocumentElement());
			data_bean.setId(webdav_resource.getDisplayName());
			
			submitted_data.add(data_bean);
		}
		
		return submitted_data;
	}
	
	public static String getResourcePath(String form_identifier, String submitted_data_file_name) {
		
		return new StringBuffer(submitted_data_path)
		.append(form_identifier)
		.append(FormReaderUtil.slash)
		.append(submitted_data_file_name)
		.toString();
	}
}