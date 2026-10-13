package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.enums.EstadoSegEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRiesgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;

@ExtendWith(MockitoExtension.class)
class RecomendacionRiesgoTest {
  @Mock private RecomendacionRepositorio recomendacionRepositorio;
  @Mock private BitacoraServicio bitacoraServicio;
  @InjectMocks private RecomendacionServicio servicio;

  private ActualizarRecomendacionDTO datosEdicion(String riesgo) {
    ActualizarRecomendacionDTO dto = new ActualizarRecomendacionDTO();
    dto.setArere_descripcion("Descripcion editada");
    dto.setArere_num_rec(5);
    dto.setArere_riesgo_verificado(riesgo);
    dto.setAuditoria(datos(riesgo).getAuditoria());
    return dto;
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"ALTO", "MODERADO", "BAJO"})
  void editaRecomendacionConRiesgoOpcionalConservandoEvidencia(String riesgo) {
    Recomendacion recomendacion = new Recomendacion();
    recomendacion.setArere_riesgo_verificado("BAJO");
    recomendacion.setArere_evidencia_requerida("Acta original");
    Date fecha = new Date();
    recomendacion.setArere_fecha_maxima(fecha);
    when(recomendacionRepositorio.findById(1L)).thenReturn(Optional.of(recomendacion));

    assertEquals(HttpStatus.CREATED, servicio.actualizarRecomendacion(datosEdicion(riesgo), 1L).getStatusCode());
    assertEquals(riesgo == null ? "BAJO" : riesgo, recomendacion.getArere_riesgo_verificado());
    assertEquals("Acta original", recomendacion.getArere_evidencia_requerida());
    assertEquals("Descripcion editada", recomendacion.getArere_descripcion());
    assertEquals(5, recomendacion.getArere_num_rec());
    assertSame(fecha, recomendacion.getArere_fecha_maxima());
    verify(recomendacionRepositorio).saveAndFlush(recomendacion);
  }

  @ParameterizedTest
  @ValueSource(strings = {"OTRO", ""})
  void rechazaRiesgoInvalidoAlEditarRecomendacion(String riesgo) {
    assertEquals(HttpStatus.BAD_REQUEST, servicio.actualizarRecomendacion(datosEdicion(riesgo), 1L).getStatusCode());
    verify(recomendacionRepositorio, never()).saveAndFlush(any());
  }

  @Test
  void noEditaRiesgoDeRecomendacionCumplida() {
    Recomendacion recomendacion = new Recomendacion();
    recomendacion.setArere_riesgo_verificado("BAJO");
    EstadoSeg estado = new EstadoSeg();
    estado.setArees_codigo(EstadoSegEnum.CUMPLIDA.getCodigo());
    recomendacion.setEstadoSeg(estado);
    when(recomendacionRepositorio.findById(1L)).thenReturn(Optional.of(recomendacion));

    assertEquals(HttpStatus.BAD_REQUEST, servicio.actualizarRecomendacion(datosEdicion("ALTO"), 1L).getStatusCode());
    assertEquals("BAJO", recomendacion.getArere_riesgo_verificado());
    verify(recomendacionRepositorio, never()).saveAndFlush(any());
  }

  @Test
  void editaSoloRiesgoConservandoEvidencia() {
    Recomendacion recomendacion = new Recomendacion();
    recomendacion.setArere_riesgo_verificado("BAJO");
    recomendacion.setArere_evidencia_requerida("Evidencia original");
    when(recomendacionRepositorio.findById(1L)).thenReturn(Optional.of(recomendacion));
    when(recomendacionRepositorio.saveAndFlush(recomendacion)).thenReturn(recomendacion);
    ActualizarRiesgoDTO dto = datos("ALTO");
    dto.setArere_evidencia_requerida(null);

    assertEquals(HttpStatus.CREATED, servicio.actualizarRiesgo(dto, 1L).getStatusCode());
    assertEquals("ALTO", recomendacion.getArere_riesgo_verificado());
    assertEquals("Evidencia original", recomendacion.getArere_evidencia_requerida());
  }

  @Test
  void editaSoloEvidenciaConservandoRiesgo() {
    Recomendacion recomendacion = new Recomendacion();
    recomendacion.setArere_riesgo_verificado("MODERADO");
    when(recomendacionRepositorio.findById(1L)).thenReturn(Optional.of(recomendacion));
    when(recomendacionRepositorio.saveAndFlush(recomendacion)).thenReturn(recomendacion);

    assertEquals(HttpStatus.CREATED, servicio.actualizarRiesgo(datos(null), 1L).getStatusCode());
    assertEquals("MODERADO", recomendacion.getArere_riesgo_verificado());
    assertEquals("Acta de cumplimiento", recomendacion.getArere_evidencia_requerida());
  }

  @Test
  void rechazaActualizacionSinCampos() {
    ActualizarRiesgoDTO dto = datos(null);
    dto.setArere_evidencia_requerida(null);
    assertEquals(HttpStatus.BAD_REQUEST, servicio.actualizarRiesgo(dto, 1L).getStatusCode());
    verify(recomendacionRepositorio, never()).saveAndFlush(any());
  }

  private ActualizarRiesgoDTO datos(String riesgo) {
    ActualizarRiesgoDTO dto = new ActualizarRiesgoDTO();
    dto.setArere_riesgo_verificado(riesgo);
    dto.setArere_evidencia_requerida("  Acta de cumplimiento  ");
    AuditoriaDTO auditoria = new AuditoriaDTO();
    auditoria.setUsuario("auditor_test");
    dto.setAuditoria(auditoria);
    return dto;
  }

  @ParameterizedTest
  @ValueSource(strings = {"ALTO", "MODERADO", "BAJO"})
  void editaRiesgoYEvidenciaConservandoDescripcionNumeroYFecha(String riesgo) {
    Recomendacion recomendacion = new Recomendacion();
    Date fecha = new Date();
    recomendacion.setArere_descripcion("Descripcion original");
    recomendacion.setArere_num_rec(3);
    recomendacion.setArere_fecha_maxima(fecha);
    when(recomendacionRepositorio.findById(1L)).thenReturn(Optional.of(recomendacion));
    when(recomendacionRepositorio.saveAndFlush(recomendacion)).thenReturn(recomendacion);

    assertEquals(HttpStatus.CREATED, servicio.actualizarRiesgo(datos(riesgo), 1L).getStatusCode());
    assertEquals(riesgo, recomendacion.getArere_riesgo_verificado());
    assertEquals("Acta de cumplimiento", recomendacion.getArere_evidencia_requerida());
    assertEquals("Descripcion original", recomendacion.getArere_descripcion());
    assertEquals(3, recomendacion.getArere_num_rec());
    assertSame(fecha, recomendacion.getArere_fecha_maxima());
    verify(recomendacionRepositorio).saveAndFlush(recomendacion);
  }

  @Test
  void rechazaRecomendacionCumplida() {
    Recomendacion recomendacion = new Recomendacion();
    recomendacion.setArere_riesgo_verificado("BAJO");
    recomendacion.setArere_evidencia_requerida("Evidencia registrada");
    EstadoSeg estado = new EstadoSeg();
    estado.setArees_codigo(EstadoSegEnum.CUMPLIDA.getCodigo());
    recomendacion.setEstadoSeg(estado);
    when(recomendacionRepositorio.findById(1L)).thenReturn(Optional.of(recomendacion));
    assertEquals(HttpStatus.BAD_REQUEST, servicio.actualizarRiesgo(datos("ALTO"), 1L).getStatusCode());
    verify(recomendacionRepositorio, never()).saveAndFlush(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"riesgo", "evidencia"})
  void bloqueaRiesgoYPermiteEvidenciaPendienteEnCumplida(String campo) {
    Recomendacion recomendacion = new Recomendacion();
    EstadoSeg estado = new EstadoSeg();
    estado.setArees_codigo(EstadoSegEnum.CUMPLIDA.getCodigo());
    recomendacion.setEstadoSeg(estado);
    boolean editaRiesgo = "riesgo".equals(campo);
    recomendacion.setArere_riesgo_verificado(editaRiesgo ? null : "BAJO");
    recomendacion.setArere_evidencia_requerida(editaRiesgo ? "Evidencia original" : "  ");
    ActualizarRiesgoDTO dto = datos(editaRiesgo ? "ALTO" : null);
    if (editaRiesgo) dto.setArere_evidencia_requerida(null);
    when(recomendacionRepositorio.findById(1L)).thenReturn(Optional.of(recomendacion));
    if (!editaRiesgo) when(recomendacionRepositorio.saveAndFlush(recomendacion)).thenReturn(recomendacion);

    assertEquals(editaRiesgo ? HttpStatus.BAD_REQUEST : HttpStatus.CREATED,
        servicio.actualizarRiesgo(dto, 1L).getStatusCode());
    assertEquals("BAJO", recomendacion.getArere_riesgo_verificado());
    if (editaRiesgo) verify(recomendacionRepositorio, never()).saveAndFlush(any());
    assertEquals(editaRiesgo ? "Evidencia original" : "Acta de cumplimiento", recomendacion.getArere_evidencia_requerida());
    assertSame(estado, recomendacion.getEstadoSeg());
  }

  @Test
  void rechazaRiesgoInvalidoYEvidenciaVacia() {
    assertEquals(HttpStatus.BAD_REQUEST, servicio.actualizarRiesgo(datos("OTRO"), 1L).getStatusCode());
    ActualizarRiesgoDTO dto = datos("BAJO");
    dto.setArere_evidencia_requerida("   ");
    assertEquals(HttpStatus.BAD_REQUEST, servicio.actualizarRiesgo(dto, 1L).getStatusCode());
    verify(recomendacionRepositorio, never()).saveAndFlush(any());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"ALTO", "MODERADO", "BAJO"})
  void riesgoEsBajoAlCompletarRecomendacion(String riesgoAnterior) {
    Recomendacion recomendacion = new Recomendacion();
    recomendacion.setArere_riesgo_verificado(riesgoAnterior);
    recomendacion.setArere_evidencia_requerida("Evidencia original");
    EstadoSeg estado = new EstadoSeg();
    estado.setArees_codigo(EstadoSegEnum.CUMPLIDA.getCodigo());
    recomendacion.setEstadoSeg(estado);

    assertEquals("BAJO", recomendacion.getArere_riesgo_verificado());
    assertEquals("BAJO", org.springframework.test.util.ReflectionTestUtils.getField(recomendacion, "arere_riesgo_verificado"));
    assertEquals("Evidencia original", recomendacion.getArere_evidencia_requerida());
    recomendacion.setArere_riesgo_verificado("ALTO");
    org.springframework.test.util.ReflectionTestUtils.invokeMethod(recomendacion, "normalizarRiesgoCumplida");
    assertEquals("BAJO", org.springframework.test.util.ReflectionTestUtils.getField(recomendacion, "arere_riesgo_verificado"));
  }
}
