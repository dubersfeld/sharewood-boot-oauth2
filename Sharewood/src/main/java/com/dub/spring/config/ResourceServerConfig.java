package com.dub.spring.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;


/**
* ResourceServerConfiguration used by Spring OAuth2 
* */

@Configuration
@EnableResourceServer
public class ResourceServerConfig
								extends ResourceServerConfigurerAdapter {

	@Value("${security.oauth2.resource.id}")
	private String resourceId;
	
	
	@Autowired// provided 
	private DataSource dataSource;


	@Override
	public void configure(HttpSecurity http) 
								throws Exception {		
		
	    http.httpBasic().disable();
	
		http.antMatcher("/api/**").authorizeRequests()	
		.antMatchers(HttpMethod.GET, "/api/testResource")
		.access("#oauth2.hasScope('READ')")
			
		.antMatchers(HttpMethod.GET, "/api/photos/**")
		.access("#oauth2.hasScope('READ') and hasAuthority('USER')")
		
		.antMatchers(HttpMethod.POST, "/api/photos/**")
		.access("#oauth2.hasScope('READ') and #oauth2.hasScope('WRITE') and hasAuthority('USER')")
		
		.antMatchers(HttpMethod.PUT, "/api/photos/**")
		.access("#oauth2.hasScope('READ') and #oauth2.hasScope('WRITE') and hasAuthority('USER')")
		
		.antMatchers(HttpMethod.DELETE, "/api/photos/**")
		.access("#oauth2.hasScope('READ') and #oauth2.hasScope('DELETE') and hasAuthority('USER')");
				
	}


	@Override
		public void configure(ResourceServerSecurityConfigurer resources) {
				
		resources.resourceId(resourceId);
		resources.accessDeniedHandler(new OAuth2AccessDeniedHandler());
		resources.expressionHandler(new OAuth2WebSecurityExpressionHandler());
		
		resources.tokenServices(tokenServices());
	}
	
	@Bean
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();	
		tokenServices.setTokenStore(new JdbcTokenStore(dataSource));
			
		return tokenServices;
	}  
	
}