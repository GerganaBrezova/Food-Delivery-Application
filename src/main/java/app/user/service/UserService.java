package app.user.service;

import app.exceptions.UsernameNotFoundException;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User %s was not found.".formatted(username)));

        return new UserAuthDetails(user.getId(), username, user.getPassword(), user.getEmail(), user.getFullName(), user.getRole(), user.isActive());
    }

}
