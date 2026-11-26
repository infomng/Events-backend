package com.events.modules.user.service;

import com.events.modules.user.dto.GetUserDto;
import com.events.modules.user.enumeration.RoleEnum;

import com.events.modules.auth.dto.RegisterCommandDto;
import com.events.modules.event.entity.Event;
import com.events.modules.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface IUserService {
    void createUser(RegisterCommandDto command);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findById(UUID userId);

    /**
     * Change le rôle d'un utilisateur (par exemple pour en faire un organisateur). Réservé aux admins.
     */
    GetUserDto changeRole(UUID userId, RoleEnum role);
}
