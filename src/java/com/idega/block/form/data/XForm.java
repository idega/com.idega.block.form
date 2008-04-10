package com.idega.block.form.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.idega.documentmanager.business.XFormState;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/04/10 01:06:11 $ by $Author: civilis $
 */
@Entity
@Table(name="XFORMS")
@NamedQueries(
		{
			@NamedQuery(name=XForm.getAllByTypeAndStorageType, query="from XForm xf where xf."+XForm.formTypeProperty+" = :"+XForm.formTypeProperty+" and xf."+XForm.formStorageTypeProperty+" = :"+XForm.formStorageTypeProperty),
			@NamedQuery(name=XForm.getByParentVersion, query="from XForm xf where xf."+XForm.formParentProperty+" = :"+XForm.formParentProperty+" and xf."+XForm.versionProperty+" = :"+XForm.versionProperty+" and xf."+XForm.formStateProperty+" = :"+XForm.formStateProperty)
		}
)
public class XForm implements Serializable {

	private static final long serialVersionUID = 6769707584973868649L;
	public static final String getAllByTypeAndStorageType = "XForm.getAllByTypeAndStorageType";
	public static final String getByParentVersion = "XForm.getByParentVersion";
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ID_", nullable=false, unique=true)
	private Long formId;
	
	public static final String formParentProperty = "formParent";
	@Column(name="PARENT")
	private Long formParent;
	
	@Column(name="STORAGE_IDENTIFIER", nullable=false)
	private String formStorageIdentifier;
	
	public static final String formStorageTypeProperty = "formStorageType";
	@Column(name="STORAGE_TYPE", nullable=false)
	private String formStorageType;
	
	public static final String formTypeProperty = "formType";
	@Column(name="TYPE", nullable=false)
	private String formType;
	
	public static final String formStateProperty = "formState";
	@Column(name="STATE", nullable=false)
	@Enumerated(EnumType.STRING)
	private XFormState formState;
	
	public static final String versionProperty = "version";
	@Column(name="VERSION", nullable=false)
	private Integer version;
	
	@Column(name="DATE_CREATED")
	private Date dateCreated;
	
	@Column(name="DISPLAY_NAME")
	private String displayName;
	
	public String getFormStorageType() {
		return formStorageType;
	}
	public void setFormStorageType(String formStorageType) {
		this.formStorageType = formStorageType;
	}
	public String getFormType() {
		return formType;
	}
	public void setFormType(String formType) {
		this.formType = formType;
	}
	public Long getFormId() {
		return formId;
	}
	public void setFormId(Long formId) {
		this.formId = formId;
	}
	public Long getFormParent() {
		return formParent;
	}
	public void setFormParent(Long formParent) {
		this.formParent = formParent;
	}
	public String getFormStorageIdentifier() {
		return formStorageIdentifier;
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
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}