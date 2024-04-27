package templates;


/*
    In here we write the Java code which is executed on the UI. Please get locators for the UIElement from Libraries created before.
    Have a look to only write on Method for each action e.g. click on a button.
    There should be one Method to click Buttons with the variable "Buttons".
    For reducing the Maintenance effort, write all basic functions like click on webElement, enterTextInWebElement or verifyTextInWebElement
    in a separate Class called BasePage.
 */

import config.BasePage;
import io.cucumber.core.exception.CucumberException;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

public class template_pages extends BasePage {

  public template_pages(RemoteWebDriver driver) {
    super(driver);
  }

  private static final Logger LOG = LogManager.getLogger(template_pages.class);
  private final Map<String, String> xpathToTextField = template_locator.createLibraryTextField();
  private final Map<String, String> xpathToButton = template_locator.createLibraryButton();
  private final Map<String, String> xpathToDropdown = template_locator.createLibraryDropdown();


  public void methodToDoSomething1() {
    //verify you already are at a specific page
  }

  public void methodToDoSomething2(String UIElement) {
    String locator = xpathToTextField.get(UIElement);
    if (locator == null) {
      throw new CucumberException("No xpath defined for UIElement " + UIElement);
    }
    //enter text in UIElement
  }

  public void methodToDoSomething3(String UIElement) {
    String locator = xpathToButton.get(UIElement);
    if (locator == null) {
      throw new CucumberException("No xpath defined for UIElement " + UIElement);
    }
    //click on the UIElement
  }
}
