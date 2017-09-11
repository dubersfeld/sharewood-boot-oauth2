package com.dub.fleetwood.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dub.fleetwood.exceptions.NoUploadFileException;
import com.dub.fleetwood.exceptions.PhotoUploadException;
import com.dub.fleetwood.exceptions.SharewoodException;
import com.dub.fleetwood.exceptions.UserDeniedAuthorizationException;
import com.dub.fleetwood.photos.Photo;
import com.dub.fleetwood.services.SharewoodServices;



@Controller
public class SharewoodController {

	private static final Logger logger = LoggerFactory.getLogger(SharewoodController.class);	
	
	
	@Autowired
	@Qualifier("sharewoodRestTemplate")
	private OAuth2RestTemplate sharewoodRestTemplate;

	
	@Autowired
	private SharewoodServices sharewoodServices;
	
	@RequestMapping("/sharewood/photosMy")
	public String photosMy(Model model) throws Exception {	
		try {
			
			List<Photo> list 
						= sharewoodServices.getSharewoodPhotosMy();
					
			model.addAttribute("photos", list);
		
			return "sharewoodList";
		} catch (UserDeniedAuthorizationException e) {
			logger.debug(e.getMessage());
			
			return "accessDenied";
		}
	}
	
	
 	@RequestMapping("/sharewood/photosList/{id}")
	public  ResponseEntity<byte[]> photo(@PathVariable String id) throws Exception {
				
		try {
			InputStream photo = sharewoodServices.loadSharewoodPhoto(id);
		
			if (photo == null) {			
				return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
			} else {
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
			}
		} catch (HttpClientErrorException e) {
			logger.debug("throwing 404");
			HttpHeaders headers = new HttpHeaders();
			return new ResponseEntity<byte[]>(null, headers, HttpStatus.NOT_FOUND);
		}
	}
 	
	@RequestMapping("/sharewood/sharedPhotos")
	public String sharedPhotos(Model model) throws Exception {	
		try {
			
			List<Photo> list = sharewoodServices.getSharewoodSharedPhotos();
			
			model.addAttribute("photos", list);
			
			return "sharewoodShared";
		} catch (UserDeniedAuthorizationException e) {
			logger.debug(e.getMessage());
			
			return "accessDenied";
		}
	}
	
	@RequestMapping(
			value = "/sharewood/createPhotoMulti", 
			method = RequestMethod.GET)
	public ModelAndView createPhotoMulti(ModelMap model, HttpServletRequest request) {
		
		// Check if present token scope is sufficient
		OAuth2AccessToken token = sharewoodRestTemplate.getAccessToken();
		
		Set<String> scopes = token.getScope();
		
		logger.debug("scope " + scopes);
		
		if (!(scopes.contains("READ") && scopes.contains("WRITE"))) {
			logger.debug("Deleting present token from OAuth2ClientContext...");
			
			// delete useless token
			sharewoodRestTemplate.getOAuth2ClientContext().setAccessToken(null);
			
			model.addAttribute("required", "READ, WRITE");
			return new ModelAndView("insufficientScope", model);
		}
			
		// token has already a sufficient scope
		model.addAttribute("photoMulti", new PhotoMultiForm());
		return new ModelAndView("createPhotoMultipart", model);
	}
	
	@RequestMapping(
    		value = "/sharewood/createPhotoMulti",
    		method = RequestMethod.POST)      	 
	public String uploadPhoto(
            @Valid @ModelAttribute("photoMulti") PhotoMultiForm form, 
            BindingResult result, ModelMap model) {	 
			
		if (result.hasErrors()) {
			logger.error("errorz " + result.getFieldErrors().get(0));
			return "createPhotoMultipart";
		}
		// Get name of uploaded file.
		MultipartFile uploadedFileRef = null;
		boolean shared = form.isShared();
		String title = form.getTitle();
		 
		uploadedFileRef = form.getUploadedFile();
						
		try {
			long photoId = sharewoodServices.createPhoto(uploadedFileRef, title, shared);
			
			model.addAttribute("photoId", photoId);
			return "createPhotoSuccess";
		} catch (InsufficientScopeException e) {
			logger.debug("InsufficientScopeException caught");
			return "insufficientScope";
		} catch (NoUploadFileException e) {
			logger.debug("NoUploadFileException caught");
			model.addAttribute("cause", "No upload file");
			return "createPhotoFailure";
		} catch (PhotoUploadException e) {
			logger.debug("PhotoUploadException caught");
			model.addAttribute("cause", "Photo upload error");
			return "createPhotoFailure";
		} catch (IOException e) {
			logger.debug("IOException caught");
			model.addAttribute("cause", "Photo upload error");
			return "createPhotoFailure";
		} catch (Exception e) {
			logger.debug("Unknown Exception caught " + e);
			model.addAttribute("cause", "Unknown error");
			return "createPhotoFailure";
		}
	
	}
 	
