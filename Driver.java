import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

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


    

    // FEATURE 02: SUMMARY STATS (age, bmi, children, charges) 
     static class Stats {

        long count = 0;
        double sum = 0.0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        void add(double v) {
            count++; sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }
        double avg() { return count == 0 ? 0.0 : sum / count; }
    }

    static Map<String, Stats> computeFeature02Stats(List<InsuranceRecord> records) {
        Stats age = new Stats(), bmi = new Stats(), children = new Stats(), charges = new Stats();
        for (InsuranceRecord r : records) {
            age.add(r.age);
            bmi.add(r.bmi);
            children.add(r.children);
            charges.add(r.charges);
        }
        Map<String, Stats> map = new LinkedHashMap<>();
        map.put("age", age);
        map.put("bmi", bmi);
        map.put("children", children);
        map.put("charges", charges);
        return map;
    }

    static void printFeature02(Map<String, Stats> stats) {
        System.out.println("\n=== Feature 02: Stats for age, bmi, children, and charges ===");
        System.out.printf("%-10s %8s %12s %12s %12s%n", "column", "count", "min", "max", "avg");
        System.out.println("--------------------------------------------------------------");
        for (Map.Entry<String, Stats> e : stats.entrySet()) {
            Stats s = e.getValue();
            System.out.printf("%-10s %8d %12.2f %12.2f %12.2f%n",
                    e.getKey(), s.count, s.min, s.max, s.avg());
        }
    }

    static void printChildrenCounts(Map<Integer,Integer> counts) {
        System.out.println("\nTotal records by number of children:");
        for (Map.Entry<Integer,Integer> e : counts.entrySet()) {
            System.out.printf("children=%d -> %d record(s)%n", e.getKey(), e.getValue());
        }
    }


    // === Feature 04: vertical BMI histogram ===
    public static Map<Integer, Integer> feature04_bmiBins(List<InsuranceRecord> records, int binSize) {
        Map<Integer, Integer> bins = new TreeMap<>();
        for (InsuranceRecord r : records) {
            int b = ((int) Math.floor(r.bmi / binSize)) * binSize;
            bins.put(b, bins.getOrDefault(b, 0) + 1);
        }
        return bins;
    }

    public static void printFeature04(Map<Integer, Integer> bins) {
        int peak = 1;
        for (int v : bins.values()) peak = Math.max(peak, v);

        for (int level = peak; level >= 1; level--) {
            StringBuilder row = new StringBuilder();
            for (int b : bins.keySet()) {
                int count = bins.get(b);
                row.append(count >= level ? " # " : "   ");
            }
            System.out.println(row);
        }
        StringBuilder base = new StringBuilder();
        for (int b : bins.keySet()) {
            base.append(String.format("%2d ", b));
        }
        System.out.println(base);
    }


    // === Feature 06: smokers vs non-smokers (vertical histogram) ===
    public static Map<String, Integer> feature06_smokerCounts(List<InsuranceRecord> records) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("smoker", 0);
        counts.put("non-smoker", 0);
        for (InsuranceRecord r : records) {
            if ("yes".equalsIgnoreCase(r.smoker)) {
                counts.put("smoker", counts.get("smoker") + 1);
            } else {
                counts.put("non-smoker", counts.get("non-smoker") + 1);
            }
        }
        return counts;
    }

    public static void printFeature06(Map<String, Integer> counts) {
        int max = 1;
        for (int v : counts.values()) max = Math.max(max, v);

        for (int level = max; level >= 1; level--) {
            StringBuilder row = new StringBuilder();
            for (String k : counts.keySet()) {
                int c = counts.get(k);
                row.append(c >= level ? " # " : "   ");
            }
            System.out.println(row);
        }
        System.out.println(" S   NS ");
    }

    // === Feature 08: charges >=50 vs <=20 ===
    public static boolean feature08_oldVsYoungCharges(List<InsuranceRecord> records) {
        double oldSum = 0.0;
        double youngSum = 0.0;
        int oldCount = 0;
        int youngCount = 0;

        for (InsuranceRecord r : records) {
            if (r.age >= 50) { oldSum += r.charges; oldCount++; }
            if (r.age <= 20) { youngSum += r.charges; youngCount++; }
        }
        if (oldCount == 0 || youngCount == 0) return false;

        double oldAvg = oldSum / oldCount;
        double youngAvg = youngSum / youngCount;
        
        return oldAvg >= 2.0 * youngAvg;
    }

    

