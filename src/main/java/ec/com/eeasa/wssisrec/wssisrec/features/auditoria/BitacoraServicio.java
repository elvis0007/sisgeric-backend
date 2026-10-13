package ec.com.eeasa.wssisrec.wssisrec.features.auditoria;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Table;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.BitacoraDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.models.Bitacora;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class BitacoraServicio {
  private static final Logger logger = LoggerFactory.getLogger(BitacoraServicio.class);
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  BitacoraRepositorio bitacoraRepositorio;

 
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorFechas(String fechaInicio,
      String fechaFin) {
    try {
      List<Bitacora> bitacoras = bitacoraRepositorio.findByFechaBetween(fechaInicio, fechaFin);

      List<BitacoraDTO> bitacoraDTOs = bitacoras.stream().map(this::convertirModelDTO).collect(Collectors.toList());

      return EstadoRespuestas.respuestaObtencion("Registros bitacora obtenidos exitosamente", bitacoraDTOs);

    } catch (Exception e) {
      logger.error("Error al obtener registros bitacora: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener registros");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorUsuario(String usuario) {
    try {
      List<Bitacora> bitacoras = bitacoraRepositorio.findByUsuario(usuario);
      List<BitacoraDTO> bitacoraDTOs = bitacoras.stream().map(this::convertirModelDTO).collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Registros bitacora obtenidos exitosamente", bitacoraDTOs);

    } catch (Exception e) {
      logger.error("Error al obtener registros bitacora: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener registros bitacora");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtener() {
    try {
      List<Bitacora> bitacoras = bitacoraRepositorio.findAll();
      List<BitacoraDTO> bitacoraDTOs = bitacoras.stream().map((registro) -> convertirModelDTO(registro))
          .collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Bitacora obtenido exitosamente", bitacoraDTOs.size());
    } catch (Exception e) {
      logger.error("Error al obtener la información de bitacora: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener la información de bitacora");
    }
  }

  private BitacoraDTO convertirModelDTO(Bitacora bitacora) {
    BitacoraDTO bitacoraDTO = new BitacoraDTO();
    bitacoraDTO.setCodigo(bitacora.getAsebit_codigo());
    bitacoraDTO.setProyecto(bitacora.getAsebit_proyecto());
    bitacoraDTO.setTabla(bitacora.getAsebit_tabla());
    bitacoraDTO.setHost(bitacora.getAsebit_host());
    bitacoraDTO.setUsuario(bitacora.getAsebit_usuario());
    bitacoraDTO.setFecha(bitacora.getAsebit_fecha());
    bitacoraDTO.setDetalle(bitacora.getAsebit_detalle());
    return bitacoraDTO;

  }

  public void registrarBitacora(Object entity, AuditoriaDTO auditoriaDTO, String operacion) throws Exception {
    // Convertir AuditoriaDTO a la entidad de Auditoria interna
    Auditoria usuario = convertirDTO(auditoriaDTO);

    // Generar el detalle de la bitácora en un formato String
    String bitacoraDetalle = generarDetalleBitacora(entity, usuario, operacion);

    // Definir la sentencia SQL para llamar al procedimiento almacenado
    String sql = "{call ROOTSISREC.SP_BITACORA_INSERT(?)}";

    // Ejecutar la llamada al procedimiento almacenado usando PreparedStatement
    jdbcTemplate.update(connection -> {
      CallableStatement callableStatement = connection.prepareCall(sql);
      // Utilizar setString para pasar el parámetro CLOB
      callableStatement.setString(1, bitacoraDetalle);
      return callableStatement;
    });
  }

  // Método para convertir AuditoriaDTO a la entidad interna Auditoria
  public Auditoria convertirDTO(AuditoriaDTO auditoriaDTO) {
    if (auditoriaDTO == null) {
      return new Auditoria("LOCALHOST", "SISTEMA");
    }
    String host = auditoriaDTO.getHost() != null ? auditoriaDTO.getHost() : "LOCALHOST";
    String usuario = auditoriaDTO.getUsuario() != null ? auditoriaDTO.getUsuario() : "SISTEMA";
    return new Auditoria(host, usuario);
  }

  // Método para generar el detalle de la bitácora basado en la entidad y los
  // detalles de auditoría
  private String generarDetalleBitacora(Object entity, Auditoria usuario, String operacion) throws Exception {
    String proyecto = (usuario != null && usuario.getProyecto() != null) ? usuario.getProyecto() : "SISREC";
    String nombreUsuario = (usuario != null && usuario.getUsuario() != null) ? usuario.getUsuario() : "SISTEMA";
    String host = (usuario != null && usuario.getHost() != null) ? usuario.getHost() : "LOCALHOST";

    if (entity == null) {
      return proyecto + "~" + nombreUsuario + "~" + "GENERAL" + "~" + operacion + "~~" + host;
    }

    String detalle = "";
    String nombreTabla = "GENERAL";

    // Si es un String, primitivo, número o clase base de Java, no acceder a campos privados por reflexión
    if (entity instanceof CharSequence || entity instanceof Number || entity.getClass().getName().startsWith("java.")) {
      detalle = entity.toString();
    } else {
      Table tableAnnotation = entity.getClass().getAnnotation(Table.class);
      if (tableAnnotation != null && tableAnnotation.name() != null && !tableAnnotation.name().isEmpty()) {
        nombreTabla = tableAnnotation.name();
      } else {
        nombreTabla = entity.getClass().getSimpleName();
      }

      Field[] allFields = entity.getClass().getDeclaredFields();
      for (Field field : allFields) {
        if (field.getName().equalsIgnoreCase("serialVersionUID")) {
          continue;
        }
        try {
          field.setAccessible(true);
          Object valorcampo = field.get(entity);
          if (valorcampo != null) {
            detalle += field.getName() + ": " + valorcampo + " | ";
          }
        } catch (Throwable t) {
          // Ignorar campos inaccesibles por restricciones de módulos de Java (InaccessibleObjectException, etc.)
        }
      }
    }

    return proyecto + "~" +
        nombreUsuario + "~" +
        nombreTabla + "~" +
        operacion + "~" +
        detalle + "~" +
        host;
  }
}
