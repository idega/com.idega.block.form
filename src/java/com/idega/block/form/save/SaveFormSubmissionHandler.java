package com.idega.block.form.save;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
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

import com.idega.block.form.presentation.FormViewer;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.documentmanager.business.PersistenceManager;
import com.idega.documentmanager.business.XFormPersistenceType;
import com.idega.documentmanager.util.FormManagerUtil;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;
import com.idega.util.xml.XPathUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/06/18 07:59:12 $ by $Author: civilis $
 */
public class SaveFormSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    
	public static final String SUBMITTED_DATA_PATH = "/files/forms/submissions";
	
	private PersistenceManager persistenceManager;
	
    public Map<String, Object> submit(Submission submission, Node instance) throws XFormsException {
    	
    	checkSubmissionActions(submission);
    	
    	String formId = FormManagerUtil.getFormId(instance);
    	
    	if(formId != null && !CoreConstants.EMPTY.equals(formId)) {
    		
    		ELUtil.getInstance().autowire(this);
    		Long fid = new Long(formId);
    		
//    		TODO: check, if form is in firm state, if not - take it
    		
    		try {
//    			trying to find form identifier from standard node (@nodeType='formIdentifier')
    			XPathUtil u = new XPathUtil(".//*[@nodeType='formIdentifier']");
    			Element el = (Element)u.getNode(instance);
    			
    			final String submissionIdentifier;
    			
    			if(el != null)
    				submissionIdentifier = el.getTextContent();
    			else {

//            		TODO: generate form identifier if not present in the submission (some default node)
    				submissionIdentifier = String.valueOf(System.currentTimeMillis());
    			}
    			
    			System.out.println("submissionidentifier="+submissionIdentifier);
    			
    			InputStream is = getISFromXML(instance);
    			Long submissionId = getPersistenceManager().saveSubmittedData(fid, is, submissionIdentifier);
    			
    			System.out.println("submissionId="+submissionId);

//    			resolving url to formviewer and setting submission param
    			IWContext iwc = IWContext.getCurrentInstance();
    			BuilderService bs = getBuilderService(iwc);
    			
    			String url = bs.getFullPageUrlByPageType(iwc, FormViewer.formviewerPageType);
    			final URIUtil uriUtil = new URIUtil(url);
    			uriUtil.setParameter(FormViewer.submissionIdParam, String.valueOf(submissionId));
    			url = uriUtil.getUri();
    			
    			System.out.println("url to formviewer="+url);
    			
//        		placing link and saved form identifier in response
    			u = new XPathUtil(".//saveFormData/formIdentifier");
    			el = (Element)u.getNode(instance);
    			el.setTextContent(submissionIdentifier);
    			u = new XPathUtil(".//saveFormData/linkToForm");
    			el = (Element)u.getNode(instance);
    			el.setTextContent(url);
    			
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
	
	protected BuilderService getBuilderService(IWApplicationContext iwc) {
		try {
			return BuilderServiceFactory.getBuilderService(iwc);
			
		} catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}
	}
}