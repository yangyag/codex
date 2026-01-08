package com.msa.member.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Member 서비스는 자체 인증을 하지 않고 JWT만 검증하므로 목 사용자로 대체
        return User.withUsername(username)
                .password("N/A")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
    }
}
