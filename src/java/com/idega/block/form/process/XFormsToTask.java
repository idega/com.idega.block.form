package com.idega.block.form.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jbpm.taskmgmt.def.Task;

import com.idega.documentmanager.business.PersistenceManager;
import com.idega.jbpm.data.ViewTaskBind;
import com.idega.jbpm.data.dao.BpmBindsDAO;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewFactory;
import com.idega.jbpm.def.ViewToTask;
import com.idega.jbpm.presentation.beans.ActorBindingViewBean;
import com.idega.webface.WFUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.12 $
 *
 * Last modified: $Date: 2008/01/25 15:23:38 $ by $Author: civilis $
 */
public class XFormsToTask implements ViewToTask {
	
	private ViewFactory viewFactory;
	private PersistenceManager xformsPersistenceManager;
	private BpmBindsDAO jbpmBindsDao; 
	
	public PersistenceManager getXformsPersistenceManager() {
		return xformsPersistenceManager;
	}

	public void setXformsPersistenceManager(
			PersistenceManager xformsPersistenceManager) {
		this.xformsPersistenceManager = xformsPersistenceManager;
	}
	
	public List<View> getAllViewsForViewType(String viewType) {
		List<SelectItem> list = getXformsPersistenceManager().getForms();
		List<View> forms = new ArrayList<View>();
		ActorBindingViewBean actorBean = (ActorBindingViewBean) WFUtil.getBeanInstance("actorBindingManager");
		String bindFormId = null;
		if(actorBean.getTaskId() != null) {
			String task = actorBean.getTaskId()[0];
			ViewTaskBind vtb = getJbpmBindsDao().getViewTaskBind(new Long(task).longValue(), viewType);
			
			if(vtb != null) {
				bindFormId = vtb.getViewIdentifier();
			}
		}
		for(Iterator<SelectItem> it = list.iterator(); it.hasNext(); ) {
			SelectItem form = it.next();
			String formId = (String) form.getValue();
			if(!formId.equals(bindFormId) && getTask(formId) != null) {
				View view = new XFormsView();
				forms.add(view);
			}
		}
		return forms;
	}
	
	public Long getTask(String viewId) {
		
		ViewTaskBind vtb = getJbpmBindsDao().getViewTaskBindByView(viewId, XFormsView.VIEW_TYPE);
		return vtb == null ? null : vtb.getTaskId();
	}
	
	public void unbind(String viewId) {
		
		ViewTaskBind vtb = getJbpmBindsDao().getViewTaskBindByView(viewId, XFormsView.VIEW_TYPE);
		
		if(vtb != null)
			getJbpmBindsDao().remove(vtb);
	}

	public void bind(View view, Task task) {

//		TODO: view type and task id should be a alternate key. that means unique too.
//		also catch when duplicate view type and task id pair is tried to be entered, and override
//		views could be versioned
		
		ViewTaskBind vtb = getJbpmBindsDao().getViewTaskBind(task.getId(), XFormsView.VIEW_TYPE);
		
		boolean newVtb = false;
		
		if(vtb == null) {
			vtb = new ViewTaskBind();
			newVtb = true;
		}
		
		vtb.setTaskId(task.getId());
		vtb.setViewIdentifier(view.getViewId());
		vtb.setViewType(view.getViewType());

		if(newVtb)
			getJbpmBindsDao().persist(vtb);
	}
	
	public View getView(long taskId) {
		
		ViewTaskBind vtb = getJbpmBindsDao().getViewTaskBind(taskId, XFormsView.VIEW_TYPE);
		
		if(vtb == null)
			throw new NullPointerException("XForms view task bind couldn't be found for task id provided: "+taskId);
		
		return getViewFactory().getView(vtb.getViewIdentifier(), true);
	}
	
	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	public void setViewFactory(ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	public BpmBindsDAO getJbpmBindsDao() {
		return jbpmBindsDao;
	}

	public void setJbpmBindsDao(BpmBindsDAO jbpmBindsDao) {
		this.jbpmBindsDao = jbpmBindsDao;
	}
}