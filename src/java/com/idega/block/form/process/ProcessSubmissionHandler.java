package com.idega.block.form.process;

import java.util.Map;

import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/12/04 14:00:37 $ by $Author: civilis $
 */
public class ProcessSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    
    /**
     * TODO: write javadoc
     */
	@SuppressWarnings("unchecked")
    public Map submit(Submission submission, Node instance) throws XFormsException {

		System.out.println("submission");
		DOMUtil.prettyPrintDOM(instance);
		if(true)
			return null;
    	//method - post, replace - none
    	if (!submission.getReplace().equalsIgnoreCase("none"))
            throw new XFormsException("Submission mode '" + submission.getReplace() + "' not supported");
    	
    	if(!submission.getMethod().equalsIgnoreCase("put") && !submission.getMethod().equalsIgnoreCase("post"))
    		throw new XFormsException("Submission method '" + submission.getMethod() + "' not supported");
    	
    	if(submission.getMethod().equalsIgnoreCase("put")) {
    		//update (put)
    		//currently unsupported
    		throw new XFormsException("Submission method '" + submission.getMethod() + "' not supported");
    		
    	} else {
    		//insert (post)
    	}
//    	VariablesHandler vh = (VariablesHandler)WFUtil.getBeanInstance("process_xforms_variablesHandler");
//    	
////    	TODO: do this somewhere else and in correct way
//    	String action = submission.getElement().getAttribute("action");
//    	String taskId = action.substring(action.indexOf("taskId=")+"taskId=".length(), action.length());
//    	
//    	vh.submit(Long.parseLong(taskId), instance);
    	
    	return null;
    }
}