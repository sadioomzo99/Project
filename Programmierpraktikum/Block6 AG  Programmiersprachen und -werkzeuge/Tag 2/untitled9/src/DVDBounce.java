import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DVDBounce {
    int posX;
    int posY;
    int speedX;
    int speedY;
    final int maxX;
    final int maxY;
    Random r = new Random();
     ArrayList<Line> lines;
    ArrayList<Integer[]> collisions;

    public DVDBounce(int maxX, int maxY) {
        this.posX=0;
        this.posY=0;
        this.speedX=0;
        this.speedY=0;
        this.maxX = maxX;
        this.maxY = maxY;
        this.lines =new ArrayList<>();
        Integer[] p1 ={posX,posY};
        collisions= new ArrayList<>();
        collisions.add(p1);
        setRandomDirection();
    }
    public void setRandomPosition() {
        this.posX =  r.nextInt(maxX);
        this.posY =  r.nextInt(maxY);
    }

    public void setRandomDirection(){

       if(r.nextInt(500)>250){
           this.speedX=1;
       }else{
           this.speedX=-1;
       }
        if(r.nextInt(500)>250){
            this.speedY=1;
        }else{
            this.speedY=-1;
        }
    }

    private CollisionDirection checkCollision(){
    if (posX<=0&&posY<=0||posX>=maxX&&posY<=0||posX>=maxX&&posY>=maxY||posX<=0&&posY>=maxY) {
    return CollisionDirection.BOTH;

    }else if (posX <= 0 || posX >= maxX) {
            return CollisionDirection.VERTICAL;
        }else if (posY <= 0 || posY >= maxY) {
            return CollisionDirection.HORIZONTAL;
        }
        return CollisionDirection.NONE;
    }

    private void move(){
        this.posX+=speedX;
        this.posY+=speedY;
    }

    private void changeDirection(CollisionDirection collision) {
        switch (collision) {
            case BOTH -> {
                this.speedY = -speedY;
                this.speedX = -speedX;
            }
            case NONE -> {
            }
            case VERTICAL -> {this.speedX=-speedX;}
               /* if (this.speedX == -1 && this.speedY == 1) {
                    this.speedX = -speedX;
                } else if (this.speedX == -1 && this.speedY == -1) {
                    this.speedX = -speedX;
                } else if (this.speedX == 1 && this.speedY == -1) {
                    this.speedX = -speedX;
                } else if (this.speedX == 1 && this.speedY == 1) {
                    this.speedX = -speedX;
                }*/


            case HORIZONTAL -> {this.speedY=-speedY;}
              /* if (this.speedX == 1 && this.speedY == 1) {
                    this.speedY = -speedY;
                } else if (this.speedX == -1 && this.speedY == 1) {
                    this.speedY = -speedY;
                } else if (this.speedX == 1 && this.speedY == -1) {
                    this.speedY = -speedY;
                } else if (this.speedX == -1 && this.speedY == -1) {
                    this.speedY = -speedY;
                }
            }*/

        }
    }
    private void logCollision(){
        if(collisions.size()>=2){
           lines.add(new Line(collisions.get(collisions.size()-1)[0],collisions.get(collisions.size()-1)[1],
                    collisions.get(collisions.size()-2)[0],collisions.get(collisions.size()-2)[1]));
        }
    }

    private void logCollisions(){
        CollisionDirection cd = checkCollision();

        while (cd == CollisionDirection.NONE){
         move();
             cd = checkCollision();
         }
        frameTrick();
        int[] p1={getPosX(),getPosY()};
        changeDirection(cd);
        move();

        CollisionDirection collision = checkCollision();
        while (collision == CollisionDirection.NONE){
            move();
           collision = checkCollision();

        }
        frameTrick();
        int[] p2={getPosX(),getPosY()};

        lines.add(new Line(p1[0],p1[1],p2[0],p2[1]));
    }

    double getShortestDistance(){
        double temp=100000;
        for (Line s:lines){
            if (s.length<temp){
                temp=s.length;
            }
        }
        return temp;
    }

    double totalDistance(){
        double temp=0;
        for (Line s:lines){
                temp+=s.length;
        }
        return temp;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

//    boolean checkRepeat1(){
//        List<Line> routes = new ArrayList<>();
//
//        CollisionDirection collision1 = checkCollision();
//        int[] p1=getPointAtCollision(collision1);
//
//        changeDirection(collision1);
//        move();
//
//        CollisionDirection collision2 = checkCollision();
//        int[] p2=getPointAtCollision(collision2);
//
//        changeDirection(collision2);
//        move();
//
//        CollisionDirection collision3 = checkCollision();
//        int[] p3=getPointAtCollision(collision3);
//        changeDirection(collision3);
//        move();
//
//        CollisionDirection collision4 = checkCollision();
//        int[] p4=getPointAtCollision(collision4);
//
//
//        changeDirection(collision4);
//        move();
//
//        CollisionDirection collision5 = checkCollision();
//        int[] p5=getPointAtCollision(collision5);
//        changeDirection(collision5);
//        move();
//
//        CollisionDirection collision6 = checkCollision();
//        int[] p6=getPointAtCollision(collision6);
//        changeDirection(collision3);
//        move();
//
//        CollisionDirection collision7 = checkCollision();
//        int[] p7=getPointAtCollision(collision7);
//
//        return Arrays.equals(p1,p5) && Arrays.equals(p2,p6);
//    }

    boolean checkRepeat() {
        if (lines.size() < 5) {
            return false;
        } else {
            return lines.get(4).compareTo(lines.get(0)) == 0;
        }
    }


    public boolean frameTrick(){
        CollisionDirection collisionDirection ;
        move();
        collisionDirection = checkCollision();
        changeDirection(collisionDirection);
        if(collisionDirection!=CollisionDirection.NONE){
        Integer[] p ={getPosX(),getPosY()};
        collisions.add(p);
        logCollision();
        if (checkRepeat()|| collisionDirection==CollisionDirection.BOTH) {

            String s = "";
            if (posX <= 0 && posY <= 0) {
                s = "upperLeft";
            }
            if (posX >= maxX && posY <= 0) {
                s = "upperRight";
            }
            if (posX <= 0 && posY >= maxY) {
                s = "downLeft";
            }
            if (posX >= maxX && posY >= maxY) {
                s = "downRight";
            }
            System.out.println("Ecke: " + s + "shortestDistance: "
                    + getShortestDistance() +
                    "Wegeabschnitte: " + lines.size() +
                    "gesamte Strecke: " + totalDistance());
            return true;
        }

    }
        return false;
    }

}
