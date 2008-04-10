package com.idega.block.form.process;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.jbpm.taskmgmt.def.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.jbpm.data.ViewTaskBind;
import com.idega.jbpm.data.dao.BPMDAO;
import com.idega.jbpm.def.TaskView;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewFactory;
import com.idega.jbpm.def.ViewToTask;
import com.idega.jbpm.def.ViewToTaskType;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.19 $
 *
 * Last modified: $Date: 2008/04/10 01:06:11 $ by $Author: civilis $
 */
@ViewToTaskType("xforms")
@Scope("singleton")
@Service("process_xforms_viewToTask")
public class XFormsToTask implements ViewToTask {
	
	private ViewFactory viewFactory;
	private PersistenceManager xformsPersistenceManager;
	private BPMDAO BPMDAO; 
	
	public PersistenceManager getXformsPersistenceManager() {
		return xformsPersistenceManager;
	}

	@Autowired
	@XFormPersistenceType("slide")
	public void setXformsPersistenceManager(
			PersistenceManager xformsPersistenceManager) {
		this.xformsPersistenceManager = xformsPersistenceManager;
	}
	
	@Transactional(readOnly=true)
	public Multimap<Long, TaskView> getAllViewsByProcessDefinitions(Collection<Long> processDefinitionsIds) {
		
		List<Object[]> procTaskViews = getBPMDAO().getProcessTasksViewsInfos(processDefinitionsIds, XFormsView.VIEW_TYPE);
		Multimap<Long, TaskView> pdsViews = new HashMultimap<Long, TaskView>();
		
		for (Object[] objects : procTaskViews) {
			
			//ProcessDefinition procDef = (ProcessDefinition)objects[0];
			Task task = (Task)objects[0];
			String viewIdentifier = (String)objects[1];
			
			TaskView view = getViewFactory().getTaskView(task);
			view.setViewId(viewIdentifier);
			
			pdsViews.put(task.getProcessDefinition().getId(), view);
		}

		return pdsViews;
	}
	
	public Long getTask(String viewId) {
		
		ViewTaskBind vtb = getBPMDAO().getViewTaskBindByView(viewId, XFormsView.VIEW_TYPE);
		return vtb == null ? null : vtb.getTaskId();
	}
	
	public void unbind(String viewId) {
		
		ViewTaskBind vtb = getBPMDAO().getViewTaskBindByView(viewId, XFormsView.VIEW_TYPE);
		
		if(vtb != null)
			getBPMDAO().remove(vtb);
	}

	public void bind(View view, Task task) {

//		TODO: view type and task id should be a alternate key. that means unique too.
//		also catch when duplicate view type and task id pair is tried to be entered, and override
//		views could be versioned
		
		ViewTaskBind vtb = getBPMDAO().getViewTaskBind(task.getId(), XFormsView.VIEW_TYPE);
		
		boolean newVtb = false;
		
		if(vtb == null) {
			vtb = new ViewTaskBind();
			newVtb = true;
		}
		
		vtb.setTaskId(task.getId());
		vtb.setViewIdentifier(view.getViewId());
		vtb.setViewType(view.getViewType());

		if(newVtb)
			getBPMDAO().persist(vtb);
	}
	
	public View getView(long taskId) {
		
		ViewTaskBind vtb = getBPMDAO().getViewTaskBind(taskId, XFormsView.VIEW_TYPE);
		
		if(vtb == null)
			throw new NullPointerException("XForms view task bind couldn't be found for task id provided: "+taskId);
		
		return getViewFactory().getView(vtb.getViewIdentifier(), true);
	}
	
	public ViewFactory getViewFactory() {
		return viewFactory;
	}

	@Resource(name="process_xforms_viewFactory")
	public void setViewFactory(ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	public BPMDAO getBPMDAO() {
		return BPMDAO;
	}

	@Autowired
	public void setBPMDAO(BPMDAO bpmdao) {
		BPMDAO = bpmdao;
	}
}