package edu.montana.csci.csci468.parser;

import java.util.*;

public class CatscriptType {

    public static final CatscriptType INT = new CatscriptType("int", Integer.class);
    public static final CatscriptType STRING = new CatscriptType("string", String.class);
    public static final CatscriptType BOOLEAN = new CatscriptType("bool", Boolean.class);
    public static final CatscriptType OBJECT = new CatscriptType("object", Object.class);
    public static final CatscriptType NULL = new CatscriptType("null", Object.class);
    public static final CatscriptType VOID = new CatscriptType("void", Object.class);

    public static Map<String, CatscriptType> VALID_TYPES = Map.of(
            "int", INT,
            "string", STRING,
            "bool", BOOLEAN,
            "object", OBJECT,
            "null", NULL,
            "void", VOID
    );

    private final String name;
    private final Class javaClass;

    public CatscriptType(String name, Class javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    public static CatscriptType getCatscriptType(String type_name) {
        return VALID_TYPES.get(type_name);
    }


    // deal with the
    // if valid type continue, else

    public boolean isAssignableFrom(CatscriptType type) {
        if (type == VOID) {
            return false;
        } else if (type == NULL) {
            return true;
        } else if (this.javaClass.isAssignableFrom(type.javaClass)) {
            return true;
        }
        return false;
    }

    // TODO memoize this call
    // declare a hash map to live along the method, then when asked for a given list type, look
    // into the hash map and return it, otherwise create it, add it to the hashmap and return it
    static HashMap<CatscriptType, ListType> cache = new HashMap<>();
    public static CatscriptType getListType(CatscriptType type) {
        ListType listType = cache.get(type);
        if (listType == null) {
            listType = new ListType(type);
            cache.put(type, listType);
        }
        return new ListType(type);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatscriptType that = (CatscriptType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Class getJavaType() {
        return javaClass;
    }

    public static class ListType extends CatscriptType {
        private final CatscriptType componentType;

        public ListType(CatscriptType componentType) {
            super("list<" + componentType.toString() + ">", List.class);
            this.componentType = componentType;
        }

        @Override
        public boolean isAssignableFrom(CatscriptType type) {
            if (type == NULL) {
                return true;
            } else if (type instanceof ListType) {
                ListType otherList = (ListType) type;
                return this.componentType.isAssignableFrom(otherList.componentType);
            }
            return false;
        }

        public CatscriptType getComponentType() {
            return componentType;
        }

        @Override
        public String toString() {
            return super.toString() + "<" + componentType.toString() + ">";
        }
    }

}
