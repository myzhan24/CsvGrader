package csvgrader;

import java.util.Comparator;

public class Trajectory implements Comparable {
    double x;
    double y;
    String id;
    double timestamp;


    public Trajectory(double xx, double yy, String idd, double ts) {
        x = xx;
        y = yy;
        id = idd;
        timestamp = ts;
    }

    public String toString() {
        return "(" + x + ", " + y + ")" + "\tID: " + id + "\tTS: " + timestamp;
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getTimeStamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Object arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static Comparator<Trajectory> TSComparator = new Comparator<Trajectory>() {

        public int compare(Trajectory traj1, Trajectory traj2) {

            double t1 = traj1.getTimeStamp();
            double t2 = traj2.getTimeStamp();

            //ascending order
            return (int) (t1 - t2);

            //descending order
            //return fruitName2.compareTo(fruitName1);
        }

    };
}
