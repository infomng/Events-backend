package com.events.modules.user.service;

import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.exception.UserNotFoundException;
import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.dto.mapper.IUserMapper;
import com.events.modules.user.entity.User;
import com.events.modules.user.enumeration.RoleEnum;
import com.events.modules.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Primary
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private final IUserMapper userMapper;

    @Override
    @CacheEvict(value = "user", key = "'existsByEmail:' + #command.email()")
    public void createUser(RegisterCommandDto command) {
       User user = User.builder()
           .fullName(command.fullName())
           .email(command.email())
           .password(command.password())
           .role(command.role())
           .verificationToken(command.verificationToken())
           .isVerified(false)
           .isEnabled(true)
           .isAccountNonExpired(true)
           .isAccountNonLocked(true)
           .isCredentialsNonExpired(true)
           .build();
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user" , key = "'findByEmail:' + #email")
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Cacheable(value = "user" , key = "'existsByEmail:' + #email")
    public boolean existsByEmail(String email) {
        log.debug("Checking if user with email: {} exists", email);
        return userRepository.existByEmail(email);
    }

    @Override
    @Cacheable(value = "user" , key = "'findById:' + #userId")
    public User findById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    // Le rôle est mis en cache avec l'utilisateur (par id et par email) : on vide le cache "user"
    @CacheEvict(value = "user", allEntries = true)
    public GetUserDto changeRole(UUID userId, RoleEnum role) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
        log.info("Role of user {} changed to {}", userId, role);
        return userMapper.toGetUserDto(user);
    }
}
