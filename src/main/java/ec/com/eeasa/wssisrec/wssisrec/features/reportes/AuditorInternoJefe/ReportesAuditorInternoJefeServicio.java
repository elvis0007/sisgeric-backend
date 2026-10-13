package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte1AIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte2PoRecomendacionesAIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte3RecomendacionesDirectorioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte4ConteoRecomendacionesEstadoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte5ConteoRecomendacionesPorDepartamentoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteHallazgos;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReportePorTipoInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteRecomendacionesFechaCorteDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte7AuditoresDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasRealizadasDTO;
@Service
public class ReportesAuditorInternoJefeServicio {


    //VAR 
    @Autowired
    private ReportesAuditorInternoJefeRepository reportesAuditorInternoJefeRepository;

    @Transactional
    public List<Reporte1AIJDTO> reporte1aijdto(String fechaInicio, String fechaFin, int organismoControl, int estadoSeguimiento){
        return this.reportesAuditorInternoJefeRepository.reporte1aijdto(fechaInicio, fechaFin, organismoControl, estadoSeguimiento);
    }
    
    @Transactional
    public List<Reporte2PoRecomendacionesAIJDTO> reporte2PoRecomendacionesAIJ(String fechaInicio, String fechaFin, String dpto, int estadoSeguimiento, String auditor_responsable){
        return this.reportesAuditorInternoJefeRepository.reporte2PoRecomendacionesAIJ(fechaInicio, fechaFin, dpto, estadoSeguimiento, auditor_responsable);
    }

    @Transactional
    public List<Reporte3RecomendacionesDirectorioDTO> reporte3RecomendacionesDirectorio(String fecha_inicial, String fecha_fin, String dpto, int org_control, int estado_seguimiento, String auditor){
        return this.reportesAuditorInternoJefeRepository.reporte3RecomendacionesDirectorio(fecha_inicial, fecha_fin, dpto, org_control, estado_seguimiento, auditor);
    }

    @Transactional
    public List<Reporte4ConteoRecomendacionesEstadoDTO> reporte4ConteoRecomendacionesEstado(String fecha_inicial, String fecha_fin){
        return this.reportesAuditorInternoJefeRepository.reporte4ConteoRecomendacionesEstado(fecha_inicial, fecha_fin);
    }

    @Transactional
    public List<Reporte5ConteoRecomendacionesPorDepartamentoDTO> reporte5ConteoRecomendacionesPorDepartamento(String fecha_inicial, String fecha_fin, String dpto, int org_control){
        return this.reportesAuditorInternoJefeRepository.reporte5ConteoRecomendacionesPorDepartamento(fecha_inicial, fecha_fin, dpto, org_control);
    }

    @Transactional
    public List<ReportePorTipoInformeDTO> reportePorTipoInforme(String fecha_inicial, String fecha_fin, int tipo_informe){
        return this.reportesAuditorInternoJefeRepository.reportePorTipoInforme(fecha_inicial, fecha_fin, tipo_informe);
    }

    @Transactional
    public List<ReporteHallazgos> reporteHallazgos(String fechaInicio, String fechaFin, int organismoControl, String auditor){
        return this.reportesAuditorInternoJefeRepository.reporteHallazgos(fechaInicio, fechaFin, organismoControl, auditor);
    }

    @Transactional
    public List<ReporteRecomendacionesFechaCorteDTO> reporteRecomendacionesFechaCorte(String fecha_corte, String dtpo, int org_control){
        return this.reportesAuditorInternoJefeRepository.reporteRecomendacionesFechaCorte(fecha_corte, dtpo, org_control);
    }

    @Transactional
    public List<ReporteRecomendacionesFechaCorteDTO> reporteRecomendacionesFechaCorte(String fecha_corte_emision, String fecha_corte_estado, String dtpo, int org_control){
        return this.reportesAuditorInternoJefeRepository.reporteRecomendacionesFechaCorte(fecha_corte_emision, fecha_corte_estado, dtpo, org_control);
    }

    @Transactional
    public List<Reporte6RecomendacionesVencidasDTO> reporteRecomendacionesVencidasAIJ(String fechaInicio, String fechaFin, int estadoSeguimiento){
        return this.reportesAuditorInternoJefeRepository.reporteRecomendacionesVencidasAIJ(fechaInicio, fechaFin, estadoSeguimiento);
    }

    @Transactional
    public List<Reporte7AuditoresDTO> reporte7Auditores(String fechaInicio, String fechaFin, String auditor){
        return this.reportesAuditorInternoJefeRepository.reporte7Auditores(fechaInicio, fechaFin, auditor);
    }

    @Transactional
    public List<ReporteGeneralAIJDTO> reporteGeneralAIJ(String fecha_inicial, String fecha_fin, int organismo_control,
            int estado_seguimiento, String auditor, String dpto) {
        return this.reportesAuditorInternoJefeRepository.reporteGeneralAIJ(fecha_inicial, fecha_fin, organismo_control,
                estado_seguimiento, auditor, dpto);
    }

    @Transactional
    public List<ReporteGeneralAIJDTO> reportePlanesYEvidenciasAIJ(String fecha_inicial, String fecha_fin, int organismo_control,
            int estado_seguimiento, String auditor, String dpto) {
        return this.reportesAuditorInternoJefeRepository.reportePlanesYEvidenciasAIJ(fecha_inicial, fecha_fin, organismo_control,
                estado_seguimiento, auditor, dpto);
    }

    @Transactional
    public ReporteProrrogasDTO reporteProrrogasAIJ() {
        return this.reportesAuditorInternoJefeRepository.reporteProrrogasAIJ();
    }

    @Transactional
    public ReporteProrrogasRealizadasDTO reporteProrrogasRealizadas() {
        return this.reportesAuditorInternoJefeRepository.reporteProrrogasRealizadas();
    }
}
