public class texto {
    boolean text() {
        for (int i = 0; i < 5; i++) {
            System.out.println(i);
            if (i == 3) {
                System.out.println("true");
                return true;
            }
        }
        return false;
    }
    public static void main(String[] args) {
   new texto().text();
    }
}
