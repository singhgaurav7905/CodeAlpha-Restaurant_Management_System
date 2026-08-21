package com.restaurant.security;

import com.restaurant.model.StaffUser;
import com.restaurant.repository.StaffUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Bridges our StaffUser table into Spring Security's login mechanism. */
@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffUserRepository staffUserRepository;

    public StaffUserDetailsService(StaffUserRepository staffUserRepository) {
        this.staffUserRepository = staffUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StaffUser staff = staffUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown staff user: " + username));
        return User.builder()
                .username(staff.getUsername())
                .password(staff.getPasswordHash())
                .disabled(!staff.isEnabled())
                .roles(staff.getRole().name())
                .build();
    }
}
