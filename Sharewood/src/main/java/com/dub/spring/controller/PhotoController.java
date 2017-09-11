package com.dub.spring.controller;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dub.spring.entities.Photo;
import com.dub.spring.exceptions.NoUploadFileException;
import com.dub.spring.exceptions.PhotoNotFoundException;
import com.dub.spring.exceptions.PhotoUploadException;
import com.dub.spring.exceptions.UnauthorizedException;
import com.dub.spring.services.PhotoServices;


/**
 * PhotoController is used for all HTTP requests
 * HTTP requests are protected by Spring Security
 * For REST requests see class PhotoRestEndpoint
 * @author Dominique Ubersfeld
 */
@Controller
@RequestMapping("photos")
public class PhotoController {
	
	private static final Logger logger 
		= LoggerFactory.getLogger(PhotoRestEndpoint.class);	

	
	@Autowired 
	private PhotoServices photoServices;
	
	@RequestMapping(
			value ="myPhotos", 
			method = RequestMethod.GET)
	public String getMyPhotos(ModelMap model) {
		if (this.getPrincipal() != null) {
					
			String username = this.getPrincipal().getUsername();
			
			List<Photo> photos = photoServices.getPhotosForCurrentUser(username);
		
			model.addAttribute("username", username);
			model.addAttribute("photos", photos);
			
			return "photos/myPhotos";	
		} else {
			return "photos/unauthorizedAccess";
		}
	}
	
	@RequestMapping(
			value ="sharedPhotos", 
			method = RequestMethod.GET)
	public String getSharedPhotos(ModelMap model) 
	{
		if (this.getPrincipal() != null) {	
			List<Photo> photos = photoServices.getSharedPhotos();				
			model.addAttribute("photos", photos);		
			return "photos/sharedPhotos";	
		} else {
			return "photos/unauthorizedAccess";
		}
	}
	
	
	
