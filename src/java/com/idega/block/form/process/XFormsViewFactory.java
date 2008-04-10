package com.idega.block.form.process;

import org.jbpm.taskmgmt.def.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.documentmanager.business.DocumentManagerFactory;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.jbpm.def.TaskView;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.impl.DefaultViewFactoryImpl;
import com.idega.jbpm.exe.Converter;
import com.idega.util.CoreConstants;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.11 $
 *
 * Last modified: $Date: 2008/04/10 01:06:12 $ by $Author: civilis $
 */
@Scope("singleton")
@Service("process_xforms_viewFactory")
public class XFormsViewFactory extends DefaultViewFactoryImpl {

	private final String beanIdentifier = "process_xforms_viewFactory";
	
	private DocumentManagerFactory documentManagerFactory;
	private PersistenceManager persistenceManager;
	private Converter converter;
	
	public View getView(String viewIdentifier, boolean submitable) {

		if(viewIdentifier == null || CoreConstants.EMPTY.equals(viewIdentifier))
			throw new NullPointerException("View identifier not provided");
		
		XFormsView view = getXFormsView();
		view.setViewId(viewIdentifier);
		view.setSubmitable(submitable);
		
		return view;
	}
	
	public XFormsView getXFormsView() {

		XFormsView view = new XFormsView();
		view.setDocumentManagerFactory(getDocumentManagerFactory());
		view.setConverter(getConverter());
		view.setPersistenceManager(getPersistenceManager());
		
		return view;
	}
	
	public TaskView getTaskView(Task task) {

		XFormsTaskView view = new XFormsTaskView(task);
		view.setDocumentManagerFactory(getDocumentManagerFactory());
		view.setConverter(getConverter());
		view.setPersistenceManager(getPersistenceManager());
		
		return view;
	}
	
	public String getViewType() {
		return XFormsView.VIEW_TYPE;
	}

	public DocumentManagerFactory getDocumentManagerFactory() {
		return documentManagerFactory;
	}

	@Autowired
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

	protected PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	@Autowired
	@XFormPersistenceType("slide")
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}
	
	class XFormsTaskView extends XFormsView implements TaskView {

		private final Task task;
		
		XFormsTaskView(Task task) {
			this.task = task;
		}
		
		public Task getTask() {
			return task;
		}
	}
}