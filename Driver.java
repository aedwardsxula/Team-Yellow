import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
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

    // Also STATIC helper to load first N records
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
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
    }
}


