package ec.com.eeasa.wssisrec.wssisrec.features.auditoria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.models.Bitacora;

public interface BitacoraRepositorio extends JpaRepository<Bitacora, String> {

  @Query(value = "SELECT * FROM ROOTSISREC.AI_SEG_BITACORA WHERE ASEBI_USUARIO LIKE %" + ":usuario"
      + "%", nativeQuery = true)
  List<Bitacora> findByUsuario(String usuario);

  @Query(value = "SELECT * FROM ROOTSISREC.AI_SEG_BITACORA WHERE TRUNC(ASEBI_FECHA) BETWEEN TO_DATE(:fechaInicio, 'YYYY-MM-DD') AND TO_DATE(:fechaFin, 'YYYY-MM-DD')", nativeQuery = true)
  List<Bitacora> findByFechaBetween(@Param("fechaInicio") String fechaInicio, @Param("fechaFin") String fechaFin);
}
