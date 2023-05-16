import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SimpleFileDatabase {
    private static final String METADATA_FILE = "metadata.txt";

    public static void createTable(String statement) {
        // Parse CREATE TABLE statement
        String[] tokens = statement.split("\\(");
        String tableName = tokens[0].replace("CREATE TABLE ", "").trim();
        System.out.println("Table Name "+tableName);
        String[] columns = tokens[1].replace(")", "").trim().split(",");

        // Store metadata in metadata file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(METADATA_FILE))) {
            writer.write(tableName + ":");
            for (String column : columns) {
                String[] columnTokens = column.trim().split(" ");
                String columnName = columnTokens[0];
                String columnType = columnTokens[1];
                writer.write(columnName + "=" + columnType + ",");
            }
            writer.newLine();
            System.out.println("Table created: " + tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insertIntoTable(String statement) {
        // Parse INSERT INTO statement
        String[] tokens = statement.split("VALUES");
        String tableName = tokens[0].replace("INSERT INTO ", "").trim();
        String[] values = tokens[1].replace("(", "").replace(")", "").trim().split(",");

        // Read metadata file to get column names and types
        String metadata = "";
        try {
            metadata = new String(Files.readAllBytes(Paths.get(METADATA_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Find the column names and types from metadata
        String[] tableMetadata = metadata.split(tableName + ":");
        String[] columnDefs = tableMetadata[1].split(",");
        String[] columnNames = new String[columnDefs.length];
        String[] columnTypes = new String[columnDefs.length];
        for (int i = 0; i < columnDefs.length-1; i++) {
            String[] columnTokens = columnDefs[i].split("=");
            columnNames[i] = columnTokens[0];
            columnTypes[i] = columnTokens[1];
        }

        // Check if the number of values matches the number of columns
        if (values.length != columnNames.length) {
            System.out.println("Number of values doesn't match the number of columns");
            return;
        }

        // Prepare the record to be inserted
        StringBuilder recordBuilder = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            recordBuilder.append(columnNames[i]).append("=").append(values[i].trim());
            if (i != columnNames.length - 1) {
                recordBuilder.append(",");
            }
        }
        String record = recordBuilder.toString();

        // Insert the record into the table file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableName + ".txt", true))) {
            writer.write(record);
            writer.newLine();
            System.out.println("Data inserted into table: " + tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Example usage
        String createStatement = "CREATE TABLE MyTable (col1 INTEGER, col2 STRING)";
        String insertStatement = "INSERT INTO MyTable VALUES (123, 'Hello')";

        createTable(createStatement);
        insertIntoTable(insertStatement);
    }
}
