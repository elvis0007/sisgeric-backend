package ec.com.eeasa.wssisrec.wssisrec.shared.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Autor: Mateo Díaz
 */

public class UtilsFunctions {

    // Obtener fecha y hora actual
    public static String getDateHour() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaHoraFormateada = localDateTime.format(dateTimeFormatter);
        return fechaHoraFormateada;
    }
}
