import java.awt.*;
import java.util.ArrayList;

public class Turtle {
    private int xPos, yPos;
    private double arc;
    private boolean penDown;
    private Color color;

    private ArrayList<Line> lines;
    private ArrayList<Circle> circle;

    public Turtle(int xPos, int yPos, double arc) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.arc = arc;
        this.color = Color.BLACK;
        this.penDown = true;
        this.lines = new ArrayList<>();
        this.circle=new ArrayList<>();
    }

    public void setPenDown(boolean penDown) {
        this.penDown = penDown;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void turn(double delta, boolean right) {
        if (right) {
            this.arc = arc - delta;

        } else {
            this.arc = arc + delta;
        }
    }

    void paint(Graphics g) {
        g.setColor(color);
        g.drawRect(this.xPos, this.yPos, 50, 60);
        for (Line line : lines) {
            line.paint(g);
        }

        for (Circle c : circle) {
            c.paint(g);
        }
    }

    void moveTo(int x, int y) {
        if (penDown) {
            Line l = new Line(xPos, yPos, x, y,this.color);
            lines.add(l);
        }

        this.xPos = x;
        this.yPos = y;
    }
    void drawCircle(int width,int height){
        circle.add(new Circle(xPos,yPos,width,height,this.color));
    }
    void goForward(int step) {
        int scaleX = (int) Math.sin(Math.toRadians(arc) * step);
        int scaleY = (int) Math.cos(Math.toRadians(arc) * step);
        if (penDown) {
            Line l = new Line(xPos, yPos, xPos + scaleX, yPos + scaleY,this.color);
            lines.add(l);
        }
        this.xPos = (xPos + scaleX);
        this.xPos = (xPos + scaleY);
    }

    void interpret(String commands) {
        String[] command = commands.split("\\n");
        for (String s : command) {
            if (s.equals("pen up")) {
                setPenDown(false);
            }
            if (s.equals("pen down")) {
                setPenDown(false);
            }
                if (s.trim().split(" ").length == 3) {
                    if (s.trim().split(" ")[0].equals("move")) {
                    int pos1 = Integer.parseInt(s.trim().split(" ")[1]);
                    int pos2 = Integer.parseInt(s.trim().split(" ")[2]);
                    moveTo(pos1, pos2);
                }
            }

            if (s.trim().split(" ").length == 2) {
                if (s.trim().split(" ")[0].equals("go")) {
                    int pos = Integer.parseInt(s.trim().split(" ")[1]);
                    goForward(pos);
                }
            }

            if (s.trim().split(" ").length == 3) {
                if (s.trim().split(" ")[0].equals("turn")) {
                    if (s.trim().split(" ")[1].equals("left")) {
                        int pos = Integer.parseInt(s.trim().split(" ")[2]);
                        turn(pos, false);
                    } else {
                        int pos = Integer.parseInt(s.trim().split(" ")[2]);
                        turn(pos, true);
                    }
                }
            }
            if (s.trim().split(" ").length == 2) {
                if (s.trim().split(" ")[0].equals("color")) {
                    setColor(Color.decode(s.trim().split(" ")[1]));
                }
            }
        }
    }
}
