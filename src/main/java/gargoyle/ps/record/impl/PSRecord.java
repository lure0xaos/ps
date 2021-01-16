package gargoyle.ps.record.impl;

import gargoyle.ps.record.IPSRecord;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PSRecord implements IPSRecord {

    private static final char CHAR_DOT = '.';

    private final List<IPSRecord> children = new ArrayList<>();

    private String name;

    private String value;

    private IPSRecord parent;

    private PSRecord(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static IPSRecord createValue(String name, String value) {
        return new PSRecord(name, value);
    }

    public static IPSRecord createGroup(String name) {
        return createValue(name, null);
    }

    public static IPSRecord createRootGroup() {
        return createValue("", null);
    }

    public static IPSRecord createValue(String name) {
        return createValue(name, "");
    }

    @Override
    public List<IPSRecord> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public IPSRecord getParent() {
        return parent;
    }

    @Override
    public void setParent(IPSRecord parent) {
        if (this.parent != null) {
            List<IPSRecord> children = this.parent.getChildren();
            if (children.contains(this)) {
                children.remove(this);
            }
        }
        if (parent != null) {
            List<IPSRecord> children = parent.getChildren();
            if (!children.contains(this)) {
                children.add(this);
            }
        }
        this.parent = parent;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getPath() {
        IPSRecord item = this;
        StringBuilder path = new StringBuilder();
        while (item != null) {
            path.insert(0, item.getName());
            item = item.getParent();
            if (item != null) {
                path.insert(0, CHAR_DOT);
            }
        }
        return path.substring(1);
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public boolean isLeaf() {
        return value != null;
    }

    @Override
    public void addChild(IPSRecord record) {
        if (!children.contains(record)) {
            children.add(record);
        }
        record.setParent(this);
    }

    @Override
    public void removeChild(IPSRecord record) {
        if (children.contains(record)) {
            children.remove(record);
        }
        record.setParent(null);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IPSRecord other = (IPSRecord) obj;
        if (name == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!Objects.equals(name, other.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("\"{0}\"{1}", name, value == null ? "" : MessageFormat.format("=\"{0}\"", value));
    }
}
