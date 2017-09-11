package com.dub.spring.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.transaction.Transactional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.dub.spring.controller.UserPrincipal;
import com.dub.spring.entities.MyUser;
import com.dub.spring.exceptions.DuplicateUserException;
import com.dub.spring.repositories.UserRepository;


@Service
public class UserServiceImpl implements UserService {
	
	 private static final SecureRandom RANDOM;

	    static
	    {
	        try
	        {
	            RANDOM = SecureRandom.getInstanceStrong();
	        }
	        catch(NoSuchAlgorithmException e)
	        {
	            throw new IllegalStateException(e);
	        }
	    }

	    private static final int HASHING_ROUNDS = 10;

	    @Autowired UserRepository userRepository;

	    @Override
	    @Transactional
	    public UserPrincipal loadUserByUsername(String username)
	    {
	    	MyUser user = userRepository.getByUsername(username);

	        UserPrincipal principal = new UserPrincipal(user);
	    	
	        // make sure the authorities and password are loaded
	        principal.getAuthorities().size();
	        principal.getPassword();
	        return principal;
	    }

	    
	    @Override
	    @Transactional
	    public void saveUser(MyUser user, String newPassword)
	    {
	    	System.out.println("DefaultUserService.saveUser");
	        if(newPassword != null && newPassword.length() > 0)
	        {
	            String salt = BCrypt.gensalt(HASHING_ROUNDS, RANDOM);
	            user.setHashedPassword(
	                    BCrypt.hashpw(newPassword, salt).getBytes()
	            );
	        }
	        try {
	        	this.userRepository.save(user);
	        } catch (Exception e) {
	        	String ex = ExceptionUtils.getRootCauseMessage(e);
				System.out.println("DAO " + ex.toString());
				if (ex.contains("user_unique")) {
					throw new DuplicateUserException();
				} else {
					throw e;
				}
	        	
	        }
	    }
	    

}