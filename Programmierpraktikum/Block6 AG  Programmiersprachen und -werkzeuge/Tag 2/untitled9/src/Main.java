import java.util.Random;

public class Main {
    public static void main(String... args) throws Exception {
       int x=-1, y=-1;
       Random r = new Random();
       if (args.length==0){
           x= r.nextInt(1000);
           y = r.nextInt(720);
        }else if (args.length<2){
            throw new Exception("Bitte 2 Integer Werte angeben");
        }else if(args.length>2){
            throw new Exception("Bitte nur 2 Integer Werte angeben");
       }else {

               try {
                   x = Integer.parseInt(args[0]);

               } catch (Exception e) {
                   System.out.println("Not a number" +
                           "Bitte Integer Werte angeben");
                   x = 30;

               }
                 try {
               y = Integer.parseInt(args[1]);

                 } catch (Exception e) {
                     System.out.println("Not a number" +
                       "Bitte Integer Werte angeben");
                         y = 50;
                    }

       }

       DVDBounce bounce = new DVDBounce(x,y);
       while(!bounce.frameTrick()){}
        System.exit(0);

    }
}
