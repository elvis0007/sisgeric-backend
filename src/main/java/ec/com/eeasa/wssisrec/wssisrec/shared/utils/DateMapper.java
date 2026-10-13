package ec.com.eeasa.wssisrec.wssisrec.shared.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DateMapper {
  private static final Logger logger = LoggerFactory.getLogger(DateMapper.class);
  public static LocalDate toLocalDate(Date date) {
    if (date == null) {
      return null;
    }
    if (date instanceof java.sql.Date) {
      return ((java.sql.Date) date).toLocalDate();
    }
    if (date instanceof java.sql.Timestamp) {
      return ((java.sql.Timestamp) date).toLocalDateTime().toLocalDate();
    }
    return new java.sql.Date(date.getTime()).toLocalDate();
  }

  public static String convertirFechaActual() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String datetime = dtf.format(LocalDateTime.now());
    return datetime;
  }

  public static String convertirAñoActual() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
    String datetime = dtf.format(LocalDateTime.now());
    return datetime;
  }

  public static String convertirFechaFormato(Date date) {
    if (date == null) {
      return null;
    }
    LocalDate localDate = toLocalDate(date);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String datetime = dtf.format(localDate);
    return datetime;
  }

  public static Date convertirFechaFormato(String date) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return dateFormat.parse(date);
    } catch (Exception e) {
      logger.error("Error al convertir la fecha: " + e.getMessage());
      return new Date();
    }
  }

  public static int calcularDias(Date date) {
    if (date == null) {
      return 0;
    }
    try {
      // Predicado para fines de semana (sábado y domingo como días no laborables)
      Predicate<LocalDate> esFinSemana = fecha -> fecha.getDayOfWeek() == DayOfWeek.SATURDAY
          || fecha.getDayOfWeek() == DayOfWeek.SUNDAY;

      // Convertir Date a LocalDate
      LocalDate endDate = toLocalDate(date);
      if (endDate == null) {
        return 0;
      }
      LocalDate startDate = LocalDate.now();

      // Determinar si las fechas están invertidas
      boolean fechasInvertidas = startDate.isAfter(endDate);

      // Asegurar que siempre calcule en orden
      LocalDate inicio = fechasInvertidas ? endDate : startDate;
      LocalDate fin = fechasInvertidas ? startDate : endDate;

      // Calcular días entre las dos fechas
      long daysBetween = ChronoUnit.DAYS.between(inicio, fin);

      // Contar días laborales (Lunes a Viernes, consistente con FN_CALCULAR_DIAS_LABORABLES en base de datos)
      long workDays = Stream.iterate(inicio, fecha -> fecha.plusDays(1))
          .limit(daysBetween)
          .filter(esFinSemana.negate())
          .count();

      // Si las fechas están invertidas, el resultado será negativo
      int resultado = (int) (fechasInvertidas ? -workDays : workDays);

      return resultado;
    } catch (Exception e) {
      logger.error("Error al calcular los días: " + e.getMessage());
      return -1;
    }
  }

}
