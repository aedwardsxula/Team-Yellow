package MedicalInsuranceData.insurance.io;

import com.example.insurance.model.InsuranceRecord;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InsuranceDataSource {
    List<InsuranceRecord> readFirstN(Path csvPath, int n) throws IOException;
}