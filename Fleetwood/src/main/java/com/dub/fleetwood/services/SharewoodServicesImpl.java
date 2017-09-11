package com.dub.fleetwood.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import com.dub.fleetwood.exceptions.NoUploadFileException;
import com.dub.fleetwood.exceptions.SharewoodException;
import com.dub.fleetwood.photos.Photo;
import com.dub.fleetwood.photos.PhotoWebServiceList;

@Service
public class SharewoodServicesImpl implements SharewoodServices {

	private static final Logger logger 
				= LoggerFactory.getLogger(SharewoodServices.class);	
	
	private String sharewoodPhotoListURL;
	private String sharewoodPhotoURLPattern;
	private String sharewoodPhotoURL;
	
	@Value("${tempDir}")
	private String tempDir; 

	@Value("${sharewoodPhotoBaseURL}")
	private String sharewoodPhotoBaseURL; 
	
    @Autowired
    @Qualifier("sharewoodRestTemplate")
    private OAuth2RestTemplate sharewoodRestTemplate;
    

	@Override
	public List<Photo> getSharewoodPhotosMy() throws SharewoodException {				
		sharewoodPhotoListURL = sharewoodPhotoBaseURL + "/photosMy";
		
		HttpHeaders headers = new HttpHeaders();
		List<MediaType> amt = new ArrayList<>();
		amt.add(MediaType.APPLICATION_JSON);
		headers.setAccept(amt);
		
		HttpEntity<PhotoWebServiceList> request = new HttpEntity<>(null, headers);
		
		ResponseEntity<PhotoWebServiceList> response 
						= sharewoodRestTemplate.exchange(
								sharewoodPhotoListURL, HttpMethod.GET, request, PhotoWebServiceList.class);

		try {
			if (response.getStatusCode() == HttpStatus.OK) {
				return response.getBody().getPhotos();
			} else {
				throw new SharewoodException("Error");
			}
		} catch (HttpStatusCodeException e) {
			logger.debug("exception caught " + e);
			String message;	
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				message = SharewoodException.PHOTO_NOT_FOUND;
			} else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED 
								|| e.getStatusCode() == HttpStatus.FORBIDDEN) {
				message = SharewoodException.UNAUTHORIZED;
			} else {
				message = SharewoodException.UNKNOWN_SERVER_ERROR;
			}
			throw new SharewoodException(message);		
		}
	}
	

	@Override
	public Photo getSharewoodPhoto(long id) throws SharewoodException {
		
	  	sharewoodPhotoURL = sharewoodPhotoBaseURL + "/" + id;
	  	
		HttpHeaders headers = new HttpHeaders();
		
		List<MediaType> list = new ArrayList<>();
		list.add(MediaType.APPLICATION_JSON);
		headers.setAccept(list);
		
		HttpEntity<Photo> request = new HttpEntity<>(null, headers);
		
		try {
			ResponseEntity<Photo> response = sharewoodRestTemplate.exchange(
                sharewoodPhotoURL, HttpMethod.GET, request, Photo.class);
	
			if (response.getStatusCode() == HttpStatus.OK) {
				return response.getBody();
			} else {
				throw new SharewoodException("Error");
			}
		} catch (HttpStatusCodeException e) {
			logger.debug("exception caught " + e);
    		String message;	
    		if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
    			message = SharewoodException.PHOTO_NOT_FOUND;
    		} else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED 
    								|| e.getStatusCode() == HttpStatus.FORBIDDEN) {
    			message = SharewoodException.UNAUTHORIZED;
    		} else {
    			message = SharewoodException.UNKNOWN_SERVER_ERROR;
    		}
    		throw new SharewoodException(message);		
		}
	}

	
	@Override
	public List<Photo> getSharewoodSharedPhotos() throws SharewoodException {	
		sharewoodPhotoListURL = sharewoodPhotoBaseURL + "/sharedPhotos";
		
		sharewoodRestTemplate.getAccessToken();
		
		HttpHeaders headers = new HttpHeaders();
		List<MediaType> amt = new ArrayList<>();
		amt.add(MediaType.APPLICATION_JSON);
		headers.setAccept(amt);
		
		HttpEntity<PhotoWebServiceList> request = new HttpEntity<>(null, headers);
		
		ResponseEntity<PhotoWebServiceList> response 
								= sharewoodRestTemplate.exchange(
                sharewoodPhotoListURL, HttpMethod.GET, request, PhotoWebServiceList.class);
		
		try {
			if (response.getStatusCode() == HttpStatus.OK) {			
				return response.getBody().getPhotos();
			} else {
				throw new SharewoodException("Error");
			}
		} catch (HttpStatusCodeException e) {
			logger.debug("exception caught " + e);
			String message;	
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				message = SharewoodException.PHOTO_NOT_FOUND;
			} else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED 
								|| e.getStatusCode() == HttpStatus.FORBIDDEN) {
				message = SharewoodException.UNAUTHORIZED;
			} else {
				message = SharewoodException.UNKNOWN_SERVER_ERROR;
			}
			throw new SharewoodException(message);	
		}
	}
	
		
	@Override
	public void deletePhoto(long id) throws SharewoodException {
		
		String url = sharewoodPhotoBaseURL + "/deletePhoto/" + id; 
				
    	try {
    		sharewoodRestTemplate.delete(url);	
    	} catch (HttpStatusCodeException e) {
    		logger.debug("Services exception caught " + e);
    		String message;	
    		if (e.getMessage().contains("404")) {
    			message = SharewoodException.PHOTO_NOT_FOUND;
    		} else if (e.getMessage().contains("401") 
    								|| e.getMessage().contains("403")) {
    			message = SharewoodException.UNAUTHORIZED;
    		} else {
    			message = SharewoodException.UNKNOWN_SERVER_ERROR;
    		}
    		throw new SharewoodException(message);	
    	}
	}

	
	@Override
	public long createPhoto(MultipartFile uploadedFileRef, 
										String title, boolean shared)
			throws SharewoodException, IOException {
			
		File tempFile = null;	    
		String pattern = sharewoodPhotoBaseURL + "/(\\d+)";	   
		Pattern r = Pattern.compile(pattern);
	
		String photoFilePath = 
				uploadedFileRef.getOriginalFilename();
		
		// first store uploaded file to temporary location 
        tempFile = uploadPhoto(uploadedFileRef);
		       	   	
        //String path = tempFile.toPath().toString();
        
        MultiValueMap<String, Object> map 
								= new LinkedMultiValueMap<>();
        map.add("uploadedFile", new FileSystemResource(tempFile));
        map.add("title", title);
        map.add("shared", shared);
        
        List<MediaType> amt = new ArrayList<>();
        amt.add(MediaType.APPLICATION_JSON);
        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(amt);// JSON expected from resource server
			
    	HttpEntity<MultiValueMap<String, Object>> requestEntity = 
				new HttpEntity<MultiValueMap<String, Object>>(map, headers);
    	
    	String url = sharewoodPhotoBaseURL + "/createPhoto";
    			
    	ResponseEntity<String> response = sharewoodRestTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);
    				
    	Path enclume = Paths.get(tempDir, photoFilePath);
    	// Clean the tempDir directory
    	Files.deleteIfExists(enclume);
    	
    	// parse response
    	Matcher m = r.matcher(response.getHeaders().toString());			
    	
    	if (m.find()) {
    		logger.debug("returning number " + m.group(1)); 
    		return Long.parseLong(m.group(1));		
    	} else {
     		logger.debug("no match");
   			return 0;
    	} 
	}

	
	@Override
	public void updatePhoto(Photo photo) throws SharewoodException, IOException {
	  	
		String url = sharewoodPhotoBaseURL + "/updatePhoto";
	  	
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		List<MediaType> list = new ArrayList<>();
		list.add(MediaType.APPLICATION_JSON);
		headers.setAccept(list);
		
		HttpEntity<Photo> request = new HttpEntity<>(photo, headers);
			
		try {
			ResponseEntity<String> response 
									= sharewoodRestTemplate.exchange(
                url, HttpMethod.PUT, request, String.class);
	  
		} catch (HttpStatusCodeException e) {
			logger.debug("exception caught " + e);
			String message;	
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				message = SharewoodException.PHOTO_NOT_FOUND;
			} else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED 
								|| e.getStatusCode() == HttpStatus.FORBIDDEN) {
				message = SharewoodException.UNAUTHORIZED;
			} else {
				message = SharewoodException.UNKNOWN_SERVER_ERROR;
			}
			throw new SharewoodException(message);	
		}
	}
	
	
	
	

	@Override
	public InputStream loadSharewoodPhoto(String id) throws SharewoodException {
		sharewoodPhotoURLPattern = sharewoodPhotoBaseURL + "/doGetPhoto/" + id;
		
		return new ByteArrayInputStream(sharewoodRestTemplate.getForObject(
				URI.create(String.format(sharewoodPhotoURLPattern, id)), byte[].class));
	}
	
	
	private File uploadPhoto(MultipartFile uploadedFileRef) 
			throws NoUploadFileException, IOException {
		/**
		 * Helper method for photo upload using MultipartFile 
		 */
		
		if (uploadedFileRef.isEmpty()) {
			logger.debug("throwing NoUploadFileException");
			throw new NoUploadFileException();
		}
	
		String fileName = uploadedFileRef.getOriginalFilename(); 
    	String path = tempDir + fileName; 
    	File outputFile = new File(path);
    	 	
    	InputStream is = null;     
    	OutputStream os = null;
         
    	byte[] buffer = new byte[1000];
    	int bytesRead = -1;
    	int totalBytes = 0;
    		
    	is = uploadedFileRef.getInputStream();
        os = new FileOutputStream(outputFile);
        	
        while ((bytesRead = is.read(buffer)) != -1) {    
        	os.write(buffer);        
            totalBytes += bytesRead;
        }        	
        logger.debug("totalBytes " + totalBytes);        	
        		
        is.close();		
        os.close();
        		
        return outputFile;
	}
}