	@RequestMapping(
			value = "doGetPhoto/{photoId}", 
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
	)
	public ResponseEntity<byte[]> doGetPhoto(
									@PathVariable("photoId") String id
									) {
		InputStream photo = null;
		
		Authentication auth = SecurityContextHolder
								.getContext().getAuthentication();
	
		Collection<? extends GrantedAuthority> authorities 
								= auth.getAuthorities();
				
		List<String> authStrs = new ArrayList<>();
		for (GrantedAuthority authority : authorities) {		
			authStrs.add(authority.getAuthority()); 
		}
		
		try {
			if (authStrs.contains("ADMIN")) {
				photo = photoServices.loadPhoto(Long.parseLong(id));		
			} else {
				photo = photoServices.loadPhoto(Long.parseLong(id), auth.getName());				
			}
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = photo.read(buffer);
			while (len >= 0) {
				out.write(buffer, 0, len);
				len = photo.read(buffer);
			}
			HttpHeaders headers = new HttpHeaders();
			photo.close();
			headers.set("Content-Type", "image/jpeg");
			return new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.OK);

		} catch (IOException e) {
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(
			value = "updatePhoto", 
			method = RequestMethod.GET)
	public ModelAndView updatePhoto(ModelMap model) 
	{
		model.addAttribute("getPhoto", new PhotoIdForm());
		return new ModelAndView("photos/updatePhoto1", model);
	}
	
	@RequestMapping(
			value = "updatePhoto1", 
			method = RequestMethod.POST)
	public String updatePhoto1(
			@Valid @ModelAttribute("getPhoto") PhotoIdForm form,
			BindingResult result, ModelMap model) 
	{
		if (result.hasErrors()) {
			return "photos/updatePhoto1";
		}
		if (this.getPrincipal() != null) {
			String username = this.getPrincipal().getUsername();
			try { 
				Photo photo = photoServices.getPhoto(form.getId(), username);	
				model.addAttribute("photo", photo);
			
				return "photos/updatePhoto2";
			} catch (PhotoNotFoundException e) {
				return "photos/photoNotFound";
			} catch (UnauthorizedException e) {
				return "photos/unauthorizedAccess";
			} catch (Exception e) {
				e.printStackTrace();
				return "error";
			}// try			
		} else {
			return "photos/unauthorizedAccess";
		}
	}
	
	
	@RequestMapping(value = "updatePhoto2", method = RequestMethod.POST)
	public String updatePhoto2(
						@Valid @ModelAttribute("photo") PhotoUpdateForm form,
						BindingResult result, ModelMap model) {		
		if (result.hasErrors()) {
			return "photos/sharePhoto2";
		} else {
			
			try {
				Photo photo = new Photo();
				photo.setId(form.getId());
				photo.setUsername(form.getUsername());
				photo.setTitle(form.getTitle());
				photo.setShared(form.isShared());
								
				photoServices.updatePhoto(photo, this.getPrincipal().getUsername());			
				return "photos/updatePhotoSuccess";
			} catch (Exception e) {
				e.printStackTrace();
				return "photos/updatePhotoFailure";
			}
		}
	}
	
	@RequestMapping(
			value = "createPhotoMulti", 
			method = RequestMethod.GET)
	public ModelAndView createPhotoMulti(ModelMap model) 
	{
		model.addAttribute("photoMulti", new PhotoMultiForm());
		return new ModelAndView("photos/createPhotoMultipart", model);
	}
		
	@RequestMapping(
    		value = "createPhotoMulti",
    		method = RequestMethod.POST)      	 
	public String uploadPhoto(
            @Valid @ModelAttribute("photoMulti") PhotoMultiForm form, 
            BindingResult result, ModelMap model) 
	{	 	
		if (result.hasErrors()) {
			logger.error("errorz " + result.getFieldErrors().get(0));
			return "photos/createPhotoMultipart";
		}
	
		// Get name of uploaded file.
		MultipartFile uploadedFileRef = null;
		boolean shared = form.isShared();
		String title = form.getTitle();
		 
		uploadedFileRef = form.getUploadedFile();
		
		if (this.getPrincipal() != null) {
			String username = this.getPrincipal().getUsername();
			
			try {
				long photoId = photoServices.createPhoto(
												uploadedFileRef, 
												username, 
												title, 
												shared);		
				model.addAttribute("photoId", photoId);
				return "photos/createPhotoSuccess";
			} catch (NoUploadFileException e) {
				logger.debug("NoUploadFileException caught");
				model.addAttribute("cause", "No upload file");
				return "photos/createPhotoFailure";
			} catch (PhotoUploadException e) {
				logger.debug("PhotoUploadException caught");
				model.addAttribute("cause", "Photo upload error");
				return "photos/createPhotoFailure";
			} catch (Exception e) {
				logger.debug("Exception caught: " + e);
				model.addAttribute("cause", "error.unknown");
				return "photos/createPhotoFailure";
			}
		} else {
			return "photos/unauthorizedAccess";
		}
	}
	
	
	@RequestMapping(
			value = "deletePhoto", 
			method = RequestMethod.GET)
	public ModelAndView deletePhoto(ModelMap model) {
		model.addAttribute("getPhoto", new PhotoIdForm());
		return new ModelAndView("photos/deletePhoto", model);
	}

	@RequestMapping(
			value = "deletePhoto", 
			method = RequestMethod.POST)
	public String deletePhoto(
					@Valid @ModelAttribute("getPhoto") PhotoIdForm form,
					BindingResult result, 
					ModelMap model) {	
		if (result.hasErrors()) {
			logger.error("errorz " + result.getFieldErrors().get(0));
			return "photos/deletePhoto";
		}

		boolean admin = false;
	
		if (this.getPrincipal() != null) {
			String username = this.getPrincipal().getUsername();
		
			Collection<? extends GrantedAuthority> auth = SecurityUtils.getAuth().getAuthorities();
	   	
			// check if ADMIN
			for (GrantedAuthority a : auth) {
				if (a.getAuthority().equals("ADMIN")) {
					admin = true;
				}
			}
	    	
			try {	
				if (admin) {
					photoServices.deletePhoto(
						form.getId());
				} else {
					photoServices.deletePhoto(
						form.getId(), 
						username);
				}
				return "photos/deletePhotoSuccess";
			} catch (UnauthorizedException e) {
				return "photos/unauthorizedAccess";
			} catch (PhotoNotFoundException e) {
				return "photos/photoNotFound";
			} catch (Exception e) {
				logger.debug("Exception caught by PhotoController: " + e);
				model.addAttribute("cause", "error.unknown");
				return "photos/deletePhotoFailure";
			}
		} else {
		return "photos/unauthorizedAccess";
		}
	}
	
	
	private UserDetails getPrincipal() {
		Authentication authentication = SecurityContextHolder
				.getContext()
				.getAuthentication();

		if (authentication.getPrincipal() instanceof UserDetails) {
			return (UserDetails)authentication.getPrincipal();
		} else {
			return null;
		}
	}
}
