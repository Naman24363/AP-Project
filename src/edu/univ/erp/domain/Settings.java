package edu.univ.erp.domain;

public class Settings {
    public String key;
    public String value;

    public Settings(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Settings{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
    }
}
