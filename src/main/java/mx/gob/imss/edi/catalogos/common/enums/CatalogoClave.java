package mx.gob.imss.edi.catalogos.common.enums;

import java.util.Arrays;
import java.util.Optional;

public enum CatalogoClave {
    CLASIF_INCAPACIDAD("clasif-incapacidad"),
    ESTATUS("estatus"),
    PERFIL("perfil"),
    RAMO_SEGURO("ramo-seguro"),
    TIPO_DOCUMENTO("tipo-documento"),
    TIPO_ESTATUS("tipo-estatus"),
    TIPO_IDENTIFICACION("tipo-identificacion"),
    TIPO_INCAPACIDAD("tipo-incapacidad"),
    TIPO_RIESGO("tipo-riesgo");

    private final String claveApi;

    CatalogoClave(String claveApi) {
        this.claveApi = claveApi;
    }

    public String getClaveApi() {
        return claveApi;
    }

    public static Optional<CatalogoClave> buscar(String claveApi) {
        return Arrays.stream(values()).filter(value -> value.claveApi.equals(claveApi)).findFirst();
    }
}
