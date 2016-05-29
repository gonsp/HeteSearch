package presentation;


import domain.NodeType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import presentation.utils.CustomMouseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;

public class GraphView extends JPanel implements ViewerListener {

    private Graph graph;
    private Viewer viewer;
    private ViewPanel panel;
    private ViewerPipe pipe;
    private Camera camera;
    private PresentationController presentationController;
    private int lastEdgeID;
    private HashMap<Integer, Color> relationColors;
    private static int MAX_NODES = 300;

    public GraphView(PresentationController presentationController) {
        super(new CardLayout());
        this.presentationController = presentationController;
        graph = new MultiGraph("GraphView");
        graph.setAutoCreate(true);
        graph.setStrict(false);
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        panel = viewer.addDefaultView(false);
        setBackground(Color.WHITE);
        add(panel, "graph");
        add(new JLabel(
                "<html><h3 style=\"text-align: center;\">The graph is too big</h3>\n<p style=\"text-align: center;\">It won't be shown entirely unless it decreases.<br>However, If you do a search and select a result, the result's related nodes will be shown</p></html>",
                SwingConstants.CENTER),
                "big");
        add(new JLabel(
                "<html><h3 style=\"text-align: center;\">The graph is empty</h3>\n<p style=\"text-align: center;\">Add some elements to see data or import an existing graph</p></html>",
                SwingConstants.CENTER),
                "empty");
        panel.setMouseManager(new CustomMouseManager());
        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double newZoom = camera.getViewPercent()+e.getWheelRotation()*0.1;
                if(newZoom >= 0.1 && newZoom <= 5) {
                    camera.setViewPercent(newZoom);
                }
            }
        });
        camera = panel.getCamera();
        pipe = viewer.newViewerPipe();
        pipe.addViewerListener(this);
        pipe.addSink(graph);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            pipe.pump();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();
        generateRelationColors();
        refresh();
    }


    public void refresh() {
        graph.clear();
        graph.setAttribute("stylesheet",
                "node { "
                + "     shape: rounded-box; "
                + "     padding: 5px; "
                + "     fill-color: rgba(0,125,164,100); "
                + "     stroke-mode: plain; "
                + "     size-mode: fit; "
                + "     text-size: 16px; "
                + "} "
                + "edge { "
                + "     text-size: 14px; "
                + "} "
                + "graph { "
                + "     padding: 40px; "
                + "}"
        );
        lastEdgeID = 0;
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        if(generateGraph()) {
            if(presentationController.getSize() > 0) {
                ((CardLayout)getLayout()).show(this, "graph");
            } else {
                ((CardLayout)getLayout()).show(this, "empty");
            }
        } else {
            ((CardLayout)getLayout()).show(this, "big");
        }
    }

    private boolean generateGraph() {
        if(presentationController.getSize() > MAX_NODES) {
            return false;
        }
        for(NodeType type : NodeType.values()) {
            int[] ids = presentationController.getNodes(type);
            for(int i = 0; i < ids.length; ++i) {
                Node node = graph.addNode(type.toString() + "_" + String.valueOf(ids[i]));
                node.addAttribute("nodetype", type);
                node.addAttribute("originalID", ids[i]);
                node.addAttribute("ui.style", "fill-color: " + getNodeColor(type) + ";");
                String label = presentationController.getNodeValue(type, ids[i]);
                if(label.length() > 15) {
                    label = label.substring(0, 15);
                    label = label.concat("...");
                }
                node.addAttribute("ui.label", label);
            }
        }
        for(Node from : graph.getNodeSet()) {
            NodeType typeFrom = from.getAttribute("nodetype");
            int nodeID = from.getAttribute("originalID");
            for(int relationID : presentationController.getRelations(typeFrom)) {
                for(int nodeTo : presentationController.getEdges(relationID, typeFrom, nodeID)) {
                    addEdge(from, graph.getNode(String.valueOf(presentationController.getNodeTypeTo(relationID, typeFrom).toString() + "_" + nodeTo)), relationID);
                }
            }
        }
        lastNode = null;
        return true;
    }

    private boolean addEdge(Node from, Node to, int relationID) {
        for(Edge edge : from.getEachEdge()) {
            if(edge.getOpposite(from) == to) {
                if((int) edge.getAttribute("relationID") == relationID) {
                    return false;
                }
            }
        }
        Edge edge = graph.addEdge(String.valueOf(++lastEdgeID), from.getId(), to.getId());
        edge.addAttribute("relationID", relationID);
        Color color = relationColors.get(relationID);
        edge.addAttribute("ui.style", "fill-color: rgb(" + String.valueOf(color.getRed()) + ", " + String.valueOf(color.getGreen()) + ", " + String.valueOf(color.getBlue()) + ");");
        return true;
    }

    public void showGraph() {
        ((CardLayout)getParent().getLayout()).show(getParent(), "GraphView");
    }

    private String getNodeColor(NodeType type) {
        if(type == NodeType.AUTHOR) {
            return "red";
        } else if(type == NodeType.PAPER) {
            return "blue";
        } else if(type == NodeType.CONF) {
            return "yellow";
        } else if(type == NodeType.TERM) {
            return "green";
        } else {
            return "cyan";
        }
    }

    private void generateRelationColors() {
        relationColors = new HashMap<Integer, Color>();
        for(int relationID : presentationController.getRelations()) {
            relationColors.put(relationID, new Color((int)(255*Math.random()), (int)(255*Math.random()), (int)(255*Math.random())));
        }
    }

    private void setEdgeLabel(Node node, boolean enabled, Node actNode) {
        for(Edge edge : node.getEachEdge()) {
            if(enabled) {
                edge.addAttribute("ui.label", presentationController.getRelationName(edge.getAttribute("relationID")));
            } else {
                if(actNode == null || edge.getOpposite(node) != actNode) {
                    edge.removeAttribute("ui.label");
                }
            }
        }
    }

    private long lastClick = 0;
    private Node lastNode = null;

    @Override
    public void buttonPushed(String id) {
        System.out.println("Node clicked with id: " + id);
        Node node = graph.getNode(id);
        if(lastNode != null && lastNode != node) {
            setEdgeLabel(lastNode, false, node);
        }
        setEdgeLabel(node, true, null);
        double[] pos = org.graphstream.algorithm.Toolkit.nodePosition(node);
        camera.setViewCenter(pos[0], pos[1], pos[2]);
        long actClick = System.currentTimeMillis();
        if(actClick-lastClick < 400) {
            ModifyElementView modifyElementView = new ModifyElementView(presentationController, presentationController.mainFrame, node.getAttribute("originalID"), node.getAttribute("nodetype"));
            modifyElementView.onOK();
        }
        lastClick = actClick;
        lastNode = node;
    }

    @Override
    public void viewClosed(String viewName) {}

    @Override
    public void buttonReleased(String id) {}
}
