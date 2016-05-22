package domain;

import persistence.PersistenceController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class DomainController {

    private Graph graph;
    private PersistenceController persistenceController;

    public DomainController() {
        newDB();
        persistenceController = new PersistenceController(graph);
    }

    public DomainController(String path) {
        persistenceController = new PersistenceController(graph);
        importDB(path);
    }

    public void newDB() {
        graph = new Graph();
    }

    public void importDB(String path) {
        graph = new Graph();
        persistenceController.importGraph(path);
    }

    public void exportDB(String path) {
        persistenceController.exportGraph(path);
    }

    public void addNode(NodeType type, String value) {
        Node node = graph.createNode(type, value);
        graph.addNode(node);
    }

    public void removeNode(NodeType type, int id) {
        try {
            graph.removeNode(type, id);
        } catch (GraphException e) {
            e.printStackTrace();
        }
    }

    public int addRelation(NodeType A, NodeType B, String name) {
        Relation relation = new Relation(A, B, name);
        graph.addRelation(relation);
        return relation.getId();
    }

    public void removeRelation(int id) {
        try {
            graph.removeRelation(id);
        } catch (GraphException e) {
            e.printStackTrace();
        }
    }

    public String getRelationName(int id) {
        try {
            return graph.getRelation(id).getName();
        } catch (GraphException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addEdge(int relationID, NodeType typeA, int nodeA, NodeType typeB, int nodeB) {
        try {
            graph.addEdge(relationID, typeA, nodeA, typeB, nodeB);
        } catch (GraphException e) {
            e.printStackTrace();
        }
    }

    public void removeEdge(int relationID, NodeType typeA, int nodeA, NodeType typeB, int nodeB) {
        try {
            graph.removeEdge(relationID, typeA, nodeA, typeB, nodeB);
        } catch (GraphException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Node> getEdges(int relationID, Node node) {
        try {
            return graph.getEdges(relationID, node);
        } catch (GraphException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<GraphSearch.Result> simpleSearch(NodeType type, String filter) {
        SimpleSearch simpleSearch = new SimpleSearch(graph, type, filter);
        simpleSearch.search();
        return simpleSearch.getResults();
    }

    public ArrayList<GraphSearch.Result> freeSearch(NodeType typeA, ArrayList<Integer> relationStructure, NodeType typeB) {
        try {
            FreeSearch freeSearch = new FreeSearch(graph, generateRelationStructure(typeA, relationStructure, typeB));
            freeSearch.search();
            return freeSearch.getResults();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<GraphSearch.Result> originSearch(NodeType typeA, int nodeFrom, ArrayList<Integer> rs, NodeType typeB) {
        try {
            RelationStructure relationStructure = generateRelationStructure(typeA, rs, typeB);
            OriginSearch originSearch = new OriginSearch(graph, relationStructure, graph.getNode(typeA, nodeFrom));
            originSearch.search();
            return originSearch.getResults();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<GraphSearch.Result> originDestinationSearch(NodeType typeA, int nodeFrom, ArrayList<Integer> rs, NodeType typeB, int nodeTo) {
        try {
            RelationStructure relationStructure = generateRelationStructure(typeA, rs, typeB);
            OriginDestinationSearch originDestinationSearch = new OriginDestinationSearch(graph, relationStructure, graph.getNode(typeA, nodeFrom), graph.getNode(typeB, nodeTo));
            originDestinationSearch.search();
            return originDestinationSearch.getResults();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RelationStructure generateRelationStructure(NodeType typeA, ArrayList<Integer> relationPath, NodeType typeB) throws RelationStructureException, GraphException {
        ArrayList<Relation> rs = new ArrayList<Relation>();
        for(int i = 0; i < relationPath.size(); ++i) {
            rs.add(graph.getRelation(relationPath.get(i)));
        }
        return new RelationStructure(typeA, rs, typeB);
    }

    public ArrayList<Integer> getAvailableRelations(NodeType from, NodeType to) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        Container<Relation>.ContainerIterator iterator = graph.getRelationIterator();
        while(iterator.hasNext()) {
            Relation relation = iterator.next();
            if((relation.getNodeTypeA() == from && relation.getNodeTypeB() == to) || (relation.getNodeTypeA() == to && relation.getNodeTypeB() == from)) {
                result.add(relation.getId());
            }
        }
        return result;
    }

    public ArrayList<NodeType> getAvailableNodeTypes(NodeType from) {
        HashSet<NodeType> availableNodeTypes = new HashSet<NodeType>();
        Container<Relation>.ContainerIterator iterator = graph.getRelationIterator();
        int types = 0;
        while(iterator.hasNext() && types < 5) {
            Relation relation = iterator.next();
            if(from == relation.getNodeTypeA()) {
                availableNodeTypes.add(relation.getNodeTypeB());
                ++types;
            } else if(from == relation.getNodeTypeB()) {
                availableNodeTypes.add(relation.getNodeTypeA());
                ++types;
            }
        }
        return new ArrayList<NodeType>(availableNodeTypes);
    }
}
