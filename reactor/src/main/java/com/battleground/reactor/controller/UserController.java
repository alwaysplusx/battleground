package com.battleground.reactor.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/info/{username}")
    Mono<User> info(@PathVariable String username) {
        return userRepository
            .findByUsername(username)
            .switchIfEmpty(Mono.error(DataNotFoundException::new));
    }

    @PostMapping("/api/register")
    Mono<String> register(@RequestBody User user) {
        String username = user.getUsername();
        return userRepository.countByUsername(username)
            .flatMap(e -> (e > 0)
                ? Mono.error(DuplicateDataException::new)
                : userRepository.save(user).flatMap(u -> Mono.just("register success!")
            ));
    }

}

@Data
@Table("user")
class User {

    @Id
    private Long id;
    private String username;
    private String password;

}

@Repository
interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByUsername(String username);

    Mono<Long> countByUsername(String username);

}


@ResponseStatus(HttpStatus.NOT_FOUND)
class DataNotFoundException extends RuntimeException {
}


@ResponseStatus(HttpStatus.BAD_REQUEST)
class DuplicateDataException extends RuntimeException {

}
