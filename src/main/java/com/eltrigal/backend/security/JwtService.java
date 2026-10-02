package com.eltrigal.backend.security;

import com.eltrigal.backend.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio encargado de la generación, extracción y validación de tokens JWT.
 * Utiliza la API moderna de jjwt 0.12.x.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Genera la clave criptográfica para firma HMAC-SHA a partir del secreto configurado.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un token JWT para un usuario con su email como subject y el rol como claim.
     *
     * @param usuario Entidad del usuario autenticado
     * @return Token JWT firmado
     */
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + expiration);

        String nombreRol = (usuario.getRol() != null && usuario.getRol().getNombre() != null)
                ? usuario.getRol().getNombre()
                : "";

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("rol", nombreRol)
                .issuedAt(ahora)
                .expiration(fechaExpiracion)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el email (subject) del token JWT.
     *
     * @param token Token JWT
     * @return Email contenido en el subject
     */
    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    /**
     * Valida si el token es estructuralmente válido, tiene firma correcta y no ha expirado.
     *
     * @param token Token JWT
     * @return true si el token es válido, false en caso contrario
     */
    public boolean esValido(String token) {
        try {
            Claims claims = extraerClaims(token);
            return claims.getExpiration() != null && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parsea y extrae el payload (Claims) del token JWT verificado.
     */
    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
