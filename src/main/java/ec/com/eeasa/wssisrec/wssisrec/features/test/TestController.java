package ec.com.eeasa.wssisrec.wssisrec.features.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  private final TestService testService;

  public TestController(TestService testService) {
    this.testService = testService;
  }

  @GetMapping("/test-connection")
  public String testConnection() {
    if (testService.isConnected()) {
      return "Connected";
    } else {
      return "Not connected";
    }
  }

}
