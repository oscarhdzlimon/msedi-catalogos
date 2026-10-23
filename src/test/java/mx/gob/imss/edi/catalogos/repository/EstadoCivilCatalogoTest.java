package mx.gob.imss.edi.catalogos.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import mx.gob.imss.edi.catalogos.services.impl.CatalogoServiceImpl;

class EstadoCivilCatalogoTest {
    @Test void consultaClavesSistEnOrdenYExcluyeInactivos() throws Exception {
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:estadoCivil;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        try (var c = ds.getConnection(); var sql = c.createStatement()) {
            sql.execute("create schema if not exists catalogo");
            sql.execute("drop table if exists catalogo.edic_estado_civil");
            sql.execute("create table catalogo.edic_estado_civil(id_estado_civil smallint primary key,cve_estado_civil varchar(1),des_estado_civil varchar(50),num_orden int,ind_activo boolean)");
            sql.execute("insert into catalogo.edic_estado_civil values(5,'5','UNIÓN LIBRE',5,true),(2,'2','CASADO',2,true),(1,'1','SOLTERO',1,true),(4,'4','DIVORCIADO',4,true),(3,'3','VIUDO',3,true),(9,'9','INACTIVO',0,false)");
        }
        var config = new Configuration(new Environment("test", new JdbcTransactionFactory(), ds));
        config.setMapUnderscoreToCamelCase(true);
        config.addMapper(CatalogoMapper.class);
        try (var session = new SqlSessionFactoryBuilder().build(config).openSession()) {
            var service = new CatalogoServiceImpl(session.getMapper(CatalogoMapper.class));
            var resultado = service.consultar("estado-civil");
            assertThat(resultado.claveCatalogo()).isEqualTo("estado-civil");
            assertThat(resultado.elementos()).extracting("clave").containsExactly("1","2","3","4","5");
            assertThat(resultado.elementos()).extracting("id").containsExactly(1L,2L,3L,4L,5L);
            assertThat(resultado.elementos()).extracting("descripcion").containsExactly("SOLTERO","CASADO","VIUDO","DIVORCIADO","UNIÓN LIBRE");
            session.getConnection().createStatement().execute("update catalogo.edic_estado_civil set ind_activo=false");
            session.clearCache();
            assertThat(service.consultar("estado-civil").elementos()).isEmpty();
        }
    }
}
