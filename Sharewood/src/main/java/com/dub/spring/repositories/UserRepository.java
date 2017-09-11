package com.dub.spring.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.dub.spring.entities.MyUser;

public interface UserRepository extends CrudRepository<MyUser, Long>
{
	MyUser getByUsername(String username);
	
	List<MyUser> findAll();
}