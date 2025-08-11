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

public class GasVolumeDashboardGUI extends JFrame {

    // Theme
    private static final Color BG_MAIN = new Color(245, 246, 248);
    private static final Color BG_PANEL = Color.WHITE;
    private static final Color TXT_PRIMARY = new Color(40, 40, 40);
    private static final Color TXT_SECONDARY = new Color(110, 110, 110);
    private static final Color ACCENT = new Color(0, 120, 215);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color YELLOW = new Color(250, 204, 21);
    private static final Color RED = new Color(239, 68, 68);

    // Domain constants
    private static final double CELL_W = 150.0, CELL_L = 150.0;
    private static final double TOP_OFFSET = 200.0; // Top = Base - 200 m
    private static final DecimalFormat DF0 = new DecimalFormat("#,##0");
    static final DecimalFormat DFP = new DecimalFormat("0.0%");

    // UI
    private JTextField tfFluid;
    private JButton btnCalc, btnCancel;
    private JPanel dashboardWrap;
    private JTable resultTable;
    private DefaultTableModel resultModel;
    private JLabel totalVolLabel, statusLabel;

    // Data
    private double[][] baseDepth = null;
    private final Map<String, Integer> rowIndexByKey = new HashMap<>();
    private final JFileChooser chooser = new JFileChooser();

