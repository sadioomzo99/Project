import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MyGraph implements Graph{
    ArrayList<Integer> vertex= new ArrayList<>();
    HashMap<Integer, ArrayList<Integer>> edges = new HashMap<>();
    
    public MyGraph(String filename) throws Exception{
        loadData(filename);
    }



    private void loadData(String filename) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filename));
        while (scanner.hasNextLine()){
           lines.add(scanner.nextLine());
        }
        for(String s:lines){
            String[] temp = s.split(" ");
            addVertex(Integer.parseInt(temp[0]));
            addVertex(Integer.parseInt(temp[1]));
            addEdge(Integer.parseInt(temp[0]),Integer.parseInt(temp[1]));
        }
    }

    public MyGraph() {
    }

    @Override
    public void addVertex(Integer v) {
     if(!contains(v) || v<0){
     vertex.add(v);
     edges.put(v,new ArrayList<Integer>());
     }
    }

    @Override
    public void addEdge(Integer v, Integer w) {
if (edges.containsKey(v) && edges.containsKey(w)){
    if(!edges.get(v).contains(w) && !edges.get(w).contains(v)) {
        edges.get(v).add(w);
        edges.get(w).add(v);
    }
}
    }

    @Override
    public void deleteVertex(Integer v) {
        if(contains(v)) {
            vertex.remove(v);
            ArrayList<Integer> temp =edges.get(v);
            edges.remove(v);
            for (Integer i : temp){
                if(edges.get(i)!=null){
                    edges.get(i).remove(v);
                }
            }

        }
    }

    @Override
    public void deleteEdge(Integer u, Integer v) {
        if (edges.containsKey(u) && edges.containsKey(v)) {
            edges.get(v).remove(u);
            edges.get(u).remove(v);
        }
    }

    @Override
    public boolean contains(Integer v) {
        return edges.containsKey(v);
    }

    @Override
    public int degree(Integer v) {
        if(contains(v)) {
            return edges.get(v).size();
        } else {
            return 0;
        }
    }

    @Override
    public boolean adjacent(Integer v, Integer w) {
        return edges.get(v).contains(w) && edges.get(w).contains(v);
    }

    @Override
    public Graph getCopy() {
        Graph newGraph = new MyGraph();
        for(Integer i: vertex) {
            newGraph.addVertex(i);
            for(Integer v: edges.get(i))
            newGraph.addEdge(i,v);
        }

        return newGraph;
    }

    @Override
    public Set<Integer> getNeighbors(Integer v) {
        if(contains(v)) {
            return new HashSet<>(edges.get(v));
        }
        return null;
    }

    @Override
    public int size() {
        return vertex.size();
    }

    @Override
    public int getEdgeCount() {
        int count=0;
        for(Integer i: edges.keySet()){
            count += edges.get(i).size();
        }
        return count /2;
    }

    @Override
    public Set<Integer> getVertices() {
        return new HashSet<>(vertex);
    }
}
