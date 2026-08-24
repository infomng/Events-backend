package com.events.modules.auth.dto;

import com.events.modules.user.enumeration.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import net.minidev.json.annotate.JsonIgnore;

@Builder
public record RegisterCommandDto(
                                 @NotBlank(message = "Veuillez saisir votre nom complet")
                                 String fullName,

                                 @Email
                                 @NotBlank(message = "Veuillez saisir une adresse email valide")
                                 String email,

                                 @Pattern(
                                         regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&()\\[\\]{}^~#_+=|:;<>,./\\\\-]).{12,}$",
                                         message = "Le mot de passe doit contenir au moins 12 caractères, une majuscule, une minuscule, un chiffre et un symbole."
                                 )
                                 @NotBlank
                                 String password,

                                 @Schema(hidden = true)
                                 @JsonIgnore
                                 RoleEnum role,

                                 @Schema(hidden = true)
                                 @JsonIgnore
                                 String verificationToken) {
}
