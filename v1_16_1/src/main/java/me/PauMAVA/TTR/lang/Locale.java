package me.PauMAVA.TTR.lang;

public class Locale {
    private final String name;
    private final String shortName;
    private final String author;

    public Locale(String name, String shortName, String author) {
        this.name = name;
        this.shortName = shortName;
        this.author = author;
    }

    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getAuthor() { return author; }
}