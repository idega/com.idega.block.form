package com.idega.block.form.process;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.taskmgmt.def.Task;

import com.idega.jbpm.data.ViewTaskBind;
import com.idega.jbpm.def.View;
import com.idega.jbpm.def.ViewToTask;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2007/10/14 10:51:07 $ by $Author: civilis $
 */
public class XFormsToTask implements ViewToTask {
	
	private JbpmConfiguration cfg;
	private SessionFactory sessionFactory;

	public void bind(View view, Task task) {

//		TODO: view type and task id should be a alternate key. that means unique too.
//		also catch when duplicate view type and task id pair is tried to be entered, and override
//		views could be versioned
		
		Session session = null;
		try {
			session = getSessionFactory().openSession();
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
			else
				session.flush();
			
		} finally {
			
			if(session != null)
				session.close();
		}
	}
	
	public View getView(long taskId) {
		
		Session session = getSessionFactory().openSession();
		
		try {
			ViewTaskBind vtb = ViewTaskBind.getViewTaskBind(session, taskId, XFormsView.VIEW_TYPE);
			
			if(vtb == null)
				return null;
			
			XFormsView view = new XFormsView();
			view.setViewId(vtb.getViewIdentifier());
			
			return view;
			
		} finally {
			
			if(session != null)
				session.close();
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