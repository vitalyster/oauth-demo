package com.example.securitydemo;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class WebConfig {
    @Configuration
    public static class ApiConfig extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                    .authorizeRequests()
                    .anyRequest().authenticated();
        }
    }
    @Configuration
    public static class WwwConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/").permitAll()
                    .antMatchers("/me").authenticated()
                    .and().formLogin()
                    .and().logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .invalidateHttpSession(true);
        }
    }
}
