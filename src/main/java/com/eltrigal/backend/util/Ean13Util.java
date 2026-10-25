package com.eltrigal.backend.util;

/**
 * Utilidad para la generación y validación de códigos de barras bajo el estándar internacional EAN-13.
 * Utiliza el prefijo oficial 775 asignado a Perú.
 */
public final class Ean13Util {

    private static final String PREFIJO_PERU = "775";

    private Ean13Util() {
        // Clase de utilería, no instanciable
    }

    /**
     * Genera un código de barras EAN-13 válido de 13 dígitos a partir de un identificador base.
     * Estructura: "775" + LPAD(base, 9, '0') + dígito verificador.
     *
     * @param base Identificador numérico (ej. id del producto)
     * @return Cadena de 13 dígitos numéricos con checksum válido
     */
    public static String generar(long base) {
        long cuerpo = 100000000L + Math.floorMod(base * 2654435761L, 900000000L);
        String base12 = PREFIJO_PERU + cuerpo;
        int verificador = calcularDigitoVerificador(base12);
        return base12 + verificador;
    }

    /**
     * Calcula el dígito verificador para una cadena base de 12 dígitos.
     * Algoritmo EAN-13: suma = (posiciones impares × 1) + (posiciones pares × 3).
     * verificador = (10 - (suma % 10)) % 10.
     *
     * @param base12 Los primeros 12 dígitos del código
     * @return Dígito verificador entre 0 y 9
     */
    public static int calcularDigitoVerificador(String base12) {
        if (base12 == null || base12.length() != 12) {
            throw new IllegalArgumentException("La base para el cálculo de EAN-13 debe tener exactamente 12 dígitos");
        }

        int suma = 0;
        for (int i = 0; i < 12; i++) {
            char c = base12.charAt(i);
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("La base debe contener únicamente dígitos numéricos: " + base12);
            }
            int digito = c - '0';
            // 1-indexado: i=0 es posición 1 (impar -> ×1), i=1 es posición 2 (par -> ×3)
            suma += (i % 2 == 0) ? digito : digito * 3;
        }

        return (10 - (suma % 10)) % 10;
    }

    /**
     * Valida que una cadena sea un código EAN-13 formalmente válido (13 dígitos y dígito verificador correcto).
     *
     * @param codigo Cadena a validar
     * @return true si cumple con el estándar EAN-13
     */
    public static boolean esValido(String codigo) {
        if (codigo == null || codigo.length() != 13 || !codigo.matches("^\\d{13}$")) {
            return false;
        }

        String base12 = codigo.substring(0, 12);
        int verificadorEsperado = calcularDigitoVerificador(base12);
        int verificadorReal = codigo.charAt(12) - '0';

        return verificadorEsperado == verificadorReal;
    }

}
