import java.util.ArrayList;

public class SearchTree {


    private boolean solve(Instance i){
        if(i.k<0){
            return false;
        }
        if (i.G.getEdgeCount()==0){
            return true;
        }
        //Creation of ArrayList
        ArrayList<Integer> list = new ArrayList<>(i.G.getVertices());
        for (Integer integer : list) {
            i.getG().deleteVertex(integer);
            i.setK(i.k - 1);
            if (solve(new Instance(i.G, i.k))) {
                return true;
            }
        }
        return false;
    }

    public int solve(Graph G){

        int count=0;
        int temp=0;
        float startTime = System.currentTimeMillis();
        System.out.println("Anzahl der Knoten: " + G.size() + "Anzahl der Kanten: " + G.getEdgeCount());
        Integer highestDegreeVertex=-1;
        if (G.size()==0 || G.getEdgeCount()==0){
            return count;
        }

        while(G.getEdgeCount()>0) {
            ArrayList<Integer> list = new ArrayList<>(G.getVertices());
            for (Integer i : list) {

                if (G.degree(i) >= temp) {
                    temp = G.degree(i);
                    highestDegreeVertex = i;
                }
            }
            G.deleteVertex(highestDegreeVertex);
            count++;
            temp=0;
        }
        float runTime = System.currentTimeMillis() - startTime;
        System.out.println("Lösungsgröße: " + count + "  Laufzeit: " + runTime);
        return count;
    }

    public void removeSingletons(Instance instance){
        for (Integer i : instance.G.getVertices()){
            if(instance.G.degree(i)==0){
                instance.G.deleteVertex(i);
            }
        }
    }

    public void removeDegOne(Instance instance){
        for (Integer i : instance.G.getVertices()){
            if(instance.G.degree(i)==1){
                instance.G.deleteVertex(i);
                instance.setK(instance.getK() -1);
            }
        }
    }
    public void removeHighDeg(Instance instance){
        for (Integer i : instance.G.getVertices()) {
            if (instance.G.degree(i) > instance.k) {
                instance.G.deleteVertex(i);
            }
        }

        instance.setK(instance.getK() -1);
    }
    static class Instance{
        Graph G;
        int k;
     public Instance(Graph G,int k){
         this.G =G;
         this.k = k;
     }

        public Graph getG() {
            return G;
        }

        public void setG(Graph g) {
            G = g;
        }

        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }
    }

    public static void main(String[] args) throws Exception {
            Graph graph =new MyGraph("C:\\Users\\sadio\\Downloads\\sample.sec");
            SearchTree st = new SearchTree();
            Instance y = new Instance(graph, 17);
            boolean m = st.solve(y);
            // int i = st.solve(graph.getCopy());

     //       System.out.println("Ergebnis: " + i);
           System.out.println("Ergebnis: " + m);
    }

}
