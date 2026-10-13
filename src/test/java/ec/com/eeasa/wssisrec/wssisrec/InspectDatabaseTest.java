package ec.com.eeasa.wssisrec.wssisrec;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.Types;

public class InspectDatabaseTest {

    @Disabled("Prueba de diagnóstico de base de datos")
    @Test
    public void testBuscarUsuarioYRecomendaciones() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. BUSCANDO RESPONSABLE ERAZO ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARERS_CODIGO, ARERS_NOMBRE, ARERS_APELLIDO, ARERS_DEPARTAMENTO, ARERS_ESTADO " +
                    "FROM AI_REC_RESPONSABLES WHERE UPPER(ARERS_NOMBRE || ' ' || ARERS_APELLIDO) LIKE '%ERAZO%'");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("ARERS_CODIGO: %d | NOMBRE: %s %s | DPTO: %s | ESTADO: %s",
                            rs.getLong("ARERS_CODIGO"), rs.getString("ARERS_NOMBRE"),
                            rs.getString("ARERS_APELLIDO"), rs.getString("ARERS_DEPARTAMENTO"),
                            rs.getString("ARERS_ESTADO")));
                }
            }

            System.out.println("\n=== 2. BUSCANDO INFORMES CON 045 / 45 / 445 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT AININ_CODIGO, AININ_MEMORANDO, AININ_TITULO, AININ_ESTADO " +
                    "FROM AI_INF_INFORMES WHERE AININ_MEMORANDO LIKE '%45%' OR AININ_MEMORANDO LIKE '%445%'");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("AININ_CODIGO: %d | MEMO: %s | ESTADO: %s | TITULO: %s",
                            rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"),
                            rs.getString("AININ_ESTADO"), rs.getString("AININ_TITULO")));
                }
            }

            System.out.println("\n=== 3. BUSCANDO RECOMENDACIONES DE ERAZO ===");
            String sqlRec = 
                "SELECT INF.AININ_MEMORANDO, REC.ARERE_CODIGO, REC.ARERE_NUM_REC, " +
                "TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD') AS REC_FECHA_MAX, " +
                "TO_CHAR(REC.ARERE_FECHA_INICIO_CONTROL, 'YYYY-MM-DD') AS REC_FECHA_INI, " +
                "DR.AREDR_CODIGO, TO_CHAR(DR.AREDR_FECHA_MAXIMA, 'YYYY-MM-DD') AS DR_FECHA_MAX, " +
                "RESP.ARERS_CODIGO, RESP.ARERS_NOMBRE, RESP.ARERS_APELLIDO, " +
                "SUBSTR(REC.ARERE_DESCRIPCION, 1, 80) AS DESCRIPCION " +
                "FROM AI_INF_INFORMES INF " +
                "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                "JOIN AI_REC_DETALLE_RECOMENDACION DR ON REC.ARERE_CODIGO = DR.ARERE_CODIGO " +
                "JOIN AI_REC_RESPONSABLES RESP ON DR.ARERS_CODIGO = RESP.ARERS_CODIGO " +
                "WHERE UPPER(RESP.ARERS_NOMBRE || ' ' || RESP.ARERS_APELLIDO) LIKE '%ERAZO%' " +
                "ORDER BY INF.AININ_MEMORANDO, REC.ARERE_NUM_REC";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlRec);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INFORME: %s | REC_ID: %d | NUM: %s | REC_FMAX: %s | AREDR_ID: %d | DR_FMAX: %s | RESP: %s %s | DESC: %s",
                            rs.getString("AININ_MEMORANDO"), rs.getLong("ARERE_CODIGO"),
                            rs.getString("ARERE_NUM_REC"), rs.getString("REC_FECHA_MAX"),
                            rs.getLong("AREDR_CODIGO"), rs.getString("DR_FECHA_MAX"),
                            rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO"),
                            rs.getString("DESCRIPCION")));
                }
            }

            System.out.println("\n=== 4. BUSCANDO PRORROGAS/SOLICITUDES PARA ESAS RECS DE ERAZO ===");
            String sqlSol = 
                "SELECT S.ARESO_CODIGO, S.ARERE_CODIGO, S.ARERS_CODIGO, S.ARESO_ESTADO, S.ARESO_ESTADO_APROB, " +
                "TO_CHAR(S.ARESO_FECHA_MAX_ANT, 'YYYY-MM-DD') AS F_ANT, " +
                "TO_CHAR(S.ARESO_FECHA_MAX_NUE, 'YYYY-MM-DD') AS F_NUE, " +
                "S.ARESO_MOTIVO " +
                "FROM AI_REC_SOLICITUDES S " +
                "JOIN AI_REC_RESPONSABLES RESP ON S.ARERS_CODIGO = RESP.ARERS_CODIGO " +
                "WHERE UPPER(RESP.ARERS_NOMBRE || ' ' || RESP.ARERS_APELLIDO) LIKE '%ERAZO%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlSol);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("SOL_ID: %d | REC_ID: %d | RESP_ID: %d | ESTADO: %s | APROB: %s | F_ANT: %s | F_NUE: %s | MOTIVO: %s",
                            rs.getLong("ARESO_CODIGO"), rs.getLong("ARERE_CODIGO"),
                            rs.getLong("ARERS_CODIGO"), rs.getString("ARESO_ESTADO"),
                            rs.getString("ARESO_ESTADO_APROB"), rs.getString("F_ANT"),
                            rs.getString("F_NUE"), rs.getString("ARESO_MOTIVO")));
                }
            }

            System.out.println("=== INSPECTING RECOMMENDATION IN SCREENSHOT ===");
            String sql = "SELECT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, RES.ARERS_NOMBRE, RES.ARERS_APELLIDO, DR.AREDR_CODIGO, " +
                         "V.AREVE_CODIGO, V.AREEV_CODIGO, EV.AREEV_DESCRIPCION, V.AREVE_FECHA_CARGA " +
                         "FROM AI_REC_RECOMENDACIONES REC " +
                         "JOIN AI_REC_DETALLE_RECOMENDACION DR ON REC.ARERE_CODIGO = DR.ARERE_CODIGO " +
                         "JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
                         "LEFT JOIN AI_REC_VERIFICABLES V ON DR.AREDR_CODIGO = V.AREDR_CODIGO AND V.AREVE_ESTADO = '1' " +
                         "LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON V.AREEV_CODIGO = EV.AREEV_CODIGO " +
                         "WHERE REC.ARERE_DESCRIPCION LIKE '%Relaciones Industriales%' " +
                         "ORDER BY REC.ARERE_CODIGO, RES.ARERS_APELLIDO, V.AREVE_FECHA_CARGA DESC";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("REC: %d | NUM: %s | RESP: %s %s | AREDR: %d | VER: %s | EST: %s (%s) | FECHA: %s",
                            rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"),
                            rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO"),
                            rs.getLong("AREDR_CODIGO"), rs.getString("AREVE_CODIGO"),
                            rs.getString("AREEV_CODIGO"), rs.getString("AREEV_DESCRIPCION"),
                            rs.getString("AREVE_FECHA_CARGA")));
                }
            }

            System.out.println("=== 1. INFORME EN AI_INF_INFORMES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT AININ_CODIGO, AININ_MEMORANDO, AININ_TITULO, AININ_ESTADO, " +
                    "TO_CHAR(AININ_FECHA_APROBACION, 'YYYY-MM-DD') AS F_APROB, " +
                    "TO_CHAR(AININ_FECHA_MEMO, 'YYYY-MM-DD') AS F_MEMO " +
                    "FROM AI_INF_INFORMES WHERE AININ_MEMORANDO LIKE '%057-2023%' OR AININ_CODIGO = 9");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("AININ_CODIGO: %d | MEMORANDO: %s | ESTADO: %s | F_APROB: %s | F_MEMO: %s | TITULO: %s",
                            rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"),
                            rs.getString("AININ_ESTADO"), rs.getString("F_APROB"),
                            rs.getString("F_MEMO"), rs.getString("AININ_TITULO")));
                }
            }

            System.out.println("\n=== 2. REGISTROS EN AI_CORTE_EVALUACION_INFORME (PARA AININ 9) ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ci.ARCEI_CODIGO, ci.ARCEV_CODIGO, ev.ARCEV_NOMBRE, ev.ARCEV_TIPO, " +
                    "ci.AININ_CODIGO, ci.ARCEI_ES_MANUAL, " +
                    "TO_CHAR(ci.ARCEI_FECHA_REGISTRO, 'YYYY-MM-DD HH24:MI:SS') AS F_REG " +
                    "FROM AI_CORTE_EVALUACION_INFORME ci " +
                    "JOIN AI_CORTE_EVALUACION ev ON ci.ARCEV_CODIGO = ev.ARCEV_CODIGO " +
                    "WHERE ci.AININ_CODIGO = 9 " +
                    "ORDER BY ci.ARCEV_CODIGO, ci.ARCEI_CODIGO");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("ARCEI: %d | PERIODO_ID: %d (%s) | TIPO: %s | MANUAL: %d | REGISTRO: %s",
                            rs.getLong("ARCEI_CODIGO"), rs.getLong("ARCEV_CODIGO"),
                            rs.getString("ARCEV_NOMBRE"), rs.getString("ARCEV_TIPO"),
                            rs.getInt("ARCEI_ES_MANUAL"), rs.getString("F_REG")));
                }
            }

            System.out.println("\n=== 3. RECOMENDACIONES DEL INFORME AI-057-2023 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.ARERE_CODIGO, r.ARERE_NUM_REC, r.ARERE_DESCRIPCION, es.AREES_DESCRIPCION " +
                    "FROM AI_INF_INFORMES i " +
                    "JOIN AI_REC_HALLAZGOS h ON i.AININ_CODIGO = h.AININ_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO " +
                    "JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO " +
                    "JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.AREES_CODIGO = es.AREES_CODIGO " +
                    "WHERE i.AININ_CODIGO = 9");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("REC_ID: %d | NUM: %s | ESTADO: %s | DESC: %s",
                            rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"),
                            rs.getString("AREES_DESCRIPCION"), rs.getString("ARERE_DESCRIPCION")));
                }
            }

            System.out.println("\n=== 4. TOTAL INFORMES EN PERIODO 5 (Cierre de Año 2026) ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS TOTAL, COUNT(DISTINCT AININ_CODIGO) AS UNICOS " +
                    "FROM AI_CORTE_EVALUACION_INFORME WHERE ARCEV_CODIGO = 5");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(String.format("TOTAL FILAS EN BD: %d | INFORMES UNICOS: %d",
                            rs.getInt("TOTAL"), rs.getInt("UNICOS")));
                }
            }

            String newSql = "SELECT DR.ARERE_CODIGO, " +
              "LISTAGG(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO || ' (' || COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') || ')', ', ') " +
              "WITHIN GROUP (ORDER BY RES.ARERS_NOMBRE, RES.ARERS_APELLIDO) AS RESPONSABLES " +
              "FROM AI_REC_RESPONSABLES RES " +
              "JOIN AI_REC_DETALLE_RECOMENDACION DR ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
              "JOIN AI_REC_RECOMENDACIONES REC ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
              "LEFT JOIN (" +
              "  SELECT AREDR_CODIGO, AREEV_CODIGO, ROW_NUMBER() OVER (" +
              "    PARTITION BY AREDR_CODIGO " +
              "    ORDER BY " +
              "      CASE " +
              "        WHEN AREEV_CODIGO = 2 THEN 1 " +
              "        WHEN AREEV_CODIGO = 1 THEN 2 " +
              "        WHEN AREEV_CODIGO = 3 THEN 3 " +
              "        ELSE 4 " +
              "      END ASC, " +
              "      NVL(AREVE_FECHA_CARGA, AREVE_FECHA) DESC NULLS LAST, " +
              "      AREVE_CODIGO DESC" +
              "  ) AS rn " +
              "  FROM AI_REC_VERIFICABLES " +
              "  WHERE TO_CHAR(AREVE_ESTADO) = '1'" +
              ") ULT ON DR.AREDR_CODIGO = ULT.AREDR_CODIGO AND ULT.rn = 1 " +
              "LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON ULT.AREEV_CODIGO = EV.AREEV_CODIGO " +
              "WHERE RES.ARERS_ESTADO = 1 AND DR.AREDR_ESTADO = 1 AND REC.ARERE_ESTADO = 1 " +
              "AND REC.ARERE_CODIGO = 847 " +
              "GROUP BY DR.ARERE_CODIGO";
            try (java.sql.PreparedStatement ps2 = conn.prepareStatement(newSql);
                 java.sql.ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    String respStr = rs2.getString("RESPONSABLES");
                    System.out.println("RESULT FOR REC 847: " + respStr);
                    org.junit.jupiter.api.Assertions.assertTrue(
                        respStr.contains("ALEXANDRA GUADALUPE RODRIGUEZ BETANCOURT (JUSTIFICA)"),
                        "Alexandra must be JUSTIFICA because she has an approved deliverable!"
                    );
                    org.junit.jupiter.api.Assertions.assertTrue(
                        respStr.contains("MERCEDES ISABEL YANEZ BAYAS (JUSTIFICA)"),
                        "Mercedes must be JUSTIFICA!"
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico de base de datos")
    @Test
    public void testDiagnoseAuditorOperativo() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String[] testAuditors = {"rmerino", "mjperez", "anicolalde", "jtipantaxi", "mguevara", "cnunez", "dcadme"};
            for (String aud : testAuditors) {
                String sqlTest = "SELECT " +
                        "COUNT(DISTINCT CASE WHEN INF.AININ_USUARIO = ? THEN REC.ARERE_CODIGO END) as cnt_ainin, " +
                        "COUNT(DISTINCT CASE WHEN REC.ARERE_USUARIO = ? THEN REC.ARERE_CODIGO END) as cnt_arere, " +
                        "COUNT(DISTINCT CASE WHEN (REC.ARERE_USUARIO = ? OR INF.AININ_USUARIO = ?) THEN REC.ARERE_CODIGO END) as cnt_or, " +
                        "COUNT(DISTINCT CASE WHEN NVL(REC.ARERE_USUARIO, INF.AININ_USUARIO) = ? THEN REC.ARERE_CODIGO END) as cnt_nvl " +
                        "FROM AI_INF_INFORMES INF " +
                        "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                        "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                        "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                        "JOIN AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO " +
                        "JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO " +
                        "JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO " +
                        "WHERE INF.AININ_ESTADO = 1 AND HALL.AREHA_ESTADO = 1 AND CON.ARECO_ESTADO = 1 " +
                        "AND REC.ARERE_ESTADO = 1 AND ORGCON.AINOC_ESTADO = 1 AND DET.AREDR_ESTADO = 1 AND RESP.ARERS_ESTADO = 1";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlTest)) {
                    ps.setString(1, aud);
                    ps.setString(2, aud);
                    ps.setString(3, aud);
                    ps.setString(4, aud);
                    ps.setString(5, aud);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println(String.format("AUDITOR: %-12s | AININ: %3d | ARERE: %3d | OR: %3d | NVL: %3d",
                                    aud, rs.getInt("cnt_ainin"), rs.getInt("cnt_arere"),
                                    rs.getInt("cnt_or"), rs.getInt("cnt_nvl")));
                        }
                    }
                }
            }

            System.out.println("\n=== INSPECTING anicolalde 99 RECS in AI_REC_RECOMENDACIONES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT R.ARERE_CODIGO, R.ARERE_NUM_REC, R.ARERE_ESTADO, R.ARERE_USUARIO, " +
                    "INF.AININ_CODIGO, INF.AININ_USUARIO, INF.AININ_ESTADO " +
                    "FROM AI_REC_RECOMENDACIONES R " +
                    "JOIN AI_REC_CONCLUSIONES C ON R.ARECO_CODIGO = C.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS H ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                    "JOIN AI_INF_INFORMES INF ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                    "WHERE R.ARERE_USUARIO = 'anicolalde' AND ROWNUM <= 10");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("REC_ID: %d | REC_NUM: %s | REC_EST: %s | REC_USU: %s | INF_ID: %d | INF_USU: %s | INF_EST: %s",
                            rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"),
                            rs.getString("ARERE_ESTADO"), rs.getString("ARERE_USUARIO"),
                            rs.getLong("AININ_CODIGO"), rs.getString("AININ_USUARIO"),
                            rs.getString("AININ_ESTADO")));
                }
            }

            System.out.println("\n=== SOURCE OF ObtenerDataGraficaPorAnioAIOS3 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT TEXT FROM ALL_SOURCE WHERE UPPER(NAME) = 'OBTENERDATAGRAFICAPORANIOAIOS3' ORDER BY LINE");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.print(rs.getString("TEXT"));
                }
                if (!found) System.out.println("NOT FOUND!");
            }
            try (CallableStatement cs = conn.prepareCall(
                    "DECLARE\n" +
                    "  v_cnt1 NUMBER;\n" +
                    "  v_cnt2 NUMBER;\n" +
                    "  p_user VARCHAR2(50) := 'rmerino';\n" +
                    "BEGIN\n" +
                    "  SELECT COUNT(DISTINCT REC.ARERE_CODIGO) INTO v_cnt1\n" +
                    "  FROM AI_INF_INFORMES INF\n" +
                    "  JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO\n" +
                    "  JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO\n" +
                    "  JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO\n" +
                    "  JOIN AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO\n" +
                    "  JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO\n" +
                    "  JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO\n" +
                    "  WHERE INF.AININ_ESTADO = 1 AND HALL.AREHA_ESTADO = 1 AND CON.ARECO_ESTADO = 1\n" +
                    "    AND REC.ARERE_ESTADO = 1 AND ORGCON.AINOC_ESTADO = 1 AND DET.AREDR_ESTADO = 1 AND RESP.ARERS_ESTADO = 1\n" +
                    "    AND (arere_usuario IS NULL OR arere_usuario = '0' OR INF.AININ_USUARIO = arere_usuario);\n" +
                    "\n" +
                    "  SELECT COUNT(DISTINCT REC.ARERE_CODIGO) INTO v_cnt2\n" +
                    "  FROM AI_INF_INFORMES INF\n" +
                    "  JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO\n" +
                    "  JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO\n" +
                    "  JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO\n" +
                    "  JOIN AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO\n" +
                    "  JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO\n" +
                    "  JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO\n" +
                    "  WHERE INF.AININ_ESTADO = 1 AND HALL.AREHA_ESTADO = 1 AND CON.ARECO_ESTADO = 1\n" +
                    "    AND REC.ARERE_ESTADO = 1 AND ORGCON.AINOC_ESTADO = 1 AND DET.AREDR_ESTADO = 1 AND RESP.ARERS_ESTADO = 1\n" +
                    "    AND (p_user IS NULL OR p_user = '0' OR INF.AININ_USUARIO = p_user);\n" +
                    "\n" +
                    "  ? := v_cnt1;\n" +
                    "  ? := v_cnt2;\n" +
                    "END;")) {
                cs.registerOutParameter(1, Types.INTEGER);
                cs.registerOutParameter(2, Types.INTEGER);
                cs.execute();
                System.out.println(String.format("CON SOMBRA (arere_usuario column): %d | SIN SOMBRA (p_user rmerino): %d",
                        cs.getInt(1), cs.getInt(2)));
            }

            System.out.println("\n=== ARERE_USUARIO in AI_REC_RECOMENDACIONES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARERE_USUARIO, COUNT(*) as cnt FROM AI_REC_RECOMENDACIONES GROUP BY ARERE_USUARIO");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("ARERE_USUARIO: '%s' | COUNT: %d", rs.getString("ARERE_USUARIO"), rs.getInt("cnt")));
                }
            }

            System.out.println("\n=== TESTING rec_estatus_aio WHERE INF.AININ_USUARIO = 'rmerino' vs 'anicolalde' ===");
            String[] testUsers = {"rmerino", "anicolalde", "jtipantaxi", "mjperez", "mguevara"};
            for (String tu : testUsers) {
                String qCount = "SELECT COUNT(DISTINCT REC.ARERE_CODIGO) as total_rec " +
                        "FROM AI_INF_INFORMES INF " +
                        "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                        "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                        "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                        "JOIN AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO " +
                        "JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO " +
                        "JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO " +
                        "WHERE INF.AININ_ESTADO = 1 AND HALL.AREHA_ESTADO = 1 AND CON.ARECO_ESTADO = 1 " +
                        "AND REC.ARERE_ESTADO = 1 AND ORGCON.AINOC_ESTADO = 1 AND DET.AREDR_ESTADO = 1 AND RESP.ARERS_ESTADO = 1 " +
                        "AND INF.AININ_USUARIO = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(qCount)) {
                    ps.setString(1, tu);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("USER " + tu + " -> RECS CON AININ_USUARIO = " + rs.getInt("total_rec"));
                        }
                    }
                }
            }

            System.out.println("\n=== SOLICITUDES IN DB (AI_REC_SOLICITUDES) ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT S.ARESO_CODIGO, S.ARERE_CODIGO, S.ARESO_ESTADO, S.ARESO_ESTADO_APROB, S.ARESO_FECHA_CARGA, S.ARESO_USUARIO, " +
                    "INF.AININ_USUARIO, R.ARERE_NUM_REC " +
                    "FROM AI_REC_SOLICITUDES S " +
                    "JOIN AI_REC_RECOMENDACIONES R ON S.ARERE_CODIGO = R.ARERE_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES CON ON R.ARECO_CODIGO = CON.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS H ON CON.AREHA_CODIGO = H.AREHA_CODIGO " +
                    "JOIN AI_INF_INFORMES INF ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                    "WHERE S.ARESO_ESTADO = '1' ORDER BY S.ARESO_CODIGO DESC");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("SOL_ID: %d | REC: %s | EST_APROB: %s | EST: %s | AUDITOR_INF: %s | SOL_USU: %s",
                            rs.getLong("ARESO_CODIGO"), rs.getString("ARERE_NUM_REC"),
                            rs.getString("ARESO_ESTADO_APROB"), rs.getString("ARESO_ESTADO"),
                            rs.getString("AININ_USUARIO"), rs.getString("ARESO_USUARIO")));
                }
            }

            System.out.println("\n=== EXECUTING ReporteDashboardS1AIO FOR EACH USER ===");
            String[] users = {"rmerino", "anicolalde", "jtipantaxi", "mjperez", "mguevara"};
            for (String u : users) {
                try (CallableStatement cs = conn.prepareCall("{call ReporteDashboardS1AIO(?, ?)}")) {
                    cs.setString(1, u);
                    cs.registerOutParameter(2, Types.CLOB);
                    cs.execute();
                    java.sql.Clob clob = cs.getClob(2);
                    String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "null";
                    System.out.println("USER: " + u + " -> " + json);
                } catch (Exception ex) {
                    System.out.println("USER: " + u + " ERROR -> " + ex.getMessage());
                }
            }

            System.out.println("\n=== EXECUTING ObtenerReporteJSON (GLOBAL) ===");
            try (CallableStatement cs = conn.prepareCall("{call ObtenerReporteJSON(?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "null";
                System.out.println("GLOBAL -> " + json);
            } catch (Exception ex) {
                System.out.println("GLOBAL ERROR -> " + ex.getMessage());
            }

            System.out.println("\n=== APLICANDO V8__ActualizarDashboardYProcedimientosAIO.sql ===");
            java.nio.file.Path v8Path = java.nio.file.Paths.get("src/main/resources/db/V8__ActualizarDashboardYProcedimientosAIO.sql");
            String v8Content = java.nio.file.Files.readString(v8Path).replace("\r\n", "\n");
            String[] v8Statements = v8Content.split("\n/\\s*(\n|$)");
            System.out.println("Total sentencias encontradas en V8: " + v8Statements.length);
            for (int i = 0; i < v8Statements.length; i++) {
                String stmt = v8Statements[i].trim();
                int createIdx = stmt.toUpperCase().indexOf("CREATE OR REPLACE");
                if (createIdx >= 0) {
                    stmt = stmt.substring(createIdx);
                    String firstLine = stmt.lines().findFirst().orElse("");
                    System.out.println("Compilando sentencia [" + i + "]: " + firstLine);
                    try (java.sql.Statement st = conn.createStatement()) {
                        st.execute(stmt);
                        System.out.println("-> Compilado con éxito!");
                    } catch (Exception ce) {
                        System.err.println("-> Error compilando: " + ce.getMessage());
                    }
                }
            }

            try (java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT NAME, TYPE, LINE, POSITION, TEXT FROM USER_ERRORS WHERE NAME IN ('REPORTEDASHBOARDS1AIO', 'OBTENERDATAGRAFICA7ANIOSS2AIO', 'OBTENERDATAGRAFICAPORANIOAIOS3', 'SP_SOLICITUDES_DEPARTAMENTAL_AUD')")) {
                boolean hasErr = false;
                while (rs.next()) {
                    hasErr = true;
                    System.err.println("ERROR EN " + rs.getString("NAME") + " linea " + rs.getInt("LINE") + ": " + rs.getString("TEXT"));
                }
                if (!hasErr) {
                    System.out.println("-> Procedimientos de V8 compilados en ROOTSISREC sin errores de compilación!");
                }
            }

            System.out.println("\n=== VERIFICANDO RESULTADOS PROCEDIMIENTOS V8 ===");
            for (String u : users) {
                System.out.println("\n--- USUARIO: " + u + " ---");
                // 1. ReporteDashboardS1AIO
                try (CallableStatement cs = conn.prepareCall("{call ReporteDashboardS1AIO(?, ?)}")) {
                    cs.setString(1, u);
                    cs.registerOutParameter(2, Types.CLOB);
                    cs.execute();
                    java.sql.Clob clob = cs.getClob(2);
                    String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "null";
                    System.out.println("1. ReporteDashboardS1AIO: " + json);
                } catch (Exception ex) {
                    System.out.println("1. ReporteDashboardS1AIO ERROR: " + ex.getMessage());
                }

                // 2. ObtenerDataGrafica7AniosS2AIO
                try (CallableStatement cs = conn.prepareCall("{call ObtenerDataGrafica7AniosS2AIO(?, ?)}")) {
                    cs.registerOutParameter(1, Types.CLOB);
                    cs.setString(2, u);
                    cs.execute();
                    java.sql.Clob clob = cs.getClob(1);
                    String json = clob != null ? clob.getSubString(1, (int) Math.min(clob.length(), 200)) : "null";
                    System.out.println("2. ObtenerDataGrafica7AniosS2AIO: " + json + "...");
                } catch (Exception ex) {
                    System.out.println("2. ObtenerDataGrafica7AniosS2AIO ERROR: " + ex.getMessage());
                }

                // 3. ObtenerDataGraficaPorAnioAIOS3
                try (CallableStatement cs = conn.prepareCall("{call ObtenerDataGraficaPorAnioAIOS3(?, ?)}")) {
                    cs.registerOutParameter(1, Types.CLOB);
                    cs.setString(2, u);
                    cs.execute();
                    java.sql.Clob clob = cs.getClob(1);
                    String json = clob != null ? clob.getSubString(1, (int) Math.min(clob.length(), 200)) : "null";
                    System.out.println("3. ObtenerDataGraficaPorAnioAIOS3: " + json + "...");
                } catch (Exception ex) {
                    System.out.println("3. ObtenerDataGraficaPorAnioAIOS3 ERROR: " + ex.getMessage());
                }

                // 4. SP_SOLICITUDES_DEPARTAMENTAL_AUD
                try (CallableStatement cs = conn.prepareCall("{call SP_SOLICITUDES_DEPARTAMENTAL_AUD(?, ?, ?, ?, ?)}")) {
                    cs.setString(1, "0");
                    cs.setString(2, u);
                    cs.setInt(3, 0);
                    cs.setString(4, "0");
                    cs.registerOutParameter(5, -10); // OracleTypes.CURSOR
                    cs.execute();
                    try (java.sql.ResultSet rsCursor = (java.sql.ResultSet) cs.getObject(5)) {
                        int solCount = 0;
                        while (rsCursor != null && rsCursor.next()) {
                            solCount++;
                        }
                        System.out.println("4. SP_SOLICITUDES_DEPARTAMENTAL_AUD: total solicitudes devueltas = " + solCount);
                    }
                } catch (Exception ex) {
                    System.out.println("4. SP_SOLICITUDES_DEPARTAMENTAL_AUD ERROR: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico")
    @Test
    public void testInspectUserProblem() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 1. ESTADOS DE SEGUIMIENTO ===\n");
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT AREES_CODIGO, AREES_DESCRIPCION FROM AI_REC_ESTADO_SEGUIMIENTO ORDER BY AREES_CODIGO");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append("ESTADO REC: ").append(rs.getInt("AREES_CODIGO")).append(" -> ").append(rs.getString("AREES_DESCRIPCION")).append("\n");
                }
            }

            sb.append("\n=== 2. ESTADOS DE VERIFICABLE ===\n");
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT AREEV_CODIGO, AREEV_DESCRIPCION FROM AI_REC_ESTADO_VERIFICABLE ORDER BY AREEV_CODIGO");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append("ESTADO VER: ").append(rs.getInt("AREEV_CODIGO")).append(" -> ").append(rs.getString("AREEV_DESCRIPCION")).append("\n");
                }
            }

            sb.append("\n=== 3. PROBANDO CALCULO PROGRESO EN REPORTES DE rmerino ===\n");
            String q = "WITH ultimos_verificables AS (\n" +
                       "    SELECT V.AREDR_CODIGO, V.AREEV_CODIGO, ROW_NUMBER() OVER (PARTITION BY V.AREDR_CODIGO ORDER BY V.AREVE_FECHA_CARGA DESC NULLS LAST, V.AREVE_CODIGO DESC) AS rn\n" +
                       "    FROM AI_REC_VERIFICABLES V WHERE V.AREVE_ESTADO = 1\n" +
                       "),\n" +
                       "prog_rec AS (\n" +
                       "    SELECT DR.ARERE_CODIGO, COUNT(DISTINCT DR.ARERS_CODIGO) AS total_resp,\n" +
                       "           COUNT(DISTINCT CASE WHEN UV.AREEV_CODIGO = 2 THEN DR.ARERS_CODIGO END) AS resp_cumplidos\n" +
                       "    FROM AI_REC_DETALLE_RECOMENDACION DR\n" +
                       "    JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1\n" +
                       "    LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1\n" +
                       "    WHERE DR.AREDR_ESTADO = 1\n" +
                       "    GROUP BY DR.ARERE_CODIGO\n" +
                       "),\n" +
                       "prog_inf AS (\n" +
                       "    SELECT INF.AININ_CODIGO, COUNT(DISTINCT REC.ARERE_CODIGO) AS total_recs,\n" +
                       "           COUNT(DISTINCT CASE WHEN REC.AREES_CODIGO = 2 OR (PR.total_resp > 0 AND PR.resp_cumplidos = PR.total_resp) THEN REC.ARERE_CODIGO END) AS recs_cumplidas_calculadas,\n" +
                       "           COUNT(DISTINCT CASE WHEN REC.AREES_CODIGO = 2 THEN REC.ARERE_CODIGO END) AS recs_cumplidas_solo_estado\n" +
                       "    FROM AI_INF_INFORMES INF\n" +
                       "    JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = INF.AININ_CODIGO AND H.AREHA_ESTADO = 1\n" +
                       "    JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO AND C.ARECO_ESTADO = 1\n" +
                       "    JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = C.ARECO_CODIGO AND REC.ARERE_ESTADO = 1\n" +
                       "    LEFT JOIN prog_rec PR ON PR.ARERE_CODIGO = REC.ARERE_CODIGO\n" +
                       "    WHERE INF.AININ_ESTADO = 1\n" +
                       "    GROUP BY INF.AININ_CODIGO\n" +
                       ")\n" +
                       "SELECT INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_USUARIO, INF.AININ_PROGRESO,\n" +
                       "       PI.total_recs, PI.recs_cumplidas_calculadas, PI.recs_cumplidas_solo_estado,\n" +
                       "       ROUND((PI.recs_cumplidas_calculadas * 100.0) / NULLIF(PI.total_recs, 0), 2) AS prog_inf_calc,\n" +
                       "       ROUND((PI.recs_cumplidas_solo_estado * 100.0) / NULLIF(PI.total_recs, 0), 2) AS prog_inf_solo_est\n" +
                       "FROM AI_INF_INFORMES INF\n" +
                       "JOIN prog_inf PI ON PI.AININ_CODIGO = INF.AININ_CODIGO\n" +
                       "WHERE INF.AININ_ESTADO = 1 AND UPPER(TRIM(INF.AININ_USUARIO)) = 'RMERINO' AND ROWNUM <= 15";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(q);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append(String.format("INF: %s (%s) | DB_PROG: %s | TOTAL_RECS: %d | CUMP_CALC: %d (%.1f%%) | CUMP_EST: %d (%.1f%%)\n",
                        rs.getString("AININ_MEMORANDO"), rs.getString("AININ_USUARIO"), rs.getString("AININ_PROGRESO"),
                        rs.getInt("total_recs"), rs.getInt("recs_cumplidas_calculadas"), rs.getDouble("prog_inf_calc"),
                        rs.getInt("recs_cumplidas_solo_estado"), rs.getDouble("prog_inf_solo_est")));
                }
            }
            sb.append("\n=== 4. PROBANDO CALCULO PROGRESO RECOMENDACIONES ===\n");
            String qRec = "WITH ultimos_verificables AS (\n" +
                          "    SELECT V.AREDR_CODIGO, V.AREEV_CODIGO, ROW_NUMBER() OVER (PARTITION BY V.AREDR_CODIGO ORDER BY V.AREVE_FECHA_CARGA DESC NULLS LAST, V.AREVE_CODIGO DESC) AS rn\n" +
                          "    FROM AI_REC_VERIFICABLES V WHERE V.AREVE_ESTADO = 1\n" +
                          "),\n" +
                          "prog_rec AS (\n" +
                          "    SELECT DR.ARERE_CODIGO, COUNT(DISTINCT DR.ARERS_CODIGO) AS total_resp,\n" +
                          "           COUNT(DISTINCT CASE WHEN UV.AREEV_CODIGO = 2 THEN DR.ARERS_CODIGO END) AS resp_cumplidos\n" +
                          "    FROM AI_REC_DETALLE_RECOMENDACION DR\n" +
                          "    JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1\n" +
                          "    LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1\n" +
                          "    WHERE DR.AREDR_ESTADO = 1\n" +
                          "    GROUP BY DR.ARERE_CODIGO\n" +
                          ")\n" +
                          "SELECT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.AREES_CODIGO, ES.AREES_DESCRIPCION,\n" +
                          "       PR.total_resp, PR.resp_cumplidos,\n" +
                          "       CASE WHEN REC.AREES_CODIGO = 2 THEN 100.0\n" +
                          "            WHEN NVL(PR.total_resp, 0) > 0 THEN ROUND((PR.resp_cumplidos * 100.0) / PR.total_resp, 2)\n" +
                          "            ELSE 0.0 END AS prog_rec_calc\n" +
                          "FROM AI_REC_RECOMENDACIONES REC\n" +
                          "JOIN AI_REC_ESTADO_SEGUIMIENTO ES ON ES.AREES_CODIGO = REC.AREES_CODIGO\n" +
                          "LEFT JOIN prog_rec PR ON PR.ARERE_CODIGO = REC.ARERE_CODIGO\n" +
                          "WHERE REC.ARERE_ESTADO = 1 AND PR.total_resp > 1 AND REC.AREES_CODIGO <> 2 AND ROWNUM <= 20";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qRec);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sb.append(String.format("REC_ID: %d (Num %s) | EST: %s | RESP_TOT: %d | RESP_CUMP: %d | PROG_CALC: %.1f%%\n",
                        rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"), rs.getString("AREES_DESCRIPCION"),
                        rs.getInt("total_resp"), rs.getInt("resp_cumplidos"), rs.getDouble("prog_rec_calc")));
                }
            }
            java.nio.file.Files.writeString(java.nio.file.Paths.get("progreso-debug.txt"), sb.toString());

            System.out.println("\n=== APLICANDO FIX EN REPORTERECOMENDACIONESVENCIDASOPERATIVO ===");
            String spFix = 
                "CREATE OR REPLACE PROCEDURE ROOTSISREC.REPORTERECOMENDACIONESVENCIDASOPERATIVO (\n" +
                "    p_json              OUT CLOB,\n" +
                "    fecha_inicial       IN  VARCHAR2,\n" +
                "    fecha_fin           IN  VARCHAR2,\n" +
                "    estado_seguimiento  IN  INTEGER,\n" +
                "    auditor_responsable IN  VARCHAR2\n" +
                ") IS\n" +
                "    v_fecha_ini    TIMESTAMP;\n" +
                "    v_fecha_fin    TIMESTAMP;\n" +
                "BEGIN\n" +
                "    IF fecha_inicial IS NULL OR TRIM(fecha_inicial) IS NULL THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\\d{2}/\\d{2}/\\d{4}$') THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial) || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}') THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial), 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\\d{4}-\\d{2}-\\d{2}') THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP(SUBSTR(TRIM(fecha_inicial), 1, 10) || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS');\n" +
                "    ELSE\n" +
                "        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    END IF;\n" +
                "\n" +
                "    IF fecha_fin IS NULL OR TRIM(fecha_fin) IS NULL THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\\d{2}/\\d{2}/\\d{4}$') THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin) || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}') THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin), 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\\d{4}-\\d{2}-\\d{2}') THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP(SUBSTR(TRIM(fecha_fin), 1, 10) || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');\n" +
                "    ELSE\n" +
                "        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    END IF;\n" +
                "\n" +
                "    WITH ultimas_prorrogas AS (\n" +
                "        SELECT \n" +
                "            S.ARERE_CODIGO,\n" +
                "            S.ARERS_CODIGO,\n" +
                "            MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA\n" +
                "        FROM ROOTSISREC.AI_REC_SOLICITUDES S\n" +
                "        WHERE S.ARESO_ESTADO = '1'\n" +
                "          AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))\n" +
                "          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL\n" +
                "        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO\n" +
                "    ),\n" +
                "    ultimos_verificables AS (\n" +
                "        SELECT \n" +
                "            VER.AREDR_CODIGO,\n" +
                "            VER.AREVE_CODIGO,\n" +
                "            VER.AREEV_CODIGO,\n" +
                "            VER.AREVE_OBSERVACION,\n" +
                "            ROW_NUMBER() OVER (PARTITION BY VER.AREDR_CODIGO ORDER BY VER.AREVE_FECHA_CARGA DESC NULLS LAST, VER.AREVE_CODIGO DESC) AS RN\n" +
                "        FROM ROOTSISREC.AI_REC_VERIFICABLES VER\n" +
                "        WHERE VER.AREVE_ESTADO = 1\n" +
                "    ),\n" +
                "    asignaciones_rec AS (\n" +
                "        SELECT \n" +
                "            DET.ARERE_CODIGO,\n" +
                "            COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS FECHA_INDIVIDUAL,\n" +
                "            UV.AREEV_CODIGO,\n" +
                "            TRIM(RESP.ARERS_NOMBRE || ' ' || RESP.ARERS_APELLIDO) ||\n" +
                "            CASE \n" +
                "                WHEN UV.AREVE_CODIGO IS NULL THEN ' (VERIFICABLE NO CREADO)'\n" +
                "                WHEN UV.AREEV_CODIGO = 1 THEN ' (PENDIENTE)'\n" +
                "                WHEN UV.AREEV_CODIGO = 2 THEN ' (JUSTIFICA)'\n" +
                "                WHEN UV.AREEV_CODIGO = 3 THEN ' (NO JUSTIFICA)'\n" +
                "                ELSE ' (VERIFICABLE NO CREADO)'\n" +
                "            END AS RESPONSABLE_CON_ESTADO\n" +
                "        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET\n" +
                "        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = DET.ARERE_CODIGO\n" +
                "        JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP ON RESP.ARERS_CODIGO = DET.ARERS_CODIGO\n" +
                "        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DET.ARERE_CODIGO AND PR.ARERS_CODIGO = DET.ARERS_CODIGO\n" +
                "        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DET.AREDR_CODIGO AND UV.RN = 1\n" +
                "        WHERE DET.AREDR_ESTADO = 1 AND RESP.ARERS_ESTADO = 1 AND REC.ARERE_ESTADO = 1\n" +
                "    ),\n" +
                "    resumen_rec AS (\n" +
                "        SELECT \n" +
                "            ARERE_CODIGO,\n" +
                "            MAX(FECHA_INDIVIDUAL) AS FECHA_MAXIMA_GLOBAL,\n" +
                "            COUNT(1) AS TOTAL_RESPONSABLES,\n" +
                "            COUNT(CASE WHEN AREEV_CODIGO = 2 THEN 1 END) AS TOTAL_CUMPLIDOS,\n" +
                "            RTRIM(XMLCAST(XMLAGG(XMLELEMENT(e, RESPONSABLE_CON_ESTADO || ', ') ORDER BY RESPONSABLE_CON_ESTADO) AS CLOB), ', ') AS LISTA_RESPONSABLES\n" +
                "        FROM asignaciones_rec\n" +
                "        GROUP BY ARERE_CODIGO\n" +
                "    ),\n" +
                "    recomendaciones_evaluadas AS (\n" +
                "        SELECT \n" +
                "            INF.AININ_CODIGO AS aininCodigo,\n" +
                "            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) AS informeFechaOrden,\n" +
                "            INF.AININ_TITULO AS tituloInforme,\n" +
                "            REC.ARERE_DESCRIPCION AS recomendacion,\n" +
                "            ORG.AINOC_DESCRIPCION AS organismoControl,\n" +
                "            INF.AININ_MEMORANDO AS memorando,\n" +
                "            INF.AININ_MEMO_APROBACION AS memoAprobacion,\n" +
                "            '' AS observacion,\n" +
                "            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'DD/MM/YYYY') AS fechaEmision,\n" +
                "            COALESCE(RES.FECHA_MAXIMA_GLOBAL, REC.ARERE_FECHA_MAXIMA) AS fechaMaximaDate,\n" +
                "            TO_CHAR(COALESCE(RES.FECHA_MAXIMA_GLOBAL, REC.ARERE_FECHA_MAXIMA), 'DD/MM/YYYY') AS fechaMaxima,\n" +
                "            FN_CALCULAR_DIAS_LABORABLES(COALESCE(RES.FECHA_MAXIMA_GLOBAL, REC.ARERE_FECHA_MAXIMA)) AS plazo,\n" +
                "            NVL(TO_CHAR(SUBSTR(RES.LISTA_RESPONSABLES, 1, 3900)), 'SIN RESPONSABLE') AS responsables,\n" +
                "            REC.ARERE_CODIGO AS arereCodigo,\n" +
                "            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\\d+')) AS numRec,\n" +
                "            REC.AREES_CODIGO AS estadoSegRec,\n" +
                "            NVL(RES.TOTAL_RESPONSABLES, 0) AS totalResp,\n" +
                "            NVL(RES.TOTAL_CUMPLIDOS, 0) AS cumplidosResp\n" +
                "        FROM ROOTSISREC.AI_INF_INFORMES INF\n" +
                "        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO\n" +
                "        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO\n" +
                "        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO\n" +
                "        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO\n" +
                "        LEFT JOIN resumen_rec RES ON RES.ARERE_CODIGO = REC.ARERE_CODIGO\n" +
                "        WHERE INF.AININ_ESTADO = 1\n" +
                "          AND HALL.AREHA_ESTADO = 1\n" +
                "          AND CON.ARECO_ESTADO = 1\n" +
                "          AND REC.ARERE_ESTADO = 1\n" +
                "          AND ORG.AINOC_ESTADO = 1\n" +
                "          AND (NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) BETWEEN v_fecha_ini AND v_fecha_fin)\n" +
                "          AND (auditor_responsable = '0' OR INF.AININ_USUARIO = auditor_responsable)\n" +
                "          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)\n" +
                "          AND REC.AREES_CODIGO NOT IN (2, 4)\n" +
                "          AND NOT (RES.TOTAL_RESPONSABLES > 0 AND RES.TOTAL_CUMPLIDOS = RES.TOTAL_RESPONSABLES)\n" +
                "    )\n" +
                "    SELECT JSON_ARRAYAGG(\n" +
                "        JSON_OBJECT(\n" +
                "            'tituloInforme'    VALUE tituloInforme,\n" +
                "            'recomendacion'    VALUE recomendacion,\n" +
                "            'organismoControl' VALUE organismoControl,\n" +
                "            'memorando'        VALUE memorando,\n" +
                "            'memoAprobacion'   VALUE memoAprobacion,\n" +
                "            'observacion'      VALUE observacion,\n" +
                "            'fechaEmision'     VALUE fechaEmision,\n" +
                "            'fechaMaxima'      VALUE fechaMaxima,\n" +
                "            'plazo'            VALUE plazo,\n" +
                "            'responsables'     VALUE responsables,\n" +
                "            'arereCodigo'      VALUE arereCodigo\n" +
                "        RETURNING CLOB)\n" +
                "    RETURNING CLOB)\n" +
                "    INTO p_json\n" +
                "    FROM (\n" +
                "        SELECT *\n" +
                "        FROM recomendaciones_evaluadas\n" +
                "        WHERE plazo < 0\n" +
                "        ORDER BY informeFechaOrden DESC, aininCodigo DESC, numRec ASC\n" +
                "    );\n" +
                "    \n" +
                "    IF p_json IS NULL THEN\n" +
                "        p_json := TO_CLOB('[]');\n" +
                "    END IF;\n" +
                "EXCEPTION\n" +
                "    WHEN OTHERS THEN\n" +
                "        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));\n" +
                "END REPORTERECOMENDACIONESVENCIDASOPERATIVO;";
            try (java.sql.Statement st = conn.createStatement()) {
                st.execute(spFix);
                System.out.println("-> REPORTERECOMENDACIONESVENCIDASOPERATIVO compilado con éxito!");
            }
            try (java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT LINE, POSITION, TEXT FROM USER_ERRORS WHERE NAME = 'REPORTERECOMENDACIONESVENCIDASOPERATIVO'")) {
                while (rs.next()) {
                    System.err.println("COMPILER ERROR en linea " + rs.getInt("LINE") + ", pos " + rs.getInt("POSITION") + ": " + rs.getString("TEXT"));
                }
            }

            System.out.println("\n=== 4. EJECUTANDO REPORTERECOMENDACIONESVENCIDASOPERATIVO ===");
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASOPERATIVO(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "0");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "[]";
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
                if (root.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : root) {
                        String memo = node.path("memorando").asText();
                        if (memo.contains("074") || memo.contains("74")) {
                            String resps = node.path("responsables").asText();
                            System.out.println("MEMO: " + memo + " | REC: " + node.path("recomendacion").asText().substring(0, Math.min(40, node.path("recomendacion").asText().length())));
                            System.out.println("  RESPONSABLES: '" + resps + "'");
                            
                            // Probar getResponsablesCount
                            int noCreadoCount = countOccurrences(resps, "(VERIFICABLE NO CREADO)");
                            int pendienteCount = countOccurrences(resps, "(PENDIENTE)");
                            int noJustificaCount = countOccurrences(resps, "(NO JUSTIFICA)");
                            int count = noCreadoCount + pendienteCount + noJustificaCount;
                            System.out.println("  BADGE COUNT CALCULADO: " + count + " (NoCreado: " + noCreadoCount + ", Pendiente: " + pendienteCount + ", NoJust: " + noJustificaCount + ")");
                        }
                    }
                } else {
                    System.out.println("Respuesta no array: " + json);
                }
            }

            System.out.println("\n=== 2. BUSCANDO INFORME AI-074-2026 EN VENCIDAS ===");
            String sql2 = "SELECT INF.AININ_CODIGO, INF.AININ_MEMORANDO, " +
                          "REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.ARERE_FECHA_MAXIMA, REC.AREES_CODIGO " +
                          "FROM AI_INF_INFORMES INF " +
                          "JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                          "JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                          "JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = C.ARECO_CODIGO " +
                          "WHERE INF.AININ_MEMORANDO LIKE '%074%' OR INF.AININ_MEMORANDO LIKE '%74%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql2);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long arereCodigo = rs.getLong("ARERE_CODIGO");
                    System.out.println("INF: " + rs.getString("AININ_MEMORANDO") + " | REC COD: " + arereCodigo + " | NUM: " + rs.getString("ARERE_NUM_REC") + " | FECHA_MAX: " + rs.getDate("ARERE_FECHA_MAXIMA") + " | ESTADO SEG: " + rs.getInt("AREES_CODIGO"));
                    
                    String sqlDet = "SELECT DR.AREDR_CODIGO, DR.ARERS_CODIGO, DR.AREDR_ESTADO, " +
                                    "RES.ARERS_NOMBRE, RES.ARERS_APELLIDO, RES.ARERS_ESTADO, " +
                                    "(SELECT AREEV_CODIGO FROM (SELECT V.AREEV_CODIGO FROM AI_REC_VERIFICABLES V WHERE V.AREDR_CODIGO = DR.AREDR_CODIGO AND V.AREVE_ESTADO = 1 ORDER BY V.AREVE_FECHA_CARGA DESC NULLS LAST, V.AREVE_CODIGO DESC) WHERE ROWNUM = 1) AS EV_COD " +
                                    "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                                    "JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                                    "WHERE DR.ARERE_CODIGO = ?";
                    try (java.sql.PreparedStatement psDet = conn.prepareStatement(sqlDet)) {
                        psDet.setLong(1, arereCodigo);
                        try (java.sql.ResultSet rsDet = psDet.executeQuery()) {
                            while (rsDet.next()) {
                                System.out.println("    -> RESP: " + rsDet.getString("ARERS_NOMBRE") + " " + rsDet.getString("ARERS_APELLIDO") +
                                                   " | EST_DET: " + rsDet.getInt("AREDR_ESTADO") +
                                                   " | EST_RESP: " + rsDet.getInt("ARERS_ESTADO") +
                                                   " | EV_COD: " + rsDet.getInt("EV_COD"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int countOccurrences(String text, String search) {
        if (text == null || search == null || search.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(search, idx)) != -1) {
            count++;
            idx += search.length();
        }
        return count;
    }

    @Disabled("Prueba manual contra base de datos")
    @Test
    public void testInspectResponsables() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("=== CONECTADO A ORACLE EXITOSAMENTE ===");
            System.out.println("=== APLICANDO V6__ActualizarOrdenReportesDirector.sql ===");
            java.nio.file.Path v6Path = java.nio.file.Paths.get("src/main/resources/db/V6__ActualizarOrdenReportesDirector.sql");
            String v6Content = java.nio.file.Files.readString(v6Path).replace("\r\n", "\n");
            
            // Split by '/' on its own line
            String[] statements = v6Content.split("\n/\\s*(\n|$)");
            System.out.println("Total sentencias encontradas en V6: " + statements.length);
            for (int i = 0; i < statements.length; i++) {
                String stmt = statements[i].trim();
                int createIdx = stmt.toUpperCase().indexOf("CREATE OR REPLACE");
                if (createIdx >= 0) {
                    stmt = stmt.substring(createIdx);
                    String firstLine = stmt.lines().findFirst().orElse("");
                    System.out.println("Compilando sentencia [" + i + "]: " + firstLine);
                    try (java.sql.Statement st = conn.createStatement()) {
                        st.execute(stmt);
                        System.out.println("-> Compilado con éxito!");
                    } catch (Exception ce) {
                        System.err.println("-> Error compilando: " + ce.getMessage());
                    }
                }
            }

            // Check USER_ERRORS
            try (java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT NAME, TYPE, LINE, POSITION, TEXT FROM USER_ERRORS WHERE NAME IN ('REPORTEDEPARTAMENTOFECHASVENCIDASDIRECTOR', 'REPORTERECOMENDACIONESPORDIRECTORDEPARTAMENTAL')")) {
                boolean hasErr = false;
                while (rs.next()) {
                    hasErr = true;
                    System.err.println("ERROR EN " + rs.getString("NAME") + " linea " + rs.getInt("LINE") + ": " + rs.getString("TEXT"));
                }
                if (!hasErr) {
                    System.out.println("-> Procedimientos compilados en ROOTSISREC sin errores de compilación!");
                }
            }

            // Probar reporteRecomendacionesPorDirectorDepartamental
            try (java.sql.CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteRecomendacionesPorDirectorDepartamental(?, ?, ?, ?, ?, ?, ?)}")) {
                cs.registerOutParameter(1, java.sql.Types.CLOB);
                cs.setString(2, "01/01/2022");
                cs.setString(3, "31/12/2026");
                cs.setInt(4, 0);
                cs.setInt(5, 0);
                cs.setString(6, "0");
                cs.setString(7, "COMERCIAL");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String res = clob != null ? clob.getSubString(1, (int) Math.min(clob.length(), 2000)) : "";
                System.out.println("TEST reporteRecomendacionesPorDirectorDepartamental (primeros 500 chars):\n" + res.substring(0, Math.min(res.length(), 500)));
            }

            // Probar reporteDepartamentoFechasVencidasDirector
            try (java.sql.CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteDepartamentoFechasVencidasDirector(?, ?, ?, ?, ?, ?, ?)}")) {
                cs.registerOutParameter(1, java.sql.Types.CLOB);
                cs.setString(2, "01/01/2022");
                cs.setString(3, "31/12/2026");
                cs.setInt(4, 0);
                cs.setInt(5, 0);
                cs.setString(6, "0");
                cs.setString(7, "COMERCIAL");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String res = clob != null ? clob.getSubString(1, (int) Math.min(clob.length(), 2000)) : "";
                System.out.println("TEST reporteDepartamentoFechasVencidasDirector (primeros 500 chars):\n" + res.substring(0, Math.min(res.length(), 500)));
            }

            StringBuilder log = new StringBuilder();
            // Test DateMapper vs Oracle FN_CALCULAR_DIAS_LABORABLES
            java.time.LocalDate today = java.time.LocalDate.now();
            log.append("=== COMPARACION EXACTA DATEMAPPER VS ORACLE ===\n");
            java.time.LocalDate[] testDates = new java.time.LocalDate[] {
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(5),
                today.minusDays(10),
                today.minusDays(30),
                today.minusDays(348),
                today.plusDays(1),
                today.plusDays(5),
                today.plusDays(10),
                today.plusDays(30),
                java.time.LocalDate.of(2025, 10, 1),
                java.time.LocalDate.of(2026, 8, 30),
                java.time.LocalDate.of(2026, 12, 31)
            };

            for (java.time.LocalDate td : testDates) {
                int javaVal = ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.calcularDias(java.sql.Date.valueOf(td));
                int oracleVal = 0;
                try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT FN_CALCULAR_DIAS_LABORABLES(?, SYSDATE) FROM DUAL")) {
                    ps.setDate(1, java.sql.Date.valueOf(td));
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) oracleVal = rs.getInt(1);
                    }
                }
                log.append(String.format("Fecha: %s | DateMapper: %d | Oracle: %d | Match: %b\n",
                    td, javaVal, oracleVal, javaVal == oracleVal));
                org.junit.jupiter.api.Assertions.assertEquals(oracleVal, javaVal, "Discrepancy on date " + td);
            }




            // Check myanez in REPORTERECOMENDACIONESVENCIDASRESPONSABLE
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASRESPONSABLE(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "myanez");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "null";
                log.append("=== VENCIDAS MYANEZ ===\n").append(json).append("\n");
            }


            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteRecomendacionesEstadoFechaCumplimientoResponsable(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "tortiz");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "null";
                log.append("ESTADO FECHA TORTIZ:\n").append(json).append("\n");
            }

            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASRESPONSABLE(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "tortiz");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "null";
                log.append("VENCIDAS TORTIZ:\n").append(json).append("\n");
            }

            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteProgresoRecomendacionesResponsable(?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setString(4, "tortiz");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "null";
                log.append("PROGRESO TORTIZ:\n").append(json).append("\n");
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            // Test deserialization of report 1 (vencidas)
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASRESPONSABLE(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "tortiz");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "[]";
                java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesVencidasResponsableDTO> list =
                    mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesVencidasResponsableDTO>>() {});
                log.append("JACKSON DESERIALIZATION VENCIDAS SUCCESS! Size: ").append(list.size()).append("\n");
            }

            // Test deserialization of report 2 (estado fecha)
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteRecomendacionesEstadoFechaCumplimientoResponsable(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "tortiz");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "[]";
                try {
                    java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO> list =
                        mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO>>() {});
                    log.append("JACKSON DESERIALIZATION ESTADO FECHA SUCCESS! Size: ").append(list.size()).append("\n");
                } catch (Exception ex) {
                    log.append("JACKSON DESERIALIZATION ESTADO FECHA FAILED: ").append(ex.getMessage()).append("\n");
                    System.err.println("FAILED ESTADO FECHA DESER: " + ex.getMessage());
                }
            }

            // Test deserialization of report 3 (progreso)
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteProgresoRecomendacionesResponsable(?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setString(4, "tortiz");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "[]";
                java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteProgresoRecomendacionesResponsableDTO> list =
                    mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteProgresoRecomendacionesResponsableDTO>>() {});
                log.append("JACKSON DESERIALIZATION PROGRESO SUCCESS! Size: ").append(list.size()).append("\n");
            }

            // Test with arodriguez
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteRecomendacionesEstadoFechaCumplimientoResponsable(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "arodriguez");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "[]";
                log.append("ARODRIGUEZ JSON:\n").append(json.substring(0, Math.min(300, json.length()))).append("\n");
                try {
                    java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO> list =
                        mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO>>() {});
                    log.append("JACKSON DESERIALIZATION ESTADO FECHA ARODRIGUEZ SUCCESS! Size: ").append(list.size()).append("\n");
                } catch (Exception ex) {
                    log.append("ARODRIGUEZ DESER FAILED: ").append(ex.getMessage()).append("\n");
                }
            }

            // Test with usuario = '0'
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteRecomendacionesEstadoFechaCumplimientoResponsable(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setString(5, "0");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "[]";
                log.append("ALL USERS JSON PREFIX:\n").append(json.substring(0, Math.min(300, json.length()))).append("\n");
                try {
                    java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO> list =
                        mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO>>() {});
                    log.append("JACKSON DESERIALIZATION ESTADO FECHA ALL USERS SUCCESS! Size: ").append(list.size()).append("\n");
                } catch (Exception ex) {
                    log.append("ALL USERS DESER FAILED: ").append(ex.getMessage()).append("\n");
                }
            }
            java.nio.file.Files.writeString(java.nio.file.Paths.get("test-output.txt"), log.toString());
            System.out.println("Log escrito a test-output.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba manual")
    @Test
    public void testInspectFunction() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String spSql = 
                "CREATE OR REPLACE PROCEDURE ROOTSISREC.obtenermetricasresponsables1 (\n" +
                "    p_json            OUT CLOB,\n" +
                "    arers_usuario_per IN VARCHAR2\n" +
                ") IS\n" +
                "    rec_pend_cumplimiento          NUMBER;\n" +
                "    solicitudes_reprog_pend        NUMBER;\n" +
                "    rec_vencidas_sin_justificar    NUMBER;\n" +
                "    rec_vencidas_justificadas      NUMBER;\n" +
                "    verificables_pend              NUMBER;\n" +
                "    usu_per                        VARCHAR2(100) := arers_usuario_per;\n" +
                "    v_es_director                  NUMBER := 0;\n" +
                "    v_estado_aprob                 NUMBER := 1;\n" +
                "BEGIN\n" +
                "    -- Métrica 1: recomendaciones pendientes de cumplimiento (vigentes) para este responsable\n" +
                "    SELECT COUNT(*)\n" +
                "    INTO rec_pend_cumplimiento\n" +
                "    FROM ROOTSISREC.ai_rec_detalle_recomendacion dr\n" +
                "    JOIN ROOTSISREC.ai_rec_responsables    res ON dr.arers_codigo = res.arers_codigo\n" +
                "    JOIN ROOTSISREC.ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo\n" +
                "    WHERE res.arers_usuario_per = usu_per\n" +
                "      AND rec.arere_estado = 1\n" +
                "      AND dr.aredr_estado = 1\n" +
                "      AND res.arers_estado = 1\n" +
                "      AND rec.arees_codigo NOT IN (2, 4)\n" +
                "      AND NOT EXISTS (\n" +
                "          SELECT 1 FROM ROOTSISREC.ai_rec_verificables v\n" +
                "          WHERE v.aredr_codigo = dr.aredr_codigo\n" +
                "            AND v.areve_estado = 1\n" +
                "            AND v.areev_codigo = 2\n" +
                "      )\n" +
                "      AND TRUNC(COALESCE(\n" +
                "          (SELECT MAX(S.ARESO_FECHA_MAX_NUE) \n" +
                "           FROM ROOTSISREC.AI_REC_SOLICITUDES S \n" +
                "           WHERE S.ARERE_CODIGO = dr.ARERE_CODIGO \n" +
                "             AND S.ARERS_CODIGO = dr.ARERS_CODIGO \n" +
                "             AND S.ARESO_ESTADO = '1' \n" +
                "             AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE)))\n" +
                "             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),\n" +
                "          dr.aredr_fecha_maxima,\n" +
                "          rec.arere_fecha_maxima\n" +
                "      )) >= TRUNC(SYSDATE);\n" +
                "\n" +
                "    -- Métrica 2: solicitudes de reprogramación pendientes\n" +
                "    SELECT COUNT(*)\n" +
                "    INTO v_es_director\n" +
                "    FROM ROOTSISREC.AI_SEG_ROL_USU\n" +
                "    WHERE UPPER(TRIM(ASERU_CUENTA)) = UPPER(TRIM(usu_per))\n" +
                "      AND ASERO_CODIGO = 5\n" +
                "      AND (ASERU_ESTADO = '1' OR ASERU_ESTADO = 1);\n" +
                "\n" +
                "    IF v_es_director > 0 THEN\n" +
                "        v_estado_aprob := 2;\n" +
                "    ELSE\n" +
                "        v_estado_aprob := 1;\n" +
                "    END IF;\n" +
                "\n" +
                "    SELECT COUNT(*)\n" +
                "    INTO solicitudes_reprog_pend\n" +
                "    FROM ROOTSISREC.ai_rec_solicitudes solic\n" +
                "    JOIN ROOTSISREC.ai_rec_responsables rec ON solic.arers_codigo = rec.arers_codigo\n" +
                "    WHERE solic.areso_estado_aprob = v_estado_aprob\n" +
                "      AND rec.arers_usuario_per = usu_per\n" +
                "      AND solic.areso_estado = 1\n" +
                "      AND rec.arers_estado = 1;\n" +
                "\n" +
                "    -- Métrica 3a: recomendaciones vencidas sin justificar para este responsable\n" +
                "    SELECT COUNT(*)\n" +
                "    INTO rec_vencidas_sin_justificar\n" +
                "    FROM ROOTSISREC.ai_rec_detalle_recomendacion dr\n" +
                "    JOIN ROOTSISREC.ai_rec_responsables    res ON dr.arers_codigo = res.arers_codigo\n" +
                "    JOIN ROOTSISREC.ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo\n" +
                "    WHERE res.arers_usuario_per = usu_per\n" +
                "      AND rec.arere_estado = 1\n" +
                "      AND dr.aredr_estado = 1\n" +
                "      AND res.arers_estado = 1\n" +
                "      AND rec.arees_codigo NOT IN (2, 4)\n" +
                "      AND NOT EXISTS (\n" +
                "          SELECT 1 FROM ROOTSISREC.ai_rec_verificables v\n" +
                "          WHERE v.aredr_codigo = dr.aredr_codigo\n" +
                "            AND v.areve_estado = 1\n" +
                "            AND v.areev_codigo = 2\n" +
                "      )\n" +
                "      AND TRUNC(COALESCE(\n" +
                "          (SELECT MAX(S.ARESO_FECHA_MAX_NUE) \n" +
                "           FROM ROOTSISREC.AI_REC_SOLICITUDES S \n" +
                "           WHERE S.ARERE_CODIGO = dr.ARERE_CODIGO \n" +
                "             AND S.ARERS_CODIGO = dr.ARERS_CODIGO \n" +
                "             AND S.ARESO_ESTADO = '1' \n" +
                "             AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE)))\n" +
                "             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),\n" +
                "          dr.aredr_fecha_maxima,\n" +
                "          rec.arere_fecha_maxima\n" +
                "      )) < TRUNC(SYSDATE);\n" +
                "\n" +
                "    -- Métrica 3b: recomendaciones vencidas justificadas para este responsable\n" +
                "    SELECT COUNT(*)\n" +
                "    INTO rec_vencidas_justificadas\n" +
                "    FROM ROOTSISREC.ai_rec_detalle_recomendacion dr\n" +
                "    JOIN ROOTSISREC.ai_rec_responsables    res ON dr.arers_codigo = res.arers_codigo\n" +
                "    JOIN ROOTSISREC.ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo\n" +
                "    WHERE res.arers_usuario_per = usu_per\n" +
                "      AND rec.arere_estado = 1\n" +
                "      AND dr.aredr_estado = 1\n" +
                "      AND res.arers_estado = 1\n" +
                "      AND rec.arees_codigo NOT IN (2, 4)\n" +
                "      AND EXISTS (\n" +
                "          SELECT 1 FROM ROOTSISREC.ai_rec_verificables v\n" +
                "          WHERE v.aredr_codigo = dr.aredr_codigo\n" +
                "            AND v.areve_estado = 1\n" +
                "            AND v.areev_codigo = 2\n" +
                "      )\n" +
                "      AND TRUNC(COALESCE(\n" +
                "          (SELECT MAX(S.ARESO_FECHA_MAX_NUE) \n" +
                "           FROM ROOTSISREC.AI_REC_SOLICITUDES S \n" +
                "           WHERE S.ARERE_CODIGO = dr.ARERE_CODIGO \n" +
                "             AND S.ARERS_CODIGO = dr.ARERS_CODIGO \n" +
                "             AND S.ARESO_ESTADO = '1' \n" +
                "             AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE)))\n" +
                "             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),\n" +
                "          dr.aredr_fecha_maxima,\n" +
                "          rec.arere_fecha_maxima\n" +
                "      )) < TRUNC(SYSDATE);\n" +
                "\n" +
                "    -- Métrica 4: Verificables pendientes\n" +
                "    verificables_pend := rec_pend_cumplimiento;\n" +
                "\n" +
                "    -- Build JSON response\n" +
                "    SELECT JSON_OBJECT(\n" +
                "            'recomendacionesPendientes'             VALUE rec_pend_cumplimiento,\n" +
                "            'solicitudesPendientesAprobacion'      VALUE solicitudes_reprog_pend,\n" +
                "            'recomendacionesVencidasSinJustificar' VALUE rec_vencidas_sin_justificar,\n" +
                "            'recomendacionesVencidasJustificadas'  VALUE rec_vencidas_justificadas,\n" +
                "            'verificablesPendientes'               VALUE verificables_pend\n" +
                "        RETURNING CLOB)\n" +
                "    INTO p_json\n" +
                "    FROM dual;\n" +
                "\n" +
                "EXCEPTION\n" +
                "    WHEN OTHERS THEN\n" +
                "        p_json := JSON_OBJECT('error' VALUE sqlerrm);\n" +
                "END obtenermetricasresponsables1;";

            try (java.sql.Statement st = conn.createStatement()) {
                st.execute(spSql);
                System.out.println("\n=== INSPECTING anicolalde INFORMES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_ESTADO, INF.AININ_FECHA_APROBACION, " +
                    "(SELECT COUNT(*) FROM AI_REC_HALLAZGOS H WHERE H.AININ_CODIGO = INF.AININ_CODIGO) as hall_cnt, " +
                    "(SELECT COUNT(*) FROM AI_REC_HALLAZGOS H JOIN AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO WHERE H.AININ_CODIGO = INF.AININ_CODIGO) as conc_cnt, " +
                    "(SELECT COUNT(*) FROM AI_REC_HALLAZGOS H JOIN AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO JOIN AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO WHERE H.AININ_CODIGO = INF.AININ_CODIGO) as rec_cnt, " +
                    "(SELECT COUNT(*) FROM AI_REC_HALLAZGOS H JOIN AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO JOIN AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO JOIN AI_REC_DETALLE_RECOMENDACION DR ON R.ARERE_CODIGO = DR.ARERE_CODIGO WHERE H.AININ_CODIGO = INF.AININ_CODIGO) as det_cnt " +
                    "FROM AI_INF_INFORMES INF WHERE INF.AININ_USUARIO = 'anicolalde'");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INF: %d | MEMO: %s | EST: %s | F_APROB: %s | HALL: %d | CONC: %d | REC: %d | DET: %d",
                            rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"), rs.getString("AININ_ESTADO"),
                            rs.getDate("AININ_FECHA_APROBACION"), rs.getInt("hall_cnt"), rs.getInt("conc_cnt"),
                            rs.getInt("rec_cnt"), rs.getInt("det_cnt")));
                }
            }
            }

            String sqlResp = "SELECT ARERS_CODIGO, ARERS_CODIGO_PER, ARERS_USUARIO_PER, ARERS_NOMBRE, ARERS_APELLIDO FROM ROOTSISREC.AI_REC_RESPONSABLES WHERE ARERS_CODIGO_PER = 'MDMPE443'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlResp);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("COD: " + rs.getLong("ARERS_CODIGO") + ", COD_PER: " + rs.getString("ARERS_CODIGO_PER") + ", USU_PER: " + rs.getString("ARERS_USUARIO_PER"));
                    String callSql = "{call ROOTSISREC.obtenermetricasresponsables1(?, ?)}";
                    try (CallableStatement cs = conn.prepareCall(callSql)) {
                        cs.registerOutParameter(1, Types.CLOB);
                        cs.setString(2, rs.getString("ARERS_USUARIO_PER"));
                        cs.execute();
                        java.sql.Clob clob = cs.getClob(1);
                        String json = clob.getSubString(1, (int) clob.length());
                        System.out.println("Métricas para " + rs.getString("ARERS_USUARIO_PER") + ": " + json);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba manual contra base de datos")
    @Test
    public void inspectSpOutput() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sqlResumen = 
                "WITH base_datos AS (\n" +
                "    SELECT \n" +
                "        R.ARERS_CODIGO,\n" +
                "        TRIM(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO) AS RESPONSABLE,\n" +
                "        REC.ARERE_CODIGO,\n" +
                "        REC.ARERE_NUM_REC,\n" +
                "        REC.ARERE_FECHA_MAXIMA AS FECHA_GLOBAL,\n" +
                "        D.AREDR_FECHA_MAXIMA AS FECHA_INDIVIDUAL,\n" +
                "        ( \n" +
                "            SELECT MAX(S.ARESO_FECHA_MAX_NUE) \n" +
                "            FROM ROOTSISREC.AI_REC_SOLICITUDES S \n" +
                "            WHERE S.ARERE_CODIGO = D.ARERE_CODIGO \n" +
                "              AND S.ARERS_CODIGO = D.ARERS_CODIGO \n" +
                "              AND S.ARESO_ESTADO = '1' \n" +
                "              AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE))) \n" +
                "              AND S.ARESO_FECHA_MAX_NUE IS NOT NULL \n" +
                "        ) AS FECHA_PRORROGA,\n" +
                "        COALESCE(\n" +
                "            ( \n" +
                "                SELECT MAX(S.ARESO_FECHA_MAX_NUE) \n" +
                "                FROM ROOTSISREC.AI_REC_SOLICITUDES S \n" +
                "                WHERE S.ARERE_CODIGO = D.ARERE_CODIGO \n" +
                "                  AND S.ARERS_CODIGO = D.ARERS_CODIGO \n" +
                "                  AND S.ARESO_ESTADO = '1' \n" +
                "                  AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE))) \n" +
                "                  AND S.ARESO_FECHA_MAX_NUE IS NOT NULL \n" +
                "            ),\n" +
                "            D.AREDR_FECHA_MAXIMA,\n" +
                "            REC.ARERE_FECHA_MAXIMA\n" +
                "        ) AS FECHA_EFECTIVA_INDIVIDUAL,\n" +
                "        ( \n" +
                "            SELECT COUNT(1) \n" +
                "            FROM ROOTSISREC.AI_REC_VERIFICABLES V \n" +
                "            WHERE V.AREDR_CODIGO = D.AREDR_CODIGO \n" +
                "              AND V.AREVE_ESTADO = '1' \n" +
                "              AND V.AREEV_CODIGO = 2 \n" +
                "        ) AS TIENE_JUSTIFICADO_INDIVIDUAL,\n" +
                "        REC.AREES_CODIGO AS ESTADO_GLOBAL_COD\n" +
                "    FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D\n" +
                "    JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = D.ARERE_CODIGO\n" +
                "    JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON R.ARERS_CODIGO = D.ARERS_CODIGO\n" +
                "    WHERE D.AREDR_ESTADO = 1 \n" +
                "      AND REC.ARERE_ESTADO = 1\n" +
                "      AND R.ARERS_ESTADO = 1\n" +
                "      AND R.ARERS_DEPARTAMENTO = 'DPTO. DE RELACIONES INDUSTRIALES'\n" +
                "),\n" +
                "clasificado AS (\n" +
                "    SELECT \n" +
                "        RESPONSABLE,\n" +
                "        ARERE_CODIGO,\n" +
                "        CASE \n" +
                "            WHEN ESTADO_GLOBAL_COD = 2 THEN 'CUMPLIDA'\n" +
                "            WHEN ESTADO_GLOBAL_COD = 4 THEN 'NO APLICA'\n" +
                "            WHEN FECHA_GLOBAL IS NULL OR TRUNC(FECHA_GLOBAL) >= TRUNC(SYSDATE) THEN 'EN PROCESO'\n" +
                "            ELSE 'NO CUMPLIDA'\n" +
                "        END AS ESTADO_GLOBAL,\n" +
                "        CASE \n" +
                "            WHEN TIENE_JUSTIFICADO_INDIVIDUAL > 0 THEN 'CUMPLIDA'\n" +
                "            WHEN ESTADO_GLOBAL_COD = 4 THEN 'NO APLICA'\n" +
                "            WHEN FECHA_EFECTIVA_INDIVIDUAL IS NULL OR TRUNC(FECHA_EFECTIVA_INDIVIDUAL) >= TRUNC(SYSDATE) THEN 'EN PROCESO'\n" +
                "            ELSE 'NO CUMPLIDA'\n" +
                "        END AS ESTADO_INDIVIDUAL\n" +
                "    FROM base_datos\n" +
                ")\n" +
                "SELECT \n" +
                "    RESPONSABLE,\n" +
                "    COUNT(1) AS TOTAL_ASIGNADAS,\n" +
                "    SUM(CASE WHEN ESTADO_GLOBAL = 'CUMPLIDA' THEN 1 ELSE 0 END) AS GLOBAL_CUMPLIDAS,\n" +
                "    SUM(CASE WHEN ESTADO_GLOBAL = 'EN PROCESO' THEN 1 ELSE 0 END) AS GLOBAL_EN_PROCESO,\n" +
                "    SUM(CASE WHEN ESTADO_GLOBAL = 'NO CUMPLIDA' THEN 1 ELSE 0 END) AS GLOBAL_NO_CUMPLIDAS,\n" +
                "    SUM(CASE WHEN ESTADO_INDIVIDUAL = 'CUMPLIDA' THEN 1 ELSE 0 END) AS INDIV_CUMPLIDAS,\n" +
                "    SUM(CASE WHEN ESTADO_INDIVIDUAL = 'EN PROCESO' THEN 1 ELSE 0 END) AS INDIV_EN_PROCESO,\n" +
                "    SUM(CASE WHEN ESTADO_INDIVIDUAL = 'NO CUMPLIDA' THEN 1 ELSE 0 END) AS INDIV_NO_CUMPLIDAS\n" +
                "FROM clasificado\n" +
                "GROUP BY RESPONSABLE\n" +
                "ORDER BY RESPONSABLE";

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlResumen);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                System.out.println(String.format("%-45s | %5s | %-24s | %-24s", "RESPONSABLE", "TOTAL", "GLOBAL (CUMP / PROC / NOCUMP)", "INDIV (CUMP / PROC / NOCUMP)"));
                System.out.println("-------------------------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.println(String.format("%-45s | %5d | %5d / %5d / %5d          | %5d / %5d / %5d",
                        rs.getString("RESPONSABLE"),
                        rs.getInt("TOTAL_ASIGNADAS"),
                        rs.getInt("GLOBAL_CUMPLIDAS"), rs.getInt("GLOBAL_EN_PROCESO"), rs.getInt("GLOBAL_NO_CUMPLIDAS"),
                        rs.getInt("INDIV_CUMPLIDAS"), rs.getInt("INDIV_EN_PROCESO"), rs.getInt("INDIV_NO_CUMPLIDAS")
                    ));
                }
            }

            System.out.println("\n=== RESUMEN EXACTO CON JOINS DEL REPORTE (AUDITORIA OPERATIVA) ===");
            String sqlResumenReporte = 
                "WITH base_datos AS (\n" +
                "    SELECT \n" +
                "        TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO) AS RESPONSABLE,\n" +
                "        REC.ARERE_CODIGO,\n" +
                "        REC.ARERE_FECHA_MAXIMA AS FECHA_GLOBAL,\n" +
                "        DR.AREDR_FECHA_MAXIMA AS FECHA_INDIVIDUAL,\n" +
                "        ( \n" +
                "            SELECT MAX(S.ARESO_FECHA_MAX_NUE) \n" +
                "            FROM ROOTSISREC.AI_REC_SOLICITUDES S \n" +
                "            WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO \n" +
                "              AND S.ARERS_CODIGO = DR.ARERS_CODIGO \n" +
                "              AND S.ARESO_ESTADO = '1' \n" +
                "              AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE))) \n" +
                "              AND S.ARESO_FECHA_MAX_NUE IS NOT NULL \n" +
                "        ) AS FECHA_PRORROGA,\n" +
                "        COALESCE(\n" +
                "            ( \n" +
                "                SELECT MAX(S.ARESO_FECHA_MAX_NUE) \n" +
                "                FROM ROOTSISREC.AI_REC_SOLICITUDES S \n" +
                "                WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO \n" +
                "                  AND S.ARERS_CODIGO = DR.ARERS_CODIGO \n" +
                "                  AND S.ARESO_ESTADO = '1' \n" +
                "                  AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE))) \n" +
                "                  AND S.ARESO_FECHA_MAX_NUE IS NOT NULL \n" +
                "            ),\n" +
                "            DR.AREDR_FECHA_MAXIMA,\n" +
                "            REC.ARERE_FECHA_MAXIMA\n" +
                "        ) AS FECHA_EFECTIVA_INDIVIDUAL,\n" +
                "        ( \n" +
                "            SELECT COUNT(1) \n" +
                "            FROM ROOTSISREC.AI_REC_VERIFICABLES V \n" +
                "            WHERE V.AREDR_CODIGO = DR.AREDR_CODIGO \n" +
                "              AND V.AREVE_ESTADO = '1' \n" +
                "              AND V.AREEV_CODIGO = 2 \n" +
                "        ) AS TIENE_JUSTIFICADO_INDIVIDUAL,\n" +
                "        REC.AREES_CODIGO AS ESTADO_GLOBAL_COD\n" +
                "    FROM ROOTSISREC.AI_INF_INFORMES INF\n" +
                "    JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO\n" +
                "    JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO\n" +
                "    JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO\n" +
                "    JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO\n" +
                "    JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON REC.ARERE_CODIGO = DR.ARERE_CODIGO AND DR.AREDR_ESTADO = 1\n" +
                "    JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO AND RES.ARERS_ESTADO = 1\n" +
                "    WHERE INF.AININ_ESTADO = 1\n" +
                "      AND HALL.AREHA_ESTADO = 1\n" +
                "      AND CON.ARECO_ESTADO = 1\n" +
                "      AND REC.ARERE_ESTADO = 1\n" +
                "      AND ORG.AINOC_ESTADO = 1\n" +
                "      AND RES.ARERS_DEPARTAMENTO = 'DPTO. DE RELACIONES INDUSTRIALES'\n" +
                "),\n" +
                "clasificado AS (\n" +
                "    SELECT \n" +
                "        RESPONSABLE,\n" +
                "        ARERE_CODIGO,\n" +
                "        CASE \n" +
                "            WHEN ESTADO_GLOBAL_COD = 2 THEN 'CUMPLIDA'\n" +
                "            WHEN ESTADO_GLOBAL_COD = 4 THEN 'NO APLICA'\n" +
                "            WHEN FECHA_GLOBAL IS NULL OR TRUNC(FECHA_GLOBAL) >= TRUNC(SYSDATE) THEN 'EN PROCESO'\n" +
                "            ELSE 'NO CUMPLIDA'\n" +
                "        END AS ESTADO_GLOBAL,\n" +
                "        CASE \n" +
                "            WHEN TIENE_JUSTIFICADO_INDIVIDUAL > 0 THEN 'CUMPLIDA'\n" +
                "            WHEN ESTADO_GLOBAL_COD = 4 THEN 'NO APLICA'\n" +
                "            WHEN FECHA_EFECTIVA_INDIVIDUAL IS NULL OR TRUNC(FECHA_EFECTIVA_INDIVIDUAL) >= TRUNC(SYSDATE) THEN 'EN PROCESO'\n" +
                "            ELSE 'NO CUMPLIDA'\n" +
                "        END AS ESTADO_INDIVIDUAL\n" +
                "    FROM base_datos\n" +
                ")\n" +
                "SELECT \n" +
                "    RESPONSABLE,\n" +
                "    COUNT(1) AS TOTAL_ASIGNADAS,\n" +
                "    SUM(CASE WHEN ESTADO_GLOBAL = 'CUMPLIDA' THEN 1 ELSE 0 END) AS GLOBAL_CUMPLIDAS,\n" +
                "    SUM(CASE WHEN ESTADO_GLOBAL = 'EN PROCESO' THEN 1 ELSE 0 END) AS GLOBAL_EN_PROCESO,\n" +
                "    SUM(CASE WHEN ESTADO_GLOBAL = 'NO CUMPLIDA' THEN 1 ELSE 0 END) AS GLOBAL_NO_CUMPLIDAS,\n" +
                "    SUM(CASE WHEN ESTADO_INDIVIDUAL = 'CUMPLIDA' THEN 1 ELSE 0 END) AS INDIV_CUMPLIDAS,\n" +
                "    SUM(CASE WHEN ESTADO_INDIVIDUAL = 'EN PROCESO' THEN 1 ELSE 0 END) AS INDIV_EN_PROCESO,\n" +
                "    SUM(CASE WHEN ESTADO_INDIVIDUAL = 'NO CUMPLIDA' THEN 1 ELSE 0 END) AS INDIV_NO_CUMPLIDAS\n" +
                "FROM clasificado\n" +
                "GROUP BY RESPONSABLE\n" +
                "ORDER BY RESPONSABLE";

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlResumenReporte);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                System.out.println(String.format("%-45s | %5s | %-24s | %-24s", "RESPONSABLE", "TOTAL", "GLOBAL (CUMP / PROC / NOCUMP)", "INDIV (CUMP / PROC / NOCUMP)"));
                System.out.println("-------------------------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.println(String.format("%-45s | %5d | %5d / %5d / %5d          | %5d / %5d / %5d",
                        rs.getString("RESPONSABLE"),
                        rs.getInt("TOTAL_ASIGNADAS"),
                        rs.getInt("GLOBAL_CUMPLIDAS"), rs.getInt("GLOBAL_EN_PROCESO"), rs.getInt("GLOBAL_NO_CUMPLIDAS"),
                        rs.getInt("INDIV_CUMPLIDAS"), rs.getInt("INDIV_EN_PROCESO"), rs.getInt("INDIV_NO_CUMPLIDAS")
                    ));
                }
            }

            String ddlSp = "CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteSeguimientoResponsableOperativo (\n" +
                "    p_json OUT CLOB,\n" +
                "    fecha_inicial IN VARCHAR2,\n" +
                "    fecha_fin IN VARCHAR2,\n" +
                "    organismo_control IN NUMBER,\n" +
                "    estado_seguimiento IN NUMBER,\n" +
                "    auditor_responsable IN VARCHAR2,\n" +
                "    dpto IN VARCHAR2\n" +
                ") AS\n" +
                "    v_fecha_ini DATE;\n" +
                "    v_fecha_fin DATE;\n" +
                "BEGIN\n" +
                "    v_fecha_ini := TO_DATE(fecha_inicial, 'YYYY-MM-DD');\n" +
                "    v_fecha_fin := TO_DATE(fecha_fin, 'YYYY-MM-DD');\n" +
                "    WITH ultimos_verificables AS (\n" +
                "        SELECT\n" +
                "            v.AREDR_CODIGO,\n" +
                "            v.AREEV_CODIGO,\n" +
                "            v.AREVE_DESCRIPCION,\n" +
                "            v.AREVE_OBSERVACION,\n" +
                "            TO_CHAR(NVL(v.AREVE_FECHA_CARGA, v.AREVE_FECHA), 'YYYY-MM-DD') AS fecha_carga,\n" +
                "            ROW_NUMBER() OVER (\n" +
                "                PARTITION BY v.AREDR_CODIGO\n" +
                "                ORDER BY NVL(v.AREVE_FECHA_CARGA, v.AREVE_FECHA) DESC NULLS LAST, v.AREVE_CODIGO DESC\n" +
                "            ) AS rn\n" +
                "        FROM AI_REC_VERIFICABLES v\n" +
                "        WHERE v.AREVE_ESTADO = 1\n" +
                "    ),\n" +
                "    ultimas_prorrogas AS (\n" +
                "        SELECT\n" +
                "            S.ARERE_CODIGO,\n" +
                "            S.ARERS_CODIGO,\n" +
                "            MAX(S.ARESO_FECHA_MAX_NUE) AS fecha_prorroga\n" +
                "        FROM AI_REC_SOLICITUDES S\n" +
                "        WHERE S.ARESO_ESTADO = '1'\n" +
                "          AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))\n" +
                "          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL\n" +
                "        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO\n" +
                "    ),\n" +
                "    datos_base AS (\n" +
                "        SELECT\n" +
                "            INF.AININ_CODIGO AS codigo_informe,\n" +
                "            SUBSTR(INF.AININ_MEMORANDO, 1, 1000) AS memo_auditoria,\n" +
                "            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'YYYY-MM-DD') AS fecha_memorando,\n" +
                "            SUBSTR(INF.AININ_MEMO_APROBACION, 1, 1000) AS memo_presidencia,\n" +
                "            TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY-MM-DD') AS fecha_aprobacion,\n" +
                "            SUBSTR(INF.AININ_USUARIO, 1, 255) AS auditor,\n" +
                "            TO_CHAR(NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO), 'YYYY-MM-DD') AS fecha_creacion_informe,\n" +
                "            SUBSTR(INF.AININ_TITULO, 1, 2000) AS titulo_informe,\n" +
                "            SUBSTR(ORG.AINOC_DESCRIPCION, 1, 1000) AS organismo_control,\n" +
                "            SUBSTR(HALL.AREHA_TITULO, 1, 3900) AS hallazgo,\n" +
                "            SUBSTR(CON.ARECO_CONCLUSION, 1, 3900) AS conclusion,\n" +
                "            REC.ARERE_CODIGO AS codigo_recomendacion,\n" +
                "            SUBSTR(REC.ARERE_DESCRIPCION, 1, 3900) AS recomendacion,\n" +
                "            TO_CHAR(COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS fecha_vencimiento,\n" +
                "            CASE\n" +
                "                WHEN uv.AREEV_CODIGO = 2 THEN 'CUMPLIDA'\n" +
                "                WHEN ES.AREES_DESCRIPCION = 'NO APLICA' THEN 'NO APLICA'\n" +
                "                WHEN COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) IS NULL THEN 'EN PROCESO'\n" +
                "                WHEN TRUNC(COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) >= TRUNC(SYSDATE) THEN 'EN PROCESO'\n" +
                "                ELSE 'NO CUMPLIDA'\n" +
                "            END AS estado_recomendacion,\n" +
                "            CASE\n" +
                "                WHEN uv.AREEV_CODIGO = 2 OR ES.AREES_DESCRIPCION IN ('CUMPLIDA', 'NO APLICA') THEN 'A TIEMPO'\n" +
                "                WHEN COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) IS NULL THEN 'EN PLAZO'\n" +
                "                WHEN TRUNC(COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) >= TRUNC(SYSDATE) THEN 'EN PLAZO'\n" +
                "                ELSE 'VENCIDA'\n" +
                "            END AS estado_plazo,\n" +
                "            CASE\n" +
                "                WHEN uv.AREEV_CODIGO = 2 OR ES.AREES_DESCRIPCION IN ('CUMPLIDA', 'NO APLICA') THEN 0\n" +
                "                WHEN COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) IS NULL THEN 0\n" +
                "                WHEN TRUNC(COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) >= TRUNC(SYSDATE) THEN 0\n" +
                "                ELSE TRUNC(SYSDATE) - TRUNC(COALESCE(pr.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA))\n" +
                "            END AS dias_vencidos,\n" +
                "            CASE\n" +
                "                WHEN uv.AREEV_CODIGO = 2 OR ES.AREES_DESCRIPCION IN ('CUMPLIDA', 'NO APLICA') THEN 100\n" +
                "                ELSE 0\n" +
                "            END AS progreso_recomendacion,\n" +
                "            SUBSTR(TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO), 1, 500) AS responsable,\n" +
                "            SUBSTR(RES.ARERS_DEPARTAMENTO, 1, 500) AS departamento,\n" +
                "            NVL(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') AS estado_verificable,\n" +
                "            uv.fecha_carga AS fecha_carga_verificable,\n" +
                "            SUBSTR(uv.AREVE_DESCRIPCION, 1, 3900) AS descripcion_verificable_responsable,\n" +
                "            SUBSTR(uv.AREVE_OBSERVACION, 1, 3900) AS observacion_auditor,\n" +
                "            NVL(INF.AININ_PROGRESO, 0) AS progreso_informe,\n" +
                "            TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') AS fecha_consulta\n" +
                "        FROM AI_INF_INFORMES INF\n" +
                "        JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO\n" +
                "        JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO\n" +
                "        JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO\n" +
                "        JOIN AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO\n" +
                "        LEFT JOIN AI_REC_ESTADO_SEGUIMIENTO ES ON REC.AREES_CODIGO = ES.AREES_CODIGO\n" +
                "        JOIN AI_REC_DETALLE_RECOMENDACION DR ON REC.ARERE_CODIGO = DR.ARERE_CODIGO AND DR.AREDR_ESTADO = 1\n" +
                "        JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO AND RES.ARERS_ESTADO = 1\n" +
                "        LEFT JOIN ultimos_verificables uv ON uv.AREDR_CODIGO = DR.AREDR_CODIGO AND uv.rn = 1\n" +
                "        LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = uv.AREEV_CODIGO\n" +
                "        LEFT JOIN ultimas_prorrogas pr ON pr.ARERE_CODIGO = DR.ARERE_CODIGO AND pr.ARERS_CODIGO = DR.ARERS_CODIGO\n" +
                "        WHERE (\n" +
                "            (INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin)\n" +
                "            OR (INF.AININ_FECHA_APROBACION IS NULL AND INF.AININ_FECHA_MEMO BETWEEN v_fecha_ini AND v_fecha_fin)\n" +
                "          )\n" +
                "          AND (organismo_control = 0 OR INF.AINOC_CODIGO = organismo_control)\n" +
                "          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)\n" +
                "          AND (auditor_responsable = '0' OR INF.AININ_USUARIO = auditor_responsable)\n" +
                "          AND (dpto = '0' OR RES.ARERS_DEPARTAMENTO = dpto)\n" +
                "          AND INF.AININ_ESTADO = 1\n" +
                "          AND HALL.AREHA_ESTADO = 1\n" +
                "          AND CON.ARECO_ESTADO = 1\n" +
                "          AND REC.ARERE_ESTADO = 1\n" +
                "          AND ORG.AINOC_ESTADO = 1\n" +
                "    )\n" +
                "    SELECT\n" +
                "        JSON_ARRAYAGG(\n" +
                "            JSON_OBJECT(\n" +
                "                'codigoInforme' VALUE codigo_informe,\n" +
                "                'memoAuditoria' VALUE memo_auditoria,\n" +
                "                'fechaMemorando' VALUE fecha_memorando,\n" +
                "                'memoPresidencia' VALUE memo_presidencia,\n" +
                "                'fechaAprobacion' VALUE fecha_aprobacion,\n" +
                "                'auditor' VALUE auditor,\n" +
                "                'fechaCreacionInforme' VALUE fecha_creacion_informe,\n" +
                "                'tituloInforme' VALUE titulo_informe,\n" +
                "                'organismoControl' VALUE organismo_control,\n" +
                "                'hallazgo' VALUE hallazgo,\n" +
                "                'conclusion' VALUE conclusion,\n" +
                "                'codigoRecomendacion' VALUE codigo_recomendacion,\n" +
                "                'recomendacion' VALUE recomendacion,\n" +
                "                'fechaVencimiento' VALUE fecha_vencimiento,\n" +
                "                'estadoRecomendacion' VALUE estado_recomendacion,\n" +
                "                'estadoPlazo' VALUE estado_plazo,\n" +
                "                'diasVencidos' VALUE dias_vencidos,\n" +
                "                'progresoRecomendacion' VALUE progreso_recomendacion,\n" +
                "                'responsable' VALUE responsable,\n" +
                "                'departamento' VALUE departamento,\n" +
                "                'estadoVerificable' VALUE estado_verificable,\n" +
                "                'fechaCargaVerificable' VALUE fecha_carga_verificable,\n" +
                "                'descripcionVerificableResponsable' VALUE descripcion_verificable_responsable,\n" +
                "                'observacionAuditor' VALUE observacion_auditor,\n" +
                "                'progresoInforme' VALUE progreso_informe,\n" +
                "                'fechaConsulta' VALUE fecha_consulta\n" +
                "                RETURNING CLOB\n" +
                "            )\n" +
                "            RETURNING CLOB\n" +
                "        )\n" +
                "    INTO p_json\n" +
                "    FROM datos_base;\n" +
                "\n" +
                "    IF p_json IS NULL THEN\n" +
                "        p_json := '[]';\n" +
                "    END IF;\n" +
                "EXCEPTION\n" +
                "    WHEN OTHERS THEN\n" +
                "        p_json := JSON_OBJECT(\n" +
                "            'error' VALUE SQLCODE,\n" +
                "            'mensaje' VALUE SQLERRM\n" +
                "        );\n" +
                "END reporteSeguimientoResponsableOperativo;";

            try (java.sql.Statement st = conn.createStatement()) {
                st.execute(ddlSp);
                System.out.println("STORED PROCEDURE RECOMPILADO CON EXITO CON REGLAS INDIVIDUALES!");
            } catch (Exception ex) {
                System.out.println("Error al recompilar SP: " + ex.getMessage());
                ex.printStackTrace();
            }
            String sqlDpto = "SELECT DISTINCT ARERS_DEPARTAMENTO FROM ROOTSISREC.AI_REC_RESPONSABLES " +
                             "WHERE UPPER(ARERS_DEPARTAMENTO) LIKE '%RELACION%' OR UPPER(ARERS_DEPARTAMENTO) LIKE '%INDUSTR%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlDpto);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("DPTO ENCONTRADO: [" + rs.getString("ARERS_DEPARTAMENTO") + "]");
                }
            }

            System.out.println("\n=== 2. RESPONSABLES Y RECOMENDACIONES DE RELACIONES INDUSTRIALES ===");
            String sqlResp = 
                "SELECT R.ARERS_CODIGO, R.ARERS_NOMBRE, R.ARERS_APELLIDO, R.ARERS_DEPARTAMENTO, " +
                "       D.AREDR_CODIGO, D.ARERE_CODIGO, TO_CHAR(D.AREDR_FECHA_MAXIMA, 'YYYY-MM-DD') AS AREDR_FECHA_MAX, " +
                "       REC.ARERE_NUM_REC, TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD') AS REC_FECHA_MAX, REC.AREES_CODIGO AS ESTADO_GLOBAL, " +
                "       ( " +
                "           SELECT S.ARESO_CODIGO || ':' || S.ARESO_ESTADO_APROB || ':' || TO_CHAR(S.ARESO_FECHA_MAX_NUE, 'YYYY-MM-DD') " +
                "           FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
                "           WHERE S.ARESO_CODIGO = ( " +
                "               SELECT MAX(S2.ARESO_CODIGO) FROM ROOTSISREC.AI_REC_SOLICITUDES S2 " +
                "               WHERE S2.ARERE_CODIGO = D.ARERE_CODIGO AND S2.ARERS_CODIGO = D.ARERS_CODIGO " +
                "           ) " +
                "       ) AS INFO_SOLICITUD, " +
                "       ( " +
                "           SELECT MAX(V.AREEV_CODIGO) " +
                "           FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                "           WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND V.AREVE_ESTADO = '1' " +
                "       ) AS MAX_AREEV_CODIGO " +
                "FROM ROOTSISREC.AI_REC_RESPONSABLES R " +
                "JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D ON D.ARERS_CODIGO = R.ARERS_CODIGO AND D.AREDR_ESTADO = 1 " +
                "JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = D.ARERE_CODIGO " +
                "WHERE (UPPER(R.ARERS_DEPARTAMENTO) LIKE '%RELACION%' OR UPPER(R.ARERS_DEPARTAMENTO) LIKE '%INDUSTR%') " +
                "ORDER BY R.ARERS_APELLIDO, D.ARERE_CODIGO";

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlResp);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println(String.format("[%d] Resp: %s %s (Cod:%d) | Rec:%d (Num:%s, GlobalEst:%s, RecMax:%s, IndMax:%s) | Solicitud (Id:Estado:FechaNue): %s | MaxVerifEv: %s",
                        count,
                        rs.getString("ARERS_NOMBRE"),
                        rs.getString("ARERS_APELLIDO"),
                        rs.getLong("ARERS_CODIGO"),
                        rs.getLong("ARERE_CODIGO"),
                        rs.getString("ARERE_NUM_REC"),
                        rs.getString("ESTADO_GLOBAL"),
                        rs.getString("REC_FECHA_MAX"),
                        rs.getString("AREDR_FECHA_MAX"),
                        rs.getString("INFO_SOLICITUD"),
                        rs.getString("MAX_AREEV_CODIGO")
                    ));
                }
                System.out.println("TOTAL RESPONSABLES-RECOMENDACIONES ENCONTRADOS: " + count);
            }

            System.out.println("\n=== 6. PROBAR SP reporteSeguimientoResponsableOperativo CON auditor='0' ===");
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteSeguimientoResponsableOperativo(?,?,?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "2000-01-01"); // fecha_inicial
                cs.setString(3, "2030-12-31"); // fecha_fin
                cs.setInt(4, 0);               // organismo_control (0 = todos)
                cs.setInt(5, 0);               // estado_seguimiento (0 = todos)
                cs.setString(6, "0");          // auditor_responsable ('0' = todos)
                cs.setString(7, "DPTO. DE RELACIONES INDUSTRIALES"); // dpto
                cs.execute();

                java.sql.Clob clob = cs.getClob(1);
                if (clob != null) {
                    String json = clob.getSubString(1, (int) clob.length());
                    System.out.println("JSON SP recibido con éxito! Longitud: " + json.length());
                    System.out.println("SNIPPET JSON: " + json.substring(0, Math.min(500, json.length())));
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    try {
                        java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO> lista = 
                            mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO>>() {});
                        System.out.println("Total registros devueltos por el SP: " + lista.size());
                        int alexEnProceso = 0;
                        int alexNoCumplida = 0;
                        int alexCumplida = 0;
                        for (ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO dto : lista) {
                            if (dto.getResponsable() != null && dto.getResponsable().contains("RODRIGUEZ")) {
                                String est = dto.getEstadoRecomendacion();
                                if ("EN PROCESO".equals(est)) alexEnProceso++;
                                else if ("NO CUMPLIDA".equals(est)) alexNoCumplida++;
                                else if ("CUMPLIDA".equals(est)) alexCumplida++;

                                System.out.println(String.format("ALEX SP -> RecId:%d | Resp:%s | FechaVenc:%s | EstRec:%s | EstPlazo:%s | DiasVenc:%d",
                                    dto.getCodigoRecomendacion(), dto.getResponsable(), dto.getFechaVencimiento(),
                                    dto.getEstadoRecomendacion(), dto.getEstadoPlazo(), dto.getDiasVencidos()));
                            }
                        }
                        System.out.println(String.format("\n=== RESUMEN SP PARA ALEXANDRA: EN PROCESO=%d | NO CUMPLIDA=%d | CUMPLIDA=%d ===",
                            alexEnProceso, alexNoCumplida, alexCumplida));
                    } catch (Exception jex) {
                        System.out.println("Error deserializando JSON: " + jex.getMessage());
                    }
                    System.out.println("\n--- PROBANDO CONSULTA MEJORADA DE PRÓRROGAS PARA RELACIONES INDUSTRIALES ---");
                    String sqlTest = 
                        "SELECT D.ARERE_CODIGO, D.ARERS_CODIGO, " +
                        "       TRIM(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO) AS RESPONSABLE, " +
                        "       TO_CHAR(D.AREDR_FECHA_MAXIMA, 'YYYY-MM-DD') AS AREDR_FECHA_MAX, " +
                        "       TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD') AS REC_FECHA_MAX, " +
                        "       TO_CHAR(( " +
                        "           SELECT MAX(S.ARESO_FECHA_MAX_NUE) " +
                        "           FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
                        "           WHERE S.ARERE_CODIGO = D.ARERE_CODIGO " +
                        "             AND S.ARERS_CODIGO = D.ARERS_CODIGO " +
                        "             AND S.ARESO_ESTADO = '1' " +
                        "             AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP))) " +
                        "             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL " +
                        "       ), 'YYYY-MM-DD') AS FECHA_PRORROGA, " +
                        "       ( " +
                        "           SELECT COUNT(1) " +
                        "           FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                        "           WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                        "             AND V.AREVE_ESTADO = '1' " +
                        "             AND V.AREEV_CODIGO = 2 " +
                        "       ) AS TIENE_JUSTIFICADO " +
                        "FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D " +
                        "JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = D.ARERE_CODIGO " +
                        "JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON R.ARERS_CODIGO = D.ARERS_CODIGO " +
                        "WHERE D.AREDR_ESTADO = 1 AND R.ARERS_DEPARTAMENTO = 'DPTO. DE RELACIONES INDUSTRIALES' " +
                        "ORDER BY RESPONSABLE, D.ARERE_CODIGO";

                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlTest);
                         java.sql.ResultSet rs = ps.executeQuery()) {
                        int count = 0;
                        while (rs.next()) {
                            String resp = rs.getString("RESPONSABLE");
                            String fProrroga = rs.getString("FECHA_PRORROGA");
                            String fInd = rs.getString("AREDR_FECHA_MAX");
                            String fRec = rs.getString("REC_FECHA_MAX");
                            int justificado = rs.getInt("TIENE_JUSTIFICADO");

                            if (fProrroga != null || (resp.contains("YANEZ") && (fRec != null && fRec.contains("2026-09")))) {
                                count++;
                                System.out.println(String.format("Resp: %s | Rec: %d | Prorroga: %s | F.Ind: %s | F.Rec: %s | Justificado: %d",
                                    resp, rs.getLong("ARERE_CODIGO"), fProrroga, fInd, fRec, justificado));
                            }
                        }
                        System.out.println("Total encontrados con solicitud o fecha relevante: " + count);
                    }
                } else {
                    System.out.println("Clob fue null!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCompileAndRunReporteSeguimientoResponsableOperativo() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. COMPILANDO reporteSeguimientoResponsableOperativo ===");
            String ddl = 
                "CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteSeguimientoResponsableOperativo (\n" +
                "    p_json              OUT CLOB,\n" +
                "    fecha_inicial       IN  VARCHAR2,\n" +
                "    fecha_fin           IN  VARCHAR2,\n" +
                "    organismo_control   IN  INTEGER,\n" +
                "    estado_seguimiento  IN  INTEGER,\n" +
                "    auditor_responsable IN  VARCHAR2,\n" +
                "    dpto                IN  VARCHAR2\n" +
                ") IS\n" +
                "    v_fecha_ini TIMESTAMP;\n" +
                "    v_fecha_fin TIMESTAMP;\n" +
                "BEGIN\n" +
                "    IF fecha_inicial IS NULL OR TRIM(fecha_inicial) IS NULL THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\\d{2}/\\d{2}/\\d{4}$') THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial) || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}') THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial), 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\\d{4}-\\d{2}-\\d{2}') THEN\n" +
                "        v_fecha_ini := TO_TIMESTAMP(SUBSTR(TRIM(fecha_inicial), 1, 10) || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS');\n" +
                "    ELSE\n" +
                "        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    END IF;\n" +
                "\n" +
                "    IF fecha_fin IS NULL OR TRIM(fecha_fin) IS NULL THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\\d{2}/\\d{2}/\\d{4}$') THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin) || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}') THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin), 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\\d{4}-\\d{2}-\\d{2}') THEN\n" +
                "        v_fecha_fin := TO_TIMESTAMP(SUBSTR(TRIM(fecha_fin), 1, 10) || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');\n" +
                "    ELSE\n" +
                "        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');\n" +
                "    END IF;\n" +
                "\n" +
                "    WITH ultimas_prorrogas AS (\n" +
                "        SELECT \n" +
                "            S.ARERE_CODIGO,\n" +
                "            S.ARERS_CODIGO,\n" +
                "            MAX(S.ARESO_FECHA_MAX_NUE) AS fecha_prorroga\n" +
                "        FROM ROOTSISREC.AI_REC_SOLICITUDES S\n" +
                "        WHERE S.ARESO_ESTADO = '1'\n" +
                "          AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))\n" +
                "          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL\n" +
                "        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO\n" +
                "    ),\n" +
                "    ultimos_verificables AS (\n" +
                "        SELECT \n" +
                "            V.AREDR_CODIGO,\n" +
                "            V.AREEV_CODIGO,\n" +
                "            V.AREVE_DESCRIPCION,\n" +
                "            V.AREVE_OBSERVACION,\n" +
                "            V.AREVE_FECHA_CARGA,\n" +
                "            ROW_NUMBER() OVER (\n" +
                "                PARTITION BY V.AREDR_CODIGO\n" +
                "                ORDER BY V.AREVE_FECHA_CARGA DESC NULLS LAST, V.AREVE_CODIGO DESC\n" +
                "            ) AS rn\n" +
                "        FROM ROOTSISREC.AI_REC_VERIFICABLES V\n" +
                "        WHERE V.AREVE_ESTADO = 1\n" +
                "    ),\n" +
                "    progreso_recomendacion AS (\n" +
                "        SELECT\n" +
                "            DR.ARERE_CODIGO,\n" +
                "            COUNT(DISTINCT DR.ARERS_CODIGO) AS total_responsables,\n" +
                "            COUNT(DISTINCT CASE WHEN UV.AREEV_CODIGO = 2 THEN DR.ARERS_CODIGO END) AS responsables_cumplidos\n" +
                "        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR\n" +
                "        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1\n" +
                "        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1\n" +
                "        WHERE DR.AREDR_ESTADO = 1\n" +
                "        GROUP BY DR.ARERE_CODIGO\n" +
                "    ),\n" +
                "    progreso_informe AS (\n" +
                "        SELECT\n" +
                "            INF.AININ_CODIGO,\n" +
                "            COUNT(DISTINCT REC.ARERE_CODIGO) AS total_recs,\n" +
                "            COUNT(DISTINCT CASE \n" +
                "                WHEN REC.AREES_CODIGO = 2 OR (PR.total_responsables > 0 AND PR.responsables_cumplidos = PR.total_responsables) \n" +
                "                THEN REC.ARERE_CODIGO \n" +
                "            END) AS recs_cumplidas\n" +
                "        FROM ROOTSISREC.AI_INF_INFORMES INF\n" +
                "        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1\n" +
                "        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1\n" +
                "        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1\n" +
                "        LEFT JOIN progreso_recomendacion PR ON PR.ARERE_CODIGO = REC.ARERE_CODIGO\n" +
                "        WHERE INF.AININ_ESTADO = 1\n" +
                "        GROUP BY INF.AININ_CODIGO\n" +
                "    ),\n" +
                "    datos_base AS (\n" +
                "        SELECT\n" +
                "            INF.AININ_CODIGO                                        AS codigoinforme,\n" +
                "            INF.AININ_MEMORANDO                                     AS memoauditoria,\n" +
                "            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'DD/MM/YYYY') AS fechamemorando,\n" +
                "            INF.AININ_MEMO_APROBACION                               AS memopresidencia,\n" +
                "            TO_CHAR(INF.AININ_FECHA_APROBACION, 'DD/MM/YYYY')       AS fechaaprobacion,\n" +
                "            INF.AININ_USUARIO                                       AS auditor,\n" +
                "            TO_CHAR(INF.AININ_FECHA, 'DD/MM/YYYY')                  AS fechacreacioninforme,\n" +
                "            TO_CHAR(INF.AININ_TITULO)                               AS tituloinforme,\n" +
                "            ORG.AINOC_DESCRIPCION                                   AS organismocontrol,\n" +
                "            TO_CHAR(HALL.AREHA_TITULO)                              AS hallazgo,\n" +
                "            CON.ARECO_CONCLUSION                                    AS conclusion,\n" +
                "            REC.ARERE_CODIGO                                        AS codigorecomendacion,\n" +
                "            TO_CHAR(REC.ARERE_DESCRIPCION)                          AS recomendacion,\n" +
                "            COALESCE(UP.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS fecha_efectiva,\n" +
                "            UV.AREEV_CODIGO                                         AS areev_codigo,\n" +
                "            REC.AREES_CODIGO                                        AS arees_codigo,\n" +
                "            TO_CHAR(UV.AREVE_FECHA_CARGA, 'DD/MM/YYYY HH24:MI:SS')  AS fechacargaverificable,\n" +
                "            TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO)     AS responsable,\n" +
                "            COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') AS estadoverificable,\n" +
                "            RES.ARERS_DEPARTAMENTO                                  AS departamento,\n" +
                "            UV.AREVE_DESCRIPCION                                    AS descripcionverificableresponsable,\n" +
                "            UV.AREVE_OBSERVACION                                    AS observacionauditor,\n" +
                "            NVL(PREC.total_responsables, 0)                         AS total_resp_rec,\n" +
                "            NVL(PREC.responsables_cumplidos, 0)                     AS resp_cump_rec,\n" +
                "            NVL(PINF.total_recs, 0)                                  AS total_recs_inf,\n" +
                "            NVL(PINF.recs_cumplidas, 0)                              AS recs_cump_inf,\n" +
                "            TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS')               AS fechaconsulta,\n" +
                "            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO)   AS fecha_orden,\n" +
                "            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\\d+'))     AS num_rec\n" +
                "        FROM ROOTSISREC.AI_INF_INFORMES INF\n" +
                "        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO\n" +
                "        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1\n" +
                "        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1\n" +
                "        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1\n" +
                "        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO AND DR.AREDR_ESTADO = 1\n" +
                "        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1\n" +
                "        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1\n" +
                "        LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = UV.AREEV_CODIGO\n" +
                "        LEFT JOIN ultimas_prorrogas UP ON UP.ARERE_CODIGO = DR.ARERE_CODIGO AND UP.ARERS_CODIGO = DR.ARERS_CODIGO\n" +
                "        LEFT JOIN progreso_recomendacion PREC ON PREC.ARERE_CODIGO = REC.ARERE_CODIGO\n" +
                "        LEFT JOIN progreso_informe PINF ON PINF.AININ_CODIGO = INF.AININ_CODIGO\n" +
                "        WHERE INF.AININ_ESTADO = 1\n" +
                "          AND ORG.AINOC_ESTADO = 1\n" +
                "          AND (NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) BETWEEN v_fecha_ini AND v_fecha_fin)\n" +
                "          AND (organismo_control = 0 OR ORG.AINOC_CODIGO = organismo_control)\n" +
                "          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)\n" +
                "          AND (auditor_responsable IS NULL OR auditor_responsable = '0' OR UPPER(TRIM(INF.AININ_USUARIO)) = UPPER(TRIM(auditor_responsable)))\n" +
                "          AND (dpto IS NULL OR dpto = '0' OR RES.ARERS_DEPARTAMENTO = dpto OR UPPER(TRIM(RES.ARERS_DEPARTAMENTO)) = UPPER(TRIM(dpto)))\n" +
                "    ),\n" +
                "    datos_calculados AS (\n" +
                "        SELECT\n" +
                "            codigoinforme,\n" +
                "            memoauditoria,\n" +
                "            fechamemorando,\n" +
                "            memopresidencia,\n" +
                "            fechaaprobacion,\n" +
                "            auditor,\n" +
                "            fechacreacioninforme,\n" +
                "            tituloinforme,\n" +
                "            organismocontrol,\n" +
                "            hallazgo,\n" +
                "            conclusion,\n" +
                "            codigorecomendacion,\n" +
                "            recomendacion,\n" +
                "            TO_CHAR(fecha_efectiva, 'DD/MM/YYYY') AS fechavencimiento,\n" +
                "            CASE\n" +
                "                WHEN areev_codigo = 2 THEN 'CUMPLIDA'\n" +
                "                WHEN arees_codigo = 4 THEN 'NO APLICA'\n" +
                "                WHEN fecha_efectiva IS NULL OR TRUNC(fecha_efectiva) >= TRUNC(CURRENT_TIMESTAMP) THEN 'EN PROCESO'\n" +
                "                ELSE 'NO CUMPLIDA'\n" +
                "            END AS estadorecomendacion,\n" +
                "            CASE\n" +
                "                WHEN areev_codigo = 2 OR arees_codigo = 4 THEN 'A TIEMPO'\n" +
                "                WHEN fecha_efectiva IS NULL OR TRUNC(fecha_efectiva) >= TRUNC(CURRENT_TIMESTAMP) THEN 'EN PLAZO'\n" +
                "                ELSE 'VENCIDO'\n" +
                "            END AS estadoplazo,\n" +
                "            CASE\n" +
                "                WHEN areev_codigo = 2 OR arees_codigo = 4 THEN 0\n" +
                "                WHEN fecha_efectiva IS NULL THEN 0\n" +
                "                WHEN FN_CALCULAR_DIAS_LABORABLES(fecha_efectiva) < 0 THEN ABS(FN_CALCULAR_DIAS_LABORABLES(fecha_efectiva))\n" +
                "                ELSE FN_CALCULAR_DIAS_LABORABLES(fecha_efectiva)\n" +
                "            END AS diasvencidos,\n" +
                "            CASE\n" +
                "                WHEN arees_codigo = 2 THEN 100.0\n" +
                "                WHEN total_resp_rec > 0 THEN ROUND((resp_cump_rec * 100.0) / total_resp_rec, 2)\n" +
                "                ELSE 0.0\n" +
                "            END AS progresorecomendacion,\n" +
                "            fechacargaverificable,\n" +
                "            responsable,\n" +
                "            estadoverificable,\n" +
                "            departamento,\n" +
                "            descripcionverificableresponsable,\n" +
                "            observacionauditor,\n" +
                "            CASE\n" +
                "                WHEN total_recs_inf > 0 THEN ROUND((recs_cump_inf * 100.0) / total_recs_inf, 2)\n" +
                "                ELSE 0.0\n" +
                "            END AS progresoinforme,\n" +
                "            fechaconsulta,\n" +
                "            fecha_orden,\n" +
                "            num_rec\n" +
                "        FROM datos_base\n" +
                "    )\n" +
                "    SELECT JSON_ARRAYAGG(\n" +
                "               JSON_OBJECT(\n" +
                "                   'codigoInforme'                    VALUE NVL(codigoinforme, 0),\n" +
                "                   'memoAuditoria'                    VALUE NVL(memoauditoria, ''),\n" +
                "                   'fechaMemorando'                   VALUE NVL(fechamemorando, ''),\n" +
                "                   'memoPresidencia'                  VALUE NVL(memopresidencia, ''),\n" +
                "                   'fechaAprobacion'                  VALUE NVL(fechaaprobacion, ''),\n" +
                "                   'auditor'                          VALUE NVL(auditor, ''),\n" +
                "                   'fechaCreacionInforme'             VALUE NVL(fechacreacioninforme, ''),\n" +
                "                   'tituloInforme'                    VALUE NVL(tituloinforme, ''),\n" +
                "                   'organismoControl'                 VALUE NVL(organismocontrol, ''),\n" +
                "                   'hallazgo'                         VALUE NVL(hallazgo, ''),\n" +
                "                   'conclusion'                       VALUE NVL(conclusion, ''),\n" +
                "                   'codigoRecomendacion'              VALUE NVL(codigorecomendacion, 0),\n" +
                "                   'numeroRecomendacion'              VALUE NVL(num_rec, 0),\n" +
                "                   'recomendacion'                    VALUE NVL(recomendacion, ''),\n" +
                "                   'fechaVencimiento'                 VALUE NVL(fechavencimiento, ''),\n" +
                "                   'estadoRecomendacion'              VALUE NVL(estadorecomendacion, ''),\n" +
                "                   'estadoPlazo'                      VALUE NVL(estadoplazo, ''),\n" +
                "                   'diasVencidos'                     VALUE NVL(diasvencidos, 0),\n" +
                "                   'progresoRecomendacion'            VALUE NVL(progresorecomendacion, 0),\n" +
                "                   'fechaCargaVerificable'            VALUE NVL(fechacargaverificable, ''),\n" +
                "                   'responsable'                      VALUE NVL(responsable, ''),\n" +
                "                   'estadoVerificable'                VALUE NVL(estadoverificable, ''),\n" +
                "                   'departamento'                     VALUE NVL(departamento, ''),\n" +
                "                   'descripcionVerificableResponsable' VALUE NVL(descripcionverificableresponsable, ''),\n" +
                "                   'observacionAuditor'               VALUE NVL(observacionauditor, ''),\n" +
                "                   'progresoInforme'                  VALUE NVL(progresoinforme, 0),\n" +
                "                   'fechaConsulta'                    VALUE NVL(fechaconsulta, '')\n" +
                "                   RETURNING CLOB\n" +
                "               )\n" +
                "               ORDER BY fecha_orden DESC, codigoinforme DESC, num_rec ASC, responsable ASC\n" +
                "               RETURNING CLOB\n" +
                "           )\n" +
                "    INTO p_json\n" +
                "    FROM datos_calculados;\n" +
                "\n" +
                "    IF p_json IS NULL THEN\n" +
                "        p_json := TO_CLOB('[]');\n" +
                "    END IF;\n" +
                "\n" +
                "EXCEPTION\n" +
                "    WHEN OTHERS THEN\n" +
                "        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));\n" +
                "END reporteSeguimientoResponsableOperativo;";

            try (java.sql.Statement st = conn.createStatement()) {
                st.execute(ddl);
                System.out.println("-> Procedimiento reporteSeguimientoResponsableOperativo creado/reemplazado con éxito!");
            }

            // Check errors
            try (java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT LINE, POSITION, TEXT FROM USER_ERRORS WHERE NAME = 'REPORTESEGUIMIENTORESPONSABLEOPERATIVO'")) {
                boolean hasErr = false;
                while (rs.next()) {
                    hasErr = true;
                    System.err.println("COMPILER ERROR en linea " + rs.getInt("LINE") + ", pos " + rs.getInt("POSITION") + ": " + rs.getString("TEXT"));
                }
                if (!hasErr) {
                    System.out.println("-> CERO ERRORES DE COMPILACION en reporteSeguimientoResponsableOperativo!");
                }
            }

            // Test execution with auditor '0'
            System.out.println("\n=== 2. PROBANDO EJECUCION CON auditor='0' ===");
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteSeguimientoResponsableOperativo(?,?,?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setInt(5, 0);
                cs.setString(6, "0");
                cs.setString(7, "0");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) Math.min(clob.length(), 2000)) : "[]";
                System.out.println("Longitud CLOB: " + (clob != null ? clob.length() : 0));
                System.out.println("Muestra primeros 600 chars:\n" + json.substring(0, Math.min(600, json.length())));
            }

            // Test execution with specific auditor 'rmerino'
            System.out.println("\n=== 3. PROBANDO EJECUCION CON auditor='rmerino' ===");
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteSeguimientoResponsableOperativo(?,?,?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setInt(4, 0);
                cs.setInt(5, 0);
                cs.setString(6, "rmerino");
                cs.setString(7, "0");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                if (clob != null) {
                    String json = clob.getSubString(1, (int) clob.length());
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO> lista = 
                        mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO>>() {});
                    System.out.println("Total registros devueltos para auditor rmerino: " + lista.size());
                    if (!lista.isEmpty()) {
                        for (int i = 0; i < Math.min(10, lista.size()); i++) {
                            ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO d = lista.get(i);
                            System.out.println(String.format("  [%d] INF:%s | AUD:%s | REC_ID:%d | NUM_REC:%d | RESP:%s | EST_REC:%s | PROG_REC:%.1f%% | PROG_INF:%.1f%%",
                                i, d.getMemoAuditoria(), d.getAuditor(), d.getCodigoRecomendacion(), d.getNumeroRecomendacion(), d.getResponsable(),
                                d.getEstadoRecomendacion(), d.getProgresoRecomendacion(), d.getProgresoInforme()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico")
    @Test
    public void testInspectEmptyRecommendation() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== INSPECCIONANDO INFORME Y RECOMENDACION AI-0133-2026 ===");
            String q1 = "SELECT INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_ESTADO, INF.AININ_USUARIO, " +
                        "       REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.ARERE_ESTADO, REC.AREES_CODIGO, " +
                        "       REC.ARERE_DESCRIPCION, REC.ARERE_FECHA_MAXIMA " +
                        "FROM AI_INF_INFORMES INF " +
                        "JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                        "JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                        "JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = C.ARECO_CODIGO " +
                        "WHERE INF.AININ_MEMORANDO LIKE '%0133%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(q1);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INF_ID:%d | MEMO:%s | INF_EST:%d | USUARIO:%s | REC_ID:%d | NUM_REC:%d | REC_EST:%d | EST_COD:%d | FECHA_MAX:%s | DESC:%.40s",
                        rs.getInt("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"), rs.getInt("AININ_ESTADO"), rs.getString("AININ_USUARIO"),
                        rs.getInt("ARERE_CODIGO"), rs.getInt("ARERE_NUM_REC"), rs.getInt("ARERE_ESTADO"), rs.getInt("AREES_CODIGO"),
                        rs.getString("ARERE_FECHA_MAXIMA"), rs.getString("ARERE_DESCRIPCION")));
                }
            }

            System.out.println("\n=== DETALLE_RECOMENDACION PARA REC_ID 873 ===");
            String q2 = "SELECT DR.* FROM AI_REC_DETALLE_RECOMENDACION DR WHERE DR.ARERE_CODIGO = 873";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(q2);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                java.sql.ResultSetMetaData md = rs.getMetaData();
                int colCount = md.getColumnCount();
                System.out.print("COLUMNAS: ");
                for (int i = 1; i <= colCount; i++) {
                    System.out.print(md.getColumnName(i) + " ");
                }
                System.out.println();
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    StringBuilder row = new StringBuilder("ROW: ");
                    for (int i = 1; i <= colCount; i++) {
                        row.append(md.getColumnName(i)).append("=").append(rs.getString(i)).append(" | ");
                    }
                    System.out.println(row.toString());
                }
                if (!found) {
                    System.out.println("-> CERO FILAS en AI_REC_DETALLE_RECOMENDACION para REC_ID 873!");
                }
            }

            System.out.println("\n=== RECOMENDACIONES DE AI-0133-2026 Y SUS RESPONSABLES ===");
            String qAllRecs = "SELECT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.AREES_CODIGO, " +
                              "       COUNT(DR.AREDR_CODIGO) as total_resp " +
                              "FROM AI_INF_INFORMES INF " +
                              "JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                              "JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                              "JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = C.ARECO_CODIGO " +
                              "LEFT JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO AND DR.AREDR_ESTADO = 1 " +
                              "WHERE INF.AININ_MEMORANDO LIKE '%0133%' " +
                              "GROUP BY REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.AREES_CODIGO " +
                              "ORDER BY TO_NUMBER(REGEXP_SUBSTR(NVL(REC.ARERE_NUM_REC, '0'), '^\\d+'))";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qAllRecs);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("  REC_ID:%d | NUM_REC:%s | EST_REC:%d | RESPONSABLES_ASIGNADOS:%d",
                        rs.getInt("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"), rs.getInt("AREES_CODIGO"), rs.getInt("total_resp")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico")
    @Test
    public void testRecompileReportePorDepartamentoEstadoSeguimiento() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== COMPILANDO reportePorDepartamentoEstadoSeguimiento DESDE V7 ===");
            java.nio.file.Path v7Path = java.nio.file.Paths.get("src/main/resources/db/V7__ActualizarReportesAuditorOperativoFechasMaximas.sql");
            String v7Content = java.nio.file.Files.readString(v7Path).replace("\r\n", "\n");
            
            String[] statements = v7Content.split("\n/\\s*(\n|$)");
            for (String stmt : statements) {
                if (stmt.toUpperCase().contains("PROCEDURE ROOTSISREC.REPORTEPORDEPARTAMENTOESTADOSEGUIMIENTO")) {
                    int createIdx = stmt.toUpperCase().indexOf("CREATE OR REPLACE");
                    if (createIdx >= 0) {
                        String ddl = stmt.substring(createIdx).trim();
                        System.out.println("Ejecutando DDL de reportePorDepartamentoEstadoSeguimiento...");
                        try (java.sql.Statement st = conn.createStatement()) {
                            st.execute(ddl);
                            System.out.println("-> DDL ejecutado exitosamente!");
                        }
                    }
                }
            }

            // Check errors
            try (java.sql.Statement st = conn.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT LINE, POSITION, TEXT FROM USER_ERRORS WHERE NAME = 'REPORTEPORDEPARTAMENTOESTADOSEGUIMIENTO'")) {
                boolean hasErr = false;
                while (rs.next()) {
                    hasErr = true;
                    System.err.println("COMPILER ERROR en linea " + rs.getInt("LINE") + ", pos " + rs.getInt("POSITION") + ": " + rs.getString("TEXT"));
                }
                if (!hasErr) {
                    System.out.println("-> CERO ERRORES DE COMPILACION en reportePorDepartamentoEstadoSeguimiento!");
                }
            }

            // Test execution
            System.out.println("\n=== PROBANDO EJECUCION DE reportePorDepartamentoEstadoSeguimiento ===");
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reportePorDepartamentoEstadoSeguimiento(?,?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2030");
                cs.setString(4, "0");
                cs.setInt(5, 0);
                cs.setString(6, "rmerino");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                if (clob != null) {
                    String json = clob.getSubString(1, (int) Math.min(clob.length(), 25000));
                    System.out.println("Longitud CLOB: " + clob.length());
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
                    if (root.isArray()) {
                        System.out.println("Total elementos devueltos: " + root.size());
                        for (int i = 0; i < Math.min(10, root.size()); i++) {
                            com.fasterxml.jackson.databind.JsonNode item = root.get(i);
                            System.out.println(String.format("  [%d] INF:%s | REC:%s | RESPONSABLES:\n       %s",
                                i,
                                item.has("memorandoAI") ? item.get("memorandoAI").asText() : "",
                                item.has("numeroRecomendacion") ? item.get("numeroRecomendacion").asText() : "",
                                item.has("funcionarioResponsable") ? item.get("funcionarioResponsable").asText() : ""
                            ));
                        }
                    } else {
                        System.out.println("Respuesta no es array: " + json.substring(0, Math.min(400, json.length())));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInspect057_2023() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== DIAGNOSTICO AI-057-2023 ===");
            String q = "SELECT INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_ESTADO, " +
                       "INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION, " +
                       "H.AREHA_CODIGO, H.AREHA_ESTADO, " +
                       "C.ARECO_CODIGO, C.ARECO_ESTADO, " +
                       "R.ARERE_CODIGO, R.ARERE_NUM_REC, R.ARERE_ESTADO, R.AREES_CODIGO, ES.AREES_DESCRIPCION, R.ARERE_FECHA_CUMPLIDA " +
                       "FROM AI_INF_INFORMES INF " +
                       "LEFT JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                       "LEFT JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                       "LEFT JOIN AI_REC_RECOMENDACIONES R ON R.ARECO_CODIGO = C.ARECO_CODIGO " +
                       "LEFT JOIN AI_REC_ESTADO_SEGUIMIENTO ES ON ES.AREES_CODIGO = R.AREES_CODIGO " +
                       "WHERE UPPER(INF.AININ_MEMORANDO) LIKE '%057%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(q);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INF_ID: %d | MEMO: %s | EST_INF: %s | F_MEMO: %s | F_APROB: %s | H_ID: %s (est:%s) | C_ID: %s (est:%s) | REC_ID: %s (num:%s, est:%s, arees:%s - %s, f_cump:%s)",
                        rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"), rs.getString("AININ_ESTADO"),
                        rs.getDate("AININ_FECHA_MEMO"), rs.getDate("AININ_FECHA_APROBACION"),
                        rs.getString("AREHA_CODIGO"), rs.getString("AREHA_ESTADO"),
                        rs.getString("ARECO_CODIGO"), rs.getString("ARECO_ESTADO"),
                        rs.getString("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"), rs.getString("ARERE_ESTADO"),
                        rs.getString("AREES_CODIGO"), rs.getString("AREES_DESCRIPCION"), rs.getDate("ARERE_FECHA_CUMPLIDA")));
                }
            }

            System.out.println("=== VERIFICABLES DE LAS RECOMENDACIONES DE 057 ===");
            String qVer = "SELECT DR.ARERE_CODIGO, DR.AREDR_CODIGO, DR.AREDR_ESTADO, RES.ARERS_CODIGO, RES.ARERS_NOMBRE, RES.ARERS_APELLIDO, RES.ARERS_ESTADO, " +
                          "V.AREVE_CODIGO, V.AREEV_CODIGO, EV.AREEV_DESCRIPCION, V.AREVE_FECHA, V.AREVE_FECHA_CARGA, V.AREVE_ESTADO " +
                          "FROM AI_INF_INFORMES INF " +
                          "JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                          "JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                          "JOIN AI_REC_RECOMENDACIONES R ON R.ARECO_CODIGO = C.ARECO_CODIGO " +
                          "JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = R.ARERE_CODIGO " +
                          "JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                          "LEFT JOIN AI_REC_VERIFICABLES V ON V.AREDR_CODIGO = DR.AREDR_CODIGO " +
                          "LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = V.AREEV_CODIGO " +
                          "WHERE UPPER(INF.AININ_MEMORANDO) LIKE '%057%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qVer);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("  REC_ID: %d | DR_ID: %d (est:%s) | RESP: %s %s (est:%s) | VER_ID: %s | EV_COD: %s (%s) | F_CARGA: %s | VER_EST: %s",
                        rs.getLong("ARERE_CODIGO"), rs.getLong("AREDR_CODIGO"), rs.getString("AREDR_ESTADO"),
                        rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO"), rs.getString("ARERS_ESTADO"),
                        rs.getString("AREVE_CODIGO"), rs.getString("AREEV_CODIGO"), rs.getString("AREEV_DESCRIPCION"),
                        rs.getDate("AREVE_FECHA_CARGA"), rs.getString("AREVE_ESTADO")));
                }
            }

            System.out.println("=== PERIODOS EXISTENTES EN AI_CORTE_EVALUACION ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT * FROM AI_CORTE_EVALUACION ORDER BY ARCEV_CODIGO DESC");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("  ARCEV_CODIGO: %d | NOMBRE: %s | TIPO: %s | F_INI: %s | F_FIN: %s | F_CORTE: %s | ANIO: %s | EST: %s",
                        rs.getLong("ARCEV_CODIGO"), rs.getString("ARCEV_NOMBRE"), rs.getString("ARCEV_TIPO"),
                        rs.getDate("ARCEV_FECHA_INICIO"), rs.getDate("ARCEV_FECHA_FIN"), rs.getDate("ARCEV_FECHA_CORTE"),
                        rs.getString("ARCEV_ANIO"), rs.getString("ARCEV_ESTADO")));
                }
            }
            System.out.println("=== TEST CON CONSULTA ACTUALIZADA PARA PERIODO 5 (CIERRE 2026) ===");
            String qTest5 = 
                "SELECT ci.AININ_CODIGO, i.AININ_MEMORANDO, r.ARERE_CODIGO, r.ARERE_NUM_REC, r.AREES_CODIGO, es.AREES_DESCRIPCION " +
                "FROM AI_INF_INFORMES i " +
                "JOIN AI_REC_HALLAZGOS h ON i.AININ_CODIGO = h.AININ_CODIGO " +
                "JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO " +
                "JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO " +
                "JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.AREES_CODIGO = es.AREES_CODIGO " +
                "JOIN AI_CORTE_EVALUACION ev ON ev.ARCEV_CODIGO = 5 " +
                "LEFT JOIN AI_CORTE_EVALUACION_INFORME ci ON ci.ARCEV_CODIGO = ev.ARCEV_CODIGO AND ci.AININ_CODIGO = i.AININ_CODIGO " +
                "WHERE (TO_CHAR(r.ARERE_ESTADO) = '1' OR r.ARERE_ESTADO = 1) " +
                "  AND TRUNC(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO)) <= ev.ARCEV_FECHA_CORTE " +
                "  AND ( " +
                "      r.AREES_CODIGO NOT IN (2, 4) " +
                "      OR (r.AREES_CODIGO = 2 AND r.ARERE_FECHA_CUMPLIDA IS NOT NULL AND TRUNC(r.ARERE_FECHA_CUMPLIDA) > ev.ARCEV_FECHA_CORTE) " +
                "  ) " +
                "  AND UPPER(TRIM(es.AREES_DESCRIPCION)) NOT IN ('NO APLICA') " +
                "  AND EXISTS ( " +
                "      SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION dr_sub " +
                "      JOIN AI_REC_RESPONSABLES res_sub ON dr_sub.arers_codigo = res_sub.arers_codigo AND res_sub.arers_estado = 1 " +
                "      LEFT JOIN ( " +
                "          SELECT v_sub.*, ROW_NUMBER() OVER (PARTITION BY v_sub.aredr_codigo ORDER BY NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha) DESC, v_sub.areve_codigo DESC) AS rn_sub " +
                "          FROM AI_REC_VERIFICABLES v_sub " +
                "          WHERE TO_CHAR(v_sub.areve_estado) = '1' " +
                "            AND (TRUNC(NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha)) <= ev.arcev_fecha_corte OR v_sub.areve_fecha_carga IS NULL) " +
                "      ) vr_sub ON dr_sub.aredr_codigo = vr_sub.aredr_codigo AND vr_sub.rn_sub = 1 " +
                "      LEFT JOIN AI_REC_ESTADO_VERIFICABLE ev_sub ON vr_sub.areev_codigo = ev_sub.areev_codigo " +
                "      WHERE dr_sub.arere_codigo = r.arere_codigo AND dr_sub.aredr_estado = 1 " +
                "        AND (vr_sub.areev_codigo IS NULL OR (vr_sub.areev_codigo != 2 AND UPPER(TRIM(NVL(ev_sub.areev_descripcion, ''))) NOT IN ('JUSTIFICA', 'JUSTIFICADO', 'JUSTIFICADA'))) " +
                "  ) " +
                "ORDER BY i.AININ_MEMORANDO, r.ARERE_NUM_REC";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qTest5);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    if (rs.getString("AININ_MEMORANDO").contains("057")) {
                        System.out.println(String.format("  -> MATCH 057 ENCONTRADO! INF: %s | REC_ID: %d (num:%s) | EST: %s (%s)",
                            rs.getString("AININ_MEMORANDO"), rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"),
                            rs.getString("AREES_CODIGO"), rs.getString("AREES_DESCRIPCION")));
                    }
                }
                System.out.println("TOTAL RECOMENDACIONES PENDIENTES DEVUELTAS POR LA CONSULTA: " + count);
            }

            System.out.println("=== TODOS LOS INFORMES DEL 2026 Y SUS FECHAS ===");
            String qInf2026 = "SELECT AININ_CODIGO, AININ_MEMORANDO, AININ_TITULO, " +
                "AININ_FECHA_MEMO, AININ_FECHA_APROBACION, AININ_ESTADO " +
                "FROM AI_INF_INFORMES " +
                "WHERE (AININ_FECHA_MEMO >= TO_DATE('2026-01-01', 'YYYY-MM-DD') OR AININ_FECHA_APROBACION >= TO_DATE('2026-01-01', 'YYYY-MM-DD')) " +
                "ORDER BY NVL(AININ_FECHA_APROBACION, AININ_FECHA_MEMO)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qInf2026);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("  INF_ID: %d | MEMO: %s | F_MEMO: %s | F_APROB: %s | EST: %s",
                        rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"),
                        rs.getDate("AININ_FECHA_MEMO"), rs.getDate("AININ_FECHA_APROBACION"),
                        rs.getString("AININ_ESTADO")));
                }
            }

            System.out.println("=== INFORMES ASIGNADOS AL PERIODO 3 (III Trimestre 2026) EN AI_CORTE_EVALUACION_INFORME ===");
            String qCi3 = "SELECT ci.*, i.AININ_MEMORANDO, i.AININ_FECHA_MEMO, i.AININ_FECHA_APROBACION, i.AININ_ESTADO " +
                "FROM AI_CORTE_EVALUACION_INFORME ci " +
                "JOIN AI_INF_INFORMES i ON ci.AININ_CODIGO = i.AININ_CODIGO " +
                "WHERE ci.ARCEV_CODIGO = 3";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qCi3);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("  ARCEV: %d | INF_ID: %d | MEMO: %s | F_MEMO: %s | F_APROB: %s | EST: %s",
                        rs.getLong("ARCEV_CODIGO"), rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"),
                        rs.getDate("AININ_FECHA_MEMO"), rs.getDate("AININ_FECHA_APROBACION"),
                        rs.getString("AININ_ESTADO")));
                }
            }

            System.out.println("=== PERIODOS 2026 DESPUES DE AUTOASIGNAR ===");
            String qPer2026 = "SELECT p.ARCEV_CODIGO, p.ARCEV_NOMBRE, p.ARCEV_TIPO, p.ARCEV_FECHA_INICIO, p.ARCEV_FECHA_FIN, p.ARCEV_FECHA_CORTE, " +
                "(SELECT COUNT(DISTINCT ci.AININ_CODIGO) FROM AI_CORTE_EVALUACION_INFORME ci JOIN AI_INF_INFORMES i ON ci.AININ_CODIGO = i.AININ_CODIGO WHERE ci.ARCEV_CODIGO = p.ARCEV_CODIGO) as total_inf, " +
                "(SELECT COUNT(DISTINCT r.ARERE_CODIGO) FROM AI_CORTE_EVALUACION_INFORME ci JOIN AI_REC_HALLAZGOS h ON ci.AININ_CODIGO = h.AININ_CODIGO JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO WHERE ci.ARCEV_CODIGO = p.ARCEV_CODIGO AND (TO_CHAR(r.ARERE_ESTADO) = '1' OR r.ARERE_ESTADO = 1)) as total_rec " +
                "FROM AI_CORTE_EVALUACION p " +
                "WHERE p.ARCEV_ANIO = 2026 AND (TO_CHAR(p.ARCEV_ESTADO) = '1' OR p.ARCEV_ESTADO = 1) " +
                "ORDER BY p.ARCEV_CODIGO ASC";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qPer2026);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("  PERIODO [%d]: %s (%s) | INFORMES: %d | RECOMENDACIONES: %d",
                        rs.getLong("ARCEV_CODIGO"), rs.getString("ARCEV_NOMBRE"), rs.getString("ARCEV_TIPO"),
                        rs.getInt("total_inf"), rs.getInt("total_rec")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Actualización ejecutada con éxito")
    @Test
    public void testUpdateNotificationMessage() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== NOTIFICACIONES A ACTUALIZAR ===");
            String selectSql = "SELECT ANOTIF_CODIGO, ANOTIF_MENSAJE FROM AI_NOTIFICACIONES WHERE ANOTIF_MENSAJE LIKE '%(Auditor Operativo) eliminó%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(selectSql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("ID: " + rs.getLong("ANOTIF_CODIGO") + " | MENSAJE: " + rs.getString("ANOTIF_MENSAJE"));
                }
            }

            String updateVerif = "UPDATE AI_NOTIFICACIONES SET ANOTIF_MENSAJE = REGEXP_REPLACE(ANOTIF_MENSAJE, '^([a-zA-Z0-9_]+) \\(Auditor Operativo\\) eliminó el verificable', 'El Auditor \\1 eliminó el verificable') WHERE ANOTIF_MENSAJE LIKE '%(Auditor Operativo) eliminó el verificable%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(updateVerif)) {
                int rows = ps.executeUpdate();
                System.out.println("Filas actualizadas (verificables): " + rows);
            }

            String updateSol = "UPDATE AI_NOTIFICACIONES SET ANOTIF_MENSAJE = REGEXP_REPLACE(ANOTIF_MENSAJE, '^([a-zA-Z0-9_]+) \\(Auditor Operativo\\) eliminó la solicitud', 'El Auditor \\1 eliminó la solicitud') WHERE ANOTIF_MENSAJE LIKE '%(Auditor Operativo) eliminó la solicitud%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(updateSol)) {
                int rows = ps.executeUpdate();
                System.out.println("Filas actualizadas (solicitudes): " + rows);
            }

            System.out.println("=== NOTIFICACIONES DESPUES DE ACTUALIZAR ===");
            String verifySql = "SELECT ANOTIF_CODIGO, ANOTIF_MENSAJE FROM AI_NOTIFICACIONES WHERE ANOTIF_MENSAJE LIKE '%eliminó el verificable%' OR ANOTIF_MENSAJE LIKE '%eliminó la solicitud%' ORDER BY ANOTIF_CODIGO DESC";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(verifySql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next() && count < 5) {
                    System.out.println("ID: " + rs.getLong("ANOTIF_CODIGO") + " | MENSAJE: " + rs.getString("ANOTIF_MENSAJE"));
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Diagnóstico completado")
    @Test
    public void testInspectAuditorEmail() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== DETALLE DE AREDR 2501 ===");
            String sqlAredr = "SELECT dr.AREDR_CODIGO, dr.ARERE_CODIGO, dr.ARERS_CODIGO, " +
                    "rec.ARERE_USUARIO, rec.ARERE_NUM_REC, inf.AININ_CODIGO, inf.AININ_MEMORANDO, inf.AININ_USUARIO, " +
                    "resp.ARERS_NOMBRE, resp.ARERS_APELLIDO, resp.ARERS_USUARIO_PER, resp.ARERS_USUARIO " +
                    "FROM AI_REC_DETALLE_RECOMENDACION dr " +
                    "JOIN AI_REC_RECOMENDACIONES rec ON dr.ARERE_CODIGO = rec.ARERE_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES con ON rec.ARECO_CODIGO = con.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS hall ON con.AREHA_CODIGO = hall.AREHA_CODIGO " +
                    "JOIN AI_INF_INFORMES inf ON hall.AININ_CODIGO = inf.AININ_CODIGO " +
                    "JOIN AI_REC_RESPONSABLES resp ON dr.ARERS_CODIGO = resp.ARERS_CODIGO " +
                    "WHERE dr.AREDR_CODIGO = 2501";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlAredr);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(String.format("AREDR: %d | ARERE: %d | REC_USUARIO: %s | INF_ID: %d | INF_USUARIO: %s | MEMO: %s | RESP: %s %s (cuenta: %s, usuario_per: %s)",
                            rs.getLong("AREDR_CODIGO"), rs.getLong("ARERE_CODIGO"),
                            rs.getString("ARERE_USUARIO"), rs.getLong("AININ_CODIGO"),
                            rs.getString("AININ_USUARIO"), rs.getString("AININ_MEMORANDO"),
                            rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO"),
                            rs.getString("ARERS_USUARIO"), rs.getString("ARERS_USUARIO_PER")));
                }
            }
            System.out.println("=== AUDITORES EN AI_SEG_ROL_USU ===");
            String sqlUsers = "SELECT ASERU_CODIGO, ASERO_CODIGO, ASERU_CUENTA, ASERU_NOMBRE, ASERU_APELLIDO, ASERU_CORREO, ASERU_ESTADO FROM AI_SEG_ROL_USU WHERE ASERO_CODIGO IN (2,3,4) ORDER BY ASERO_CODIGO, ASERU_CUENTA";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlUsers);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("ROL: %d | CUENTA: %s | NOMBRE: %s %s | CORREO: %s | ESTADO: %s",
                            rs.getInt("ASERO_CODIGO"), rs.getString("ASERU_CUENTA"),
                            rs.getString("ASERU_NOMBRE"), rs.getString("ASERU_APELLIDO"),
                            rs.getString("ASERU_CORREO"), rs.getString("ASERU_ESTADO")));
                }
            }

            System.out.println("\n=== INFORMES RECIENTES Y SUS AUDITORES ===");
            String sqlInf = "SELECT INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_TITULO, INF.AININ_USUARIO, " +
                    "(SELECT USU.ASERU_CORREO FROM AI_SEG_ROL_USU USU WHERE USU.ASERU_CUENTA = INF.AININ_USUARIO AND USU.ASERU_ESTADO = '1' AND ROWNUM = 1) AS CORREO_USU_INF " +
                    "FROM AI_INF_INFORMES INF ORDER BY INF.AININ_CODIGO DESC FETCH FIRST 10 ROWS ONLY";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlInf);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INF_ID: %d | MEMO: %s | TITULO: %s | AININ_USUARIO: %s | CORREO: %s",
                            rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"),
                            rs.getString("AININ_TITULO"), rs.getString("AININ_USUARIO"),
                            rs.getString("CORREO_USU_INF")));
                }
            }

            System.out.println("\n=== RECOMENDACIONES DEL INFORME PRUEBA AI O RECIENTES ===");
            String sqlRec = "SELECT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.ARERE_USUARIO, INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_USUARIO " +
                    "FROM AI_REC_RECOMENDACIONES REC " +
                    "JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                    "JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                    "ORDER BY REC.ARERE_CODIGO DESC FETCH FIRST 10 ROWS ONLY";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlRec);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("REC_ID: %d | NUM: %d | REC_USUARIO: %s | INF_ID: %d | MEMO: %s | INF_USUARIO: %s",
                            rs.getLong("ARERE_CODIGO"), rs.getInt("ARERE_NUM_REC"),
                            rs.getString("ARERE_USUARIO"), rs.getLong("AININ_CODIGO"),
                            rs.getString("AININ_MEMORANDO"), rs.getString("AININ_USUARIO")));
                }
            }

            System.out.println("\n=== CORREO DEL AUDITOR POR INFORME Y POR RECOMENDACION ===");
            // Probar las dos consultas: por informe y por recomendacion
            String qAudInf = "SELECT USU.ASERU_CORREO, USU.ASERU_CUENTA, USU.ASERU_NOMBRE, USU.ASERU_APELLIDO " +
                    "FROM AI_SEG_ROL_USU USU " +
                    "WHERE USU.ASERU_CUENTA = (SELECT INF.AININ_USUARIO FROM AI_INF_INFORMES INF WHERE INF.AININ_CODIGO = 111) " +
                    "AND USU.ASERU_CORREO IS NOT NULL AND USU.ASERU_ESTADO = '1' FETCH FIRST 1 ROWS ONLY";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qAudInf);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Por Informe (111): " + rs.getString("ASERU_CUENTA") + " -> " + rs.getString("ASERU_CORREO"));
                } else {
                    System.out.println("Por Informe (111): NO ENCONTRADO");
                }
            }

            String qAudRec = "SELECT USU.ASERU_CORREO, USU.ASERU_CUENTA, USU.ASERU_NOMBRE, USU.ASERU_APELLIDO " +
                    "FROM AI_SEG_ROL_USU USU " +
                    "JOIN AI_REC_RECOMENDACIONES REC ON REC.ARERE_USUARIO = USU.ASERU_CUENTA " +
                    "WHERE REC.ARERE_CODIGO = 895 AND USU.ASERU_CORREO IS NOT NULL AND USU.ASERU_ESTADO = '1' FETCH FIRST 1 ROWS ONLY";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(qAudRec);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Por Recomendacion (895): " + rs.getString("ASERU_CUENTA") + " -> " + rs.getString("ASERU_CORREO"));
                } else {
                    System.out.println("Por Recomendacion (895): NO ENCONTRADO");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de envío completada")
    @Test
    public void testSendWsCorreo() {
        try {
            System.out.println("=== TEST SEND WS_CORREO ===");
            javax.net.ssl.SSLContext sslContext = org.apache.http.ssl.SSLContextBuilder.create()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();

            org.apache.http.conn.ssl.SSLConnectionSocketFactory socketFactory = new org.apache.http.conn.ssl.SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1.2"},
                    null,
                    org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE);

            org.apache.http.impl.client.CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            org.springframework.http.client.HttpComponentsClientHttpRequestFactory factory = new org.springframework.http.client.HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(30000);

            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate(factory);
            java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters = new java.util.ArrayList<>(restTemplate.getMessageConverters());
            for (int i = 0; i < converters.size(); i++) {
                if (converters.get(i) instanceof org.springframework.http.converter.StringHttpMessageConverter) {
                    converters.set(i, new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));
                    break;
                }
            }
            restTemplate.setMessageConverters(converters);

            String apiKeySys = "8badb283-06ea-4662-8c2e-fb06a90db80a";
            String apiKeyTemp = "dc7e2579-bdc1-42eb-9436-be8e2223f212";
            String url = "https://app.eeasa.com.ec/ws_correo/eeasa/mail/send";
            String usuarioNombre = "SISREC - Unidad de Auditoria Interna";
            String destinatario = "angiecaiza27@gmail.com";
            String asunto = "Notificación: Verificable pendiente de revisión - SISREC";

            java.util.Map<String, Object> vars = new java.util.HashMap<>();
            vars.put("fecha", "21/09/2026 09:00:00");
            vars.put("auditor", "MARIA ELENA GUEVARA UVIDIA");
            vars.put("tituloInforme", "PRUEBA 1");
            vars.put("memoAprobacion", "PRUEBA AI");
            vars.put("numRecomendacion", "1");
            vars.put("recDescripcion", "Recomendación de prueba");
            vars.put("nombreResponsable", "JUAN PEREZ");
            vars.put("descripcionVerificable", "Verificable cargado");
            vars.put("nombreArchivo", "documento.pdf");

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String parametrosJson = objectMapper.writeValueAsString(vars);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
            headers.add("apiKey_sys", apiKeySys);
            headers.add("apiKey_temp", apiKeyTemp);
            headers.add("destinatario", destinatario);
            headers.add("usuarioNombre", usuarioNombre);
            headers.add("asunto", asunto);

            org.springframework.http.HttpHeaders textPartHeaders = new org.springframework.http.HttpHeaders();
            textPartHeaders.setContentType(new org.springframework.http.MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8));

            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("apiKey_sys", new org.springframework.http.HttpEntity<>(apiKeySys, textPartHeaders));
            body.add("apiKey_temp", new org.springframework.http.HttpEntity<>(apiKeyTemp, textPartHeaders));
            body.add("destinatario", new org.springframework.http.HttpEntity<>(destinatario, textPartHeaders));
            body.add("usuarioNombre", new org.springframework.http.HttpEntity<>(usuarioNombre, textPartHeaders));
            body.add("asunto", new org.springframework.http.HttpEntity<>(asunto, textPartHeaders));
            body.add("parametros", new org.springframework.http.HttpEntity<>(parametrosJson, textPartHeaders));

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(body, headers);

            System.out.println("Enviando petición a ws_correo...");
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            System.out.println("Respuesta Status: " + response.getStatusCode());
            System.out.println("Respuesta Body: " + response.getBody());
        } catch (Exception e) {
            System.out.println("EXCEPCIÓN al llamar ws_correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testInspectPaloraRecommendations() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("\n=== 1. RECOMMENDATIONS FOR AREA PALORA (AP) ===");
            String sql = "SELECT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.AREES_CODIGO, " +
                    "EST.AREES_DESCRIPCION, REC.ARERE_ESTADO, " +
                    "TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD') as f_max_rec, " +
                    "INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_USUARIO, " +
                    "TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY-MM-DD') as f_apr, " +
                    "RESP.ARERS_CODIGO, RESP.ARERS_NOMBRE, RESP.ARERS_DEPARTAMENTO, RESP.ARERS_DEPARTAMENTO_ABREV, " +
                    "DET.AREDR_CODIGO, DET.AREDR_ESTADO, TO_CHAR(DET.AREDR_FECHA_MAXIMA, 'YYYY-MM-DD') as f_max_det " +
                    "FROM AI_REC_RECOMENDACIONES REC " +
                    "JOIN AI_REC_ESTADO_SEGUIMIENTO EST ON REC.AREES_CODIGO = EST.AREES_CODIGO " +
                    "JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO " +
                    "JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                    "JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                    "WHERE (UPPER(RESP.ARERS_DEPARTAMENTO) LIKE '%PALORA%' OR UPPER(RESP.ARERS_DEPARTAMENTO_ABREV) = 'AP') " +
                    "ORDER BY REC.ARERE_CODIGO";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("REC_ID: %d | NUM_REC: %s | ESTADO_REC: %s (cod:%d) | INF: %s (id:%d, usu:%s) | RESP: %s (%s) | F_APR: %s | F_MAX_REC: %s | F_MAX_DET: %s",
                            rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"), rs.getString("AREES_DESCRIPCION"), rs.getInt("AREES_CODIGO"),
                            rs.getString("AININ_MEMORANDO"), rs.getLong("AININ_CODIGO"), rs.getString("AININ_USUARIO"),
                            rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_DEPARTAMENTO_ABREV"),
                            rs.getString("f_apr"), rs.getString("f_max_rec"), rs.getString("f_max_det")));
                }
            }

            System.out.println("\n=== 7. DESCRIPTION OF RECS 877, 878, 879 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARERE_CODIGO, ARERE_NUM_REC, ARERE_DESCRIPCION, AREES_CODIGO " +
                    "FROM AI_REC_RECOMENDACIONES WHERE ARERE_CODIGO IN (877, 878, 879)")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("REC " + rs.getLong("ARERE_CODIGO") + " (#" + rs.getString("ARERE_NUM_REC") + ") [est:" + rs.getInt("AREES_CODIGO") + "]: " + rs.getString("ARERE_DESCRIPCION"));
                    }
                }
            }

            System.out.println("\n=== 5. CHECK ALL PALORA RECS ACROSS ALL INFORMS ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.AREES_CODIGO, EST.AREES_DESCRIPCION, " +
                    "INF.AININ_CODIGO, INF.AININ_MEMORANDO, " +
                    "TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY-MM-DD') as f_apr, " +
                    "TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD') as f_max, " +
                    "COUNT(V.AREVE_CODIGO) as total_vers, " +
                    "COUNT(CASE WHEN V.AREEV_CODIGO = 2 THEN 1 END) as vers_validados " +
                    "FROM AI_REC_RECOMENDACIONES REC " +
                    "JOIN AI_REC_ESTADO_SEGUIMIENTO EST ON REC.AREES_CODIGO = EST.AREES_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                    "JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                    "JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO " +
                    "JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO " +
                    "LEFT JOIN AI_REC_VERIFICABLES V ON DET.AREDR_CODIGO = V.AREDR_CODIGO AND V.AREVE_ESTADO = '1' " +
                    "WHERE (UPPER(RESP.ARERS_DEPARTAMENTO) LIKE '%PALORA%' OR UPPER(RESP.ARERS_DEPARTAMENTO_ABREV) = 'AP') " +
                    "AND REC.ARERE_ESTADO = 1 AND DET.AREDR_ESTADO = 1 " +
                    "GROUP BY REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.AREES_CODIGO, EST.AREES_DESCRIPCION, INF.AININ_CODIGO, INF.AININ_MEMORANDO, INF.AININ_FECHA_APROBACION, REC.ARERE_FECHA_MAXIMA " +
                    "ORDER BY INF.AININ_CODIGO, REC.ARERE_CODIGO")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("INF: %-15s (id:%d, apr:%s) | REC_ID: %d | NUM: %-3s | EST: %-12s (cod:%d) | F_MAX: %s | VERS: %d (validados:%d)",
                                rs.getString("AININ_MEMORANDO"), rs.getLong("AININ_CODIGO"), rs.getString("f_apr"),
                                rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"),
                                rs.getString("AREES_DESCRIPCION"), rs.getInt("AREES_CODIGO"),
                                rs.getString("f_max"), rs.getInt("total_vers"), rs.getInt("vers_validados")));
                    }
                }
            }

            System.out.println("\n=== 17. DETAILED INSPECTION OF 5 STUCK RECS ===");
            long[] sampleRecs = {700, 777, 809, 853, 877};
            for (long recId : sampleRecs) {
                System.out.println("\n--- REC " + recId + " ---");
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.AREES_CODIGO, " +
                        "TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD') as f_max_rec, " +
                        "DET.AREDR_CODIGO, RESP.ARERS_NOMBRE, RESP.ARERS_DEPARTAMENTO_ABREV, " +
                        "TO_CHAR(DET.AREDR_FECHA_MAXIMA, 'YYYY-MM-DD') as f_max_det, " +
                        "V.AREVE_CODIGO, V.AREEV_CODIGO, EV.AREEV_DESCRIPCION as est_ver, " +
                        "TO_CHAR(V.AREVE_FECHA_CARGA, 'YYYY-MM-DD HH24:MI') as f_carga " +
                        "FROM AI_REC_RECOMENDACIONES REC " +
                        "JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO " +
                        "JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO " +
                        "LEFT JOIN AI_REC_VERIFICABLES V ON DET.AREDR_CODIGO = V.AREDR_CODIGO AND V.AREVE_ESTADO = '1' " +
                        "LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON V.AREEV_CODIGO = EV.AREEV_CODIGO " +
                        "WHERE REC.ARERE_CODIGO = ? AND REC.ARERE_ESTADO = 1 AND DET.AREDR_ESTADO = 1 " +
                        "ORDER BY DET.AREDR_CODIGO, V.AREVE_FECHA_CARGA DESC")) {
                    ps.setLong(1, recId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            System.out.println(String.format("REC %d (#%s) [est_rec:%d, f_max:%s] | DET %d: %s (%s) [f_max_det:%s] | VER %s: %s [carga:%s]",
                                    rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"), rs.getInt("AREES_CODIGO"), rs.getString("f_max_rec"),
                                    rs.getLong("AREDR_CODIGO"), rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_DEPARTAMENTO_ABREV"),
                                    rs.getString("f_max_det"), rs.getString("AREVE_CODIGO"), rs.getString("est_ver"), rs.getString("f_carga")));
                        }
                    }
                }
            }

            System.out.println("\n=== 16. RECS WITH APPROVED PRORROGAS (>= SYSDATE) BUT MARKED NO CUMPLIDA ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DISTINCT REC.ARERE_CODIGO, REC.ARERE_NUM_REC, INF.AININ_MEMORANDO, " +
                    "TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD') as f_max_rec, " +
                    "TO_CHAR(MAX(S.ARESO_FECHA_MAX_NUE), 'YYYY-MM-DD') as f_prorroga " +
                    "FROM AI_REC_RECOMENDACIONES REC " +
                    "JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                    "JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                    "JOIN AI_REC_SOLICITUDES S ON REC.ARERE_CODIGO = S.ARERE_CODIGO " +
                    "WHERE REC.AREES_CODIGO = 3 AND REC.ARERE_ESTADO = 1 AND INF.AININ_ESTADO = 1 " +
                    "AND S.ARESO_ESTADO_APROB = '3' AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE) " +
                    "GROUP BY REC.ARERE_CODIGO, REC.ARERE_NUM_REC, INF.AININ_MEMORANDO, REC.ARERE_FECHA_MAXIMA " +
                    "ORDER BY REC.ARERE_CODIGO")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("REC %d (#%s) in %s | F_MAX_REC: %s | F_PRORROGA: %s",
                                rs.getLong("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"), rs.getString("AININ_MEMORANDO"),
                                rs.getString("f_max_rec"), rs.getString("f_prorroga")));
                    }
                }
            }

            System.out.println("\n=== 10. DETALLE RECOMENDACION FOR RECS 877, 878, 879 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT D.AREDR_CODIGO, D.ARERE_CODIGO, D.ARERS_CODIGO, R.ARERS_NOMBRE, R.ARERS_DEPARTAMENTO_ABREV, " +
                    "TO_CHAR(D.AREDR_FECHA_MAXIMA, 'YYYY-MM-DD HH24:MI:SS') as f_max_det, D.AREDR_ESTADO, " +
                    "TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'YYYY-MM-DD HH24:MI:SS') as f_max_rec, REC.AREES_CODIGO " +
                    "FROM AI_REC_DETALLE_RECOMENDACION D " +
                    "JOIN AI_REC_RECOMENDACIONES REC ON D.ARERE_CODIGO = REC.ARERE_CODIGO " +
                    "JOIN AI_REC_RESPONSABLES R ON D.ARERS_CODIGO = R.ARERS_CODIGO " +
                    "WHERE D.ARERE_CODIGO IN (877, 878, 879) ORDER BY D.ARERE_CODIGO, D.AREDR_CODIGO")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("REC: %d | DET: %d | RESP_COD: %d | RESP: %s (%s) | F_MAX_DET: %s | F_MAX_REC: %s | ESTADO_REC: %d | DET_EST: %s",
                                rs.getLong("ARERE_CODIGO"), rs.getLong("AREDR_CODIGO"), rs.getLong("ARERS_CODIGO"),
                                rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_DEPARTAMENTO_ABREV"),
                                rs.getString("f_max_det"), rs.getString("f_max_rec"), rs.getInt("AREES_CODIGO"), rs.getString("AREDR_ESTADO")));
                    }
                }
            }

            System.out.println("\n=== 11. SOLICITUDES FOR RECS 877, 878, 879 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT S.ARESO_CODIGO, S.ARERE_CODIGO, S.ARERS_CODIGO, S.ARESO_ESTADO_APROB, " +
                    "TO_CHAR(S.ARESO_FECHA_MAX_ANT, 'YYYY-MM-DD') as f_ant, " +
                    "TO_CHAR(S.ARESO_FECHA_MAX_NUE, 'YYYY-MM-DD') as f_nue, " +
                    "S.ARESO_DESCRIPCION " +
                    "FROM AI_REC_SOLICITUDES S " +
                    "WHERE S.ARERE_CODIGO IN (877, 878, 879) ORDER BY S.ARESO_CODIGO")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("SOL: %d | REC: %d | RESP: %s | APROB: %s | F_ANT: %s | F_NUE: %s | DESC: %s",
                                rs.getLong("ARESO_CODIGO"), rs.getLong("ARERE_CODIGO"), rs.getString("ARERS_CODIGO"),
                                rs.getString("ARESO_ESTADO_APROB"), rs.getString("f_ant"), rs.getString("f_nue"),
                                rs.getString("ARESO_DESCRIPCION")));
                    }
                }
            }

            System.out.println("\n=== 9. CHECK IF THERE ARE ANY TABLES WITH HISTORY OR LOGS ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = 'ROOTSISREC' ORDER BY TABLE_NAME")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("TABLE: " + rs.getString("TABLE_NAME"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Diagnóstico ejecutado")
    @Test
    public void testInspectRec854() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. REC 854 DATA ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARERE_CODIGO, ARERE_NUM_REC, ARERE_FECHA_MAXIMA, AREES_CODIGO, ARERE_NUM_PRORROGA, ARERE_ESTADO FROM AI_REC_RECOMENDACIONES WHERE ARERE_CODIGO = 854")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("REC 854: NUM=%s | F_MAX=%s | AREES=%s | NUM_PRORR=%s | ESTADO=%s",
                                rs.getString("ARERE_NUM_REC"), rs.getString("ARERE_FECHA_MAXIMA"),
                                rs.getString("AREES_CODIGO"), rs.getString("ARERE_NUM_PRORROGA"),
                                rs.getString("ARERE_ESTADO")));
                    }
                }
            }

            System.out.println("\n=== 2. DETALLES FOR REC 854 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, DR.ARERS_CODIGO, DR.AREDR_FECHA_MAXIMA, DR.AREDR_ESTADO, R.ARERS_CODIGO_PER, R.ARERS_NOMBRE, R.ARERS_ESTADO " +
                    "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                    "JOIN AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO " +
                    "WHERE DR.ARERE_CODIGO = 854")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("DET: AREDR=%d | ARERS_COD=%d | COD_PER=%s | NOMBRE=%s | F_MAX_DET=%s | DET_EST=%s | RESP_EST=%s",
                                rs.getLong("AREDR_CODIGO"), rs.getLong("ARERS_CODIGO"),
                                rs.getString("ARERS_CODIGO_PER"),
                                rs.getString("ARERS_NOMBRE"), rs.getString("AREDR_FECHA_MAXIMA"),
                                rs.getString("AREDR_ESTADO"), rs.getString("ARERS_ESTADO")));
                    }
                }
            }

            System.out.println("\n=== 2b. RESPONSABLE 2596 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM AI_REC_RESPONSABLES WHERE ARERS_CODIGO = 2596")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("RESP 2596: COD_PER=%s | NOMBRE=%s %s | CARGO=%s",
                                rs.getString("ARERS_CODIGO_PER"), rs.getString("ARERS_NOMBRE"),
                                rs.getString("ARERS_APELLIDO"), rs.getString("ARERS_CARGO")));
                    }
                }
            }

            System.out.println("\n=== RECOMENDACIONES CON ESTADO 3 (NO CUMPLIDAS) PERO CON DETALLES VIGENTES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT R.ARERE_CODIGO, R.ARERE_NUM_REC, R.ARERE_FECHA_MAXIMA, R.AREES_CODIGO, " +
                    "       MAX(D.AREDR_FECHA_MAXIMA) as MAX_DET_FECHA " +
                    "FROM AI_REC_RECOMENDACIONES R " +
                    "LEFT JOIN AI_REC_DETALLE_RECOMENDACION D ON R.ARERE_CODIGO = D.ARERE_CODIGO AND D.AREDR_ESTADO = '1' " +
                    "WHERE R.ARERE_ESTADO = 1 AND R.AREES_CODIGO = 3 " +
                    "GROUP BY R.ARERE_CODIGO, R.ARERE_NUM_REC, R.ARERE_FECHA_MAXIMA, R.AREES_CODIGO")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("INCONSISTENCIA: REC=%s | NUM=%s | F_MAX_REC=%s | MAX_DET=%s",
                                rs.getString("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"),
                                rs.getString("ARERE_FECHA_MAXIMA"), rs.getString("MAX_DET_FECHA")));
                    }
                }
            }

            System.out.println("\n=== RECOMENDACIONES CON PRORROGAS APROBADAS ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT S.ARESO_CODIGO, S.ARERE_CODIGO, S.ARERS_CODIGO, S.ARESO_ESTADO_APROB, " +
                    "       S.ARESO_FECHA_MAX_ANT, S.ARESO_FECHA_MAX_NUE, " +
                    "       R.ARERE_FECHA_MAXIMA, R.AREES_CODIGO, D.AREDR_FECHA_MAXIMA " +
                    "FROM AI_REC_SOLICITUDES S " +
                    "JOIN AI_REC_RECOMENDACIONES R ON S.ARERE_CODIGO = R.ARERE_CODIGO " +
                    "LEFT JOIN AI_REC_DETALLE_RECOMENDACION D ON D.ARERE_CODIGO = S.ARERE_CODIGO AND D.ARERS_CODIGO = S.ARERS_CODIGO " +
                    "WHERE S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("SOL APROBADA: SOL=%s | REC=%s | RESP=%s | F_NUE=%s | F_MAX_REC=%s | AREES=%s | F_MAX_DET=%s",
                                rs.getString("ARESO_CODIGO"), rs.getString("ARERE_CODIGO"),
                                rs.getString("ARERS_CODIGO"), rs.getString("ARESO_FECHA_MAX_NUE"),
                                rs.getString("ARERE_FECHA_MAXIMA"), rs.getString("AREES_CODIGO"),
                                rs.getString("AREDR_FECHA_MAXIMA")));
                    }
                }
            }

            System.out.println("\n=== FIN DE CONSULTAS ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico de REC 1")
    @Test
    public void testInspectRec1Memo115() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== CONSULTANDO REC 1 DEL MEMORANDO AI-115-2026 ===");
            String sql = "SELECT I.AININ_CODIGO, I.AININ_MEMORANDO, I.AININ_TITULO, " +
                         "       R.ARERE_CODIGO, R.ARERE_NUM_REC, " +
                         "       R.ARERE_FECHA_MAXIMA, R.AREES_CODIGO, E.AREES_DESCRIPCION " +
                         "FROM AI_INF_INFORMES I " +
                         "JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = I.AININ_CODIGO " +
                         "JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                         "JOIN AI_REC_RECOMENDACIONES R ON R.ARECO_CODIGO = C.ARECO_CODIGO " +
                         "LEFT JOIN AI_REC_ESTADO_SEGUIMIENTO E ON E.AREES_CODIGO = R.AREES_CODIGO " +
                         "WHERE (I.AININ_MEMORANDO LIKE '%115-2026%' OR I.AININ_ORDEN_TRABAJO LIKE '%115-2026%') " +
                         "ORDER BY R.ARERE_NUM_REC";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INFORME: COD=%s | MEMO=%s", rs.getString("AININ_CODIGO"), rs.getString("AININ_MEMORANDO")));
                    System.out.println(String.format("REC 1: ARERE_CODIGO=%s | NUM_REC=%s | F_MAX=%s | ESTADO_COD=%s | ESTADO_DESC=%s",
                            rs.getString("ARERE_CODIGO"), rs.getString("ARERE_NUM_REC"), rs.getString("ARERE_FECHA_MAXIMA"),
                            rs.getString("AREES_CODIGO"), rs.getString("AREES_DESCRIPCION")));
                }
            }

            System.out.println("\n=== DETALLES (RESPONSABLES) DE REC 1 ===");
            String sqlDet = "SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, R.ARERS_NOMBRE, R.ARERS_APELLIDO, " +
                            "       DR.AREDR_FECHA_MAXIMA, DR.AREDR_ESTADO " +
                            "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                            "JOIN AI_REC_RESPONSABLES R ON R.ARERS_CODIGO = DR.ARERS_CODIGO " +
                            "WHERE DR.ARERE_CODIGO IN (" +
                            "    SELECT R2.ARERE_CODIGO FROM AI_INF_INFORMES I2 " +
                            "    JOIN AI_REC_HALLAZGOS H2 ON H2.AININ_CODIGO = I2.AININ_CODIGO " +
                            "    JOIN AI_REC_CONCLUSIONES C2 ON C2.AREHA_CODIGO = H2.AREHA_CODIGO " +
                            "    JOIN AI_REC_RECOMENDACIONES R2 ON R2.ARECO_CODIGO = C2.ARECO_CODIGO " +
                            "    WHERE (I2.AININ_MEMORANDO LIKE '%115-2026%' OR I2.AININ_ORDEN_TRABAJO LIKE '%115-2026%') " +
                            ")";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlDet);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("DET: AREDR=%s | RESP=%s %s | F_MAX=%s | EST=%s",
                            rs.getString("AREDR_CODIGO"), rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO"),
                            rs.getString("AREDR_FECHA_MAXIMA"), rs.getString("AREDR_ESTADO")));
                }
            }

            System.out.println("\n=== VERIFICABLES DE REC 1 ===");
            String sqlVer = "SELECT V.AREVE_CODIGO, V.AREDR_CODIGO, V.AREEV_CODIGO, EV.AREEV_DESCRIPCION, V.AREVE_ESTADO, V.AREVE_FECHA_CARGA " +
                            "FROM AI_REC_VERIFICABLES V " +
                            "LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = V.AREEV_CODIGO " +
                            "WHERE V.AREDR_CODIGO IN (2385, 2386)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlVer);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("VER: COD=%s | AREDR=%s | AREEV=%s (%s) | EST=%s | FECHA=%s",
                            rs.getString("AREVE_CODIGO"), rs.getString("AREDR_CODIGO"),
                            rs.getString("AREEV_CODIGO"), rs.getString("AREEV_DESCRIPCION"),
                            rs.getString("AREVE_ESTADO"), rs.getString("AREVE_FECHA_CARGA")));
                }
            }

            System.out.println("\n=== RESULTADO DE FN_RECOMENDACIONES_INFORME(104, '0') ===");
            try (java.sql.CallableStatement cs = conn.prepareCall("{?=call ROOTSISREC.FN_RECOMENDACIONES_INFORME(?,?)}")) {
                cs.registerOutParameter(1, java.sql.Types.REF_CURSOR);
                cs.setLong(2, 104L);
                cs.setString(3, "0");
                cs.execute();
                try (java.sql.ResultSet rs = (java.sql.ResultSet) cs.getObject(1)) {
                    while (rs.next()) {
                        if (rs.getInt("ARERE_NUM_REC") == 1) {
                            System.out.println(String.format("FN RESULT: ARERE=%d | NUM=%d | F_MAX=%s | AREES_CODIGO=%s | PORCENTAJE=%s | TOT_RESP=%s | CUMP=%s | VENC=%s",
                                    rs.getLong("ARERE_CODIGO"), rs.getInt("ARERE_NUM_REC"),
                                    rs.getString("ARERE_FECHA_MAXIMA"), rs.getString("AREES_CODIGO"),
                                    rs.getString("PORCENTAJE"), rs.getString("TOTAL_RESPONSABLES"),
                                    rs.getString("RESPONSABLES_CUMPLIDOS"), rs.getString("RESPONSABLES_VENCIDOS")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Migración V12 ya ejecutada y validada en BD")
    @Test
    public void testExecuteMigrationV12() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== EXECUTING MIGRATION V12 ===");
            java.nio.file.Path sqlPath = java.nio.file.Paths.get("src/main/resources/db/V12__PriorizarEstadoVerificableJustificaEnReportes.sql");
            String content = java.nio.file.Files.readString(sqlPath, java.nio.charset.StandardCharsets.UTF_8);

            System.out.println("=== COLUMNS OF AI_REC_SOLICITUDES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLS WHERE OWNER = 'ROOTSISREC' AND TABLE_NAME = 'AI_REC_SOLICITUDES' ORDER BY COLUMN_ID")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("  COL: %s (%s)", rs.getString("COLUMN_NAME"), rs.getString("DATA_TYPE")));
                    }
                }
            }

            // Split by lines that contain only /
            String[] blocks = content.split("(?m)^\\s*/\\s*$");
            System.out.println("Total blocks detected: " + blocks.length);

            int executed = 0;
            for (int i = 0; i < blocks.length; i++) {
                String block = blocks[i].trim();
                if (block.toUpperCase().contains("CREATE OR REPLACE")) {
                    executed++;
                    // Extract procedure name
                    String firstLine = "";
                    for (String line : block.split("\n")) {
                        if (line.toUpperCase().contains("CREATE OR REPLACE")) {
                            firstLine = line.trim();
                            break;
                        }
                    }
                    System.out.println("[" + executed + "] Executing: " + firstLine);
                    try (java.sql.Statement st = conn.createStatement()) {
                        st.execute(block);
                        System.out.println("    -> OK");
                    } catch (Exception ex) {
                        System.err.println("    -> ERROR executing block: " + ex.getMessage());
                        // Don't throw immediately, let other procedures finish so we see all statuses
                    }
                }
            }
            System.out.println("Total procedures executed: " + executed);

            System.out.println("\n=== CHECKING COMPILATION STATUS OF 15 PROCEDURES ===");
            String[] procedures = {
                "REPORTEDEPARTAMENTOFECHASVENCIDASDIRECTOR",
                "REPORTERECOMENDACIONESPORDIRECTORDEPARTAMENTAL",
                "OBTENERREPORTES1DPTO",
                "REPORTEPORDEPARTAMENTOESTADOSEGUIMIENTO",
                "REPORTERECOMENDACIONESVENCIDASOPERATIVO",
                "REPORTERECOMENDACIONESPROGRESO",
                "REPORTESEGUIMIENTORESPONSABLEOPERATIVO",
                "REPORTERECOMENDACIONESVENCIDASRESPONSABLE",
                "REPORTERECOMENDACIONESESTADOFECHACUMPLIMIENTORESPONSABLE",
                "REPORTEGENERALAIJ",
                "REPORTE7AUDITORES",
                "REPORTEPRORROGASAIJ",
                "REPORTEFECHACORTERECOMENDACIONES",
                "REPORTE2PORECOMENDACIONESAIJ",
                "REPORTERECOMENDACIONESVENCIDASAIJ"
            };

            for (String proc : procedures) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT OBJECT_NAME, STATUS FROM ALL_OBJECTS WHERE OWNER = 'ROOTSISREC' AND OBJECT_NAME = ?")) {
                    ps.setString(1, proc);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println(String.format("PROCEDURE: %-58s | STATUS: %s", rs.getString("OBJECT_NAME"), rs.getString("STATUS")));
                        } else {
                            System.out.println(String.format("PROCEDURE: %-58s | STATUS: NOT FOUND!", proc));
                        }
                    }
                }
            }

            System.out.println("\n=== CHECKING FOR ANY COMPILATION ERRORS ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT NAME, TYPE, LINE, POSITION, TEXT FROM ALL_ERRORS WHERE OWNER = 'ROOTSISREC' AND NAME IN (" +
                    "  'REPORTEDEPARTAMENTOFECHASVENCIDASDIRECTOR','REPORTERECOMENDACIONESPORDIRECTORDEPARTAMENTAL','OBTENERREPORTES1DPTO'," +
                    "  'REPORTEPORDEPARTAMENTOESTADOSEGUIMIENTO','REPORTERECOMENDACIONESVENCIDASOPERATIVO','REPORTERECOMENDACIONESPROGRESO'," +
                    "  'REPORTESEGUIMIENTORESPONSABLEOPERATIVO','REPORTERECOMENDACIONESVENCIDASRESPONSABLE','REPORTERECOMENDACIONESESTADOFECHACUMPLIMIENTORESPONSABLE'," +
                    "  'REPORTEGENERALAIJ','REPORTE7AUDITORES','REPORTEPRORROGASAIJ','REPORTEFECHACORTERECOMENDACIONES'," +
                    "  'REPORTE2PORECOMENDACIONESAIJ','REPORTERECOMENDACIONESVENCIDASAIJ')")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    boolean hasErrors = false;
                    while (rs.next()) {
                        hasErrors = true;
                        System.err.println(String.format("ERROR IN %s (line %d, col %d): %s",
                                rs.getString("NAME"), rs.getInt("LINE"), rs.getInt("POSITION"), rs.getString("TEXT")));
                    }
                    if (!hasErrors) {
                        System.out.println("SUCCESS: 0 compilation errors across all 13 procedures!");
                    } else {
                        org.junit.jupiter.api.Assertions.fail("There are PL/SQL compilation errors in procedures!");
                    }
                }
            }

            System.out.println("\n=== TESTING SAMPLE PROCEDURE EXECUTION ===");
            // Test 1: reportegeneralaij (7 parameters)
            try (java.sql.CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reportegeneralaij(?,?,?,?,?,?,?)}")) {
                cs.registerOutParameter(1, java.sql.Types.CLOB);
                cs.setString(2, "01/01/2020 00:00:00");
                cs.setString(3, "31/12/2026 23:59:59");
                cs.setInt(4, 0); // todos organismos
                cs.setInt(5, 0); // todos estados
                cs.setString(6, "0"); // todos auditores
                cs.setString(7, "0"); // todos departamentos
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String jsonSample = clob != null ? clob.getSubString(1, Math.min(200, (int) clob.length())) : "NULL";
                System.out.println("reportegeneralaij OK! Sample output: " + jsonSample + "...");
            }

            // Test 2: reportePorDepartamentoEstadoSeguimiento (6 parameters as called by Spring Boot)
            try (java.sql.CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reportePorDepartamentoEstadoSeguimiento(?,?,?,?,?,?)}")) {
                cs.registerOutParameter(1, java.sql.Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2026");
                cs.setString(4, "0"); // dpto
                cs.setInt(5, 0); // estado_seguimiento
                cs.setString(6, "TODOS"); // auditor_responsable
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String jsonSample = clob != null ? clob.getSubString(1, Math.min(200, (int) clob.length())) : "NULL";
                System.out.println("reportePorDepartamentoEstadoSeguimiento OK! Sample output: " + jsonSample + "...");
            }

            // Test 3: reporteDepartamentoFechasVencidasDirector
            try (java.sql.CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteDepartamentoFechasVencidasDirector(?,?,?,?,?,?,?)}")) {
                cs.registerOutParameter(1, java.sql.Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2026");
                cs.setInt(4, 0);
                cs.setInt(5, 0);
                cs.setString(6, "TODOS");
                cs.setString(7, "FINANCIERO");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String jsonSample = clob != null ? clob.getSubString(1, Math.min(200, (int) clob.length())) : "NULL";
                System.out.println("reporteDepartamentoFechasVencidasDirector OK! Sample output: " + jsonSample + "...");
            }

            // Test 4: reporteFechaCorteRecomendaciones (5 parameters)
            try (java.sql.CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteFechaCorteRecomendaciones(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, java.sql.Types.CLOB);
                cs.setString(2, "2026-12-31");
                cs.setString(3, "2026-12-31");
                cs.setString(4, "0");
                cs.setInt(5, 0);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String jsonSample = clob != null ? clob.getSubString(1, Math.min(200, (int) clob.length())) : "NULL";
                System.out.println("reporteFechaCorteRecomendaciones OK! Sample output: " + jsonSample + "...");
            }

            System.out.println("\n=== VERIFYING HIERARCHICAL RANKING ON MULTI-VERIFIABLE DETALLES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT AREDR_CODIGO, COUNT(*) as cnt, " +
                    "       SUM(CASE WHEN AREEV_CODIGO = 2 THEN 1 ELSE 0 END) as just_cnt, " +
                    "       SUM(CASE WHEN AREEV_CODIGO = 1 THEN 1 ELSE 0 END) as pend_cnt " +
                    "FROM ROOTSISREC.AI_REC_VERIFICABLES " +
                    "WHERE AREVE_ESTADO = '1' " +
                    "GROUP BY AREDR_CODIGO " +
                    "HAVING COUNT(*) > 1 AND SUM(CASE WHEN AREEV_CODIGO = 2 THEN 1 ELSE 0 END) > 0 " +
                    "FETCH FIRST 5 ROWS ONLY")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long aredr = rs.getLong("AREDR_CODIGO");
                        int cnt = rs.getInt("cnt");
                        int justCnt = rs.getInt("just_cnt");
                        int pendCnt = rs.getInt("pend_cnt");
                        System.out.println(String.format("AREDR=%d: total_verif=%d (JUSTIFICA=%d, PENDIENTE=%d)", aredr, cnt, justCnt, pendCnt));

                        // Test how the ranking resolves this AREDR
                        String rankSql = "WITH ultimos_verificables AS (" +
                                "  SELECT v.*, ROW_NUMBER() OVER (" +
                                "    PARTITION BY v.AREDR_CODIGO " +
                                "    ORDER BY " +
                                "      CASE " +
                                "        WHEN v.AREEV_CODIGO = 2 THEN 1 " +
                                "        WHEN v.AREEV_CODIGO = 1 THEN 2 " +
                                "        WHEN v.AREEV_CODIGO = 3 THEN 3 " +
                                "        ELSE 4 " +
                                "      END ASC, " +
                                "      NVL(v.AREVE_FECHA_CARGA, v.AREVE_FECHA) DESC NULLS LAST, " +
                                "      v.AREVE_CODIGO DESC " +
                                "  ) AS rn " +
                                "  FROM ROOTSISREC.AI_REC_VERIFICABLES v " +
                                "  WHERE v.AREVE_ESTADO = '1' AND v.AREDR_CODIGO = ? " +
                                ") SELECT AREVE_CODIGO, AREEV_CODIGO, AREVE_FECHA_CARGA FROM ultimos_verificables WHERE rn = 1";
                        try (java.sql.PreparedStatement psRank = conn.prepareStatement(rankSql)) {
                            psRank.setLong(1, aredr);
                            try (java.sql.ResultSet rsRank = psRank.executeQuery()) {
                                if (rsRank.next()) {
                                    System.out.println(String.format("  -> WINNER: AREVE_CODIGO=%d | AREEV_CODIGO=%d (Expected 2=JUSTIFICA)",
                                            rsRank.getLong("AREVE_CODIGO"), rsRank.getInt("AREEV_CODIGO")));
                                    org.junit.jupiter.api.Assertions.assertEquals(2, rsRank.getInt("AREEV_CODIGO"),
                                            "AREEV_CODIGO must be 2 (JUSTIFICA) when at least one verifiable justifies!");
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("\n=== VERIFYING THAT ZERO VERIFIABLES PRODUCES 'VERIFICABLE NO CREADO' ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DR.AREDR_CODIGO " +
                    "FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR " +
                    "WHERE DR.AREDR_ESTADO = '1' " +
                    "  AND NOT EXISTS (SELECT 1 FROM ROOTSISREC.AI_REC_VERIFICABLES V WHERE V.AREDR_CODIGO = DR.AREDR_CODIGO AND V.AREVE_ESTADO = '1') " +
                    "FETCH FIRST 1 ROWS ONLY")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long aredrNoVerif = rs.getLong("AREDR_CODIGO");
                        System.out.println("Found AREDR with 0 verifiables: " + aredrNoVerif);
                        String testSql = "WITH ultimos_verificables AS (" +
                                "  SELECT v.*, ROW_NUMBER() OVER (" +
                                "    PARTITION BY v.AREDR_CODIGO " +
                                "    ORDER BY " +
                                "      CASE " +
                                "        WHEN v.AREEV_CODIGO = 2 THEN 1 " +
                                "        WHEN v.AREEV_CODIGO = 1 THEN 2 " +
                                "        WHEN v.AREEV_CODIGO = 3 THEN 3 " +
                                "        ELSE 4 " +
                                "      END ASC, " +
                                "      NVL(v.AREVE_FECHA_CARGA, v.AREVE_FECHA) DESC NULLS LAST, " +
                                "      v.AREVE_CODIGO DESC " +
                                "  ) AS rn " +
                                "  FROM ROOTSISREC.AI_REC_VERIFICABLES v " +
                                "  WHERE v.AREVE_ESTADO = '1' " +
                                ") SELECT DR.AREDR_CODIGO, COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') AS ESTADO_VERIF " +
                                "FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR " +
                                "LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1 " +
                                "LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = UV.AREEV_CODIGO " +
                                "WHERE DR.AREDR_CODIGO = ?";
                        try (java.sql.PreparedStatement psZero = conn.prepareStatement(testSql)) {
                            psZero.setLong(1, aredrNoVerif);
                            try (java.sql.ResultSet rsZero = psZero.executeQuery()) {
                                if (rsZero.next()) {
                                    System.out.println("  -> Result ESTADO_VERIF: " + rsZero.getString("ESTADO_VERIF"));
                                    org.junit.jupiter.api.Assertions.assertEquals("VERIFICABLE NO CREADO", rsZero.getString("ESTADO_VERIF"));
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("\n=== ALL TESTS COMPLETED SUCCESSFULLY ===");
        } catch (Exception e) {
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail("Migration failed: " + e.getMessage());
        }
    }

    @Test
    public void testInspectSixReports() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String[] procs = {
                "REPORTERECOMENDACIONESVENCIDASOPERATIVO",
                "REPORTE2PORECOMENDACIONESAIJ",
                "REPORTERECOMENDACIONESVENCIDASAIJ",
                "REPORTEFECHACORTERECOMENDACIONES",
                "REPORTEPRORROGASAIJ",
                "REPORTERECOMENDACIONESPROGRESO"
            };

            for (String proc : procs) {
                System.out.println("\n========================================================");
                System.out.println("PROCEDURE: " + proc);
                System.out.println("========================================================");
                
                // Check status and arguments
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT OBJECT_NAME, STATUS FROM ALL_OBJECTS WHERE OWNER = 'ROOTSISREC' AND OBJECT_TYPE = 'PROCEDURE' AND UPPER(OBJECT_NAME) = ?")) {
                    ps.setString(1, proc.toUpperCase());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("STATUS: " + rs.getString("STATUS"));
                        } else {
                            System.out.println("STATUS: NOT FOUND in ALL_OBJECTS!");
                        }
                    }
                }

                // Check arguments
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT ARGUMENT_NAME, POSITION, DATA_TYPE, IN_OUT FROM ALL_ARGUMENTS WHERE OWNER = 'ROOTSISREC' AND UPPER(OBJECT_NAME) = ? ORDER BY POSITION")) {
                    ps.setString(1, proc.toUpperCase());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            System.out.println(String.format("  ARG #%d: %s (%s) [%s]",
                                    rs.getInt("POSITION"), rs.getString("ARGUMENT_NAME"),
                                    rs.getString("DATA_TYPE"), rs.getString("IN_OUT")));
                        }
                    }
                }

                // Get all lines of source
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT LINE, TEXT FROM ALL_SOURCE WHERE OWNER = 'ROOTSISREC' AND UPPER(NAME) = ? ORDER BY LINE")) {
                    ps.setString(1, proc.toUpperCase());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            System.out.print(String.format("%3d: %s", rs.getInt("LINE"), rs.getString("TEXT")));
                        }
                    }
                }
            }

            System.out.println("\n=== TESTING CURRENT EXECUTION OF ALL 6 PROCEDURES ===");
            // 1. REPORTERECOMENDACIONESVENCIDASOPERATIVO
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASOPERATIVO(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2026");
                cs.setInt(4, 0);
                cs.setString(5, "0");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String s = clob != null ? clob.getSubString(1, Math.min(300, (int) clob.length())) : "NULL";
                System.out.println("1. REPORTERECOMENDACIONESVENCIDASOPERATIVO -> len=" + (clob != null ? clob.length() : 0) + " | " + s);
            } catch (Exception e) {
                System.err.println("1. REPORTERECOMENDACIONESVENCIDASOPERATIVO ERROR: " + e.getMessage());
            }

            // 2. reporte2PoRecomendacionesAIJ
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporte2PoRecomendacionesAIJ(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2026");
                cs.setInt(4, 0);
                cs.setString(5, "0");
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String s = clob != null ? clob.getSubString(1, Math.min(300, (int) clob.length())) : "NULL";
                System.out.println("2. reporte2PoRecomendacionesAIJ -> len=" + (clob != null ? clob.length() : 0) + " | " + s);
            } catch (Exception e) {
                System.err.println("2. reporte2PoRecomendacionesAIJ ERROR: " + e.getMessage());
            }

            // 3. REPORTERECOMENDACIONESVENCIDASAIJ
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASAIJ(?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020 00:00:00");
                cs.setString(3, "31/12/2026 23:59:59");
                cs.setInt(4, 0);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String s = clob != null ? clob.getSubString(1, Math.min(300, (int) clob.length())) : "NULL";
                System.out.println("3. REPORTERECOMENDACIONESVENCIDASAIJ -> len=" + (clob != null ? clob.length() : 0) + " | " + s);
            } catch (Exception e) {
                System.err.println("3. REPORTERECOMENDACIONESVENCIDASAIJ ERROR: " + e.getMessage());
            }

            // 4. reporteFechaCorteRecomendaciones
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteFechaCorteRecomendaciones(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "2026-12-31");
                cs.setString(3, "2026-12-31");
                cs.setString(4, "0");
                cs.setInt(5, 0);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String s = clob != null ? clob.getSubString(1, Math.min(300, (int) clob.length())) : "NULL";
                System.out.println("4. reporteFechaCorteRecomendaciones -> len=" + (clob != null ? clob.length() : 0) + " | " + s);
            } catch (Exception e) {
                System.err.println("4. reporteFechaCorteRecomendaciones ERROR: " + e.getMessage());
            }

            // 5. reporteprorrogasaij
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteprorrogasaij(?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String s = clob != null ? clob.getSubString(1, Math.min(300, (int) clob.length())) : "NULL";
                System.out.println("5. reporteprorrogasaij -> len=" + (clob != null ? clob.length() : 0) + " | " + s);
            } catch (Exception e) {
                System.err.println("5. reporteprorrogasaij ERROR: " + e.getMessage());
            }

            // 6. reporteRecomendacionesProgreso
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteRecomendacionesProgreso(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2026");
                cs.setString(4, "0");
                cs.setInt(5, 0);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String s = clob != null ? clob.getSubString(1, Math.min(300, (int) clob.length())) : "NULL";
                System.out.println("6. reporteRecomendacionesProgreso -> len=" + (clob != null ? clob.length() : 0) + " | " + s);
            } catch (Exception e) {
                System.err.println("6. reporteRecomendacionesProgreso ERROR: " + e.getMessage());
            }

            System.out.println("\n=== SOLICITUDES COUNT BY ESTADO_APROB ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT ARESO_ESTADO_APROB, COUNT(*) as cnt FROM ROOTSISREC.AI_REC_SOLICITUDES GROUP BY ARESO_ESTADO_APROB")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("  APROB=%s: %d", rs.getString("ARESO_ESTADO_APROB"), rs.getInt("cnt")));
                    }
                }
            }

            System.out.println("\n=== TESTING PRORROGAS JSON DESERIALIZATION ===");
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteprorrogasaij(?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                if (clob != null) {
                    String json = clob.getSubString(1, (int) clob.length());
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasDTO dto =
                        mapper.readValue(json, ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasDTO.class);
                    System.out.println("Prorrogas DTO datos size: " + (dto != null && dto.getDatos() != null ? dto.getDatos().size() : 0));
                    if (dto != null && dto.getDatos() != null && !dto.getDatos().isEmpty()) {
                        System.out.println("First item: " + dto.getDatos().get(0));
                    }
                }
            } catch (Exception e) {
                System.err.println("Prorrogas DTO error: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico de base de datos")
    @Test
    public void testDiagnoseRecomendacionesVencidasAIJ() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. COMPILING PROCEDURES 13 AND 15 FROM V12 INTO DATABASE ===");
            String v12Content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/db/V12__PriorizarEstadoVerificableJustificaEnReportes.sql")), java.nio.charset.StandardCharsets.UTF_8);

            // Extract Procedure 13
            String p13Marker = "CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteFechaCorteRecomendaciones";
            int p13Start = v12Content.indexOf(p13Marker);
            int p13End = v12Content.indexOf("END reporteFechaCorteRecomendaciones;", p13Start) + "END reporteFechaCorteRecomendaciones;".length();
            String p13Sql = v12Content.substring(p13Start, p13End);

            // Extract Procedure 15
            String p15Marker = "CREATE OR REPLACE PROCEDURE ROOTSISREC.REPORTERECOMENDACIONESVENCIDASAIJ";
            int p15Start = v12Content.indexOf(p15Marker);
            int p15End = v12Content.indexOf("END REPORTERECOMENDACIONESVENCIDASAIJ;", p15Start) + "END REPORTERECOMENDACIONESVENCIDASAIJ;".length();
            String p15Sql = v12Content.substring(p15Start, p15End);

            try (java.sql.Statement stmt = conn.createStatement()) {
                System.out.println("Compiling reporteFechaCorteRecomendaciones...");
                stmt.execute(p13Sql);
                System.out.println("reporteFechaCorteRecomendaciones compiled successfully!");

                System.out.println("Compiling REPORTERECOMENDACIONESVENCIDASAIJ...");
                stmt.execute(p15Sql);
                System.out.println("REPORTERECOMENDACIONESVENCIDASAIJ compiled successfully!");
            }

            // Verify status in ALL_OBJECTS
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT OBJECT_NAME, STATUS FROM ALL_OBJECTS WHERE OWNER = 'ROOTSISREC' AND OBJECT_NAME IN ('REPORTEFECHACORTERECOMENDACIONES', 'REPORTERECOMENDACIONESVENCIDASAIJ')")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("OBJECT: " + rs.getString("OBJECT_NAME") + " | STATUS: " + rs.getString("STATUS"));
                    }
                }
            }

            System.out.println("\n=== 2. TESTING REPORTERECOMENDACIONESVENCIDASAIJ ===");
            // Test with frontend defaults (2026-01-01 to 2026-09-24)
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASAIJ(?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "2026-01-01");
                cs.setString(3, "2026-09-24");
                cs.setInt(4, 0);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "";
                System.out.println("Vencidas AIJ (2026 ISO) -> len=" + json.length());
                if (json.startsWith("[")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO> list =
                        mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO>>() {});
                    System.out.println("Total recommendations returned (2026): " + (list != null ? list.size() : 0));
                    int vencidas = 0, enProgreso = 0;
                    if (list != null) {
                        for (ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO r : list) {
                            if (r.getPlazo() < 0) vencidas++;
                            else enProgreso++;
                        }
                    }
                    System.out.println("  -> Vencidas (plazo < 0): " + vencidas);
                    System.out.println("  -> En Progreso (plazo >= 0): " + enProgreso);
                } else {
                    System.out.println("Non-array response: " + json);
                }
            }

            // Test with wider range (01/01/2020 to 31/12/2026)
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.REPORTERECOMENDACIONESVENCIDASAIJ(?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "01/01/2020");
                cs.setString(3, "31/12/2026");
                cs.setInt(4, 0);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "";
                System.out.println("\nVencidas AIJ (2020-2026 DD/MM/YYYY) -> len=" + json.length());
                if (json.startsWith("[")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO> list =
                        mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO>>() {});
                    System.out.println("Total recommendations returned (2020-2026): " + (list != null ? list.size() : 0));
                    int vencidas = 0, enProgreso = 0;
                    if (list != null) {
                        for (ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO r : list) {
                            if (r.getPlazo() < 0) vencidas++;
                            else enProgreso++;
                        }
                    }
                    System.out.println("  -> Vencidas (plazo < 0): " + vencidas);
                    System.out.println("  -> En Progreso (plazo >= 0): " + enProgreso);
                }
            }

            System.out.println("\n=== 3. TESTING reporteFechaCorteRecomendaciones ===");
            try (CallableStatement cs = conn.prepareCall("{call ROOTSISREC.reporteFechaCorteRecomendaciones(?,?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.CLOB);
                cs.setString(2, "2026-12-31");
                cs.setString(3, "2026-12-31");
                cs.setString(4, "0");
                cs.setInt(5, 0);
                cs.execute();
                java.sql.Clob clob = cs.getClob(1);
                String json = clob != null ? clob.getSubString(1, (int) clob.length()) : "";
                System.out.println("Fecha Corte (2026-12-31, dpto=0, org=0) -> len=" + json.length());
                if (json.startsWith("[")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<java.util.Map<String, Object>> list =
                        mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {});
                    System.out.println("Total records returned: " + (list != null ? list.size() : 0));
                    if (list != null) {
                        for (java.util.Map<String, Object> r : list) {
                            String rec = (String) r.get("recomendacion");
                            if (rec != null && rec.contains("3 y 111 meses")) {
                                System.out.println("\n=== FOUND RECOMMENDATION '3 y 111 meses' ===");
                                System.out.println("idInforme: " + r.get("idInforme"));
                                System.out.println("arere_codigo: " + r.get("arere_codigo"));
                                System.out.println("numeroRecomendacion: " + r.get("numeroRecomendacion"));
                                System.out.println("estadoRecomendacion: " + r.get("estadoRecomendacion"));
                                System.out.println("progresoRecomendacion: " + r.get("progresoRecomendacion"));
                                System.out.println("listaResponsables:\n" + r.get("listaResponsables"));
                                System.out.println("listaDepartamentos:\n" + r.get("listaDepartamentos"));
                            }
                        }
                    }

                    System.out.println("\n=== DETALLE DE RESPONSABLES PARA ARERE=469 ===");
                    String q469 = "SELECT DR.AREDR_CODIGO, RES.ARERS_NOMBRE, RES.ARERS_APELLIDO, RES.ARERS_DEPARTAMENTO, " +
                            "VR.AREEV_CODIGO, VR.AREVE_CODIGO, " +
                            "LENGTH(VALL.observacion_auditoria_concat) as len_obs_all, " +
                            "LENGTH(VALL.descripcion_verificable_concat) as len_desc_all " +
                            "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                            "JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
                            "LEFT JOIN ( " +
                            "  SELECT v.*, ROW_NUMBER() OVER (PARTITION BY v.aredr_codigo ORDER BY CASE WHEN v.AREEV_CODIGO=2 THEN 1 WHEN v.AREEV_CODIGO=1 THEN 2 WHEN v.AREEV_CODIGO=3 THEN 3 ELSE 4 END ASC, NVL(v.areve_fecha_carga, v.areve_fecha) DESC NULLS LAST, v.areve_codigo DESC) as rn " +
                            "  FROM AI_REC_VERIFICABLES v WHERE v.areve_estado = 1 " +
                            ") VR ON DR.AREDR_CODIGO = VR.AREDR_CODIGO AND VR.rn = 1 " +
                            "LEFT JOIN ( " +
                            "  SELECT v.aredr_codigo, " +
                            "  SUBSTRB(LISTAGG(v.areve_descripcion, ' ;;; ' ON OVERFLOW TRUNCATE) WITHIN GROUP (ORDER BY v.areve_codigo), 1, 3500) as descripcion_verificable_concat, " +
                            "  SUBSTRB(LISTAGG(v.areve_observacion, ' ;;; ' ON OVERFLOW TRUNCATE) WITHIN GROUP (ORDER BY v.areve_codigo), 1, 3500) as observacion_auditoria_concat " +
                            "  FROM AI_REC_VERIFICABLES v WHERE v.areve_estado = 1 GROUP BY v.aredr_codigo " +
                            ") VALL ON DR.AREDR_CODIGO = VALL.AREDR_CODIGO " +
                            "WHERE DR.ARERE_CODIGO = 469 AND DR.AREDR_ESTADO = 1";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(q469);
                         java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            System.out.println(String.format("  Resp: %s %s [%s] | Ver: %s (Estado: %s) | lenObsAll: %s | lenDescAll: %s",
                                    rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO"), rs.getString("ARERS_DEPARTAMENTO"),
                                    rs.getString("AREVE_CODIGO"), rs.getString("AREEV_CODIGO"),
                                    rs.getString("len_obs_all"), rs.getString("len_desc_all")));
                        }
                    }

                    System.out.println("\n=== TESTING XMLAGG ON ARERE=469 ===");
                    String qXml = "WITH " +
                            "verificables_corte AS (" +
                            "  SELECT v.*, ROW_NUMBER() OVER (PARTITION BY v.aredr_codigo ORDER BY CASE WHEN v.AREEV_CODIGO=2 THEN 1 WHEN v.AREEV_CODIGO=1 THEN 2 WHEN v.AREEV_CODIGO=3 THEN 3 ELSE 4 END ASC, NVL(v.areve_fecha_carga, v.areve_fecha) DESC NULLS LAST, v.areve_codigo DESC) as rn " +
                            "  FROM AI_REC_VERIFICABLES v WHERE v.areve_estado = 1" +
                            "), " +
                            "verificables_corte_all AS (" +
                            "  SELECT v.aredr_codigo, " +
                            "  SUBSTRB(LISTAGG(v.areve_descripcion, ' ;;; ' ON OVERFLOW TRUNCATE) WITHIN GROUP (ORDER BY v.areve_codigo), 1, 3500) as descripcion_verificable_concat, " +
                            "  SUBSTRB(LISTAGG(v.areve_observacion, ' ;;; ' ON OVERFLOW TRUNCATE) WITHIN GROUP (ORDER BY v.areve_codigo), 1, 3500) as observacion_auditoria_concat " +
                            "  FROM AI_REC_VERIFICABLES v WHERE v.areve_estado = 1 GROUP BY v.aredr_codigo " +
                            "), " +
                            "detalle_estado_global AS (" +
                            "  SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, RES.ARERS_NOMBRE, RES.ARERS_APELLIDO, RES.ARERS_DEPARTAMENTO, " +
                            "  VR.AREEV_CODIGO, VR.AREVE_FECHA_VALIDACION, " +
                            "  CASE WHEN VR.AREEV_CODIGO = 2 THEN 'JUSTIFICA' WHEN VR.AREEV_CODIGO = 1 THEN 'NO JUSTIFICA' ELSE 'PENDIENTE' END as estado_verificable, " +
                            "  VR.AREVE_CODIGO as verificable_codigo, " +
                            "  VR.AREVE_DESCRIPCION as descripcion_verificable, VR.AREVE_OBSERVACION as observacion_auditoria, " +
                            "  VALL.descripcion_verificable_concat as descripcion_verificable_all, VALL.observacion_auditoria_concat as observacion_auditoria_all " +
                            "  FROM AI_REC_DETALLE_RECOMENDACION DR " +
                            "  JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
                            "  JOIN AI_REC_RECOMENDACIONES REC ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                            "  JOIN AI_REC_ESTADO_SEGUIMIENTO ES ON REC.AREES_CODIGO = ES.AREES_CODIGO " +
                            "  LEFT JOIN verificables_corte VR ON DR.AREDR_CODIGO = VR.AREDR_CODIGO AND VR.rn = 1 " +
                            "  LEFT JOIN verificables_corte_all VALL ON DR.AREDR_CODIGO = VALL.AREDR_CODIGO " +
                            "  WHERE DR.ARERE_CODIGO = 469 AND DR.AREDR_ESTADO = 1 " +
                            ") " +
                            "SELECT " +
                            "  RTRIM( " +
                            "    XMLCAST( " +
                            "      XMLAGG( " +
                            "        XMLELEMENT(e, " +
                            "          ARERS_NOMBRE || ' ' || ARERS_APELLIDO || ' [' || ARERS_DEPARTAMENTO || '] (' || estado_verificable || ') [V:' || NVL(verificable_codigo, 0) || ']' || " +
                            "          CASE WHEN observacion_auditoria_all IS NOT NULL THEN ' - ObsAll: ' || observacion_auditoria_all ELSE '' END || " +
                            "          CASE WHEN descripcion_verificable_all IS NOT NULL THEN ' - DescAll: ' || descripcion_verificable_all ELSE '' END || " +
                            "          CASE WHEN observacion_auditoria IS NOT NULL THEN ' - Obs: ' || observacion_auditoria ELSE '' END || " +
                            "          CASE WHEN descripcion_verificable IS NOT NULL THEN ' - Desc: ' || descripcion_verificable ELSE '' END || ' | ' " +
                            "        ) ORDER BY ARERS_APELLIDO, ARERS_NOMBRE " +
                            "      ) AS CLOB " +
                            "    ), ' | ' " +
                            "  ) as xml_lista " +
                            "FROM detalle_estado_global GROUP BY ARERE_CODIGO";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(qXml);
                         java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            java.sql.Clob c = rs.getClob("xml_lista");
                            String str = c != null ? c.getSubString(1, (int) c.length()) : "";
                            System.out.println("XMLAGG RESULT (length=" + str.length() + "):");
                            System.out.println(str);
                        }
                    }
                } else {
                    System.out.println("Non-array response: " + json);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Disabled
    @Test
    public void testDetalleQuery() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== BUSCANDO VERIFICABLE DZOP-DIR-454-2026 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT V.AREVE_CODIGO, V.AREDR_CODIGO, V.AREVE_DESCRIPCION, DR.ARERE_CODIGO, DR.ARERS_CODIGO " +
                    "FROM AI_REC_VERIFICABLES V " +
                    "JOIN AI_REC_DETALLE_RECOMENDACION DR ON V.AREDR_CODIGO = DR.AREDR_CODIGO " +
                    "WHERE V.AREVE_DESCRIPCION LIKE '%DZOP-DIR-454-2026%'");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long arereCodigo = rs.getLong("ARERE_CODIGO");
                    System.out.println(String.format("AREVE: %d | AREDR: %d | ARERE: %d | ARERS: %d | DESC: %s",
                            rs.getLong("AREVE_CODIGO"), rs.getLong("AREDR_CODIGO"),
                            arereCodigo, rs.getLong("ARERS_CODIGO"),
                            rs.getString("AREVE_DESCRIPCION")));

                    // Probar la consulta EXACTA que hace InformeRepositorio.findByRecomendacion:
                    String sqlInf = "SELECT * FROM AI_INF_INFORMES I, AI_REC_HALLAZGOS H, AI_REC_CONCLUSIONES C, AI_REC_RECOMENDACIONES R " +
                                    "WHERE I.AININ_CODIGO = H.AININ_CODIGO AND H.AREHA_CODIGO=C.AREHA_CODIGO " +
                                    "AND C.ARECO_CODIGO=R.ARECO_CODIGO AND R.ARERE_CODIGO = ?";
                    try (java.sql.PreparedStatement psInf = conn.prepareStatement(sqlInf)) {
                        psInf.setLong(1, arereCodigo);
                        try (java.sql.ResultSet rsInf = psInf.executeQuery()) {
                            java.sql.ResultSetMetaData md = rsInf.getMetaData();
                            int cols = md.getColumnCount();
                            System.out.println("-> Columnas retornadas por SELECT * de findByRecomendacion: " + cols);
                            boolean hasDuplicateCols = false;
                            java.util.Set<String> colNames = new java.util.HashSet<>();
                            for (int i = 1; i <= cols; i++) {
                                String colName = md.getColumnName(i);
                                if (!colNames.add(colName)) {
                                    System.out.println("   [DUPLICADA]: " + colName);
                                    hasDuplicateCols = true;
                                }
                            }
                            if (rsInf.next()) {
                                System.out.println("-> Fila leída con éxito");
                            }
                        }
                    }

                    // Probar ConclusionRepositorio.findByRecomendacion:
                    String sqlCon = "SELECT * FROM AI_REC_CONCLUSIONES C, AI_REC_RECOMENDACIONES R " +
                                    "WHERE C.ARECO_CODIGO = R.ARECO_CODIGO AND R.ARERE_CODIGO = ?";
                    try (java.sql.PreparedStatement psCon = conn.prepareStatement(sqlCon)) {
                        psCon.setLong(1, arereCodigo);
                        try (java.sql.ResultSet rsCon = psCon.executeQuery()) {
                            if (rsCon.next()) {
                                System.out.println("-> Conclusion encontrada: " + rsCon.getLong("ARECO_CODIGO"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico")
    @Test
    public void testInspectPlanAccion() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. CHECK TABLES ===");
            String[] tables = {"AI_REC_PLAN_ACCION", "AI_REC_PLAN_ACTIVIDADES", "AI_REC_EVIDENCIAS_REQUERIDAS", "AI_REC_EVIDENCIA_COTEJO"};
            for (String t : tables) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = 'ROOTSISREC' AND TABLE_NAME = ?")) {
                    ps.setString(1, t);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("Table " + t + " exists: " + (rs.getInt(1) > 0));
                        }
                    }
                }
            }

            System.out.println("\n=== 2. CHECK SEQUENCES ===");
            String[] seqs = {"AI_REC_PLAN_ACCION_SEQ", "AI_REC_PLAN_ACT_SEQ", "AI_REC_EVIDENCIA_REQ_SEQ", "AI_REC_EVIDENCIA_COTEJO_SEQ"};
            for (String s : seqs) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = 'ROOTSISREC' AND SEQUENCE_NAME = ?")) {
                    ps.setString(1, s);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("Sequence " + s + " exists: " + (rs.getInt(1) > 0));
                        }
                    }
                }
            }

            System.out.println("\n=== 3. CHECK A RECOMMENDATION AND DETALLE FOR TEST ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, DR.ARERS_CODIGO, RES.ARERS_NOMBRE, RES.ARERS_APELLIDO " +
                    "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                    "JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
                    "WHERE DR.AREDR_ESTADO = 1 AND ROWNUM <= 3")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("AREDR: %d | ARERE: %d | ARERS: %d | RESP: %s %s",
                                rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4), rs.getString(5)));
                    }
                }
            }

            System.out.println("\n=== 3. CHECK COLUMNS OF AI_REC_PLAN_ACCION ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT COLUMN_NAME, DATA_TYPE, NULLABLE FROM ALL_TAB_COLUMNS WHERE OWNER = 'ROOTSISREC' AND TABLE_NAME = 'AI_REC_PLAN_ACCION'")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + " | " + rs.getString(2) + " | NULLABLE: " + rs.getString(3));
                    }
                }
            }

            System.out.println("\n=== 4. CHECK COLUMNS OF AI_REC_PLAN_ACTIVIDADES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT COLUMN_NAME, DATA_TYPE, NULLABLE FROM ALL_TAB_COLUMNS WHERE OWNER = 'ROOTSISREC' AND TABLE_NAME = 'AI_REC_PLAN_ACTIVIDADES'")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + " | " + rs.getString(2) + " | NULLABLE: " + rs.getString(3));
                    }
                }
            }

            System.out.println("\n=== 5. CHECK ROWS IN AI_REC_PLAN_ACCION ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM AI_REC_PLAN_ACCION")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("Total rows in AI_REC_PLAN_ACCION: " + rs.getInt(1));
                }
            }

            System.out.println("\n=== 7. TEST INSERT INTO AI_REC_PLAN_ACCION & AI_REC_PLAN_ACTIVIDADES ===");
            long testArere = 12;
            long testAredr = 28;
            Long testPlanId = null;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT AI_REC_PLAN_ACCION_SEQ.NEXTVAL FROM DUAL")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) testPlanId = rs.getLong(1);
                }
            }
            System.out.println("Generated testPlanId: " + testPlanId);

            String insertPlan = "INSERT INTO AI_REC_PLAN_ACCION (ARPA_CODIGO, ARERE_CODIGO, AREDR_CODIGO, ARPA_FECHA_MAXIMA_REF, ARPA_FECHA_ACTUALIZACION, ARPA_ESTADO) " +
                                "VALUES (?, ?, ?, SYSDATE, SYSDATE, 'A')";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertPlan)) {
                ps.setLong(1, testPlanId);
                ps.setLong(2, testArere);
                ps.setLong(3, testAredr);
                int rows = ps.executeUpdate();
                System.out.println("Plan insert rows: " + rows);
            }

            Long testActId = null;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT AI_REC_PLAN_ACT_SEQ.NEXTVAL FROM DUAL")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) testActId = rs.getLong(1);
                }
            }
            System.out.println("Generated testActId: " + testActId);

            String insertAct = "INSERT INTO AI_REC_PLAN_ACTIVIDADES (ARPAC_CODIGO, ARPA_CODIGO, ARPAC_NUM_ACTIVIDAD, ARPAC_ACTIVIDAD, ARPAC_FECHA_EJECUCION, ARPAC_ENTREGABLE_ESPERADO, ARPAC_ESTADO) " +
                               "VALUES (?, ?, 1, 'Actividad de prueba', SYSDATE, 'Entregable de prueba', 'A')";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertAct)) {
                ps.setLong(1, testActId);
                ps.setLong(2, testPlanId);
                int rows = ps.executeUpdate();
                System.out.println("Actividad insert rows: " + rows);
            }

            conn.commit();
            System.out.println("COMMITTED SUCCESSFULLY!");

            // Now check that rows exist in DB:
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM AI_REC_PLAN_ACCION WHERE ARPA_CODIGO = ?")) {
                ps.setLong(1, testPlanId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("Rows in AI_REC_PLAN_ACCION for test: " + rs.getInt(1));
                }
            }
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM AI_REC_PLAN_ACTIVIDADES WHERE ARPA_CODIGO = ?")) {
                ps.setLong(1, testPlanId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("Rows in AI_REC_PLAN_ACTIVIDADES for test: " + rs.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico para 765")
    @Test
    public void testDiagnosticoInforme765() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. BUSCAR 765 EN INFORMES Y RECOMENDACIONES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT AININ_CODIGO, AININ_MEMORANDO, AININ_TITULO, AININ_ESTADO, AININ_NOTIFICADO, AININ_FECHA " +
                    "FROM AI_INF_INFORMES " +
                    "WHERE AININ_CODIGO = 765 OR AININ_MEMORANDO LIKE '%765%'");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("INF ENCONTRADO -> CODIGO=" + rs.getLong("AININ_CODIGO") + 
                                       ", MEMO=" + rs.getString("AININ_MEMORANDO") + 
                                       ", ESTADO=" + rs.getString("AININ_ESTADO") + 
                                       ", NOTIFICADO=" + rs.getString("AININ_NOTIFICADO") + 
                                       ", FECHA=" + rs.getString("AININ_FECHA") + 
                                       ", TITULO=" + rs.getString("AININ_TITULO"));
                }
            }

            System.out.println("=== DETALLE DE RECOMENDACION 765 (INFORME 90) ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, DR.AREDR_ESTADO, " +
                    "RES.ARERS_CODIGO, RES.ARERS_NOMBRE, RES.ARERS_APELLIDO, RES.ARERS_CORREO, RES.ARERS_ESTADO, " +
                    "REC.AREES_CODIGO, REC.ARERE_ESTADO, " +
                    "(SELECT COUNT(*) FROM AI_REC_PLAN_ACCION PA WHERE PA.ARERE_CODIGO = DR.ARERE_CODIGO AND PA.AREDR_CODIGO = DR.AREDR_CODIGO AND PA.ARPA_ESTADO = 'A') AS TIENE_PLAN_ACTIVO " +
                    "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                    "JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                    "JOIN AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = DR.ARERE_CODIGO " +
                    "WHERE DR.ARERE_CODIGO = 765");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("DR=%d | RES=%d (%s %s, %s, ESTADO=%s) | REC_ESTADO=%s | AREES=%s | TIENE_PLAN_ACTIVO=%d",
                            rs.getLong("AREDR_CODIGO"),
                            rs.getLong("ARERS_CODIGO"),
                            rs.getString("ARERS_NOMBRE"),
                            rs.getString("ARERS_APELLIDO"),
                            rs.getString("ARERS_CORREO"),
                            rs.getString("ARERS_ESTADO"),
                            rs.getString("ARERE_ESTADO"),
                            rs.getObject("AREES_CODIGO"),
                            rs.getInt("TIENE_PLAN_ACTIVO")));
                }
            }

            System.out.println("=== 1.2 ULTIMOS 10 INFORMES REGISTRADOS ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM (SELECT AININ_CODIGO, AININ_MEMORANDO, AININ_TITULO, AININ_ESTADO, AININ_NOTIFICADO, TO_CHAR(AININ_FECHA, 'YYYY-MM-DD HH24:MI:SS') AS F FROM AI_INF_INFORMES ORDER BY AININ_CODIGO DESC) WHERE ROWNUM <= 10");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("ULTIMO INF -> CODIGO=" + rs.getLong("AININ_CODIGO") + 
                                       ", MEMO=" + rs.getString("AININ_MEMORANDO") + 
                                       ", FECHA=" + rs.getString("F") + 
                                       ", ESTADO=" + rs.getString("AININ_ESTADO") + 
                                       ", NOTIF=" + rs.getString("AININ_NOTIFICADO"));
                }
            }

            System.out.println("=== 2. HALLAZGOS Y CONCLUSIONES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT H.AREHA_CODIGO, H.AREHA_ESTADO, C.ARECO_CODIGO, C.ARECO_ESTADO " +
                    "FROM AI_REC_HALLAZGOS H " +
                    "LEFT JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                    "WHERE H.AININ_CODIGO = 765");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("HALL/CON -> AREHA=" + rs.getLong("AREHA_CODIGO") + 
                                       ", AREHA_ESTADO=" + rs.getString("AREHA_ESTADO") + 
                                       ", ARECO=" + rs.getLong("ARECO_CODIGO") + 
                                       ", ARECO_ESTADO=" + rs.getString("ARECO_ESTADO"));
                }
            }

            System.out.println("=== 3. RECOMENDACIONES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT R.ARERE_CODIGO, R.ARERE_ESTADO, R.AREES_CODIGO, R.ARECO_CODIGO, SUBSTR(R.ARERE_DESCRIPCION, 1, 50) AS DESC_REC " +
                    "FROM AI_REC_RECOMENDACIONES R " +
                    "JOIN AI_REC_CONCLUSIONES C ON C.ARECO_CODIGO = R.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS H ON H.AREHA_CODIGO = C.AREHA_CODIGO " +
                    "WHERE H.AININ_CODIGO = 765");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("REC -> ARERE=" + rs.getLong("ARERE_CODIGO") + 
                                       ", ARERE_ESTADO=" + rs.getString("ARERE_ESTADO") + 
                                       ", AREES_CODIGO=" + rs.getObject("AREES_CODIGO") + 
                                       ", DESC=" + rs.getString("DESC_REC"));
                }
            }

            System.out.println("=== 4. DETALLE_RECOMENDACION Y RESPONSABLES ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, DR.AREDR_ESTADO, RES.ARERS_CODIGO, RES.ARERS_NOMBRE, RES.ARERS_APELLIDO, RES.ARERS_ESTADO " +
                    "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                    "JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                    "JOIN AI_REC_RECOMENDACIONES R ON R.ARERE_CODIGO = DR.ARERE_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES C ON C.ARECO_CODIGO = R.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS H ON H.AREHA_CODIGO = C.AREHA_CODIGO " +
                    "WHERE H.AININ_CODIGO = 765");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("DR/RES -> AREDR=" + rs.getLong("AREDR_CODIGO") + 
                                       ", ARERE=" + rs.getLong("ARERE_CODIGO") + 
                                       ", AREDR_ESTADO=" + rs.getString("AREDR_ESTADO") + 
                                       ", ARERS=" + rs.getLong("ARERS_CODIGO") + 
                                       ", NOMBRE=" + rs.getString("ARERS_NOMBRE") + " " + rs.getString("ARERS_APELLIDO") + 
                                       ", RES_ESTADO=" + rs.getString("ARERS_ESTADO"));
                }
            }

            System.out.println("=== 5. PLANES DE ACCION ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT PA.ARPA_CODIGO, PA.ARERE_CODIGO, PA.AREDR_CODIGO, PA.ARPA_ESTADO " +
                    "FROM AI_REC_PLAN_ACCION PA " +
                    "JOIN AI_REC_RECOMENDACIONES R ON R.ARERE_CODIGO = PA.ARERE_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES C ON C.ARECO_CODIGO = R.ARECO_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS H ON H.AREHA_CODIGO = C.AREHA_CODIGO " +
                    "WHERE H.AININ_CODIGO = 765");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("PA -> ARPA=" + rs.getLong("ARPA_CODIGO") + 
                                       ", ARERE=" + rs.getLong("ARERE_CODIGO") + 
                                       ", AREDR=" + rs.getLong("AREDR_CODIGO") + 
                                       ", ESTADO=" + rs.getString("ARPA_ESTADO"));
                }
            }

            System.out.println("=== 6. PROBAR QUERY COMPLETA PENDIENTES POR RECOMENDACION 765 ===");
            String querySql = 
                "SELECT " +
                "INF.AININ_CODIGO AS aininCodigo, " +
                "INF.AININ_TITULO AS aininTitulo, " +
                "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                "RES.ARERS_NOMBRE AS arersNombre, " +
                "RES.ARERS_APELLIDO AS arersApellido, " +
                "RES.ARERS_CORREO AS arersCorreo, " +
                "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                "REC.ARERE_CODIGO AS arereCodigo, " +
                "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax " +
                "FROM AI_REC_RECOMENDACIONES REC " +
                "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                "INNER JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                "WHERE REC.ARERE_CODIGO = 765 " +
                "  AND DR.AREDR_ESTADO = 1 " +
                "  AND RES.ARERS_ESTADO = 1 " +
                "  AND NOT EXISTS ( " +
                "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                "        AND PA.ARPA_ESTADO = 'A' " +
                "  )";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(querySql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("FILA ENCONTRADA -> RES=" + rs.getString("arersNombre") + " " + rs.getString("arersApellido") + " (" + rs.getString("arersCorreo") + "), REC=" + rs.getLong("arereCodigo") + ", INF=" + rs.getLong("aininCodigo"));
                }
                System.out.println("TOTAL FILAS ENCONTRADAS POR RECOMENDACION 765: " + count);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEvidenciaAI0022025() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== BUSCAR INFORME AI-002-2025 Y SU REC 4 ===");
            String sql = "SELECT INF.AININ_CODIGO, INF.AININ_MEMORANDO, REC.ARERE_CODIGO, REC.ARERE_NUM_REC, REC.ARERE_EVIDENCIA_REQUERIDA, REC.ARERE_RIESGO_VERIFICADO " +
                         "FROM AI_INF_INFORMES INF " +
                         "JOIN AI_REC_HALLAZGOS H ON H.AININ_CODIGO = INF.AININ_CODIGO " +
                         "JOIN AI_REC_CONCLUSIONES C ON C.AREHA_CODIGO = H.AREHA_CODIGO " +
                         "JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = C.ARECO_CODIGO " +
                         "WHERE INF.AININ_MEMORANDO LIKE '%002-2025%'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INF: %s (COD=%d) | REC: %d (NUM=%d) | EVIDENCIA: [%s] | RIESGO: [%s]",
                            rs.getString("AININ_MEMORANDO"),
                            rs.getLong("AININ_CODIGO"),
                            rs.getLong("ARERE_CODIGO"),
                            rs.getInt("ARERE_NUM_REC"),
                            rs.getString("ARERE_EVIDENCIA_REQUERIDA"),
                            rs.getString("ARERE_RIESGO_VERIFICADO")));
                }
            }

            System.out.println("=== CONSULTAR TABLA AI_REC_EVIDENCIAS_REQUERIDAS PARA REC 503 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement("SELECT * FROM AI_REC_EVIDENCIAS_REQUERIDAS WHERE ARERE_CODIGO = 503");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println(String.format("EVID_REQ -> COD: %d | REC: %d | DESC: [%s] | ESTADO: [%s]",
                            rs.getLong("ARER_CODIGO"),
                            rs.getLong("ARERE_CODIGO"),
                            rs.getString("ARER_DESCRIPCION"),
                            rs.getString("ARER_ESTADO")));
                }
                if (!found) {
                    System.out.println("NO HAY EVIDENCIAS EN AI_REC_EVIDENCIAS_REQUERIDAS PARA REC 503");
                }
            }

            System.out.println("=== ACTUALIZAR ARERE_EVIDENCIA_REQUERIDA PARA REC 503 ===");
            String texto503 = "1. Manual de Funciones actualizado y aprobado con niveles de autoridad de Comercialización y Sistemas, DZO y Recaudador Agencias\n" +
                              "2. informe jurídico o aprobación competente\n" +
                              "3. comunicación de la reforma a los cargos involucrados.";
            try (java.sql.PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE AI_REC_RECOMENDACIONES SET ARERE_EVIDENCIA_REQUERIDA = ? WHERE ARERE_CODIGO = 503")) {
                updatePs.setString(1, texto503);
                int rows = updatePs.executeUpdate();
                System.out.println("Filas actualizadas para REC 503: " + rows);
            }

            System.out.println("=== VERIFICAR REC 503 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARERE_CODIGO, ARERE_NUM_REC, ARERE_EVIDENCIA_REQUERIDA FROM AI_REC_RECOMENDACIONES WHERE ARERE_CODIGO = 503");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(String.format("VERIFICADO -> REC: %d | NUM: %d | EVIDENCIA: [%s]",
                            rs.getLong("ARERE_CODIGO"),
                            rs.getInt("ARERE_NUM_REC"),
                            rs.getString("ARERE_EVIDENCIA_REQUERIDA")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico para alerta plan de acción vencido")
    @Test
    public void testAlertaPlanAccionVencido() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== PROBAR QUERY DE PLAN DE ACCION VENCIDO (>48 HORAS) ===");
            String querySql = 
                "SELECT " +
                "INF.AININ_CODIGO AS aininCodigo, " +
                "INF.AININ_TITULO AS aininTitulo, " +
                "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                "RES.ARERS_NOMBRE AS arersNombre, " +
                "RES.ARERS_APELLIDO AS arersApellido, " +
                "RES.ARERS_CORREO AS arersCorreo, " +
                "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                "REC.ARERE_CODIGO AS arereCodigo, " +
                "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax, " +
                "COALESCE(GREATEST(1, TRUNC(SYSDATE) - TRUNC(CAST(INF.AININ_FECHA AS DATE) + 2)), 1) AS diasRetraso " +
                "FROM AI_INF_INFORMES INF " +
                "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                "INNER JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                "WHERE INF.AININ_ESTADO = 1 " +
                "  AND INF.AININ_NOTIFICADO = '1' " +
                "  AND HALL.AREHA_ESTADO = 1 " +
                "  AND CON.ARECO_ESTADO = 1 " +
                "  AND REC.ARERE_ESTADO = 1 " +
                "  AND (REC.AREES_CODIGO IS NULL OR REC.AREES_CODIGO NOT IN (2, 4)) " +
                "  AND DR.AREDR_ESTADO = 1 " +
                "  AND RES.ARERS_ESTADO = 1 " +
                "  AND INF.AININ_FECHA IS NOT NULL " +
                "  AND (CAST(SYSDATE AS DATE) - CAST(INF.AININ_FECHA AS DATE)) * 24 > 48 " +
                "  AND NOT EXISTS ( " +
                "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                "        AND PA.ARPA_ESTADO = 'A' " +
                "  ) " +
                "  AND NOT EXISTS ( " +
                "      SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                "      WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                "        AND VER.AREVE_ESTADO = '1' " +
                "        AND VER.AREEV_CODIGO = 2 " +
                "  ) " +
                "ORDER BY INF.AININ_CODIGO DESC, REC.ARERE_CODIGO DESC";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(querySql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    if (count <= 10) {
                        System.out.println(String.format("PENDIENTE VENCIDO -> INF=%d (%s) | REC=%d | RES=%s %s (%s) | DIAS RETRASO=%d",
                                rs.getLong("aininCodigo"),
                                rs.getString("aininMemo"),
                                rs.getLong("arereCodigo"),
                                rs.getString("arersNombre"),
                                rs.getString("arersApellido"),
                                rs.getString("arersCorreo"),
                                rs.getInt("diasRetraso")));
                    }
                }
                System.out.println("TOTAL RECOMENDACIONES VENCIDAS (>48H) ENCONTRADAS: " + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Diagnóstico puntual de plan de acción 918")
    @Test
    public void testCheckPlanAccion918() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. RECOMENDACION 918 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARERE_CODIGO, ARERE_NUM_REC, ARERE_FECHA_MAXIMA FROM AI_REC_RECOMENDACIONES WHERE ARERE_CODIGO = 918")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("REC: " + rs.getLong(1) + " | NUM: " + rs.getString(2) + " | FECHA_MAX_GLOBAL: " + rs.getString(3));
                    }
                }
            }

            System.out.println("=== 2. DETALLES RECOMENDACION 918 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, DR.ARERS_CODIGO, DR.AREDR_FECHA_MAXIMA, R.ARERS_NOMBRE, R.ARERS_APELLIDO, R.ARERS_CODIGO_PER " +
                    "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                    "LEFT JOIN AI_REC_RESPONSABLES R ON R.ARERS_CODIGO = DR.ARERS_CODIGO " +
                    "WHERE DR.ARERE_CODIGO = 918")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("AREDR: %d | ARERE: %d | ARERS: %d | FECHA_IND: %s | RESPONSABLE: %s %s | COD_PER: %s",
                                rs.getLong("AREDR_CODIGO"), rs.getLong("ARERE_CODIGO"), rs.getLong("ARERS_CODIGO"),
                                rs.getString("AREDR_FECHA_MAXIMA"),
                                rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO"), rs.getString("ARERS_CODIGO_PER")));
                    }
                }
            }

            System.out.println("\n=== 3. RESTAURANDO FECHA DE JOAN SEBASTIAN A 11/10/2026 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "UPDATE AI_REC_DETALLE_RECOMENDACION SET AREDR_FECHA_MAXIMA = TO_DATE('2026-10-11', 'YYYY-MM-DD') WHERE AREDR_CODIGO = 2527")) {
                int updated = ps.executeUpdate();
                System.out.println("Filas restauradas para Joan Sebastian (2527): " + updated);
            }

            System.out.println("=== 4. VERIFICANDO DETALLES POST-RESTAURACION ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT DR.AREDR_CODIGO, DR.ARERE_CODIGO, DR.ARERS_CODIGO, DR.AREDR_FECHA_MAXIMA, R.ARERS_NOMBRE, R.ARERS_APELLIDO " +
                    "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                    "LEFT JOIN AI_REC_RESPONSABLES R ON R.ARERS_CODIGO = DR.ARERS_CODIGO " +
                    "WHERE DR.ARERE_CODIGO = 918")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("AREDR: %d | ARERE: %d | FECHA_IND: %s | RESPONSABLE: %s %s",
                                rs.getLong("AREDR_CODIGO"), rs.getLong("ARERE_CODIGO"),
                                rs.getString("AREDR_FECHA_MAXIMA"),
                                rs.getString("ARERS_NOMBRE"), rs.getString("ARERS_APELLIDO")));
                    }
                }
            }

            System.out.println("\n=== 2. PLANES DE ACCION REGISTRADOS PARA REC 918 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARPA_CODIGO, ARERE_CODIGO, AREDR_CODIGO, ARPA_ESTADO, ARPA_FECHA_ACTUALIZACION " +
                    "FROM AI_REC_PLAN_ACCION WHERE ARERE_CODIGO = 918")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("ARPA: %d | ARERE: %d | AREDR: %d | ESTADO: %s | FECHA: %s",
                                rs.getLong("ARPA_CODIGO"), rs.getLong("ARERE_CODIGO"), rs.getLong("AREDR_CODIGO"),
                                rs.getString("ARPA_ESTADO"), rs.getString("ARPA_FECHA_ACTUALIZACION")));
                    }
                }
            }

            System.out.println("\n=== 3. ACTIVIDADES DE PLAN REGISTRADAS PARA REC 918 ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ACT.ARPAC_CODIGO, ACT.ARPA_CODIGO, ACT.ARPAC_NUM_ACTIVIDAD, ACT.ARPAC_ACTIVIDAD, ACT.ARPAC_ENTREGABLE_ESPERADO " +
                    "FROM AI_REC_PLAN_ACTIVIDADES ACT " +
                    "INNER JOIN AI_REC_PLAN_ACCION PA ON PA.ARPA_CODIGO = ACT.ARPA_CODIGO " +
                    "WHERE PA.ARERE_CODIGO = 918")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(String.format("ARPAC: %d | ARPA: %d | NUM: %d | ACT: %s | ENTREGABLE: %s",
                                rs.getLong("ARPAC_CODIGO"), rs.getLong("ARPA_CODIGO"), rs.getInt("ARPAC_NUM_ACTIVIDAD"),
                                rs.getString("ARPAC_ACTIVIDAD"), rs.getString("ARPAC_ENTREGABLE_ESPERADO")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Prueba de diagnóstico de periodos e informes")
    @Test
    public void testDiagnosticoPeriodosEInformes() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== 1. PERIODOS DE EVALUACION EN BASE DE DATOS ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARCEV_CODIGO, ARCEV_NOMBRE, ARCEV_TIPO, " +
                    "TO_CHAR(ARCEV_FECHA_INICIO, 'YYYY-MM-DD') AS F_INI, " +
                    "TO_CHAR(ARCEV_FECHA_FIN, 'YYYY-MM-DD') AS F_FIN, " +
                    "TO_CHAR(ARCEV_FECHA_CORTE, 'YYYY-MM-DD') AS F_CORTE, " +
                    "ARCEV_ANIO, ARCEV_ESTADO " +
                    "FROM AI_CORTE_EVALUACION ORDER BY ARCEV_ANIO DESC, ARCEV_CODIGO ASC");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("ID: %d | NOMBRE: %s | TIPO: %s | INI: %s | FIN: %s | CORTE: %s | ANIO: %d | EST: %d",
                            rs.getLong("ARCEV_CODIGO"), rs.getString("ARCEV_NOMBRE"), rs.getString("ARCEV_TIPO"),
                            rs.getString("F_INI"), rs.getString("F_FIN"), rs.getString("F_CORTE"),
                            rs.getInt("ARCEV_ANIO"), rs.getInt("ARCEV_ESTADO")));
                }
            }

            System.out.println("\n=== 2. INFORMES EN AI_CORTE_EVALUACION_INFORME POR PERIODO ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT ARCEV_CODIGO, COUNT(*) AS TOTAL, " +
                    "SUM(CASE WHEN ARCEI_ES_MANUAL = 1 THEN 1 ELSE 0 END) AS MANUALES, " +
                    "SUM(CASE WHEN ARCEI_ES_MANUAL = 0 THEN 1 ELSE 0 END) AS AUTOMATICOS " +
                    "FROM AI_CORTE_EVALUACION_INFORME GROUP BY ARCEV_CODIGO ORDER BY ARCEV_CODIGO ASC");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("PERIODO ID: %d | TOTAL INFORMES: %d | MANUALES: %d | AUTOMATICOS: %d",
                            rs.getLong("ARCEV_CODIGO"), rs.getInt("TOTAL"), rs.getInt("MANUALES"), rs.getInt("AUTOMATICOS")));
                }
            }

            System.out.println("\n=== 3. ULTIMOS 15 INFORMES EMITIDOS/REGISTRADOS ===");
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT AININ_CODIGO, AININ_MEMORANDO, " +
                    "TO_CHAR(AININ_FECHA_APROBACION, 'YYYY-MM-DD') AS F_APROB, " +
                    "TO_CHAR(AININ_FECHA_MEMO, 'YYYY-MM-DD') AS F_MEMO, " +
                    "TO_CHAR(AININ_FECHA, 'YYYY-MM-DD') AS F_REG, " +
                    "AININ_ESTADO, AININ_TITULO " +
                    "FROM AI_INF_INFORMES ORDER BY AININ_CODIGO DESC FETCH FIRST 15 ROWS ONLY");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format("INF_ID: %d | MEMO: %s | F_APROB: %s | F_MEMO: %s | F_REG: %s | EST: %s | TIT: %s",
                            rs.getLong("AININ_CODIGO"), rs.getString("AININ_MEMORANDO"),
                            rs.getString("F_APROB"), rs.getString("F_MEMO"), rs.getString("F_REG"),
                            rs.getString("AININ_ESTADO"), rs.getString("AININ_TITULO")));
                }
            }
            System.out.println("\n=== 4. SIMULACION: QUE INFORMES ENTRARIAN AUTOMATICAMENTE A CADA PERIODO 2026? ===");
            String sqlCheck = 
                "SELECT :pId AS PER_ID, i.AININ_CODIGO, i.AININ_MEMORANDO, " +
                "TO_CHAR(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO), 'YYYY-MM-DD') AS F_EMISION " +
                "FROM AI_INF_INFORMES i " +
                "WHERE TRUNC(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO)) <= :fCorte " +
                "  AND (TO_CHAR(i.AININ_ESTADO) = '1' OR i.AININ_ESTADO = 1) " +
                "  AND ( " +
                "      EXISTS ( " +
                "          SELECT 1 FROM AI_CORTE_EVALUACION p " +
                "          WHERE p.ARCEV_CODIGO = :pId AND UPPER(TRIM(p.ARCEV_TIPO)) != 'CIERRE_ANUAL' " +
                "            AND TRUNC(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO)) BETWEEN p.ARCEV_FECHA_INICIO AND p.ARCEV_FECHA_FIN " +
                "      ) " +
                "      OR EXISTS ( " +
                "          SELECT 1 FROM AI_CORTE_EVALUACION p " +
                "          WHERE p.ARCEV_CODIGO = :pId AND UPPER(TRIM(p.ARCEV_TIPO)) = 'CIERRE_ANUAL' " +
                "            AND EXISTS ( " +
                "                SELECT 1 FROM AI_REC_HALLAZGOS h " +
                "                JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO " +
                "                JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO " +
                "                JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.AREES_CODIGO = es.AREES_CODIGO " +
                "                WHERE h.AININ_CODIGO = i.AININ_CODIGO " +
                "                  AND (TO_CHAR(r.ARERE_ESTADO) = '1' OR r.ARERE_ESTADO = 1) " +
                "                  AND ( " +
                "                      r.AREES_CODIGO NOT IN (2, 4) " +
                "                      OR (r.AREES_CODIGO = 2 AND r.ARERE_FECHA_CUMPLIDA IS NOT NULL AND TRUNC(r.ARERE_FECHA_CUMPLIDA) > :fCorte) " +
                "                  ) " +
                "                  AND UPPER(TRIM(es.AREES_DESCRIPCION)) NOT IN ('NO APLICA') " +
                "            ) " +
                "      ) " +
                "  ) " +
                "  AND NOT EXISTS ( " +
                "      SELECT 1 FROM AI_CORTE_EVALUACION_INFORME ci " +
                "      WHERE ci.ARCEV_CODIGO = :pId AND ci.AININ_CODIGO = i.AININ_CODIGO " +
                "  )";

            long[] periodos2026 = {1, 2, 3, 4, 5};
            for (long pId : periodos2026) {
                // Obtener fecha corte
                java.sql.Date fCorte = null;
                String pNombre = "";
                try (java.sql.PreparedStatement psP = conn.prepareStatement("SELECT ARCEV_NOMBRE, ARCEV_FECHA_CORTE FROM AI_CORTE_EVALUACION WHERE ARCEV_CODIGO = ?")) {
                    psP.setLong(1, pId);
                    try (java.sql.ResultSet rsP = psP.executeQuery()) {
                        if (rsP.next()) {
                            pNombre = rsP.getString("ARCEV_NOMBRE");
                            fCorte = rsP.getDate("ARCEV_FECHA_CORTE");
                        }
                    }
                }
                if (fCorte != null) {
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCheck.replace(":pId", String.valueOf(pId)))) {
                        ps.setDate(1, fCorte);
                        ps.setDate(2, fCorte);
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            int pendientes = 0;
                            while (rs.next()) {
                                pendientes++;
                            }
                            System.out.println(String.format("Total informes pendientes de auto-asignar para PERIODO %d (%s): %d", pId, pNombre, pendientes));
                        }
                    }
                }
            }
            System.out.println("\n=== 5. EJECUTANDO AUTOASIGNACION PARA PERIODOS 2026 ===");
            for (long pId : periodos2026) {
                java.sql.Date fCorte = null;
                String pNombre = "";
                try (java.sql.PreparedStatement psP = conn.prepareStatement("SELECT ARCEV_NOMBRE, ARCEV_FECHA_CORTE FROM AI_CORTE_EVALUACION WHERE ARCEV_CODIGO = ?")) {
                    psP.setLong(1, pId);
                    try (java.sql.ResultSet rsP = psP.executeQuery()) {
                        if (rsP.next()) {
                            pNombre = rsP.getString("ARCEV_NOMBRE");
                            fCorte = rsP.getDate("ARCEV_FECHA_CORTE");
                        }
                    }
                }
                if (fCorte != null) {
                    String sqlInsert = 
                        "INSERT INTO AI_CORTE_EVALUACION_INFORME (ARCEV_CODIGO, AININ_CODIGO, ARCEI_ES_MANUAL, ARCEI_FECHA_REGISTRO) " +
                        "SELECT " + pId + ", i.AININ_CODIGO, 0, SYSTIMESTAMP " +
                        "FROM AI_INF_INFORMES i " +
                        "WHERE TRUNC(COALESCE(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO, i.AININ_FECHA)) <= ? " +
                        "  AND (TO_CHAR(i.AININ_ESTADO) = '1' OR i.AININ_ESTADO = 1) " +
                        "  AND ( " +
                        "      EXISTS ( " +
                        "          SELECT 1 FROM AI_CORTE_EVALUACION p " +
                        "          WHERE p.ARCEV_CODIGO = " + pId + " AND UPPER(TRIM(p.ARCEV_TIPO)) != 'CIERRE_ANUAL' " +
                        "            AND TRUNC(COALESCE(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO, i.AININ_FECHA)) BETWEEN p.ARCEV_FECHA_INICIO AND p.ARCEV_FECHA_FIN " +
                        "      ) " +
                        "      OR EXISTS ( " +
                        "          SELECT 1 FROM AI_CORTE_EVALUACION p " +
                        "          WHERE p.ARCEV_CODIGO = " + pId + " AND UPPER(TRIM(p.ARCEV_TIPO)) = 'CIERRE_ANUAL' " +
                        "            AND EXISTS ( " +
                        "                SELECT 1 FROM AI_REC_HALLAZGOS h " +
                        "                JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO " +
                        "                JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO " +
                        "                JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.AREES_CODIGO = es.AREES_CODIGO " +
                        "                WHERE h.AININ_CODIGO = i.AININ_CODIGO " +
                        "                  AND (TO_CHAR(r.ARERE_ESTADO) = '1' OR r.ARERE_ESTADO = 1) " +
                        "                  AND ( " +
                        "                      r.AREES_CODIGO NOT IN (2, 4) " +
                        "                      OR (r.AREES_CODIGO = 2 AND r.ARERE_FECHA_CUMPLIDA IS NOT NULL AND TRUNC(r.ARERE_FECHA_CUMPLIDA) > ?) " +
                        "                  ) " +
                        "                  AND UPPER(TRIM(es.AREES_DESCRIPCION)) NOT IN ('NO APLICA') " +
                        "            ) " +
                        "      ) " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_CORTE_EVALUACION_INFORME ci " +
                        "      WHERE ci.ARCEV_CODIGO = " + pId + " AND ci.AININ_CODIGO = i.AININ_CODIGO " +
                        "  )";
                    try (java.sql.PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                        psIns.setDate(1, fCorte);
                        psIns.setDate(2, fCorte);
                        int inserted = psIns.executeUpdate();
                        System.out.println(String.format("  PERIODO [%d - %s] -> Se insertaron %d informes nuevos automáticos", pId, pNombre, inserted));
                    }
                }
            }

            System.out.println("\n=== 6. CONTEO ACTUALIZADO DESPUES DE AUTOASIGNAR ===");
            for (long pId : periodos2026) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) AS TOTAL FROM AI_CORTE_EVALUACION_INFORME WHERE ARCEV_CODIGO = ?")) {
                    ps.setLong(1, pId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println(String.format("PERIODO %d TOTAL INFORMES: %d", pId, rs.getInt("TOTAL")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Disabled("Ejecutado para actualizar FN_RECOMENDACIONES_FILTROS_R")
    @Test
    public void testActualizarFnFiltrosR() {
        String url = "jdbc:oracle:thin:@//172.20.35.209:1521/testorcl";
        String user = "ROOTSISREC";
        String pass = "sisrec1";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== ACTUALIZANDO FN_RECOMENDACIONES_FILTROS_R EN ORACLE ===");
            String ddl = 
                "CREATE OR REPLACE FUNCTION ROOTSISREC.FN_RECOMENDACIONES_FILTROS_R (\n" +
                "    p_anio          IN NUMBER,\n" +
                "    p_arees_codigo  IN NUMBER,\n" +
                "    p_arers_codper  IN VARCHAR2\n" +
                ") RETURN SYS_REFCURSOR\n" +
                "IS\n" +
                "    v_cursor   SYS_REFCURSOR;\n" +
                "    v_sql      VARCHAR2(4000);\n" +
                "BEGIN\n" +
                "    v_sql :=\n" +
                "      'WITH solicitudes_prorroga AS (\n" +
                "           SELECT S.ARERE_CODIGO, S.ARERS_CODIGO, MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA\n" +
                "           FROM ROOTSISREC.AI_REC_SOLICITUDES S\n" +
                "           WHERE S.ARESO_ESTADO = ''1''\n" +
                "             AND S.ARESO_ESTADO_APROB = ''3''\n" +
                "             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL\n" +
                "           GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO\n" +
                "       )\n" +
                "       SELECT DISTINCT D.*, R.ARERE_FECHA_MAXIMA\n" +
                "         FROM ROOTSISREC.AI_INF_INFORMES            I\n" +
                "         JOIN ROOTSISREC.AI_REC_HALLAZGOS           H ON H.AININ_CODIGO = I.AININ_CODIGO\n" +
                "         JOIN ROOTSISREC.AI_REC_CONCLUSIONES        C ON C.AREHA_CODIGO = H.AREHA_CODIGO\n" +
                "         JOIN ROOTSISREC.AI_REC_RECOMENDACIONES     R ON R.ARECO_CODIGO = C.ARECO_CODIGO\n" +
                "         JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D ON D.ARERE_CODIGO = R.ARERE_CODIGO\n" +
                "         JOIN ROOTSISREC.AI_REC_RESPONSABLES        S ON S.ARERS_CODIGO = D.ARERS_CODIGO\n" +
                "         LEFT JOIN solicitudes_prorroga             SP ON SP.ARERE_CODIGO = D.ARERE_CODIGO AND SP.ARERS_CODIGO = D.ARERS_CODIGO\n" +
                "        WHERE R.ARERE_ESTADO = 1\n" +
                "          AND I.AININ_ESTADO = 1\n" +
                "          AND D.AREDR_ESTADO = 1\n" +
                "          AND S.ARERS_ESTADO = 1\n" +
                "          AND S.ARERS_CODIGO_PER = ''' || p_arers_codper || '''';\n" +
                "\n" +
                "    IF p_arees_codigo IS NOT NULL AND p_arees_codigo <> 0 THEN\n" +
                "        IF p_arees_codigo = 1 THEN\n" +
                "            v_sql := v_sql || ' AND (\n" +
                "                R.AREES_CODIGO != 2\n" +
                "                AND (COALESCE(SP.FECHA_PRORROGA, D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA) IS NULL \n" +
                "                     OR TRUNC(COALESCE(SP.FECHA_PRORROGA, D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA)) >= TRUNC(SYSDATE))\n" +
                "                AND NOT EXISTS (\n" +
                "                    SELECT 1 FROM ROOTSISREC.AI_REC_VERIFICABLES V \n" +
                "                    WHERE V.AREDR_CODIGO = D.AREDR_CODIGO AND V.AREVE_ESTADO = 1 AND V.AREEV_CODIGO = 2\n" +
                "                )\n" +
                "            )';\n" +
                "        ELSIF p_arees_codigo = 2 THEN\n" +
                "            v_sql := v_sql || ' AND (\n" +
                "                R.AREES_CODIGO = 2\n" +
                "                OR EXISTS (\n" +
                "                    SELECT 1 FROM ROOTSISREC.AI_REC_VERIFICABLES V \n" +
                "                    WHERE V.AREDR_CODIGO = D.AREDR_CODIGO AND V.AREVE_ESTADO = 1 AND V.AREEV_CODIGO = 2\n" +
                "                )\n" +
                "            )';\n" +
                "        ELSIF p_arees_codigo = 3 THEN\n" +
                "            v_sql := v_sql || ' AND (\n" +
                "                R.AREES_CODIGO NOT IN (2, 4)\n" +
                "                AND TRUNC(COALESCE(SP.FECHA_PRORROGA, D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA)) < TRUNC(SYSDATE)\n" +
                "                AND NOT EXISTS (\n" +
                "                    SELECT 1 FROM ROOTSISREC.AI_REC_VERIFICABLES V \n" +
                "                    WHERE V.AREDR_CODIGO = D.AREDR_CODIGO AND V.AREVE_ESTADO = 1 AND V.AREEV_CODIGO = 2\n" +
                "                )\n" +
                "            )';\n" +
                "        END IF;\n" +
                "    END IF;\n" +
                "\n" +
                "    IF p_anio IS NOT NULL AND p_anio <> 0 THEN\n" +
                "        v_sql := v_sql || ' AND EXTRACT(YEAR FROM NVL(R.ARERE_FECHA_INICIO_CONTROL, NVL(I.AININ_FECHA_APROBACION, I.AININ_FECHA_MEMO))) = ' || p_anio;\n" +
                "    END IF;\n" +
                "\n" +
                "    v_sql := v_sql || ' ORDER BY COALESCE(D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA) DESC';\n" +
                "\n" +
                "    OPEN v_cursor FOR v_sql;\n" +
                "    RETURN v_cursor;\n" +
                "EXCEPTION\n" +
                "    WHEN OTHERS THEN\n" +
                "        RAISE_APPLICATION_ERROR(-20001, 'Error en FN_RECOMENDACIONES_FILTROS_R: ' || SQLERRM);\n" +
                "END FN_RECOMENDACIONES_FILTROS_R;";

            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute(ddl);
                System.out.println("FN_RECOMENDACIONES_FILTROS_R actualizada EXITOSAMENTE!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




