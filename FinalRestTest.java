import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;

public class FinalRestTest {
    public static void main(String[] args) {
        testVariableResolutionInUrlAndParams();
        testJsonFormatting();
    }

    private static void testVariableResolutionInUrlAndParams() {
        System.out.println("Testing Variable Resolution...");
        Map<String, String> env = new HashMap<>();
        env.put("host", "api.example.com");
        env.put("id", "123");

        String baseUrl = "https://{{host}}/posts/{{id}}";
        Map<String, String> params = new HashMap<>();
        params.put("user_{{id}}", "val_{{host}}");

        String resolvedUrl = resolveVariables(baseUrl, env);
        StringBuilder urlBuilder = new StringBuilder(resolvedUrl);
        urlBuilder.append("?");

        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.append(URLEncoder.encode(resolveVariables(entry.getKey(), env), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(resolveVariables(entry.getValue(), env), "UTF-8"))
                        .append("&");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String finalUrl = urlBuilder.toString();
        if (finalUrl.endsWith("&")) finalUrl = finalUrl.substring(0, finalUrl.length() - 1);

        System.out.println("Final URL: " + finalUrl);
        assert finalUrl.equals("https://api.example.com/posts/123?user_123=val_api.example.com");
        System.out.println("Variable resolution test passed!");
    }

    private static void testJsonFormatting() {
        System.out.println("\nTesting JSON Formatting...");
        String json = "{\"a\":1,\"b\":[1,2],\"c\":{\"d\":3}}";
        String formatted = formatJson(json);
        System.out.println("Formatted:\n" + formatted);

        // Basic check for indentation and newlines
        assert formatted.contains("\n  \"a\": 1,");
        assert formatted.contains("\n  \"b\": [\n    1,");
        System.out.println("JSON formatting test passed!");
    }

    private static String resolveVariables(String text, Map<String, String> env) {
        if (text == null || text.isEmpty()) return text;
        for (Map.Entry<String, String> entry : env.entrySet()) {
            text = text.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return text;
    }

    private static String formatJson(String json) {
        if (json == null || json.trim().isEmpty()) return json;
        try {
            StringBuilder formatted = new StringBuilder();
            int indentLevel = 0;
            boolean inString = false;
            boolean isEscaped = false;
            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);
                if (isEscaped) { formatted.append(c); isEscaped = false; continue; }
                if (c == '\\') { formatted.append(c); isEscaped = true; continue; }
                if (c == '"') { inString = !inString; formatted.append(c); continue; }
                if (inString) { formatted.append(c); continue; }
                if (c == '{' || c == '[') {
                    formatted.append(c).append('\n');
                    indentLevel++;
                    for (int j = 0; j < indentLevel; j++) formatted.append("  ");
                } else if (c == '}' || c == ']') {
                    formatted.append('\n');
                    indentLevel--;
                    for (int j = 0; j < indentLevel; j++) formatted.append("  ");
                    formatted.append(c);
                } else if (c == ',') {
                    formatted.append(c).append('\n');
                    for (int j = 0; j < indentLevel; j++) formatted.append("  ");
                } else if (c == ':') {
                    formatted.append(c).append(' ');
                } else if (!Character.isWhitespace(c)) {
                    formatted.append(c);
                }
            }
            return formatted.toString().trim();
        } catch (Exception e) { return json; }
    }
}
