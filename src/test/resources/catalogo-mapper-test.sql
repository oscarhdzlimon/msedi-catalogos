create schema if not exists catalogo;

drop table if exists catalogo.edic_clasif_incapacidad;
drop table if exists catalogo.edic_estatus;
drop table if exists catalogo.edic_perfil;
drop table if exists catalogo.edic_ramo_seguro;
drop table if exists catalogo.edic_tipo_documento;
drop table if exists catalogo.edic_tipo_estatus;
drop table if exists catalogo.edic_tipo_identificacion;
drop table if exists catalogo.edic_tipo_incapacidad;
drop table if exists catalogo.edic_tipo_riesgo;

create table catalogo.edic_clasif_incapacidad (
  id_clasif_incapacidad bigint primary key,
  cve_clasif_incapacidad varchar(50),
  des_clasif_incapacidad varchar(100),
  num_orden integer,
  ind_activo boolean
);
create table catalogo.edic_estatus (
  id_estatus bigint primary key,
  cve_estatus varchar(50),
  des_estatus varchar(100),
  id_tipo_estatus bigint,
  ind_activo boolean
);
create table catalogo.edic_perfil (
  id_perfil bigint primary key,
  cve_perfil varchar(30),
  des_perfil varchar(100),
  ind_activo boolean
);
create table catalogo.edic_ramo_seguro (
  id_ramo_seguro bigint primary key,
  cve_ramo_seguro varchar(30),
  des_ramo_seguro varchar(100),
  num_orden integer,
  ind_activo boolean
);
create table catalogo.edic_tipo_documento (
  id_tipo_documento bigint primary key,
  cve_tipo_documento varchar(50),
  des_tipo_documento varchar(100),
  ref_formato varchar(100),
  ind_activo boolean
);
create table catalogo.edic_tipo_estatus (
  id_tipo_estatus bigint primary key,
  cve_tipo_estatus varchar(50),
  des_tipo_estatus varchar(100),
  ind_activo boolean
);
create table catalogo.edic_tipo_identificacion (
  id_tipo_identificacion bigint primary key,
  cve_tipo_identificacion varchar(50),
  des_tipo_identificacion varchar(100),
  num_orden integer,
  ind_activo boolean
);
create table catalogo.edic_tipo_incapacidad (
  id_tipo_incapacidad bigint primary key,
  cve_tipo_incapacidad varchar(50),
  des_tipo_incapacidad varchar(100),
  num_orden integer,
  ind_activo boolean
);
create table catalogo.edic_tipo_riesgo (
  id_tipo_riesgo bigint primary key,
  cve_tipo_riesgo varchar(50),
  des_tipo_riesgo varchar(100),
  num_orden integer,
  ind_activo boolean
);

insert into catalogo.edic_clasif_incapacidad values (1, 'CLASIFICACION', 'Clasificacion', 1, true);
insert into catalogo.edic_estatus values (2, 'ACTIVO', 'Activo', 20, true);
insert into catalogo.edic_perfil values (3, 'MED', 'Medico', true);
insert into catalogo.edic_ramo_seguro values (4, 'EG', 'Enfermedad general', 1, true);
insert into catalogo.edic_tipo_documento values (5, 'DOCUMENTO', 'Documento', '^[0-9]+$', true);
insert into catalogo.edic_tipo_estatus values (6, 'TIPO_ESTATUS', 'Tipo estatus', true);
insert into catalogo.edic_tipo_identificacion values (7, 'INE_IFE', 'INE', 1, true);
insert into catalogo.edic_tipo_incapacidad values (8, 'INICIAL', 'Inicial', 1, true);
insert into catalogo.edic_tipo_riesgo values (9, 'ACCIDENTE_TRABAJO', 'Accidente de trabajo', 1, true);
insert into catalogo.edic_tipo_riesgo values (10, 'INACTIVO', 'Inactivo', 2, false);
