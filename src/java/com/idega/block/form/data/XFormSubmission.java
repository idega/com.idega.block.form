package com.idega.block.form.data;

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

import com.idega.documentmanager.business.Submission;

/**
 * 
 * Stores submissions for each form.
 * Primary use is for storing partial submission of saved form.
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2008/10/27 20:18:15 $ by $Author: civilis $
 */
@Entity
@Table(name="XFORMS_SUBMISSIONS")
@NamedQueries(
		{
		}
)
public class XFormSubmission implements Serializable, Submission {

	private static final long serialVersionUID = -7231560026323818449L;

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="SUBMISSION_ID")
	private Long submissionId;
	
	@Column(name="SUBMISSION_IDENTIFIER")
	private String submissionIdentifier;
	
	@Column(name="SUBMISSION_STORAGE_TYPE", nullable=false)
	private String submissionStorageType;
	
	@Column(name="SUBMISSION_STORAGE_IDENTIFIER", nullable=false)
	private String submissionStorageIdentifier;
	
	@Column(name="DATE_SUBMITTED", nullable=false)
	private Date dateSubmitted;
	
	public static final String isFinalSubmissionProperty = "isFinalSubmission";
	@Column(name="FINAL_SUBMISSION")
	private Boolean isFinalSubmission;
	
	public static final String xformProperty = "xform";
	@ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name="XFORM_FK", nullable=false)
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
	public void setSubmissionStorageIdentifier(String submissionStorageIdentifier) {
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
		return isFinalSubmission;
	}
	public void setIsFinalSubmission(Boolean isFinalSubmission) {
		this.isFinalSubmission = isFinalSubmission;
	}
}