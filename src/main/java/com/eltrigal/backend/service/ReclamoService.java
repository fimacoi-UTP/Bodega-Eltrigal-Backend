package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.ReclamoEstadoRequest;
import com.eltrigal.backend.dto.ReclamoRequest;
import com.eltrigal.backend.dto.ReclamoResponse;
import com.eltrigal.backend.entity.EstadoReclamo;
import com.eltrigal.backend.entity.Reclamo;
import com.eltrigal.backend.entity.TipoDocumento;
import com.eltrigal.backend.entity.TipoRespuesta;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.ReclamoRepository;
import com.eltrigal.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de negocio para la gestión del Libro de Reclamaciones (Indecopi).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReclamoService {

    private final ReclamoRepository reclamoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registra un nuevo reclamo o queja (endpoint público).
     * Calcula fecha límite en 15 días hábiles y genera el número correlativo con formato REC-YYYY-XXXXXX.
     */
    @Transactional
    public ReclamoResponse crear(ReclamoRequest req) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaLimite = calcularFechaLimiteHabiles(ahora, 15);

        TipoDocumento tipoDoc = req.getTipoDocumento() != null ? req.getTipoDocumento() : TipoDocumento.DNI;
        TipoRespuesta tipoResp = req.getTipoRespuesta() != null ? req.getTipoRespuesta() : TipoRespuesta.CORREO;
        boolean menorDeEdad = Boolean.TRUE.equals(req.getMenorDeEdad());

        Reclamo reclamo = Reclamo.builder()
                .nombres(req.getNombres().trim())
                .apellidoPaterno(req.getApellidoPaterno().trim())
                .apellidoMaterno(req.getApellidoMaterno() != null ? req.getApellidoMaterno().trim() : null)
                .tipoDocumento(tipoDoc)
                .numeroDocumento(req.getNumeroDocumento().trim())
                .telefono(req.getTelefono() != null ? req.getTelefono().trim() : null)
                .tipoRespuesta(tipoResp)
                .correo(req.getCorreo().trim())
                .direccion(req.getDireccion() != null ? req.getDireccion().trim() : null)
                .urbanizacion(req.getUrbanizacion() != null ? req.getUrbanizacion().trim() : null)
                .departamento(req.getDepartamento() != null ? req.getDepartamento().trim() : null)
                .provincia(req.getProvincia() != null ? req.getProvincia().trim() : null)
                .distrito(req.getDistrito() != null ? req.getDistrito().trim() : null)
                .menorDeEdad(menorDeEdad)
                .canalCompra(req.getCanalCompra())
                .motivo(req.getMotivo())
                .relacionadoA(req.getRelacionadoA())
                .numeroPedido(req.getNumeroPedido() != null ? req.getNumeroPedido().trim() : null)
                .descripcionBien(req.getDescripcionBien() != null ? req.getDescripcionBien().trim() : null)
                .detalle(req.getDetalle().trim())
                .requerimiento(req.getRequerimiento().trim())
                .montoReclamado(req.getMontoReclamado())
                .estado(EstadoReclamo.PENDIENTE)
                .fechaCreacion(ahora)
                .fechaLimiteRespuesta(fechaLimite)
                .build();

        // Si el usuario está autenticado, asociamos su usuarioId
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            usuarioRepository.findByEmail(auth.getName()).ifPresent(u -> reclamo.setUsuarioId(u.getId()));
        }

        // Primer guardado para obtener el ID generado por la BD
        Reclamo guardado = reclamoRepository.save(reclamo);

        // Generar correlativo REC-YYYY-000000
        int anio = guardado.getFechaCreacion().getYear();
        String correlativo = String.format("REC-%d-%06d", anio, guardado.getId());
        guardado.setNumeroCorrelativo(correlativo);

        // Segundo guardado con correlativo
        guardado = reclamoRepository.save(guardado);
        log.info("Reclamo creado exitosamente con correlativo: {}", correlativo);

        return ReclamoResponse.fromEntity(guardado);
    }

    /**
     * Lista los reclamos registrados, con filtro opcional por estado (solo ADMIN).
     */
    @Transactional(readOnly = true)
    public List<ReclamoResponse> listar(EstadoReclamo estado) {
        List<Reclamo> lista = (estado != null)
                ? reclamoRepository.findByEstadoOrderByFechaCreacionDesc(estado)
                : reclamoRepository.findAllByOrderByFechaCreacionDesc();
        return lista.stream().map(ReclamoResponse::fromEntity).toList();
    }

    /**
     * Obtiene el detalle de un reclamo por su ID (solo ADMIN).
     */
    @Transactional(readOnly = true)
    public ReclamoResponse obtenerPorId(Long id) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo no encontrado con id: " + id));
        return ReclamoResponse.fromEntity(reclamo);
    }

    /**
     * Actualiza el estado y las acciones tomadas de un reclamo (solo ADMIN).
     */
    @Transactional
    public ReclamoResponse actualizarEstado(Long id, ReclamoEstadoRequest req) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamo no encontrado con id: " + id));

        reclamo.setEstado(req.getEstado());
        if (req.getAccionesTomadas() != null) {
            reclamo.setAccionesTomadas(req.getAccionesTomadas().trim());
        }

        Reclamo actualizado = reclamoRepository.save(reclamo);
        log.info("Reclamo ID {} actualizado a estado: {}", id, actualizado.getEstado());
        return ReclamoResponse.fromEntity(actualizado);
    }

    /**
     * Calcula una fecha límite sumando días hábiles (excluyendo sábados y domingos).
     */
    public LocalDateTime calcularFechaLimiteHabiles(LocalDateTime inicio, int diasHabiles) {
        if (inicio == null) {
            inicio = LocalDateTime.now();
        }
        LocalDateTime actual = inicio;
        int agregados = 0;
        while (agregados < diasHabiles) {
            actual = actual.plusDays(1);
            DayOfWeek dow = actual.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                agregados++;
            }
        }
        return actual;
    }
}

