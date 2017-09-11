package com.dub.spring.controller;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class NameForm {

	@NotNull(message = "{validate.firstName.required}")
	@Size(min = 1, message = "{validate.firstName.required}")
	String firstName;

	@NotNull(message = "{validate.lastName.required}")
	@Size(min = 1, message = "{validate.lastName.required}")
	String lastName;


	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}

