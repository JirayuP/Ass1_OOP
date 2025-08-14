import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class importtxt {
    public importtxt() {
        //nothing
    }
    public String importfile() {
        JFileChooser file =new JFileChooser();
        int response = file.showOpenDialog(null);

        System.out.println(response); // 0 = approve , 1 = cancel, X

        if (response == JFileChooser.APPROVE_OPTION) {
            String str="";
            String textdata="";
            File filelocation = new File(file.getSelectedFile().getAbsolutePath());
            System.out.println(filelocation);  // location
            try {
                BufferedReader txt=new BufferedReader(new FileReader(filelocation));  //String location
                for (; ; ) {
                    if (txt.ready()) {
                        str=txt.readLine();          // String (line)
                        str = str.trim();
                        textdata=textdata+str+"\n";  // +"\n" to count line
                        //System.out.println(str);
                    }
                    else {
                        break;
                    }
                }
            }
            catch (FileNotFoundException err) {
            }
            catch (IOException err){
            }
            System.out.println(textdata);  // test
            return textdata;
        }
        else {
            return "";
        }
    }
}
