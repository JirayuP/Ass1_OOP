import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class importtxt {

    public importtxt() {

    }
    public String importfile() {
        String location;
        String str="";
        String textdata="";
        JFileChooser file =new JFileChooser();
        int response = file.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            File filelocation = new File(file.getSelectedFile().getAbsolutePath());
            System.out.println(filelocation);
            try {
                BufferedReader txt=new BufferedReader(new FileReader(filelocation));
                for (; ; )
                {
                    if (txt.ready())
                    {
                        str=txt.readLine();// void = null
                        textdata=textdata+str+"\n";
                        System.out.println(str);
                    }
                    else
                    {
                        break;
                    }
                }
                //return textdata;
            }
            catch (FileNotFoundException err) {
            }
            catch (IOException err){
            }
            return textdata;
        }
        else {
            return "";
        }


    }
}
