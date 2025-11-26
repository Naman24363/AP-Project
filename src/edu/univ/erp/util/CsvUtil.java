package edu.univ.erp.util;

public class CsvUtil {
    public static String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) return "\"" + s.replace("\"","\"\"") + "\"";
        return s;
    }
}
