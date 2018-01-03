package by.ny2018;

public class Launcher {

    public static void main(String[] args) {

        WebBandog webBandog = new WebBandog();
        int count = webBandog.findWords("http://jprof.by/", "java"); // args[0], args[1]
        System.out.println("Total: " + count);

    }
}
