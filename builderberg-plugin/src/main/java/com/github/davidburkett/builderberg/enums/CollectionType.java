package com.github.davidburkett.builderberg.enums;

public enum CollectionType {
    COLLECTION("java.util.Collection", "unmodifiableCollection"),
    LIST("java.util.List", "unmodifiableList"),
    SET("java.util.Set", "unmodifiableSet"),
    SORTED_SET("java.util.SortedSet", "unmodifiableSortedSet"),
    NAVIGABLE_SET("java.util.NavigableSet", "unmodifiableNavigableSet"),
    MAP("java.util.Map", "unmodifiableMap"),
    SORTED_MAP("java.util.SortedMap", "unmodifiableSortedMap"),
    NAVIGABLE_MAP("java.util.NavigableMap", "unmodifiableNavigableMap");

    private String canonicalName;
    private String unmodifiableMethod;

    CollectionType(final String canonicalName, final String unmodifiableMethod) {
        this.canonicalName = canonicalName;
        this.unmodifiableMethod = unmodifiableMethod;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getUnmodifiableMethod() {
        return "java.util.Collections." + unmodifiableMethod;
    }
}
