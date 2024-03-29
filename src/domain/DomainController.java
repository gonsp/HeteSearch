package domain;

import persistence.PersistenceController;

import java.util.ArrayList;
import java.util.HashSet;

public class DomainController {

    private Graph graph;
    private PersistenceController persistenceController;

    public DomainController() {
        newDB();
    }

    public void newDB() {
        graph = new Graph();
    }

    public void importDB(String path) {
        graph = new Graph();
        persistenceController = new PersistenceController(graph);
        persistenceController.importGraph(path);
    }

    public void exportDB(String path) {
        persistenceController = new PersistenceController(graph);
        persistenceController.exportGraph(path);
    }

    public int addNode(NodeType type, String value) {
        Node node = graph.createNode(type, value);
        graph.addNode(node);
        return node.getId();
    }

    public void removeNode(NodeType type, int id) {
        try {
            graph.removeNode(type, id);
        } catch (GraphException e) {
            e.printStackTrace();
        }
    }

    public int[] getNodes(NodeType type) {
        int[] nodesID = new int[graph.getSize(type)];
        Container<Node>.ContainerIterator iterator = graph.getNodeIterator(type);
        int i = 0;
        while(iterator.hasNext()) {
            Node node = iterator.next();
            nodesID[i++] = node.getId();
        }
        return nodesID;
    }

    public String getNodeValue(NodeType type, int id) {
        try {
            return graph.getNode(type, id).getValue();
        } catch (GraphException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setNodeValue(NodeType type, int id, String value) {
        try {
            graph.getNode(type, id).setValue(value);
        } catch (GraphException e) {
            e.printStackTrace();
        }
    }

    public boolean nodeExists(NodeType type, int id) {
        try {
            Node node = graph.getNode(type, id);
            return true;
        } catch (GraphException e) {
            return false;
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

    public ArrayList<Integer> getRelations() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        Container<Relation>.ContainerIterator iterator = graph.getRelationIterator();
        while(iterator.hasNext()) {
            Relation relation = iterator.next();
            result.add(relation.getId());
        }
        return result;
    }

    public ArrayList<Integer> getRelations(NodeType type) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        Container<Relation>.ContainerIterator iterator = graph.getRelationIterator();
        while(iterator.hasNext()) {
            Relation relation = iterator.next();
            if(relation.getNodeTypeA() == type || relation.getNodeTypeB() == type) {
                result.add(relation.getId());
            }
        }
        return result;
    }

    public String getRelationName(int id) {
        try {
            return graph.getRelation(id).getName();
        } catch (GraphException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isCustomRelation(int id) {
        try {
            return !graph.getRelation(id).isDefault();
        } catch (GraphException e) {
            e.printStackTrace();
            return false;
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

    public ArrayList<Integer> getEdges(int relationID, NodeType type, int nodeID) {
        try {
            return graph.getNode(type, nodeID).getEdges(relationID);
        } catch (GraphException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer[] simpleSearch(NodeType type, String filter) {
        SimpleSearch simpleSearch = new SimpleSearch(graph, type, filter);
        simpleSearch.search();
        Integer[] results = new Integer[simpleSearch.getResults().size()];
        int i = 0;
        for(GraphSearch.Result result : simpleSearch.getResults()) {
            results[i++] = result.from.getId();
        }
        return results;
    }

    public ArrayList<Number[]> freeSearch(NodeType typeA, ArrayList<Integer> relationStructure, NodeType typeB) {
        try {
            FreeSearch freeSearch = new FreeSearch(graph, generateRelationStructure(typeA, relationStructure, typeB));
            freeSearch.search();
            ArrayList<Number[]> results = new ArrayList<Number[]>();
            for(GraphSearch.Result result : freeSearch.getResults()) {
                results.add(new Number[]{result.from.getId(), result.to.getId(), result.hetesim});
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Number[]> originSearch(NodeType typeA, int nodeFrom, ArrayList<Integer> rs, NodeType typeB) {
        try {
            RelationStructure relationStructure = generateRelationStructure(typeA, rs, typeB);
            OriginSearch originSearch = new OriginSearch(graph, relationStructure, graph.getNode(typeA, nodeFrom));
            originSearch.search();
            ArrayList<Number[]> results = new ArrayList<Number[]>();
            for(GraphSearch.Result result : originSearch.getResults()) {
                results.add(new Number[]{result.from.getId(), result.to.getId(), result.hetesim});
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Number[] originDestinationSearch(NodeType typeA, int nodeFrom, ArrayList<Integer> rs, NodeType typeB, int nodeTo) {
        try {
            RelationStructure relationStructure = generateRelationStructure(typeA, rs, typeB);
            OriginDestinationSearch originDestinationSearch = new OriginDestinationSearch(graph, relationStructure, graph.getNode(typeA, nodeFrom), graph.getNode(typeB, nodeTo));
            originDestinationSearch.search();
            return new Number[]{nodeFrom, nodeTo, originDestinationSearch.getResults().get(0).hetesim};
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

    public NodeType[] getNodeTypesFromRelation(int relationID) {
        try {
            Relation relation = graph.getRelation(relationID);
            return new NodeType[]{relation.getNodeTypeA(), relation.getNodeTypeB()};
        } catch (GraphException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getSize(NodeType type) {
        return graph.getSize(type);
    }
}
