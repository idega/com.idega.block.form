package com.idega.block.form.xml.xforms.connector.context;

import org.chiba.xml.xforms.connector.URIResolver;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas ‰ivilis</a>
 * @version 1.0
 * 
 */
public class KeyContextResolver extends org.chiba.xml.xforms.connector.context.ContextResolver implements URIResolver {

	public static final String autofill_key_prefix = "fb-afk-";
	private static final String response_part1 = "<?xml version='1.0' encoding='UTF-8'?><data><";
	private static final String response_part2 = ">";
	private static final String response_part3 = "</";
	private static final String response_part4 = "></data>";
	private static final String faces_exp_part1 = "#{";
	private static final String faces_exp_part2 = "}";
	
	/**
	 * resolves object, which is configured in the faces-config.xml, method value
	 */
    public Object resolve() throws XFormsException {
    	
        try {
        	String xpath = new URI(getURI()).getSchemeSpecificPart();
        	System.out.println("resolving: "+xpath);

            if(!xpath.startsWith(autofill_key_prefix))
            	return super.resolve();
            
            String key = xpath.substring(autofill_key_prefix.length());
            FacesContext ctx = FacesContext.getCurrentInstance();
            Object value = 
            	ctx.getApplication().createValueBinding(
            		new StringBuilder(faces_exp_part1)
            		.append(key)
            		.append(faces_exp_part2)
            		.toString()
            	).getValue(ctx);
            
	        return createResponseDocument(value == null ? "" : value instanceof String ? (String)value : value.toString(), xpath).getDocumentElement();
        }
        catch (Exception e) {
        	
        	try {
        		return createResponseDocument(autofill_key_prefix, "").getDocumentElement();
			} catch (Exception e2) {
				throw new XFormsException(e2);
			}
        }
    }
    
    protected Document createResponseDocument(String response_text, String key) throws Exception {
		
//      TODO: optimize this if needed (e.g. add caching or smth) 
        String response_xml = 
        	new StringBuilder(response_part1)
        	.append(key)
        	.append(response_part2)
        	.append(response_text)
        	.append(response_part3)
        	.append(key)
        	.append(response_part4)
        	.toString();
        
        InputStream stream = new ByteArrayInputStream(response_xml.getBytes("UTF-8"));
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        stream.close();
        return doc;
	}
}