package com.example.ola.dto.security;

import com.example.ola.domain.User;
import com.example.ola.domain.UserRole;
import com.example.ola.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String name;
    private Long ageRange;
    private String homeGym;
    Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal fromUser(User user) {
        Set<UserRole> roles = Set.of(UserRole.USER);
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getName(),
                user.getAgeRange(),
                user.getHomeGym(),
                roles.stream().map(UserRole::getName).map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {return this.authorities; }

    @Override public String getPassword() { return this.password; }

    @Override public String getUsername() { return this.username; }

    @Override public boolean isAccountNonExpired() { return true; }

    @Override public boolean isAccountNonLocked() { return true; }

    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override public boolean isEnabled() { return true; }
}
