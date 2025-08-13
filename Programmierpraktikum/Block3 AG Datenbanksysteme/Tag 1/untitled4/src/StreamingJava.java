import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamingJava {
    // Aufgabe 2) a)
    public static <E> Stream<E> flatStreamOf(List<List<E>> list) {

        return list.stream().flatMap(Collection::stream);
    }

    // Aufgabe 2) b)
    public static <E> Stream<E> mergeStreamsOf(Stream<Stream<E>> stream) {

        return stream.reduce(Stream.empty(), Stream::concat);
    }

    // Aufgabe 2) c)
    public static <E extends Comparable<? super E>> E minOf(List<List<E>> list) {
        // TODO
        return list.parallelStream().flatMap(Collection::stream).min(Comparator.naturalOrder()).orElseThrow(NoSuchElementException::new);
    }

    // Aufgabe 2) d)
    public static <E> E lastWithOf(Stream<E> stream, Predicate<? super E> predicate) {
        // TODO
        ;
        return (E) stream.filter(predicate).reduce(null,(first, second) -> second);
    }

    // Aufgabe 2) e)
    public static <E> Set<E> findOfCount(Stream<E> stream, int count) {
        // TODO

        Map<E, Long> result = stream.collect(Collectors.groupingBy(Function.identity(),
                Collectors.counting()));
        return result.keySet().stream().filter(x -> result.get(x) == count).collect(Collectors.toSet());


    }

    // Aufgabe 2) f)
    public static IntStream makeStreamOf(String[] strings) {
        // TODO

        return Stream.of(strings).flatMap(x -> x.chars().mapToObj(i -> (char) i)).flatMapToInt(IntStream::of);
    }

//-------------------------------------------------------------------------------------------------

    // Aufgabe 3) a)
    public static Stream<String> fileLines(String path) throws IOException {

        // TODO
        BufferedReader reader = Files.newBufferedReader(Paths.get(path));
        return reader.lines().sequential().skip(1).onClose(()-> System.out.println("Stream Closed."));

    }

    // Aufgabe 3) b)
    public static double averageCost(Stream<String> lines) {
        List<String[]> lines1 = lines.map(l -> l.split(","))
                .collect(Collectors.toList());


        DoubleStream ds  = lines1.stream().mapToDouble(row -> Double.parseDouble(row[row.length - 1]));

        return ds.average().orElse(-1.0);

    }

    // Aufgabe 3) c)
    public static long countCleanEnergyLevy(Stream<String> stream) {
        // TODO
        List<String[]> lines1 = stream.map(l -> l.split(","))
                .collect(Collectors.toList());

        return lines1.stream().map(row -> row[row.length- 3]).filter(x -> x.isEmpty() || x.equals("0")).count();
    }

    // Aufgabe 3) d)
    // TODO:
    //  1. Create record "NaturalGasBilling".
    //  2. Implement static method: "Stream<NaturalGasBilling> orderByInvoiceDateDesc(Stream<String> stream)".
    static record NaturalGasBilling(String InvoiceDate,
                                    String FromDate,
                                    String ToDate,
                                    String BillingDays,
                                    String BilledGJ,
                                    String Basiccharge,
                                    String Deliverycharges,
                                    String Storageandtransport,
                                    String Commoditycharges,
                                    String Tax,
                                    String Cleanenergylevy,
                                    String Carbontax,
                                    String Amount) {





            Stream<Byte> toBytes() {
            return
                    Stream.of(this.InvoiceDate, ",",
                            this.FromDate, ",",
                            this.ToDate, ",",
                            this.BillingDays, ",",
                            this.BilledGJ, ",",
                            this.Basiccharge, ",",
                            this.Deliverycharges, ",",
                            this.Storageandtransport, ",",
                            this.Commoditycharges, ",",
                            this.Tax, ",",
                            this.Cleanenergylevy, ",",
                            this.Carbontax, ",",
                            this.Amount,
                            "\n").flatMapToInt(String::chars).mapToObj(m -> (byte) m);
        }

    }
     public NaturalGasBilling loadNaturalGasBilling(String s){
        String[] x =  s.split(",");
        return new NaturalGasBilling(x[0],x[1],x[2],x[3],x[4],x[5],x[6],x[7],x[8],x[9],x[10],x[11],x[12]);
    }

    Stream<NaturalGasBilling> orderByInvoiceDateDesc(Stream<String>  stream){
        Stream<NaturalGasBilling> ngb=stream.map(this::loadNaturalGasBilling);
        return ngb.sorted(Comparator.comparing(NaturalGasBilling::InvoiceDate).reversed());
    }

    // Aufgabe 3) e)
    // TODO: Implement object method: "Stream<Byte> toBytes()" for record "NaturalGasBilling".

    Stream<Byte> serialize(Stream<NaturalGasBilling> stream) throws FileNotFoundException {
    Stream<Byte> stream1 =Stream.of("Invoice Date",",","From Date",",","To Date",",","Billing Days",",","Billed GJ",",","Basic charge",",",
            "Delivery charges",",","Storage and transport",",","Commodity charges",",","Tax",",","Clean energy levy",",","Carbon tax",",","Amount","\n") //164
            .flatMapToInt(String::chars).mapToObj(m-> (byte) m);
        Stream<Byte> stream2= stream.map(NaturalGasBilling::toBytes).reduce(Stream.empty(), Stream::concat);
       return Stream.concat(stream1,stream2);
        //return Stream.of(stream1,stream2).reduce(Stream.empty(), Stream::concat);


    }

    // Aufgabe 3) f)
    // TODO: Implement static method: "Stream<Byte> serialize(Stream<NaturalGasBilling> stream)".

    // Aufgabe 3) g)
    // TODO: Implement static method: "Stream<NaturalGasBilling> deserialize(Stream<Byte> stream)".
    // TODO: Execute the call: "deserialize(serialize(orderByInvoiceDateDesc(fileLines(Datei aus f))))"
    // TODO: in a main Method and print the output to the console.
    Stream<NaturalGasBilling> deserialize(Stream<Byte> stream){
        return deserializeData(stream.skip(164));
    }
    Stream<NaturalGasBilling> deserializeData(Stream<Byte> stream){
       List<Byte> lines =stream.collect(Collectors.toList());
        StringBuilder stringBuilder=new StringBuilder();
        Stream<NaturalGasBilling> stream1= Stream.of();
int count=0;
        if (lines.isEmpty()){
            return Stream.empty();
        }

        for(Byte s :lines){
            count++;
            if(s!=10){
                stringBuilder.append((char) s.byteValue());

            }else{
                break;
            }
        }

        int i=0;
        while(i<count){
             lines.remove(0);
             i++;
        }

        String string= stringBuilder.toString();
        return Stream.concat(stream1,Stream.concat(Stream.of(loadNaturalGasBilling(string)), deserializeData(lines.stream())));

    }
    // Aufgabe 3) h)
    public static Stream<File> findFilesWith(String dir, String startsWith, String endsWith, int maxFiles) throws IOException {

        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
           // paths.filter(Files::isRegularFile).filter(x->x.getFileName().toString().startsWith(startsWith)&& x.getFileName().toString().endsWith(endsWith))
            //       .sorted(Comparator.comparing(x->x.toFile().length())).sorted(Comparator.reverseOrder()).limit(maxFiles).map(Path::toFile).forEach(x-> System.out.println(x.getName()));
            return paths.sequential()
                    .filter(Files::isRegularFile)
                    .filter(x->x.getFileName().toString().startsWith(startsWith)&& x.getFileName().toString().endsWith(endsWith))
                    .sorted(Comparator.comparingLong(x->x.toFile().length()))
                    .sorted(Comparator.reverseOrder())
                    .limit(maxFiles)
                    .map(Path::toFile);
        }



    }

    public static void main(String[] args) throws IOException {
        List<Integer> l1 = Arrays.asList(1,2,3,3,3,4,4);
        List<Integer> l2 = Arrays.asList(4,5,6);
        List<List<Integer>> list = List.of(l1,l2);
        String[] ss= new String[]{"a","1"};
        Stream<Integer> s1 = l1.stream();
        Stream<Integer> s2 = l2.stream();
        Stream<Stream<Integer>> stream= Stream.of(s1,s2);
        Stream<String> data = fileLines("C:\\Users\\sadio\\Downloads\\NaturalGasBilling.csv");
        String p= "\r";
        Byte aByte = 97;
//data.forEach(System.out::println);
    //System.out.println(( countCleanEnergyLevy(data)));

       // s.close();
        //mergeStreamsOf(stream).forEach(System.out::println);
        NaturalGasBilling ngb = new NaturalGasBilling("2012-04-19", "2012-03-19", "2012-04-19", "31", "3.2", "12.06", "11.29", "4.37", "10.76", "2.12","" , "3.97", "44.57");
    // System.out.println( ngb.toBytes().count());//79
            // ngb.toBytes().forEach(System.out::println);
Stream<NaturalGasBilling> sbgb= Stream.of(ngb);
        StreamingJava d = new StreamingJava();
d.deserialize(d.serialize(d.orderByInvoiceDateDesc(data))).forEach(System.out::println);

      //d.deserialize(d.serialize(sbgb)).forEach(System.out::println);
      // d.deserialize(d.serialize(d.orderByInvoiceDateDesc(data))).forEach(System.out::println);
     //   makeStreamOf(ss).forEach(System.out::println);;
       // System.out.println(findOfCount(s1, 2));
       // System.out.println((minOf(list)));
      //  System.out.println(lastWithOf(s1, new Predicate<Integer>() {
      //      @Override
      //      public boolean test(Integer integer) {
        //        return integer>=2;
      //      }
      //  }));

     //   flatStreamOf(list).forEach(System.out::println);



       // System.out.println(findFilesWith("C:\\Users\\sadio\\Bureau\\new", "st", "dm.txt", 2));
//findFilesWith("C:\\Users\\sadio\\Bureau\\new", "st", "dm.txt", 2).forEach(System.out::println);
        //System.out.println(fileStream.collect(Collectors.toList()));
    }
}
