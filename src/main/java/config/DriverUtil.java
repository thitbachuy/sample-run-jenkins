package config;

import static org.openqa.selenium.chrome.ChromeOptions.LOGGING_PREFS;
import static org.openqa.selenium.remote.Browser.EDGE;
import static org.openqa.selenium.remote.Browser.FIREFOX;
import static org.openqa.selenium.remote.CapabilityType.ACCEPT_INSECURE_CERTS;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.exception.CucumberException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

public class DriverUtil {

  public static final String PATH_TO_DOWNLOAD_DIR =
    System.getProperty("user.home") + System.getProperty("file.separator") + "Downloads";
  private static RemoteWebDriver driver;
  private static DesiredCapabilities dc;
  private static final Logger LOG = LogManager.getLogger(DriverUtil.class);
  public static final ThreadLocal<Map<String, RemoteWebDriver>> threadLocalActiveBrowsers = new ThreadLocal<>();
  private static String proxyHost;
  private static String proxyPort;
  private static final String HUB_ENDPOINT = "jenkins-setting:4444";
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");
  private DriverUtil() {
  }

  public static RemoteWebDriver getDriver() {
    return threadLocalActiveBrowsers.get().get("current");
  }
  /* DOCKER DRIVER */

  public static RemoteWebDriver initDockerChrome(boolean incognito) {
    RemoteWebDriver driver;
    dc = new DesiredCapabilities();
    dc.setCapability(BROWSER_NAME, Browser.CHROME);
    dc.setBrowserName(Browser.CHROME.browserName());
    dc.setPlatform(Platform.LINUX);

    try {
      dc.setBrowserName("chrome");
      LoggingPreferences logPrefsCHROME = new LoggingPreferences();
      logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

      ChromeOptions chromeOptions = new ChromeOptions();
      Map<String, Object> preferences = new HashMap<>();
      preferences.put("profile.default_content_setting_values.notifications", 2);
      chromeOptions.setExperimentalOption("prefs", preferences);
      chromeOptions.addArguments("--disable-gpu");
      if (System.getProperty("os.name").toLowerCase().contains("linux")) {
        // in Linux, we need these config to work
        LOG.info("Working in Linux OS");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-shm-usage");
        chromeOptions.addArguments("--disable-setuid-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-using");
        chromeOptions.addArguments("start-maximized");
        chromeOptions.addArguments("disable-infobars");
        chromeOptions.addArguments("--headless=new");
      }

      chromeOptions.addArguments("--lang=en");
      chromeOptions.addArguments("--enable-javascript");
      chromeOptions.addArguments("--window-size=1920,1080");
      chromeOptions.setExperimentalOption("w3c", true);
      chromeOptions.setCapability("browserName", "chrome");
      chromeOptions.setAcceptInsecureCerts(true);
      chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

      if (incognito) {
        LOG.info("adding incognito to Browser");
        chromeOptions.addArguments("--incognito");
      }
      dc.merge(chromeOptions);
      LOG.info("Hub endpoint is: {}", HUB_ENDPOINT);
      driver = new RemoteWebDriver(new URL("http://" + HUB_ENDPOINT + "/wd/hub"), dc);
      LOG.info("Initialized driver is: {}", driver);
    } catch (Exception e) {
      throw new CucumberException("Failed to initialize ChromeDriver. Error: " + e.getMessage());
    }
    return driver;
  }

