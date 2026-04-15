public class ConsoleUtils {

    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 10; i++) {
                System.out.println();
            }
        }
    }public static void enableWindowsAnsi() {
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                // This small hack triggers the Windows terminal to interpret ANSI codes
                new ProcessBuilder("cmd", "/c", "echo off").start().waitFor();
            } catch (Exception ignored) {}
        }
    }
}