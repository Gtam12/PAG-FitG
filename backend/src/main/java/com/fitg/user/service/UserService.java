package com.fitg.user.service;

import com.fitg.user.dto.UserResponse;
import com.fitg.user.exception.UserNotFoundException;
import com.fitg.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
     private UserRepository userRepository;


     @Transactional(readOnly = true)
     public UserResponse getMe(UUID id) {
     return  userRepository.findById(id).map(UserResponse::from).orElseThrow(UserNotFoundException::new);
     }


}
