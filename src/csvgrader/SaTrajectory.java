package csvgrader;

import java.util.Comparator;

public class SaTrajectory implements Comparable {
    String id;
    int distBinId;
    int timeBinId;
    int answer;
    double distance;

    double timestamp;

    public SaTrajectory(double distance, String idd, double ts, int aw, int dbi, int tbi) {
        this.distance = distance;
        id = idd;
        timestamp = ts;

        answer = aw;
        distBinId = dbi;
        timeBinId = tbi;
    }


    public double getDistance() {
        return distance;
    }

    public void modifyTimeStamp(double t) {
        timestamp = timestamp + t;
    }

    public int getAnswer() {
        return answer;
    }

    public String toString() {
        return "ID: " + id + "\tTS: " + timestamp + "\tDIST: " + distance;
    }

    public String getId() {
        return id;
    }

    public double getTimeStamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Object arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public static Comparator<AnswerTrajectory> TSComparator = new Comparator<AnswerTrajectory>() {

        public int compare(AnswerTrajectory traj1, AnswerTrajectory traj2) {

            double t1 = traj1.getTimeStamp();
            double t2 = traj2.getTimeStamp();

            //ascending order
            return (int) (t1 - t2);

            //descending order
            //return fruitName2.compareTo(fruitName1);
        }

    };
}
