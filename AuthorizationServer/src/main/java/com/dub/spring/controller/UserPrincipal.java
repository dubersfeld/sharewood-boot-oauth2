package com.dub.spring.controller;


import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dub.spring.entities.MyUser;
import com.dub.spring.entities.UserAuthority;


public class UserPrincipal implements UserDetails, CredentialsContainer, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String username;
	private byte[] hashedPassword;
	private Set<UserAuthority> authorities = new HashSet<>();
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	

	
	public byte[] getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(byte[] hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public void setAuthorities(Set<UserAuthority> authorities) {
		this.authorities = authorities;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	public UserPrincipal(MyUser user) {		
		this.username = user.getUsername();
		this.hashedPassword = user.getHashedPassword();
		this.enabled = user.isEnabled();
		this.authorities = user.getAuthorities();
		this.credentialsNonExpired = user.isCredentialsNonExpired();
		this.accountNonExpired = user.isAccountNonExpired();
		this.accountNonLocked = user.isAccountNonLocked();
	}

	@Override
    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return enabled;
	}

	@Override
	public boolean isAccountNonLocked() {
		return enabled;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
    public void eraseCredentials()
    {
        this.hashedPassword = null;
    }

	 
	@Override
	public String getPassword() {    
		return this.getHashedPassword() == null ? null :
			new String(this.getHashedPassword(), StandardCharsets.UTF_8);    
	}
	
	 @Override   
	 public int hashCode() {
		 return this.username.hashCode();  
	 }
	
	@Override
    public boolean equals(Object other)
    {
        return other instanceof UserPrincipal 
        		&&
            ((UserPrincipal)other).username == this.username
                &&
            ((UserPrincipal)other).getPassword() == this.getPassword();
    }

    @Override
    protected UserPrincipal clone()
    {
        try {
            return (UserPrincipal)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // not possible
        }
    }


}
