package ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto.ReporteRecomendacionesPorDepartamentoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto.ReporteRecomendacionesPorDptoBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto.ReporteDepartamentoFechasVencidasDTO;

@Service
public class ReportesDirectorDepartamentalService {
    @Autowired
    private ReportesDirectorDepartamentalRepository reportesDirectorDepartamentalRepository;

    @Transactional
    public List<ReporteRecomendacionesPorDepartamentoDTO> obtenerReporteRecomendacionesPorDepartamento(ReporteRecomendacionesPorDptoBody reporteRecomendacionesPorDptoBody) {  
        return this.reportesDirectorDepartamentalRepository.obtenerReporteRecomendacionesPorDepartamento(reporteRecomendacionesPorDptoBody);
    }

    @Transactional
    public List<ReporteDepartamentoFechasVencidasDTO> obtenerReporteDepartamentoFechasVencidas(ReporteRecomendacionesPorDptoBody reporteRecomendacionesPorDptoBody) {  
        return this.reportesDirectorDepartamentalRepository.obtenerReporteDepartamentoFechasVencidas(reporteRecomendacionesPorDptoBody);
    }
}
