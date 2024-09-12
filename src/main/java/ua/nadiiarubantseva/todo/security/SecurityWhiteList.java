package ua.nadiiarubantseva.todo.security;

public class SecurityWhiteList {

    public static final String[] AUTH_WHITE_LIST = {
            "/api/v1/auth/**",
            "/pwd/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/error"
    };
}
