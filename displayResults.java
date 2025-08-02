import java.util.ArrayList;

import javax.swing.BorderFactory;

private void displayResults(ArrayList<Double> baseDepths){
            displayPanel.removeAll();
           int dataSize = baseDepths.size();
           int gridSize = (int) Math.ceil(Math.sqrt(dataSize));
           JPanel gridPanel = new JPanel(new GridLayout(gridSize,gridSize,4,4));
           gridPanel.setBackground(COLOR_BACKGROUND);

           for(int i = ; i < dataSize; i++ ){
                JPanel cellPanel = new JPanel();
                cellPanel.setBackground(Color.WHITE);
                cellPanel.Border(BorderFactory.createLineBorder(new Color(220,220,220)));
                gridPanel.add(cellPanel);

              }
                 displayPanel.add(new JScrollPane(gridPanel));
                 displayPanel.revalidate();
                 displayPanel.repaint();
}
