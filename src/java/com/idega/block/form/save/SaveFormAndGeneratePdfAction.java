package com.idega.block.form.save;

import org.chiba.xml.xforms.core.Instance;
import org.chiba.xml.xforms.core.Model;
import org.chiba.xml.xforms.core.ModelItem;
import org.chiba.xml.xforms.exception.XFormsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import com.idega.block.form.presentation.FormViewer;
import com.idega.block.pdf.business.PDFWriterProvider;
import com.idega.chiba.web.xml.xforms.util.XFormsUtil;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MediaWritable;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;

/**
 * Handles XForm's action - saves form and generates URI to media servlet that does actual PDF
 * rendering
 *
 * @author <a href="mailto:valdas@idega.com>Valdas Žemaitis</a> Created: 2009.02.12
 * @version $Revision: 1.2 $ Last modified: $Date: 2009/05/05 14:12:00 $ by $Author: civilis $
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

			final String submissionRepresentationExp = getXFormsAttribute("submissionIdentifier");
			setSubmissionRepresentationExp(submissionRepresentationExp);

			final String submissionIdExp = getXFormsAttribute(FormViewer.submissionIdParam);
			setSubmissionIdExp(submissionIdExp);

			final String formIdExp = getXFormsAttribute("formId");
			final String formIdStr = (String) (formIdExp != null
			        && formIdExp.length() != 0 ? XFormsUtil
			        .getValueFromExpression(formIdExp, this) : null);

			if (!StringUtil.isEmpty(formIdStr)) {

				setFormId(new Long(formIdStr));

			} else {

				throw new XFormsException(
				        "No form id resolved from the expression provided="
				                + formIdExp);
			}

		} else {
			throw new XFormsException("No action specified");
		}
	}

	protected void saveForm() throws XFormsException {
		super.perform();
		saveSubmission();

		doRebuild(true);
		doRecalculate(true);
		doRevalidate(true);
		doRefresh(true);
	}

	@Override
	public void perform() throws XFormsException {
		saveForm();
		final String submissionUUID = getSubmissionUUID();

		Instance instance = getInstance();

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