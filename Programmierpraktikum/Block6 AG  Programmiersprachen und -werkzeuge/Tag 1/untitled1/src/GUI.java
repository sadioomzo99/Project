import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {
  final int width =1280;
  final int height=720;
  final String title="Hello World";



    public GUI(String title){
        super();
        setSize(width,height);
        this.setTitle(title);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       //PaintArea
        PaintArea paintArea = new PaintArea();
        this.add(paintArea,BorderLayout.WEST);

        //JPanel
        JPanel jPanel =new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.PAGE_AXIS));
        jPanel.setPreferredSize(new Dimension(200,720));
        //JTextArea
        JTextArea jTextArea =new JTextArea();
        jTextArea.setSize(200,680);
        jPanel.add(jTextArea);

        //JButton
        JButton jButton = new JButton();
        jButton.setText("send");

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String temp =jTextArea.getText();
                paintArea.getTurtle().interpret(temp);
                paintArea.repaint();
            }
        });
        jPanel.add(jButton);

        this.add(jPanel,BorderLayout.EAST);
        this.setVisible(true);

    }

    public static void main(String[] args) {
        GUI gi= new GUI("Test");
    }
}