// === Feature 10: more children ⇒ lower charge per child ===
public static boolean feature10_lowerChargePerChild(List<InsuranceRecord> records) {
    Map<Integer, List<Double>> groups = new TreeMap<>();
    for (InsuranceRecord r : records) {
        groups.computeIfAbsent(r.children, k -> new ArrayList<>()).add(r.charges);
    }

    double prev = Double.MAX_VALUE; // we expect per-child avg to be nonincreasing as children↑
    for (int c : groups.keySet()) {
        List<Double> list = groups.get(c);
        double sum = 0.0;
        for (double v : list) sum += v;
        double avg = list.isEmpty() ? 0.0 : sum / list.size();
        double perChild = (c == 0) ? avg : avg / c;

        if (perChild > prev) return false; // broke the nonincreasing condition
        prev = perChild;
    }
    return true;
}





    // === Feature 12: south smokers ≥25% more ===
    public static boolean feature12_southSmokers(List<InsuranceRecord> records) {
        double southSum = 0.0;
        double otherSum = 0.0;
        int southCount = 0;
        int otherCount = 0;

        for (int i = 0; i < records.size(); i++) {
            InsuranceRecord r = records.get(i);
            if (r.smoker.equalsIgnoreCase("yes")) {
                String reg = r.region.toLowerCase();
                if (reg.indexOf("south") >= 0) {
                    southSum += r.charges;
                    southCount++;
                } else {
                    otherSum += r.charges;
                    otherCount++;
                }
            }
        }
        if (southCount == 0 || otherCount == 0) return false;

        double southAvg = southSum / southCount;
        double otherAvg = otherSum / otherCount;
        return southAvg >= 1.25 * otherAvg;
    }

     // === Feature 14: smoker age distribution ===
    public static Map<Integer, Integer> feature14_smokerAgeDist(List<InsuranceRecord> records) {
        Map<Integer, Integer> dist = new TreeMap<Integer, Integer>();
        for (int i = 0; i < records.size(); i++) {
            InsuranceRecord r = records.get(i);
            if (r.smoker.equalsIgnoreCase("yes")) {
                Integer cur = dist.get(r.age);
                if (cur == null) cur = 0;
                dist.put(r.age, cur + 1);
            }
        }
        return dist;
    }

    // === Feature 16: avg age smokers vs non-smokers ===
    public static Map<String, Double> feature16_avgAges(List<InsuranceRecord> records) {
        double smokerSum = 0.0;
        double nonSum = 0.0;
        int smokerCount = 0;
        int nonCount = 0;

        for (int i = 0; i < records.size(); i++) {
            InsuranceRecord r = records.get(i);
            if (r.smoker.equalsIgnoreCase("yes")) {
                smokerSum += r.age;
                smokerCount++;
            } else {
                nonSum += r.age;
                nonCount++;
            }
        }
        Map<String, Double> out = new LinkedHashMap<String, Double>();
        double sAvg = 0.0;
        double nAvg = 0.0;
        if (smokerCount != 0) sAvg = smokerSum / smokerCount;
        if (nonCount != 0) nAvg = nonSum / nonCount;
        out.put("smoker_avg_age", sAvg);
        out.put("nonsmoker_avg_age", nAvg);
        return out;
    }

    // === Feature 18: avg BMI south vs north ===
    public static Map<String, Double> feature18_bmiSouthNorth(List<InsuranceRecord> records) {
        double southSum = 0.0;
        double northSum = 0.0;
        int southCount = 0;
        int northCount = 0;

        for (int i = 0; i < records.size(); i++) {
            InsuranceRecord r = records.get(i);
            String reg = r.region.toLowerCase();
            if (reg.indexOf("south") >= 0) {
                southSum += r.bmi;
                southCount++;
            } else if (reg.indexOf("north") >= 0) {
                northSum += r.bmi;
                northCount++;
            }
        }
        Map<String, Double> out = new LinkedHashMap<String, Double>();
        double sAvg = 0.0;
        double nAvg = 0.0;
        if (southCount != 0) sAvg = southSum / southCount;
        if (northCount != 0) nAvg = northSum / northCount;
        out.put("south_avg_bmi", sAvg);
        out.put("north_avg_bmi", nAvg);
        return out;
    }


     // === Feature 20: regression charges ~ BMI ===
    public static void feature20_regressionBMI(List<InsuranceRecord> records) {
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;

        int n = records.size();
        for (int i = 0; i < n; i++) {
            InsuranceRecord r = records.get(i);
            sumX += r.bmi;
            sumY += r.charges;
            sumXY += r.bmi * r.charges;
            sumX2 += r.bmi * r.bmi;
            sumY2 += r.charges * r.charges;
        }

        double denomSlope = (n * sumX2 - sumX * sumX);
        if (denomSlope == 0.0) {
            System.out.println("Cannot compute regression: degenerate X variance.");
            return;
        }
        double slope = (n * sumXY - sumX * sumY) / denomSlope;
        double intercept = (sumY - slope * sumX) / n;

        double rNum = n * sumXY - sumX * sumY;
        double rDenTermX = n * sumX2 - sumX * sumX;
        double rDenTermY = n * sumY2 - sumY * sumY;
        double rDen = Math.sqrt(rDenTermX * rDenTermY);
        double r = 0.0;
        if (rDen != 0.0) r = rNum / rDen;

        System.out.printf("y = %.2f + %.2f*x, r=%.3f%n", intercept, slope, r);

        for (int i = 0; i <= 10; i++) {
            double x = 15 + i * 3.0; // BMI values
            double pred = intercept + slope * x;
            System.out.printf("BMI %.1f => charges %.2f%n", x, pred);
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

            // Feature 02: summary stats
            Map<String, Stats> stats = computeFeature02Stats(records);
            printFeature02(stats);


            // Feature 04: BMI vertical histogram (bin=5)
            Map<Integer, Integer> bmiBins = feature04_bmiBins(records, 5);
            System.out.println("\n=== Feature 04: BMI Vertical Histogram (bin=5) ===");
            printFeature04(bmiBins);

            // Feature 06: smokers vs non-smokers
            Map<String, Integer> smokeCounts = feature06_smokerCounts(records);
            System.out.println("\n=== Feature 06: Smokers vs Non-Smokers (Vertical) ===");
            printFeature06(smokeCounts);

            
        
            //Feature 08
            System.out.println("\n=== Feature 08: Avg charges age>=50 at least 2x age<=20 ? ===");
            boolean f08 = Driver.feature08_oldVsYoungCharges(records);
            if (f08) System.out.println("TRUE");
            else System.out.println("FALSE");

        //  Feature 10
        System.out.println("\n=== Feature 10: More children ⇒ lower charge per child (monotone) ? ===");
        boolean f10 = Driver.feature10_lowerChargePerChild(records);
        if (f10) System.out.println("TRUE");
        else System.out.println("FALSE");

        // Feature 12
        System.out.println("\n=== Feature 12: South smokers pay ≥25% more than other smokers ? ===");
        boolean f12 = Driver.feature12_southSmokers(records);
        if (f12) System.out.println("TRUE");
        else System.out.println("FALSE");

        // Feature 14
        System.out.println("\n=== Feature 14: Smoker Age Distribution (age -> count) ===");
        Map<Integer, Integer> f14 = Driver.feature14_smokerAgeDist(records);
        // simple print
        Iterator<Map.Entry<Integer, Integer>> it14 = f14.entrySet().iterator();
        while (it14.hasNext()) {
            Map.Entry<Integer, Integer> e = it14.next();
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
        //  Feature 16
        System.out.println("\n=== Feature 16: Avg Age (smokers vs non-smokers) ===");
        Map<String, Double> f16 = Driver.feature16_avgAges(records);
        System.out.printf("smoker_avg_age: %.2f%n", f16.get("smoker_avg_age"));
        System.out.printf("nonsmoker_avg_age: %.2f%n", f16.get("nonsmoker_avg_age"));

        // Feature 18
        System.out.println("\n=== Feature 18: Avg BMI (south vs north) ===");
        Map<String, Double> f18 = Driver.feature18_bmiSouthNorth(records);
        System.out.printf("south_avg_bmi: %.2f%n", f18.get("south_avg_bmi"));
        System.out.printf("north_avg_bmi: %.2f%n", f18.get("north_avg_bmi"));

        // Feature 20
        System.out.println("\n=== Feature 20: Regression charges ~ BMI ===");
        Driver.feature20_regressionBMI(records);



            

            // Additional histograms (ages)

            bmiBins = Driver.feature04_bmiBins(records, 5);
            System.out.println("\n=== Feature 04: BMI Vertical Histogram (bin=5) ===");
            Driver.printFeature04(bmiBins);
 

            // --- histograms ---

            List<Integer> ages = agesFrom(records);
            printPerAgeHistogram(ages, 50);
            printBinnedHistogram(ages, 5, 50);

            // Children counts
            Map<Integer,Integer> byChildren = childrenCounts(records);
            printChildrenCounts(byChildren);

        } catch (IOException e) {;
            System.exit(1);
        }
    }

}


          //  System.err.println("I/O error: " + e.getMessage())