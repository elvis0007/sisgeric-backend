package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models.OrganismoControl;

@Repository
public interface OrganismosControlRepositorio extends JpaRepository<OrganismoControl, Long> {

  @Query(value = "SELECT * FROM ROOTSISREC.AI_INF_ORG_CONT WHERE AINOC_DESCRIPCION = :ainoc_descripcion", nativeQuery = true)
  OrganismoControl findByAinoc_descripcion(String ainoc_descripcion);

  @Query(value = "SELECT * FROM ROOTSISREC.AI_INF_ORG_CONT WHERE AINOC_ESTADO = '1'", nativeQuery = true)
  List<OrganismoControl> findByEstadoActivo();

}
