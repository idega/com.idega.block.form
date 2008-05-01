package com.idega.block.form.process;

import java.util.Map;

import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import com.idega.webface.WFUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.8 $
 *
 * Last modified: $Date: 2008/05/01 15:34:47 $ by $Author: civilis $
 */
public class XFormsBPMSubmissionHandler extends AbstractConnector implements SubmissionHandler {
	
	public static final String variablesUploadManagerBeanIdentifier = "variablesUploadManager";
    
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
    	
    	
    	XFormsBPMSubmissionHandlerBean handlerBean = (XFormsBPMSubmissionHandlerBean)WFUtil.getBeanInstance(XFormsBPMSubmissionHandlerBean.beanIdentifier);
    	return handlerBean.handle(submission, submissionInstance);
    }
}