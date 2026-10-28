package mx.gob.imss.edi.catalogos.common.enums;

import java.util.Arrays;
import java.util.Optional;

/** Códigos estables y respaldos locales; los textos de catálogo se resuelven desde base. */
public enum MensajeEdi {
    CATALOGO_NO_DISPONIBLE("CATALOGO_NO_DISPONIBLE", "El catalogo no se encuentra disponible"),
    CATALOGO_NO_SOPORTADO("CATALOGO_NO_SOPORTADO", "El catalogo solicitado no esta soportado"),
    ERROR_INTERNO("ERROR_INTERNO", "Ocurrio un error interno al procesar la solicitud"),
    FRACCION_EMPRESA_INVALIDA("FRACCION_EMPRESA_INVALIDA", "La fraccion de empresa debe ser un entero entre 0 y 9999"),
    TOKEN_INVALIDO_TECNICO("TOKEN_INVALIDO", "El Bearer Token es requerido, invalido o ha expirado"),
    TRANSACCION_INVALIDA("TRANSACCION_INVALIDA", "El header X-Transaccion-Id debe ser numerico"),
    TRANSACCION_NO_CORRESPONDE("TRANSACCION_NO_CORRESPONDE", "La transaccion no corresponde con el Bearer Token"),
    TRANSACCION_REQUERIDA("TRANSACCION_REQUERIDA", "El header X-Transaccion-Id es obligatorio");

    private final String codigo;
    private final String textoRespaldo;

    MensajeEdi(String codigo, String textoRespaldo) {
        this.codigo = codigo;
        this.textoRespaldo = textoRespaldo;
    }

    public String codigo() { return codigo; }
    public String textoRespaldo() { return textoRespaldo; }

    /** Retorna la definición principal; conserva los códigos desconocidos en los contratos dinámicos. */
    public static Optional<MensajeEdi> buscar(String codigo) {
        return Arrays.stream(values()).filter(mensaje -> mensaje.codigo.equals(codigo)).findFirst();
    }
}
