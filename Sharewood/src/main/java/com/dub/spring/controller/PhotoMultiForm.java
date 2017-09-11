package com.dub.spring.controller;

import org.springframework.web.multipart.MultipartFile;

/** Command object */
public class PhotoMultiForm {
	
	private MultipartFile uploadedFile;
	private String title;
	private boolean shared;
	
	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public MultipartFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(MultipartFile uploadedFile) {
		this.uploadedFile = uploadedFile;
		System.out.println("setUploadedFile");
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	

}
