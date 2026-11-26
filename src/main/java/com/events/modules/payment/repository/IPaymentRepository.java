package com.events.modules.payment.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Collection;

import com.events.modules.payment.entity.Payment;
import com.events.modules.payment.enumeration.PaymentStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByTransactionId(String transactionId);

    /** Une réservation peut avoir plusieurs paiements (tentatives échouées puis nouvelle tentative). */
    List<Payment> findAllByBookingId(UUID bookingId);

    List<Payment> findByUserId(UUID userId);

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    List<Payment> findByUserIdAndStatus(UUID userId, PaymentStatusEnum status);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.id = :paymentId")
    Optional<Payment> findByUserIdAndPaymentId(@Param("userId") UUID userId, @Param("paymentId") UUID paymentId);

    boolean existsByBookingId(UUID bookingId);

    boolean existsByBookingIdAndStatusIn(UUID bookingId, Collection<PaymentStatusEnum> statuses);

    /** Verrou pessimiste : évite qu'une confirmation client et un webhook traitent le même paiement en parallèle. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentReference = :paymentReference")
    Optional<Payment> findForUpdateByPaymentReference(@Param("paymentReference") String paymentReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.stripePaymentIntentId = :intentId")
    Optional<Payment> findForUpdateByStripePaymentIntentId(@Param("intentId") String intentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId")
    Optional<Payment> findForUpdateById(@Param("paymentId") UUID paymentId);
}
