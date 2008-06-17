package com.idega.block.form.save;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.chiba.adapter.ChibaAdapter;
import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.util.CoreConstants;
import com.idega.util.expression.ELUtil;
import com.idega.util.xml.XPathUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/06/17 12:18:15 $ by $Author: civilis $
 */
public class SaveFormSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    
	public static final String SUBMITTED_DATA_PATH = "/files/forms/submissions";
	private PersistenceManager persistenceManager;
	
    public Map<String, Object> submit(Submission submission, Node instance) throws XFormsException {
    	
    	checkSubmissionActions(submission);
    	
    	String formId = FormManagerUtil.getFormId(instance);
    	
    	if(formId != null && !CoreConstants.EMPTY.equals(formId)) {
    		
    		Long fid = new Long(formId);
    		ELUtil.getInstance().autowire(this);
    		
//    		TODO: check, if form is in firm state, if not - take it
    		
    		try {
//        		TODO: generate form identifier if not present in the submission (some default node)
//    			TODO: locate identifier at submission
    			String submissionIdentifier = "foo";
    			
//        		saving submission for form id
    			Node submittedInstance = instance.cloneNode(true);
    			XPathUtil u = new XPathUtil(".//saveFormData");
    			Element el = (Element)u.getNode(submittedInstance);
    			el.getParentNode().removeChild(el);
    			
    			InputStream is = getISFromXML(submittedInstance);
    			getPersistenceManager().saveSubmittedData(fid, is, submissionIdentifier);

//        		TODO: generate link
    			String link = "theLink";

//        		placing link and saved form identifier in response
    			u = new XPathUtil(".//saveFormData/formIdentifier");
    			el = (Element)u.getNode(instance);
    			el.setTextContent(submissionIdentifier);
    			u = new XPathUtil(".//saveFormData/linkToForm");
    			el = (Element)u.getNode(instance);
    			el.setTextContent(link);
    			
    			is = getISFromXML(instance);
    			
    			HashMap<String, Object> response = new HashMap<String, Object>(1);

//    			TODO: what's the non-deprecated way of doing it?
                response.put(ChibaAdapter.SUBMISSION_RESPONSE_STREAM, is);
                
                return response;
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
    	}

        return null;
    }
    
    private InputStream getISFromXML(Node node) throws TransformerException {
    	
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtil.prettyPrintDOM(node, out);
		return new ByteArrayInputStream(out.toByteArray());
    }
    
    private void checkSubmissionActions(Submission submission) throws XFormsException {
    	if(!submission.getMethod().equalsIgnoreCase("post"))
    		throw new XFormsException("submission method '" + submission.getMethod() + "' not supported");
    	
    	if (!submission.getReplace().equals("instance"))
            throw new XFormsException("submission mode '" + submission.getReplace() + "' not supported");
    }
    
	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	@Autowired
	@XFormPersistenceType("slide")
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}
}