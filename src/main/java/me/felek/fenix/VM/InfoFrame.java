package me.felek.fenix.VM;

import me.felek.fenix.asm.registers.RegisterUtils;
import me.felek.fenix.disk.Disk;
import me.felek.fenix.disk.Sector;
import me.felek.fenix.mem.MemoryBlock;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class InfoFrame {
    private int WIDTH = 1280;
    private int HEIGHT = 720;

    public InfoFrame(MemoryBlock memoryBlock, Disk d) {
        JFrame frame = new JFrame();
        frame.setTitle("FenixVM - INFO");
        frame.setSize(WIDTH, HEIGHT);

        JTabbedPane pane = new JTabbedPane();

        JPanel registers = getRegistersPanel();
        JPanel memory = getMemoryPanel(memoryBlock);
        JPanel disk = getDiskPanel(d);

        pane.addTab("Registers", registers);
        pane.addTab("Memory dump", memory);
        pane.addTab("Disk", disk);

        frame.setJMenuBar(getMenu(d, memoryBlock));

        frame.add(pane);

        frame.setVisible(true);
    }

    public JMenuBar getMenu(Disk disk, MemoryBlock memory) {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem saveDisk = new JMenuItem("Save disk...");
        saveDisk.addActionListener((e) -> {
            JFileChooser save = new JFileChooser();

            save.showSaveDialog(fileMenu);
            disk.saveDisk(save.getSelectedFile().getPath());

            JOptionPane.showMessageDialog(fileMenu, "File saved to: " + save.getSelectedFile().getAbsolutePath());
        });

        fileMenu.add(saveDisk);

        bar.add(fileMenu);

        return bar;
    }

    public JPanel getDiskPanel(Disk disk) {
        JPanel diskPanel = new JPanel(null);

        int totalBytes = disk.sectors.length * Sector.SIZE;
        String[][] data = new String[totalBytes][5];

        int row = 0;
        for (int s = 0; s < disk.sectors.length; s++) {
            Sector currentSector = disk.sectors[s];
            int[] rawData = currentSector.getRaw();
            for (int i = 0; i < Sector.SIZE; i++) {
                int val = rawData[i];

                data[row][0] = String.valueOf(s);
                data[row][1] = String.valueOf(i);
                data[row][2] = String.format("0x%02X", val);
                data[row][3] = String.valueOf(val);

                char c = (char) val;
                data[row][4] = String.valueOf(c);

                row++;
            }
        }

        String[] columns = new String[]{"Sector", "Offset", "Hex", "Decimal", "Char"};

        JTable table = new JTable(data, columns);
        table.setCellSelectionEnabled(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(0, 0, WIDTH - 30, HEIGHT - 70);

        diskPanel.add(sp);

        return diskPanel;
    }

    public JPanel getMemoryPanel(MemoryBlock memoryBlock) {
        JPanel mem = new JPanel(null);
        String[][] data = new String[memoryBlock.getSize()][3];

        for (int i = 0; i < memoryBlock.getSize(); i++) {
            data[i][0] = String.valueOf(i);
            data[i][1] = String.valueOf(memoryBlock.get(i));
            data[i][2] = String.valueOf((char) memoryBlock.get(i));
        }

        String[] columns = new String[]{"Field", "Value", "Character"};

        JTable table = new JTable(data, columns);
        table.setCellSelectionEnabled(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(0, 0, WIDTH-50, HEIGHT-70);

        mem.add(sp);

        return mem;
    }

    public JPanel getRegistersPanel() {
        JPanel reg = new JPanel(null);
        String[][] data = new String[16][2];

        for (int i = 0; i < 16; i++) {
            data[i][0] = String.valueOf(i);
            data[i][1] = String.valueOf(RegisterUtils.getRegisterValue(i));
        }

        String[] columns = new String[]{"Register", "Value"};

        JTable table = new JTable(data, columns);
        table.setCellSelectionEnabled(false);
        table.setBounds(0, 0, WIDTH-50, HEIGHT-70);

        reg.add(table);
        return reg;
    }
}
