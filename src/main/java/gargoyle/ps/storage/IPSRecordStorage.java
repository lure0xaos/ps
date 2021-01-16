package gargoyle.ps.storage;

import gargoyle.ps.record.IPSRecord;

import java.nio.file.Path;

public interface IPSRecordStorage {

    Path getPath();

    IPSRecord getRoot();

    boolean isDirty();

    void makeDirty();

    void load();

    void save();

    void save(Path path);
}
