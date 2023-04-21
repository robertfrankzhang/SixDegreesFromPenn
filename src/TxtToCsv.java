import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TxtToCsv {

  public static void main(String[] args) {
    String inputFilePath = "./src/WikiRoughPageRank.txt";
    String outputFilePath = "./src/WikiRoughPageRank.csv";

    try {
      convertTxtToCsv(inputFilePath, outputFilePath);
      System.out.println("Conversion complete.");
    } catch (IOException e) {
      System.err.println("Error occurred while processing the files: " + e.getMessage());
    }
  }

  public static void convertTxtToCsv(String inputFilePath, String outputFilePath) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
        PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {

      String line;
      reader.readLine();
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("  |\t", 2); // Split the line using two spaces
        if (parts.length == 2) {
          String number = parts[0].trim();
          String text = parts[1].trim();

          // Write the values as a CSV line
          writer.println(number + "," + text);
        }
      }
    }
  }
}