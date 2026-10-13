package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.models.Rol;

public interface RolReporitorio extends JpaRepository<Rol, Long> {
  @Query(value = "SELECT * FROM ROOTSISREC.AI_SEG_ROL WHERE ASERO_DESCRIPCION=:asero_descripcion", nativeQuery = true)
  Rol findByAsero_descripcion(String asero_descripcion);
}
