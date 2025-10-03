package MedicalInsuranceData.insurance.io;
import com.example.insurance.model.InsuranceRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public final class CsvInsuranceDataSource implements InsuranceDataSource {
    @Override
    public List<InsuranceRecord> readFirstN(Path csvPath, int n) throws IOException {
        if (n <= 0) throw new IllegalArgumentException("n must be > 0");
        List<InsuranceRecord> out = new ArrayList<>(n);
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String header = br.readLine();
            if (header == null) throw new IOException("Empty CSV");
            String line;
            while ((line = br.readLine()) != null && out.size() < n) {
                if (line.isEmpty()) continue;
                String[] p = line.split(",", -1);
                if (p.length < 7) continue;
                try {
                    out.add(new InsuranceRecord(
                        Integer.parseInt(p[0].trim()),
                        p[1].trim(),
                        Double.parseDouble(p[2].trim()),
                        Integer.parseInt(p[3].trim()),
                        p[4].trim(),
                        p[5].trim(),
                        Double.parseDouble(p[6].trim())
                    ));
                } catch (NumberFormatException ignore) {}
            }
        }
        return out;
    }
}
