import java.awt.Dimension;
import java.awt.Toolkit;
public class ThisScreen {

    private int width;
    private int height;
    public ThisScreen() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int)size.getWidth();
        height = (int)size.getHeight();
        System.out.println(width+" "+height);
    }
    public int getWidthScreen() {
        return this.width;
    }
    public int getHeightScreen() {
        return this.height;
    }
}
