import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class gui {
    public static void main(String[] args) {
        myFrame frame = new myFrame();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class myFrame extends JFrame implements ActionListener {
    Button[] gridButton = new Button[200];
    JLabel output = new JLabel("Gas percent");
    public myFrame() {
        //JFrameSetting
        this.setTitle("Gui");
        this.setSize(1920, 1080);
        this.setLayout(new BorderLayout());

        //inputPanel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setPreferredSize(new Dimension(100, 100));
        this.add(inputPanel, BorderLayout.NORTH);

        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(400, 20));
        JButton openfile = new JButton("Open File");
        JButton cal = new JButton("Calculation");
        JButton res = new JButton("Reset");

        inputPanel.add(openfile);
        inputPanel.add(tf);
        inputPanel.add(cal);
        inputPanel.add(res);

        //gridButtonPanel
        JPanel gridPanel = new JPanel(new GridLayout(10, 20));
        gridPanel.setPreferredSize(new Dimension(100, 100));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        this.add(gridPanel, BorderLayout.CENTER);

        Random random = new Random();
        for (int i = 0; i < gridButton.length; i++) {
            gridButton[i] = new Button(" ");
            int rand = random.nextInt(3);
            switch (rand){
                case 0:
                    gridButton[i].setBackground(Color.GREEN);
                    break;
                case 1:
                    gridButton[i].setBackground(Color.YELLOW);
                    break;
                case 2:
                    gridButton[i].setBackground(Color.RED);
                    break;
            }
            gridButton[i].addActionListener(this);
            gridPanel.add(gridButton[i]);
        }

        //detailsPanel
        JPanel detailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        detailPanel.setPreferredSize(new Dimension(300, 100));
        detailPanel.add(output);
        detailPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        this.add(detailPanel, BorderLayout.EAST);

        //westPanel
        JPanel westPanel = new JPanel();
        westPanel.setPreferredSize(new Dimension(100, 100));
        this.add(westPanel, BorderLayout.WEST);

        //southPanel
        JPanel southPanel = new JPanel();
        southPanel.setPreferredSize(new Dimension(100, 300));
        this.add(southPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i=0;i<gridButton.length;i++){
            if (e.getSource() == gridButton[i]&&gridButton[i].getBackground().equals(Color.GREEN)){
                output.setText("Gas more than 50%");
            }
            else if (e.getSource() == gridButton[i]&&gridButton[i].getBackground().equals(Color.YELLOW)){
                output.setText("Gas less than 50%");
            }
            else if (e.getSource() == gridButton[i]&&gridButton[i].getBackground().equals(Color.RED)){
                output.setText("No Gas");
            }
        }
    }
}
