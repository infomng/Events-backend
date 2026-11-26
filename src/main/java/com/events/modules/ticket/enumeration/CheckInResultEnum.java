package com.events.modules.ticket.enumeration;

public enum CheckInResultEnum {
    /** Billet valide : entrée autorisée, billet désormais utilisé. */
    ACCEPTED,
    /** Billet déjà scanné : entrée refusée. */
    ALREADY_USED,
    /** Billet annulé (remboursement...) : entrée refusée. */
    CANCELLED,
    /** Billet valide mais pour un autre événement. */
    WRONG_EVENT,
    /** QR code falsifié, illisible ou inconnu. */
    INVALID
}
