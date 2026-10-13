package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteProgresoRecomendacionesResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesVencidasResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteVerificablesDTO;

@Service
public class ReportesResponsableService {
    
    @Autowired
    private ReportesResponsableRepository reportesResponsableRepository;

    @Transactional
    public List<ReporteRecomendacionesResponsableDTO> obtenerReporteRecomendacionesResponsable(String fecha_inicial, String fecha_fin, int estado_seguimiento, String usuario) {
        return this.reportesResponsableRepository.obtenerReporteRecomendacionesResponsable(fecha_inicial, fecha_fin, estado_seguimiento, usuario);
    }

    @Transactional
    public List<ReporteProgresoRecomendacionesResponsableDTO> obtenerReporteProgresoRecomendacionesResponsable(String fecha_inicial, String fecha_fin, String usuario) {
        return this.reportesResponsableRepository.obtenerReporteProgresoRecomendacionesResponsable(fecha_inicial, fecha_fin, usuario);
    }

    @Transactional
    public List<ReporteVerificablesDTO> obtenerReporteVerificables(String fecha_inicial, String fecha_fin, String usuario) {
        return this.reportesResponsableRepository.obtenerReporteVerificables(fecha_inicial, fecha_fin, usuario);
    }

    @Transactional
    public List<ReporteRecomendacionesVencidasResponsableDTO> obtenerReporteRecomendacionesVencidasResponsable(String fecha_inicial, String fecha_fin, int estado_seguimiento, String usuario) {
        return this.reportesResponsableRepository.obtenerReporteRecomendacionesVencidasResponsable(fecha_inicial, fecha_fin, estado_seguimiento, usuario);
    }
}
