package config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.core.exception.CucumberException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class FilesUtils {

  private static final Logger LOG = LogManager.getLogger(FilesUtils.class);

  public static String readTextFile(String filePath) {
    try (FileInputStream fis = new FileInputStream(filePath)) {
      return IOUtils.toString(fis, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new CucumberException(e.getMessage());
    }
  }

  public static String readDocFile(String filePath) {
    StringBuilder data = new StringBuilder();
    try (FileInputStream fis = new FileInputStream(filePath)) {
      XWPFDocument document = new XWPFDocument(fis);
      List<XWPFParagraph> paragraphs = document.getParagraphs();
      for (XWPFParagraph para : paragraphs) {
        data.append(para.getText());
      }
    } catch (IOException e) {
      throw new CucumberException(e.getMessage());
    }
    LOG.info("The texts retrieved from the document: {}", data);
    return data.toString();
  }


  public void updateCucumberJsonFile() {
    String testResultCucumberDirectoryPath = Paths.get("").toAbsolutePath().toString();
    LOG.info("Current working directory is: {}", testResultCucumberDirectoryPath);
    //Get all available file name in test set (only support .txt)
    FilenameFilter textFiler = (dir, name) -> name.toLowerCase().matches("^cucumber.*\\.json$");
    File[] testExecutionFileList = Objects.requireNonNull(
      new File(testResultCucumberDirectoryPath).listFiles(textFiler));
    for (File file : testExecutionFileList) {
      LOG.info("Found cucumber json file: {}", file.getName());
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        // Update the JSON data
        JsonNode jsonNode = objectMapper.readTree(file);
        JsonNode tagsNode = jsonNode.get(0).get("tags");
        ((ObjectNode) jsonNode.get(0)).put("description", "@E2EAutomationTestResults");
        LOG.info("The 'Description' is updated to '@E2EAutomationTestResults'");
        for (JsonNode tagNode : tagsNode) {
          LOG.info("Put the 'name' field to empty from the 'tags' node '{}'", tagNode);
          ((ObjectNode) tagNode).put("name", "");
        }
        // Write the updated JSON string back to the file
        objectMapper.writeValue(file, jsonNode);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
