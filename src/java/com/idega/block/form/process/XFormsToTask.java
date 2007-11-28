package com.idega.block.form.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.JbpmConfiguration;
import org.jbpm.taskmgmt.def.Task;

import com.idega.documentmanager.business.PersistenceManager;
import com.idega.jbpm.business.JbpmProcessBusinessBean;
import com.idega.jbpm.data.ViewTaskBind;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewToTask;
import com.idega.jbpm.presentation.beans.ActorBindingViewBean;
import com.idega.webface.WFUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.6 $
 *
 * Last modified: $Date: 2007/11/28 13:12:01 $ by $Author: alexis $
 */
public class XFormsToTask implements ViewToTask {
	
	private JbpmConfiguration cfg;
	private SessionFactory sessionFactory;
	private PersistenceManager xformsPersistenceManager;
	private JbpmProcessBusinessBean jbpmProcessBusiness;

	public JbpmProcessBusinessBean getJbpmProcessBusiness() {
		return jbpmProcessBusiness;
	}

	public void setJbpmProcessBusiness(JbpmProcessBusinessBean jbpmProcessBusiness) {
		this.jbpmProcessBusiness = jbpmProcessBusiness;
	}

	public PersistenceManager getXformsPersistenceManager() {
		return xformsPersistenceManager;
	}

	public void setXformsPersistenceManager(
			PersistenceManager xformsPersistenceManager) {
		this.xformsPersistenceManager = xformsPersistenceManager;
	}
	
	public List<View> getAllViewsForViewType(String viewType) {
		List<SelectItem> list = xformsPersistenceManager.getForms();
		List<View> forms = new ArrayList<View>();
		ActorBindingViewBean actorBean = (ActorBindingViewBean) WFUtil.getBeanInstance("actorBindingManager");
		String bindFormId = null;
		if(actorBean.getTaskId() != null) {
			String task = actorBean.getTaskId()[0];
			Session session = null;
			try {
				session = getSessionFactory().openSession();
				ViewTaskBind vtb = ViewTaskBind.getViewTaskBind(session, new Long(task).longValue(), viewType);
				
				if(vtb != null) {
					bindFormId = vtb.getViewIdentifier();
				}
			} finally {
				if(session != null)
					session.close();
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
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		try {
			if(!transactionWasActive)
				transaction.begin();
			
			ViewTaskBind vtb = ViewTaskBind.getViewTaskBindByView(session, viewId, XFormsView.VIEW_TYPE);
			
			return vtb == null ? null : vtb.getTaskId();
		} finally {
			if(session != null)
				session.close();
		}
	}
	
	public void unbind(String viewId) {
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		try {
			if(!transactionWasActive)
				transaction.begin();
			
			ViewTaskBind vtb = ViewTaskBind.getViewTaskBindByView(session, viewId, XFormsView.VIEW_TYPE);
			
			if(vtb != null)
				session.delete(vtb);
			
		} finally {
			
			if(!transactionWasActive)
				transaction.commit();
			
		}
		
	}

	public void bind(View view, Task task) {

//		TODO: view type and task id should be a alternate key. that means unique too.
//		also catch when duplicate view type and task id pair is tried to be entered, and override
//		views could be versioned
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		try {
			if(!transactionWasActive)
				transaction.begin();
			
			ViewTaskBind vtb = ViewTaskBind.getViewTaskBind(session, task.getId(), XFormsView.VIEW_TYPE);
			
			boolean newVtb = false;
			
			if(vtb == null) {
				vtb = new ViewTaskBind();
				newVtb = true;
			}
			
			vtb.setTaskId(task.getId());
			vtb.setViewIdentifier(view.getViewId());
			vtb.setViewType(view.getViewType());

			if(newVtb)
				session.save(vtb);
			
		} finally {
			
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	public View getView(long taskId) {
		
		Session session = getSessionFactory().getCurrentSession();
		
		Transaction transaction = session.getTransaction();
		boolean transactionWasActive = transaction.isActive();
		
		if(!transactionWasActive)
			transaction.begin();
		
		try {
			ViewTaskBind vtb = ViewTaskBind.getViewTaskBind(session, taskId, XFormsView.VIEW_TYPE);
			
			if(vtb == null)
				return null;
			
			XFormsView view = new XFormsView();
			view.setViewId(vtb.getViewIdentifier());
			
			return view;
			
		} finally {
			if(!transactionWasActive)
				transaction.commit();
		}
	}
	
	public void setJbpmConfiguration(JbpmConfiguration cfg) {
		this.cfg = cfg;
	}
	
	public JbpmConfiguration getJbpmConfiguration() {
		return cfg;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}