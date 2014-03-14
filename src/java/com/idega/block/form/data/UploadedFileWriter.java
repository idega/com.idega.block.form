package com.idega.block.form.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.form.presentation.FormViewer;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.presentation.IWContext;
import com.idega.repository.bean.RepositoryItem;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.idega.util.expression.ELUtil;
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

public class UploadedFileWriter extends DownloadWriter implements MediaWritable {

	public static final String SUBMISSION_ID = FormViewer.submissionIdParam;
	public static final String FILE_ID = "fileId";

	private UploadedFile selectedFile;
	private UploadedFileResolver fileResolver;

	private static final Logger logger = Logger.getLogger(UploadedFileWriter.class.getName());

	@Autowired
	@XFormPersistenceType(CoreConstants.REPOSITORY)
	private transient PersistenceManager persistenceManager;

	public UploadedFileWriter(){}

	@Override
	public void init(HttpServletRequest req, IWContext iwc) {
		String submissionId = iwc.getParameter(SUBMISSION_ID);
		String fileId = iwc.getParameter(FILE_ID);

		Submission sub = getPersistenceManager().getSubmission(Long.parseLong(submissionId));
		List<UploadedFile> files = getFileResolver().resolveUploadedList(sub.getSubmissionDocument());

		selectedFile = getFileFromListById(fileId, files);

		setAsDownload(iwc, selectedFile.getFileName(), getUploadedFileContentLength(selectedFile.getFileURI()));
	}

	@Override
	public String getMimeType() {
		return selectedFile.getMimeType();
	}

	@Override
	public void writeTo(OutputStream streamOut) throws IOException {
		if (selectedFile == null) {
			logger.log(Level.SEVERE, "Unable to get Attached file");
			return;
		}

		InputStream streamIn = getUploadedFileInputStream(selectedFile.getFileURI());
		FileUtil.streamToOutputStream(streamIn, streamOut);

		streamOut.flush();
		streamOut.close();
		streamIn.close();
	}

	private PersistenceManager getPersistenceManager() {
		if(persistenceManager == null) {
			ELUtil.getInstance().autowire(this);
		}
		return persistenceManager;
	}

	private InputStream getUploadedFileInputStream(URI fileUri) {
		try {
			return getRepositoryService().getInputStream(fileUri.getPath());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private int getUploadedFileContentLength(URI fileUri) {
		try {
			RepositoryItem file = getRepositoryService().getRepositoryItemAsRootUser(fileUri.getPath());
			return Long.valueOf(file.getLength()).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private UploadedFile getFileFromListById(String fileId, List<UploadedFile> files) {
		if(files != null) {
			for(UploadedFile file : files) {
				String idToCompareWith = String.valueOf(file.getFileURI().getPath().hashCode());
				if(fileId.equals(idToCompareWith))
					return file;
			}
		}
		return null;
	}

	private UploadedFileResolver getFileResolver() {
		if(fileResolver == null)
			ELUtil.getInstance().autowire(this);
		return fileResolver;
	}

	public void setFileResolver(UploadedFileResolver fileResolver) {
		this.fileResolver = fileResolver;
	}
}
