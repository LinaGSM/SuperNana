package demo.service;

import demo.model.AppUser;
import demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for loading user details.
 * <p>
 * This service implements the UserDetailsService interface and is responsible for loading user details
 * from the database based on the provided username. It is used by Spring Security for authentication and authorization.
 * </p>
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;


    /**
     * Loads the user details by username.
     * <p>
     * This method retrieves the {@link AppUser} from the database using the given username. If the user is found,
     * it returns a {@link User} object with the user's username, password, and role. If the user is not found,
     * a {@link UsernameNotFoundException} is thrown.
     * </p>
     *
     * @param username The username of the user to load.
     * @return The user's details.
     * @throws UsernameNotFoundException If the user is not found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
