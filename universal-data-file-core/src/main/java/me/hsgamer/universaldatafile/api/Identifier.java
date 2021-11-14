package me.hsgamer.universaldatafile.api;

public interface Identifier {
    String START_FORMAT = "UDF>>>";
    String END_FORMAT = "UDF<<<";

    default String getStartFormat() {
        return START_FORMAT + getName();
    }

    default String getEndFormat() {
        return END_FORMAT + getName();
    }

    String getName();
}