	@RequestMapping(
			value = "/sharewood/updatePhoto", 
			method = RequestMethod.GET)
	public ModelAndView updatePhoto(ModelMap model) {
		
		// Check if present token scope is sufficient
		OAuth2AccessToken token = sharewoodRestTemplate.getAccessToken();
				
		Set<String> scopes = token.getScope();
				
		logger.debug("scope " + scopes);
				
		if (!(scopes.contains("READ") && scopes.contains("WRITE"))) {
			logger.debug("Deleting present token from OAuth2ClientContext...");
			
			// delete useless token
			sharewoodRestTemplate.getOAuth2ClientContext().setAccessToken(null);
			
			model.addAttribute("required", "READ, WRITE");
			return new ModelAndView("insufficientScope", model);
		}
		
		model.addAttribute("getPhoto", new PhotoIdForm());
		return new ModelAndView("updatePhoto1", model);
	}
	
	@RequestMapping(
			value = "/sharewood/updatePhoto1", 
			method = RequestMethod.POST)
	public String updatePhoto1(
			@Valid @ModelAttribute("getPhoto") PhotoIdForm form,
			BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			return "updatePhoto1";
		}
	
		try { 
					
			Photo photo = sharewoodServices.getSharewoodPhoto(form.getId());				
				model.addAttribute("photo", photo);
				return "updatePhoto2";
		} catch (InsufficientScopeException e) {
			logger.debug("InsufficientScopeException caught");
			return "insufficientScope";
		} catch (SharewoodException e) {
			logger.debug("exception: " + e.getMessage());
			if (e.getMessage().equals(SharewoodException.UNAUTHORIZED)) {
				return "accessDenied";
			} else if (e.getMessage().equals(SharewoodException.PHOTO_NOT_FOUND)) {
				return "photoNotFound";
			} else {
				model.addAttribute("cause", "Unknow error");
				return "updatePhotoFailure";
			}
		} catch (Exception e) {
				model.addAttribute("cause", "Unknow error");
				return "updatePhotoFailure";
		}
	}
	

	@RequestMapping(
			value = "/sharewood/updatePhoto2", 
			method = RequestMethod.POST)
	public String updatePhoto2(@Valid @ModelAttribute("photo") PhotoUpdateForm form,
			BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			return "photos/updatePhoto2";
		}
		try {
			Photo photo = new Photo();
			photo.setId(form.getId());
			photo.setUsername(form.getUsername());
			photo.setTitle(form.getTitle());
			photo.setShared(form.isShared());
			
			sharewoodServices.updatePhoto(photo);			
			return "updatePhotoSuccess";
		} catch (Exception e) {
			model.addAttribute("cause", "Unknow error");
			return "updatePhotoFailure";
		}
	}
	
	
	@RequestMapping(
			value = "/sharewood/deletePhoto", 
			method = RequestMethod.GET)
	public ModelAndView deletePhoto(ModelMap model) {
			
		// Check if present token scope is sufficient
		OAuth2AccessToken token = sharewoodRestTemplate.getAccessToken();
						
		Set<String> scopes = token.getScope();
						
		if (!(scopes.contains("READ") && scopes.contains("DELETE"))) {
			logger.debug("Deleting present token from OAuth2ClientContext...");
			
			// delete useless token
			sharewoodRestTemplate.getOAuth2ClientContext().setAccessToken(null);
			
			model.addAttribute("required", "READ, DELETE");
			return new ModelAndView("insufficientScope", model);
		}
		
		model.addAttribute("getPhoto", new PhotoIdForm());
		return new ModelAndView("deletePhoto", model);
	}
	
	@RequestMapping(
			value = "/sharewood/deletePhoto", 
			method = RequestMethod.POST)
	public String deletePhoto(
					@Valid @ModelAttribute("getPhoto") PhotoIdForm form,
					BindingResult result, 
					ModelMap model) {	
		if (result.hasErrors()) {
			logger.debug("errorz " + result.getFieldErrors().get(0));
			return "deletePhoto";
		}
		
		try {
			sharewoodServices.deletePhoto(form.getId());
			return "deletePhotoSuccess";
		} catch (InsufficientScopeException e) {
			logger.debug("InsufficientScopeException caught");
			return "insufficientScope";
		} catch (SharewoodException e) {
			if (e.getMessage().equals(SharewoodException.UNAUTHORIZED)) {
				return "accessDenied";
			} else if (e.getMessage().equals(SharewoodException.PHOTO_NOT_FOUND)) {
				return "photoNotFound";
			} else {
				model.addAttribute("cause", "Unknow error");
				return "deletePhotoFailure";
			}
		} catch (Exception e) {
			logger.debug("Unknown Exception caught " + e.getMessage());
			model.addAttribute("cause", "Unknown error");
			return "deletePhotoFailure";
		}	
	}
}
