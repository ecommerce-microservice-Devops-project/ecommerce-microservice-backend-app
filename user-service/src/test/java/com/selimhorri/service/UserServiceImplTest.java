package com.selimhorri.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    private User user;
    private Credential credential;
    private Set<Address> addresses;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);

        addresses = new HashSet<>();
        addresses.add(new Address());

        credential = new Credential();
        credential.setCredentialId(42);
        credential.setUsername("janedoe");
        credential.setPassword("securepass");
        credential.setRoleBasedAuthority(RoleBasedAuthority.ROLE_USER);
        credential.setIsEnabled(true);
        credential.setIsAccountNonExpired(true);
        credential.setIsAccountNonLocked(true);
        credential.setIsCredentialsNonExpired(true);

        user = new User(
            42,
            "Jane",
            "Doe",
            "https://example.com/avatar/jane.png",
            "jane.doe@example.com",
            "+573001112233",
            addresses,
            credential
        );
    }

    @Test
    void testFindAllReturnsMappedUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
        UserDto dto = result.get(0);
        assertEquals(user.getUserId(), dto.getUserId());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertNotNull(user.getCredential());

        verify(userRepository).findAll();
    }

    @Test
    void testFindByIdReturnsUserDtoWhenFound() {
        when(userRepository.findById(42)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(42);

        assertEquals(42, result.getUserId());
        assertEquals("Jane", result.getFirstName());
        verify(userRepository).findById(42);
    }

    @Test
    void testFindByIdThrowsExceptionWhenNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(UserObjectNotFoundException.class, () -> userService.findById(999));

        verify(userRepository).findById(999);
    }

    @Test
    void testFindByUsernameReturnsUserDto() {
        user.setUserId(101);
        when(userRepository.findByCredentialUsername("janedoe")).thenReturn(Optional.of(user));

        UserDto dto = userService.findByUsername("janedoe");

        assertEquals(101, dto.getUserId());
        assertEquals("janedoe", dto.getCredentialDto().getUsername());
        verify(userRepository).findByCredentialUsername("janedoe");
    }

    @Test
    void testFindByUsernameThrowsExceptionWhenNotFound() {
        when(userRepository.findByCredentialUsername("ghostuser")).thenReturn(Optional.empty());

        assertThrows(UserObjectNotFoundException.class, () -> userService.findByUsername("ghostuser"));

        verify(userRepository).findByCredentialUsername("ghostuser");
    }

    @Test
    void testSaveUserCallsRepositorySaveAndReturnsDto() {
        UserDto inputDto = Optional.of(user).map(UserMappingHelper::map).orElse(null);
        inputDto.setFirstName("Elena");

        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0)); // Retorna lo que le pasen (el User con "Elena")


        UserDto result = userService.save(inputDto);

        assertEquals(42, result.getUserId());
        assertEquals("Elena", result.getFirstName());
        verify(userRepository).save(any(User.class));
    }


    @Test
    void testDeleteByIdCallsRepositoryDelete() {
        doNothing().when(userRepository).deleteById(42);

        userService.deleteById(42);

        verify(userRepository).deleteById(42);
    }
}
