package com.example.demo1;

import com.example.demo1.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Đánh dấu lớp này là một lớp cấu hình cho Spring Context.
@EnableWebSecurity // Kích hoạt tính năng bảo mật web của Spring Security.
@RequiredArgsConstructor // Lombok tự động tạo constructor có tham số cho tất cả các trường final.
public class SecurityConfig {

        private final UserService userService; // Tiêm UserService vào lớp cấu hình này.
        private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        @Bean
        public UserDetailsService userDetailsService() {
                return userService;
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
                auth.setUserDetailsService(userDetailsService());
                auth.setPasswordEncoder(passwordEncoder);
                return auth;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http) throws Exception {
                return http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/", "/oauth/**",
                                                                "/register", "/error",
                                                                "/cart", "/cart/**", "/api/**", "/order/momo-return",
                                                                "/order/momo-notify")
                                                .permitAll() // Cho phép truy cập không cần xác thực.
                                                .requestMatchers("/reward/**")
                                                .authenticated() // Cho phép tất cả người dùng đã đăng nhập truy cập trang đổi điểm.
                                                .requestMatchers("/product/add/**", "/product/edit/**", "/product/update/**",
                                                                "/products/add/**", "/products/edit/**", "/products/update/**")
                                                .hasAnyAuthority("ADMIN", "MANAGER") // ADMIN và MANAGER quản lý sản phẩm.
                                                .requestMatchers("/product/delete/**", "/products/delete/**")
                                                .hasAnyAuthority("ADMIN") // Chỉ ADMIN mới có quyền xóa sản phẩm.
                                                .requestMatchers("/categories/add/**", "/categories/edit/**",
                                                                "/categories/delete/**", "/categories/update/**",
                                                                "/admin/categories/**")
                                                .hasAnyAuthority("ADMIN") // Chỉ ADMIN mới có quyền quản lý danh mục.
                                                .requestMatchers("/product/detail/**", "/products/detail/**", "/categories")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/**")
                                                .permitAll() // API GET mở cho mọi người dùng.
                                                .requestMatchers("/api/products/**")
                                                .hasAnyAuthority("ADMIN", "MANAGER") 
                                                .requestMatchers("/api/**")
                                                .hasAnyAuthority("ADMIN") // Các API khác dành cho ADMIN.
                                                .anyRequest().authenticated() 
                                )
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/cart/quantity", "/cart/**", "/api/**",
                                                "/order/momo-notify", "/reward/**"))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login") 
                                                .deleteCookies("JSESSIONID") 
                                                .invalidateHttpSession(true) 
                                                .clearAuthentication(true) 
                                                .permitAll())
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login") 
                                                .loginProcessingUrl("/login") 
                                                .defaultSuccessUrl("/") 
                                                .failureUrl("/login?error") 
                                                .permitAll())
                                .rememberMe(rememberMe -> rememberMe
                                                .key("hutech")
                                                .rememberMeCookieName("hutech")
                                                .tokenValiditySeconds(24 * 60 * 60) 
                                                .userDetailsService(userDetailsService()))
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .accessDeniedPage("/403") 
                                )
                                .sessionManagement(sessionManagement -> sessionManagement
                                                .maximumSessions(1) 
                                                .expiredUrl("/login") 
                                )
                                .httpBasic(httpBasic -> httpBasic
                                                .realmName("hutech") 
                                )
                                .build(); 
        }
}
