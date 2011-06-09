package com.idega.block.form.data;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.xml.XmlUtil;
import com.idega.xformsmanager.business.Submission;

/**
 * Stores submissions for each form. Primary use is for storing partial submission of saved form.
 *
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.9 $ Last modified: $Date: 2009/05/05 14:10:45 $ by $Author: civilis $
 */
@Entity
@Table(name = "XFORMS_SUBMISSIONS")
@NamedQueries( {})
public class XFormSubmission implements Serializable, Submission {

	private static final long serialVersionUID = -7231560026323818449L;
	private static final String submissionFileName = "submission.xml";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SUBMISSION_ID")
	private Long submissionId;

	public static final String submissionUUIDProperty = "submissionUUID";
	@Column(name = "SUBMISSION_UUID", nullable = false)
	private String submissionUUID;

	@Column(name = "SUBMISSION_STORAGE_TYPE", nullable = false)
	private String submissionStorageType;

	@Column(name = "SUBMISSION_STORAGE_IDENTIFIER", nullable = false)
	private String submissionStorageIdentifier;

	public static final String dateSubmittedProperty = "dateSubmitted";
	@Column(name = "DATE_SUBMITTED", nullable = false)
	private Date dateSubmitted;

	public static final String formSubmitterProperty = "formSubmitter";
	@Column(name = "FORM_SUBMITTER")
	private Integer formSubmitter;

	public static final String isFinalSubmissionProperty = "isFinalSubmission";
	@Column(name = "FINAL_SUBMISSION")
	private Boolean isFinalSubmission;

	public static final String isValidSubmissionProperty = "isValidSubmission";
	@Column(name = "VALID_SUBMISSION")
	private Boolean isValidSubmission;

	public static final String xformProperty = "xform";
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
	        CascadeType.REFRESH, CascadeType.REMOVE })
	@JoinColumn(name = "XFORM_FK", nullable = false)
	private XForm xform;

	@Override
	public Long getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(Long submissionId) {
		this.submissionId = submissionId;
	}

	@Override
	public String getSubmissionStorageIdentifier() {
		return submissionStorageIdentifier;
	}

	public void setSubmissionStorageIdentifier(
	        String submissionStorageIdentifier) {
		this.submissionStorageIdentifier = submissionStorageIdentifier;
	}

	@Override
	public String getSubmissionStorageType() {
		return submissionStorageType;
	}

	public void setSubmissionStorageType(String submissionStorageType) {
		this.submissionStorageType = submissionStorageType;
	}

	@Override
	public Date getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(Date dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	@Override
	public XForm getXform() {
		return xform;
	}

	public void setXform(XForm xform) {
		this.xform = xform;
	}

	@Override
	public Boolean getIsFinalSubmission() {
		return isFinalSubmission == null ? true : isFinalSubmission;
	}

	public void setIsFinalSubmission(Boolean isFinalSubmission) {
		this.isFinalSubmission = isFinalSubmission;
	}

	@Override
	public Document getSubmissionDocument() {

		String submissionPath = getSubmissionStorageIdentifier();
		StringBuilder subSB = new StringBuilder(submissionPath);

		if (!submissionPath.endsWith(CoreConstants.SLASH))
			subSB.append(CoreConstants.SLASH);

		subSB.append(submissionFileName);
		submissionPath = subSB.toString();

		Document submissionDoc = loadXMLResourceFromSlide(submissionPath);

		return submissionDoc;
	}

	private Document loadXMLResourceFromSlide(String resourcePath) {

		try {
			if (!getSlideService().getExistence(resourcePath))
				throw new IllegalArgumentException("Expected webdav resource doesn't exist. Path provided: " + resourcePath);

			InputStream is = getSlideService().getInputStream(resourcePath);
			DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
			Document resourceDocument = docBuilder.parse(is);
			return resourceDocument;

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private IWSlideService getSlideService() throws IBOLookupException {
		return IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), IWSlideService.class);
	}

	@Override
	public String getSubmissionUUID() {
		return submissionUUID;
	}

	public void setSubmissionUUID(String submissionUUID) {
		this.submissionUUID = submissionUUID;
	}

	public Boolean getIsValidSubmission() {
		return isValidSubmission;
	}

	public void setIsValidSubmission(Boolean isValidSubmission) {
		this.isValidSubmission = isValidSubmission;
	}

	public Integer getFormSubmitter() {
		return formSubmitter;
	}

	public void setFormSubmitter(Integer formSubmitter) {
		this.formSubmitter = formSubmitter;
	}

}