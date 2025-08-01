import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PolynomialSolver {

    static class Point {
        int x;
        BigInteger y;
        public Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PolynomialSolver <input.json>");
            return;
        }

        try {
            // Read and clean JSON file
            String json = readAndCleanJsonFile(args[0]);
            
            // Extract keys section
            String keysSection = extractJsonSection(json, "keys");
            if (keysSection == null) {
                System.out.println("Error: Could not find 'keys' section in JSON");
                return;
            }
            
            // Parse n and k
            int n = extractJsonInt(keysSection, "n");
            int k = extractJsonInt(keysSection, "k");
            
            // Parse all points
            List<Point> points = new ArrayList<>();
            int pointCount = 0;
            
            while (pointCount < n) {
                pointCount++;
                String pointSection = extractJsonSection(json, Integer.toString(pointCount));
                if (pointSection == null) continue;
                
                String base = extractJsonString(pointSection, "base");
                String value = extractJsonString(pointSection, "value");
                BigInteger y = convertToDecimal(value, base);
                points.add(new Point(pointCount, y));
            }
            
            // Validate
            if (points.size() < k) {
                System.out.printf("Error: Need %d points but found %d%n", k, points.size());
                return;
            }
            
            // Calculate
            BigInteger C = calculateConstantTerm(points, k);
            System.out.println("The secret constant C is: " + C);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String readAndCleanJsonFile(String path) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Remove all whitespace for simpler parsing
                content.append(line.replaceAll("\\s", ""));
            }
        }
        return content.toString();
    }

    private static String extractJsonSection(String json, String key) {
        // Pattern to match "key":{...}
        String pattern = "\"" + key + "\":\\{([^}]*)\\}";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static int extractJsonInt(String section, String key) throws Exception {
        String pattern = "\"" + key + "\":(\\d+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(section);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        throw new Exception("Missing or invalid integer value for key: " + key);
    }

    private static String extractJsonString(String section, String key) throws Exception {
        String pattern = "\"" + key + "\":\"([^\"]*)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(section);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception("Missing string value for key: " + key);
    }

    private static BigInteger convertToDecimal(String value, String baseStr) throws Exception {
        BigInteger base = new BigInteger(baseStr);
        BigInteger result = BigInteger.ZERO;
        String digits = "0123456789abcdef";
        
        for (char c : value.toLowerCase().toCharArray()) {
            int digit = digits.indexOf(c);
            if (digit < 0 || digit >= base.intValue()) {
                throw new Exception("Invalid digit '" + c + "' for base " + base);
            }
            result = result.multiply(base).add(BigInteger.valueOf(digit));
        }
        return result;
    }

    private static BigInteger calculateConstantTerm(List<Point> points, int k) {
        BigInteger C = BigInteger.ZERO;
        
        for (int i = 0; i < k; i++) {
            BigInteger term = points.get(i).y;
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xi = BigInteger.valueOf(points.get(i).x);
                    BigInteger xj = BigInteger.valueOf(points.get(j).x);
                    term = term.multiply(xj.negate())
                              .divide(xi.subtract(xj));
                }
            }
            C = C.add(term);
        }
        return C;
    }
}