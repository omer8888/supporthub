package com.surense.supporthub.ticket;

import com.surense.supporthub.exception.ForbiddenException;
import com.surense.supporthub.ticket.dto.CreateTicketRequest;
import com.surense.supporthub.user.Role;
import com.surense.supporthub.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock TicketRepository ticketRepository;
    @InjectMocks TicketService ticketService;

    private User customer(long id, User agent) {
        return User.builder().id(id).username("c" + id).role(Role.CUSTOMER).agent(agent).build();
    }

    private User agent(long id) {
        return User.builder().id(id).username("a" + id).role(Role.AGENT).build();
    }

    @Test
    void createTicket_setsUserToCaller() {
        User c = customer(5L, agent(1L));
        CreateTicketRequest req = new CreateTicketRequest("Help", "Broken");
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        Ticket saved = ticketService.create(req, c);

        assertThat(saved.getUser()).isSameAs(c);
        assertThat(saved.getSubject()).isEqualTo("Help");
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void createTicket_asAgent_throwsForbidden() {
        User a = agent(1L);
        CreateTicketRequest req = new CreateTicketRequest("x", "y");

        assertThatThrownBy(() -> ticketService.create(req, a))
                .isInstanceOf(ForbiddenException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void listTickets_asAgent_returnsOnlyHisCustomersTickets() {
        User a = agent(9L);
        when(ticketRepository.findByOwnerAgentId(9L)).thenReturn(List.of(new Ticket()));

        ticketService.list(a);

        verify(ticketRepository).findByOwnerAgentId(9L);
        verify(ticketRepository, never()).findAll();
        verify(ticketRepository, never()).findByUserId(any());
    }

    @Test
    void listTickets_asCustomer_returnsOnlyOwnTickets() {
        User c = customer(11L, agent(9L));
        when(ticketRepository.findByUserId(11L)).thenReturn(List.of(new Ticket()));

        ticketService.list(c);

        verify(ticketRepository).findByUserId(11L);
        verify(ticketRepository, never()).findAll();
        verify(ticketRepository, never()).findByOwnerAgentId(any());
    }

    @Test
    void listTickets_asAdmin_returnsAll() {
        User admin = User.builder().id(1L).role(Role.ADMIN).build();
        when(ticketRepository.findAll()).thenReturn(List.of());

        ticketService.list(admin);

        verify(ticketRepository).findAll();
    }
}
