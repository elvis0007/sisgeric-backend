package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.EvidenciaRequeridaDTO;

@Service
public class EvidenciaRequeridaServicio {
  private final JdbcTemplate jdbc;
  public EvidenciaRequeridaServicio(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public List<EvidenciaRequeridaDTO> listar(Long recomendacion) {
    return jdbc.query("SELECT ARER_CODIGO, ARER_DESCRIPCION FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS " +
        "WHERE ARERE_CODIGO = ? AND ARER_ESTADO = 'A' ORDER BY ARER_CODIGO", (rs, row) -> {
          EvidenciaRequeridaDTO dto = new EvidenciaRequeridaDTO();
          dto.setArer_codigo(rs.getLong(1)); dto.setArer_descripcion(rs.getString(2)); return dto;
        }, recomendacion);
  }

  public List<EvidenciaRequeridaDTO> preparar(String texto, List<EvidenciaRequeridaDTO> lista) {
    if (lista == null && texto == null) return null;
    List<EvidenciaRequeridaDTO> resultado = new ArrayList<>();
    if (lista != null) {
      resultado.addAll(lista);
    } else {
      String contenido = texto.trim();
      String[] partes = contenido.matches("(?s)^1[.)]\\s+.*")
          ? contenido.split("(?:\\r?\\n)+(?=\\d+[.)]\\s+)") : new String[] { contenido };
      for (String parte : partes) {
        EvidenciaRequeridaDTO dto = new EvidenciaRequeridaDTO();
        dto.setArer_descripcion(parte.replaceFirst("^\\d+[.)]\\s+", "")); resultado.add(dto);
      }
    }
    if (resultado.isEmpty()) throw new IllegalArgumentException("Ingrese al menos una evidencia requerida.");
    Set<Long> ids = new HashSet<>();
    for (EvidenciaRequeridaDTO dto : resultado) {
      if (dto == null || dto.getArer_descripcion() == null || dto.getArer_descripcion().trim().isEmpty()
          || dto.getArer_descripcion().trim().length() > 500)
        throw new IllegalArgumentException("Cada evidencia requerida debe contener entre 1 y 500 caracteres.");
      dto.setArer_descripcion(dto.getArer_descripcion().trim());
      if (dto.getArer_codigo() != null && !ids.add(dto.getArer_codigo()))
        throw new IllegalArgumentException("Una evidencia requerida no puede repetirse por codigo.");
    }
    if (serializar(resultado).length() > 2000)
      throw new IllegalArgumentException("Las evidencias requeridas no pueden superar 2000 caracteres en total.");
    return resultado;
  }

  public String serializar(List<EvidenciaRequeridaDTO> lista) {
    StringJoiner texto = new StringJoiner("\n");
    for (int i = 0; i < lista.size(); i++) texto.add((i + 1) + ". " + lista.get(i).getArer_descripcion());
    return texto.toString();
  }

  @Transactional
  public void guardar(Long recomendacion, List<EvidenciaRequeridaDTO> solicitadas) {
    if (solicitadas == null) return;
    // Serializa ediciones concurrentes de la misma recomendacion.
    jdbc.queryForObject("SELECT ARERE_CODIGO FROM ROOTSISREC.AI_REC_RECOMENDACIONES WHERE ARERE_CODIGO = ? FOR UPDATE", Long.class, recomendacion);
    List<EvidenciaRequeridaDTO> existentes = listar(recomendacion);
    Map<Long, EvidenciaRequeridaDTO> porId = new HashMap<>();
    existentes.forEach(e -> porId.put(e.getArer_codigo(), e));
    for (EvidenciaRequeridaDTO e : solicitadas)
      if (e.getArer_codigo() != null && !porId.containsKey(e.getArer_codigo()))
        throw new IllegalArgumentException("La evidencia no pertenece a la recomendacion o ya fue desactivada. Recargue los datos.");
    Set<Long> utilizados = new HashSet<>();
    // Reservar IDs antes de buscar coincidencias para clientes anteriores sin IDs.
    solicitadas.stream().filter(e -> e.getArer_codigo() != null).forEach(e -> utilizados.add(e.getArer_codigo()));
    for (EvidenciaRequeridaDTO e : solicitadas) {
      Long id = e.getArer_codigo();
      if (id == null) {
        id = existentes.stream().filter(x -> !utilizados.contains(x.getArer_codigo())
            && x.getArer_descripcion().equals(e.getArer_descripcion()))
            .map(EvidenciaRequeridaDTO::getArer_codigo).findFirst().orElse(null);
      }
      if (id == null) {
        id = jdbc.queryForObject("SELECT ROOTSISREC.AI_REC_EVIDENCIA_REQ_SEQ.NEXTVAL FROM DUAL", Long.class);
        jdbc.update("INSERT INTO ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS (ARER_CODIGO, ARERE_CODIGO, ARER_DESCRIPCION, ARER_ESTADO) VALUES (?, ?, ?, 'A')", id, recomendacion, e.getArer_descripcion());
      } else {
        jdbc.update("UPDATE ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS SET ARER_DESCRIPCION = ? WHERE ARER_CODIGO = ? AND ARERE_CODIGO = ? AND ARER_ESTADO = 'A'", e.getArer_descripcion(), id, recomendacion);
      }
      utilizados.add(id); e.setArer_codigo(id);
    }
    // Baja logica: mantiene las FK de actividades que ya usan la evidencia.
    for (EvidenciaRequeridaDTO e : existentes)
      if (!utilizados.contains(e.getArer_codigo()))
        jdbc.update("UPDATE ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS SET ARER_ESTADO = 'I' WHERE ARER_CODIGO = ? AND ARERE_CODIGO = ?", e.getArer_codigo(), recomendacion);
  }
}
