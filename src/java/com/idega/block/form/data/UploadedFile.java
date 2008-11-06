package com.idega.block.form.data;

import java.net.URI;

/**
 *
 * 
 * @author <a href="anton@idega.com">Anton Makarov</a>
 * @version Revision: 1.0 
 *
 * Last modified: Oct 31, 2008 by Author: Anton 
 *
 */

public class UploadedFile {
	private String fileName;
	private URI fileURI;
	private	String mimeType;
	
	public UploadedFile(){}
	
	public UploadedFile(String fileName, URI uri, String mimeType) {
		this.fileName = fileName;
		this.fileURI = uri;
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public URI getFileURI() {
		return fileURI;
	}

	public void setFileURI(URI fileURI) {
		this.fileURI = fileURI;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getId() {
		return String.valueOf(getFileURI().getPath().hashCode());
	}
}
