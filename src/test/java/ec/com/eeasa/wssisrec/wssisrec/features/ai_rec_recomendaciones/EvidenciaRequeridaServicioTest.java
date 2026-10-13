package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.EvidenciaRequeridaDTO;

class EvidenciaRequeridaServicioTest {
  JdbcTemplate jdbc;
  EvidenciaRequeridaServicio servicio;
  @BeforeEach void preparar() {
    jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=Oracle;DB_CLOSE_DELAY=-1", "sa", ""));
    jdbc.execute("CREATE SCHEMA ROOTSISREC");
    jdbc.execute("CREATE SEQUENCE ROOTSISREC.AI_REC_EVIDENCIA_REQ_SEQ START WITH 1");
    jdbc.execute("CREATE TABLE ROOTSISREC.AI_REC_RECOMENDACIONES (ARERE_CODIGO BIGINT PRIMARY KEY)");
    jdbc.execute("INSERT INTO ROOTSISREC.AI_REC_RECOMENDACIONES VALUES (10), (20)");
    jdbc.execute("CREATE TABLE ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS (ARER_CODIGO BIGINT PRIMARY KEY, ARERE_CODIGO BIGINT NOT NULL REFERENCES ROOTSISREC.AI_REC_RECOMENDACIONES, ARER_DESCRIPCION VARCHAR(500) NOT NULL, ARER_ESTADO CHAR(1) NOT NULL)");
    jdbc.execute("CREATE TABLE ROOTSISREC.AI_REC_PLAN_ACTIVIDADES (ARER_CODIGO BIGINT REFERENCES ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS)");
    servicio = new EvidenciaRequeridaServicio(jdbc);
  }
  private EvidenciaRequeridaDTO fila(Long id, String texto) {
    EvidenciaRequeridaDTO e = new EvidenciaRequeridaDTO(); e.setArer_codigo(id); e.setArer_descripcion(texto); return e;
  }
  @Test void creaFilasDesdeListaNumerada() {
    List<EvidenciaRequeridaDTO> lista = servicio.preparar("1. Acta\n2. Informe", null);
    servicio.guardar(10L, lista);
    assertEquals(2, servicio.listar(10L).size());
    assertEquals("Acta", servicio.listar(10L).get(0).getArer_descripcion());
    assertNotNull(lista.get(0).getArer_codigo());
  }
  @Test void actualizaMismoIdYSinRomperEnlaceDeActividad() {
    List<EvidenciaRequeridaDTO> lista = servicio.preparar("1. Acta\n2. Informe", null); servicio.guardar(10L, lista);
    Long id = lista.get(0).getArer_codigo();
    jdbc.update("INSERT INTO ROOTSISREC.AI_REC_PLAN_ACTIVIDADES VALUES (?)", id);
    servicio.guardar(10L, servicio.preparar(null, Arrays.asList(fila(id, "Acta aprobada"))));
    assertEquals(id, servicio.listar(10L).get(0).getArer_codigo());
    assertEquals("Acta aprobada", servicio.listar(10L).get(0).getArer_descripcion());
    assertEquals(id, jdbc.queryForObject("SELECT ARER_CODIGO FROM ROOTSISREC.AI_REC_PLAN_ACTIVIDADES", Long.class));
    assertEquals("I", jdbc.queryForObject("SELECT ARER_ESTADO FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS WHERE ARER_CODIGO = ?", String.class, lista.get(1).getArer_codigo()));
  }
  @Test void retirarEvidenciaReferenciadaSoloLaDesactiva() {
    List<EvidenciaRequeridaDTO> lista = servicio.preparar("Acta", null); servicio.guardar(10L, lista);
    Long id = lista.get(0).getArer_codigo(); jdbc.update("INSERT INTO ROOTSISREC.AI_REC_PLAN_ACTIVIDADES VALUES (?)", id);
    servicio.guardar(10L, servicio.preparar("Informe", null));
    assertEquals("I", jdbc.queryForObject("SELECT ARER_ESTADO FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS WHERE ARER_CODIGO = ?", String.class, id));
    assertEquals(id, jdbc.queryForObject("SELECT ARER_CODIGO FROM ROOTSISREC.AI_REC_PLAN_ACTIVIDADES", Long.class));
  }
  @Test void rechazaIdDeOtraRecomendacionAntesDeInsertar() {
    List<EvidenciaRequeridaDTO> lista = servicio.preparar("Acta", null); servicio.guardar(20L, lista);
    assertThrows(IllegalArgumentException.class, () -> servicio.guardar(10L,
        Arrays.asList(fila(null, "Nueva"), fila(lista.get(0).getArer_codigo(), "Ajena"))));
    assertTrue(servicio.listar(10L).isEmpty());
  }
  @Test void validaLimitesYConservaParrafosDeTextoAnterior() {
    assertThrows(IllegalArgumentException.class, () -> servicio.preparar(String.join("", Collections.nCopies(501, "x")), null));
    assertThrows(IllegalArgumentException.class, () -> servicio.preparar(null, Arrays.asList(fila(1L, "A"), fila(1L, "B"))));
    assertEquals(1, servicio.preparar("Documento\ncon detalles", null).size());
    List<EvidenciaRequeridaDTO> lista = servicio.preparar("1. Acta\n2. Informe", null); servicio.guardar(10L, lista);
    servicio.guardar(10L, servicio.preparar("1. Acta\n2. Informe", null));
    assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS", Integer.class));
  }
}
