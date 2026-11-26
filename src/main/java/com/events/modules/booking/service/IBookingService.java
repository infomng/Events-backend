package com.events.modules.booking.service;

import com.events.modules.booking.dto.BookingNoticeDto;
import com.events.modules.booking.dto.BookingPaymentDto;
import com.events.modules.booking.dto.CheckoutRequestDto;
import com.events.modules.booking.dto.GetBookingDto;
import com.events.modules.booking.dto.SalesReportDto;
import com.events.modules.event.enumeration.BookingStatusEnum;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IBookingService {

    /**
     * Transforme les articles du panier d'un événement en réservation et bloque les places.
     * Une réservation gratuite est confirmée immédiatement.
     */
    GetBookingDto checkout(CheckoutRequestDto dto);

    Page<GetBookingDto> getMyBookings(int page, int size);

    GetBookingDto getMyBooking(UUID bookingId);

    /**
     * Annule une réservation non payée de l'utilisateur et libère ses places.
     */
    void cancelMyBooking(UUID bookingId);

    /**
     * Retourne une réservation payable appartenant à l'utilisateur.
     * Échoue si la réservation n'existe pas, n'appartient pas à l'utilisateur,
     * n'est plus en attente de paiement ou a expiré.
     */
    BookingPaymentDto getPayableBooking(UUID bookingId, UUID userId);

    /**
     * Marque la réservation comme payée, émet les billets et notifie l'acheteur.
     * @return false si la réservation n'a pas pu être honorée (expirée et places reprises) :
     * le paiement doit alors être remboursé
     */
    boolean confirmBookingPayment(UUID bookingId, String paymentId);

    /**
     * Annule la réservation suite à un remboursement : billets annulés, places libérées.
     */
    void cancelBookingAfterRefund(UUID bookingId);

    /** Identifiants des réservations en attente dont la date limite de paiement est dépassée. */
    List<UUID> findOverdueBookingIds();

    /** Expire une réservation non payée et libère ses places (sans effet si elle n'est plus en attente). */
    void expireBooking(UUID bookingId);

    /** Réservations d'un événement ayant l'un des statuts donnés. */
    List<UUID> findBookingIdsForEvent(UUID eventId, List<BookingStatusEnum> statuses);

    /** Annule une réservation non payée parce que l'événement a été annulé (places libérées, paiement en cours annulé). */
    void cancelPendingBookingForCancelledEvent(UUID bookingId);

    BookingNoticeDto getBookingNotice(UUID bookingId);

    /** Tableau de bord des ventes d'un événement. Réservé à l'organisateur et aux admins. */
    SalesReportDto getSalesReport(UUID eventId);
}
