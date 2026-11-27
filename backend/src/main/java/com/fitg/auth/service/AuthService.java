package com.fitg.auth.service;

import com.fitg.auth.dto.AuthResponse;
import com.fitg.auth.dto.LoginRequest;
import com.fitg.auth.dto.RegisterRequest;
import com.fitg.auth.exception.EmailAlreadyUsedException;
import com.fitg.auth.exception.InvalidCredentialsException;
import com.fitg.user.model.UserEntity;
import com.fitg.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final String DUMMY_HASH =
            "$2a$10$6C7BirHi9KfveZ.1fYt0cOAHmr66KVK/TsBtmA4zyVN4t/S6Ouc/S";

    @Transactional
    public AuthResponse register(RegisterRequest request){
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if(userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException();
        }

        UserEntity user = new UserEntity(email, passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        try {
            user = userRepository.saveAndFlush(user);
        } catch(DataIntegrityViolationException e) {
            throw new EmailAlreadyUsedException();
        }
       return jwtService.issue(user.getId());
    }


    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity  user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        String hash = (user != null ) ? user.getPasswordHash() : DUMMY_HASH;
        boolean ok = passwordEncoder.matches(request.password(), hash);
        if (user == null || !ok)
                throw new InvalidCredentialsException();
        return jwtService.issue(user.getId());
    }


}
