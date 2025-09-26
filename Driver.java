import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.*;


public class Driver {
        public static void main(String[] args) {
                System.out.println("1. Write code that stores the first N records of the dataset in some custom object.");
                

                class InsuranceRecord{
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
                return String.format("Age: %d | Sex: %s | BMI: %.2f | Children: %d | Smoker: %s | Region: %s | Charges: %.2f",
                        age, sex, bmi, children, smoker, region, charges);
        }
        }

        public class InsuranceDataset {
        public static void main(String[] args) {
                if (args.length != 2) {
                System.err.println("Usage: java InsuranceDataset <path-to-insurance.csv> <N>");
                System.exit(1);
                }

                String path = args[0];
                int N = Integer.parseInt(args[1]);

                List<InsuranceRecord> records = new ArrayList<>();

                try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
                String line = br.readLine(); // skip header
                if (line == null) {
                        System.err.println("Error: Empty file.");
                        return;
                }

                int count = 0;
                while ((line = br.readLine()) != null && count < N) {
                        String[] parts = line.split(",");
                        if (parts.length == 7) {
                        InsuranceRecord rec = new InsuranceRecord(
                                Integer.parseInt(parts[0].trim()),   // age
                                parts[1].trim(),                     // sex
                                Double.parseDouble(parts[2].trim()), // bmi
                                Integer.parseInt(parts[3].trim()),   // children
                                parts[4].trim(),                     // smoker
                                parts[5].trim(),                     // region
                                Double.parseDouble(parts[6].trim())  // charges
                        );
                        records.add(rec);
                        count++;
                        }
                }

                } catch (IOException e) {
                System.err.println("I/O Error: " + e.getMessage());
                }

                // Print stored records
                System.out.println("Stored " + records.size() + " records:");
                for (InsuranceRecord r : records) {
                System.out.println(r);
                }
        }
}




                System.out.println("3. Write code that displays a horizontal text-based histogram of the ages. Remember, you can't use any plotting libraries.");
                System.out.println("5. Write code that determines the total number of records for each number of children.");
                
        }
}
