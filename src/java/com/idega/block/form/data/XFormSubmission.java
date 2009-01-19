package com.idega.block.form.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
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

import org.apache.commons.httpclient.HttpException;
import org.w3c.dom.Document;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.xformsmanager.business.Submission;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.util.CoreConstants;
import com.idega.util.xml.XmlUtil;

/**
 * 
 * Stores submissions for each form. Primary use is for storing partial
 * submission of saved form.
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.6 $
 * 
 *          Last modified: $Date: 2009/01/19 21:48:53 $ by $Author: civilis $
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

	@Column(name = "SUBMISSION_IDENTIFIER")
	private String submissionIdentifier;
	
	public static final String submissionUUIDProperty = "submissionUUID";
	@Column(name = "SUBMISSION_UUID", nullable = false)
	private String submissionUUID;

	@Column(name = "SUBMISSION_STORAGE_TYPE", nullable = false)
	private String submissionStorageType;

	@Column(name = "SUBMISSION_STORAGE_IDENTIFIER", nullable = false)
	private String submissionStorageIdentifier;

	@Column(name = "DATE_SUBMITTED", nullable = false)
	private Date dateSubmitted;

	public static final String isFinalSubmissionProperty = "isFinalSubmission";
	@Column(name = "FINAL_SUBMISSION")
	private Boolean isFinalSubmission;

	public static final String xformProperty = "xform";
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH, CascadeType.REMOVE })
	@JoinColumn(name = "XFORM_FK", nullable = false)
	private XForm xform;

	public Long getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(Long submissionId) {
		this.submissionId = submissionId;
	}

	public String getSubmissionIdentifier() {
		return submissionIdentifier;
	}

	public void setSubmissionIdentifier(String submissionIdentifier) {
		this.submissionIdentifier = submissionIdentifier;
	}

	public String getSubmissionStorageIdentifier() {
		return submissionStorageIdentifier;
	}

	public void setSubmissionStorageIdentifier(
			String submissionStorageIdentifier) {
		this.submissionStorageIdentifier = submissionStorageIdentifier;
	}

	public String getSubmissionStorageType() {
		return submissionStorageType;
	}

	public void setSubmissionStorageType(String submissionStorageType) {
		this.submissionStorageType = submissionStorageType;
	}

	public Date getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(Date dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	public XForm getXform() {
		return xform;
	}

	public void setXform(XForm xform) {
		this.xform = xform;
	}

	public Boolean getIsFinalSubmission() {
		return isFinalSubmission == null ? true : isFinalSubmission;
	}

	public void setIsFinalSubmission(Boolean isFinalSubmission) {
		this.isFinalSubmission = isFinalSubmission;
	}

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
			WebdavExtendedResource resource = getWebdavExtendedResource(resourcePath);

			if (!resource.exists())
				throw new IllegalArgumentException(
						"Expected webdav resource doesn't exist. Path provided: "
								+ resourcePath);

			InputStream is = resource.getMethodData();
			DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
			Document resourceDocument = docBuilder.parse(is);
			return resourceDocument;

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private WebdavExtendedResource getWebdavExtendedResource(String path)
			throws HttpException, IOException, RemoteException,
			IBOLookupException {

		IWSlideService service;
		try {
			service = (IWSlideService) IBOLookup.getServiceInstance(
					IWMainApplication.getDefaultIWApplicationContext(),
					IWSlideService.class);
		} catch (IBOLookupException e) {
			throw e;
		}
		return service.getWebdavExtendedResource(path, service
				.getRootUserCredentials());
	}

	public String getSubmissionUUID() {
    	return submissionUUID;
    }

	public void setSubmissionUUID(String submissionUUID) {
    	this.submissionUUID = submissionUUID;
    }
}