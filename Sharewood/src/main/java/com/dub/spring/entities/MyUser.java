package com.dub.spring.entities;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * Entity, does not implement UserDetails 
*/

@Entity
@Table(name="user")
public class MyUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String username;
	private byte[] hashedPassword;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	
	private Set<UserAuthority> authorities = new HashSet<>();
	

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "user_Authority", joinColumns = {
			@JoinColumn(name = "userId", referencedColumnName = "userId")
	})
	public Set<UserAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<UserAuthority> authorities) {
		this.authorities = authorities;
	}

	@Column(name="hashedPassword")
	public byte[] getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(byte[] hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	@Column(name="accountNonExpired")
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	@Column(name="accountNonLocked")
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	@Column(name="credentialsNonExpired")
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}


		
	@Id
    @Column(name = "userId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId()
    {
        return this.id;
    }

    public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setId(long id)
    {
        this.id = id;
    }
	
    @Column(name = "username")
    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
	

	@Column(name = "enabled")
	public boolean isEnabled() {
		return enabled;
	}

}
