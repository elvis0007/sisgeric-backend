package ec.com.eeasa.wssisrec.wssisrec.features.dashboards;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.Dashboard1DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAIJS2DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAIJS3DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAIOS1DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAuditadoS1DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardListaInformesAIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardListaInformesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardListaVerificablesResponsableDTO;

/*
 * Autor: Mateo Díaz
 */

@Service
public class DashboardService {
    //VAR
    @Autowired
    DashboardRepository dashboardRepository;


    @Transactional
    public Dashboard1DTO obtenerDetalleRecomendacionesDashboard(){
        return this.dashboardRepository.obtenerReporteDptos();
    }

    @Transactional
    public DashboardAIJS2DTO obtenerReporteRecAnios(){
        return this.dashboardRepository.obtenerReporteRecAnios();
    }

    @Transactional
    public DashboardAIJS3DTO obtenerReporteAnios(){
        return this.dashboardRepository.obtenerReporteAnios();
    }

    @Transactional
    public DashboardAIOS1DTO obtenerDatosPorDptoAIO(String arere_usuario){
        return this.dashboardRepository.obtenerDatosDptosAIO(arere_usuario);
    }

    @Transactional
    public DashboardAIJS3DTO obtenerDatos7AniosAIO(String arere_usuario){
        return this.dashboardRepository.obtenerDatos7AniosAIO(arere_usuario);
    }

    @Transactional
    public DashboardAIJS2DTO obtenerDatosPorDptoAnioAIO(String arere_usuario){
        return this.dashboardRepository.obtenerDatosPorDptoFiltroAIO(arere_usuario);
    }

    @Transactional
    public Dashboard1DTO obtenerDatosDirectoDptoS1(String nombre_dpto){
        return this.dashboardRepository.obtenerDatosDirectorDpto(nombre_dpto);
    }

    @Transactional
    public DashboardAIJS3DTO obtenerDatos7aniosS2Dpto(String nombre_dpto){
        return this.dashboardRepository.obtenerDatos7aniosS2Dpto(nombre_dpto);
    }

    @Transactional
    public DashboardListaInformesDTO obteneDashboardListaInformesDptoS3(String nombre_dpto){
        return this.dashboardRepository.obtenerDatosListaInformesDptoS3(nombre_dpto);
    }

    @Transactional
    public DashboardAuditadoS1DTO obtenerMetricasAuditadoS1(String arers_usuario_per){
        return this.dashboardRepository.obtenerMetricasAuditadoS1(arers_usuario_per);
    }

    @Transactional
    public DashboardAIJS3DTO obtenerReporte7AniosRecomendacionesAuditado(String arers_usuario_per){
        return this.dashboardRepository.obtenerReporte7AniosRecomendacionesAuditado(arers_usuario_per);
    }

    @Transactional
    public DashboardListaInformesDTO obtenerListaInformesAuditado(String arers_usuario_per){
        return this.dashboardRepository.obtenerListaInformesAuditado(arers_usuario_per);
    }

    @Transactional
    public DashboardListaInformesAIJDTO obtenerListaInformesAIS4(){
        return this.dashboardRepository.obtenerListaInformesAIS4();
    }

    @Transactional
    public DashboardListaVerificablesResponsableDTO obtenerListaVerificablesResponsable(String usu_per){
        return this.dashboardRepository.obtenerListaVerificablesResponsable(usu_per);
    }
}
