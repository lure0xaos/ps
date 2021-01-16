package gargoyle.ps.record;

import java.util.List;

public interface IPSRecord {

    String getName();

    void setName(String name);

    IPSRecord getParent();

    void setParent(IPSRecord parent);

    String getValue();

    void setValue(String value);

    String getPath();

    List<IPSRecord> getChildren();

    boolean hasChildren();

    boolean isLeaf();

    void addChild(IPSRecord record);

    void removeChild(IPSRecord record);
}
