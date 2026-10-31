package co.edu.uts.tecnoturno.config;

import co.edu.uts.tecnoturno.repository.UsuarioRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(UsuarioRepository repo) {
    return email -> {
      var u =
          repo.findByEmail(email.trim().toLowerCase())
              .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
      return User.withUsername(u.getEmail())
          .password(u.getPassword())
          .roles(u.getRol().name())
          .disabled(!u.isActivo())
          .build();
    };
  }

  @Bean
  SecurityFilterChain security(HttpSecurity h) throws Exception {
    h.authorizeHttpRequests(
            a ->
                a.requestMatchers("/login", "/css/**", "/webjars/**", "/error")
                    .permitAll()
                    .requestMatchers("/usuarios/**", "/equipos/**", "/ordenes/nueva")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
        .logout(l -> l.logoutSuccessUrl("/login?logout"))
        .exceptionHandling(e -> e.accessDeniedPage("/denegado"));
    return h.build();
  }
}
