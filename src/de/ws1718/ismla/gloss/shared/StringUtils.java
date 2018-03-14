package de.ws1718.ismla.gloss.shared;

public class StringUtils {
    public static String[] split(String s, char c) {
        String[] res = new String[count(s, c) + 1];
        int i = s.indexOf(c);
        int j = -1;
        int n = 0;
        while (i >= 0) {
            res[n] = s.substring(j+1, i);
            n++;
            j = i;
            i = s.indexOf(c, j+1);
        }
        res[n] = s.substring(j+1);

        return res;
    }

    public static int count(String s, char c) {
        int n = 0;
        int i = s.indexOf(c);
        while (i >= 0) {
            n++;
            i = s.indexOf(c, i+1);
        }
        return n;
    }

    public static String join(String[] a, char c) {
        StringBuilder s = new StringBuilder();
        for (String p : a)
            s.append(p).append(c);
        return s.deleteCharAt(s.length()-1).toString();
    }
}
