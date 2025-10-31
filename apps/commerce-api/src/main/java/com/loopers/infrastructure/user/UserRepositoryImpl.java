package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> save(User user) {
        userJpaRepository.save(user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> findByLoginId(String id) {
        return userJpaRepository.findByLoginId(id);
    }
}
