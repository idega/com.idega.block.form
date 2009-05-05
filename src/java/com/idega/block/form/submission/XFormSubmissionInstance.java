package com.idega.block.form.submission;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.idega.util.URIUtil;
import com.idega.xformsmanager.util.FormManagerUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $ Last modified: $Date: 2009/05/05 14:12:09 $ by $Author: civilis $
 */
public class XFormSubmissionInstance {
	
	private Node submissionInstance;
	
	public XFormSubmissionInstance(Node submissionInstance) {
		this.submissionInstance = submissionInstance;
	}
	
	/**
	 * @return parameters, that are stored in the instance element node with attribute
	 *         nodeType='formParams'
	 */
	public Map<String, String> getParameters() {
		
		Element paramsEl = FormManagerUtil
		        .getFormParamsElement(submissionInstance);
		
		Map<String, String> parameters = paramsEl == null ? new URIUtil(null)
		        .getParameters() : new URIUtil(paramsEl.getTextContent())
		        .getParameters();
		
		return parameters;
	}
}