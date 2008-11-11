package com.idega.block.form.data;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chiba.xml.dom.DOMUtil;
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
public class UploadedFileResolver implements Serializable{
	private static final long serialVersionUID = -2903473188539180900L;
    
    public UploadedFileResolver() {}
	
	public List<UploadedFile> resolveUploadedList(Node submissionInstance) {
		XPathUtil entryFilesXPath = new XPathUtil(".//entry[@filename]");
	    XPathUtil entryFileNameXPath =  new XPathUtil(".//@filename");
	    XPathUtil entryMimeTypeXPath = new XPathUtil(".//@mediatype");
	    
	    final Logger logger = Logger.getLogger(getClass().getName());
		
		final NodeList entries;
		final Node instance = submissionInstance;
		List<UploadedFile> files = null;

		DOMUtil.prettyPrintDOM(instance);
		entries = entryFilesXPath.getNodeset(instance);

		if(entries != null && entries.getLength() != 0) {
			files = new ArrayList<UploadedFile>(entries.getLength());
			
			for (int i = 0; i < entries.getLength(); i++) {
				Node item = entries.item(i);
				DOMUtil.prettyPrintDOM(item);
				String fileName = entryFileNameXPath.getString(item);
				String mimeType = entryMimeTypeXPath.getString(item);
				
				Node uriStrItem = item.getChildNodes().item(0);
		    	String uriStr = uriStrItem.getTextContent();
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
