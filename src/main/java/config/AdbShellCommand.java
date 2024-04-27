package config;

import io.cucumber.core.exception.CucumberException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.IOException;

public class AdbShellCommand {
    public static void clearApplicationDataOfDevice(String deviceUDID, String appPackage) {
        // Clear chrome application data
        CommandLine cmdClear = CommandLine.parse("adb -s " + deviceUDID + " shell pm clear " + appPackage);
        CommandLine cmdStop = CommandLine.parse("adb -s " + deviceUDID + " shell am force-stop " + appPackage);
        DefaultExecutor shell = new DefaultExecutor();
        int exitValue = 9999;
        //Delete app cache and data
        try {
            exitValue = shell.execute(cmdClear);
        } catch (IOException ignored) {
        }
        if (exitValue > 0) throw new CucumberException("Cannot remove Chrome app data!");
        try {
            exitValue = shell.execute(cmdStop);
        } catch (IOException ignored) {
        }
        if (exitValue > 0) throw new CucumberException("Cannot stop " + appPackage + " app!");
    }

    public static void openUrl(String deviceUDID, String url) {
        // Clear chrome application data
        CommandLine cmdClear = CommandLine.parse("adb -s " + deviceUDID + " shell am start -a android.intent.action.VIEW -d " + url);
        DefaultExecutor shell = new DefaultExecutor();
        int exitValue = 9999;
        //Delete app cache and data
        try {
            exitValue = shell.execute(cmdClear);
        } catch (IOException ignored) {
        }
        if (exitValue > 0) throw new CucumberException("Cannot open url " + url);
    }
}
