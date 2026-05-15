package com.surense.supporthub.ticket;

import com.surense.supporthub.ticket.dto.CreateTicketRequest;
import com.surense.supporthub.ticket.dto.TicketResponse;
import com.surense.supporthub.user.User;
import com.surense.supporthub.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest req) {
        User caller = userService.currentUser();
        return TicketResponse.from(ticketService.create(req, caller));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    public List<TicketResponse> list() {
        User caller = userService.currentUser();
        return ticketService.list(caller).stream().map(TicketResponse::from).toList();
    }
}
