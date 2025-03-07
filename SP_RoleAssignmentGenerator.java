import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SailpointCSVParser {

    public static void main(String[] args) {
        String inputFilePath = "roles_input.csv";  // Input CSV file (RoleName, Criteria)
        String outputFilePath = "roles_output.csv"; // Output CSV file (RoleName, JSON Criteria)

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

                // Debugging Output
                System.out.println("✅ Processed: " + roleName);
            }

            System.out.println("✅ CSV processing complete. Output written to: " + outputFilePath);

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
        Pattern pattern = Pattern.compile("\"?(.*?)\"?\\s*contains\\s*\"?(.*?)\"?");
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String attribute = matcher.group(1).trim();
            String[] values = matcher.group(2).split(" OR ");

            for (String value : values) {
                value = sanitizeValue(value.trim());

                // Debugging Output
                System.out.println("🔍 CONTAINS Matched: Attribute = " + attribute + ", Value = " + value);

                children.put(createCriteria("attribute." + attribute, "IDENTITY", "CONTAINS", value));
            }
        } else {
            System.out.println("❌ No match found for: " + condition);
        }

        JSONObject containsCondition = new JSONObject();
        containsCondition.put("children", children);
        containsCondition.put("operation", "OR");
        return containsCondition;
    }

    private static JSONObject parseEqualsCondition(String condition) {
        Pattern pattern = Pattern.compile("\"?(.*?)\"?\\s*equals\\s*\"?(.*?)\"?");
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String attribute = matcher.group(1).trim();
            String value = sanitizeValue(matcher.group(2).trim());

            // Debugging Output
            System.out.println("🔍 EQUALS Matched: Attribute = " + attribute + ", Value = " + value);

            return createCriteria("attribute." + attribute, "IDENTITY", "EQUALS", value);
        } else {
            System.out.println("❌ No match found for: " + condition);
        }

        return new JSONObject();
    }

    private static JSONObject parseNotEqualsCondition(String condition) {
        Pattern pattern = Pattern.compile("\"?(.*?)\"?\\s*NOT_EQUALS\\s*\"?(.*?)\"?");
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String attribute = matcher.group(1).trim();
            String value = sanitizeValue(matcher.group(2).trim());

            // Debugging Output
            System.out.println("🔍 NOT_EQUALS Matched: Attribute = " + attribute + ", Value = " + value);

            return createCriteria("attribute." + attribute, "IDENTITY", "NOT_EQUALS", value);
        } else {
            System.out.println("❌ No match found for: " + condition);
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
        // Preserve semicolon-wrapped values
        if (value.startsWith(";") && value.endsWith(";")) {
            return value;
        }
        // Preserve numeric values
        if (value.matches("^\\d+$")) {
            return value;
        }
        return value.replaceAll("^\"|\"$", ""); // Remove leading and trailing quotes
    }
}
