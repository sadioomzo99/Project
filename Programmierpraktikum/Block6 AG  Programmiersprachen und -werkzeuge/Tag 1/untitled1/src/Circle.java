import java.awt.*;

public class Circle {
    int startPosX, startPosY, endPosY, endPosX;
    Color lineColor;

    public Circle(int xPos, int yPos, int x, int y,Color l) {
        this.startPosX = xPos;
        this.startPosY = yPos;
        this.endPosX = x;
        this.endPosY = y;
        this.lineColor=l;
    }


    public void paint(Graphics g) {
        g.setColor(lineColor);
        g.drawOval(startPosX, startPosY, endPosX, endPosY);
    }
}
