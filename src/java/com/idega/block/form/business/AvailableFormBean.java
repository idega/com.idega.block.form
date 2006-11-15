package com.idega.block.form.business;

/**
 * @author <a href="mailto:civilis@idega.com">Vytautas �ivilis</a>
 * @version 1.0
 */
public class AvailableFormBean {
	
	private String form_id;
	private String form_title;
	
	public String getId() {
		return form_id;
	}
	public void setId(String form_id) {
		this.form_id = form_id;
	}
	public String getFormTitle() {
		return form_title;
	}
	public void setFormTitle(String form_title) {
		this.form_title = form_title;
	}
	
	@Override
	public String toString() {
		
		return "id: "+form_id+" title: "+form_title;
	}
}