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

## SECTION 2 - BASIC AUTH

### `User` Entity

The `User` class represents a user in the application and is annotated as a JPA entity, mapped to the `users` table in the database.

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String username;
    private String password;

    public User() {
    }

    public User(Long id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    // Constructor without id (for new user creation)
    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
    }

    @Override
    public String getPassword() {
        return password ;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

**Explanation**:
- **Annotations**:
   - `@Entity`: Marks the class as a JPA entity.
   - `@Table(name = "users")`: Specifies the table in the database.
   - `@Id`: Marks the `id` field as the primary key.
   - `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Automatically generates the primary key value.

- **Fields**:
   - `id`: Unique identifier for the user.
   - `name`, `username`, `password`: Other user details.

- **Constructors**:
   - No-argument constructor required by Hibernate.
   - Constructors with parameters for initializing fields.

- **Methods**:
   - Implements `UserDetails` for Spring Security, providing user authorities, password, and account status.

---

### `UserDetailsService`

The `UserDetailsService` class is a Spring Security service that loads user-specific data.

```java
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserService userService;

    public UserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userService.getByUsername(username);

        return user.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
```

**Explanation**:
- **Class**:
   - Annotated with `@Service` to designate it as a Spring service component.

- **Constructor**:
   - Takes a `UserService` instance to interact with the user repository.

- **Method**:
   - `loadUserByUsername(String username)`: Fetches user details by username. If the user is not found, it throws `UsernameNotFoundException`. This method is used by Spring Security during authentication to load user-specific data.

---

### `UserService`

The `UserService` class handles operations related to user management, including user creation and retrieval.

```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(CreateUserRequest request) {
        User user = new User(request.name(), request.username(), bCryptPasswordEncoder.encode(request.password()));
        userRepository.save(user);
        return user;
    }
}
```

**Explanation**:
- **Class**:
   - Annotated with `@Service` to indicate that it is a Spring service component.

- **Fields**:
   - `userRepository`: Interface for database operations related to users.
   - `bCryptPasswordEncoder`: Used for encoding user passwords securely.

- **Methods**:
   - `getByUsername(String username)`: Retrieves a user by username from the repository. Returns an `Optional<User>`.
   - `createUser(CreateUserRequest request)`: Creates a new user, encodes the password using `BCryptPasswordEncoder`, and saves the user to the repository.

---

### Basic Authentication Flow

Here’s a high-level overview of how basic authentication works in this application:

1. **User Registration**:
   - The user submits a registration form with their name, username, and password.
   - The `UserService` receives this information, encodes the password, and saves the new user to the database.

2. **User Login**:
   - When the user attempts to log in, Spring Security uses the `UserDetailsService` to fetch user details by username.
   - `UserDetailsService` calls `UserService` to retrieve the user.
   - The user details, including the encoded password, are returned and used to authenticate the user.

3. **Authentication**:
   - Spring Security compares the submitted password with the encoded password stored in the database.
   - If they match, the user is authenticated and granted access to protected resources.

**Diagram**:

```plaintext
+------------------+         +--------------------+         +------------------+
|  User Registration| -----> |     User Service   | -----> |    User Repository |
+------------------+         +--------------------+         +------------------+
                                  |
                                  v
                         +---------------------+
                         |  BCryptPasswordEncoder |
                         +---------------------+

+------------------+         +--------------------+         +------------------+
|    User Login    | -----> | UserDetailsService | -----> |   User Service   |
+------------------+         +--------------------+         +------------------+
                                  |
                                  v
                         +---------------------+
                         |     User Repository    |
                         +---------------------+
```

- **User Registration**: User data is saved with encrypted password.
- **User Login**: User is authenticated using the provided credentials compared against the stored data.

This diagram represents the flow from user registration to login and authentication, demonstrating how the different components interact in the process.
