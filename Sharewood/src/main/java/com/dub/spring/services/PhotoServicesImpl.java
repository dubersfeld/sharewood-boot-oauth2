package com.dub.spring.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dub.spring.controller.PhotoRestEndpoint;
import com.dub.spring.entities.Photo;
import com.dub.spring.exceptions.NoUploadFileException;
import com.dub.spring.exceptions.PhotoNotFoundException;
import com.dub.spring.exceptions.PhotoUploadException;
import com.dub.spring.exceptions.UnauthorizedException;
import com.dub.spring.repositories.PhotoRepository;

@Service
public class PhotoServicesImpl implements PhotoServices {
	
	private static final Logger logger 
			= LoggerFactory.getLogger(PhotoServicesImpl.class);

	@Value("${photos.baseDirPath}")	
	private String baseDirPath;

	@Autowired 
	private PhotoRepository photoRepository;


	@Override
	public List<Photo> getPhotosForCurrentUser(String username) {
		return photoRepository.findPhotosByUsername(username);
	}


	@Override
	public List<Photo> getAllPhotos() {
		return (List<Photo>)photoRepository.findAll();
	}

	
	@Override
	public void deletePhoto(long id) throws IOException {
		// delete row in database
		String filename = "photo" + id + ".jpg";
		
		try {
			photoRepository.delete(id);
			// delete actual photo file			
			Path path = FileSystems.getDefault().getPath(baseDirPath, filename);
	
			Files.deleteIfExists(path);
		} catch (EmptyResultDataAccessException e) {
			throw new PhotoNotFoundException();
		} 
		
	}


	@Override
	public void deletePhoto(long id, String username) throws IOException {
		Photo photo = photoRepository.findOne(id);
		if (photo == null) {
			throw new PhotoNotFoundException();
		}
		String user = photo.getUsername();
	
		if (!user.equals(username)) {
			logger.debug("throwing UnauthorizedException");
			throw new UnauthorizedException();
		}
		
		deletePhoto(id);	
	}

	@Override
	public long createPhoto(
				MultipartFile uploadedFileRef, String username, 
				String title, boolean shared) throws IOException {			
		InputStream is = null;     
		OutputStream os = null;
	
		// This buffer will store the data read from 'uploadedFileRef'
		byte[] buffer = new byte[1000];
		int bytesRead = -1;
		int totalBytes = 0;
	    
		String fileName = "photo" + "Tmp.jpg";
	
		String path = baseDirPath + fileName;
	
		if (uploadedFileRef.getSize() == 0) {
			logger.debug("throwing NoUploadFileException");
			throw new NoUploadFileException();
		}
	
		is = uploadedFileRef.getInputStream();
		os = new FileOutputStream(path);
	
		while ((bytesRead = is.read(buffer)) != -1) {
			os.write(buffer);
			totalBytes += bytesRead;
		}
		os.close();
		
		if (totalBytes != uploadedFileRef.getSize()) {
			logger.debug("throwing");
			throw new PhotoUploadException();
		} else {
			// now update database
			Photo photo = new Photo();
			photo.setTitle(title);
			photo.setUsername(username);
			photo.setShared(shared);
			long newId = photoRepository.save(photo).getId();
			
			// now change file name
			Path source = FileSystems.getDefault().getPath(baseDirPath, fileName);
			Path target = FileSystems.getDefault().getPath(baseDirPath, "photo" + newId + ".jpg");
			//Path source = 
			Files.move(source, target);
				
			return newId;
		}	
	}


	public String getBaseDirPath() {
		return baseDirPath;
	}

	public void setBaseDirPath(String baseDirPath) {
		this.baseDirPath = baseDirPath;
	}

	@Override
	public List<Photo> getSharedPhotos() {
		return photoRepository.findPhotosByShared(true);
	}


	@Override
	public Photo getPhoto(long id, String username) {
	
		Photo photo = photoRepository.findOne(id);// may be null	
	
		if (photo == null) {
			throw new PhotoNotFoundException();
		}
		
		try {
			if ( username.equals(photo.getUsername()) ) {
				return photo;
			} else {
				logger.debug("throwing UnauthorizedException");
				throw new UnauthorizedException();
			}
		} catch (PhotoNotFoundException e) {
		throw e;
		}	
	}


	@Override
	public InputStream loadPhoto(long id, String username) throws FileNotFoundException {
		Photo photo = photoRepository.findOne(id);
		if (photo == null) {
			throw new PhotoNotFoundException();
		}

		String path = baseDirPath + "photo" + photo.getId() + ".jpg";
		
		if (photo.isShared() || username.equals(photo.getUsername())) {			
			return new FileInputStream(path);// throws FileNotFoundException
		} else {
			throw new UnauthorizedException();// caught by RestEndpoint
		}	
	}


	@Override
	public InputStream loadPhoto(long id) throws FileNotFoundException {
	
		Photo photo = photoRepository.findOne(id);
	
		String path = baseDirPath + "photo" + photo.getId() + ".jpg";
				
		return new FileInputStream(path);		
	}


	@Override
	public void updatePhoto(Photo photo, String username) {
	
		if (!username.equals(photo.getUsername())) {
			throw new UnauthorizedException();
		}
		photoRepository.save(photo);
	}

}
