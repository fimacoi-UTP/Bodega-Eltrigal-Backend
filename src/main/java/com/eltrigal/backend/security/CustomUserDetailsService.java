package com.eltrigal.backend.security;

import com.eltrigal.backend.entity.Usuario;
import com.eltrigal.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de carga de detalles de usuario para la autenticación en Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException("El usuario con email " + email + " se encuentra inactivo");
        }

        String rolNombre = (usuario.getRol() != null && usuario.getRol().getNombre() != null)
                ? usuario.getRol().getNombre().trim().toUpperCase()
                : "CLIENTE";

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rolNombre));

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                authorities
        );
    }

}
