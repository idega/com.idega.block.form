package com.idega.block.form.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idega.util.CoreConstants;
import com.idega.util.xml.XPathUtil;

/**
 *
 * 
 * @author <a href="anton@idega.com">Anton Makarov</a>
 * @version Revision: 1.0 
 *
 * Last modified: Oct 31, 2008 by Author: Anton 
 *
 */

@Service
@Scope("singleton")
public class UploadedFileResolver {
	
    private final XPathUtil entryFilesXPath = new XPathUtil(".//entry[@filename]");
    private final XPathUtil entryFileNameXPath =  new XPathUtil(".//@filename");
    private final XPathUtil entryMimeTypeXPath = new XPathUtil(".//@mediatype");;
    
	private final Logger logger = Logger.getLogger(getClass().getName());;
    
    public UploadedFileResolver() {}
	
	public List<UploadedFile> resolveUploadedList(Node submissionInstance) {
		final NodeList entries;
		final Node instance = submissionInstance;
		List<UploadedFile> files = null;

		entries = entryFilesXPath.getNodeset(instance);

		if(entries != null && entries.getLength() != 0) {
			files = new ArrayList<UploadedFile>(entries.getLength());
			
			for (int i = 0; i < entries.getLength(); i++) {
				Node item = entries.item(i);
				String fileName = entryFileNameXPath.getString(item);
				String mimeType = entryMimeTypeXPath.getString(item);
				String uriStr = item.getTextContent();
		    	
		    	if(!CoreConstants.EMPTY.equals(uriStr) && !uriStr.startsWith(CoreConstants.NEWLINE)) {
		    		
		    		try {
		    			URI uri = new URI(uriStr);
		    			
		    			UploadedFile file = new UploadedFile(fileName, uri, mimeType);	
		    			files.add(file);
					} catch (Exception e) {
						logger.log(Level.WARNING, "Exception while decoding and creating uri from uri string. Skipping. Uri="+uriStr, e);
					}
		    	}
			}
		}
		return files;
	}
}
