package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.DirectorResponsablesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario;

public interface RolUsuarioRepositorio extends JpaRepository<RolUsuario, Long> {
  @Query(value = "SELECT * FROM ROOTSISREC.AI_SEG_ROL_USU WHERE ASERO_CODIGO = :asero_codigo AND ASERU_ESTADO = '1'", nativeQuery = true)
  List<RolUsuario> findByAsero_codigo(Long asero_codigo);
  
  @Query(value = "SELECT * FROM ROOTSISREC.AI_SEG_ROL_USU WHERE ASERO_CODIGO = :asero_codigo AND ASERU_DEPARTAMENTO = :departamento AND ASERU_ESTADO = '1'", nativeQuery = true)
  List<RolUsuario> findByAsero_codigoAndDepartamento(Long asero_codigo, String departamento);

  @Query(value = "SELECT * FROM ROOTSISREC.AI_SEG_ROL_USU WHERE UPPER(TRIM(ASERU_CUENTA)) = UPPER(TRIM(:dsgus_cuenta))", nativeQuery = true)
  List<RolUsuario> findByDsgus_cuenta(String dsgus_cuenta);

  @Query(value = "SELECT * FROM ROOTSISREC.AI_SEG_ROL_USU WHERE UPPER(TRIM(ASERU_CODIGO_PER)) = UPPER(TRIM(:aseru_codigo_per))", nativeQuery = true)
  List<RolUsuario> findByAseru_codigo_per(String aseru_codigo_per);

