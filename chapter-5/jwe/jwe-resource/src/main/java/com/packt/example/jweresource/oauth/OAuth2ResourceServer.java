package com.packt.example.jweresource.oauth;

import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

import com.packt.example.jweresource.oauth.jwt.JweTokenSerializer;
import com.packt.example.jweresource.oauth.jwt.JweTokenStore;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServer extends ResourceServerConfigurerAdapter {

    @Autowired
    private ResourceServerProperties resource;

    @Autowired
    private DefaultAccessTokenConverter defaultAccessTokenConverter;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .anyRequest().authenticated().and()
            .requestMatchers().antMatchers("/api/**");
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore());
    }

    @Bean
    public JwtAccessTokenConverter jwtTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setAccessTokenConverter(defaultAccessTokenConverter);
        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        JweTokenSerializer tokenSerializer = new JweTokenSerializer();
        JwtTokenStore jwtTokenStore = new JwtTokenStore(jwtTokenConverter());
        return new JweTokenStore(getSignKey(), jwtTokenStore, jwtTokenConverter(), tokenSerializer);
    }

    private String getSignKey() {
        RestTemplate keyUriRestTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String username = this.resource.getClientId();
        String password = this.resource.getClientSecret();
        if (username != null && password != null) {
            byte[] token = Base64.getEncoder().encode((username + ":" + password).getBytes());
            headers.add("Authorization", "Basic " + new String(token));
        }
        HttpEntity<Void> request = new HttpEntity<Void>(headers);
        String url = this.resource.getJwt().getKeyUri();
        return (String) keyUriRestTemplate
                .exchange(url, HttpMethod.GET, request, Map.class).getBody()
                .get("value");
    }
}
