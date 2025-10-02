import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.nio.file.*;

public class Driver {

    static class InsuranceRecord {
        int age;
        String sex;
        double bmi;
        int children;
        String smoker;
        String region;
        double charges;

        InsuranceRecord(int age, String sex, double bmi, int children, String smoker, String region, double charges) {
            this.age = age;
            this.sex = sex;
            this.bmi = bmi;
            this.children = children;
            this.smoker = smoker;
            this.region = region;
            this.charges = charges;
        }

        @Override
        public String toString() {
            return String.format(
                "Age: %d | Sex: %s | BMI: %.2f | Children: %d | Smoker: %s | Region: %s | Charges: %.2f",
                age, sex, bmi, children, smoker, region, charges
            );
        }
    }

    // ---- Load first N records from CSV ----
    static List<InsuranceRecord> loadFirstN(String csvPath, int N) throws IOException {
        List<InsuranceRecord> out = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(csvPath))) {
            String header = br.readLine(); // skip header
            if (header == null) throw new IOException("Empty CSV (no header).");

            String line;
            int count = 0;
            while ((line = br.readLine()) != null && count < N) {
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", -1); // keep empty trailing fields
                if (parts.length < 7) continue;       // skip malformed lines

                InsuranceRecord r = new InsuranceRecord(
                    Integer.parseInt(parts[0].trim()),        // age
                    parts[1].trim(),                           // sex
                    Double.parseDouble(parts[2].trim()),       // bmi
                    Integer.parseInt(parts[3].trim()),         // children
                    parts[4].trim(),                           // smoker
                    parts[5].trim(),                           // region
                    Double.parseDouble(parts[6].trim())        // charges
                );
                out.add(r);
                count++;
            }
        }
        return out;
    }

    // ==== HISTOGRAM UTILITIES ====

    static List<Integer> agesFrom(List<InsuranceRecord> records) {
        List<Integer> ages = new ArrayList<>(records.size());
        for (InsuranceRecord r : records) ages.add(r.age);
        return ages;
    }

    static void printPerAgeHistogram(List<Integer> ages, int maxWidth) {
        if (ages.isEmpty()) { System.out.println("No ages to plot."); return; }
        Map<Integer,Integer> freq = new TreeMap<>();
        for (int a : ages) freq.merge(a, 1, Integer::sum);
        int maxCount = freq.values().stream().mapToInt(Integer::intValue).max().orElse(1);

        System.out.println("\nHorizontal Histogram (per age):");
        for (Map.Entry<Integer,Integer> e : freq.entrySet()) {
            int age = e.getKey(), count = e.getValue();
            System.out.printf("%3d: %s (%d)%n", age, bar(count, maxCount, maxWidth), count);
        }
    }

    static void printBinnedHistogram(List<Integer> ages, int binSize, int maxWidth) {
        if (ages.isEmpty()) { System.out.println("No ages to plot."); return; }
        int min = ages.stream().mapToInt(i -> i).min().orElse(0);
        int max = ages.stream().mapToInt(i -> i).max().orElse(0);

        int start = (int)Math.floor(min / (double)binSize) * binSize;
        int end   = (int)Math.ceil((max + 1) / (double)binSize) * binSize - 1;

        Map<String,Integer> bins = new LinkedHashMap<>();
        for (int lo = start; lo <= end; lo += binSize) {
            int hi = lo + binSize - 1;
            bins.put(String.format("%d-%d", lo, hi), 0);
        }

        for (int a : ages) {
            int lo = (a / binSize) * binSize;
            int hi = lo + binSize - 1;
            String label = String.format("%d-%d", lo, hi);
            if (!bins.containsKey(label)) {
                if (a < start) label = String.format("%d-%d", start, start + binSize - 1);
                else label = String.format("%d-%d", end - binSize + 1, end);
            }
            bins.put(label, bins.get(label) + 1);
        }

        int maxCount = bins.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int labelWidth = bins.keySet().stream().mapToInt(String::length).max().orElse(7);

        System.out.printf("\nHorizontal Histogram (bins, size=%d):%n", binSize);
        for (Map.Entry<String,Integer> e : bins.entrySet()) {
            String label = e.getKey();
            int count = e.getValue();
            System.out.printf("%" + labelWidth + "s: %s (%d)%n", label, bar(count, maxCount, maxWidth), count);
        }
    }

    static String bar(int count, int maxCount, int maxWidth) {
        if (count <= 0 || maxCount <= 0) return "";
        int len = (int)Math.round((count * 1.0 / maxCount) * maxWidth);
        len = Math.max(len, 1); // show at least one '#'
        char[] arr = new char[len];
        Arrays.fill(arr, '#');
        return new String(arr);
    }

    // ==== NEW: CHILDREN COUNTS ====

    /** Returns counts keyed by number of children (0,1,2,...) sorted ascending. */
    static Map<Integer,Integer> childrenCounts(List<InsuranceRecord> records) {
        Map<Integer,Integer> counts = new TreeMap<>();
        for (InsuranceRecord r : records) {
            counts.merge(r.children, 1, Integer::sum);
        }
        return counts;
    }

    static void printChildrenCounts(Map<Integer,Integer> counts) {
        System.out.println("\nTotal records by number of children:");
        for (Map.Entry<Integer,Integer> e : counts.entrySet()) {
            System.out.printf("children=%d -> %d record(s)%n", e.getKey(), e.getValue());
        }
    }

    // ==== MAIN ====
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Driver <path-to-insurance.csv> <N>");
            System.exit(2);
        }
        String path = args[0];
        int N;
        try {
            N = Integer.parseInt(args[1]);
            if (N <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.err.println("N must be a positive integer.");
            System.exit(2);
            return;
        }

        try {
            List<InsuranceRecord> records = loadFirstN(path, N);
            System.out.println("Stored " + records.size() + " records:");
            for (int i = 0; i < records.size(); i++) {
                System.out.printf("#%d %s%n", i + 1, records.get(i));
            }

            // --- histograms ---
            List<Integer> ages = agesFrom(records);
            printPerAgeHistogram(ages, 50);
            printBinnedHistogram(ages, 5, 50);

            // --- children counts ---
            Map<Integer,Integer> byChildren = childrenCounts(records);
            printChildrenCounts(byChildren);

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
    }
}
