package com.dub.fleetwood.exceptions;

public class SharewoodException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String UNAUTHORIZED = "Unauthorized request";
	public static final String PHOTO_NOT_FOUND = "Photo not found";
	public static final String UNKNOWN_SERVER_ERROR = "Unknown server error";
	
	public SharewoodException(String message) {
		super(message);
	}

}
