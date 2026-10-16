
package com.gaurav.service.impl;

import java.util.List;

import com.gaurav.domain.USER_ROLE;
import com.gaurav.model.Seller;
import com.gaurav.model.User;
import com.gaurav.repository.SellerRepository;
import com.gaurav.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomeUserServiceImplementation implements UserDetailsService {

    private static final String SELLER_PREFIX = "seller_";

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    public CustomeUserServiceImplementation(
            UserRepository userRepository,
            SellerRepository sellerRepository) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if (username == null || username.isBlank()) {
            log.warn("Authentication failed: username is empty");
            throw new UsernameNotFoundException("Username must not be empty");
        }

        if (username.startsWith(SELLER_PREFIX)) {
            String actualUsername = username.substring(SELLER_PREFIX.length());

            log.debug("Loading seller authentication details");

            Seller seller = sellerRepository.findByEmail(actualUsername);

            if (seller != null) {
                log.debug("Seller authentication details loaded successfully");
                return buildUserDetails(
                        seller.getEmail(),
                        seller.getPassword(),
                        seller.getRole()
                );
            }
        } else {
            log.debug("Loading customer authentication details");

            User user = userRepository.findByEmail(username);

            if (user != null) {
                log.debug("Customer authentication details loaded successfully");
                return buildUserDetails(
                        user.getEmail(),
                        user.getPassword(),
                        user.getRole()
                );
            }
        }

        log.warn("Authentication failed: account not found");
        throw new UsernameNotFoundException(
                "User or Seller not found"
        );
    }

    private UserDetails buildUserDetails(
            String email,
            String password,
            USER_ROLE role) {

        USER_ROLE effectiveRole = role != null
                ? role
                : USER_ROLE.ROLE_CUSTOMER;

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(effectiveRole.toString())
        );

        return new org.springframework.security.core.userdetails.User(
                email,
                password,
                authorities
        );
    }
}

