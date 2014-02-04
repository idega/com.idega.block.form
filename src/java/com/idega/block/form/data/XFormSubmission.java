package com.idega.block.form.data;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilder;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.idega.block.form.business.XFormPersistenceService;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.IOUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;
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
@NamedQueries({})
public class XFormSubmission implements Serializable, Submission {

	private static final long serialVersionUID = -7231560026323818449L;
	private static final String submissionFileName = "submission.xml";

	public XFormSubmission() {
		super();
	}

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

	@Column(name = "provider")
	private Long provider;

	public static final String isDeletedProperty = "deleted";
	public static final String COLUMN_ID_DELETED = "isDeleted";
	@Column(name = COLUMN_ID_DELETED, nullable = true)
	private Boolean deleted;

	public Boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getProvider() {
		return provider;
	}

	public void setProvider(Long provider) {
		this.provider = provider;
	}

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

	@Transient
	private Document document;

	@Override
	public Document getSubmissionDocument() {
		if (document == null) {
			String submissionPath = getSubmissionStorageIdentifier();
			StringBuilder subSB = new StringBuilder(submissionPath);

			if (!submissionPath.endsWith(CoreConstants.SLASH))
				subSB.append(CoreConstants.SLASH);

			subSB.append(submissionFileName);
			submissionPath = subSB.toString();

			document = loadXMLResourceFromSlide(submissionPath);
		}

		return document;
	}

	private Document loadXMLResourceFromSlide(String resourcePath) {
		Document xform = null;
		InputStream stream = null;
		DocumentBuilder docBuilder = null;

		try {
			if (!getSlideService().getExistence(resourcePath))
				throw new IllegalArgumentException("Expected webdav resource doesn't exist. Path provided: " + resourcePath);

			stream = getSlideService().getInputStream(resourcePath);
			docBuilder = XmlUtil.getDocumentBuilder();
			xform = docBuilder.parse(stream);
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error loading resource: " + resourcePath, e);
		} finally {
			IOUtil.close(stream);
		}

		if (xform != null)
			return xform;

		try {
			stream = getSlideService().getInputStream(resourcePath);
			String content = XmlUtil.getCleanedXml(stream);
			IOUtil.close(stream);

			stream = StringHandler.getStreamFromString(content);
			xform = docBuilder.parse(stream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(stream);
		}

		return xform;
	}

	private IWSlideService getSlideService() throws IBOLookupException {
		return IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), IWSlideService.class);
	}

	/**
	 *
	 * <p>Searches for variable value from saved document.</p>
	 * @param variableName to find value for, not <code>null</code>;
	 * @return BPM variable value, if found, null otherwise.
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	@Transient
	public String getVariableValue(String variableName) {
		if (StringUtil.isEmpty(variableName)) {
			return null;
		}

		Map<String, String> variables = getVariables();
		if (MapUtil.isEmpty(variables)) {
			return null;
		}

		return variables.get(variableName);
	}

	@Transient
	private Map<String, String> variables;

	@Transient
	public Map<String, String> getVariables() {
		if (variables == null) {
			variables = new HashMap<String, String>();

			if (getProvider() == null || getProvider() < 0) {
				//	Loading variables from repository
				variables = getVariablesFromRepository();
			} else {
				//	Getting variables from DB
				try {
					variables = getVariablesFromDatabase();
				} catch (Exception e) {
					//	In case of error, loading from repository
					variables = getVariablesFromRepository();
				}
			}
		}

		return variables;
	}

	@Transient
	private Map<String, String> getVariablesFromRepository() {
		if (variables == null) {
			variables = new HashMap<String, String>();
		}

		Document submissionDocument = getSubmissionDocument();
		if (submissionDocument == null) {
			return variables;
		}

		List<Node> nodes = XmlUtil.getChildNodes(submissionDocument.getDocumentElement(), null, null, "mapping", null);
		if (ListUtil.isEmpty(nodes)) {
			return variables;
		}

		for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
			Node node = i.next();

			org.w3c.dom.Element element = null;
			if (node instanceof org.w3c.dom.Element) {
				element = (org.w3c.dom.Element) node;
			} else {
				continue;
			}

			String name = element.getAttribute("mapping");
			String value = node.getTextContent();
			if (!StringUtil.isEmpty(name) && !StringUtil.isEmpty(value)) {
				variables.put(name.trim(), value.trim());
			}
		}

		return variables;
	}

	@Transient
	private Map<String, String> getVariablesFromDatabase() throws Exception {
		IWMainApplication iwma = IWMainApplication.getDefaultIWMainApplication();
		@SuppressWarnings("unchecked")
		Map<String, Object> beans = WebApplicationContextUtils.getWebApplicationContext(iwma.getServletContext())
				.getBeansOfType(XFormPersistenceService.class);
		if (MapUtil.isEmpty(beans)) {
			throw new RuntimeException("Unable to find any implementation(s) for " + XFormPersistenceService.class.getName());
		}

		for (Object object: beans.values()) {
			XFormPersistenceService service = CoreUtil.getUnProxied(object);
			Map<String, String> variables = service.getVariables(getProvider());
			if (!MapUtil.isEmpty(variables)) {
				return variables;
			}
		}

		throw new RuntimeException("No variables were found by " + beans.values());
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

	@Override
	public String toString() {
		return "ID: " + getSubmissionId() + ", UUID: " + getSubmissionUUID() + ", submitted: " + getDateSubmitted() + " by (user ID): "
				+ getFormSubmitter();
	}
}