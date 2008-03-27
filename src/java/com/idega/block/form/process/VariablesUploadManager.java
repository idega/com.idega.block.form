package com.idega.block.form.process;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.idega.chiba.web.upload.UploadManagerType;
import com.idega.chiba.web.xml.xforms.connector.webdav.FileUploadManager;
import com.idega.util.xml.XPathUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2008/03/27 14:13:12 $ by $Author: civilis $
 */
@Scope("singleton")
@Service(XFormsBPMSubmissionHandler.variablesUploadManagerBeanIdentifier)
@UploadManagerType("variables")
public class VariablesUploadManager extends FileUploadManager {
	
	private static final String VARIABLE_NAME_VAR = "variableName";
	private static final String NODESET_XPATH = ".//node()[@mapping = $variableName]";
	
	final private XPathUtil util = new XPathUtil(NODESET_XPATH);
	
	@Override
	protected Element getUploadsElement(String identifier, Node context) {
		
		if(context instanceof Element && identifier.equals(((Element)context).getAttribute("mapping"))) {
			
			return (Element)context;
		}

		synchronized (util) {
		
			util.setVariable(VARIABLE_NAME_VAR, identifier);
			Element lala = (Element) util.getNode(context);
			return lala;
		}
	}
}