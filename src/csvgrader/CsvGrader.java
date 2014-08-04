package csvgrader;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.*;

public class CsvGrader {


    //public ArrayList<Trajectory> trajectories;
    public HashMap<String, ArrayList<Trajectory>> trajectories;

    public static void main(String args[]) {
        CsvGrader mycg = new CsvGrader();
        //	mycg.generateGradeDistCutTime("answers.csv","answers.properties","questionLocations.txt","Event1Data",60);
        mycg.trajectories = new HashMap<String, ArrayList<Trajectory>>();
        mycg.readTrajectories("trajectory_points.csv");
        mycg.generateTrajectoryDistances("Event1Data60.csv", "questionLocations.txt", "funfun", 60);
        //mycg.generateTrajectorySpeeds("Event1Data60.csv", "trajspeeds");

    }


    public void readTrajectories(String trajFileName) {
        Scanner csv = null;
        try {
            csv = new Scanner(new File(trajFileName));
            ArrayList<Trajectory> tj = new ArrayList<Trajectory>();
            String currentId = "";
            while (csv.hasNextLine()) {
                String[] line = csv.nextLine().split(",");
                line[0] = line[0].substring(6, line[0].length() - 1);
                String[] locs = line[0].split(" ");

                if (currentId.equals(""))    // first device
                {
                    currentId = line[1];
                } else if (!currentId.equals(line[1])) // we are looking at a new device, add arrayList to the map, reset arrayList
                {
                    trajectories.put(currentId, tj);
                    tj = new ArrayList<Trajectory>();
                    currentId = line[1];
                }
                tj.add(new Trajectory(Double.parseDouble(locs[0]), Double.parseDouble(locs[1]), line[1], Double.parseDouble(line[2])));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateTrajectoryDistances(String csvName, String questionLocationsName, String fileName, double meters) {
        Scanner csv = null;
        Scanner ql = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            ql = new Scanner(new File(questionLocationsName));
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();
            csv.nextLine();
            String currentId = "";
            ArrayList<AnswerTrajectory> anstj = new ArrayList<AnswerTrajectory>();
            ArrayList<String> Ids = new ArrayList<String>();

            ArrayList<Point2D.Double> questionLocations = new ArrayList<Point2D.Double>();

            while (ql.hasNextLine()) {
                questionLocations.add(new Point2D.Double(ql.nextDouble(), ql.nextDouble()));
            }


            while (csv.hasNextLine()) {         //go through each answer entry and add trajectories of the answer list for each unique device id
                String[] line = csv.nextLine().split(",");
                if (currentId.equals("")) {
                    currentId = line[12];
                } else if (!currentId.equals(line[12])) {
                    csvTraj.put(currentId, anstj);
                    Ids.add(currentId);         //if the id is new, then add it to the list of all known ids
                    anstj = new ArrayList<AnswerTrajectory>();
                    currentId = line[12];
                }
                anstj.add(new AnswerTrajectory(Double.parseDouble(line[13]), Double.parseDouble(line[14]), line[12], Double.parseDouble(line[9]), Integer.parseInt(line[0]), Integer.parseInt(line[16])));
            }
            writer.append("Distance,AnswerType\n");

            ArrayList<Trajectory> alltj = new ArrayList<Trajectory>();
            for (String s : Ids)        //for each device ID determine the corresponding answer list
            {
                System.out.println(s);
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds all trajectories for this device
                alltj = trajectories.get(s);                        //get all the trajectories under this Id

                if (anstj == null || alltj == null)
                    System.out.println("Could not find trajectory data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);        //sort it by time
                    Collections.sort(alltj, Trajectory.TSComparator);              //sort it by time



                    int i = 0;
                    int currentQuestion = anstj.get(i).getQuestion();
                    double currentQuestionTimeStamp = anstj.get(i).getTimeStamp();
                    int currentAnswer = anstj.get(i).getAnswer();
                    int correct = 0;
                    if (currentAnswer == 1 || currentAnswer == 2)
                        correct = 1;

                    boolean BC = true;// before the first question
                    int total = 0;
                    for (Trajectory t : alltj) {

                        double timestamp = t.getTimeStamp()*1000;

                        if (BC && timestamp < anstj.get(0).getTimeStamp())    //use first trajectory as if it's before the first question
                        {
                            total++;
                            //determine the distance and answer type
                            double qX = questionLocations.get(currentQuestion - 1).getX();
                            double qY = questionLocations.get(currentQuestion - 1).getY();

                            double tX = t.getX();
                            double tY = t.getY();

                            double dist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));

                            int answerId = 0;
                            if (dist <= meters) {
                                if (correct == 1)
                                    answerId = 1;

                                if (correct == 0)
                                    answerId = 3;
                            } else {
                                if (correct == 1)
                                    answerId = 2;

                                if (correct == 0)
                                    answerId = 4;
                            }

                            writer.append(dist + "," + answerId + "\n");
                            BC = false;
                        }
                        else
                        {

                            break;
                        }

                    }

                    for(int a = 1; a < anstj.size(); a++)   //for questions 1-n
                    {
                        //determine the distance and answer type

                        double qX = questionLocations.get(anstj.get(a).getQuestion() - 1).getX();
                        double qY = questionLocations.get(anstj.get(a).getQuestion() - 1).getY();

                        double tX = anstj.get(a).getX();
                        double tY = anstj.get(a).getY();

                        double dist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));

                        int answerId = 0;
                        if (dist <= meters) {
                            if (correct == 1)
                                answerId = 1;

                            if (correct == 0)
                                answerId = 3;
                        } else {
                            if (correct == 1)
                                answerId = 2;

                            if (correct == 0)
                                answerId = 4;
                        }
                        writer.append(dist + "," + answerId + "\n");
                    }

                }
            }


            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csv != null)
                csv.close();

            if (ql != null)
                ql.close();

        }
    }
