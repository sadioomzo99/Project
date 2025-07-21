import java.awt.*;

public class Line {
    int startPosX,startPosY,endPosY,endPosX;
    Color lineColor;

    public Line(int xPos, int yPos, int x, int y ,Color l) {
        this.startPosX=xPos;
        this.startPosY=yPos;
        this.endPosX=x;
        this.endPosY=y;
        this.lineColor=l;
    }


    public void paint(Graphics g){
        g.setColor(lineColor);
        g.drawLine(startPosX,startPosY,endPosX,endPosY);
    }
}
