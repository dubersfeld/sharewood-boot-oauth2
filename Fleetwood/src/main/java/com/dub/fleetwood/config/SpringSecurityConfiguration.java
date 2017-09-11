package com.dub.fleetwood.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import org.springframework.context.annotation.AdviceMode;

@Configuration
@EnableGlobalMethodSecurity(
        prePostEnabled = true, order = 0, mode = AdviceMode.PROXY,
        proxyTargetClass = true
)
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {
		  
	@Bean
	public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
	    	return new DefaultWebSecurityExpressionHandler();
	}
	

	@Lazy
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception
	{    
		return super.authenticationManagerBean();
	}
	

    @Bean
    protected SessionRegistry sessionRegistryImpl() {
        return new SessionRegistryImpl();
    }

    
    @Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
			.withUser("Marissa").password("wombat").roles("USER")
			.and()
			.withUser("Steve").password("apple").roles("USER")
			.and()
			.withUser("Bill").password("orange").roles("USER");
	}

    
    @Override
    protected void configure(HttpSecurity security) 
    		throws Exception {
        security
                .authorizeRequests()                                                        	
                    .antMatchers("/login/**").permitAll()
                    .antMatchers("/login").permitAll()
                    .antMatchers("/logout").permitAll()    
                    .antMatchers("/**").hasAuthority("ROLE_USER")                                                
                    .and().formLogin()
                    .loginPage("/login").failureUrl("/login?loginFailed")
                    .defaultSuccessUrl("/index")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
                .and().logout()
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true).deleteCookies("JSESSIONID")
                    .permitAll()
                .and().sessionManagement()
                    .sessionFixation().changeSessionId()
                    .maximumSessions(1).maxSessionsPreventsLogin(false)
                    .sessionRegistry(this.sessionRegistryImpl())
                .and().and().csrf().disable();
        
    }            
}

