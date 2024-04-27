package steps;

import static config.DriverUtil.closeDriver;
import static config.DriverUtil.threadLocalActiveBrowsers;

import com.assertthat.selenium_shutterbug.core.Capture;
import com.assertthat.selenium_shutterbug.core.Shutterbug;
import config.DriverUtil;
import config.TestDataLoader;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Hook {

  private static final Logger LOG = LogManager.getLogger(Hook.class);
  public static String browser = System.getProperty("browser");
  public static String testedEnv = System.getProperty("testedEnv");
  public static String platform = System.getProperty("platform");
  public static ThreadLocal<Boolean> threadLocalCookieAccepted = new ThreadLocal<>();
  public static final ThreadLocal<Map<String, String>> threadLocalDataSetInExecution = new ThreadLocal<>();
  @BeforeAll
  public static void beforeAll() {
  }

  @Before
  public static void beginScenario(Scenario scenario) {
    Collection<String> tags = scenario.getSourceTagNames();
    LOG.info("-------------------------------------------");
    LOG.info("Running Tests in \"{}\" browser", browser);
    threadLocalActiveBrowsers.set(new HashMap<>());
    RemoteWebDriver driver = DriverUtil.initDriver(browser, false, false);
    if (browser.contains("GCP") || browser.contains("Headless")) {
      driver.manage().window().setSize(new Dimension(1920, 1080));
    } else {
      driver.manage().window().maximize();
    }
    driver.manage().deleteAllCookies();
    LOG.info("START SCENARIO '{}'", scenario.getName());
    LOG.info("With Tags: {}", tags);
    LOG.info("-------------------------------------------");
    TestDataLoader abc = new TestDataLoader();
  }


  @After(order = 1)
  public static void endScenario(Scenario scenario) {
    LOG.info("-------------------------------------------");
    LOG.info("END SCENARIO {}", scenario.getName());
    LOG.info("-------------------------------------------");
    if (scenario.isFailed()) {
      captureFullScreenShot(scenario);
    }
    closeDriver();
  }

  @After(order = 3)
  public void printTestCaseData(Scenario scenario) {
    String dataSetInExecutionString = "**********************************************************\n";
    Map<String, String> scenarioInfo = new HashMap<>();
    scenarioInfo.put("Scenario", scenario.getName());
    scenarioInfo.put("Tags", scenario.getSourceTagNames().toString());
    scenarioInfo.put("Executing env", System.getProperty("executingEnv"));
    scenarioInfo.put("Tested env", testedEnv);
    for (Map.Entry<String, String> data : scenarioInfo.entrySet()) {
      dataSetInExecutionString += String.format("%s: %s%n", StringUtils.rightPad(data.getKey(), 30),
        data.getValue());
    }
    dataSetInExecutionString += "**********************************************************\n";
    byte[] testDataMap = dataSetInExecutionString.getBytes(StandardCharsets.UTF_8);
    scenario.attach(testDataMap, "text/plain",
      scenario.getName().replace(" ", "_") + "_collected_information");
  }


  @AfterAll
  public static void AfterAll() {
  }

  public static void captureFullScreenShot(Scenario message) {
    byte[] screenshot;
    try {
      screenshot = Shutterbug.shootPage(DriverUtil.getDriver(), Capture.FULL).getBytes();
      message.attach(screenshot, "image/png",
        message.getName().replace(" ", "_") + "_full_error_screenshot");
    } catch (IOException e) {
      LOG.info("Can not capture full screenshot: ", e);
    }
  }
}
