package com.idega.block.form.save;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $ Last modified: $Date: 2009/05/05 14:12:09 $ by $Author: civilis $
 */
@Service
@Scope("singleton")
public class FormSavePhasePluginFactory {
	
	private Multimap<String, String> pluginsIdentifiers = new HashMultimap<String, String>();
	
	@Autowired
	public void setFormSavePhasePlugins(List<FormSavePhasePlugin> plugins) {
		
		pluginsIdentifiers.clear();
		
		for (FormSavePhasePlugin plugin : plugins) {
			
			pluginsIdentifiers.put(plugin.getSubmissionScheme(), plugin
			        .getBeanIdentifier());
		}
	}
	
	public Collection<FormSavePhasePlugin> getPlugins(String scheme) {
		
		Collection<String> pluginsIdentifiers = this.pluginsIdentifiers
		        .get(scheme);
		
		List<FormSavePhasePlugin> plugins;
		
		if (!ListUtil.isEmpty(pluginsIdentifiers)) {
			
			plugins = new ArrayList<FormSavePhasePlugin>(pluginsIdentifiers
			        .size());
			
			for (String identifier : pluginsIdentifiers) {
				
				FormSavePhasePlugin plugin = ELUtil.getInstance().getBean(
				    identifier);
				String pluginScheme = plugin.getSubmissionScheme();
				
				if (scheme.equals(pluginScheme)) {
					
					plugins.add(plugin);
					
				} else {
					Logger.getLogger(getClass().getName()).log(
					    Level.WARNING,
					    "Plugin with wrong schema type resolved by schema = "
					            + scheme + ", plugin provided schema="
					            + pluginScheme);
				}
			}
		} else {
			plugins = Collections.emptyList();
		}
		
		return plugins;
	}
}