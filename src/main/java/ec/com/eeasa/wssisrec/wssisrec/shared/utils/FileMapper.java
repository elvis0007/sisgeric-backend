package ec.com.eeasa.wssisrec.wssisrec.shared.utils;

public class FileMapper {

  public static String getFileName(String originalName, String fileName) {
    String ext = originalName.substring(originalName.lastIndexOf(".") + 1);
    String fileNameNew = fileName + "-" + DateMapper.convertirFechaActual() + "." + ext;
    return fileNameNew;

  }

  public static String obtenerRutaArchivoFtp(String directorio, String organismoControl, Long codigo,
      String titulo) {
    // Año + "/" + OrganismoControl + "/" +Informe (+ Codigo) + "/"+informe + "/"
    return directorio + DateMapper.convertirAñoActual() + "/" + organismoControl + "/" + "Informe" + codigo + "/"
        + titulo
        + "/";
  }

  public static String obtenerRutaArchivoBd(String nombreHost, String rutaRelativaFtp, String nombreArchivo) {
    return nombreHost + rutaRelativaFtp + nombreArchivo;
  }
}
