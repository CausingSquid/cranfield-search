package ie.tcd.cranfield;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CranfieldParser {
    private CranfieldParser() {
    }

    static List<Map<String, String>> parse(Path file) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, StringBuilder> current = null;
        String field = null;

        for (String line : Files.readAllLines(file, StandardCharsets.ISO_8859_1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith(".I")) {
                if (current != null) {
                    records.add(toStrings(current));
                }
                current = new HashMap<>();
                current.put("I", new StringBuilder(trimmed.substring(2).trim()));
                field = null;
            } else if (trimmed.length() == 2 && trimmed.charAt(0) == '.'
                    && "TABW".indexOf(trimmed.charAt(1)) >= 0) {
                field = trimmed.substring(1);
                current.putIfAbsent(field, new StringBuilder());
            } else if (field != null && current != null) {
                current.get(field).append(line).append(' ');
            }
        }
        if (current != null) {
            records.add(toStrings(current));
        }
        return records;
    }

    private static Map<String, String> toStrings(Map<String, StringBuilder> record) {
        Map<String, String> out = new HashMap<>();
        record.forEach((k, v) -> out.put(k, v.toString().trim()));
        return out;
    }
}
