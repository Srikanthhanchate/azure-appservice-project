package com.orginsight.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.orginsight.entity.User;
import com.orginsight.repository.UserRepository;
import com.orginsight.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
