package com.idega.block.form.submission;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chiba.xml.xforms.core.Submission;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $ Last modified: $Date: 2009/05/05 14:12:09 $ by $Author: civilis $
 */
public class XFormSubmission {
	
	private Submission submission;
	
	public XFormSubmission(Submission submission) {
		this.submission = submission;
	}
	
	public String getScheme() {
		
		String action = submission.getAction();
		URI uri;
		
		try {
			uri = new URI(action);
			
		} catch (URISyntaxException e) {
			
			Logger.getLogger(getClass().getName()).log(Level.WARNING,
			    "Could not parse the submission action = " + action, e);
			return null;
		}
		
		return uri.getScheme();
	}
}