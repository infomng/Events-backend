package com.events.modules.ticket.dto;

import com.events.modules.ticket.enumeration.CheckInResultEnum;

import java.time.LocalDateTime;

/**
 * Résultat d'un scan, pensé pour l'écran du contrôleur : un verdict et de quoi vérifier le billet.
 */
public record CheckInResultDto(
        CheckInResultEnum result,
        boolean entryAllowed,
        String ticketCode,
        String category,
        String holderName,
        LocalDateTime checkedInAt
) {
    public static CheckInResultDto invalid() {
        return new CheckInResultDto(CheckInResultEnum.INVALID, false, null, null, null, null);
    }
}
