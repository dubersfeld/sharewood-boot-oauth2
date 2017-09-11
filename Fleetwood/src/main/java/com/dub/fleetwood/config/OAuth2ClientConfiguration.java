package com.dub.fleetwood.config;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;


@Configuration
@EnableOAuth2Client
public class OAuth2ClientConfiguration {

	@Value("${accessTokenUri}")
	private String accessTokenUri;
	
	@Value("${userAuthorizationUri}")
	private String userAuthorizationUri;
	
	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	
	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;

	
	/** Here we use authorization code grant */
	@Lazy
	@Bean
	public OAuth2ProtectedResourceDetails sharewood() {
		AuthorizationCodeResourceDetails details 
				= new AuthorizationCodeResourceDetails();
		details.setId("oAuth2ClientBean");
		details.setClientAuthenticationScheme(AuthenticationScheme.header);
		details.setClientId(clientId);
		details.setClientSecret(clientSecret);
		details.setAuthenticationScheme(AuthenticationScheme.header);
		details.setGrantType("authorization_code");
		details.setAccessTokenUri(accessTokenUri);
		details.setUserAuthorizationUri(userAuthorizationUri);
		details.setScope(Arrays.asList("READ", "WRITE", "DELETE"));
		return details;
	}

	
	@Lazy
	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
		public DefaultAccessTokenRequest accessTokenRequestProxy(
				@Value("#{request.parameterMap}") Map<String, String[]> parameters, 
				@Value("#{request.getAttribute('currentUri')}") String currentUri) {
		DefaultAccessTokenRequest requestProxy 
			= new DefaultAccessTokenRequest(parameters);
		requestProxy.setCurrentUri(currentUri);
		return requestProxy;
	}
	


	@Configuration
	protected static class OAuth2ClientContextConfiguration {

		@Resource
		@Qualifier("accessTokenRequest")
		private AccessTokenRequest accessTokenRequestProxy;

		@Resource
		@Qualifier("sharewood")
		private OAuth2ProtectedResourceDetails sharewood;

		@Lazy
		@Bean
		@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
		public OAuth2ClientContext clientContextProxy() { 		
			return new DefaultOAuth2ClientContext(accessTokenRequestProxy);
		}
		
		/** Begin with default implementation */
		@Bean
		public OAuth2RestTemplate sharewoodRestTemplate() {

			OAuth2RestTemplate sharewoodRestTemplate 
								= new OAuth2RestTemplate(sharewood, clientContextProxy());
			
			return sharewoodRestTemplate;
		}
	}
}