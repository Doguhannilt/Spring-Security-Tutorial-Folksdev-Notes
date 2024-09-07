# Spring-Security-Tutorial-Folksdev


- This repository represents my notes about Spring Security.
- All informations comes from this <a href="https://www.youtube.com/watch?v=JdnwMpP6YhE">Youtube video</a>. Also don't forget to check their <a href="https://github.com/folksdev">Github Account</a>.

## SECTION 1 - IN-MEMORY 

### In-Memory User Details Configuration

The code snippet below defines a Spring `@Bean` that configures an in-memory `UserDetailsService`. This service manages two users, `user1` and `admin`, with different roles and credentials.

- The `UserDetailsService` interface is used to retrieve user information.
- We are using the `User.builder()` method to create two user accounts: 
  1. **User1**:
     - Username: `"fsk"`
     - Password: `"fsk"` (encoded using the `passwordEncoder()` method)
     - Role: `"USER"`
  2. **Admin**:
     - Username: `"fsk_admin"`
     - Password: `"fsk_admin"` (also encoded using `passwordEncoder()`)
     - Role: `"ADMIN"`

Both users are managed by the `InMemoryUserDetailsManager`, which stores user details directly in memory, rather than using an external database.

#### Code Explanation:

```java
@Bean
public UserDetailsService users() {
    UserDetails user1 = User.builder()
            .username("fsk")
            .password(passwordEncoder().encode("fsk"))
            .roles("USER")
            .build();
    UserDetails admin = User.builder()
            .username("fsk_admin")
            .password(passwordEncoder().encode("fsk_admin"))
            .roles("ADMIN")
            .build();
    return new InMemoryUserDetailsManager(user1, admin);
}
```

- **UserDetailsService**: Provides a way to retrieve user details based on the username.
- **User.builder()**: A utility method to construct `UserDetails` instances with specific attributes such as username, password, and role.
- **InMemoryUserDetailsManager**: Stores user data in memory for simplicity, typically useful for development or testing environments.

The passwords are encoded using `passwordEncoder()` to ensure that plain text passwords are not stored directly.



### Configuring the Security Filter Chain

The following code defines a `SecurityFilterChain` bean that configures the security settings for a Spring Boot application. It controls how requests are handled, which URLs are accessible without authentication, and sets up basic HTTP authentication.

#### Code Explanation:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
    security
        // Disable frame options to allow embedded content (like H2 console)
        .headers(x -> x.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
        
        // Disable CSRF protection for simplicity (useful for non-browser clients or testing)
        .csrf(csrf -> csrf.disable())
        
        // Disable the default form login (since we're using basic authentication)
        .formLogin(AbstractHttpConfigurer::disable)
        
        // Allow access to "/public/**" and "/auth/**" URLs without authentication
        .authorizeHttpRequests(x -> x.requestMatchers("/public/**", "/auth/**").permitAll())
        
        // Require authentication for all other requests
        .authorizeHttpRequests(x -> x.anyRequest().authenticated())
        
        // Enable basic authentication
        .httpBasic(Customizer.withDefaults());
    
    // Build and return the configured security object
    return security.build();
}
```

#### Detailed Breakdown:

1. **`headers(x -> x.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))`**:
   - Disables the default `X-Frame-Options` header, which prevents embedding content in a frame (useful when using the H2 database console or similar embedded content).
   
2. **`csrf(csrf -> csrf.disable())`**:
   - Disables Cross-Site Request Forgery (CSRF) protection. This is typically used in APIs where CSRF isn't a concern, or in testing environments. For production systems handling browser requests, enabling CSRF is recommended.

3. **`formLogin(AbstractHttpConfigurer::disable)`**:
   - Disables the default Spring Security form login page. Instead, we are configuring HTTP Basic authentication, which requires credentials to be passed in the request headers.

4. **`.authorizeHttpRequests(x -> x.requestMatchers("/public/**", "/auth/**").permitAll())`**:
   - Allows all users to access any endpoints that match `/public/**` or `/auth/**` without needing to be authenticated.

5. **`.authorizeHttpRequests(x -> x.anyRequest().authenticated())`**:
   - Requires that all other requests (those not under `/public/**` or `/auth/**`) must be authenticated. Users must provide credentials to access these routes.

6. **`.httpBasic(Customizer.withDefaults())`**:
   - Enables HTTP Basic Authentication, where the client sends the username and password with each request via the `Authorization` header.

This setup is useful for a simple API or application that doesn't require complex authentication mechanisms but still enforces basic security measures.

