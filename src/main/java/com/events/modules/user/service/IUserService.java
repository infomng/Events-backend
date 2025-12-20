package com.events.modules.user.service;

import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.user.entity.User;

public interface IUserService {
    void createUser(RegisterCommandDto command);

    User findByEmail(String email);

    boolean existsByEmail(String email);
}
