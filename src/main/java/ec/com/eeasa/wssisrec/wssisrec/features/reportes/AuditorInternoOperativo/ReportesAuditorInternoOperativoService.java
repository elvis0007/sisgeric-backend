package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReportePorDepartamentoEstadoSeguimientoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReporteRecomendacionesProgresoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReporteRecomendacionesVencidasOperativoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO;

import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;

@Service
public class ReportesAuditorInternoOperativoService {

    @Autowired
    private ReportesAuditorInternoOperativoRepository reportesAuditorInternoOperativoRepository;
    
    @Transactional
    public List<ReportePorDepartamentoEstadoSeguimientoDTO> obtenerReportePorDepartamentoEstadoSeguimiento(String fecha_inicial, String fecha_fin, String dpto, int estado_seguimiento, String auditor) {
        return this.reportesAuditorInternoOperativoRepository.obtenerReportePorDepartamentoEstadoSeguimiento(fecha_inicial, fecha_fin, dpto, estado_seguimiento, auditor);
    }

    @Transactional
    public List<ReporteRecomendacionesProgresoDTO> obtenerReporteRecomendacionesProgreso(String fecha_inicial, String fecha_fin, String auditor, int org_control) {
        return this.reportesAuditorInternoOperativoRepository.obtenerReporteRecomendacionesProgreso(fecha_inicial, fecha_fin, auditor, org_control);
    }

    @Transactional
    public List<ReporteRecomendacionesVencidasOperativoDTO> obtenerReporteRecomendacionesVencidasOperativo(String fecha_inicial, String fecha_fin, int estado_seguimiento, String auditor) {
        return this.reportesAuditorInternoOperativoRepository.obtenerReporteRecomendacionesVencidasOperativo(fecha_inicial, fecha_fin, estado_seguimiento, auditor);
    }

    @Transactional
    public List<ReporteGeneralAIJDTO> obtenerReporteSeguimientoResponsable(String fecha_inicial, String fecha_fin, int organismo_control, int estado_seguimiento, String auditor, String dpto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
                List<String> roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());
                boolean esAuditorJefe = roles.contains("2") || roles.contains(RolEnum.AUDITOR_JEFE_INTERNO.getCode().toString());
                if (!esAuditorJefe) {
                    // Es Auditor Operativo: forzar al usuario autenticado para restringir solo a sus informes asignados
                    auditor = auth.getName();
                }
            }
        } catch (Exception ex) {
            // Mantener valor recibido
        }
        return this.reportesAuditorInternoOperativoRepository.obtenerReporteSeguimientoResponsable(fecha_inicial, fecha_fin, organismo_control, estado_seguimiento, auditor, dpto);
    }

    @Transactional
    public List<ReporteGeneralAIJDTO> reportePlanesYEvidenciasAO(String fecha_inicial, String fecha_fin, int organismo_control, int estado_seguimiento, String auditor, String dpto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
                List<String> roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());
                boolean esAuditorJefe = roles.contains("2") || roles.contains(RolEnum.AUDITOR_JEFE_INTERNO.getCode().toString());
                if (!esAuditorJefe) {
                    // Es Auditor Operativo: forzar al usuario autenticado para restringir solo a sus informes asignados
                    auditor = auth.getName();
                }
            }
        } catch (Exception ex) {
            // Mantener valor recibido
        }
        return this.reportesAuditorInternoOperativoRepository.reportePlanesYEvidenciasAO(fecha_inicial, fecha_fin, organismo_control, estado_seguimiento, auditor, dpto);
    }
}
