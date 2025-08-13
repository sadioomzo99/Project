import javax.swing.*;
import java.awt.*;

public class PaintArea extends JPanel {


    Turtle turtle;
    public PaintArea() {
        this.turtle =new Turtle(10,10,45);
        this.setPreferredSize(new Dimension(1060,720));
        this.setBackground(Color.white);
    }

    public Turtle getTurtle() {
        return turtle;
    }

    @Override
    public void paint(Graphics g){
       super.paint(g);
       turtle.paint(g);
    }
}