/*
    public void generateTrajectoryDistances(String csvName, String questionLocationsName, String fileName, double meters) {
        Scanner csv = null;
        Scanner ql = null;
        try {
            csv = new Scanner(new File(csvName));
            ql = new Scanner(new File(questionLocationsName));
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();
            csv.nextLine();
            String currentId = "";
            ArrayList<AnswerTrajectory> anstj = new ArrayList<AnswerTrajectory>();
            ArrayList<String> Ids = new ArrayList<String>();

            ArrayList<Point2D.Double> questionLocations = new ArrayList<Point2D.Double>();

            while (ql.hasNextLine()) {
                questionLocations.add(new Point2D.Double(ql.nextDouble(), ql.nextDouble()));
            }


            while (csv.hasNextLine()) {
                String[] line = csv.nextLine().split(",");
                if (currentId.equals("")) {
                    currentId = line[12];
                } else if (!currentId.equals(line[12])) {
                    csvTraj.put(currentId, anstj);
                    Ids.add(currentId);
                    anstj = new ArrayList<AnswerTrajectory>();
                    currentId = line[12];
                }
                anstj.add(new AnswerTrajectory(Double.parseDouble(line[13]), Double.parseDouble(line[14]), line[12], Double.parseDouble(line[9]), Integer.parseInt(line[0]), Integer.parseInt(line[16])));
            }
            writer.append("Distance,AnswerType\n");

            ArrayList<Trajectory> alltj = new ArrayList<Trajectory>();
            for (String s : Ids)        //for each device ID determine the corresponding answer list
            {
                //System.out.println(s);
                anstj = csvTraj.get(s);
                alltj = trajectories.get(s);                        //get all the trajectories under this Id

                if (anstj == null || alltj == null)
                    System.out.println("Could not find trajectory data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);        //sort it by time
                    Collections.sort(alltj, Trajectory.TSComparator);            //sort it by time

                    int i = 0;
                    int currentQuestion = anstj.get(i).getQuestion();
                    double currentQuestionTimeStamp = anstj.get(i).getTimeStamp();
                    int currentAnswer = anstj.get(i).getAnswer();
                    int correct = 0;
                    if (currentAnswer == 1 || currentAnswer == 2)
                        correct = 1;

                    for (Trajectory t : alltj) {
                        double timestamp = t.getTimeStamp();

                        if (timestamp <= currentQuestionTimeStamp)    //keep looking at this question
                        {
                            //determine the distance and answer type
                            double qX = questionLocations.get(currentQuestion - 1).getX();
                            double qY = questionLocations.get(currentQuestion - 1).getY();

                            double tX = t.getX();
                            double tY = t.getY();

                            double dist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));

                            int answerId = 0;
                            if (dist <= meters) {
                                if (correct == 1)
                                    answerId = 1;

                                if (correct == 0)
                                    answerId = 3;
                            } else {
                                if (correct == 1)
                                    answerId = 2;

                                if (correct == 0)
                                    answerId = 4;
                            }

                            writer.append(dist + "," + answerId + "\n");
                        } else if (i < anstj.size())    // move onto the next time stamp
                        {
                            i++;
                            currentQuestion = anstj.get(i).getQuestion();
                            currentQuestionTimeStamp = anstj.get(i).getTimeStamp();
                            currentAnswer = anstj.get(i).getAnswer();
                            if (currentAnswer == 1 || currentAnswer == 2)
                                correct = 1;
                        } else {
                            System.out.println("uh oh!");
                        }
                    }

                }
            }


            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csv != null)
                csv.close();

            if (ql != null)
                ql.close();

        }
    }*/

    public void generateTrajectorySpeeds(String csvName, String fileName) {
        Scanner csv = null;
        try {
            csv = new Scanner(new File(csvName));
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();
            csv.nextLine();
            String currentId = "";
            ArrayList<AnswerTrajectory> anstj = new ArrayList<AnswerTrajectory>();
            ArrayList<String> Ids = new ArrayList<String>();


            while (csv.hasNextLine()) {
                String[] line = csv.nextLine().split(",");
                if (currentId.equals("")) {
                    currentId = line[12];
                } else if (!currentId.equals(line[12])) {
                    csvTraj.put(currentId, anstj);
                    Ids.add(currentId);
                    anstj = new ArrayList<AnswerTrajectory>();
                    currentId = line[12];
                }
                anstj.add(new AnswerTrajectory(Double.parseDouble(line[13]), Double.parseDouble(line[14]), line[12], Double.parseDouble(line[9]), Integer.parseInt(line[0]), Integer.parseInt(line[16])));
            }
            writer.append("Speed\n");

            ArrayList<Trajectory> alltj = new ArrayList<Trajectory>();
            for (String s : Ids)        //for each device ID determine the corresponding answer list
            {
                //System.out.println(s);
                alltj = trajectories.get(s);                        //get all the trajectories under this Id

                if (alltj == null)
                    System.out.println("Could not find trajectory data for deviceId: " + s);

                else {
                    Collections.sort(alltj, Trajectory.TSComparator);            //sort it by time

                    for (int i = 0; i < alltj.size() - 1; i++) {
                        Trajectory t1 = alltj.get(i);
                        Trajectory t2 = alltj.get(i + 1);


                        double x1 = t1.getX();
                        double x2 = t2.getX();
                        double y1 = t1.getY();
                        double y2 = t2.getY();


                        double dist = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
                        double dt = t2.getTimeStamp() - t1.getTimeStamp();


                        if (dt == 0) {
                            System.out.println(t1 + "\n" + t2 + "\tDIST: " + dist + "\n\n");
                            alltj.remove(i+1);
                            i--;
                        }

                        else
                        {
                            double speed = dist / dt;
                            writer.append(speed + "\n");

                        }


                    }


                }
            }


            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csv != null)
                csv.close();


        }
    }

    /**
     * generateGradeDist
     * Adds x,y,Answer,AnswerInt,Distance to a csv file
     *
     * @param csvName
     * @param answersName
     * @param questionLocationsName
     * @param fileName
     * @param meters
     */
    public void generateGradeDist(String csvName, String answersName, String questionLocationsName, String fileName, float meters) {
        Scanner csv = null;
        Scanner ql = null;
        Properties ans = new Properties();
        InputStream ansInput = null;
        try {
            csv = new Scanner(new File(csvName));
            ql = new Scanner(new File(questionLocationsName));
            ansInput = new FileInputStream(answersName);
            ans.load(ansInput);

            FileWriter writer = new FileWriter(fileName + (int) meters + ".csv");
            String topLine = csv.nextLine();
            writer.append(topLine.substring(0, topLine.length() - 2) + ",x,y,Answer,AnswerInt,Distance\n");

            ArrayList<Point2D.Double> questionLocations = new ArrayList<Point2D.Double>();

            while (ql.hasNextLine()) {
                questionLocations.add(new Point2D.Double(ql.nextDouble(), ql.nextDouble()));
            }

            while (csv.hasNextLine()) {

                String line = csv.nextLine();
                String[] orig = line.split(",");


                String givAns = ans.getProperty(orig[0]).toLowerCase();
                String myAns = orig[1].toLowerCase();
                int whichQuestion = Integer.parseInt(orig[0]);

                int correct = 0;

                if (givAns.equals(myAns))
                    correct = 1;


                //System.out.println(orig[orig.length-2] + " y: "+orig[orig.length-1]);
                //calculate the distance between the given input and the answer location
                double x1 = Double.parseDouble(orig[orig.length - 2]);
                double x2 = questionLocations.get(whichQuestion - 1).x;
                double y1 = Double.parseDouble(orig[orig.length - 1]);
                double y2 = questionLocations.get(whichQuestion - 1).y;


                Double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

                String answerType = "";
                int aTypeInt = 0;
                if (correct == 1) {
                    if (dist <= meters) {
                        answerType = "CC";
                        aTypeInt = 1;
                    } else {
                        answerType = "CD";
                        aTypeInt = 2;
                    }
                } else {
                    if (dist <= meters) {
                        answerType = "IC";
                        aTypeInt = 3;
                    } else {
                        answerType = "ID";
                        aTypeInt = 4;
                    }
                }

                writer.append(line + "," + answerType + "," + aTypeInt + "," + dist + "\n");
                //System.out.println(myAns+":"+givAns+"\t\t\t"+correct);
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ql != null) {
                try {
                    ql.close();
                } catch (Exception e) {

                }
            }
            if (csv != null) {
                try {
                    csv.close();
                } catch (Exception e) {

                }
            }
            if (ans != null) {
                try {
                    csv.close();
                } catch (Exception e) {

                }
            }

        }

    }

    /**
     * generateGradeDistCutTime
     * adds x,y,Answer,AnswerInt,Distance and excludes all entries that are outside of the first event time
     *
     * @param csvName
     * @param answersName
     * @param questionLocationsName
     * @param fileName
     * @param meters
     */
    public void generateGradeDistCutTime(String csvName, String answersName, String questionLocationsName, String fileName, float meters) {
        Scanner csv = null;
        Scanner ql = null;
        Properties ans = new Properties();
        InputStream ansInput = null;
        try {
            csv = new Scanner(new File(csvName));
            ql = new Scanner(new File(questionLocationsName));
            ansInput = new FileInputStream(answersName);
            ans.load(ansInput);

            FileWriter writer = new FileWriter(fileName + (int) meters + "v2.csv");
            String topLine = csv.nextLine();
            writer.append(topLine.substring(0, topLine.length() - 2) + ",x,y,Answer,AnswerInt,Distance\n");

            ArrayList<Point2D.Double> questionLocations = new ArrayList<Point2D.Double>();

            while (ql.hasNextLine()) {
                questionLocations.add(new Point2D.Double(ql.nextDouble(), ql.nextDouble()));
            }
            int total = 0;
            while (csv.hasNextLine()) {

                String line = csv.nextLine();
                String[] orig = line.split(",");

                long ts = Long.parseLong(orig[9]);
                //if(ts < 1377334800000l || ts > 1377381600000l) //11 am to end of day
                if (ts < 1377334800000l || ts > 1377342000000l) // 11-1pm
                //if( ts < 1377334800000l|| ts > 1377429200343l) // both days
                //if( ts < 1377334800000l|| ts > 1377343122000l) // all of 24th
                {

                } else {
                    System.out.println(orig[9] + "\t" + ++total);
                    String givAns = ans.getProperty(orig[0]).toLowerCase();
                    String myAns = orig[1].toLowerCase();
                    int whichQuestion = Integer.parseInt(orig[0]);

                    int correct = 0;

                    if (givAns.equals(myAns))
                        correct = 1;


                    //System.out.println(orig[orig.length-2] + " y: "+orig[orig.length-1]);
                    //calculate the distance between the given input and the answer location
                    double x1 = Double.parseDouble(orig[orig.length - 2]);
                    double x2 = questionLocations.get(whichQuestion - 1).x;
                    double y1 = Double.parseDouble(orig[orig.length - 1]);
                    double y2 = questionLocations.get(whichQuestion - 1).y;


                    Double dist = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

                    String answerType = "";
                    int aTypeInt = 0;
                    if (correct == 1) {
                        if (dist <= meters) {
                            answerType = "CC";
                            aTypeInt = 1;
                        } else {
                            answerType = "CD";
                            aTypeInt = 2;
                        }
                    } else {
                        if (dist <= meters) {
                            answerType = "IC";
                            aTypeInt = 3;
                        } else {
                            answerType = "ID";
                            aTypeInt = 4;
                        }
                    }

                    writer.append(line + "," + answerType + "," + aTypeInt + "," + dist + "\n");
                    //System.out.println(myAns+":"+givAns+"\t\t\t"+correct);
                }
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ql != null) {
                try {
                    ql.close();
                } catch (Exception e) {

                }
            }
            if (csv != null) {
                try {
                    csv.close();
                } catch (Exception e) {

                }
            }
            if (ans != null) {
                try {
                    csv.close();
                } catch (Exception e) {

                }
            }

        }

    }

    /**
     * generateGrade
     * adds x,y,Correct to a csv, with correct being 1 or 0
     *
     * @param csvName
     * @param answersName
     * @param fileName
     */
    public void generateGrade(String csvName, String answersName, String fileName) {
        Scanner csv = null;
        Properties ans = new Properties();
        InputStream ansInput = null;
        try {
            csv = new Scanner(new File(csvName));
            ansInput = new FileInputStream(answersName);
            ans.load(ansInput);


            FileWriter writer = new FileWriter(fileName);
            String topLine = csv.nextLine();
            writer.append(topLine.substring(0, topLine.length() - 2) + ",x,y,Correct\n");

            while (csv.hasNextLine()) {

                String line = csv.nextLine();
                String[] orig = line.split(",");


                String givAns = ans.getProperty(orig[0]).toLowerCase();
                String myAns = orig[1].toLowerCase();

                String correct = "0";

                if (givAns.equals(myAns))
                    correct = "1";

                writer.append(line + "," + correct + "\n");
                System.out.println(myAns + ":" + givAns + "\t\t\t" + correct);


            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csv != null) {
                try {
                    csv.close();
                } catch (Exception e) {

                }
            }
            if (ansInput != null) {
                try {
                    ansInput.close();
                } catch (Exception e) {

                }
            }
        }
    }

}
