package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainView extends JFrame {

    private PresentationController presentationController;
    private JPanel panel;
    private PathGenerator pathGenerator;
    private JButton addNodeButton;

    protected MainView(PresentationController presentationController) {
        super("HeteSearch");
        this.presentationController = presentationController;
        initialize();
    }

    private void initialize() {
        setMinimumSize(new Dimension(700, 700));
        //setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contentPane = (JPanel) getContentPane();
        contentPane.add(panel);
        addNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO implement this
            }
        });
        createMenu();
        pack();
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem newGraph = new JMenuItem("New Graph");
        newGraph.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        presentationController.newDB();
                    }
                }
        );
        JMenuItem importGraph = new JMenuItem("Import Graph");
        importGraph.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Importing graph");
                        DataChooser dc = null;
                        try {
                            dc = new DataChooser(importGraph, false);
                            presentationController.newDB();
                            presentationController.importDB(dc.getDirectory());
                            System.out.println("Done");
                        } catch (DataChooserException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
        );
        JMenuItem exportGraph = new JMenuItem("Export Graph");
        exportGraph.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Exporting graph");
                        DataChooser dc = null;
                        try {
                            dc = new DataChooser(exportGraph, true);
                            presentationController.exportDB(dc.getDirectory());
                            System.out.println("Done");
                        } catch (DataChooserException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
        );
        menu.add(newGraph);
        menu.add(importGraph);
        menu.add(exportGraph);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    public void setAddNodeButton(boolean enabled) {
        addNodeButton.setEnabled(enabled );
    }

    private void createUIComponents() {
        pathGenerator = new PathGenerator(presentationController, this);
    }
}