import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
  features = "src/test/java/features/test",
  glue = {"steps"},
//  tags = "@Tiki",
  plugin = {"pretty","timeline:target/cucumber-report", "html:target/cucumber-report.html",
    "json:target/cucumber-report/cucumber.json", "junit:target/cucumber-report/cucumber.xml"}
)
public class TestRunner extends AbstractTestNGCucumberTests {

  @Override
  @DataProvider(parallel = true)
  public Object[][] scenarios() {
    return super.scenarios();
  }
}

