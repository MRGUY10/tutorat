package com.iiil.tutoring.security;

import com.iiil.tutoring.entity.User;
import com.iiil.tutoring.enums.UserStatus;
import com.iiil.tutoring.repository.UserRepository;
import com.iiil.tutoring.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

/**
 * Custom reactive user details service for Spring Security
 */
@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getStatut() == UserStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Utilisateur non trouvé ou désactivé: " + email)))
                .flatMap(user -> 
                    roleService.getUserRoles(user.getId())
                            .map(role -> "ROLE_" + role.getNom().getValue())
                            .collectList()
                            .map(roles -> roles.isEmpty() ? 
                                List.of("ROLE_" + determineUserRoleFromType(user)) : roles)
                            .map(roles -> new CustomUserDetails(user, roles))
                );
    }

    /**
     * Fallback method to determine role from user type when no roles are assigned
     */
    private String determineUserRoleFromType(User user) {
        String className = user.getClass().getSimpleName();
        switch (className) {
            case "Etudiant":
                return "STUDENT";
            case "Tuteur":
                return "TUTOR";
            case "Admin":
                return "ADMIN";
            default:
                return "USER";
        }
    }

    /**
     * Custom UserDetails implementation
     */
    public static class CustomUserDetails implements UserDetails {
        private final User user;
        private final Collection<? extends GrantedAuthority> authorities;

        public CustomUserDetails(User user, List<String> roles) {
            this.user = user;
            this.authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        // Legacy constructor for backward compatibility
        public CustomUserDetails(User user) {
            this.user = user;
            // Determine role based on user type as fallback
            String role = determineUserRole(user);
            this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        }

        private String determineUserRole(User user) {
            String className = user.getClass().getSimpleName();
            switch (className) {
                case "Etudiant":
                    return "STUDENT";
                case "Tuteur":
                    return "TUTOR";
                case "Admin":
                    return "ADMIN";
                default:
                    return "USER";
            }
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getMotDePasse();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getStatut() != UserStatus.SUSPENDED;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getStatut() == UserStatus.ACTIVE;
        }

        public User getUser() {
            return user;
        }
    }
}