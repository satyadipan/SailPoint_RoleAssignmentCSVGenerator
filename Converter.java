import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CsvToJsonConverter {
    public static void main(String[] args) {
        String inputCsv = "input.csv";  // Your input CSV file
        String outputCsv = "output.csv"; // Output CSV file with JSON

        try (CSVReader reader = new CSVReader(new FileReader(inputCsv));
             FileWriter writer = new FileWriter(outputCsv)) {

            // Read CSV header (ignore first row)
            String[] headers = reader.readNext();

            // Write output CSV header
            writer.write("assignUsersToRole,Role Name,COMPLEX_CRITERIA,JSON\n");

            // Read CSV rows
            String[] row;
            while ((row = reader.readNext()) != null) {
                String roleName = row[0];
                List<ConditionGroup> groups = new ArrayList<>();
                String[] logicalOperators = row[row.length - 1].split(" ");

                // Read condition groups dynamically
                for (int i = 1; i < row.length - 1; i += 4) {
                    if (i + 3 >= row.length) break; // Ensure no out-of-bounds access
                    String source = row[i];
                    String attribute = row[i + 1];
                    String operator = row[i + 2];
                    String values = row[i + 3];

                    if (!source.isEmpty() && !attribute.isEmpty() && !operator.isEmpty() && !values.isEmpty()) {
                        groups.add(new ConditionGroup(source, attribute, operator, values));
                    }
                }

                // Convert to JSON
                String jsonCriteria = generateJson(groups, logicalOperators);

                // Write to output CSV
                writer.write(String.format("assignUsersToRole,%s,COMPLEX_CRITERIA,\"%s\"\n", roleName, jsonCriteria));
            }

            System.out.println("Output CSV generated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateJson(List<ConditionGroup> groups, String[] logicalOperators) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode children = root.putArray("children");

        for (int i = 0; i < groups.size(); i++) {
            ConditionGroup group = groups.get(i);
            if (group.hasMultipleValues()) {
                ObjectNode orNode = mapper.createObjectNode();
                ArrayNode orChildren = orNode.putArray("children");
                for (String value : group.getValuesArray()) {
                    orChildren.add(group.toJson(mapper, value));
                }
                orNode.put("operation", "OR");
                children.add(orNode);
            } else {
                children.add(group.toJson(mapper, group.values));
            }
        }

        root.put("operation", determineRootOperation(logicalOperators));
        return root.toString();
    }

    private static String determineRootOperation(String[] logicalOperators) {
        if (logicalOperators.length == 0) return "AND"; // Default
        boolean hasOr = false, hasAnd = false;
        for (String op : logicalOperators) {
            if ("OR".equalsIgnoreCase(op)) hasOr = true;
            if ("AND".equalsIgnoreCase(op)) hasAnd = true;
        }
        return (hasOr && hasAnd) ? "AND" : (hasOr ? "OR" : "AND");
    }

    static class ConditionGroup {
        String source, attribute, operator, values;

        ConditionGroup(String source, String attribute, String operator, String values) {
            this.source = source;
            this.attribute = attribute;
            this.operator = operator;
            this.values = values;
        }

        boolean hasMultipleValues() {
            return values.contains(" OR ");
        }

        String[] getValuesArray() {
            return values.split(" OR ");
        }

        ObjectNode toJson(ObjectMapper mapper, String value) {
            ObjectNode node = mapper.createObjectNode();
            ObjectNode keyNode = node.putObject("key");
            keyNode.put("property", "attribute." + attribute);
            keyNode.put("type", source.equalsIgnoreCase("IDENTITY") ? "IDENTITY" : "ACCOUNT");
            if (!source.equalsIgnoreCase("IDENTITY")) keyNode.put("sourceId", source);
            node.put("operation", operator);
            node.put("value", value);
            return node;
        }
    }
}