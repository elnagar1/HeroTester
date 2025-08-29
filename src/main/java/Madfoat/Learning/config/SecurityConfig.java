package Madfoat.Learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // You'll need to inject your custom UserDetailsService here later
    // private UserDetailsService userDetailsService;

    // @Autowired
    // public SecurityConfig(UserDetailsService userDetailsService) {
    //     this.userDetailsService = userDetailsService;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll() // Allow access to registration, login, and static resources
                .anyRequest().authenticated() // All other requests require authentication
            )
            .formLogin(form -> form
                .loginPage("/login") // Specify custom login page
                .defaultSuccessUrl("/", true) // Redirect to home page after successful login
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout") // Redirect to login page after logout
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Temporarily disable CSRF for easier testing, enable with proper handling in production

        return http.build();
    }

    // You'll implement a custom UserDetailsService here or in a separate class later
    // @Bean
    // public UserDetailsService userDetailsService() {
    //     // For now, return a dummy user. This will be replaced by your database integration.
    //     return username -> {
    //         if ("user".equals(username)) {
    //             return org.springframework.security.core.userdetails.User.withUsername("user")
    //                     .password(passwordEncoder().encode("password"))
    //                     .roles("USER")
    //                     .build();
    //         }
    //         throw new UsernameNotFoundException("User not found.");
    //     };
    // }
}
