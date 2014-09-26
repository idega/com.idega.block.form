package com.idega.block.form.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.idega.block.process.data.Case;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;
import com.idega.xformsmanager.business.Form;
import com.idega.xformsmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.8 $
 *
 *          Last modified: $Date: 2008/12/28 11:39:50 $ by $Author: civilis $
 */
@Entity
@Table(name = "XFORMS")
@NamedQueries( {
		@NamedQuery(name = XForm.getAllByTypeAndStorageType, query = "from XForm xf where xf."
				+ XForm.formTypeProperty
				+ " = :"
				+ XForm.formTypeProperty
				+ " and xf."
				+ XForm.formStorageTypeProperty
				+ " = :"
				+ XForm.formStorageTypeProperty),
		@NamedQuery(name = XForm.getByParentVersion, query = "from XForm xf where xf."
				+ XForm.formParentProperty
				+ " = :"
				+ XForm.formParentProperty
				+ " and xf."
				+ XForm.versionProperty
				+ " = :"
				+ XForm.versionProperty
				+ " and xf."
				+ XForm.formStateProperty
				+ " = :" + XForm.formStateProperty),
		@NamedQuery(name = XForm.getByFormId, query = "from XForm xf where xf."
				+ XForm.formIdProperty + " = :" + XForm.formIdProperty) })
public class XForm implements Serializable, Form {

	private static final long serialVersionUID = 6769707584973868649L;
	public static final String getAllByTypeAndStorageType = "XForm.getAllByTypeAndStorageType";
	public static final String getByParentVersion = "XForm.getByParentVersion";
	public static final String getByFormId = "XForm.getByFormId";

	public static final String formIdProperty = "formId";
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "FORM_ID")
	private Long formId;

	public static final String formParentProperty = "formParent";
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
			CascadeType.REFRESH, CascadeType.REMOVE })
	@JoinColumn(name = "FORM_PARENT")
	private XForm formParent;

	public static final String formStorageIdentifierProperty = "formStorageIdentifier";
	@Column(name = "FORM_STORAGE_IDENTIFIER", nullable = false)
	private String formStorageIdentifier;

	public static final String formStorageTypeProperty = "formStorageType";
	@Column(name = "FORM_STORAGE_TYPE", nullable = false)
	private String formStorageType;

	public static final String formTypeProperty = "formType";
	@Column(name = "FORM_TYPE", nullable = false)
	private String formType;

	public static final String formStateProperty = "formState";
	@Column(name = "FORM_STATE", nullable = false)
	@Enumerated(EnumType.STRING)
	private XFormState formState;

	public static final String versionProperty = "version";
	@Column(name = "FORM_VERSION", nullable = false)
	private Integer version;

	@Column(name = "DATE_CREATED")
	private Date dateCreated;

	public static final String displayNameProperty = "displayName";
	@Column(name = "DISPLAY_NAME")
	private String displayName;

	public static final String xformSubmissionsProperty = "xformSubmissions";
	@OneToMany(mappedBy = XFormSubmission.xformProperty, cascade = {
			CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	private List<XFormSubmission> xformSubmissions;

	@Override
	public String getFormStorageType() {
		return formStorageType;
	}

	public void setFormStorageType(String formStorageType) {
		this.formStorageType = formStorageType;
	}

	@Override
	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	@Override
	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	@Override
	public XForm getFormParent() {
		return formParent;
	}

	public void setFormParent(XForm formParent) {
		this.formParent = formParent;
	}

	@Override
	public String getFormStorageIdentifier() {
		return formStorageIdentifier;
	}

	/**
	 *
	 * <p>Splits path to form and takes process name from it.</p>
	 * @return process name or <code>null</code> if does not exit.
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	@Transient
	public String getJBPMProcessDefinitionName() {
		if (StringUtil.isEmpty(getFormStorageIdentifier())) {
			return null;
		}

		String[] array = getFormStorageIdentifier().split(CoreConstants.SLASH);
		if (ArrayUtil.isEmpty(array) || array.length < 4) {
			return null;
		}

		return array[3];
	}

	/**
	 *
	 * @return {@link Case#getCaseIdentifier()} or <code>null</code> on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	@Transient
	public String getCaseIdentifier() {
		if (StringUtil.isEmpty(getFormStorageIdentifier())) {
			return null;
		}

		String[] array = getFormStorageIdentifier().split(CoreConstants.SLASH);
		if (ArrayUtil.isEmpty(array) || array.length < 5) {
			return null;
		}

		return array[4];
	}

	public void setFormStorageIdentifier(String formStorageIdentifier) {
		this.formStorageIdentifier = formStorageIdentifier;
	}

	public XFormState getFormState() {
		return formState;
	}

	public void setFormState(XFormState formState) {
		this.formState = formState;
	}

	@Override
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public List<XFormSubmission> getXformSubmissions() {
		return xformSubmissions;
	}

	public void setXformSubmissions(List<XFormSubmission> xformSubmissions) {
		this.xformSubmissions = xformSubmissions;
	}

	@Override
	public String toString() {
		return "XForm ID: " + getFormId();
	}
}