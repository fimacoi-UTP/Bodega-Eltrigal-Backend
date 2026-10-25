package com.eltrigal.backend.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Ean13UtilTest {

    @Test
    void testGenerarEan13FormulaDispersa() {
        // Base 1:
        // cuerpo = 100000000L + (1 * 2654435761L % 900000000L) = 100000000 + 854435761 = 954435761
        // base12 = "775954435761" -> verificador = 5 -> "7759544357615"
        String barcode1 = Ean13Util.generar(1L);
        assertEquals("7759544357615", barcode1);
        assertTrue(Ean13Util.esValido(barcode1));

        // Verificar que los primeros 21 productos generan códigos válidos, únicos y sin cadenas de ceros
        Set<String> codigos = new HashSet<>();
        for (long id = 1; id <= 21; id++) {
            String codigo = Ean13Util.generar(id);

            // Estructura y validez
            assertEquals(13, codigo.length(), "Debe tener 13 caracteres para ID " + id);
            assertTrue(codigo.startsWith("775"), "Debe comenzar con el prefijo de Perú 775 para ID " + id);
            assertTrue(Ean13Util.esValido(codigo), "Debe ser un EAN-13 válido con checksum correcto para ID " + id);

            // Realismo: no debe contener bloques de ceros consecutivos ("0000")
            assertFalse(codigo.contains("0000"), "El código no debe contener bloques de ceros: " + codigo);

            // Unicidad
            assertTrue(codigos.add(codigo), "No debe haber duplicados entre los IDs generados: " + codigo);
        }
    }

    @Test
    void testEsValido() {
        assertTrue(Ean13Util.esValido("7759544357615"));
        assertFalse(Ean13Util.esValido("7759544357614")); // Dígito verificador incorrecto
        assertFalse(Ean13Util.esValido("77595443576"));   // Menos de 13 dígitos
        assertFalse(Ean13Util.esValido("775954435761A")); // Carácter alfabético
        assertFalse(Ean13Util.esValido(null));
    }

}
