
package com.gaurav.controller;

import com.gaurav.exception.UserException;
import com.gaurav.model.User;
import com.gaurav.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getUserProfileShouldReturnAuthenticatedUserProfile()
            throws UserException {

        String jwt = "Bearer test-token";
        User user = mock(User.class);

        when(userService.findUserProfileByJwt(jwt))
                .thenReturn(user);

        ResponseEntity<User> response =
                userController.getUserProfileHandler(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(user, response.getBody());

        verify(userService).findUserProfileByJwt(jwt);
    }

    @Test
    void getUserProfileShouldPropagateUserException()
            throws UserException {

        String jwt = "Bearer invalid-token";

        when(userService.findUserProfileByJwt(jwt))
                .thenThrow(new UserException("User not found"));

        org.junit.jupiter.api.Assertions.assertThrows(
                UserException.class,
                () -> userController.getUserProfileHandler(jwt)
        );

        verify(userService).findUserProfileByJwt(jwt);
    }
}