  public static RemoteWebDriver initDockerFirefox(boolean incognito) {
    dc = new DesiredCapabilities();
    dc.setCapability(BROWSER_NAME, FIREFOX);
    dc.setBrowserName(FIREFOX.browserName());
    dc.setPlatform(Platform.LINUX);

    FirefoxProfile testProfile = new FirefoxProfile();
    testProfile.setAcceptUntrustedCertificates(true);
    testProfile.setPreference("dom.webnotifications.enabled", false);
    testProfile.setPreference("javascript.enabled", true);

    FirefoxOptions options = new FirefoxOptions();
    options.setCapability(ACCEPT_INSECURE_CERTS, true);
//        options.setCapability("acceptSslCerts", true);
//        options.setCapability("marionette", true);
    options.setAcceptInsecureCerts(true);
    options.setProfile(testProfile);

    if (incognito) {
      LOG.info("adding incognito to Browser");
      testProfile.setPreference("browser.privatebrowsing.autostart", true);
    }

    try {
      dc.setBrowserName("firefox");
      dc.merge(options);
      LOG.info("Hub endpoint is: {}", HUB_ENDPOINT);
      driver = new RemoteWebDriver(new URL("http://" + HUB_ENDPOINT + "/wd/hub"), dc);
      LOG.info("Initialized driver is: {}", driver);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return driver;
  }

  public static RemoteWebDriver initDockerEdge(boolean incognito) {
    dc = new DesiredCapabilities();
    dc.setCapability(BROWSER_NAME, EDGE);
    dc.setBrowserName(EDGE.browserName());
    dc.setPlatform(Platform.LINUX);

    HashMap<String, Object> edgePrefs = new HashMap<>();
    edgePrefs.put("profile.default_content_settings.popups", 2);
    edgePrefs.put("profile.default_content_setting_values.notifications", 2);

    EdgeOptions edgeOptions = new EdgeOptions();
    edgeOptions.setCapability("prefs", edgePrefs);
    edgeOptions.setCapability(ACCEPT_INSECURE_CERTS, true);
    edgeOptions.setCapability("acceptInsecureCerts", true);
    edgeOptions.setCapability("acceptSslCerts", true);
    edgeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);

    if (incognito) {
      LOG.info("adding incognito to Browser");
      edgeOptions.setCapability("ms:inPrivate", true);
    }

    try {
      dc.setBrowserName("edge");
      dc.merge(edgeOptions);
      LOG.info("Hub endpoint is: {}", HUB_ENDPOINT);
      driver = new RemoteWebDriver(new URL("http://" + HUB_ENDPOINT + "/wd/hub"), dc);
      LOG.info("Initialized driver is: {}", driver);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return driver;
  }

  /* LOCAL DRIVER */

  public static RemoteWebDriver initChrome(boolean incognito, boolean proxy) {
    RemoteWebDriver driver;
    LoggingPreferences logPrefsCHROME = new LoggingPreferences();
    logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);
    ChromeOptions chromeOptions = new ChromeOptions();
    Map<String, Object> preferences = new HashMap<>();
    preferences.put("profile.default_content_setting_values.notifications", 2);
    preferences.put("safebrowsing.enabled", "true");
    preferences.put("plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"});
    preferences.put("plugins.always_open_pdf_externally", true);
    preferences.put("profile.default_content_settings.popups", 0);
    preferences.put("download.default_directory", PATH_TO_DOWNLOAD_DIR);

    chromeOptions.setExperimentalOption("prefs", preferences);
    chromeOptions.addArguments("--remote-allow-origins=*");
    chromeOptions.addArguments("--disable-gpu");
    if (System.getProperty("os.name").toLowerCase().contains("linux")) {
      // in Linux, we need these config to work
      LOG.info("Working in Linux OS");
      chromeOptions.addArguments("--no-sandbox");
      chromeOptions.addArguments("--disable-shm-usage");
      chromeOptions.addArguments("--disable-dev-shm-usage");
      chromeOptions.addArguments("--disable-setuid-sandbox");
      chromeOptions.addArguments("--disable-dev-shm-using");
      chromeOptions.addArguments("--disable-extensions");
      chromeOptions.addArguments("disable-infobars");
    }
    chromeOptions.addArguments("--remote-allow-origins=*");
    chromeOptions.addArguments("--lang=en");
    chromeOptions.addArguments("--enable-javascript");
    chromeOptions.setExperimentalOption("w3c", true);
    chromeOptions.setCapability("browserName", "chrome");
    chromeOptions.setAcceptInsecureCerts(true);
    chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

