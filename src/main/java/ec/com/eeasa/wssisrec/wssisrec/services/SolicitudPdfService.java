package ec.com.eeasa.wssisrec.wssisrec.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SolicitudPdfService {
  private static final Logger logger = LoggerFactory.getLogger(SolicitudPdfService.class);

  public byte[] generarPdfAprobacion(byte[] pdfOriginal, String aprobadorNombre, String cargo, String departamento) {
    return generarPdfAprobacion(pdfOriginal, aprobadorNombre, cargo, departamento, false, false);
  }

  public byte[] generarPdfAprobacion(byte[] pdfOriginal, String aprobadorNombre, String cargo, String departamento,
      boolean esAuditor, boolean colocarDebajoDirector) {
    if (pdfOriginal == null) {
      logger.error("PDF original nulo");
      return null;
    }

    try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfOriginal));
        ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

      if (document.getNumberOfPages() == 0) {
        logger.error("El PDF no contiene páginas");
        return null;
      }

      PDPage page = document.getPage(document.getNumberOfPages() - 1);
      PDPageContentStream content = new PDPageContentStream(document, page, AppendMode.APPEND, true, true);

      float centerX = 420f; // <- Disminuye (ej. 420f) para mover a la izquierda. Aumenta (ej. 460f) para la
                // derecha.
      float currentY = 140f;
      if (esAuditor && colocarDebajoDirector) {
        // colocar debajo de la firma del director (misma posición previa usada antes)
        currentY = 92f;
      }
      float espaciadoLineas = 14f;
      float maxTextWidth = 220f;

      if (esAuditor) {
        // 1. Limpiar SOLO el área del texto para NO borrar los bordes del cuadro
        // general ni la línea inferior
        content.setNonStrokingColor(255, 255, 255);
        if (esAuditor && colocarDebajoDirector) {
          content.addRect(345f, 82f, 175f, 18f);
        } else {
          content.addRect(315f, 100f, 210f, 62f);
        }
        content.fill();

        // Restaurar color a negro para el texto
        content.setNonStrokingColor(0, 0, 0);

        // 2. Dibujar el texto del auditor. Cuando la solicitud viene de un
        // Responsable, el auditor va en una sola línea pequeña a la derecha del
        // cuadro de aprobación, debajo de la firma del director.
        String nombre = (aprobadorNombre != null ? aprobadorNombre : "").toUpperCase();
        if (esAuditor && colocarDebajoDirector) {
          String textoAuditor = "APROBADO POR : " + nombre;
          float auditorFontSize = 7.5f;
          float auditorWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(textoAuditor) / 1000f) * auditorFontSize;

          while (auditorWidth > 165f && auditorFontSize > 6f) {
            auditorFontSize -= 0.25f;
            auditorWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(textoAuditor) / 1000f) * auditorFontSize;
          }

          content.beginText();
          content.setFont(PDType1Font.HELVETICA_BOLD, auditorFontSize);
          content.newLineAtOffset(505f - auditorWidth, currentY);
          content.showText(textoAuditor);
          content.endText();
        } else {
          float nameFontSize = 10f;
          float nameWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(nombre) / 1000f) * nameFontSize;

          while (nameWidth > maxTextWidth && nameFontSize > 7f) {
            nameFontSize -= 0.5f;
            nameWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(nombre) / 1000f) * nameFontSize;
          }

          content.beginText();
          content.setFont(PDType1Font.HELVETICA_BOLD, nameFontSize);
          content.newLineAtOffset(centerX - (nameWidth / 2f), currentY);
          content.showText(nombre);
          content.endText();

          currentY -= espaciadoLineas;
          String cargoText = cargo != null ? ("CARGO: " + cargo.toUpperCase()) : "";
          float cargoFontSize = 9f;
          float cargoWidth = (PDType1Font.HELVETICA.getStringWidth(cargoText) / 1000f) * cargoFontSize;

          while (cargoWidth > maxTextWidth && cargoFontSize > 6f) {
            cargoFontSize -= 0.5f;
            cargoWidth = (PDType1Font.HELVETICA.getStringWidth(cargoText) / 1000f) * cargoFontSize;
          }

          content.beginText();
          content.setFont(PDType1Font.HELVETICA, cargoFontSize);
          content.newLineAtOffset(centerX - (cargoWidth / 2f), currentY);
          content.showText(cargoText);
          content.endText();

          currentY -= espaciadoLineas;
          String deptoText = departamento != null ? departamento.toUpperCase() : "";
          float deptoFontSize = 9f;
          float deptoWidth = (PDType1Font.HELVETICA.getStringWidth(deptoText) / 1000f) * deptoFontSize;

          while (deptoWidth > maxTextWidth && deptoFontSize > 6f) {
            deptoFontSize -= 0.5f;
            deptoWidth = (PDType1Font.HELVETICA.getStringWidth(deptoText) / 1000f) * deptoFontSize;
          }

          content.beginText();
          content.setFont(PDType1Font.HELVETICA, deptoFontSize);
          content.newLineAtOffset(centerX - (deptoWidth / 2f), currentY);
          content.showText(deptoText);
          content.endText();
        }
      } else {
        // --- 1. Nombre del Aprobador ---
        String nombre = (aprobadorNombre != null ? aprobadorNombre : "").toUpperCase();
        float nameFontSize = 10f;
        float nameWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(nombre) / 1000f) * nameFontSize;

        // Reducir fuente si es muy largo
        while (nameWidth > maxTextWidth && nameFontSize > 7f) {
          nameFontSize -= 0.5f;
          nameWidth = (PDType1Font.HELVETICA_BOLD.getStringWidth(nombre) / 1000f) * nameFontSize;
        }

        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, nameFontSize);
        content.newLineAtOffset(centerX - (nameWidth / 2f), currentY);
        content.showText(nombre);
        content.endText();

        // --- 2. Cargo ---
        currentY -= espaciadoLineas;
        String cargoText = cargo != null ? ("CARGO: " + cargo.toUpperCase()) : "";
        float cargoFontSize = 9f;
        float cargoWidth = (PDType1Font.HELVETICA.getStringWidth(cargoText) / 1000f) * cargoFontSize;

        // Reducir fuente si es muy largo (ej. "JEFE DE SECCION 3 LOEP - DIRECTOR LOEP
        // (E)")
        while (cargoWidth > maxTextWidth && cargoFontSize > 6f) {
          cargoFontSize -= 0.5f;
          cargoWidth = (PDType1Font.HELVETICA.getStringWidth(cargoText) / 1000f) * cargoFontSize;
        }

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, cargoFontSize);
        content.newLineAtOffset(centerX - (cargoWidth / 2f), currentY);
        content.showText(cargoText);
        content.endText();

        // --- 3. Departamento ---
        currentY -= espaciadoLineas;
        String deptoText = departamento != null ? departamento.toUpperCase() : "";
        float deptoFontSize = 9f;
        float deptoWidth = (PDType1Font.HELVETICA.getStringWidth(deptoText) / 1000f) * deptoFontSize;

        while (deptoWidth > maxTextWidth && deptoFontSize > 6f) {
          deptoFontSize -= 0.5f;
          deptoWidth = (PDType1Font.HELVETICA.getStringWidth(deptoText) / 1000f) * deptoFontSize;
        }

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, deptoFontSize);
        content.newLineAtOffset(centerX - (deptoWidth / 2f), currentY);
        content.showText(deptoText);
        content.endText();
      }

      content.close();
      document.save(salida);
      return salida.toByteArray();

    } catch (Exception e) {
      logger.error("Error agregando datos al PDF: {}", e.getMessage(), e);
      return null;
    }
  }
}