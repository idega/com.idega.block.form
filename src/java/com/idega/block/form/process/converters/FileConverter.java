package com.idega.block.form.process.converters;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.idega.chiba.web.upload.UploadManagerType;
import com.idega.chiba.web.xml.xforms.connector.webdav.FileUploadManager;
import com.idega.jbpm.def.VariableDataType;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/03/27 14:13:11 $ by $Author: civilis $
 */
@Scope("singleton")
@Service
public class FileConverter implements DataConverter {
	
	private FileUploadManager uploadsManager;
	private static final String mappingAtt = "mapping";

	public Object convert(Element ctx) {
		
		String variableName = ctx.getAttribute(mappingAtt);
		
		List<File> files = getUploadsManager().getFiles(variableName, ctx);
		return files.isEmpty() ? null : files.iterator().next();
	}
	public Element revert(Object o, Element e) {
	
		Logger.getLogger(getClass().getName()).log(Level.WARNING, "UNSUPPORTED OPERATION");
		return e;
	}
	
	public VariableDataType getDataType() {
		return VariableDataType.FILE;
	}
	
	public FileUploadManager getUploadsManager() {
		return uploadsManager;
	}
	
	@Autowired
	@UploadManagerType("variables")
	public void setUploadsManager(FileUploadManager uploadsManager) {
		this.uploadsManager = uploadsManager;
	}
}