         /*
        Ensure that your Chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
    if (proxy) {
      Proxy proxyBrowser = new Proxy();
      getProxyServer();
      proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
      chromeOptions.setCapability("proxy", proxyBrowser);
    }

    if (incognito) {
      LOG.info("adding incognito to Browser");
      chromeOptions.addArguments("--incognito");
    }
    driver = new ChromeDriver(chromeOptions);
    return driver;
  }

  public static RemoteWebDriver initChromeHeadless(boolean incognito, boolean proxy) {
    RemoteWebDriver driver;
    LoggingPreferences logPrefsCHROME = new LoggingPreferences();
    logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

    ChromeOptions chromeOptions = new ChromeOptions();
    Map<String, Object> preferences = new HashMap<>();
    preferences.put("profile.default_content_setting_values.notifications", 2);
    preferences.put("profile.default_content_settings.popups", 0);
    chromeOptions.setExperimentalOption("prefs", preferences);
    chromeOptions.addArguments("--disable-gpu");
    chromeOptions.addArguments("--remote-allow-origins=*");

    chromeOptions.addArguments("--no-sandbox");
    chromeOptions.addArguments("--disable-shm-usage");
    chromeOptions.addArguments("--disable-dev-shm-usage");
    chromeOptions.addArguments("--disable-setuid-sandbox");
    chromeOptions.addArguments("--disable-dev-shm-using");
    chromeOptions.addArguments("window-size=1920,1080");
    chromeOptions.addArguments("disable-infobars");
    chromeOptions.addArguments("--headless=new");
    chromeOptions.addArguments("--lang=en");
    chromeOptions.addArguments("--enable-javascript");
    chromeOptions.setExperimentalOption("w3c", true);
    chromeOptions.setCapability("browserName", "chrome");
    chromeOptions.setAcceptInsecureCerts(true);
    chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

         /*
        Ensure that your chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
    if (proxy) {
      Proxy proxyBrowser = new Proxy();
      getProxyServer();
      proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
      chromeOptions.setCapability("proxy", proxyBrowser);
    }

    if (incognito) {
      LOG.info("adding incognito to Browser");
      chromeOptions.addArguments("--incognito");
    }
    ChromeDriverService driverService = ChromeDriverService.createDefaultService();
    driver = new ChromeDriver(driverService, chromeOptions);
    //Setup to enable download file in headless
    enableDownloadFileHeadless(driverService.getUrl().toString(), driver);
    return driver;
  }

  public static RemoteWebDriver initChromeExtension(boolean incognito, boolean proxy) {
    if (System.getProperty("executingEnv").contains("GCP")) {
      dc = new DesiredCapabilities();
      dc.setCapability(BROWSER_NAME, Browser.CHROME);
      dc.setBrowserName(Browser.CHROME.browserName());
      dc.setPlatform(Platform.LINUX);
      dc.setBrowserName("chrome");
    }
    //Setup chrome options
    LoggingPreferences logPrefsCHROME = new LoggingPreferences();
    logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

    ChromeOptions chromeOptions = new ChromeOptions();
    Map<String, Object> preferences = new HashMap<>();
    preferences.put("profile.default_content_setting_values.notifications", 2);
    LOG.info("Add chrome extension");
    String extensionPath = "src/test/java/config/chromeExtensions/feahianecghpnipmhphmfgmpdodhcapi.crx";
    chromeOptions.addExtensions(new File(extensionPath));
    LOG.info("Absolute Extension Path is: {}", new File(extensionPath).getAbsolutePath());
    LOG.info("Relative Extension Path is: {}", Paths.get(extensionPath));
    chromeOptions.setExperimentalOption("prefs", preferences);
    chromeOptions.addArguments("--disable-gpu");
    if (System.getProperty("os.name").toLowerCase().contains("linux")) {
      // in Linux, we need these config to work
      LOG.info("Working in Linux OS");
      chromeOptions.addArguments("--no-sandbox");
      chromeOptions.addArguments("--disable-shm-usage");
      chromeOptions.addArguments("--disable-dev-shm-usage");
      chromeOptions.addArguments("--disable-setuid-sandbox");
      chromeOptions.addArguments("--disable-dev-shm-using");
      chromeOptions.addArguments("start-maximized");
      chromeOptions.addArguments("disable-infobars");
      chromeOptions.addArguments("--headless");
    }
    chromeOptions.addArguments("--lang=en");
    chromeOptions.addArguments("--enable-javascript");
    chromeOptions.setExperimentalOption("w3c", true);
    chromeOptions.setCapability("browserName", "chrome");
    chromeOptions.setAcceptInsecureCerts(true);
    chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);
    if (incognito) {
      LOG.info("adding incognito to Browser");
      chromeOptions.addArguments("--incognito");
    }
    if (System.getProperty("executingEnv").contains("GCP")) {
      dc.merge(chromeOptions);
      LOG.info("Hub endpoint is: {}", HUB_ENDPOINT);
      try {
        driver = new RemoteWebDriver(new URL("http://" + HUB_ENDPOINT + "/wd/hub"), dc);
        LOG.info("Initialized driver is: {}", driver);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    } else {
        /*
        Ensure that your chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
      if (proxy) {
        Proxy proxyBrowser = new Proxy();
        getProxyServer();
        proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
        chromeOptions.setCapability("proxy", proxyBrowser);
      }
      driver = new ChromeDriver(chromeOptions);
    }
    return driver;
  }


  public static RemoteWebDriver initChromeSSH(boolean incognito, boolean proxy) {

    try {
      LoggingPreferences logPrefsCHROME = new LoggingPreferences();
      logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

      DesiredCapabilities dcSSH = new DesiredCapabilities();
      dcSSH.setCapability(BROWSER_NAME, Browser.CHROME);
      dcSSH.setBrowserName(Browser.CHROME.browserName());
      dcSSH.setPlatform(Platform.MAC);

      ChromeOptions chromeOptions = new ChromeOptions();
      Map<String, Object> preferences = new HashMap<>();
      preferences.put("profile.default_content_setting_values.notifications", 2);
      chromeOptions.setExperimentalOption("prefs", preferences);
      chromeOptions.addArguments("--disable-gpu");
      chromeOptions.addArguments("--lang=en");
      chromeOptions.addArguments("--enable-javascript");
      chromeOptions.setExperimentalOption("w3c", true);
      chromeOptions.setCapability("browserName", "chrome");
      chromeOptions.setAcceptInsecureCerts(true);
      chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

         /*
        Ensure that your chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
      if (proxy) {
        Proxy proxyBrowser = new Proxy();
        getProxyServer();
        proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
        chromeOptions.setCapability("proxy", proxyBrowser);
      }

      if (incognito) {
        LOG.info("adding incognito to Browser");
        chromeOptions.addArguments("--incognito");
      }
      dcSSH.merge(chromeOptions);
      driver = new RemoteWebDriver(new URL("http://192.168.178.39:4723/wd/hub"), dcSSH);
      driver.manage().window().setSize(new Dimension(1920, 1080));
      LOG.info("running driver on IP Adress 192.168.178.39:4723");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return driver;
  }

  public static RemoteWebDriver initFirefox(boolean incognito, boolean proxy) {

    LoggingPreferences logPrefsFOX = new LoggingPreferences();
    logPrefsFOX.enable(LogType.PERFORMANCE, Level.ALL);

    FirefoxOptions options = new FirefoxOptions();
    options.setCapability(ACCEPT_INSECURE_CERTS, true);
//        options.setCapability("acceptSslCerts", true);
//        options.setCapability("marionette", true);
    options.setAcceptInsecureCerts(true);

    // input type of firefox profile folder
    FirefoxProfile testProfile = (new ProfilesIni()).getProfile("automation");
    if (testProfile == null) {
      testProfile = new FirefoxProfile();
    }
    testProfile.setAcceptUntrustedCertificates(true);
    testProfile.setPreference("dom.webnotifications.enabled", false);
    testProfile.setPreference("javascript.enabled", true);

        /*
        Ensure that your firefox browser has proxy enabled.
        Options - Network Settings - Settings : Use System Proxy Settings
         */
    if (proxy) {
      getProxyServer();
      testProfile.setPreference("network.proxy.type", 1);
      testProfile.setPreference("network.proxy.ssl", proxyHost);
      testProfile.setPreference("network.proxy.ssl_port", Integer.parseInt(proxyPort));
    }

