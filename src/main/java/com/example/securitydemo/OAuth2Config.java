/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.securitydemo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

/**
 * @author Greg Turnquist
 * @author Dave Syer
 * @since 1.3.0
 */
@Configuration
public class OAuth2Config
        extends AuthorizationServerConfigurerAdapter {

    private static final Log logger = LogFactory
            .getLog(OAuth2Config.class);

    private final BaseClientDetails details;

    private final AuthenticationManager authenticationManager;

    private final TokenStore tokenStore;

    private final AccessTokenConverter tokenConverter;

    public OAuth2Config(BaseClientDetails details,
                                                  AuthenticationConfiguration authenticationConfiguration,
                                                  ObjectProvider<TokenStore> tokenStore,
                                                  ObjectProvider<AccessTokenConverter> tokenConverter) throws Exception {
        this.details = details;
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.tokenStore = tokenStore.getIfAvailable();
        this.tokenConverter = tokenConverter.getIfAvailable();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        ClientDetailsServiceBuilder<InMemoryClientDetailsServiceBuilder>.ClientBuilder builder = clients
                .inMemory().withClient(this.details.getClientId());
        builder.secret(this.details.getClientSecret())
                .resourceIds(this.details.getResourceIds().toArray(new String[0]))
                .authorizedGrantTypes(
                        this.details.getAuthorizedGrantTypes().toArray(new String[0]))
                .authorities(
                        AuthorityUtils.authorityListToSet(this.details.getAuthorities())
                                .toArray(new String[0]))
                .scopes(this.details.getScope().toArray(new String[0]));

        if (this.details.getAutoApproveScopes() != null) {
            builder.autoApprove(
                    this.details.getAutoApproveScopes().toArray(new String[0]));
        }
        if (this.details.getAccessTokenValiditySeconds() != null) {
            builder.accessTokenValiditySeconds(
                    this.details.getAccessTokenValiditySeconds());
        }
        if (this.details.getRefreshTokenValiditySeconds() != null) {
            builder.refreshTokenValiditySeconds(
                    this.details.getRefreshTokenValiditySeconds());
        }
        if (this.details.getRegisteredRedirectUri() != null) {
            builder.redirectUris(
                    this.details.getRegisteredRedirectUri().toArray(new String[0]));
        }
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        if (this.tokenConverter != null) {
            endpoints.accessTokenConverter(this.tokenConverter);
        }
        if (this.tokenStore != null) {
            endpoints.tokenStore(this.tokenStore);
        }
        if (this.details.getAuthorizedGrantTypes().contains("password")) {
            endpoints.authenticationManager(this.authenticationManager);
        }
        endpoints.pathMapping("/oauth/token", "/api/oauth/token");
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Configuration
    protected static class ClientDetailsLogger {

        private final OAuth2ClientProperties credentials;

        protected ClientDetailsLogger(OAuth2ClientProperties credentials) {
            this.credentials = credentials;
        }

        @PostConstruct
        public void init() {
            String prefix = "security.oauth2.client";
            boolean defaultSecret = this.credentials.isDefaultSecret();
            logger.info(String.format(
                    "Initialized OAuth2 Client%n%n%s.client-id = %s%n"
                            + "%s.client-secret = %s%n%n",
                    prefix, this.credentials.getClientId(), prefix,
                    defaultSecret ? this.credentials.getClientSecret() : "****"));
        }

    }

    @Configuration
    @ConditionalOnMissingBean(BaseClientDetails.class)
    protected static class BaseClientDetailsConfiguration {

        private final OAuth2ClientProperties client;

        protected BaseClientDetailsConfiguration(OAuth2ClientProperties client) {
            this.client = client;
        }

        @Bean
        @ConfigurationProperties(prefix = "security.oauth2.client")
        public BaseClientDetails oauth2ClientDetails() {
            BaseClientDetails details = new BaseClientDetails();
            if (this.client.getClientId() == null) {
                this.client.setClientId(UUID.randomUUID().toString());
            }
            details.setClientId(this.client.getClientId());
            details.setClientSecret(this.client.getClientSecret());
            details.setAuthorizedGrantTypes(Arrays.asList("authorization_code",
                    "password", "client_credentials", "implicit", "refresh_token"));
            details.setAuthorities(
                    AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
            details.setRegisteredRedirectUri(Collections.<String>emptySet());
            return details;
        }

    }

}
