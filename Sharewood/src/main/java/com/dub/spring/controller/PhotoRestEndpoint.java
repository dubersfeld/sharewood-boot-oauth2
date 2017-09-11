package com.dub.spring.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dub.spring.entities.Photo;
import com.dub.spring.exceptions.NoUploadFileException;
import com.dub.spring.exceptions.PhotoNotFoundException;
import com.dub.spring.exceptions.UnauthorizedException;
import com.dub.spring.services.PhotoServices;

@RestController
@RequestMapping("/api/photos")
public class PhotoRestEndpoint {
	
	private static final Logger logger 
		= LoggerFactory.getLogger(PhotoRestEndpoint.class);	
	
	@Autowired
	private PhotoServices photoServices;

	@RequestMapping(
			value ="photosMy",
			produces = {MediaType.APPLICATION_JSON_VALUE},
			method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
	public PhotoWebServiceList getMyPhotos(HttpServletRequest request) {
		if (this.getPrincipal() != null) {		
			String username = this.getPrincipal().getUsername();
			List<Photo> photos = photoServices.getPhotosForCurrentUser(username);
			
			PhotoWebServiceList list = new PhotoWebServiceList();
			list.setPhotos(photos);
		
			return list;
		} else {
			return new PhotoWebServiceList();// not null
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
			value ="sharedPhotos",
			produces = {MediaType.APPLICATION_JSON_VALUE, 
							MediaType.APPLICATION_XML_VALUE},
			method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
	public PhotoWebServiceList getSharedPhotos(HttpServletRequest request) {	
		if (this.getPrincipal() != null) {		
			List<Photo> photos = photoServices.getSharedPhotos();
		
			PhotoWebServiceList list = new PhotoWebServiceList();
			list.setPhotos(photos);
		
			return list;
		} else {
			return new PhotoWebServiceList();
		}
	}
	
	
	@RequestMapping(
    		value = "createPhoto",
    		method = RequestMethod.POST,
    		produces = MediaType.APPLICATION_JSON_VALUE,
    		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )	
	public ResponseEntity<String> addPhoto(
	           @RequestParam("uploadedFile") MultipartFile uploadedFileRef, 
	           @RequestParam("title") String title,
	           @RequestParam("shared") boolean shared, HttpServletRequest request) {
		
		if (this.getPrincipal() != null) {
			String username = this.getPrincipal().getUsername();
			try {
				long photoId = photoServices.createPhoto(uploadedFileRef, username, title, shared);
				
				String uri = ServletUriComponentsBuilder.fromCurrentServletMapping()
	                .path("/api/photos/{id}").buildAndExpand(photoId).toString();
				
				HttpHeaders headers = new HttpHeaders();
				headers.add("Location", uri);
			
				return new ResponseEntity<String>(null, headers, HttpStatus.CREATED);
			} catch (NoUploadFileException e) {
				logger.debug("NoUploadFileException caught");
				return new ResponseEntity<String>(null, new HttpHeaders(), HttpStatus.EXPECTATION_FAILED);
			} catch (Exception e) {
				logger.debug("Unknown exception caught " + e);
				return new ResponseEntity<String>(null, new HttpHeaders(), HttpStatus.EXPECTATION_FAILED);			
			}
		} else {
			return new ResponseEntity<String>(null, new HttpHeaders(), HttpStatus.UNAUTHORIZED);			
		}
	}
	
	
	@RequestMapping(
    		value = "updatePhoto",
    		method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE,
    		consumes = MediaType.APPLICATION_JSON_VALUE
		)
	public ResponseEntity<String> grungePhoto( 
				@RequestBody Photo photo, HttpServletRequest request) {
		if (this.getPrincipal() != null) {
			String username = this.getPrincipal().getUsername();
		
			HttpHeaders headers = new HttpHeaders();
	    
			photoServices.updatePhoto(photo, username);
 
			return new ResponseEntity<String>(null, headers, HttpStatus.OK);
	
		} else {
			return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<Photo> getPhoto(
									@PathVariable("id") long id, 
									Principal principal, HttpServletRequest request) {	
		if (this.getPrincipal() != null) {
			String username = this.getPrincipal().getUsername();
		
			try {
				HttpHeaders headers = new HttpHeaders();
			
				Photo photo = photoServices.getPhoto(id, username);
				return new ResponseEntity<Photo>(photo, headers, HttpStatus.OK);
			
			} catch (UnauthorizedException e) {
				logger.debug("returning HttpStatus.UNAUTHORIZED");
				return new ResponseEntity<Photo>(HttpStatus.UNAUTHORIZED);
			} catch (PhotoNotFoundException e) {
				return new ResponseEntity<Photo>(HttpStatus.NOT_FOUND);
			} 
		} else {
			return new ResponseEntity<Photo>(HttpStatus.UNAUTHORIZED);
		} 
	}

	
	@RequestMapping(
			value = "deletePhoto/{photoId}", 
			method = RequestMethod.DELETE
			)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<?> deletePhoto(
					@PathVariable("photoId") String photoIdStr, HttpServletRequest request) {		
		if (this.getPrincipal() != null) {
			String username = this.getPrincipal().getUsername();
			try {	
				photoServices.deletePhoto(
						Long.parseLong(photoIdStr), 
						username);
				return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			} catch (UnauthorizedException e) {
				logger.debug("PhotoRestEndpoint returning HttpStatus.UNAUTHORIZED");
				return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
			} catch (PhotoNotFoundException e) {
				return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
			} catch (Exception e) {
				return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
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
