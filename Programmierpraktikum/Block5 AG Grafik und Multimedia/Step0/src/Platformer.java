import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.io.Serial;
import javax.swing.JFrame;

public class Platformer extends JFrame {
	@Serial
	private static final long serialVersionUID = 5736902251450559962L;

	public Platformer() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("HelloWorld");
		this.setBounds(0, 0, 320, 240);
		this.setVisible(true);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g; // cast to Graphics2D
		Line2D.Double line = new Line2D.Double(20.0, 50.0, 50.0, 200.0);
		g2d.draw(line);
		Rectangle2D.Double rect = new Rectangle2D.Double(100.0, 50.0, 60.0, 80.0);
		g2d.draw(rect); // also try g2d.fill(rect);
		Ellipse2D.Double circle = new Ellipse2D.Double(200.0, 100.0, 80.0, 80.0);
		g2d.draw(circle); // also try g2d.fill(circle);
	}
}
