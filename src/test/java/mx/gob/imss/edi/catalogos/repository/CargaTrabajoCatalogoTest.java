package mx.gob.imss.edi.catalogos.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import mx.gob.imss.edi.catalogos.exceptions.EdiException;
import mx.gob.imss.edi.catalogos.services.impl.CatalogoServiceImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class CargaTrabajoCatalogoTest {
    @Test
    void resuelveFraccionConIdsAcordadosYExcluyeInactivosSinCargaYAusentes() throws Exception {
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:carga" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        try (var connection = ds.getConnection(); var sql = connection.createStatement()) {
            sql.execute("create schema catalogo");
            sql.execute("create table catalogo.edic_carga_trabajo(id_carga_trabajo smallint primary key,des_carga_trabajo varchar(30),ind_activo boolean)");
            sql.execute("create table catalogo.edic_actividad_economica(cve_actividad_economica varchar(4) primary key,id_carga_trabajo smallint,ind_activo boolean)");
            sql.execute("insert into catalogo.edic_carga_trabajo values(3,'Pesado',true),(1,'Liviano',true),(2,'Moderado',true)");
            sql.execute("insert into catalogo.edic_actividad_economica values('3510',3,true),('9403',1,true),('9201',2,true),('0',null,true),('101',3,false)");
        }
        var config = new Configuration(new Environment("test", new JdbcTransactionFactory(), ds));
        config.setMapUnderscoreToCamelCase(true);
        config.addMapper(CatalogoMapper.class);
        try (var session = new SqlSessionFactoryBuilder().build(config).openSession()) {
            var service = new CatalogoServiceImpl(session.getMapper(CatalogoMapper.class));
            var resultado = service.consultar("carga-trabajo", "3510");
            assertThat(resultado.claveCatalogo()).isEqualTo("carga-trabajo");
            assertThat(resultado.elementos()).singleElement().satisfies(carga -> {
                assertThat(carga.id()).isEqualTo(3L);
                assertThat(carga.clave()).isEqualTo("3");
                assertThat(carga.descripcion()).isEqualTo("Pesado");
                assertThat(carga.orden()).isEqualTo(3);
                assertThat(carga.referencia()).isEqualTo("3510");
            });
            assertThat(service.consultar("carga-trabajo", "9403").elementos().getFirst().id()).isEqualTo(1L);
            assertThat(service.consultar("carga-trabajo", "9201").elementos().getFirst().id()).isEqualTo(2L);
            for (String fraccion : new String[]{"0", "0000", "9999", "101"}) {
                assertThat(service.consultar("carga-trabajo", fraccion).elementos()).isEmpty();
            }
            session.getConnection().createStatement().execute("update catalogo.edic_carga_trabajo set ind_activo=false where id_carga_trabajo=3");
            session.clearCache();
            assertThat(service.consultar("carga-trabajo", "3510").elementos()).isEmpty();
        }
    }

    @Test
    void exigeFraccionEnteraValidaAntesDeConsultar() {
        var service = new CatalogoServiceImpl(org.mockito.Mockito.mock(CatalogoMapper.class));
        for (String fraccion : new String[]{null, "", "-1", "10000", "3.5", "abc", "3510' or 1=1", " 3510"}) {
            assertThatThrownBy(() -> service.consultar("carga-trabajo", fraccion))
                    .isInstanceOfSatisfying(EdiException.class,
                            error -> assertThat(error.getCodigo()).isEqualTo("FRACCION_EMPRESA_INVALIDA"));
        }
    }
}
