package com.idega.block.form.process;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.w3c.dom.Node;

import com.idega.block.form.process.XFormsView;
import com.idega.chiba.web.xml.xforms.connector.webdav.FileUploadManager;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.jbpm.IdegaJbpmContext;
import com.idega.jbpm.exe.BPMFactory;
import com.idega.jbpm.exe.ProcessConstants;
import com.idega.util.URIUtil;
import com.idega.webface.WFUtil;

/**
 * TODO: move all this logic to spring bean
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.5 $
 *
 * Last modified: $Date: 2008/04/11 01:25:29 $ by $Author: civilis $
 */
public class XFormsBPMSubmissionHandler extends AbstractConnector implements SubmissionHandler {
	
	public static final String variablesUploadManagerBeanIdentifier = "variablesUploadManager";
	private static final String bpmFactoryBeanIdentifier = "bpmFactory";
	private static final String xformsViewBeanIdentifier = "process_xforms_viewFactory";
	private static final String jbpmContextBeanIdentifier = "idegaJbpmContext";
    
	@SuppressWarnings("unchecked")
    public Map submit(Submission submission, Node submissionInstance) throws XFormsException {
		
    	//method - post, replace - none
    	if (!submission.getReplace().equalsIgnoreCase("none"))
            throw new XFormsException("Submission mode '" + submission.getReplace() + "' not supported");
    	
    	if(!submission.getMethod().equalsIgnoreCase("put") && !submission.getMethod().equalsIgnoreCase("post"))
    		throw new XFormsException("Submission method '" + submission.getMethod() + "' not supported");
    	
    	if(submission.getMethod().equalsIgnoreCase("put")) {
    		//update (put)
    		//currently unsupported
    		throw new XFormsException("Submission method '" + submission.getMethod() + "' not yet supported");
    		
    	} else {
    		//insert (post)
    	}
    	
    	BPMFactory bpmFactory = (BPMFactory)WFUtil.getBeanInstance(bpmFactoryBeanIdentifier);
    	XFormsViewFactory xfvFact = (XFormsViewFactory)WFUtil.getBeanInstance(xformsViewBeanIdentifier);
    	XFormsView casesXFormsView = xfvFact.getXFormsView();
    	casesXFormsView.setSubmission(submission, submissionInstance);
    	
    	IdegaJbpmContext ijCtx = (IdegaJbpmContext)WFUtil.getBeanInstance(jbpmContextBeanIdentifier);
    	JbpmContext ctx = ijCtx.createJbpmContext();
    	
    	try {
    		String action = submission.getElement().getAttribute(FormManagerUtil.action_att);
        	Map<String, String> parameters = new URIUtil(action).getParameters();
        	
        	ProcessDefinition processDefinition;
        	
        	if(parameters.containsKey(ProcessConstants.START_PROCESS)) {
        		
        		long tskInstId = Long.parseLong(parameters.get(ProcessConstants.TASK_INSTANCE_ID));
        		processDefinition = ctx.getTaskInstance(tskInstId).getProcessInstance().getProcessDefinition();
        		bpmFactory.getProcessManager(processDefinition.getId()).startProcess(tskInstId, casesXFormsView);
        		
        	} else if(parameters.containsKey(ProcessConstants.TASK_INSTANCE_ID)) {
        		
        		long tskInstId = Long.parseLong(parameters.get(ProcessConstants.TASK_INSTANCE_ID));
        		processDefinition = ctx.getTaskInstance(tskInstId).getProcessInstance().getProcessDefinition();
        		bpmFactory.getProcessManager(processDefinition.getId()).submitTaskInstance(tskInstId, casesXFormsView);

        	} else {
            	
        		Logger.getLogger(XFormsBPMSubmissionHandler.class.getName()).log(Level.SEVERE, "Couldn't handle submission. No action associated with the submission action: "+action);
        	}
			
		} finally {
			ijCtx.closeAndCommit(ctx);
		}
		
		FileUploadManager uploadManager = (FileUploadManager)WFUtil.getBeanInstance(variablesUploadManagerBeanIdentifier);
		uploadManager.cleanup(submissionInstance);
    	
    	return null;
    }
}