  @Query(value = "SELECT COUNT(ASERU_CODIGO) AS CANTIDAD FROM ROOTSISREC.AI_SEG_ROL_USU "
      + "WHERE UPPER(TRIM(ASERU_CUENTA)) = UPPER(TRIM(:dsgus_cuenta)) "
      + "AND ASERO_CODIGO = :asero_codigo AND ASERU_ESTADO = '1'", nativeQuery = true)
  Integer findByDsgus_cuentaAndAsero_codigo(String dsgus_cuenta, Long asero_codigo);
/*
  //OBTENER LOS CORREOS DE LOS AUDITORES INTERNOES JEFE Y  OPERATIVOS PARA ENVIO DE CORREOS
  @Query(value = "SELECT DISTINCT USU.ASERU_CORREO FROM AI_SEG_ROL_USU USU INNER JOIN AI_SEG_ROL ROL ON USU.ASERO_CODIGO = ROL.ASERO_CODIGO WHERE ROL.ASERO_CODIGO IN (2,3)", nativeQuery = true)
  List<String> getCorreosAuditores();
*/
/*
  //OBTENER LOS CORREOS DE LOS DIRECTORES DEPARTAMENTALES PARA ENVIO DE CORREOS (RECORDATORIO DE 7 DIAS)
  @Query(value = "SELECT DISTINCT USU.ASERU_CORREO FROM AI_SEG_ROL_USU USU INNER JOIN AI_SEG_ROL ROL ON USU.ASERO_CODIGO = ROL.ASERO_CODIGO WHERE ROL.ASERO_CODIGO = 5", nativeQuery = true)
  List<String> getCorreosDirectoresDeparatamentales();
*/
  //OBTENER EL CORREO DEL AUDITOR RESPONSABLE DE UN INFORME (POR CÓDIGO DE INFORME)
  @Query(value = "SELECT DISTINCT USU.ASERU_CORREO FROM AI_SEG_ROL_USU USU WHERE USU.ASERU_CUENTA = ( SELECT INF.AININ_USUARIO FROM AI_INF_INFORMES INF WHERE INF.AININ_CODIGO = :ainin_codigo ) AND USU.ASERU_CORREO IS NOT NULL AND USU.ASERU_ESTADO = '1' FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
  String getCorreoAuditorResponsable(Long ainin_codigo);

  //OBTENER EL NOMBRE/CUENTA DEL AUDITOR RESPONSABLE DE UN INFORME (POR CÓDIGO DE INFORME)
  @Query(value = "SELECT DISTINCT NVL(TRIM(USU.ASERU_NOMBRE || ' ' || USU.ASERU_APELLIDO), USU.ASERU_CUENTA) FROM AI_SEG_ROL_USU USU WHERE USU.ASERU_CUENTA = ( SELECT INF.AININ_USUARIO FROM AI_INF_INFORMES INF WHERE INF.AININ_CODIGO = :ainin_codigo ) AND USU.ASERU_ESTADO = '1' FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
  String getNombreAuditorResponsable(Long ainin_codigo);

  //OBTENER EL CORREO DIRECTO POR CUENTA DE USUARIO
  @Query(value = "SELECT DISTINCT USU.ASERU_CORREO FROM AI_SEG_ROL_USU USU WHERE UPPER(TRIM(USU.ASERU_CUENTA)) = UPPER(TRIM(:cuenta)) AND USU.ASERU_CORREO IS NOT NULL AND USU.ASERU_ESTADO = '1' FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
  String getCorreoByCuenta(@Param("cuenta") String cuenta);

  //OBTENER EL NOMBRE DIRECTO POR CUENTA DE USUARIO
  @Query(value = "SELECT DISTINCT NVL(TRIM(USU.ASERU_NOMBRE || ' ' || USU.ASERU_APELLIDO), USU.ASERU_CUENTA) FROM AI_SEG_ROL_USU USU WHERE UPPER(TRIM(USU.ASERU_CUENTA)) = UPPER(TRIM(:cuenta)) AND USU.ASERU_ESTADO = '1' FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
  String getNombreByCuenta(@Param("cuenta") String cuenta);

  //OBTENER LOS CORREOS DE LOS DIRECTORES DEPARTAMENTALES POR INFORME (POR CDIGO DE INFORME)
  @Query(value = "SELECT USU.ASERU_CORREO FROM AI_SEG_ROL_USU USU WHERE USU.ASERU_DEPARTAMENTO IN ( " +
                 "  SELECT DISTINCT RES.ARERS_DEPARTAMENTO FROM AI_REC_RESPONSABLES RES " +
                 "  JOIN AI_REC_DETALLE_RECOMENDACION DR ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                 "  JOIN AI_REC_RECOMENDACIONES REC ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                 "  JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                 "  JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                 "  JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                 "  WHERE INF.AININ_CODIGO = :ainin_codigo " +
                 "    AND DR.AREDR_ESTADO = '1' " +
                 "    AND RES.ARERS_ESTADO = '1' " +
                 "    AND REC.ARERE_ESTADO = '1' " +
                 ") AND USU.ASERO_CODIGO = 5 AND USU.ASERU_ESTADO = '1'", nativeQuery = true)
  List<String> getCorreosDirectoresByInforme(Long ainin_codigo);

  // Método para llamar al procedimiento
  @Procedure(procedureName = "getCorreosDirectoresYResponsables")
  List<DirectorResponsablesDTO> callGetCorreosDirectoresYResponsables(Integer p_ainin_codigo);

  @Procedure(procedureName = "P_ACTUALIZAR_USUARIO")
  void updateUserEntities(String usuario_old, String usuario_new);

  @Query(value = "SELECT COUNT(*) FROM AI_SEG_ROL_USU USU WHERE USU.ASERU_CUENTA = :usuario AND USU.ASERU_ESTADO = 1", nativeQuery = true)
  int conteoRolRegistrado(@Param("usuario") String usuario);

  @Query(value = "SELECT COUNT(ASERU_CODIGO) AS CANTIDAD FROM ROOTSISREC.AI_SEG_ROL_USU WHERE ASERU_CUENTA = :dsgus_cuenta AND ASERU_NUM_ROL = :aseru_num_rol", nativeQuery = true)
  Integer findByDsgus_cuentaAndAseru_num_rol(String dsgus_cuenta, String aseru_num_rol);

  // Obtener las cuentas de usuario del Auditor Interno Jefe activo
  @Query(value = "SELECT DISTINCT TRIM(USU.ASERU_CUENTA) FROM AI_SEG_ROL_USU USU WHERE USU.ASERO_CODIGO = 2 AND USU.ASERU_ESTADO = 1", nativeQuery = true)
  List<String> getCuentasAuditoresJefe();
}