    if (incognito) {
      LOG.info("adding incognito to Browser");
      testProfile.setPreference("browser.privatebrowsing.autostart", true);
    }

    options.setProfile(testProfile);
    driver = new FirefoxDriver(options);
    return driver;
  }

  public static RemoteWebDriver initEdge(boolean incognito) {

    HashMap<String, Object> edgePrefs = new HashMap<>();
    edgePrefs.put("profile.default_content_settings.popups", 2);
    edgePrefs.put("profile.default_content_setting_values.notifications", 2);

    EdgeOptions edgeOptions = new EdgeOptions();
    edgeOptions.setCapability("prefs", edgePrefs);
    edgeOptions.setCapability(ACCEPT_INSECURE_CERTS, true);
    edgeOptions.setCapability("acceptInsecureCerts", true);
    edgeOptions.setCapability("acceptSslCerts", true);
    edgeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);

    if (incognito) {
      LOG.info("adding incognito to Browser");
      edgeOptions.setCapability("ms:inPrivate", true);
    }

    edgeOptions.merge(edgeOptions);
    driver = new EdgeDriver(edgeOptions);
    return driver;
  }

  public static RemoteWebDriver initSafari() {
    SafariOptions safariOptions = new SafariOptions();
    safariOptions.setCapability("acceptSslCerts", true);
    driver = new SafariDriver(safariOptions);
    return driver;
  }

  public static RemoteWebDriver initDriver(String browser, boolean incognito, boolean proxy) {
    RemoteWebDriver driver = null;
    switch (browser) {
      case "chromeGCP":
        driver = initDockerChrome(incognito);
        break;
      case "chromeHeadless":
        driver = initChromeHeadless(incognito, proxy);
        setUpUserAgentOfDriver(driver);
        driver = initChromeHeadlessUserAgent(TestDataLoader.getTestData("@TD:user-agent"), proxy,
          incognito);
        break;
      case "firefoxGCP":
        driver = initDockerFirefox(incognito);
        break;
      case "edgeGCP":
        driver = initDockerEdge(incognito);
        setUpUserAgentOfDriver(driver);
        driver = initEdgeDockerUserAgent(TestDataLoader.getTestData("@TD:user-agent"), incognito);
        break;
      case "chrome":
        driver = initChrome(incognito, proxy);
        break;
      case "firefox":
        driver = initFirefox(incognito, proxy);
        break;
      case "edge":
        driver = initEdge(incognito);
        break;
      case "safari":
        driver = initSafari();
        break;
      case "chrome-ssh":
        driver = initChromeSSH(incognito, proxy);
        break;
      case "chromeextension":
        driver = initChromeExtension(incognito, proxy);
        break;
      default:
        LOG.error("Browser {} is not supported by test automation framework!", browser);
        System.exit(0);
    }
    setCurrentDriver(driver);
    LOG.info("The current thread id '{}' has the following active browsers: {}",
      Thread.currentThread().getName(), threadLocalActiveBrowsers.get());
    return driver;
  }

  private static void setUpUserAgentOfDriver(RemoteWebDriver driver) {
    //Get user-agent in chrome headless
    String userAgent = driver.executeScript("return navigator.userAgent;").toString();
    LOG.info("Driver \"{}\" has user-agent \"{}\"", driver, userAgent);
    //Convert to user-agent chrome and set in test data
    TestDataLoader.setTestData("user-agent", userAgent.replace("HeadlessChrome", "Chrome"));
    LOG.info("Unused driver \"{}\" will be closed", driver);
    driver.quit();
    LOG.info("Close unused driver successfully!");
  }


  public static void setCurrentDriver(RemoteWebDriver remoteWebDriver) {
    threadLocalActiveBrowsers.get().put("current", remoteWebDriver);
  }

  public static void closeDriver() {
    LOG.info("remaining open Browsers: {}", threadLocalActiveBrowsers.get().keySet());
    threadLocalActiveBrowsers.get().keySet().forEach(driverAlias -> {
      threadLocalActiveBrowsers.get().get(driverAlias).quit();
      LOG.info("Driver: {} closed as expected :)", driverAlias);
    });
    driver = null;
  }

  public static RemoteWebDriver getBrowser(String browserName) {
    return threadLocalActiveBrowsers.get().get(browserName);
  }

  public static RemoteWebDriver initNewDriver(String browserName, boolean incognito,
    boolean proxy) {
    if (System.getProperty("executingEnv").contains("GCP") && !browserName.contains("GCP")
      && !browserName.contains("Extension")) {
      browserName = browserName.toLowerCase() + "GCP";
    } else {
      if (!browserName.equals("chromeHeadless") && !browserName.contains("GCP")) {
        browserName = browserName.toLowerCase();
      }
    }
    RemoteWebDriver newDriver = DriverUtil.initDriver(browserName, incognito, proxy);
    if (browserName.contains("GCP") || browserName.contains("Headless")) {
      newDriver.manage().window().setSize(new Dimension(1920, 1080));
    } else if (System.getProperty("executingEnv").contains("GCP") && browserName.contains(
      "extension")) {
      newDriver.manage().window().setSize(new Dimension(1920, 1080));
    } else {
      newDriver.manage().window().maximize();
    }
    newDriver.manage().deleteAllCookies();
    return newDriver;
  }

  public static void switchToBrowser(String browserName) {
    threadLocalActiveBrowsers.get().put("current", getBrowser(browserName));
    BasePage.threadLocalDriverBasePage.set(threadLocalActiveBrowsers.get().get("current"));
  }

  public static void closeBrowser(String browserName) {
    getBrowser(browserName).quit();
    threadLocalActiveBrowsers.get().remove(browserName);
  }

  public static void getProxyServer() {
    proxyHost = TestDataLoader.getTestData("@TD:proxyHost");
    proxyPort = TestDataLoader.getTestData("@TD:proxyPort");
  }

  public static RemoteWebDriver initDriverWithUserAgent(String browserName, String userAgent) {
    RemoteWebDriver driver;
    switch (browserName) {
      case "chromeHeadless":
        driver = initChromeHeadlessUserAgent(userAgent, false, false);
        break;
      case "chromeGCP":
        driver = initChromeDockerUserAgent(userAgent, false);
        break;
      case "chrome":
        driver = initChrome(false, false);
        break;
      case "edgeGCP":
        driver = initEdgeDockerUserAgent(userAgent, false);
        break;
      case "edge":
        driver = initEdge(false);
        break;
      case "firefoxGCP":
        driver = initDockerFirefox(false);
        break;
      case "firefox":
        driver = initFirefox(false, false);
        break;
      default:
        throw new CucumberException(
          "Browser name should be one of the following values: chromeHeadless, chromeGCP, chrome, edgeGCP, edge, firefoxGCP, firefox");
    }
    setCurrentDriver(driver);
    LOG.info("The current thread id '{}' has the following active browsers: {}",
      Thread.currentThread().getName(), threadLocalActiveBrowsers.get());
    return driver;
  }

  private static RemoteWebDriver initEdgeDockerUserAgent(String userAgent, boolean incognito) {
    dc = new DesiredCapabilities();
    dc.setCapability(BROWSER_NAME, EDGE);
    dc.setBrowserName(EDGE.browserName());
    dc.setPlatform(Platform.LINUX);

    HashMap<String, Object> edgePrefs = new HashMap<>();
    edgePrefs.put("profile.default_content_settings.popups", 2);
    edgePrefs.put("profile.default_content_setting_values.notifications", 2);

    EdgeOptions edgeOptions = new EdgeOptions();
    edgeOptions.setCapability("prefs", edgePrefs);
    edgeOptions.setCapability(ACCEPT_INSECURE_CERTS, true);
    edgeOptions.setCapability("acceptInsecureCerts", true);
    edgeOptions.setCapability("acceptSslCerts", true);
    edgeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
    edgeOptions.addArguments("--headless");
    edgeOptions.addArguments("--user-agent=" + userAgent);

    if (incognito) {
      LOG.info("adding incognito to Browser");
      edgeOptions.setCapability("ms:inPrivate", true);
    }

    try {
      dc.setBrowserName("edge");
      dc.merge(edgeOptions);
      LOG.info("Hub endpoint is: {}", HUB_ENDPOINT);
      driver = new RemoteWebDriver(new URL("http://" + HUB_ENDPOINT + "/wd/hub"), dc);
      LOG.info("Initialized driver is: {}", driver);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return driver;
  }

  private static RemoteWebDriver initChromeDockerUserAgent(String userAgent, boolean incognito) {
    RemoteWebDriver driver;
    dc = new DesiredCapabilities();
    dc.setCapability(BROWSER_NAME, Browser.CHROME);
    dc.setBrowserName(Browser.CHROME.browserName());
    dc.setPlatform(Platform.LINUX);

    try {
      dc.setBrowserName("chrome");
      LoggingPreferences logPrefsCHROME = new LoggingPreferences();
      logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

      ChromeOptions chromeOptions = new ChromeOptions();
      Map<String, Object> preferences = new HashMap<>();
      preferences.put("profile.default_content_setting_values.notifications", 2);
      preferences.put("plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"});
      preferences.put("download.default_directory", PATH_TO_DOWNLOAD_DIR);
      preferences.put("plugins.always_open_pdf_externally", true);
      preferences.put("profile.default_content_settings.popups", 0);
      chromeOptions.setExperimentalOption("prefs", preferences);
      chromeOptions.addArguments("--disable-gpu");
      if (System.getProperty("os.name").toLowerCase().contains("linux")) {
        // in Linux, we need these config to work
        LOG.info("Working in Linux OS");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-shm-usage");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-setuid-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-using");
        chromeOptions.addArguments("start-maximized");
        chromeOptions.addArguments("disable-infobars");
        chromeOptions.addArguments("--headless=new");
      }
      chromeOptions.addArguments("--user-agent=" + userAgent);
      LOG.info("Add user-agent \"{}\" to chrome", userAgent);
      chromeOptions.addArguments("--lang=en");
      chromeOptions.addArguments("--enable-javascript");
      chromeOptions.addArguments("--window-size=1920,1080");
      chromeOptions.setExperimentalOption("w3c", true);
      chromeOptions.setCapability("browserName", "chrome");
      chromeOptions.setAcceptInsecureCerts(true);
      chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

      if (incognito) {
        LOG.info("adding incognito to Browser");
        chromeOptions.addArguments("--incognito");
      }
      dc.merge(chromeOptions);
      LOG.info("Hub endpoint is: {}", HUB_ENDPOINT);
      driver = new RemoteWebDriver(new URL("http://" + HUB_ENDPOINT + "/wd/hub"), dc);
      LOG.info("Initialized driver is: {}", driver);
    } catch (Exception e) {
      throw new CucumberException("Failed to initialize ChromeDriver. Error: " + e.getMessage());
    }
    return driver;
  }

  private static RemoteWebDriver initChromeHeadlessUserAgent(String userAgent, boolean proxy,
    boolean incognito) {
    RemoteWebDriver driver;
    LoggingPreferences logPrefsCHROME = new LoggingPreferences();
    logPrefsCHROME.enable(LogType.PERFORMANCE, Level.ALL);

    ChromeOptions chromeOptions = new ChromeOptions();
    Map<String, Object> preferences = new HashMap<>();
    preferences.put("profile.default_content_setting_values.notifications", 2);
    preferences.put("plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"});
    preferences.put("download.default_directory", PATH_TO_DOWNLOAD_DIR);
    preferences.put("plugins.always_open_pdf_externally", true);
    preferences.put("profile.default_content_settings.popups", 0);
    chromeOptions.setExperimentalOption("prefs", preferences);
    chromeOptions.addArguments("--disable-gpu");
    chromeOptions.addArguments("--remote-allow-origins=*");

    chromeOptions.addArguments("--no-sandbox");
    chromeOptions.addArguments("--disable-shm-usage");
    chromeOptions.addArguments("--disable-dev-shm-usage");
    chromeOptions.addArguments("--disable-setuid-sandbox");
    chromeOptions.addArguments("--disable-dev-shm-using");
    chromeOptions.addArguments("window-size=1920,1080");
    chromeOptions.addArguments("disable-infobars");
    chromeOptions.addArguments("--headless=new");
    chromeOptions.addArguments("--user-agent=" + userAgent);
    chromeOptions.addArguments("--lang=en");
    chromeOptions.addArguments("--enable-javascript");
    chromeOptions.setExperimentalOption("w3c", true);
    chromeOptions.setCapability("browserName", "chrome");
    chromeOptions.setAcceptInsecureCerts(true);
    chromeOptions.setCapability(LOGGING_PREFS, logPrefsCHROME);

         /*
        Ensure that your chrome browser has proxy enabled.
        Settings - Advanced - System : Open your computer proxy settings should be able to open the dialog
         */
    if (proxy) {
      Proxy proxyBrowser = new Proxy();
      getProxyServer();
      proxyBrowser.setSslProxy(proxyHost + ":" + proxyPort);
      chromeOptions.setCapability("proxy", proxyBrowser);
    }

    if (incognito) {
      LOG.info("adding incognito to Browser");
      chromeOptions.addArguments("--incognito");
    }
    driver = new ChromeDriver(chromeOptions);
    return driver;
  }

  public static void getDeviceCapabilities(String device) {
    String deviceInfo = TestDataLoader.getTestData(device);
    String[] infos = deviceInfo.split("[\\|]");
    System.setProperty("deviceUDID", infos[0].trim());
    System.setProperty("deviceName", infos[1].trim());
    System.setProperty("platformVersion", infos[2].trim());
    LOG.info("deviceUDID: {}", System.getProperty("deviceUDID"));
    LOG.info("deviceName: {}", System.getProperty("deviceName"));
    LOG.info("platformVersion: {}", System.getProperty("platformVersion"));
  }

  //Setup to enable download file in headless
  private static void enableDownloadFileHeadless(String driverServiceUrl, RemoteWebDriver driver) {
    Map<String, Object> commandParams = new HashMap<>();
    commandParams.put("cmd", "Page.setDownloadBehavior");
    Map<String, String> params = new HashMap<>();
    params.put("behavior", "allow");
    params.put("downloadPath", PATH_TO_DOWNLOAD_DIR);
    commandParams.put("params", params);
    ObjectMapper objectMapper = new ObjectMapper();
    HttpClient httpClient = HttpClientBuilder.create().build();
    try {
      String command = objectMapper.writeValueAsString(commandParams);
      String url =
        driverServiceUrl + "/session/" + driver.getSessionId() + "/chromium/send_command";
      LOG.info("Driver service url is: {}", driverServiceUrl);
      LOG.info("Driver session id is: {}", driver.getSessionId());
      HttpPost request = new HttpPost(url);
      request.addHeader("content-type", "application/json");
      request.setEntity(new StringEntity(command));
      httpClient.execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void initializeBrowserWithSessionAlias(String browserName, String sessionAlias) {
    if (threadLocalActiveBrowsers.get().size() == 1) {
      threadLocalActiveBrowsers.get().put("initial", DriverUtil.getBrowser("current"));
    }
    if (browserName.contains("Headless") || browserName.contains("GCP")) {
      threadLocalActiveBrowsers.get().put(sessionAlias,
        DriverUtil.initDriverWithUserAgent(browserName,
          TestDataLoader.getTestData("@TD:user-agent")));
    } else {
      threadLocalActiveBrowsers.get().put(sessionAlias,
        DriverUtil.initNewDriver(browserName, browserName.contains("incognito"), false));
    }
    BasePage.threadLocalDriverBasePage.set(threadLocalActiveBrowsers.get().get(sessionAlias));
  }
}
