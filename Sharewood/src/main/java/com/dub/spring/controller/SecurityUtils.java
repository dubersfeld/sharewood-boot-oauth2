package com.dub.spring.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/** Helper utility class */
public class SecurityUtils {
	 public static String getPrincipal() {    
		 String userName = null;     
		 Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();        
		 if (principal instanceof UserDetails) {   
			 userName = ((UserDetails)principal).getUsername();	        
		 } else {   
			 userName = principal.toString();
		 }
		 return userName;
	 }
	 
	 public static Authentication getAuth() {   		
		 return SecurityContextHolder
				 				.getContext()
	    						.getAuthentication();   
	 }
}
