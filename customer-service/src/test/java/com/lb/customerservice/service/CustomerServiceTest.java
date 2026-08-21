package com.lb.customerservice.service;

import com.lb.customerservice.domain.Customer;
import com.lb.customerservice.domain.CustomerStatus;
import com.lb.customerservice.dto.CustomerRequest;
import com.lb.customerservice.exception.CpfAlreadyExistsException;
import com.lb.customerservice.exception.CustomerNotFoundException;
import com.lb.customerservice.repository.CustomerJdbcRepository;
import com.lb.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerJdbcRepository customerJdbcRepository;
    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest request;

    @BeforeEach
    void setUp() {
        request = CustomerRequest.builder()
                .name("Joao da Silva").cpf("11144477735").email("joao@email.com").build();
    }

    @Test
    void deveCriarClienteQuandoCpfNaoExiste() {
        when(customerRepository.existsByCpf("11144477735")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Customer created = customerService.create(request);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void naoDeveCriarClienteComCpfDuplicado() {
        when(customerRepository.existsByCpf("11144477735")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(CpfAlreadyExistsException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoBuscarClienteInexistente() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoExcluirClienteInexistente() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepository, never()).delete(any());
    }

    @Test
    void naoDeveBloquearAtualizacaoQuandoCpfNaoMudou() {
        Customer existing = Customer.builder().id(1L).name("Old").cpf("11144477735")
                .email("old@email.com").status(CustomerStatus.ACTIVE).build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer updated = customerService.update(1L, request);

        assertThat(updated.getName()).isEqualTo("Joao da Silva");
        verify(customerRepository, never()).existsByCpf(any());
    }

    @Test
    void deveUsarJdbcTemplateQuandoStatusInformado() {
        when(customerJdbcRepository.findByStatus(CustomerStatus.ACTIVE)).thenReturn(List.of());

        customerService.findAll(CustomerStatus.ACTIVE);

        verify(customerJdbcRepository).findByStatus(CustomerStatus.ACTIVE);
        verify(customerRepository, never()).findAll();
    }

    @Test
    void deveUsarJpaQuandoStatusNaoInformado() {
        when(customerRepository.findAll()).thenReturn(List.of());

        customerService.findAll(null);

        verify(customerRepository).findAll();
        verify(customerJdbcRepository, never()).findByStatus(any());
    }
}