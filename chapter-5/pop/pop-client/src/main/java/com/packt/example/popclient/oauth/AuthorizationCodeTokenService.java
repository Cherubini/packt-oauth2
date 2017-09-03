package com.packt.example.popclient.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthorizationCodeTokenService {
    //@formatter:off
    @Autowired
    private AuthorizationCodeConfiguration configuration;

    public String getAuthorizationEndpoint() {
        String endpoint = "http://localhost:8080/oauth/authorize";

        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("client_id", "clientapp");
        authParameters.put("response_type", "code");
        authParameters.put("redirect_uri",
                getEncodedUrl("http://localhost:9000/callback"));
        authParameters.put("scope", getEncodedUrl("read_profile"));

        return buildUrl(endpoint, authParameters);
    }

    private String buildUrl(String endpoint, Map<String, String> parameters) {
        List<String> paramList = new ArrayList<>(parameters.size());

        parameters.forEach((name, value) -> {
            paramList.add(name + "=" + value);
        });

        return endpoint + "?" + paramList.stream()
              .reduce((a, b) -> a + "&" + b).get();
    }

    private String getEncodedUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public OAuth2AccessToken getToken(String authorizationCode) {
        RestTemplate rest = new RestTemplate();
        String authBase64 = configuration.encodeCredentials("clientapp",
                "123456");

        RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<>(
            configuration.getBody(authorizationCode),
            configuration.getHeader(authBase64), HttpMethod.POST,
            URI.create("http://localhost:8080/oauth/token"));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = rest.postForObject("http://localhost:8080/oauth/token", requestEntity, Map.class);

        // converts result from token endpoint to an OAuth2AccessToken
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken((String) result.get("access_token"));
        token.setRefreshToken(new DefaultOAuth2RefreshToken((String) result.get("refresh_token")));
        token.setTokenType((String) result.get("token_type"));
        Calendar expiration = Calendar.getInstance();
        expiration.setTimeInMillis((Integer) result.get("expires_in"));
        token.setExpiration(expiration.getTime());
        token.setScope(new LinkedHashSet<String>(Arrays.asList(String.class.cast(result.get("scope")).split(" "))));

        return token;
    }

    // @formatter:on
}
