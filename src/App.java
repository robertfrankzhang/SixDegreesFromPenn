import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class App {
  private static Element connectToURL(String url) {
    Element root;
    try {
      Document doc = Jsoup.connect(url).get();
      root = doc.body();
      return root;
    } catch (IOException e) {
      // e.printStackTrace();
    }
    return null;
  }

  public static Map<String, List<String>> computeOutgoingEdges(List<List<String>> edgeList) {
    Map<String, List<String>> outgoingEdges = new HashMap<>();
    for (List<String> edge : edgeList) {
      String origin = edge.get(0);
      String end = edge.get(1);
      if (outgoingEdges.containsKey(origin)) {
        outgoingEdges.get(origin).add(end);
      } else {
        List<String> outgoing = new ArrayList<>();
        outgoing.add(end);
        outgoingEdges.put(origin, outgoing);
      }
    }
    return outgoingEdges;
  }

  public static Map<String, List<String>> computeIncomingEdges(List<List<String>> edgeList) {
    Map<String, List<String>> incomingEdges = new HashMap<>();
    for (List<String> edge : edgeList) {
      String origin = edge.get(0);
      String end = edge.get(1);
      if (incomingEdges.containsKey(end)) {
        incomingEdges.get(end).add(origin);
      } else {
        List<String> incoming = new ArrayList<>();
        incoming.add(origin);
        incomingEdges.put(end, incoming);
      }
    }
    return incomingEdges;
  }

  // Exploration Methods //
  public static Set<String> extractUniqueWikipediaLinks(Element document) {
    Set<String> uniqueLinks = new HashSet<>();
    if (document == null) {
      return null;
    }
    Elements links = document.select("a[href]");
    for (Element link : links) {
      String href = link.attr("abs:href");
      if (href.startsWith("https://en.wikipedia.org/wiki/") && !href.contains("#")) {
        uniqueLinks.add(href);
      }
    }
    return uniqueLinks;
  }

  public static String extractWikipediaArticleName(Element document) {
    if (document == null) {
      return null;
    }
    Element title = document.selectFirst("h1");
    String articleName = title.text();
    return articleName;
  }

  public static void exploreLink(String url, Map<String, Integer> nodeDict,
      List<List<String>> edgeList, Map<String, List<String>> outgoingEdges,
      Map<String, List<String>> incomingEdges, List<String> topPages) {
    // We assume that the link to be explored is already on the node dict
    // (unexplored), and that it is a top page. But has not been visited until now.
    Set<String> links = extractUniqueWikipediaLinks(connectToURL(url));
    if (links == null) {
      return;
    }
    for (String link : links) {
      // Connect to link and check if it is a top page
      String approximateArticleName = link.substring(link.lastIndexOf("/") + 1).replace("_", " ");
      if (checkIsArticle(link) && topPages.contains(approximateArticleName.toLowerCase())) {
        // Add to node dict
        if (!nodeDict.containsKey(link)) {
          nodeDict.put(link, 0);
        }

        // Add to edge list and dicts
        edgeList.add(new ArrayList<>(Arrays.asList(url, link)));
        if (outgoingEdges.containsKey(url)) {
          outgoingEdges.get(url).add(link);
        } else {
          List<String> outgoing = new ArrayList<>();
          outgoing.add(link);
          outgoingEdges.put(url, outgoing);
        }
        if (incomingEdges.containsKey(link)) {
          incomingEdges.get(link).add(url);
        } else {
          List<String> incoming = new ArrayList<>();
          incoming.add(url);
          incomingEdges.put(link, incoming);
        }
      }
    }
    // Update parent node as visited
    nodeDict.put(url, 1);
  }

  public static boolean checkIsArticle(String link) {
    String approximateArticleName = link.substring(link.lastIndexOf("/") + 1).replace("_", " ");
    String checkArticleName = link.replaceFirst("https://en.wikipedia.org/wiki/", "").replace("_", " ");
    return approximateArticleName.equals(checkArticleName) && !checkArticleName.contains(":");
  }

  public static List<String> BFS(String start, String end, Map<String, List<String>> outgoingEdges) {
    // BFS from start to end
    // Return list of nodes in path
    List<String> path = new ArrayList<>();
    List<String> queue = new ArrayList<>();
    Map<String, String> parent = new HashMap<>();
    queue.add(start);
    while (!queue.isEmpty()) {
      String node = queue.remove(0);
      if (node.equals(end)) {
        // Found path
        String curr = node;
        while (!curr.equals(start)) {
          path.add(0, curr);
          curr = parent.get(curr);
        }
        path.add(0, start);
        return path;
      }
      if (outgoingEdges.containsKey(node)) {
        for (String child : outgoingEdges.get(node)) {
          if (!parent.containsKey(child)) {
            parent.put(child, node);
            queue.add(child);
          }
        }
      }
    }
    return null;
  }

  public static List<List<String>> getTopXYKeys(Map<String, Float> map, int X, int Y) {
    PriorityQueue<Map.Entry<String, Float>> minHeap = new PriorityQueue<>(
        (entry1, entry2) -> Float.compare(entry1.getValue(), entry2.getValue()));

    for (Map.Entry<String, Float> entry : map.entrySet()) {
      minHeap.offer(entry);
      if (minHeap.size() > Y) {
        minHeap.poll();
      }
    }

    List<String> topY = new ArrayList<>(Y);
    while (!minHeap.isEmpty()) {
      topY.add(minHeap.poll().getKey());
    }
    Collections.reverse(topY);

    List<String> topX = topY.subList(0, X);

    List<List<String>> result = new ArrayList<>();
    result.add(topX);
    result.add(topY);

    return result;
  }

  public static void main(String[] args) throws Exception {
    // Node: (Link, hasExplored). Where hasExplored = 1 if the node was added
    // to node dict, its outgoing edges were added to the edge list, and its
    // outgoing nodes were added to the node dict
    // Edge: (Origin Link, End Link)

    // (1) Continue Penn graph exploration
    // (2) Query
    // Ask user if they want to do path 1 or 2
    Scanner input = new Scanner(System.in);
    int choice = 0;

    while (choice != 1 && choice != 2) {
      System.out.println("Please select a run option:");
      System.out.println("1. Continue Penn graph exploration for N nodes");
      System.out.println("2. Provide a wiki link to find a path from Penn");
      choice = input.nextInt();
      if (choice != 1 && choice != 2) {
        System.out.println("Invalid choice. Please select either 1 or 2.");
      }
    }

    if (choice == 1) {
      int numNodes;
      while (true) {
        System.out.println("Please enter the number of nodes to explore:");
        numNodes = input.nextInt();
        if (numNodes > 0) {
          break;
        }
        System.out.println("Invalid number of nodes. Please enter a positive integer.");
      }
      input.close();

      // Load node and edge csv into map and list respectively, if it exists
      Map<String, Integer> nodeDict;
      if (Files.exists(Paths.get("./src/nodes.csv"))) {
        nodeDict = FileManagement.loadNodeDict("./src/nodes.csv");
      } else {
        nodeDict = new HashMap<>();
      }

      List<List<String>> edgeList;
      if (Files.exists(Paths.get("./src/edges.csv"))) {
        edgeList = FileManagement.loadEdgeList("./src/edges.csv");
      } else {
        edgeList = new ArrayList<>();
      }
      Map<String, List<String>> outgoingEdges = computeOutgoingEdges(edgeList);
      Map<String, List<String>> incomingEdges = computeIncomingEdges(edgeList);

      List<String> topPages = FileManagement.loadTopPagesList("./src/WikiRoughPageRank.csv");

      // Explore N unexplored nodes
      int N_left = numNodes;
      if (nodeDict.size() == 0) {
        nodeDict.put("https://en.wikipedia.org/wiki/University_of_Pennsylvania", 0);
        exploreLink("https://en.wikipedia.org/wiki/University_of_Pennsylvania", nodeDict, edgeList, outgoingEdges,
            incomingEdges, topPages);
        N_left -= 1;
        System.out.println("Explored: Penn (" + N_left + " left)");
      }

      while (N_left > 0) {
        // Find the key of the first element where int = 0
        String next_node = null;
        for (Map.Entry<String, Integer> entry : nodeDict.entrySet()) {
          if (entry.getValue() == 0) {
            next_node = entry.getKey();
            break;
          }
        }
        if (next_node == null) {
          break;
        }
        exploreLink(next_node, nodeDict, edgeList, outgoingEdges, incomingEdges, topPages);
        N_left -= 1;
        System.out.println("Explored: " + next_node + " (" + N_left + " left)");
      }

      // Save results into updated node and edge csv
      FileManagement.saveNodesToCSV(nodeDict, "./src/nodes.csv");
      FileManagement.saveEdgesToCSV(edgeList, "./src/edges.csv");

    } else {
      /**
       * - Query user for wikipedia link, top N = 50, iteration count == 50, glove
       * model (1 - 4)
       * - Load node and edge csv into map and list respectively, (fail if they don't
       * exist)
       * - Check if nodes in node_dict match the given link. If yes, return path. If
       * no:
       * -- Add all nodes in node dict to candidate dict (node link, [parent link =
       * null, GloVe embedding, was_explored = 0])
       * - while candidate map does not have given link and iteration count is under
       * min:
       * -- Find GloVe embeddings for any link in candidate map that does not have a
       * GloVe embedding
       * -- Choose the top N closest nodes to the given link that have not been
       * explored. Mark was_explored = 1 for those nodes.
       * -- Explore the top N closest nodes, and add all links to candidate list (node
       * link, parent link = respective parent top N node link)
       * - If iteration count exceeds max and candidate map does not have given link,
       * print and error message. Then find the closest node to the given link and
       * return the path to that node.
       * - Else, return path to given link by tracing back through parent links to the
       * root node. Then BFS from root node to Penn node.
       */

      // Query user for wikipedia link, top N = 50, iteration count == 50, glove model
      // (1 - 4)
      String given_link;
      String original_given_link;
      while (true) {
        System.out.println(
            "Please enter a link from the English wikipedia in the format: https://en.wikipedia.org/wiki/Link_Name");
        given_link = input.next();
        original_given_link = given_link;
        if (checkIsArticle(given_link) && connectToURL(given_link) != null) {
          break;
        } else {
          System.out.println(
              "Invalid link. Please enter a link from the English wikipedia in the format: https://en.wikipedia.org/wiki/Link_Name");
        }
      }
      int N;
      while (true) {
        System.out.println(
            "Please enter the # of top articles to explore per iteration (i.e., the breadth of search, recommended = 100):");
        N = input.nextInt();
        if (N > 0) {
          break;
        } else {
          System.out.println("Invalid number. Please enter a positive integer.");
        }
      }
      int max_iterations;
      while (true) {
        System.out.println("Please enter the # of search iterations (i.e., the depth of search, recommended = 10):");
        max_iterations = input.nextInt();
        if (max_iterations > 0) {
          break;
        } else {
          System.out.println("Invalid number. Please enter a positive integer.");
        }
      }
      int glove_model;
      while (true) {
        System.out.println(
            "Please select a GloVe NLP model to use (1 - 4), where 4 is the largest model (i.e., the strength of search, recommended = 4):");
        glove_model = input.nextInt();
        if (glove_model >= 1 && glove_model <= 4) {
          break;
        } else {
          System.out.println("Invalid number. Please enter a number between 1 and 4.");
        }
      }
      input.close();

      System.out.println("Thanks for your input! Computing now...");
      System.out.println();

      // Load node and edge csv into map and list respectively, (fail if they don't
      // exist)
      Map<String, Integer> nodeDict;
      if (Files.exists(Paths.get("./src/nodes.csv"))) {
        nodeDict = FileManagement.loadNodeDict("./src/nodes.csv");
      } else {
        System.out.println("Error: nodes.csv does not exist. Generate by running the program with option 1 first.");
        return;
      }

      List<List<String>> edgeList;
      if (Files.exists(Paths.get("./src/edges.csv"))) {
        edgeList = FileManagement.loadEdgeList("./src/edges.csv");
      } else {
        System.out.println("Error: edges.csv does not exist. Generate by running the program with option 1 first.");
        return;
      }
      Map<String, List<String>> outgoingEdges = computeOutgoingEdges(edgeList);
      Map<String, List<String>> incomingEdges = computeIncomingEdges(edgeList);

      // Check if nodes in node_dict match the given link. If yes, return path from
      // University of Pennsylvania to given link using BFS.
      if (nodeDict.containsKey(given_link)) {
        System.out.println("No Problem! Here's a path from University of Pennsylvania to your given link:");
        List<String> path = BFS("https://en.wikipedia.org/wiki/University_of_Pennsylvania", given_link, outgoingEdges);
        System.out.println("Path from University of Pennsylvania to given link:");
        for (String link : path) {
          System.out.println(link);
        }
        return;
      }

      // Load Glove Embeddings
      Map<String, float[]> gloveEmbeddings;
      if (glove_model == 1) {
        gloveEmbeddings = GloVe.loadGloveModel("./src/glove/glove.6B.50d.txt");
      } else if (glove_model == 2) {
        gloveEmbeddings = GloVe.loadGloveModel("./src/glove/glove.6B.100d.txt");
      } else if (glove_model == 3) {
        gloveEmbeddings = GloVe.loadGloveModel("./src/glove/glove.6B.200d.txt");
      } else {
        gloveEmbeddings = GloVe.loadGloveModel("./src/glove/glove.6B.300d.txt");
      }

      // Add all nodes in node dict to candidate dict (node link, [parent link = null,
      // GloVe embedding, was_explored = 0])
      Map<String, String> candidateDictParent = new HashMap<String, String>();
      Map<String, float[]> candidateDictEmbeddings = new HashMap<String, float[]>();
      Map<String, Integer> candidateDictWasExplored = new HashMap<String, Integer>();
      for (Map.Entry<String, Integer> entry : nodeDict.entrySet()) {
        candidateDictParent.put(entry.getKey(), null);
        candidateDictEmbeddings.put(entry.getKey(), GloVe.getLinkEmbedding(entry.getKey(), gloveEmbeddings));
        candidateDictWasExplored.put(entry.getKey(), 0);
      }

      // Get embedding of given link
      float[] given_link_embedding = GloVe.getLinkEmbedding(given_link, gloveEmbeddings);
      Element given_article_element = connectToURL(given_link);
      String given_article_name = extractWikipediaArticleName(given_article_element);
      Set<String> given_article_links = extractUniqueWikipediaLinks(given_article_element);
      List<float[]> given_article_link_embeddings = new ArrayList<>();
      float[] average_given_article_link_embedding;
      if (given_article_links != null) {
        for (String link : given_article_links) {
          given_article_link_embeddings.add(GloVe.getLinkEmbedding(link, gloveEmbeddings));
        }

        // Get average of given_article_link_embeddings
        average_given_article_link_embedding = new float[given_article_link_embeddings.get(0).length];
        for (float[] link_embedding : given_article_link_embeddings) {
          for (int i = 0; i < link_embedding.length; i++) {
            average_given_article_link_embedding[i] += link_embedding[i];
          }
        }
        for (int i = 0; i < average_given_article_link_embedding.length; i++) {
          average_given_article_link_embedding[i] /= given_article_link_embeddings.size();
        }
      } else {
        average_given_article_link_embedding = given_link_embedding;
      }

      // While candidate map does not have given link and iteration count under max:
      int iteration_count = 0;
      float old_average_similarity_term = 0;
      float old_average_similarity_context = 0;
      while (!candidateDictParent.containsKey(given_link) && iteration_count < max_iterations) {
        // Find GloVe embeddings for any link in candidate map that does not have a
        // GloVe embedding
        for (Map.Entry<String, float[]> entry : candidateDictEmbeddings.entrySet()) {
          if (entry.getValue() == null) {
            entry.setValue(GloVe.getLinkEmbedding(entry.getKey(), gloveEmbeddings));
          }
        }

        // Compute cosine similarities of candidate links
        Map<String, Float> similarityScoresTerm = new HashMap<String, Float>();
        Map<String, Float> similarityScoresContext = new HashMap<String, Float>();
        for (Map.Entry<String, float[]> entry : candidateDictEmbeddings.entrySet()) {
          float term_similarity_score = GloVe.cosineSimilarity(entry.getValue(), given_link_embedding);
          float context_similarity_score = GloVe.cosineSimilarity(entry.getValue(),
              average_given_article_link_embedding);
          similarityScoresTerm.put(entry.getKey(), term_similarity_score);
          similarityScoresContext.put(entry.getKey(), context_similarity_score);
        }

        // Sort similarity scores and find the top N1*2 and N2*2 closest nodes to the
        // given
        // link
        // that have not been explored
        int N1 = (int) ((N * (iteration_count + 1)) / 2);
        int N2 = (int) ((N * (iteration_count + 2)) / 2);

        List<List<String>> term_result = getTopXYKeys(similarityScoresTerm, N1, N2);
        List<List<String>> context_result = getTopXYKeys(similarityScoresContext, N1, N2);
        List<String> topN1term = term_result.get(0);
        List<String> topN2term = term_result.get(1);
        List<String> topN1context = context_result.get(0);
        List<String> topN2context = context_result.get(1);

        Set<String> topN1Set = new HashSet<String>(topN1term);
        topN1Set.addAll(topN1context);
        List<String> topN1 = new ArrayList<String>(topN1Set);
        Set<String> topN2Set = new HashSet<String>(topN2term);
        topN2Set.addAll(topN2context);// topN1 should ALWAYS be a subset of topN2

        // Update metrics. Print iteration info
        float current_average_similarity_term = 0;
        float current_average_similarity_context = 0;
        int term_counter = 0;
        int context_counter = 0;
        for (String link : topN1) {
          if (similarityScoresTerm.containsKey(link)) {
            current_average_similarity_term += similarityScoresTerm.get(link);
            term_counter++;
          }
          if (similarityScoresContext.containsKey(link)) {
            current_average_similarity_context += similarityScoresContext.get(link);
            context_counter++;
          }
        }
        current_average_similarity_term /= term_counter;
        current_average_similarity_context /= context_counter;

        System.out.println("Iteration " + iteration_count + ":");
        System.out.println("Average similarity of target article titles to given article title (1 is best): "
            + current_average_similarity_term);
        System.out
            .println("Average similarity of target article titles to given article content (1 is best): "
                + current_average_similarity_context);

        // Clean up candidate maps, keeping only values in N2 || was explored
        candidateDictParent.entrySet().removeIf(
            entry -> (!topN2Set.contains(entry.getKey()) && candidateDictWasExplored.get(entry.getKey()) == 0));
        candidateDictEmbeddings.entrySet().removeIf(
            entry -> (!topN2Set.contains(entry.getKey()) && candidateDictWasExplored.get(entry.getKey()) == 0));
        candidateDictWasExplored.entrySet().removeIf(
            entry -> (!topN2Set.contains(entry.getKey()) && candidateDictWasExplored.get(entry.getKey()) == 0));

        // Explore the top N1 closest nodes, and add all links to candidate list (node
        // link, parent link = respective parent top N node link)
        int counter = 0;
        for (String link : topN1) {
          if (candidateDictWasExplored.get(link) == 1) {
            continue;
          }
          Element new_document = connectToURL(link);
          String article_name = extractWikipediaArticleName(new_document);
          if (article_name == null) {
            continue;
          }
          if (article_name.equals(given_article_name)) {
            given_link = link;
            given_link_embedding = GloVe.getLinkEmbedding(given_link, gloveEmbeddings);
            break;
          }

          Set<String> new_links = extractUniqueWikipediaLinks(new_document);
          if (new_links == null) {
            continue;
          }
          for (String new_link : new_links) {
            if (!candidateDictParent.containsKey(new_link) && checkIsArticle(new_link)) {
              candidateDictParent.put(new_link, link);
              candidateDictEmbeddings.put(new_link, GloVe.getLinkEmbedding(new_link, gloveEmbeddings));
              candidateDictWasExplored.put(new_link, 0);
            }
          }
          candidateDictWasExplored.put(link, 1);
          counter += 1;
        }
        System.out.println("Number of Target Articles Newly Explored in this Iteration: " + counter);
        System.out.println("New Candidate Target Article Pool Size: " + candidateDictParent.size());
        System.out.println();

        iteration_count += 1;

        if ((Math.abs(current_average_similarity_term - old_average_similarity_term) < 0.0001
            && Math.abs(current_average_similarity_context - old_average_similarity_context) < 0.0001)
            || (current_average_similarity_term < old_average_similarity_term
                && current_average_similarity_context < old_average_similarity_context)) {
          System.out
              .println(
                  "Average similarity between iterations has converged :/ Stopping search prematurely to save you time.");
          break;
        }
        old_average_similarity_term = current_average_similarity_term;
        old_average_similarity_context = current_average_similarity_context;
      }

      // If iteration count exceeds max and candidate map does not have given link,
      // print an error message. Then find the closest node to the given link and
      // return the path to that node.
      if (!candidateDictParent.containsKey(given_link)) {
        System.out.println(
            "Hrm. I couldn't find your article in time. I'm returning a path to an article I found that's most related to your given article.");
        float best_similarity = Float.MIN_VALUE;
        String best_link = "";
        for (Map.Entry<String, float[]> entry : candidateDictEmbeddings.entrySet()) {
          float term_similarity_score = GloVe.cosineSimilarity(entry.getValue(), given_link_embedding);
          float context_similarity_score = GloVe.cosineSimilarity(entry.getValue(),
              average_given_article_link_embedding);
          float similarity = (float) (0.8 * term_similarity_score + 0.2 * context_similarity_score);
          if (similarity > best_similarity) {
            best_similarity = similarity;
            best_link = entry.getKey();
          }
        }
        // Use parent to trace min_link back to root node
        List<String> path1 = new ArrayList<String>();
        String parent = best_link;
        path1.add(best_link);
        while (true) {
          parent = candidateDictParent.get(parent);
          if (parent == null) {
            break;
          }
          path1.add(parent);
        }

        // Reverse path1
        List<String> path = new ArrayList<String>();
        for (int i = path1.size() - 1; i >= 0; i--) {
          path.add(path1.get(i));
        }

        List<String> path2 = BFS("https://en.wikipedia.org/wiki/University_of_Pennsylvania", path.get(0),
            outgoingEdges);
        // add path 2 (minus the end node) to start of path
        for (int i = 0; i < path2.size() - 1; i++) {
          path.add(i, path2.get(i));
        }

        for (String link : path) {
          System.out.println(link);
        }
        if (!original_given_link.equals(given_link)) {
          System.out.println("NOTE: " + given_link + " redirects to " + original_given_link);
        }
        return;
      } else {// Else, return path to given link by tracing back through parent links to the
              // root node. Then BFS from root node to Penn node.
        // Use parent to trace given_link back to root node
        List<String> path1 = new ArrayList<String>();
        String parent = given_link;
        path1.add(given_link);
        while (true) {
          parent = candidateDictParent.get(parent);
          if (parent == null) {
            break;
          }
          path1.add(parent);
        }

        // Reverse path1
        List<String> path = new ArrayList<String>();
        for (int i = path1.size() - 1; i >= 0; i--) {
          path.add(path1.get(i));
        }

        List<String> path2 = BFS("https://en.wikipedia.org/wiki/University_of_Pennsylvania", path.get(0),
            outgoingEdges);
        // add path 2 (minus the end node) to start of path
        for (int i = 0; i < path2.size() - 1; i++) {
          path.add(i, path2.get(i));
        }

        System.out.println(
            "That was a bit harder! But I did it :). Here's a path from University of Pennsylvania to your given link:");
        for (String link : path) {
          System.out.println(link);
        }
        if (!original_given_link.equals(given_link)) {
          System.out.println("NOTE: " + given_link + " redirects to " + original_given_link);
        }
        return;
      }

    }
  }
}
