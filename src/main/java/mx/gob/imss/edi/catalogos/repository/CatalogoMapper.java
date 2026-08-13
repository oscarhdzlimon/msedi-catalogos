package mx.gob.imss.edi.catalogos.repository;

import java.util.List;
import mx.gob.imss.edi.catalogos.models.dto.CatalogoItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CatalogoMapper {

    @Select("""
            select id_clasif_incapacidad as id,
                   null::varchar as clave,
                   des_clasif_incapacidad as descripcion,
                   num_orden as orden,
                   null::bigint as id_padre,
                   null::varchar as referencia
              from catalogo.clasif_incapacidad
             where ind_activo = true
             order by num_orden, id_clasif_incapacidad
            """)
    List<CatalogoItemDto> consultarClasificacionesIncapacidad();

    @Select("""
            select id_estatus as id,
                   null::varchar as clave,
                   des_estatus as descripcion,
                   null::integer as orden,
                   id_tipo_estatus as id_padre,
                   null::varchar as referencia
              from catalogo.estatus
             where ind_activo = true
             order by des_estatus, id_estatus
            """)
    List<CatalogoItemDto> consultarEstatus();

    @Select("""
            select id_perfil as id,
                   cve_perfil as clave,
                   des_perfil as descripcion,
                   null::integer as orden,
                   null::bigint as id_padre,
                   null::varchar as referencia
              from catalogo.perfil
             where ind_activo = true
             order by des_perfil, id_perfil
            """)
    List<CatalogoItemDto> consultarPerfiles();

    @Select("""
            select id_ramo_seguro as id,
                   cve_ramo_seguro as clave,
                   des_ramo_seguro as descripcion,
                   num_orden as orden,
                   null::bigint as id_padre,
                   null::varchar as referencia
              from catalogo.ramo_seguro
             where ind_activo = true
             order by num_orden, id_ramo_seguro
            """)
    List<CatalogoItemDto> consultarRamosSeguro();

    @Select("""
            select id_tipo_documento as id,
                   null::varchar as clave,
                   des_tipo_documento as descripcion,
                   null::integer as orden,
                   null::bigint as id_padre,
                   ref_formato as referencia
              from catalogo.tipo_documento
             where ind_activo = true
             order by des_tipo_documento, id_tipo_documento
            """)
    List<CatalogoItemDto> consultarTiposDocumento();

    @Select("""
            select id_tipo_estatus as id,
                   null::varchar as clave,
                   des_tipo_estatus as descripcion,
                   null::integer as orden,
                   null::bigint as id_padre,
                   null::varchar as referencia
              from catalogo.tipo_estatus
             where ind_activo = true
             order by des_tipo_estatus, id_tipo_estatus
            """)
    List<CatalogoItemDto> consultarTiposEstatus();

    @Select("""
            select id_tipo_identificacion as id,
                   null::varchar as clave,
                   des_tipo_identificacion as descripcion,
                   num_orden as orden,
                   null::bigint as id_padre,
                   null::varchar as referencia
              from catalogo.tipo_identificacion
             where ind_activo = true
             order by num_orden, id_tipo_identificacion
            """)
    List<CatalogoItemDto> consultarTiposIdentificacion();

    @Select("""
            select id_tipo_incapacidad as id,
                   null::varchar as clave,
                   des_tipo_incapacidad as descripcion,
                   num_orden as orden,
                   null::bigint as id_padre,
                   null::varchar as referencia
              from catalogo.tipo_incapacidad
             where ind_activo = true
             order by num_orden, id_tipo_incapacidad
            """)
    List<CatalogoItemDto> consultarTiposIncapacidad();

    @Select("""
            select id_tipo_riesgo as id,
                   null::varchar as clave,
                   des_tipo_riesgo as descripcion,
                   num_orden as orden,
                   null::bigint as id_padre,
                   null::varchar as referencia
              from catalogo.tipo_riesgo
             where ind_activo = true
             order by num_orden, id_tipo_riesgo
            """)
    List<CatalogoItemDto> consultarTiposRiesgo();
}
