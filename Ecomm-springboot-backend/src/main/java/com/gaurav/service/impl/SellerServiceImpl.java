
package com.gaurav.service.impl;

import com.gaurav.config.JwtProvider;
import com.gaurav.domain.AccountStatus;
import com.gaurav.domain.USER_ROLE;
import com.gaurav.exception.SellerException;
import com.gaurav.model.Address;
import com.gaurav.model.Seller;
import com.gaurav.repository.AddressRepository;
import com.gaurav.repository.SellerRepository;
import com.gaurav.service.SellerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional(readOnly = true)
    public Seller getSellerProfile(String jwt) throws SellerException {
        if (jwt == null || jwt.isBlank()) {
            log.warn("Seller profile lookup rejected: JWT is missing");
            throw new IllegalArgumentException("JWT must not be blank");
        }

        String email = jwtProvider.getEmailFromJwtToken(jwt);

        if (email == null || email.isBlank()) {
            log.warn("Seller profile lookup failed: JWT did not provide an email");
            throw new SellerException("Unable to identify seller from token");
        }

        log.debug("Retrieving seller profile from authenticated token");
        return getSellerByEmail(email);
    }

    @Override
    @Transactional
    public Seller createSeller(Seller seller) throws SellerException {
        if (seller == null) {
            throw new IllegalArgumentException("Seller must not be null");
        }

        if (seller.getEmail() == null || seller.getEmail().isBlank()) {
            throw new IllegalArgumentException("Seller email must not be blank");
        }

        if (seller.getPassword() == null || seller.getPassword().isBlank()) {
            throw new IllegalArgumentException("Seller password must not be blank");
        }

        if (seller.getPickupAddress() == null) {
            throw new IllegalArgumentException("Seller pickup address is required");
        }

        log.debug("Checking whether seller email is already registered");

        Seller existingSeller = sellerRepository.findByEmail(seller.getEmail());

        if (existingSeller != null) {
            log.warn("Seller registration rejected: email is already registered");
            throw new SellerException(
                    "Seller already exists; use a different email"
            );
        }

        Address savedAddress =
                addressRepository.save(seller.getPickupAddress());

        Seller newSeller = new Seller();
        newSeller.setEmail(seller.getEmail());
        newSeller.setPickupAddress(savedAddress);
        newSeller.setSellerName(seller.getSellerName());
        newSeller.setGSTIN(seller.getGSTIN());
        newSeller.setRole(USER_ROLE.ROLE_SELLER);
        newSeller.setMobile(seller.getMobile());
        newSeller.setPassword(passwordEncoder.encode(seller.getPassword()));
        newSeller.setBankDetails(seller.getBankDetails());
        newSeller.setBusinessDetails(seller.getBusinessDetails());

        // Never log the complete Seller object because it may contain secrets.
        Seller savedSeller = sellerRepository.save(newSeller);

        log.info("Seller account created successfully with ID: {}",
                savedSeller.getId());

        return savedSeller;
    }

    @Override
    @Transactional(readOnly = true)
    public Seller getSellerById(Long id) throws SellerException {
        validateId(id, "Seller ID");

        return sellerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Seller not found for ID: {}", id);
                    return new SellerException("Seller not found");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Seller getSellerByEmail(String email) throws SellerException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Seller email must not be blank");
        }

        Seller seller = sellerRepository.findByEmail(email);

        if (seller == null) {
            log.warn("Seller not found for requested email");
            throw new SellerException("Seller not found");
        }

        return seller;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seller> getAllSellers(AccountStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Account status must not be null");
        }

        log.debug("Retrieving sellers by account status: {}", status);
        return sellerRepository.findByAccountStatus(status);
    }

    @Override
    @Transactional
    public Seller updateSeller(Long id, Seller seller) throws SellerException {
        validateId(id, "Seller ID");

        if (seller == null) {
            throw new IllegalArgumentException("Seller update data must not be null");
        }

        Seller existingSeller = sellerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update seller: ID {} was not found", id);
                    return new SellerException(
                            "Seller not found with id " + id
                    );
                });

        if (seller.getSellerName() != null) {
            existingSeller.setSellerName(seller.getSellerName());
        }

        if (seller.getMobile() != null) {
            existingSeller.setMobile(seller.getMobile());
        }

        if (seller.getEmail() != null) {
            if (seller.getEmail().isBlank()) {
                throw new IllegalArgumentException(
                        "Seller email must not be blank"
                );
            }

            Seller emailOwner = sellerRepository.findByEmail(seller.getEmail());

            if (emailOwner != null
                    && !Objects.equals(emailOwner.getId(), id)) {
                throw new SellerException(
                        "Email is already associated with another seller"
                );
            }

            existingSeller.setEmail(seller.getEmail());
        }

        if (seller.getBusinessDetails() != null
                && seller.getBusinessDetails().getBusinessName() != null) {

            if (existingSeller.getBusinessDetails() == null) {
                existingSeller.setBusinessDetails(seller.getBusinessDetails());
            } else {
                existingSeller.getBusinessDetails().setBusinessName(
                        seller.getBusinessDetails().getBusinessName()
                );
            }
        }

        if (seller.getBankDetails() != null
                && seller.getBankDetails().getAccountHolderName() != null
                && seller.getBankDetails().getIfscCode() != null
                && seller.getBankDetails().getAccountNumber() != null) {

            if (existingSeller.getBankDetails() == null) {
                existingSeller.setBankDetails(seller.getBankDetails());
            } else {
                existingSeller.getBankDetails().setAccountHolderName(
                        seller.getBankDetails().getAccountHolderName()
                );
                existingSeller.getBankDetails().setAccountNumber(
                        seller.getBankDetails().getAccountNumber()
                );
                existingSeller.getBankDetails().setIfscCode(
                        seller.getBankDetails().getIfscCode()
                );
            }
        }

        if (seller.getPickupAddress() != null
                && seller.getPickupAddress().getAddress() != null
                && seller.getPickupAddress().getMobile() != null
                && seller.getPickupAddress().getCity() != null
                && seller.getPickupAddress().getState() != null) {

            Address incomingAddress = seller.getPickupAddress();
            Address currentAddress = existingSeller.getPickupAddress();

            if (currentAddress == null) {
                Address savedAddress = addressRepository.save(incomingAddress);
                existingSeller.setPickupAddress(savedAddress);
            } else {
                currentAddress.setAddress(incomingAddress.getAddress());
                currentAddress.setCity(incomingAddress.getCity());
                currentAddress.setState(incomingAddress.getState());
                currentAddress.setMobile(incomingAddress.getMobile());
                currentAddress.setPinCode(incomingAddress.getPinCode());
            }
        }

        if (seller.getGSTIN() != null) {
            existingSeller.setGSTIN(seller.getGSTIN());
        }

        Seller updatedSeller = sellerRepository.save(existingSeller);

        log.info("Seller updated successfully with ID: {}", id);

        return updatedSeller;
    }

    @Override
    @Transactional
    public void deleteSeller(Long id) throws SellerException {
        validateId(id, "Seller ID");

        if (!sellerRepository.existsById(id)) {
            log.warn("Cannot delete seller: ID {} was not found", id);
            throw new SellerException("Seller not found with id " + id);
        }

        sellerRepository.deleteById(id);
        log.info("Seller deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public Seller verifyEmail(String email, String otp) throws SellerException {
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("OTP must not be blank");
        }

        Seller seller = getSellerByEmail(email);
        seller.setEmailVerified(true);

        Seller updatedSeller = sellerRepository.save(seller);

        log.info("Email verification status updated for seller ID: {}",
                updatedSeller.getId());

        return updatedSeller;
    }

    @Override
    @Transactional
    public Seller updateSellerAccountStatus(
            Long sellerId,
            AccountStatus status
    ) throws SellerException {

        validateId(sellerId, "Seller ID");

        if (status == null) {
            throw new IllegalArgumentException("Account status must not be null");
        }

        Seller seller = getSellerById(sellerId);
        seller.setAccountStatus(status);

        Seller updatedSeller = sellerRepository.save(seller);

        log.info("Account status updated for seller ID: {}", sellerId);

        return updatedSeller;
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be a positive number"
            );
        }
    }
}

