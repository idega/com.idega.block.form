package com.idega.block.form.process.converters;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chiba.xml.dom.DOMUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idega.core.file.data.ExtendedFile;
import com.idega.core.file.tmp.TmpFilesManager;
import com.idega.core.file.tmp.TmpFileResolverType;
import com.idega.core.file.tmp.TmpFileResolver;
import com.idega.jbpm.def.VariableDataType;
import com.idega.util.CoreConstants;
import com.idega.util.xml.XPathUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.4 $
 *
 * Last modified: $Date: 2008/05/12 23:12:00 $ by $Author: anton $
 */
@Scope("singleton")
@Service
public class FilesConverter implements DataConverter {
	
	private TmpFilesManager uploadsManager;
	private TmpFileResolver uploadResourceResolver;
	private static final String mappingAtt = "mapping";

	public Object convert(Element ctx) {
		
		String variableName = ctx.getAttribute(mappingAtt);
		
		Collection<File> files = getUploadsManager().getFiles(variableName, ctx, getUploadResourceResolver());
		Collection<ExtendedFile> filesAndDescriptions = new ArrayList<ExtendedFile>();
		
		for(File file : files) {
			String path = file.getPath();
			String description = getDescriptionByUri(variableName, ctx, path);
			ExtendedFile exFile = new ExtendedFile(file, description);
			filesAndDescriptions.add(exFile);
		}
		
		return filesAndDescriptions;
	}
	
	private String getDescriptionByUri(String identifier, Object resource, String uri) {
		String desc = null;
		
		if(!(resource instanceof Node)) {	
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Wrong resource provided. Expected of type "+Node.class.getName()+", but got "+resource.getClass().getName());
			return null;
		}
		XPathUtil entriesXPUT = new XPathUtil("./entry");
		
		Node instance = (Node)resource;
		Element node = getUploadsElement(identifier, instance);
		NodeList entries;
		
		synchronized (entriesXPUT) {
			entries = entriesXPUT.getNodeset(node);
		}
		
		if(entries != null) {
			for (int i = 0; i < entries.getLength(); i++) {
				String uriStr = entries.item(i).getChildNodes().item(0).getTextContent();
		    	
		    	if(!CoreConstants.EMPTY.equals(uriStr) && !CoreConstants.DOUBLENEWLINE.equals(uriStr)) {
		    		
		    		if(uriStr.startsWith("file:"))
		    			uriStr = uriStr.substring("file:".length());
		    		
		    		try {
						uriStr = URLDecoder.decode(uriStr, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					if(uriStr.equals(uri)) {
						Node descNode = entries.item(i).getChildNodes().item(1);
						if(descNode != null) {
							desc = descNode.getTextContent();
							break;
						}
					}
		    	}
			}
		}
		return desc;
	}
	
	protected Element getUploadsElement(String identifier, Node context) {
		if(context instanceof Element && identifier.equals(((Element)context).getAttribute("mapping"))) {	
			return (Element)context;
		} else {
			return null;
		}
	}
	
	public Element revert(Object o, Element e) {
	
		Logger.getLogger(getClass().getName()).log(Level.WARNING, "UNSUPPORTED OPERATION");
		return e;
	}
	
	public VariableDataType getDataType() {
		return VariableDataType.FILES;
	}
	
	public TmpFilesManager getUploadsManager() {
		return uploadsManager;
	}
	
	@Autowired
	public void setUploadsManager(TmpFilesManager uploadsManager) {
		this.uploadsManager = uploadsManager;
	}
	public TmpFileResolver getUploadResourceResolver() {
		return uploadResourceResolver;
	}
	
	@Autowired
	public void setUploadResourceResolver(@TmpFileResolverType("xformVariables")
			TmpFileResolver uploadResourceResolver) {
		this.uploadResourceResolver = uploadResourceResolver;
	}
}