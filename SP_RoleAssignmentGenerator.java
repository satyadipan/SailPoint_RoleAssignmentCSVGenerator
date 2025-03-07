import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SailpointCSVParser {

    public static void main(String[] args) {
        String inputFilePath = "roles_input.csv";  // Input CSV File (RoleName, Criteria)
        String outputFilePath = "roles_output.csv"; // Output CSV File (RoleName, JSON Criteria)

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            bw.write("RoleName,CriteriaJSON\n"); // Writing header to output file

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2); // Split CSV into RoleName and Criteria
                if (parts.length < 2) continue; // Skip invalid lines

                String roleName = parts[0].trim();
                String criteria = parts[1].trim();

                JSONObject jsonOutput = parseComplexCondition(criteria);
                bw.write(roleName + "," + jsonOutput.toString().replaceAll("\\s+", "") + "\n");
            }

            System.out.println("CSV processing complete. Output written to: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject parseComplexCondition(String condition) {
        JSONObject finalCondition = new JSONObject();
        JSONArray mainChildren = new JSONArray();

        Pattern groupPattern = Pattern.compile("\\(([^()]+)\\)"); // Extract inner conditions
        Matcher matcher = groupPattern.matcher(condition);

        while (matcher.find()) {
            String subCondition = matcher.group(1);
            if (subCondition.contains("contains")) {
                mainChildren.put(parseContainsCondition(subCondition));
            } else if (subCondition.contains("equals")) {
                mainChildren.put(parseEqualsCondition(subCondition));
            } else if (subCondition.contains("NOT_EQUALS")) {
                mainChildren.put(parseNotEqualsCondition(subCondition));
            }
        }

        finalCondition.put("children", mainChildren);
        finalCondition.put("operation", condition.contains("AND") ? "AND" : "OR");

        return finalCondition;
    }

    private static JSONObject parseContainsCondition(String condition) {
        JSONArray children = new JSONArray();
        Matcher matcher = Pattern.compile("\"?(.*?)\"? contains \"?([^\"]*?)\"?").matcher(condition);

        if (matcher.find()) {
            String attribute = matcher.group(1).trim();
            String[] values = matcher.group(2).split(" OR ");

            for (String value : values) {
                children.put(createCriteria("attribute." + attribute, "IDENTITY", "CONTAINS", sanitizeValue(value.trim())));
            }
        }

        JSONObject containsCondition = new JSONObject();
        containsCondition.put("children", children);
        containsCondition.put("operation", "OR");
        return containsCondition;
    }

    private static JSONObject parseEqualsCondition(String condition) {
        Matcher matcher = Pattern.compile("\"?(.*?)\"? equals \"?([^\"]*?)\"?").matcher(condition);

        if (matcher.find()) {
            String attribute = matcher.group(1).trim();
            String value = sanitizeValue(matcher.group(2).trim());
            return createCriteria("attribute." + attribute, "IDENTITY", "EQUALS", value);
        }
        return new JSONObject();
    }

    private static JSONObject parseNotEqualsCondition(String condition) {
        Matcher matcher = Pattern.compile("\"?(.*?)\"? NOT_EQUALS \"?([^\"]*?)\"?").matcher(condition);

        if (matcher.find()) {
            String attribute = matcher.group(1).trim();
            String value = sanitizeValue(matcher.group(2).trim());
            return createCriteria("attribute." + attribute, "IDENTITY", "NOT_EQUALS", value);
        }
        return new JSONObject();
    }

    private static JSONObject createCriteria(String property, String type, String operation, String value) {
        JSONObject key = new JSONObject();
        key.put("property", property);
        key.put("type", type);

        JSONObject condition = new JSONObject();
        condition.put("key", key);
        condition.put("operation", operation);
        condition.put("value", value);

        return condition;
    }

    private static String sanitizeValue(String value) {
        // Preserve semicolons (if present)
        if (value.startsWith(";") && value.endsWith(";")) {
            return value;  // Keep semicolon-wrapped values intact
        }
        return value.replaceAll("^\"|\"$", ""); // Remove leading and trailing quotes (if any)
    }
}
