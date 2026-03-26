package com.example.demo1.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, unique = true)
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 1, max = 50, message = "Tên đăng nhập phải từ 1 đến 50 ký tự")
    private String username;

    @Column(name = "password", length = 250)
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Column(name = "email", length = 50, unique = true)
    @NotBlank(message = "Email không được để trống")
    @Size(min = 1, max = 50, message = "Email phải từ 1 đến 50 ký tự")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Column(name = "phone", length = 10, unique = true)
    @Length(min = 10, max = 10, message = "Số điện thoại phải đúng 10 chữ số")
    @Pattern(regexp = "^[0-9]*$", message = "Số điện thoại chỉ được chứa chữ số")
    private String phone;
    @Column(name = "provider", length = 50)
    private String provider;

    @Builder.Default
    private double redeemedVipPoints = 0.0;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> userRoles = this.getRoles();
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }
    @Override
    public String getPassword() {
        return password;
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;

        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}