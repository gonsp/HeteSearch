package presentation;


import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;

public class GraphView {

    private Graph graph;
    private Viewer viewer;
    private ViewPanel panel;

    public GraphView() {
        graph = new SingleGraph("Graph");
        graph.addNode("A");
        graph.addNode("B");
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        panel = viewer.addDefaultView(false);
    }

    public JPanel getPanel() {
        return panel;
    }
}