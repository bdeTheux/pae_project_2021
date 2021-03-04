package be.vinci.pae.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

  private static Properties props = new Properties();

  public static void load(String file) {
    try (FileInputStream in = new FileInputStream(file)) {
      props.load(in);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getProperty(String key) {
    return props.getProperty(key);
  }
}