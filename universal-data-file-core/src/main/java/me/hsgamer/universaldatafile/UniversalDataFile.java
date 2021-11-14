package me.hsgamer.universaldatafile;

public final class UniversalDataFile {
    private UniversalDataFile() {
        // EMPTY
    }

    public static UniversalDataReader reader() {
        return new UniversalDataReader();
    }

    public static UniversalDataWriter writer() {
        return new UniversalDataWriter();
    }
}
