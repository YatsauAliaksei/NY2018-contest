package by.ny2018;

public class Launcher {

    public static void main(String[] args) {

        WebBandog webBandog = new WebBandog("https://jprof.by"); // args[0]
        int count = webBandog.findWords("java"); // args[]
        System.out.println("Total: " + count);

    }
}
