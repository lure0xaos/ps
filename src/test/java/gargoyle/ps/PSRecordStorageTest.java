package gargoyle.ps;

import gargoyle.ps.storage.IPSRecordStorage;
import gargoyle.ps.storage.impl.PSRecordStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class PSRecordStorageTest {
    private static final String DOCUMENTS = "Documents";
    private static final String PASS_TXT = "pass.txt";

    @Test
    public void testLoad() {
        IPSRecordStorage storage = PSRecordStorage.create(Paths.get(System.getProperty("user.home"), DOCUMENTS, PASS_TXT));
        storage.load();
        Assertions.assertNotNull(storage);
    }
}
