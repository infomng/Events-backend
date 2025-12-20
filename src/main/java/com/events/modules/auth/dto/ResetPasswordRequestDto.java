package com.events.modules.auth.dto;

public record ResetPasswordRequestDto(String token,
                                      String newPassword) {

}