    public GasVolumeDashboardGUI() {
        super("Gas Volume Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 720));
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout(8, 8));

        // Menu
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem mOpen = new JMenuItem("Open");
        mOpen.addActionListener(this::onOpenFile);
        file.add(mOpen);
        mb.add(file);
        setJMenuBar(mb);
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        // Top
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setBackground(BG_PANEL);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,new Color(225,225,225)),
                new EmptyBorder(6,12,6,12)
        ));
        JLabel lblFluid = new JLabel("Fluid Contact (m)");
        lblFluid.setForeground(TXT_PRIMARY);
        tfFluid = new JTextField(10);
        btnCalc = new JButton("Calculate Volume");
        stylePrimary(btnCalc);
        btnCalc.setEnabled(false);
        btnCalc.addActionListener(this::onCalculate);
        btnCancel = new JButton("Cancel");
        styleGhost(btnCancel);
        btnCancel.addActionListener(e -> clearAll());
        top.add(lblFluid); top.add(tfFluid); top.add(btnCalc); top.add(btnCancel);
        top.add(Box.createHorizontalStrut(20));
        top.add(buildLegend());
        add(top, BorderLayout.NORTH);

        // Center
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_MAIN);
        center.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0,0,0,10);
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        dashboardWrap = new JPanel(new BorderLayout());
        dashboardWrap.setBackground(BG_PANEL);
        dashboardWrap.setBorder(boxBorder("Dashboard"));
        dashboardWrap.add(placeholder("Please open a file to begin."), BorderLayout.CENTER);

        gc.gridx=0; gc.weightx=0.66;
        center.add(dashboardWrap, gc);

        String[] cols = {"Row","Col","Base (m)","Top (m)","Gas (m)","Percent","Volume (m³)"};
        resultModel = new DefaultTableModel(cols, 0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        resultTable = new JTable(resultModel);
        resultTable.setFillsViewportHeight(true);
        resultTable.setAutoCreateRowSorter(true);
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(BG_PANEL);
        resultPanel.setBorder(boxBorder("Result"));
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        gc.gridx=1; gc.weightx=0.34; gc.insets = new Insets(0,0,0,0);
        center.add(resultPanel, gc);
        add(center, BorderLayout.CENTER);

        // Status
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(BG_PANEL);
        status.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(225,225,225)));
        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(TXT_SECONDARY);
        totalVolLabel = new JLabel("Total Volume: -");
        totalVolLabel.setForeground(TXT_PRIMARY);
        status.add(statusLabel, BorderLayout.WEST);
        status.add(totalVolLabel, BorderLayout.EAST);
        add(status, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    // ====== Actions ======
    private void onOpenFile(ActionEvent ev) {
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        statusLabel.setText("Loading " + f.getName() + " ...");
        try {
            List<double[]> rows = new ArrayList<>();
            int maxCols = 0;
            for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
                String s = line.trim(); if (s.isEmpty()) continue;
                String[] toks = s.split("[,\\s]+");
                double[] arr = new double[toks.length];
                for (int i=0;i<toks.length;i++) arr[i] = Double.parseDouble(toks[i]);
                rows.add(arr); maxCols = Math.max(maxCols, arr.length);
            }
            if (rows.isEmpty()) throw new IllegalArgumentException("Empty file.");
            baseDepth = new double[rows.size()][maxCols];
            for (int r=0;r<rows.size();r++) {
                double[] src = rows.get(r);
                for (int c=0;c<maxCols;c++) baseDepth[r][c] = (c<src.length)?src[c]:src[src.length-1];
            }
            btnCalc.setEnabled(true);
            statusLabel.setText("Loaded: " + baseDepth.length + " rows × " + baseDepth[0].length + " cols.");
            showEmptyGrid();
            clearResultTableOnly();
            totalVolLabel.setText("Total Volume: -");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading file:\n"+ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error loading file.");
        }
    }

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
                double base = baseDepth[r][c];
                double top = base - TOP_OFFSET;
                double full = TOP_OFFSET;
                double gas = Math.max(0, Math.min(base, fluid) - top);
                if (gas > full) gas = full;
                double pct = (full>0)? (gas/full):0.0;
                double vol = gas * area;
                total += vol;

                PillCell btn = new PillCell(pct, base, top, gas, fluid, vol, r, c);
                styleCell(btn, pct);
                btn.addActionListener(e -> toggleResultRow(btn));
                grid.add(btn);
            }
        }
        dashboardWrap.removeAll();
        dashboardWrap.add(new JScrollPane(grid), BorderLayout.CENTER);
        dashboardWrap.revalidate(); dashboardWrap.repaint();

        totalVolLabel.setText("Total Volume: " + DF0.format(total) + " m³");
        statusLabel.setText("Calculation complete.");
        clearResultTableOnly();
    }

    private void toggleResultRow(PillCell cb) {
        String key = cb.row + "," + cb.col;
        if (cb.isSelected()) {
            if (!rowIndexByKey.containsKey(key)) {
                Object[] row = {
                        cb.row, cb.col,
                        DF0.format(cb.base),
                        DF0.format(cb.top),
                        DF0.format(cb.gas),
                        DFP.format(cb.percent),
                        DF0.format(cb.volume)
                };
                resultModel.addRow(row);
                rowIndexByKey.put(key, resultModel.getRowCount()-1);
            }
        } else {
            Integer idx = rowIndexByKey.remove(key);
            if (idx != null && idx < resultModel.getRowCount()) {
                resultModel.removeRow(idx);
                // rebuild map
                rowIndexByKey.clear();
                for (int i=0;i<resultModel.getRowCount();i++) {
                    int rr = Integer.parseInt(resultModel.getValueAt(i,0).toString());
                    int cc = Integer.parseInt(resultModel.getValueAt(i,1).toString());
                    rowIndexByKey.put(rr+","+cc, i);
                }
            }
        }
    }

    private void clearAll() {
        tfFluid.setText("");
        totalVolLabel.setText("Total Volume: -");
        statusLabel.setText("Cleared.");
        clearResultTableOnly();
        if (baseDepth != null) showEmptyGrid();
    }
    private void clearResultTableOnly() {
        resultModel.setRowCount(0);
        rowIndexByKey.clear();
    }

    // ====== Helpers ======
    private void showEmptyGrid() {
        JPanel grid = new JPanel(new GridLayout(baseDepth.length, baseDepth[0].length, 4, 4));
        grid.setBackground(BG_MAIN);
        for (int i=0;i<baseDepth.length*baseDepth[0].length;i++) {
            JPanel cell = new JPanel();
            cell.setBackground(Color.WHITE);
            cell.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));
            grid.add(cell);
        }
        dashboardWrap.removeAll();
        dashboardWrap.add(new JScrollPane(grid), BorderLayout.CENTER);
        dashboardWrap.revalidate(); dashboardWrap.repaint();
    }

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
        row.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(14,14));
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        JLabel lb = new JLabel(text);
        row.add(box); row.add(lb);
        return row;
    }
    private javax.swing.border.Border boxBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        title),
                new EmptyBorder(8,8,8,8)
        );
    }
    private JPanel placeholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(TXT_SECONDARY);
        l.setFont(l.getFont().deriveFont(Font.ITALIC, 16f));
        p.add(l, BorderLayout.CENTER);
        return p;
    }
    private void stylePrimary(JButton b) {
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }
    private void styleGhost(JButton b) {
        b.setBackground(new Color(245,245,245));
        b.setForeground(TXT_PRIMARY);
        b.setFocusPainted(false);
    }

    // ====== PillCell (ปุ่มพิลล์สีเต็ม) ======
    static class PillCell extends JToggleButton {
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
            setToolTipText("คลิกเพื่อเพิ่ม/ลบจาก Result");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int x = INSET, y = INSET, ww = w - INSET*2, hh = h - INSET*2;
            g2.setColor(getBackground());
            g2.fillRoundRect(x, y, ww, hh, RADIUS, RADIUS);
            String text = DFP.format(percent);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(getForeground());
            g2.setFont(getFont());
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }

    // กำหนดสี/ตัวอักษรของพิลล์ตามเปอร์เซ็นต์
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
    b.setText(DFP.format(pct)); 
    b.setToolTipText("คลิกเพื่อเพิ่ม/ลบจาก Result");
    }

    // main
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new GasVolumeDashboardGUI().setVisible(true));
    }
}

