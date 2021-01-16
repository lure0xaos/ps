package gargoyle.ps.storage.impl;

import gargoyle.ps.record.IPSRecord;
import gargoyle.ps.record.impl.PSRecord;
import gargoyle.ps.storage.IPSRecordStorage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;

public final class PSRecordStorage implements IPSRecordStorage {

    private static final Pattern PATTERN_KEY_VALUE = Pattern.compile("\\s*[:=]\\s+");

    private static final char CHAR_TAB = '\t';

    private static final char CHAR_COLON = ':';

    private static final String COMMENTS = "-#;/";

    private Path path;

    private boolean dirty;

    private IPSRecord root;

    private PSRecordStorage(Path path) {
        this.path = path;
    }

    public static IPSRecordStorage create(Path path) {
        return new PSRecordStorage(path);
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public IPSRecord getRoot() {
        return root;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void makeDirty() {
        dirty = true;
    }

    @Override
    public void load() {
        root = PSRecord.createRootGroup();
        IPSRecord currentParent = root;
        if(Files.exists(path))
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            IPSRecord prev = root;
            int curIndent = 0;
            for (String line : lines) {
                if (line == null || line.trim().isEmpty() || COMMENTS.contains(line.trim().substring(0, 1))) {
                    continue;
                }
                String[] pairs = PATTERN_KEY_VALUE.split(line, 2);
                if (pairs.length == 0) {
                    continue;
                }
                String pairName = pairs[0];
                if (pairName.isEmpty()) {
                    continue;
                }
                int indent = 0;
                while (pairName.charAt(indent) == CHAR_TAB) {
                    indent++;
                }
                String name = pairName.trim();
                String value = pairs.length > 1 ? pairs[1].trim() : null;
                IPSRecord rec = PSRecord.createValue(name, value);
                int sub = indent - curIndent;
                if (sub == 0) {
                    currentParent.addChild(rec);
                }
                if (sub > 0) {
                    currentParent = prev;
                    currentParent.addChild(rec);
                }
                if (sub < 0) {
                    for (int i = sub; i < 0; i++) {
                        currentParent = currentParent.getParent();
                    }
                    currentParent.addChild(rec);
                }
                curIndent = indent;
                prev = rec;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        dirty = false;
    }

    @Override
    public void save() {
        doSave(path);
    }

    @Override
    public void save(Path path) {
        this.path = path;
        doSave(path);
    }

    private void doSave(Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            for (IPSRecord record : root.getChildren()) {
                write(writer, 0, record);
            }
            writer.flush();
            dirty = false;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void write(BufferedWriter writer, int indent, IPSRecord record) throws IOException {
        if (record.getName() == null) {
            return;
        }
        if (indent != 0) {
            for (int i = 0; i < indent; i++) {
                writer.write(CHAR_TAB);
            }
        }
        writer.write(record.getName());
        if (record.getValue() != null) {
            writer.write(CHAR_COLON);
            writer.write(CHAR_TAB);
            writer.write(record.getValue());
        }
        writer.newLine();
        for (IPSRecord childRecord : record.getChildren()) {
            write(writer, indent + 1, childRecord);
        }
    }
}
