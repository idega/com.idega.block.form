package com.idega.block.form.process;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.impl.DefaultViewFactoryImpl;
import com.idega.jbpm.exe.Converter;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.10 $
 *
 * Last modified: $Date: 2008/03/27 14:13:12 $ by $Author: civilis $
 */
public class XFormsViewFactory extends DefaultViewFactoryImpl {

	private final String beanIdentifier = "process_xforms_viewFactory";
	
	private DocumentManagerFactory documentManagerFactory;
	private Converter converter;
	
	public View getView(String viewIdentifier, boolean submitable) {

		if(viewIdentifier == null || CoreConstants.EMPTY.equals(viewIdentifier))
			throw new NullPointerException("View identifier not provided");
		
		XFormsView view = new XFormsView();
		view.setViewId(viewIdentifier);
		view.setDocumentManagerFactory(getDocumentManagerFactory());
		view.setConverter(getConverter());
		view.setSubmitable(submitable);
		
		return view;
	}
	
	public XFormsView getXFormsView() {

		XFormsView view = new XFormsView();
		view.setDocumentManagerFactory(getDocumentManagerFactory());
		view.setConverter(getConverter());
		
		return view;
	}

	public String getViewType() {
		return XFormsView.VIEW_TYPE;
	}

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	public void setDocumentManagerFactory(
			DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}

	public Converter getConverter() {
		return converter;
	}

	@Autowired
	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public String getBeanIdentifier() {
		return beanIdentifier;
	}
}