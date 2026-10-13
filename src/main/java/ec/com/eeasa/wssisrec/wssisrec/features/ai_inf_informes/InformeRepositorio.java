package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;

/**
 * Repositorio para la entidad Informe
 * Procedimiento SP_INFORMES_FILTROS_D espera:
 * - AINOC_ORGANISMO_CONTROL: Long
 * - AINTI_TIPO_INF: Long
 * - FECHA_INICIO: String
 * - FECHA_FIN: String
 * - DEPARTAMENTO: String
 * - RESULTADO: REF_CURSOR (salida)
 */
public interface InformeRepositorio extends JpaRepository<Informe, Long>, JpaSpecificationExecutor<Informe> {

  @Query(value = "SELECT MAX(AININ_CODIGO) FROM AI_INF_INFORMES", nativeQuery = true)
  Long maxValue();

  @Query(value = "SELECT I.* FROM AI_INF_INFORMES I, AI_REC_HALLAZGOS H, AI_REC_CONCLUSIONES C, AI_REC_RECOMENDACIONES R WHERE I.AININ_CODIGO = H.AININ_CODIGO AND H.AREHA_CODIGO=C.AREHA_CODIGO AND C.ARECO_CODIGO=R.ARECO_CODIGO AND R.ARERE_CODIGO = :arere_codigo", nativeQuery = true)
  Informe findByRecomendacion(@Param("arere_codigo") Long arere_codigo);

  @Query(value = "SELECT I.* FROM AI_INF_INFORMES I, AI_REC_HALLAZGOS H, AI_REC_CONCLUSIONES C WHERE I.AININ_CODIGO=H.AININ_CODIGO AND H.AREHA_CODIGO = C.AREHA_CODIGO AND C.ARECO_CODIGO = :areco_codigo", nativeQuery = true)
  Informe findByConclusion(@Param("areco_codigo") Long areco_codigo);

  @Query(value = "SELECT I.* FROM AI_INF_INFORMES I, AI_REC_HALLAZGOS H WHERE I.AININ_CODIGO = H.AININ_CODIGO AND H.AREHA_CODIGO = :areha_codigo", nativeQuery = true)
  Informe findByHallazgo(@Param("areha_codigo") Long areha_codigo);

  @Query(value = "SELECT * FROM AI_INF_INFORMES WHERE AININ_MEMORANDO = :memorando AND AININ_ESTADO = '1'", nativeQuery = true)
  Informe findByMemorando(@Param("memorando") String memorando);

  @Procedure(name = "SP_INFORMES_FILTROS_D")
  List<Object[]> findByFilterDepartamental(
      @Param("AINOC_ORGANISMO_CONTROL") Long ainoc_organismo_control,
      @Param("AINTI_TIPO_INF") Long ainti_tipo_inf,
      @Param("FECHA_INICIO") String fecha_inicio,
      @Param("FECHA_FIN") String fecha_fin,
      @Param("DEPARTAMENTO") String departamento);
}
