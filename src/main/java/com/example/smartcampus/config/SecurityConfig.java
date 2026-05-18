package com.example.smartcampus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.smartcampus.security.CustomAccessDeniedHandler;
import com.example.smartcampus.security.CustomAuthEntryPoint;
import com.example.smartcampus.security.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthEntryPoint authEntryPoint;

    public SecurityConfig(@Lazy JwtAuthFilter jwtAuthFilter,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomAuthEntryPoint authEntryPoint) {
        this.jwtAuthFilter   = jwtAuthFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authEntryPoint  = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/test").permitAll()
                .requestMatchers("/api/auth/me").authenticated()
                // Reset password
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/validate-reset-token").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/me").authenticated()


                // Usuarios
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/users").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET,  "/api/users").permitAll()

                // Carreras
                .requestMatchers(HttpMethod.GET, "/api/careers").permitAll()

                // Ubicaciones
                .requestMatchers(HttpMethod.GET, "/api/locations").permitAll()

                // ── FAVORITOS  ─────────────
                .requestMatchers(HttpMethod.GET,
                    "/api/news/favorites",
                    "/api/news/favorites/**")
                    .hasAnyRole("ESTUDIANTE", "PUBLICADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST,
                    "/api/news/favorites/**")
                    .hasAnyRole("ESTUDIANTE", "PUBLICADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/news/favorites/**")
                    .hasAnyRole("ESTUDIANTE", "PUBLICADOR", "ADMINISTRADOR")

                // ── Noticias  
                .requestMatchers(HttpMethod.GET, "/api/news").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/news/**").permitAll()

                // ── Noticias  solo PUBLICADOR 
                .requestMatchers(HttpMethod.POST,   "/api/news").hasRole("PUBLICADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/news/**").hasRole("PUBLICADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/news/**").hasRole("PUBLICADOR")

                // Subida de archivos
                .requestMatchers(HttpMethod.POST, "/api/files/**").permitAll()

                // Perfil
                .requestMatchers("/api/profile/**").hasAnyRole("ESTUDIANTE", "PUBLICADOR", "ADMINISTRADOR")

                // Eventos — lectura pública
                .requestMatchers(HttpMethod.GET, "/api/events").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                // Inscripción/cancelación de eventos (usuarios autenticados)
                .requestMatchers(HttpMethod.POST, "/api/events/*/register").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/events/*/register").authenticated()
                // Eventos — escritura solo PUBLICADOR (POST en raíz, PUT y DELETE en específicos)
                .requestMatchers(HttpMethod.POST,   "/api/events").hasRole("PUBLICADOR")
                .requestMatchers(HttpMethod.PUT,    "/api/events/*").hasRole("PUBLICADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/events/*").hasRole("PUBLICADOR")

                // Reclamos — estudiante
                .requestMatchers("/api/complaints/**").authenticated()
                
                // Reclamos — admin
                .requestMatchers(HttpMethod.GET,   "/api/admin/complaints/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/admin/complaints/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST,  "/api/admin/complaints/**").hasRole("ADMINISTRADOR")

                // Dashboard — admin
                .requestMatchers(HttpMethod.GET, "/api/dashboard/admin").hasRole("ADMINISTRADOR")

                // Categorías
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()

                // ── NUEVO: Logs de acceso — solo ADMINISTRADOR ────────────────
                .requestMatchers(HttpMethod.GET, "/api/access-logs").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/users/*/status").hasRole("ADMINISTRADOR")
                // Error
                .requestMatchers("/error").permitAll()

                // Todo lo demás autenticado
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authEntryPoint)
            )
            .httpBasic(h -> h.disable())
            .formLogin(f -> f.disable())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}