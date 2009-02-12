package com.idega.block.form.save;

import org.chiba.xml.xforms.core.Instance;
import org.chiba.xml.xforms.core.Model;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import com.idega.block.pdf.business.PDFWriterProvider;
import com.idega.chiba.web.xml.xforms.util.XFormsUtil;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MediaWritable;
import com.idega.util.URIUtil;

/**
 * Handles XForm's action - saves form and generates URI to media servlet that does actual PDF rendering
 * @author <a href="mailto:valdas@idega.com>Valdas Žemaitis</a>
 * Created: 2009.02.12
 * @version $Revision: 1.1 $
 * Last modified: $Date: 2009/02/12 14:02:01 $ by $Author: valdas $
 */
public class SaveFormAndGeneratePdfAction extends SaveFormAction {

	private static final String saveFormAndGeneratePdf = "saveFormAndGeneratePdf";
	
	@Autowired
	private PDFWriterProvider writerProvider;
	
	public SaveFormAndGeneratePdfAction(Element element, Model model) {
		super(element, model);
	}
	
	@Override
	public void init() throws XFormsException {
		final String action = getXFormsAttribute("action");
		
		if (saveFormAndGeneratePdf.equals(action)) {
			final String instanceId = getXFormsAttribute("instanceId");
			setInstanceId(instanceId);
			
			final String linkExp = getXFormsAttribute("linkLocation");
			setLinkExp(linkExp);
			
			final String submissionIdentifier = getXFormsAttribute("submissionIdentifier");
			setSubmissionIdentifier(submissionIdentifier);
			
			final String submissionIdExp = getXFormsAttribute("submissionId");
			setSubmissionIdExp(submissionIdExp);
			
			final String formIdExp = getXFormsAttribute("formId");
			final Object formIdVal = formIdExp != null
			        && formIdExp.length() != 0 ? XFormsUtil
			        .getValueFromExpression(formIdExp, this) : null;
			setFormId(formIdVal instanceof String ? formIdVal.toString() : null);
			        
		} else {
			throw new XFormsException("No action specified");
		}
	}

	@Override
	public void perform() throws XFormsException {
		super.perform();
		
		String formId = getFormId();
		Long fid = new Long(formId);
		Instance instance = getInstance();
		
		final String[] submissionMeta = saveSubmission(fid, instance);
		final String submissionUUID = submissionMeta[0];
		
		doRebuild(true);
		doRecalculate(true);
		doRevalidate(true);
		doRefresh(true);
			
		URIUtil uriUtil = new URIUtil(IWMainApplication.getDefaultIWMainApplication().getMediaServletURI());
		uriUtil.setParameter(MediaWritable.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(getWriterProvider().getPDFWriterClass()));
		uriUtil.setParameter(getWriterProvider().getFormSubmissionUniqueIdParameterName(), submissionUUID);

		ModelItem mi = instance.getModelItem(getLinkExp());
		mi.setValue(uriUtil.getUri());
	}
	
	public PDFWriterProvider getWriterProvider() {
		return writerProvider;
	}

	public void setWriterProvider(PDFWriterProvider writerProvider) {
		this.writerProvider = writerProvider;
	}

}