package com.eltrigal.backend.service;

import com.eltrigal.backend.dto.PromocionRequest;
import com.eltrigal.backend.dto.PromocionResponse;
import com.eltrigal.backend.entity.Categoria;
import com.eltrigal.backend.entity.Producto;
import com.eltrigal.backend.entity.Promocion;
import com.eltrigal.backend.exception.BadRequestException;
import com.eltrigal.backend.exception.ResourceNotFoundException;
import com.eltrigal.backend.repository.CategoriaRepository;
import com.eltrigal.backend.repository.ProductoRepository;
import com.eltrigal.backend.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio que contiene la lógica de negocio para la gestión de promociones M:N
 * y el cálculo de precios finales con soporte para promociones acumulables y no
 * acumulables.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PromocionService {

    private static final Set<String> TIPOS_VALIDOS = Set.of("PORCENTAJE", "MONTO_FIJO", "DOS_X_UNO", "COMBO");

    private final PromocionRepository promocionRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * Lista todas las promociones registradas en el sistema ordenadas por prioridad
     * e id.
     *
     * @return Lista de DTO PromocionResponse
     */
    @Transactional(readOnly = true)
    public List<PromocionResponse> listar() {
        return promocionRepository.findAllByOrderByPrioridadAscIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene las entidades Promocion activas y vigentes en la fecha actual.
     *
     * @return Lista de entidades Promocion
     */
    @Transactional(readOnly = true)
    public List<Promocion> obtenerVigentes() {
        return promocionRepository.findVigentes(LocalDate.now());
    }

    /**
     * Obtiene las promociones vigentes al día de hoy en formato DTO.
     *
     * @return Lista de DTO PromocionResponse
     */
    @Transactional(readOnly = true)
    public List<PromocionResponse> obtenerVigentesResponse() {
        return obtenerVigentes()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Alias compatible para obtener entidades vigentes.
     */
    @Transactional(readOnly = true)
    public List<Promocion> obtenerEntidadesVigentes() {
        return obtenerVigentes();
    }

    /**
     * Registra una nueva promoción en el sistema validando tipo, valor y
     * asociaciones M:N.
     *
     * @param req Datos de la promoción
     * @return DTO PromocionResponse creado
     */
    public PromocionResponse crear(PromocionRequest req) {
        String tipoNorm = validarTipo(req.getTipo());
        BigDecimal valor = validarValor(tipoNorm, req.getValor());

        boolean aplicaTodos = Boolean.TRUE.equals(req.getAplicaTodos());
        Set<Producto> productos = resolverProductos(req.getProductoIds());
        Set<Categoria> categorias = resolverCategorias(req.getCategoriaIds());

        if (!aplicaTodos && productos.isEmpty() && categorias.isEmpty()) {
            throw new BadRequestException(
                    "Debe asociar al menos un producto o categoría si la promoción no aplica a todos");
        }

        Promocion promo = new Promocion();
        promo.setNombre(req.getNombre().trim());
        promo.setDescripcion(req.getDescripcion());
        promo.setTipo(tipoNorm);
        promo.setValor(valor);
        promo.setAplicaTodos(aplicaTodos);
        promo.setPrioridad(req.getPrioridad() != null ? req.getPrioridad() : 0);
        promo.setEsAcumulable(req.getEsAcumulable() != null ? req.getEsAcumulable() : true);
        promo.setFechaInicio(req.getFechaInicio());
        promo.setFechaFin(req.getFechaFin());
        promo.setActivo(req.getActivo() != null ? req.getActivo() : true);
        promo.setProductos(productos);
        promo.setCategorias(categorias);

        Promocion guardada = promocionRepository.save(promo);
        return toResponse(guardada);
    }

    /**
     * Actualiza una promoción existente.
     *
     * @param id  Identificador de la promoción
     * @param req Datos actualizados
     * @return DTO PromocionResponse actualizado
     */
    public PromocionResponse actualizar(Long id, PromocionRequest req) {
        Promocion promo = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));

        String tipoNorm = validarTipo(req.getTipo());
        BigDecimal valor = validarValor(tipoNorm, req.getValor());

        boolean aplicaTodos = req.getAplicaTodos() != null ? req.getAplicaTodos()
                : Boolean.TRUE.equals(promo.getAplicaTodos());
        Set<Producto> productos = resolverProductos(req.getProductoIds());
        Set<Categoria> categorias = resolverCategorias(req.getCategoriaIds());

        if (!aplicaTodos && productos.isEmpty() && categorias.isEmpty()) {
            throw new BadRequestException(
                    "Debe asociar al menos un producto o categoría si la promoción no aplica a todos");
        }

        promo.setNombre(req.getNombre().trim());
        promo.setDescripcion(req.getDescripcion());
        promo.setTipo(tipoNorm);
        promo.setValor(valor);
        promo.setAplicaTodos(aplicaTodos);
        if (req.getPrioridad() != null) {
            promo.setPrioridad(req.getPrioridad());
        }
        if (req.getEsAcumulable() != null) {
            promo.setEsAcumulable(req.getEsAcumulable());
        }
        promo.setFechaInicio(req.getFechaInicio());
        promo.setFechaFin(req.getFechaFin());
        if (req.getActivo() != null) {
            promo.setActivo(req.getActivo());
        }
        promo.setProductos(productos);
        promo.setCategorias(categorias);

        Promocion actualizada = promocionRepository.save(promo);
        return toResponse(actualizada);
    }

    /**
     * Realiza un soft delete de la promoción cambiando activo a false.
     *
     * @param id Identificador de la promoción
     */
    public void eliminar(Long id) {
        Promocion promo = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        promo.setActivo(false);
        promocionRepository.save(promo);
    }

    /**
     * Calcula el precio final de un producto aplicando acumulativa o selectivamente
     * las promociones vigentes.
     * a) Filtra aplicables (aplicaTodos == true, producto en promo.productos, o
     * categoria en promo.categorias).
     * b) Acumulables: si todas son acumulables -> apila en orden prioridad ASC; si
     * hay alguna no acumulable -> aplica
     * únicamente la de menor prioridad (desempata por menor id).
     * c) Fórmulas exactas según tipo (PORCENTAJE, MONTO_FIJO, DOS_X_UNO, COMBO).
     *
     * @param producto Entidad Producto base
     * @param vigentes Lista de promociones vigentes a evaluar
     * @return BigDecimal con el precio final calculado con escala 2 HALF_UP
     */
    public BigDecimal calcularPrecioFinal(Producto producto, List<Promocion> vigentes) {
        if (producto == null || producto.getPrecio() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal precio = producto.getPrecio();

        if (vigentes == null || vigentes.isEmpty()) {
            return precio.setScale(2, RoundingMode.HALF_UP);
        }

        Long prodId = producto.getId();
        Integer catId = producto.getCategoria() != null ? producto.getCategoria().getId() : null;

        // a) Filtra promociones aplicables al producto
        List<Promocion> aplicables = vigentes.stream()
                .filter(p -> {
                    if (p == null) {
                        return false;
                    }
                    if (Boolean.TRUE.equals(p.getAplicaTodos())) {
                        return true;
                    }
                    if (prodId != null && p.getProductos() != null) {
                        boolean matchProd = p.getProductos().stream()
                                .anyMatch(prod -> prod.getId() != null && prod.getId().equals(prodId));
                        if (matchProd) {
                            return true;
                        }
                    }
                    if (catId != null && p.getCategorias() != null) {
                        boolean matchCat = p.getCategorias().stream()
                                .anyMatch(cat -> cat.getId() != null && cat.getId().equals(catId));
                        if (matchCat) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();

        if (aplicables.isEmpty()) {
            return precio.setScale(2, RoundingMode.HALF_UP);
        }

        Comparator<Promocion> ordenPrioridadEId = Comparator
                .comparing((Promocion p) -> p.getPrioridad() != null ? p.getPrioridad() : 0)
                .thenComparing((Promocion p) -> p.getId() != null ? p.getId() : 0L);

        // b) Determinar promociones a aplicar (acumulables vs no acumulables)
        boolean todasAcumulables = aplicables.stream()
                .allMatch(p -> Boolean.TRUE.equals(p.getEsAcumulable()));

        List<Promocion> aAplicar;
        if (todasAcumulables) {
            aAplicar = aplicables.stream()
                    .sorted(ordenPrioridadEId)
                    .toList();
        } else {
            Promocion seleccionada = aplicables.stream()
                    .min(ordenPrioridadEId)
                    .orElse(null);
            aAplicar = seleccionada != null ? List.of(seleccionada) : List.of();
        }

        // c) Fórmulas de cálculo
        for (Promocion promo : aAplicar) {
            String tipo = promo.getTipo() != null ? promo.getTipo().trim().toUpperCase() : "";
            BigDecimal val = promo.getValor() != null ? promo.getValor() : BigDecimal.ZERO;

            switch (tipo) {
                case "PORCENTAJE" -> {
                    BigDecimal factor = BigDecimal.ONE.subtract(
                            val.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    precio = precio.multiply(factor);
                }
                case "MONTO_FIJO" -> {
                    precio = precio.subtract(val);
                    if (precio.compareTo(BigDecimal.ZERO) < 0) {
                        precio = BigDecimal.ZERO;
                    }
                }
                case "DOS_X_UNO" -> {
                    precio = precio.multiply(new BigDecimal("0.5"));
                }
                case "COMBO" -> {
                    precio = precio.multiply(new BigDecimal("0.8"));
                }
            }
        }

        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            precio = BigDecimal.ZERO;
        }

        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    private String validarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new BadRequestException("El tipo de promoción es obligatorio");
        }
        String tipoNorm = tipo.trim().toUpperCase();
        if (!TIPOS_VALIDOS.contains(tipoNorm)) {
            throw new BadRequestException("Tipo de promoción no válido: '" + tipo + "'. Permitidos: " + TIPOS_VALIDOS);
        }
        return tipoNorm;
    }

    private BigDecimal validarValor(String tipoNorm, BigDecimal valorReq) {
        if ("PORCENTAJE".equals(tipoNorm) || "MONTO_FIJO".equals(tipoNorm)) {
            if (valorReq == null || valorReq.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("El valor debe ser mayor a 0 para promociones de tipo " + tipoNorm);
            }
            return valorReq;
        }
        // Para DOS_X_UNO y COMBO el valor se ignora
        return null;
    }

    private Set<Producto> resolverProductos(List<Long> productoIds) {
        Set<Producto> productos = new HashSet<>();
        if (productoIds != null) {
            for (Long prodId : productoIds) {
                Producto prod = productoRepository.findById(prodId)
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + prodId));
                productos.add(prod);
            }
        }
        return productos;
    }

    private Set<Categoria> resolverCategorias(List<Integer> categoriaIds) {
        Set<Categoria> categorias = new HashSet<>();
        if (categoriaIds != null) {
            for (Integer catId : categoriaIds) {
                Categoria cat = categoriaRepository.findById(catId)
                        .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + catId));
                categorias.add(cat);
            }
        }
        return categorias;
    }

    /**
     * Mapea una entidad Promocion a su DTO PromocionResponse cargando las
     * colecciones dentro de la transacción.
     */
    private PromocionResponse toResponse(Promocion p) {
        List<PromocionResponse.ProductoRef> productos = p.getProductos() != null
                ? p.getProductos().stream()
                        .map(prod -> PromocionResponse.ProductoRef.builder()
                                .id(prod.getId())
                                .nombre(prod.getNombre())
                                .build())
                        .sorted(Comparator.comparing(PromocionResponse.ProductoRef::getId))
                        .toList()
                : List.of();

        List<PromocionResponse.CategoriaRef> categorias = p.getCategorias() != null
                ? p.getCategorias().stream()
                        .map(cat -> PromocionResponse.CategoriaRef.builder()
                                .id(cat.getId())
                                .nombre(cat.getNombre())
                                .build())
                        .sorted(Comparator.comparing(PromocionResponse.CategoriaRef::getId))
                        .toList()
                : List.of();

        return PromocionResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .tipo(p.getTipo())
                .valor(p.getValor())
                .aplicaTodos(p.getAplicaTodos())
                .prioridad(p.getPrioridad())
                .esAcumulable(p.getEsAcumulable())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activo(p.getActivo())
                .creadoEn(p.getCreadoEn())
                .actualizadoEn(p.getActualizadoEn())
                .productos(productos)
                .categorias(categorias)
                .build();
    }

}
