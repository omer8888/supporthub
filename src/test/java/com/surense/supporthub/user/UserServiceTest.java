package com.surense.supporthub.user;

import com.surense.supporthub.exception.ConflictException;
import com.surense.supporthub.user.dto.CreateCustomerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private User agent(long id) {
        return User.builder().id(id).username("a" + id).role(Role.AGENT).password("x").build();
    }

    private User admin() {
        return User.builder().id(1L).username("admin").role(Role.ADMIN).password("x").build();
    }

    @Test
    void createCustomer_asAgent_setsAgentIdToCallingAgent() {
        User agent = agent(10L);
        CreateCustomerRequest req = new CreateCustomerRequest("c1", "pw", "c1@x.com", "C One", null);
        when(userRepository.existsByUsername("c1")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.createCustomer(req, agent);

        assertThat(saved.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(saved.getAgent()).isSameAs(agent);
        assertThat(saved.getPassword()).isEqualTo("HASH");
    }

    @Test
    void createCustomer_asAdmin_requiresAgentId_andResolvesAgent() {
        User admin = admin();
        User targetAgent = agent(42L);
        CreateCustomerRequest req = new CreateCustomerRequest("c2", "pw", "c2@x.com", "C Two", 42L);
        when(userRepository.existsByUsername("c2")).thenReturn(false);
        when(userRepository.findById(42L)).thenReturn(Optional.of(targetAgent));
        when(passwordEncoder.encode("pw")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.createCustomer(req, admin);

        assertThat(saved.getAgent()).isSameAs(targetAgent);
    }

    @Test
    void createCustomer_duplicateUsername_throwsConflict() {
        User agent = agent(10L);
        CreateCustomerRequest req = new CreateCustomerRequest("dup", "pw", null, null, null);
        when(userRepository.existsByUsername("dup")).thenReturn(true);

        assertThatThrownBy(() -> userService.createCustomer(req, agent))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getCustomersForCurrentUser_asAgent_returnsOnlyOwnCustomers() {
        User agent = agent(7L);
        User mine = User.builder().id(100L).role(Role.CUSTOMER).agent(agent).build();
        when(userRepository.findByAgentId(7L)).thenReturn(List.of(mine));

        List<User> result = userService.listCustomers(agent);

        assertThat(result).containsExactly(mine);
        verify(userRepository).findByAgentId(7L);
        verify(userRepository, never()).findByRole(any());
        verify(userRepository, never()).findAll();
    }

    @Test
    void getCustomersForCurrentUser_asAdmin_returnsAllCustomers() {
        User admin = admin();
        when(userRepository.findByRole(Role.CUSTOMER)).thenReturn(List.of());

        userService.listCustomers(admin);

        verify(userRepository).findByRole(Role.CUSTOMER);
        verify(userRepository, never()).findByAgentId(any());
    }
}
