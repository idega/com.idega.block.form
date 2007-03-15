package com.idega.block.form.bean;

import javax.faces.model.SelectItem;

/**
 * A container for list of form ids and names: <code>value</code> holds formId,
 * <code>label</code> holds form's title. This class is needed for formadmin, which
 * expects a property named <code>id</code>, which is just a synonym for <code>value</code>.
 * 
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version 1.0
 */
public class AvailableFormBean extends SelectItem {
	
	private static final long serialVersionUID = -3793938049046388576L;

	public AvailableFormBean() {
		super();
	}
	
	public AvailableFormBean(String id, String label) {
		super(id, label);
	}
	
	public String getId() {
		return (String) getValue();
	}

	public void setId(String id) {
		setValue(id);
	}

}