package com.dub.fleetwood.controller;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PhotoIdForm {

	@Min(value  = 1, message = "{validate.min.photoId}")
	@NotNull(message = "{validate.required.photoId}")
	Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

}
