package com.tips.oauth2.social.facebook;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.tips.oauth2.social.SocialService;
import com.tips.oauth2.social.userconnection.UserConnection;

public class FacebookOAuth2ClientAuthenticationProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

	private ObjectMapper mapper = new ObjectMapper();
    private SocialService socialService;

    public FacebookOAuth2ClientAuthenticationProcessingFilter(SocialService socialService) {
        super("/login/facebook");
        this.socialService = socialService;
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

    	final OAuth2AccessToken accessToken = restTemplate.getAccessToken();
        final OAuth2Authentication auth = (OAuth2Authentication) authResult;
        final Object details = auth.getUserAuthentication().getDetails();

        final FacebookUserDetails userDetails = mapper.convertValue(details, FacebookUserDetails.class);
        userDetails.setAccessToken(accessToken);
        
        final UserConnection userConnection = UserConnection.valueOf(userDetails);
        final UsernamePasswordAuthenticationToken authenticationToken = socialService.doAuthentication(userConnection);
        
        super.successfulAuthentication(request, response, chain, authenticationToken);
    }
}