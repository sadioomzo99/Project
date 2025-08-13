public class Line implements Comparable<Line> {
    final int startX;
    final int startY;
    final int endX;
    final  int endY;
    final public double length;


    public Line(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        int[]p1 = {startX,startY};
        int[]p2 = {endX,endY};
        this.length=Distance(p1,p2);
    }
   public  double Distance(int[] p1, int[] p2){
        return Math.sqrt(Math.pow((p1[0]-p2[0]),2)+Math.pow((p1[1]-p2[1]),2));
   }

    @Override
    public int compareTo(Line o) {
        int[]p1 = {startX,startY};
        int[]p2 = {0,0};
        int[]p3 = { o.startX, o.startY};
        if(this.length==o.length){
            if (Distance(p3,p2)==Distance(p1,p2)){
                if (this.startX==o.startX){
                    return Integer.compare(this.startY, o.startY);

                }else if(this.startX>o.startX){
                    return 1;
                }else{
                    return -1;
                }
            }else if(Distance(p1,p2)>Distance(p3,p2)){
                return 1;
            }else{
                return -1;
            }
        }else if(this.length>o.length){
          return 1;
        }else {
            return -1;
        }
    }
}
