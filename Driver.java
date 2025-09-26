import java.io.*;
import java.util.*;

public class Driver {

    // ========== InsuranceRecord Class ==========
    static class InsuranceRecord {
        int age;
        double bmi;
        int children;
        String smoker;
        String region;
        double charges;

        InsuranceRecord(int age, double bmi, int children, String smoker, String region, double charges) {
            this.age = age;
            this.bmi = bmi;
            this.children = children;
            this.smoker = smoker;
            this.region = region;
            this.charges = charges;
        }
    }

    // ========== CSV Loader ==========
    public static List<InsuranceRecord> load(String filename, int limit) {
        List<InsuranceRecord> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean first = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                if (limit > 0 && count >= limit) break;

                String[] parts = line.split(",");
                if (parts.length < 7) continue;

                int age = Integer.parseInt(parts[0].trim());
                String sex = parts[1].trim(); // not used
                double bmi = Double.parseDouble(parts[2].trim());
                int children = Integer.parseInt(parts[3].trim());
                String smoker = parts[4].trim();
                String region = parts[5].trim();
                double charges = Double.parseDouble(parts[6].trim());

                records.add(new InsuranceRecord(age, bmi, children, smoker, region, charges));
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    // ========== Feature 02: Stats ==========
    public static Map<String, Map<String, Double>> stats(List<InsuranceRecord> rows) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        result.put("age", computeStats(rows, r -> r.age));
        result.put("bmi", computeStats(rows, r -> r.bmi));
        result.put("children", computeStats(rows, r -> r.children));
        result.put("charges", computeStats(rows, r -> r.charges));
        return result;
    }

    private static Map<String, Double> computeStats(List<InsuranceRecord> rows, ToDouble<InsuranceRecord> f) {
        List<Double> values = new ArrayList<>();
        for (InsuranceRecord r : rows) values.add(f.get(r));
        Collections.sort(values);

        double sum = 0;
        for (double v : values) sum += v;
        double mean = sum / values.size();

        double variance = 0;
        for (double v : values) variance += Math.pow(v - mean, 2);
        variance /= values.size();
        double std = Math.sqrt(variance);

        Map<String, Double> m = new LinkedHashMap<>();
        m.put("count", (double) values.size());
        m.put("mean", mean);
        m.put("std", std);
        m.put("min", values.get(0));
        m.put("p25", percentile(values, 25));
        m.put("p50", percentile(values, 50));
        m.put("p75", percentile(values, 75));
        m.put("max", values.get(values.size() - 1));
        return m;
    }

    private static double percentile(List<Double> vals, double p) {
        if (vals.isEmpty()) return 0;
        double idx = (p / 100.0) * (vals.size() - 1);
        int i = (int) idx;
        double frac = idx - i;
        if (i + 1 < vals.size()) {
            return vals.get(i) * (1 - frac) + vals.get(i + 1) * frac;
        } else {
            return vals.get(i);
        }
    }

    interface ToDouble<T> { double get(T x); }

    // ========== Feature 04: BMI Histogram ==========
    public static List<String> bmiHistogramVertical(List<InsuranceRecord> rows, double binSize) {
        List<Double> bmis = new ArrayList<>();
        for (InsuranceRecord r : rows) bmis.add(r.bmi);

        double min = Collections.min(bmis);
        double max = Collections.max(bmis);
        int bins = (int) Math.ceil((max - min) / binSize) + 1;
        int[] counts = new int[bins];

        for (double v : bmis) {
            int idx = (int) ((v - min) / binSize);
            counts[idx]++;
        }

        int maxCount = Arrays.stream(counts).max().orElse(1);
        List<String> output = new ArrayList<>();

        for (int level = maxCount; level > 0; level--) {
            StringBuilder sb = new StringBuilder();
            for (int c : counts) {
                sb.append(c >= level ? " | " : "   ");
            }
            output.add(sb.toString());
        }
        return output;
    }

    // ========== Feature 06: Smoker Histogram ==========
    public static List<String> smokerHistogramVertical(List<InsuranceRecord> rows) {
        int smokers = 0, nonsmokers = 0;
        for (InsuranceRecord r : rows) {
            if (r.smoker.equalsIgnoreCase("yes")) smokers++;
            else nonsmokers++;
        }

        int max = Math.max(smokers, nonsmokers);
        List<String> output = new ArrayList<>();

        for (int level = max; level > 0; level--) {
            StringBuilder sb = new StringBuilder();
            sb.append(smokers >= level ? " | " : "   ");
            sb.append(nonsmokers >= level ? " | " : "   ");
            output.add(sb.toString());
        }
        output.add(" S   N "); // labels
        return output;
    }

    // ========== MAIN ==========
    public static void main(String[] args) {
        System.out.println("Driver running...");

        List<InsuranceRecord> data = load("insurance (1).csv", 50);

        // Feature 02
        System.out.println("=== Feature 02: Stats ===");
        Map<String, Map<String, Double>> stats = stats(data);
        for (String col : stats.keySet()) {
            System.out.println(col + " -> " + stats.get(col));
        }
        System.out.println();

        // Feature 04
        System.out.println("=== Feature 04: BMI Histogram ===");
        for (String line : bmiHistogramVertical(data, 2.0)) {
            System.out.println(line);
        }
        System.out.println();

        // Feature 06
        System.out.println("=== Feature 06: Smoker Histogram ===");
        for (String line : smokerHistogramVertical(data)) {
            System.out.println(line);
        }
    }
}

