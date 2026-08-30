package com.orginsight.service;

import java.util.Optional;

import com.orginsight.entity.User;

public interface UserService {
    User save(User user);
    Optional<User> findByUsername(String username);
}
