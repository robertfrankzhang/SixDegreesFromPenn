import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GloVe {
  public static Map<String, float[]> loadGloveModel(String filePath) throws IOException {
    Map<String, float[]> embeddings = new HashMap<>();
    BufferedReader br = new BufferedReader(new FileReader(filePath));
    String line;

    while ((line = br.readLine()) != null) {
      String[] split = line.split(" ");
      String word = split[0];
      float[] embedding = new float[split.length - 1];

      for (int i = 1; i < split.length; i++) {
        embedding[i - 1] = Float.parseFloat(split[i]);
      }

      embeddings.put(word, embedding);
    }

    br.close();
    return embeddings;
  }

  public static float[] getLinkEmbedding(String link, Map<String, float[]> embeddings) {
    String phrase = link.substring(link.lastIndexOf("/") + 1).replace("_", " ");
    String[] words = phrase.toLowerCase().split(" ");
    int dimension = embeddings.values().iterator().next().length;
    float[] phraseEmbedding = new float[dimension];

    for (String word : words) {
      float[] wordEmbedding = embeddings.getOrDefault(word, new float[dimension]);
      for (int i = 0; i < dimension; i++) {
        phraseEmbedding[i] += wordEmbedding[i];
      }
    }

    for (int i = 0; i < dimension; i++) {
      phraseEmbedding[i] /= words.length;
    }

    return phraseEmbedding;
  }

  public static float cosineSimilarity(float[] a, float[] b) {
    float dotProduct = 0;
    float normA = 0;
    float normB = 0;

    for (int i = 0; i < a.length; i++) {
      dotProduct += a[i] * b[i];
      normA += Math.pow(a[i], 2);
      normB += Math.pow(b[i], 2);
    }

    float result = dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    result = Float.isNaN(result) ? 0 : result;
    return result;
  }
}