package com.idega.block.form.save;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $ Last modified: $Date: 2009/05/05 14:12:09 $ by $Author: civilis $
 */
public interface FormSavePhasePlugin {
	
	/**
	 * used to designate which plugins should be used regarding submission schema
	 * 
	 * @return
	 */
	public abstract String getSubmissionScheme();
	
	/**
	 * used to create new instances of managed beans
	 * 
	 * @return
	 */
	public abstract String getBeanIdentifier();
	
	public abstract void afterSave(FormSavePhasePluginParams params);
}