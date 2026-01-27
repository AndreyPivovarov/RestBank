package com.example.bankcards.util;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil(){}

    public static boolean hasRole(String role) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities() == null) return false;

        String expected = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        for (GrantedAuthority a : auth.getAuthorities()) {
            if (expected.equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
