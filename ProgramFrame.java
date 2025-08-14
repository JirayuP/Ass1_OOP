import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.AncestorListener;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ProgramFrame extends JFrame implements ActionListener {
    ThisScreen screen = new ThisScreen();  //set frame
    hub calinfo = new hub(); //set data,get data

    JPanel panelmap = new JPanel();
    JButton bmap[][];                //button map

    JPanel panel2 = new JPanel();
    String sum_output = "";
    TextArea show_output = new TextArea();  // in panel 2

    JPanel lowpanel = new JPanel();
    JButton openf =new JButton("Open file");
    JTextField inputfulid = new JTextField();
    JButton setfluid = new JButton("Set fluid"); //low
    TextArea showbase = new TextArea();  // low panel  awt
    TextField state = new TextField();   //ตกแต่ง ขวาสุด

    public ProgramFrame() {
        int w = screen.getWidthScreen();
        int h = screen.getHeightScreen();
        String datadepth="";
        setLocation(0,0);
        setSize(w,h);
        setLayout(null);

        // add .add
        openf.addActionListener(this);
        setfluid.addActionListener(this);

        lowpanel.setLayout(null);
        lowpanel.setSize(w-100,h-580);
        lowpanel.setLocation(50,580);
        lowpanel.setBackground(Color.GRAY);
        add(lowpanel);
        openf.setSize(200,50);  // low panel
        openf.setLocation(0,0);
        lowpanel.add(openf);                 //add
        showbase.setSize(300,200);
        showbase.setLocation(220,10);
        lowpanel.add(showbase);
        inputfulid.setSize(200,50);
        inputfulid.setLocation(540,10);
        lowpanel.add(inputfulid);
        setfluid.setSize(200,50);
        setfluid.setLocation(740,10);
        lowpanel.add(setfluid);
        state.setSize(300,50);
        state.setLocation(1000,10);
        lowpanel.add(state);

        panel2.setLayout(null);
        panel2.setLocation(w-450,20);
        panel2.setSize(400,500);
        panel2.setBackground(Color.DARK_GRAY);
        add(panel2);
        show_output.setSize(300,400);
        show_output.setLocation(50,50);
        panel2.add(show_output);

    }
    public void map(String datadepth){

        calinfo.set(datadepth);// set object(class)
        double[][] AB = calinfo.getBase(); //arraybase
        panelmap.setLayout(new GridLayout(AB.length, AB[0].length));
        panelmap.setSize(1000,500);
        panelmap.setLocation(50,20);
        add(panelmap);

        //JButton[][] bmap = new JButton[AB.length][AB[0].length];
        bmap = new JButton[AB.length][AB[0].length];
        for(int i=0;i<AB.length;i=i+1){
            for(int j=0;j<AB[i].length;j=j+1){
                bmap[i][j] = new JButton();
                if (calinfo.getpersentage(i,j)>50){
                    bmap[i][j].setBackground(Color.GREEN);
                }
                else if(calinfo.getpersentage(i,j)<50 && calinfo.getpersentage(i,j)>0.00){
                    bmap[i][j].setBackground(Color.YELLOW);
                }
                else {
                    bmap[i][j].setBackground(Color.RED);
                }
                int index1 = i;
                int index2 = j;
                bmap[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {     //inner class
                        if(e.getSource() == bmap[index1][index2]){
                            state.setText("");
                            sum_output = sum_output+"["+index1+"]["+index2+"]\n"
                                        +"Base Horizon : "+calinfo.getBase(index1,index2)+"\n"
                                        +"Top Horizon : "+calinfo.getTop(index1,index2)+"\n"
                                        +"Volume : "+calinfo.getvolume(index1,index2)+"\n"
                                        +"Persentage : "+calinfo.getpersentage(index1,index2)+"\n"
                                        +"Fluid Contact = "+calinfo.getfluidcontact()+"\n";
                            show_output.setText(sum_output); //panel2
                        }
                    }                                                //inner class
                });
                panelmap.add(bmap[i][j]);
            }
        }
    }
    @Override
    public void actionPerformed(ActionEvent e){
        if(e.getSource() == openf){
            importtxt file = new importtxt();
            String data = file.importfile();   //String data
            if(data == null || data.equals("")){
                //do nothing when press X or cancel
                //else, return ""; , null = no data
                state.setText("no import");
            }
            else {
                if(panelmap.isShowing()){
                    panelmap.removeAll();
                /*
                for(int i=0;i<bmap.length;i=i+1){
                    for(int j=0;j<bmap[i].length;j=j+1){
                        panelmap.remove(bmap[i][j]);
                    }
                }*/
                }
                map(data);
                showbase.setText(data); // low panel (show txt)
                panelmap.repaint();     //update GUI components.
                state.setText("import pass");
                sum_output = "";
                show_output.setText("");
                setVisible(true);
            }
        }
        else if(e.getSource() == setfluid){
            String inputfulidText = inputfulid.getText();
            if( calinfo.gettxt() == null ){
                //do nothing
                state.setText("setfluidcontact error");
            }
            else {
                try{
                    double fluid = Double.parseDouble(inputfulidText);  //setfluidcontact(double ... );
                    calinfo.setfluidcontact(fluid);
                    if(panelmap.isShowing()){
                        panelmap.removeAll();
                    }
                    map(calinfo.gettxt());
                    state.setText("setfluidcontact pass");
                    setVisible(true);
                }
                catch(NumberFormatException err){
                    state.setText("setfluidcontact error : can not parse");
                }
            }
        }
    }
}
