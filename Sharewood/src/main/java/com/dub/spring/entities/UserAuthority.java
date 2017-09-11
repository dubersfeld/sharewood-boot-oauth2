package com.dub.spring.entities;

import javax.persistence.Embeddable;

import org.springframework.security.core.GrantedAuthority;

@Embeddable
public class UserAuthority implements GrantedAuthority
{
    /**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	
	private String authority;

    public UserAuthority() { }

    public UserAuthority(String authority)
    {
        this.authority = authority;
    }

    @Override
    public String getAuthority()
    {
        return this.authority;
    }

    public void setAuthority(String authority)
    {
        this.authority = authority;
    }
}
