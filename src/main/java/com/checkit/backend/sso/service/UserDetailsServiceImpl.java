package com.checkit.backend.sso.service;

import com.checkit.backend.common.exeption.BadRequestException;
import com.checkit.backend.sso.model.persistent.ApplicationUser;
import com.checkit.backend.sso.model.persistent.UserRole;
import com.checkit.backend.sso.repository.ApplicationUserRepository;
import com.checkit.backend.sso.model.dto.request.SignUpUserRequest;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Optional;

/**
 * Created by Gleb Dovzhenko on 22.04.2018.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ApplicationUserRepository applicationUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserDetailsServiceImpl(ApplicationUserRepository applicationUserRepository,
                                  BCryptPasswordEncoder bCryptPasswordEncoder) {

        this.applicationUserRepository = applicationUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional(readOnly = true)
    @Override
    public ApplicationUser loadUserByUsername(@NonNull String username) {
        Optional<ApplicationUser> probablyUserAccount = applicationUserRepository.findByEmail(username);
        if (!probablyUserAccount.isPresent()) {
            throw new UsernameNotFoundException("User with email " + username + " not found");
        }
        return probablyUserAccount.get();
    }

    @Transactional
    public ApplicationUser registerUser(@Valid SignUpUserRequest signUpUserRequest) {

        if(applicationUserRepository.findByEmail(signUpUserRequest.getEmail()).isPresent())
        throw new BadRequestException("Email already exists");

        ApplicationUser applicationUser = new ApplicationUser(bCryptPasswordEncoder.encode(signUpUserRequest.getPassword()),
                                                              signUpUserRequest.getEmail(),
                                                              UserRole.USER);
        applicationUserRepository.save(applicationUser);
        return applicationUser;
    }
}
