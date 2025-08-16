import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class GasCalculate extends JFrame {

    // Theme constants (ค่าคงที่สำหรับกำหนดสี)
    private static final Color BG_MAIN = new Color(245, 246, 248);
    private static final Color BG_PANEL = Color.WHITE;
    private static final Color TXT_PRIMARY = new Color(40, 40, 40);
    private static final Color TXT_SECONDARY = new Color(110, 110, 110);
    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color YELLOW = new Color(250, 204, 21);
    private static final Color RED = new Color(239, 68, 68);

    // Domain constants (ค่าคงที่ที่ใช้ในการคำนวณ)
    private static final double CELL_W = 150.0, CELL_L = 150.0; // ความกว้างและความยาวของแต่ละช่อง
    private static final double TOP_OFFSET = 200.0; // ความลึกจาก Base ไปยัง Top (Top = Base - 200 m)
    private static final DecimalFormat DF0 = new DecimalFormat("#,##0"); // รูปแบบตัวเลขมีคอมม่าไม่มีทศนิยม
    static final DecimalFormat DFP = new DecimalFormat("0.0%"); // รูปแบบตัวเลขเป็นเปอร์เซ็นต์

    // UI components (ส่วนประกอบของหน้าจอ)
    private JTextField tfFluid;
    private JButton btnCalc, btnCancel;
    private JPanel dashboardWrap;
    private JTable resultTable;
    private DefaultTableModel resultModel;
    private JLabel totalVolLabel, statusLabel;

    // Data storage (การจัดเก็บข้อมูล)
    private double[][] baseDepth = null;// เก็บข้อมูลความลึกฐานที่อ่านจากไฟล์
    private final Map<String, Integer> rowIndexByKey = new HashMap<>();// Map ที่เก็บข้อมูลตำแหน่ง (row, col) ของแต่ละช่องกับ index ของแถวในตารางผลลัพธ์
    private final JFileChooser chooser = new JFileChooser(); // หน้าต่างสำหรับเลือกไฟล์

    //หน้าที่หลัก: เป็นเมธอดคอนสตรักเตอร์ (Constructor) ที่ใช้ในการ สร้างหน้าต่างโปรแกรมและจัดวางส่วนประกอบ UI ทั้งหมด
    public GasCalculate() {
        super("Gas Volume Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 720));
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout(8, 8));

        // สร้าง Menu Bar
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem mOpen = new JMenuItem("Open");
        mOpen.addActionListener(this::onOpenFile);// เมื่อคลิก "Open" จะเรียกใช้เมธอด onOpenFile
        file.add(mOpen);
        mb.add(file);
        setJMenuBar(mb);
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));//filter ให้รับได้แค่ไฟล์ txt

        // สร้าง Top Panel สำหรับปุ่มควบคุมและคำอธิบายสี
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setBackground(BG_PANEL);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(225,225,225)),//set เส้นขอบของ top Panel
                new EmptyBorder(6,12,6,12)//ระยะห่าวระหว่าง component และขอบของ panel
        ));
        JLabel lblFluid = new JLabel("Fluid Contact (m)");
        lblFluid.setForeground(TXT_PRIMARY);//สีตัวหนังสือ
        tfFluid = new JTextField(10);
        tfFluid.setText("2500");// กำหนดค่าความลึกเริ่มต้นของ Fluid Contact เป็น 2500
        btnCalc = new JButton("Calculate Volume");
        stylePrimary(btnCalc);//เรียกใช้ method stylePrimary โดยส่ง btnCalc เป็น parameter
        btnCalc.setEnabled(false);// ปิดการใช้งานปุ่มคำนวณตั้งแต่แรก จนกว่าจะมีการโหลดไฟล์
        btnCalc.addActionListener(this::onCalculate);
        btnCancel = new JButton("Cancel");
        stylePrimary(btnCancel);
        btnCancel.addActionListener(e -> clearAll());//เรียกใช้ method clearAll() clear ทุกอย่างเมื่อกดปุุ่ม cancel
        top.add(lblFluid); top.add(tfFluid); top.add(btnCalc); top.add(btnCancel);//เพิ่มแต่ละปุ่มเข้า top Panel
        top.add(Box.createHorizontalStrut(20));//เว้นระยะห่างในแนวนอนของ component ของ top panel
        top.add(buildLegend()); // เรียกใช้เมธอดเพื่อสร้างคำอธิบายสี
        add(top, BorderLayout.NORTH);

        // สร้าง Center Panel สำหรับ Dashboard และตารางผลลัพธ์
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_MAIN);
        center.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0,0,0,10);//set ตำแหน่งใน GridBagLayout
        gc.fill = GridBagConstraints.BOTH;//ถ้ามีช่องว่างก็จะขยายให้เต็มใน GridBagLayout() แนวนอน,แนวตั้ง
        gc.weighty = 1;

        dashboardWrap = new JPanel(new BorderLayout());
        dashboardWrap.setBackground(BG_PANEL);
        dashboardWrap.setBorder(boxBorder("Dashboard"));
        dashboardWrap.add(placeholder("Please open a file to begin."), BorderLayout.CENTER);// แสดงข้อความเริ่มต้นก่อนจะมีการโหลดไฟล์

        gc.gridx=0; gc.weightx=0.77;//จัดตำแหน่ง component
        center.add(dashboardWrap, gc);

        // สร้างตารางแสดงผลลัพธ์
        String[] cols = {"Row","Col","Base (m)","Top (m)","Gas (m)","Percent","Volume (m³)"};//หัวข้อของตาราง
        resultModel = new DefaultTableModel(cols, 0){ @Override public boolean isCellEditable(int r,int c){return false;} };//set ไม่ให้เปลี่ยนข้อความได้
        resultTable = new JTable(resultModel);
        resultTable.setFillsViewportHeight(true);
        resultTable.setAutoCreateRowSorter(true); // ทำให้สามารถจัดเรียงข้อมูลในตารางได้
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(BG_PANEL);
        resultPanel.setBorder(boxBorder("Result"));
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        gc.gridx=1; gc.weightx=0.23; gc.insets = new Insets(0,0,0,0);
        center.add(resultPanel, gc);
        add(center, BorderLayout.CENTER);

        // สร้างแถบสถานะ (Status Bar)
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(BG_PANEL);
        status.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(225,225,225)));//กำหนดความทึบหนา
        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(TXT_SECONDARY);
        totalVolLabel = new JLabel("Total Volume: -");
        totalVolLabel.setForeground(TXT_PRIMARY);
        status.add(statusLabel, BorderLayout.WEST);
        status.add(totalVolLabel, BorderLayout.EAST);
        add(status, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // ====== Actions (การทำงานเมื่อผู้ใช้กดปุ่ม) ======
    private void onOpenFile(ActionEvent ev) {
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        statusLabel.setText("Loading " + f.getName() + " ...");
        try {
            List<double[]> rows = new ArrayList<>();
            int maxCols = 0;
            for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
                String s = line.trim(); if (s.isEmpty()) continue;//เมื่ออ่านหมดแล้วให้ไปบรรทัดถัดไป
                String[] toks = s.split("[,\\s]+");//แยกแต่ละ index ด้วย space
                double[] arr = new double[toks.length];
                for (int i=0;i<toks.length;i++) arr[i] = Double.parseDouble(toks[i]);//แปลง toks เป็น double
                rows.add(arr); maxCols = Math.max(maxCols, arr.length);//เพิ่ม arr เข้า rows, maxCols หา col ที่มากที่สุด
            }
            if (rows.isEmpty()) throw new IllegalArgumentException("Empty file.");
            baseDepth = new double[rows.size()][maxCols];//เก็บตัวเลขทั้งหมดในไฟล์เป็น array 2D,maxCols คือจำนวน col ที่มากที่สุด
            for (int r=0;r<rows.size();r++) {
                double[] src = rows.get(r);
                for (int c=0;c<maxCols;c++) baseDepth[r][c] = (c<src.length)?src[c]:src[src.length-1];//เช็คว่า c น้อยกว่า src.length ไหม ถ้าใช่จับ scr[c] ยัดลงใน baseDepth[r][c]
            }
            btnCalc.setEnabled(true);//อัปเดตสถานะที่ statusLabel และเปิดใช้งานปุ่ม "Calculate Volume"
            statusLabel.setText("Loaded: " + baseDepth.length + " rows × " + baseDepth[0].length + " cols.");
            showEmptyGrid(); // แสดงตารางเปล่าหลังจากโหลดไฟล์สำเร็จ
            clearResultTableOnly();//clear แถวและตารางใน map
            totalVolLabel.setText("Total Volume: -");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading file:\n"+ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);//ข้อความที่แสดงเมื่อไฟล์ไม่ใช่ .txt
            statusLabel.setText("Error loading file.");
        }
    }

    //จัดการการทำงานเมื่อผู้ใช้คลิกปุ่ม "Calculate Volume"
    private void onCalculate(ActionEvent ev) {
        if (baseDepth == null) return;
        double fluid;
        try { fluid = Double.parseDouble(tfFluid.getText().trim()); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "กรุณาใส่ตัวเลข Fluid Contact (m) ให้ถูกต้อง",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int R = baseDepth.length, C = baseDepth[0].length;
        JPanel grid = new JPanel(new GridLayout(R, C, 4, 4));
        grid.setBackground(BG_MAIN);

        double total = 0.0, area = CELL_W * CELL_L;
        for (int r=0;r<R;r++) {
            for (int c=0;c<C;c++) {
                double base = baseDepth[r][c]; // ความลึกฐานจากข้อมูลที่โหลดมา
                double top = base - TOP_OFFSET; // คำนวณความลึก Top
                double full = TOP_OFFSET; // ความสูงของคอลัมน์ก๊าซทั้งหมดที่เป็นไปได้

                // คำนวณความสูงของคอลัมน์ก๊าซ
                // โดยเป็นค่าต่ำสุดระหว่าง (ความลึกฐาน - ความลึก Top) กับ (ความลึก Fluid Contact - ความลึก Top)
                // และต้องไม่ต่ำกว่า 0
                double gas = Math.max(0, Math.min(base, fluid) - top);
                // ตรวจสอบว่าความสูงของก๊าซไม่เกินความสูงเต็มของคอลัมน์ (TOP_OFFSET)
                if (gas > full) gas = full;

                // คำนวณเปอร์เซ็นต์ของปริมาตรก๊าซ
                double pct = (full>0)? (gas/full):0.0;
                double vol = gas * area;
                total += vol;

                // สร้างปุ่ม PillCell สำหรับแต่ละช่องในตาราง
                PillCell btn = new PillCell(pct, base, top, gas, fluid, vol, r, c);
                styleCell(btn, pct);
                // เมื่อคลิกปุ่ม จะเพิ่มหรือลบแถวในตารางผลลัพธ์
                btn.addActionListener(e -> toggleResultRow(btn));
                grid.add(btn);
            }
        }
        // ลบเนื้อหาใน dashboard และเพิ่มตารางใหม่เข้าไป
        dashboardWrap.removeAll();
        dashboardWrap.add(new JScrollPane(grid), BorderLayout.CENTER);
        dashboardWrap.revalidate(); dashboardWrap.repaint();

        totalVolLabel.setText("Total Volume: " + DF0.format(total) + " m³");
        statusLabel.setText("Calculation complete.");
        clearResultTableOnly();
    }

    // เพิ่มหรือลบแถวจากตารางผลลัพธ์ตามสถานะของปุ่ม (เลือก/ไม่เลือก)
    private void toggleResultRow(PillCell cb) {
        String key = cb.row + "," + cb.col;
        if (cb.isSelected()) {
            //ตรวจสอบสถานะของปุ่ม
            if (!rowIndexByKey.containsKey(key)) {
                //เมื่อปุ่มถูกเลือก สร้างอาร์เรย์ออบเจกต์ที่มีข้อมูลรายละเอียดของเซลล์นั้นๆ และเพิ่มเป็นแถวใหม่ในตาราง
                Object[] row = {
                        cb.row, cb.col,
                        DF0.format(cb.base),
                        DF0.format(cb.top),
                        DF0.format(cb.gas),
                        DFP.format(cb.percent),
                        DF0.format(cb.volume)
                };
                resultModel.addRow(row);
                // เก็บการจับคู่ระหว่าง key ของช่องกับ index ของแถวในตาราง
                rowIndexByKey.put(key, resultModel.getRowCount()-1);
            }
        } else {
            Integer idx = rowIndexByKey.remove(key);
            if (idx != null && idx < resultModel.getRowCount()) {
                resultModel.removeRow(idx);
                // สร้าง map ใหม่เพื่อให้ index ของแถวถูกต้องหลังจากมีการลบแถว
                rowIndexByKey.clear();
                for (int i=0;i<resultModel.getRowCount();i++) {
                    int rr = Integer.parseInt(resultModel.getValueAt(i,0).toString());
                    int cc = Integer.parseInt(resultModel.getValueAt(i,1).toString());
                    rowIndexByKey.put(rr+","+cc, i);
                }
            }
        }
    }

    //ล้างข้อมูลและหน้าจอ
    private void clearAll() {
        tfFluid.setText("");
        totalVolLabel.setText("Total Volume: -");
        statusLabel.setText("Cleared.");
        clearResultTableOnly();//ล้างเฉพาะข้อมูลในตารางผลลัพธ์ (resultTable) เท่านั้น
        if (baseDepth != null) showEmptyGrid();//วาด grid เปล่าขึ้นมาเมื่อกด cancel
    }
    //ล้างเฉพาะข้อมูลในตารางผลลัพธ์ (resultTable) เท่านั้น
    private void clearResultTableOnly() {
        resultModel.setRowCount(0);
        rowIndexByKey.clear();
    }

    // ====== Helpers (เมธอดช่วยเสริม) ======
    // แสดงตารางเปล่าสำหรับ Dashboard ก่อนการคำนวณ
    private void showEmptyGrid() {
        JPanel grid = new JPanel(new GridLayout(baseDepth.length, baseDepth[0].length, 4, 4));//สร้าง JPanel ใหม่ที่มี GridLayout ตามขนาดของข้อมูลที่อ่านได้
        grid.setBackground(BG_MAIN);
        for (int i=0;i<baseDepth.length*baseDepth[0].length;i++) {
            JPanel cell = new JPanel();
            cell.setBackground(Color.WHITE);
            cell.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
            grid.add(cell);//เพิ่ม JPanel สีขาวขนาดเล็กเข้าไปในตารางกริดเพื่อใช้เป็น Placeholder
        }
        dashboardWrap.removeAll();
        dashboardWrap.add(new JScrollPane(grid), BorderLayout.CENTER);
        dashboardWrap.revalidate(); dashboardWrap.repaint();//นำแผงกริดที่สร้างเสร็จแล้วไปแสดงใน dashboardWrap
    }

    // สร้างส่วนคำอธิบายสี (legend)
    private JPanel buildLegend() {
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.add(legendRow(RED,   "NoGas  = 0%"));
        legend.add(legendRow(YELLOW,"LowGas < 50%"));
        legend.add(legendRow(GREEN, "HighGas ≥ 50%"));
        return legend;
    }
    private JPanel legendRow(Color color, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);//set ความโปร่งใสของพื้นหลัง
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(14,14));
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        JLabel lb = new JLabel(text);
        row.add(box); row.add(lb);
        return row;
    }
    //เมธอดช่วยในการจัดรูปแบบ UI
    private javax.swing.border.Border boxBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),//สร้างเส้นขอบแบบมีหัวข้อ (Titled Border) ให้กับแผงต่างๆ
                        title),
                new EmptyBorder(8,8,8,8)
        );
    }
    //สร้างแผงที่มีข้อความอยู่ตรงกลางสำหรับใช้เป็นตัวยึดพื้นที่
    //จัดการข้อกลางจอก่อนจะ import file
    private JPanel placeholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(TXT_SECONDARY);
        l.setFont(l.getFont().deriveFont(Font.ITALIC, 16f));
        p.add(l, BorderLayout.CENTER);
        return p;
    }
    //จัดรูปแบบปุ่มและเซลล์ต่างๆ
    private void stylePrimary(JButton b) {
        b.setBackground(new Color(245,245,245));
        b.setForeground(TXT_PRIMARY);
        b.setFocusPainted(false);
    }

    // ====== PillCell (ปุ่มที่ใช้แสดงผลในตาราง) ======
    static class PillCell extends JToggleButton {
        // ข้อมูลที่เกี่ยวข้องกับแต่ละช่อง
        final double percent, base, top, gas, fluid, volume;
        final int row, col;
        private static final int RADIUS = 10;
        private static final int INSET  = 2;

        PillCell(double percent, double base, double top, double gas, double fluid,
                 double volume, int row, int col) {
            this.percent = percent; this.base = base; this.top = top; this.gas = gas;
            this.fluid = fluid; this.volume = volume; this.row = row; this.col = col;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4,6,4,6));
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
        }

    }

    // กำหนดสีและตัวอักษรของปุ่ม PillCell ตามเปอร์เซ็นต์ของก๊าซ
    private void styleCell(AbstractButton b, double pct) {
        if (pct <= 0) {
            b.setBackground(RED);
            b.setForeground(Color.BLACK);
        } else if (pct < 0.5) {
            b.setBackground(YELLOW);
            b.setForeground(Color.BLACK);
        } else {
            b.setBackground(GREEN);
            b.setForeground(Color.BLACK);
        }
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setVerticalAlignment(SwingConstants.CENTER);
        b.setMargin(new Insets(2, 2, 2, 2));
        b.setText(DFP.format(pct)); // แสดงเปอร์เซ็นต์ก๊าซบนปุ่ม
    }

    // เมธอดหลักสำหรับรันโปรแกรม
    public static void main(String[] args) {
        GasCalculate proframe = new GasCalculate();
        proframe.setVisible(true);
    }

}
