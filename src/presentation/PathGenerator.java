package presentation;

import domain.NodeType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class PathGenerator extends JPanel implements ActionListener{

    private PresentationController presentationController;
    private MainView mainView;
    protected ArrayList<Integer> actualRS = new ArrayList<Integer>();
    protected NodeType from = null;

    private JPanel panel;

    private RoundButton label;
    private RoundButton term;
    private RoundButton paper;
    private RoundButton author;
    private RoundButton conference;
    private RoundButton[] buttons;

    private JButton resetButton;
    private JLabel pathLabel;

    private static String LABEL = "Lab";
    private static String AUTHOR = "Aut";
    private static String PAPER = "Pap";
    private static String CONFERENCE = "Con";
    private static String TERM = "Ter";

    protected PathGenerator(PresentationController presentationController, MainView mainView) {
        this.presentationController = presentationController;
        this.mainView = mainView;
        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        resetButton.setVisible(true);
        NodeType to = getNodeType(event.getActionCommand());
        PresentationController.PathInfo info = presentationController.getPathInfo(from, to);
        if(info.availableRelations.size() == 1) {
            actualRS.add(info.availableRelations.get(0));
        } else if(info.availableRelations.size() > 1) {
            ArrayList<String> relationsName = new ArrayList<String>();
            for(int i = 0; i < info.availableRelations.size(); ++i) {
                relationsName.add(presentationController.getRelationName(info.availableRelations.get(i)));
            }
            RelationSelection relationSelection = new RelationSelection(info.availableRelations, relationsName);
            actualRS.add(relationSelection.getIdChosen());
        }
        setEnabledButtons(false);
        for(int i = 0; i < info.availableNodeTypes.size(); ++i) {
            int id = getButtonID(info.availableNodeTypes.get(i));
            buttons[id].setEnabled(true);
        }
        if(actualRS.size() > 0) {
            if(actualRS.size() == 1) {
                pathLabel.setText(from.toString());
            }
            pathLabel.setText(pathLabel.getText() + " -> (" + presentationController.getRelationName(actualRS.get(actualRS.size()-1)) + ") -> " + to.toString());
        }
        from = to;
        mainView.update();
    }

    public void reset() {
        resetButton.setVisible(false);
        from = null;
        setEnabledButtons(true);
        actualRS = new ArrayList<Integer>();
        pathLabel.setText("");
        mainView.update();
    }

    private void createUIComponents() {
        label = new RoundButton(LABEL, Color.YELLOW);
        author = new RoundButton(AUTHOR, Color.YELLOW);
        paper = new RoundButton(PAPER, Color.YELLOW);
        conference = new RoundButton(CONFERENCE, Color.YELLOW);
        term = new RoundButton(TERM, Color.YELLOW);
        buttons = new RoundButton[]{label, author, paper, conference, term};
        for(int id = 0; id < buttons.length; ++id) {
            buttons[id].addActionListener(this);
            buttons[id].setPreferredSize(new Dimension(100, 100));
            buttons[id].setFont(new Font("Arial", Font.PLAIN, 20));
        }

        resetButton = new JButton("Reset");
        resetButton.setVisible(false);
        resetButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        reset();
                    }
                }
        );
    }

    private void setEnabledButtons(boolean enable) {
        for(int id = 0; id < buttons.length; ++id) {
            buttons[id].setEnabled(enable);
        }
    }

    private int getButtonID(NodeType type) {
        if(type == NodeType.LABEL) {
            return 0;
        } else if(type == NodeType.AUTHOR) {
            return 1;
        } else if(type == NodeType.PAPER) {
            return 2;
        } else if(type == NodeType.CONF) {
            return 3;
        } else {
            return 4;
        }
    }

    private NodeType getNodeType(String type) {
        if(type.equals(LABEL)) {
            return NodeType.LABEL;
        } else if(type.equals(AUTHOR)) {
            return NodeType.AUTHOR;
        } else if(type.equals(PAPER)) {
            return NodeType.PAPER;
        } else if(type.equals(CONFERENCE)) {
            return NodeType.CONF;
        } else {
            return NodeType.TERM;
        }
    }
}
