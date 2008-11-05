package com.idega.block.form.submission;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.core.Submission;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

import com.idega.chiba.web.xml.xforms.util.XFormsUtil;
import com.idega.xformsmanager.business.Document;
import com.idega.xformsmanager.business.DocumentManager;
import com.idega.xformsmanager.business.DocumentManagerFactory;
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.business.XFormPersistenceType;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.util.expression.ELUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.3 $
 * 
 *          Last modified: $Date: 2008/11/05 08:51:02 $ by $Author: civilis $
 */
public class StandaloneSubmissionHandler extends AbstractConnector implements
		SubmissionHandler {

	@Autowired
	@XFormPersistenceType("slide")
	private PersistenceManager persistenceManager;
	@Autowired private DocumentManagerFactory documentManagerFactory;

	public Map<String, Object> submit(Submission submission, Node instance)
			throws XFormsException {

		// method - post, replace - none
		if (!submission.getReplace().equalsIgnoreCase("none"))
			throw new XFormsException("Submission mode '"
					+ submission.getReplace() + "' not supported");

		if (!submission.getMethod().equalsIgnoreCase("put")
				&& !submission.getMethod().equalsIgnoreCase("post"))
			throw new XFormsException("Submission method '"
					+ submission.getMethod() + "' not supported");

		if (submission.getMethod().equalsIgnoreCase("put")) {
			// update (put)
			// currently unsupported
			throw new XFormsException("Submission method '"
					+ submission.getMethod() + "' not yet supported");

		} else {
			// insert (post)
		}

		try {
			String submissionIdentifier = String.valueOf(System
					.currentTimeMillis());
			InputStream is = getISFromXML(instance);
			String formId = XFormsUtil.getFormId(instance);
			Long fid = new Long(formId);
			
			IWMainApplication iwma = IWContext.getCurrentInstance().getIWMainApplication();
			
			DocumentManager docMan = getDocumentManagerFactory().newDocumentManager(iwma);
//			taking form, meaning, that this form should be saved to the repository already, before submitting it 
//			(that could be issue in formbuilder preview). just fix it, probably saving before taking, or something
			Document formDocument = docMan.takeForm(fid);

			/* Long submissionId = */getPersistenceManager().saveSubmittedData(
					formDocument.getFormId(), is, submissionIdentifier, true);
			return null;

		} catch (Exception e) {
			throw new XFormsException(e);
		}
	}

	private InputStream getISFromXML(Node node) throws TransformerException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtil.prettyPrintDOM(node, out);
		return new ByteArrayInputStream(out.toByteArray());
	}

	PersistenceManager getPersistenceManager() {

		if (persistenceManager == null)
			ELUtil.getInstance().autowire(this);

		return persistenceManager;
	}

	void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	DocumentManagerFactory getDocumentManagerFactory() {
		
		if(documentManagerFactory == null)
			ELUtil.getInstance().autowire(this);
		
		return documentManagerFactory;
	}

	void setDocumentManagerFactory(DocumentManagerFactory documentManagerFactory) {
		this.documentManagerFactory = documentManagerFactory;
	}
}