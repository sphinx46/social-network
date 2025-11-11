package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface UserService{
    User create(User user);
    User save(User user);
    User getByUsername(String username);
    User getById(Long id);
    UserDetailsService userDetailsService();
    User getCurrentUser();
    void getAdmin();
}
