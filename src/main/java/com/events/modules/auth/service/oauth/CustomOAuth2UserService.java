package com.events.modules.auth.service.oauth;

import com.events.common.utils.contants.Constants;
import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.user.enumeration.RoleEnum;
import com.events.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final IUserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get(Constants.EMAIL);
        String name = (String) attributes.get(Constants.NAME);

        if(!userService.existsByEmail(email)) {

            RegisterCommandDto command = RegisterCommandDto.builder()
                    .fullName(name)
                    .email(email)
                    .password(Constants.EMPTY_STRING)
                    .role(RoleEnum.USER)
                    .build();

            userService.createUser(command);
        }

        return oAuth2User;
    }
}
