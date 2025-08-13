public class Main {
    public static void main(String[] args) throws Exception {
    Graph graph =new MyGraph("C:\\Users\\sadio\\Downloads\\sample.sec");
    SearchTree st = new SearchTree();
     int i = st.solve(graph.getCopy());

     System.out.println("Ergebnis: " + i);
    }

}
