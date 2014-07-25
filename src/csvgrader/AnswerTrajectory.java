package csvgrader;

import java.util.Comparator;

public class AnswerTrajectory implements Comparable{
	double x;
	double y;
	String id;
	int question;
	int answer;
	
	double timestamp;
	
	
	
	public AnswerTrajectory(double xx, double yy,String idd, double ts, int q, int aw)
	{
		x=xx;
		y=yy;
		id=idd;
		timestamp = ts;
		question = q;
		answer = aw;
	}
	public int getQuestion()
	{
		return question;
	}
	public int getAnswer()
	{
		return answer;
	}
	public String toString()
	{
		return "("+x+", "+y+")" + "\tID: "+id+"\tTS: "+timestamp;
	}
	public String getId()
	{
		return id;
	}
	public double getX()
	{
		return x;
	}
	public double getY()
	{
		return y;
	}
	public double getTimeStamp()
	{
		return timestamp;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	public static Comparator<AnswerTrajectory> TSComparator  = new Comparator<AnswerTrajectory>() {

		public int compare(AnswerTrajectory traj1, AnswerTrajectory traj2) {
		
		double t1 = traj1.getTimeStamp();
		double t2 = traj2.getTimeStamp();
		
		//ascending order
		return (int)(t1-t2);
		
		//descending order
		//return fruitName2.compareTo(fruitName1);
		}
		
		};
}
