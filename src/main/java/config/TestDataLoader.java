package config;

/*
Set the filename for INPUT Data as SystemProperty when executing the test via Maven
to get TestData via Execution use method getTestData, to write testdata in File to reuse later again use method setTestData
*/

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.exception.CucumberException;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import steps.Hook;

public class TestDataLoader {

  private static final Logger LOG = LogManager.getLogger(TestDataLoader.class);
  private static final ThreadLocal<Map<String, String>> commonData = new ThreadLocal<>();
  private static final ThreadLocal<Map<String, String>> specifiedEnvData = new ThreadLocal<>();
  private static final ThreadLocal<Map<String, String>> threadLocalTestDataRuntime = new ThreadLocal<>();
  private static Random random = new Random();

  public TestDataLoader() {
    commonData.set(readJsonFile("COMMON_DATA.json"));
    specifiedEnvData.set(readJsonFile("INPUT_" + Hook.testedEnv.toUpperCase() + ".json"));
    assert specifiedEnvData.get() != null;
    assert commonData.get() != null;
    threadLocalTestDataRuntime.set(mergeTestData(commonData.get(), specifiedEnvData.get()));
  }

  public static Map<String, String> readJsonFile(String dataFileToLoad) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(new File("src/main/java/testdata/input/" + dataFileToLoad),
        new TypeReference<>() {
        });

    } catch (Exception e) {
      e.printStackTrace();
    }
    return Collections.emptyMap();
  }

  public static Map<String, String> mergeTestData(Map<String, String> common,
    Map<String, String> envSpecific) {
    if (common == null) {
      throw new CucumberException("common data must not null");
    }
    if (envSpecific == null) {
      throw new CucumberException("envSpecific data must not null");
    }
    common.forEach((key, value) -> {
      envSpecific.forEach((key2, value2) -> {
        if (key.equals(key2)) {
          LOG.info("found duplicate key \"{}\"", key);
        }
      });
    });
    Map<String, String> environmentVariables = System.getenv();
    environmentVariables.forEach((key, value) ->
      LOG.info("Key {} has value {}", key, value)
    );
    common.putAll(envSpecific);
    common.putAll(environmentVariables);
    return common;
  }

  public static String getTestData(String key) {
    assert threadLocalTestDataRuntime.get() != null;
    String value = "";

    if (key.startsWith("@TD:") || key.startsWith("@td:")) {
      int end = key.length();
      value = threadLocalTestDataRuntime.get().get(key.substring(4, end));
      checkTestData(key, value);
    } else if (key.equals("@RandomFirstName") || key.equals("@RandomLastName")) {
      value = getTestDataNameValue(key, value);
    } else {
      value = key;
    }
    return value;
  }

  public static void checkTestData(String key, String value) {
    if (value == null) {
      throw new CucumberException("your key " + key + " is not pointing to a Value!");
    } else {
      String lowercaseKey = key.toLowerCase();
      if (lowercaseKey.contains("password") || lowercaseKey.contains("pw") || lowercaseKey.contains(
        "pin") || lowercaseKey.contains("clientid") || lowercaseKey.contains("clientsecret")
        || lowercaseKey.contains("credentials") || lowercaseKey.contains("mailosaur_apikey")) {
        LOG.info("Loading \"{}\" as \"*********\" from Test Data Set", key);
      } else {
        LOG.info("Loading \"{}\" as \"{}\" from Test Data Set", value, key);
      }
    }
  }

  public static String getTestDataNameValue(String key, String value) {
    if (key.equals("@RandomFirstName")) {
      value = "SkyTest";
      setTestData("currentRandomFirstName", value);
    } else if (key.equals("@RandomLastName")) {
      value = "Deutsch " + StringUtils.capitalize(generateString(12));
      setTestData("currentRandomLastName", value);
    }
    return value;
  }

  public static String generateString(Integer lenght) {
    char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    StringBuilder sb = new StringBuilder(lenght);
    for (int i = 0; i < lenght; i++) {
      char c = chars[random.nextInt(chars.length)];
      sb.append(c);
    }
    String output = sb.toString();
    LOG.info("created string: {}", output);
    return output;
  }

  public static void setTestData(String key, String value) {
    assert threadLocalTestDataRuntime.get() != null;
    threadLocalTestDataRuntime.get().put(key, value);
    if (Hook.threadLocalDataSetInExecution.get() == null) {
      Hook.threadLocalDataSetInExecution.set(new HashMap<>());
    }
    Hook.threadLocalDataSetInExecution.get().put(key, value);
    LOG.info("Saving \"{}\" as \"{}\" in Test Data Set", value, key);
  }

  public static String calculateTimestampWithFormat(String calculation, Locale... locales) {
    String[] dateTimeInfoArray = calculation.split("_FORMAT");
    String differenceDateTime = dateTimeInfoArray[0];
    String dateTimeFormat = dateTimeInfoArray[1];
    DateTimeFormatter dtf;
    if (locales.length > 0) {
      dtf = DateTimeFormatter.ofPattern(dateTimeFormat).withLocale(locales[0]);
    } else {
      dtf = DateTimeFormatter.ofPattern(dateTimeFormat);
    }
    LocalDate newDate = calculateDateTime(differenceDateTime);

    return dtf.format(newDate);
  }

  public static LocalDate calculateDateTime(String value) {
    LocalDate newDate = LocalDate.now(ZoneId.of("Europe/Paris"));
    if (!value.equals("")) {
      long calc = Long.parseLong(value.replaceAll("[a-zA-Z]", ""));
      if (value.contains("day")) {
        newDate = newDate.plusDays(calc);
      } else if (value.contains("week")) {
        newDate = newDate.plusWeeks(calc);
      } else if (value.contains("month")) {
        newDate = newDate.plusMonths(calc);
      } else if (value.contains("year")) {
        newDate = newDate.plusYears(calc);
      } else {
        throw new CucumberException("please choose a valid format like day, week, month or year");
      }
    }
    return newDate;
  }
}
