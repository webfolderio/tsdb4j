package io.webfolder.tsdb4j;

import java.io.Serializable;

public class Tag implements Serializable, Comparable<Tag> {

    private static final long serialVersionUID = -3434406985795149411L;

    private final String name;

    private final String value;

    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tag other = (Tag) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public int compareTo(Tag o) {
        return name != null && o != null ? name.compareTo(o.name) : 1;
    }

    @Override
    public String toString() {
        return "Tag [name=" + name + ", value=" + value + "]";
    }
}
