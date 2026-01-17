package com.events.modules.user.service;

import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.auth.exception.UserNotFoundException;
import com.events.modules.user.entity.User;
import com.events.modules.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Primary
@Transactional
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserRepository userRepository;

    @Override
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
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existByEmail(email);
    }

    @Override
    public User findById(java.util.UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
