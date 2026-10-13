package ec.com.eeasa.wssisrec.wssisrec.features.test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestService {
  private final JdbcTemplate jdbcTemplate;

  public TestService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public boolean isConnected() {
    try {
      String result = jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", String.class);
      return result != null;
    } catch (Exception e) {
      return false;
    }
  }

}
