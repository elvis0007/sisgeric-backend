package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.NuevaRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RecomendacionMapperTest {
    private final ObjectMapper json = new ObjectMapper();
    private final RecomendacionMapper mapper = new RecomendacionMapper();

    @ParameterizedTest
    @ValueSource(strings = {"ALTO", "MODERADO", "BAJO"})
    void conservaRiesgoYEvidenciaDesdeElRegistroHastaLaConsulta(String riesgo) throws Exception {
        String payload = "{\"arere_descripcion\":\"Actualizar controles\","
            + "\"arere_num_rec\":1,\"arere_fecha_maxima\":\"2026-12-31\","
            + "\"arere_fecha_inicio_control\":\"2026-10-06\","
            + "\"arere_riesgo_verificado\":\"" + riesgo + "\","
            + "\"arere_evidencia_requerida\":\"Acta y documentos de respaldo\","
            + "\"auditoria\":{\"usuario\":\"auditor_test\"}}";
        NuevaRecomendacionDTO dto = json.readValue(payload, NuevaRecomendacionDTO.class);

        EstadoSeg estado = new EstadoSeg();
        estado.setArees_codigo(1L);
        estado.setArees_descripcion("EN PROCESO");
        Recomendacion recomendacion = mapper.convertirCrearDTOModel(dto, estado, null);
        assertEquals(riesgo, recomendacion.getArere_riesgo_verificado());
        assertEquals("Acta y documentos de respaldo", recomendacion.getArere_evidencia_requerida());

        JsonNode respuesta = json.valueToTree(mapper.convertirModelDTO(recomendacion));
        assertEquals(riesgo, respuesta.get("arere_riesgo_verificado").asText());
        assertEquals("Acta y documentos de respaldo", respuesta.get("arere_evidencia_requerida").asText());
        assertTrue(respuesta.has("arere_riesgo_verificado"));
        assertFalse(respuesta.has("arere_riesgo_nivel"));
        assertFalse(respuesta.has("arere_riesgo_justificacion"));
    }
}
