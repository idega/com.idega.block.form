package com.idega.block.form.entries.presentation.beans;

import java.util.List;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.form.data.UploadedFile;
import com.idega.block.form.data.UploadedFileResolver;
import com.idega.block.form.data.UploadedFileWriter;
import com.idega.xformsmanager.business.PersistenceManager;
import com.idega.xformsmanager.business.Submission;
import com.idega.xformsmanager.business.XFormPersistenceType;

/**
 *
 * 
 * @author <a href="anton@idega.com">Anton Makarov</a>
 * @version Revision: 1.0 
 *
 * Last modified: Oct 31, 2008 by Author: Anton 
 *
 */
@Scope("singleton")
@Service("entryAttachments")
public class FormEntryAttachments {
	private static final String submissionIdParam = "submissionId";
	
	@Autowired @XFormPersistenceType("slide") private transient PersistenceManager persistenceManager;
	@Autowired private UploadedFileResolver fileResolver;
	
	public List<UploadedFile> getAttachments() {
		Submission sub = getPersistenceManager().getSubmission(getSubmissionId());
		UploadedFileResolver resolver = new UploadedFileResolver();
		List<UploadedFile> files = resolver.resolveUploadedList(sub.getSubmissionDocument());

		return files;			
	}
	
	public Class<?> getDownloadWriter() {
		return UploadedFileWriter.class;
	}	
	
	private PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}
	
	private long getSubmissionId() {
		String submissionIdStr = (String) FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get(submissionIdParam);
		
		return Long.parseLong(submissionIdStr);
	}
	
	private UploadedFileResolver getFileResolver() {
		return fileResolver;
	}
}
