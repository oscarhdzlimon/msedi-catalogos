package mx.gob.imss.edi.catalogos.security;

public record JwtPrincipal(
        String subject,
        String jti,
        Long idTransaccion,
        String cveTransaccion,
        String sistemaOrigen,
        String user) {
}
