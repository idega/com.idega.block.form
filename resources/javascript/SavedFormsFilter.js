if(SavedFormsFilter == null) var SavedFormsFilter = {};

SavedFormsFilter.variables = [];
SavedFormsFilter.LOADING_MESSAGE = "";

SavedFormsFilter.getVariables = function() {
	return SavedFormsFilter.variables;
}

SavedFormsFilter.clear = function() {
	var inputs = jQuery('input.value', jQuery('div.parametersLayer'));
	jQuery.each(inputs, function() {
		jQuery(this).val('');
	});
	
	SavedFormsFilter.deleteAllVariables(); 
	SavedFormsFilter.loadFilteredForms(null);
}

SavedFormsFilter.deleteAllVariables = function() {
	for (var i in SavedFormsFilter.variables) {
		delete SavedFormsFilter.variables[i];
	}
}

SavedFormsFilter.addVariable = function(variableName, variableValue) {
	SavedFormsFilter.variables.push(variableName + '=' + variableValue);
	return true;
}

SavedFormsFilter.removeVariable = function(variableName) {
	if (variableName == null || variableName.length < 1) {
		return false;
	}
	
	for (var i in SavedFormsFilter.variables) {
		if (SavedFormsFilter.variables[i].indexOf(variableName) != -1) {
			delete SavedFormsFilter.variables[i];
			return true;
		}
	}
	
	return false;
}

SavedFormsFilter.updateVariable = function(variableName) {
	var variableValue = jQuery("[name='" + "prm_" + variableName + "']").val();
	
	if (SavedFormsFilter.getVariables().length > 0) {
		SavedFormsFilter.removeVariable(variableName);
	}
	
	if (variableValue == null || variableValue.length < 1) {
		return false;
	}
	
	return SavedFormsFilter.addVariable(variableName, variableValue);
}

SavedFormsFilter.loadFilteredForms = function(parameters) {
	showLoadingMessage(SavedFormsFilter.LOADING_MESSAGE);
	
	var instanceID = jQuery('.savedFormsViewer').attr('id');
	var parentContainer = jQuery('#' + instanceID).parent();
	if (parentContainer != null && parentContainer.length > 0) {
		jQuery('#' + instanceID).remove();
	}
	
	var properties = [];
	var variableString = '';
	
	if (parameters != null && parameters.length > 0) {
		for (var i in parameters) {
			variableString = variableString + parameters[i];
			if (i < parameters.length - 1) {
				variableString = variableString + ',';
			}
		}
		
		properties.push({id: 'setVariablesWithValues', value: variableString});
	}
	
	IWCORE.renderComponent(instanceID, parentContainer, function(result) {
		closeAllLoadingMessages();
	}, properties, {append: true});
}