package locators;

import java.util.HashMap;
import java.util.Map;

public class SearchLocators {

  public static Map<String, String> createLibraryInput() {
    Map<String, String> xpathToInput = new HashMap<>();
    xpathToInput.put("correct search input", "//*[@name='q']");
    xpathToInput.put("incorrect search input", "//input[@id='uat']");
    xpathToInput.put("tiki search", "//input[@data-view-id='main_search_form_input']");
    xpathToInput.put("shopee search", "//input");
    return xpathToInput;
  }
}
