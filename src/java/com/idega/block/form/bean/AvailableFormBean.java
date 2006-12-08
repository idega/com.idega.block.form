package com.idega.block.form.bean;

import javax.faces.model.SelectItem;

/**
 * Formadmin needs property named id
 * @author <a href="mailto:gediminas@idega.com">Gediminas Paulauskas</a>
 * @version 1.0
 */
public class AvailableFormBean extends SelectItem {
	
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