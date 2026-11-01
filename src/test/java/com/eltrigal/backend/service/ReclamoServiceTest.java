package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.ReclamoEstadoRequest;
import com.eltrigal.backend.dto.ReclamoRequest;
import com.eltrigal.backend.dto.ReclamoResponse;
import com.eltrigal.backend.entity.CanalCompra;
import com.eltrigal.backend.entity.EstadoReclamo;
import com.eltrigal.backend.entity.Motivo;
import com.eltrigal.backend.entity.Reclamo;
import com.eltrigal.backend.entity.RelacionadoA;
import com.eltrigal.backend.entity.TipoDocumento;
import com.eltrigal.backend.entity.TipoRespuesta;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.ReclamoRepository;
import com.eltrigal.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamoServiceTest {

    @Mock
    private ReclamoRepository reclamoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReclamoService reclamoService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe calcular exactamente 15 días hábiles sin contar sábados ni domingos")
    void testCalcularFechaLimiteHabiles() {
        // Lunes 20 de Octubre a las 10:00
        LocalDateTime inicioLunes = LocalDateTime.of(2026, 10, 19, 10, 0);
        LocalDateTime resultado = reclamoService.calcularFechaLimiteHabiles(inicioLunes, 15);

        // 15 días hábiles = 3 semanas exactas (lunes 19 + 3 semanas = lunes 9 de Noviembre)
        assertEquals(LocalDateTime.of(2026, 11, 9, 10, 0), resultado);
        assertEquals(DayOfWeek.MONDAY, resultado.getDayOfWeek());
    }

    @Test
    @DisplayName("Debe crear reclamo correctamente con número correlativo y estado PENDIENTE")
    void testCrearReclamo() {
        ReclamoRequest req = ReclamoRequest.builder()
                .nombres("Juan Carlos")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Gómez")
                .tipoDocumento(TipoDocumento.DNI)
                .numeroDocumento("71234567")
                .telefono("987654321")
                .tipoRespuesta(TipoRespuesta.CORREO)
                .correo("juan.perez@example.com")
                .direccion("Av. Grau 123")
                .urbanizacion("Miraflores")
                .departamento("Piura")
                .provincia("Piura")
                .distrito("Piura")
                .menorDeEdad(false)
                .canalCompra(CanalCompra.LOCAL_PIURA)
                .motivo(Motivo.RECLAMO)
                .relacionadoA(RelacionadoA.PRODUCTO)
                .numeroPedido("PED-2026-0001")
                .descripcionBien("Leche Gloria 1L")
                .detalle("El producto estaba vencido al momento de la entrega.")
                .requerimiento("Cambio de producto o reembolso inmediato.")
                .montoReclamado(new BigDecimal("5.50"))
                .build();

        Reclamo mockGuardado1 = Reclamo.builder()
                .id(42L)
                .fechaCreacion(LocalDateTime.of(2026, 10, 29, 15, 30))
                .nombres("Juan Carlos")
                .apellidoPaterno("Pérez")
                .estado(EstadoReclamo.PENDIENTE)
                .build();

        when(reclamoRepository.save(any(Reclamo.class))).thenAnswer(invocation -> {
            Reclamo r = invocation.getArgument(0);
            if (r.getId() == null) {
                r.setId(42L);
            }
            return r;
        });

        ReclamoResponse res = reclamoService.crear(req);

        assertNotNull(res);
        assertEquals("REC-2026-000042", res.getNumeroCorrelativo());
        assertEquals(EstadoReclamo.PENDIENTE, res.getEstado());
        verify(reclamoRepository, times(2)).save(any(Reclamo.class));
    }

    @Test
    @DisplayName("Debe listar reclamos filtrando por estado si se especifica")
    void testListarConFiltro() {
        Reclamo r = Reclamo.builder()
                .id(1L)
                .numeroCorrelativo("REC-2026-000001")
                .nombres("Ana")
                .apellidoPaterno("Torres")
                .estado(EstadoReclamo.PENDIENTE)
                .build();

        when(reclamoRepository.findByEstadoOrderByFechaCreacionDesc(EstadoReclamo.PENDIENTE))
                .thenReturn(List.of(r));

        List<ReclamoResponse> lista = reclamoService.listar(EstadoReclamo.PENDIENTE);
        assertEquals(1, lista.size());
        assertEquals("REC-2026-000001", lista.get(0).getNumeroCorrelativo());
    }

    @Test
    @DisplayName("Debe actualizar el estado del reclamo y registrar las acciones tomadas")
    void testActualizarEstado() {
        Reclamo r = Reclamo.builder()
                .id(10L)
                .numeroCorrelativo("REC-2026-000010")
                .estado(EstadoReclamo.PENDIENTE)
                .build();

        when(reclamoRepository.findById(10L)).thenReturn(Optional.of(r));
        when(reclamoRepository.save(any(Reclamo.class))).thenAnswer(i -> i.getArgument(0));

        ReclamoEstadoRequest req = ReclamoEstadoRequest.builder()
                .estado(EstadoReclamo.RESUELTO)
                .accionesTomadas("Se contactó al cliente y se emitió la nota de crédito.")
                .build();

        ReclamoResponse res = reclamoService.actualizarEstado(10L, req);

        assertEquals(EstadoReclamo.RESUELTO, res.getEstado());
        assertEquals("Se contactó al cliente y se emitió la nota de crédito.", res.getAccionesTomadas());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el reclamo no existe")
    void testReclamoNoExiste() {
        when(reclamoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reclamoService.obtenerPorId(999L));
    }
}

