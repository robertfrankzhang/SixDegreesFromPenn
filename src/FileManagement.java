import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManagement {
  // File Load and Save Methods //
  public static void saveNodesToCSV(Map<String, Integer> dictionary, String filePath) {
    try {
      Path path = Paths.get(filePath);
      if (Files.exists(path)) {
        Files.delete(path);
      }
      Files.createFile(path);

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
          String key = entry.getKey();
          int value = entry.getValue();
          bw.write(key + "," + value);
          bw.newLine();
        }
      }
    } catch (IOException e) {
      System.err.println("Error saving the dictionary to a CSV file: " + e.getMessage());
    }
  }

  public static void saveEdgesToCSV(List<List<String>> list, String filePath) {
    try {
      Path path = Paths.get(filePath);
      if (Files.exists(path)) {
        Files.delete(path);
      }
      Files.createFile(path);

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        for (List<String> edge : list) {
          String origin = edge.get(0);
          String end = edge.get(1);
          bw.write(origin + "," + end);
          bw.newLine();
        }
      }
    } catch (IOException e) {
      System.err.println("Error saving the list to a CSV file: " + e.getMessage());
    }
  }

  public static String[] splitByFinalComma(String input) {
    int lastCommaIndex = input.lastIndexOf(',');
    if (lastCommaIndex != -1) {
      String firstPart = input.substring(0, lastCommaIndex);
      String secondPart = input.substring(lastCommaIndex + 1).trim();
      return new String[] { firstPart, secondPart };
    } else {
      return new String[] { input };
    }
  }

  public static Map<String, Integer> loadNodeDict(String filePath) {
    String line;
    Map<String, Integer> map = new HashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      while ((line = br.readLine()) != null) {
        String[] keyValue = splitByFinalComma(line);
        map.put(keyValue[0], Integer.parseInt(keyValue[1]));
      }
      return map;
    } catch (IOException e) {
      System.out.println("File not found");
      return null;
    }
  }

  public static List<List<String>> loadEdgeList(String filePath) {
    String line;
    String csvSplitBy = ",https://";
    List<List<String>> list = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      while ((line = br.readLine()) != null) {
        String[] keyValue = line.split(csvSplitBy);
        List<String> edge = new ArrayList<>();
        edge.add(keyValue[0]);
        edge.add("https://" + keyValue[1]);
        list.add(edge);
      }
      return list;
    } catch (IOException e) {
      System.out.println("File not found");
      return null;
    }
  }

  public static List<String> loadTopPagesList(String filePath) {
    String line;
    String csvSplitBy = ",";
    List<String> list = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      while ((line = br.readLine()) != null) {
        String[] keyValue = line.split(csvSplitBy, 2);
        list.add(keyValue[1].toLowerCase());
      }
      return list;
    } catch (IOException e) {
      System.out.println("File not found");
      return null;
    }
  }
}
