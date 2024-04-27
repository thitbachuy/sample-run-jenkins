package templates;

/*
    This class only stores the Locators. We use HashMaps<> to connect a key and a value.
    As an Example I created Libraries for TextField, Button and Dropdown.
    All Locator Libraries of ONE Page should be stored in here!
 */

import java.util.HashMap;
import java.util.Map;

public class template_locator {

  public static Map<String, String> createLibraryTextField() {
    Map<String, String> xpathToTextField = new HashMap<>();
    xpathToTextField.put("key", "value");
    return xpathToTextField;
  }

  public static Map<String, String> createLibraryButton() {
    Map<String, String> xpathToButton = new HashMap<>();
    xpathToButton.put("key", "value");
    return xpathToButton;
  }

  public static Map<String, String> createLibraryDropdown() {
    Map<String, String> xpathToDropdown = new HashMap<>();
    xpathToDropdown.put("key", "value");
    return xpathToDropdown;
  }

}
