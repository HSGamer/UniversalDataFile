package me.hsgamer.universaldatafile;

public final class UniversalDataFile {
    private UniversalDataFile() {
        // EMPTY
    }

    public static UniversalDataReader reader() {
        return UniversalDataReader.create();
    }

    public static UniversalDataWriter writer() {
        return UniversalDataWriter.create();
    }
}
