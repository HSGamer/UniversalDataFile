package me.hsgamer.universaldatafile;

public class TagSettings {
    public static final TagSettings DEFAULT = TagSettings.create("UDF");

    public final String startFormat;
    public final String endFormat;

    private TagSettings(String startFormat, String endFormat) {
        this.startFormat = startFormat;
        this.endFormat = endFormat;

        if (startFormat.isEmpty()) {
            throw new IllegalArgumentException("Start format cannot be empty");
        }
        if (endFormat.isEmpty()) {
            throw new IllegalArgumentException("End format cannot be empty");
        }
    }

    public static TagSettings create(String startFormat, String endFormat) {
        return new TagSettings(startFormat, endFormat);
    }

    public static TagSettings create(String name) {
        return new TagSettings(name + ">>>", name + "<<<");
    }

    public UniversalDataReader newReader() {
        return UniversalDataReader.create(this);
    }

    public UniversalDataWriter newWriter() {
        return UniversalDataWriter.create(this);
    }
}
