package com.events.modules.ticket.service;

import com.events.common.exception.ResourceNotFoundException;
import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.event.exception.EventForbidenException;
import com.events.modules.event.service.IEventService;
import com.events.modules.qrcode.service.IQrCodeService;
import com.events.modules.ticket.dto.CheckInRequestDto;
import com.events.modules.ticket.dto.CheckInResultDto;
import com.events.modules.ticket.entity.Ticket;
import com.events.modules.ticket.enumeration.CheckInResultEnum;
import com.events.modules.ticket.enumeration.TicketStatusEnum;
import com.events.modules.ticket.repository.ITicketRepository;
import com.events.modules.ticket.security.TicketTokenSigner;
import com.events.modules.ticket.service.impl.TicketService;
import com.events.modules.user.entity.User;
import com.events.modules.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceTest {

    @Mock private ITicketRepository ticketRepository;
    @Mock private IUserService userService;
    @Mock private IEventService eventService;
    @Mock private IQrCodeService qrCodeService;
    @Mock private IAuthService authService;

    private final TicketTokenSigner signer = new TicketTokenSigner("unit-test-signing-secret-0123456789abcdef");
    private TicketService ticketService;

    private User staff;
    private User holder;
    private UUID eventId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, userService, eventService, qrCodeService,
                authService, signer, Clock.fixed(Instant.parse("2026-06-01T20:00:00Z"), ZoneId.of("UTC")));
        staff = User.builder().id(UUID.randomUUID()).fullName("Staff").build();
        holder = User.builder().id(UUID.randomUUID()).fullName("Holder").build();
        eventId = UUID.randomUUID();
        ticket = Ticket.builder().id(UUID.randomUUID()).eventId(eventId).user(holder)
                .ticketCode("code-1").category("VIP").price(50.0).status(TicketStatusEnum.VALID).build();

        given(authService.getCurrentUser()).willReturn(staff);
        given(eventService.isCurrentUserEntryManager(eventId)).willReturn(true);
        given(ticketRepository.findByTicketCode("code-1")).willReturn(Optional.of(ticket));
    }

    @Nested
    class SignatureTicketTest {

        @Test
        void signedTokenIsVerified() {
            assertThat(signer.verify(signer.sign("code-1"))).contains("code-1");
        }

        @Test
        void forgedOrTamperedTokenIsRejected() {
            String token = signer.sign("code-1");
            assertThat(signer.verify(token.replace("code-1", "code-2"))).isEmpty();
            assertThat(signer.verify("EVT1.code-1.forged")).isEmpty();
            assertThat(signer.verify("code-1")).isEmpty();
            assertThat(new TicketTokenSigner("another-signing-secret-0123456789abcdef").verify(token)).isEmpty();
        }

        @Test
        void tooShortSecretIsRefusedAtStartup() {
            assertThatThrownBy(() -> new TicketTokenSigner("short")).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class CheckInTicketTest {

        @Test
        void validTicketIsAccepted() {
            given(ticketRepository.markAsUsed(eq("code-1"), eq(eventId), eq(staff.getId()), any(), any(), any()))
                    .willReturn(1);

            CheckInResultDto result = ticketService.checkIn(new CheckInRequestDto(eventId, signer.sign("code-1")));

            assertThat(result.result()).isEqualTo(CheckInResultEnum.ACCEPTED);
            assertThat(result.entryAllowed()).isTrue();
            assertThat(result.holderName()).isEqualTo("Holder");
        }

        @Test
        void secondScanIsRefusedAsAlreadyUsed() {
            given(ticketRepository.markAsUsed(any(), any(), any(), any(), any(), any())).willReturn(0);
            ticket.setStatus(TicketStatusEnum.USED);

            CheckInResultDto result = ticketService.checkIn(new CheckInRequestDto(eventId, signer.sign("code-1")));

            assertThat(result.result()).isEqualTo(CheckInResultEnum.ALREADY_USED);
            assertThat(result.entryAllowed()).isFalse();
        }

        @Test
        void cancelledTicketIsRefused() {
            given(ticketRepository.markAsUsed(any(), any(), any(), any(), any(), any())).willReturn(0);
            ticket.setStatus(TicketStatusEnum.CANCELLED);

            CheckInResultDto result = ticketService.checkIn(new CheckInRequestDto(eventId, signer.sign("code-1")));

            assertThat(result.result()).isEqualTo(CheckInResultEnum.CANCELLED);
        }

        @Test
        void forgedQrCodeIsInvalidWithoutDatabaseLookup() {
            CheckInResultDto result = ticketService.checkIn(new CheckInRequestDto(eventId, "EVT1.code-1.forged"));

            assertThat(result.result()).isEqualTo(CheckInResultEnum.INVALID);
            verify(ticketRepository, never()).findByTicketCode(anyString());
        }

        @Test
        void ticketOfAnotherEventIsRefused() {
            UUID otherEvent = UUID.randomUUID();
            given(eventService.isCurrentUserEntryManager(otherEvent)).willReturn(true);

            CheckInResultDto result = ticketService.checkIn(new CheckInRequestDto(otherEvent, signer.sign("code-1")));

            assertThat(result.result()).isEqualTo(CheckInResultEnum.WRONG_EVENT);
            verify(ticketRepository, never()).markAsUsed(any(), any(), any(), any(), any(), any());
        }

        @Test
        void onlyEntryManagersCanScan() {
            given(eventService.isCurrentUserEntryManager(eventId)).willReturn(false);

            assertThatThrownBy(() -> ticketService.checkIn(new CheckInRequestDto(eventId, signer.sign("code-1"))))
                    .isInstanceOf(EventForbidenException.class);
        }
    }

    @Nested
    class ExportTicketTest {

        @Test
        void csvEscapesQuotesAndNeutralisesFormulas() {
            User attacker = User.builder().id(UUID.randomUUID())
                    .fullName("=HYPERLINK(\"http://evil\")").email("a@example.com").build();
            Ticket t = Ticket.builder().id(UUID.randomUUID()).eventId(eventId).user(attacker)
                    .ticketCode("code-9").category("VIP").price(10.0).status(TicketStatusEnum.VALID).build();
            given(ticketRepository.findByEventIdWithHolder(eventId)).willReturn(List.of(t));

            String csv = ticketService.exportParticipantsCsv(eventId);

            verify(eventService).assertCurrentUserManagesEvent(eventId);
            assertThat(csv).contains("\"'=HYPERLINK(\"\"http://evil\"\")\"");
            assertThat(csv).contains("\"code-9\";\"VIP\"");
        }
    }

    @Nested
    class OwnershipTicketTest {

        @Test
        void cannotReadSomeoneElsesTicket() {
            given(ticketRepository.findById(ticket.getId())).willReturn(Optional.of(ticket));
            given(eventService.isCurrentUserEntryManager(eventId)).willReturn(false);

            assertThatThrownBy(() -> ticketService.getTicketById(ticket.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
