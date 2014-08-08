package csvgrader;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.*;


/**
 * CsvGrader.java
 * the purpose of this program is to create data files for MatLAB to process graphs using:
 *      an answer list .csv file that has been graded ("Event1Data60.csv" is an example of a .csv file that has gone through: (1) being made by the .csv maker and (2) being altered by one of the generateGrade functions
 *      a .csv of the trajectory data
 *      a .txt of the question locations
 */
public class CsvGrader {


    //public ArrayList<Trajectory> trajectories;
    // public HashMap<String, ArrayList<Trajectory>> trajectories;

    public static void main(String args[]) {
        CsvGrader mycg = new CsvGrader();
        //	mycg.generateGradeDistCutTime("answers.csv","answers.properties","questionLocations.txt","Event1Data",60);
        //mycg.generateTrajectoryDistances("Event1Data60.csv", "questionLocations.txt", "distanceArea", 60);
        //mycg.generateTrajectoryDistancesAndTime("Event1Data60.csv", "questionLocations.txt", "SURFACE", 60);
        //int dbin = 20;
        //int tbin = 1800;
        //mycg.generateSurfacePercentages("SURFACE.csv","s"+dbin+"x"+tbin+"",dbin,tbin);
        //mycg.generateTrajectorySpeeds("Event1Data60.csv", "trajspeeds");

        //mycg.generateAverageSpeeds("trajectory_points.csv","averageSpeeds");
        //mycg.generateTimeOfFirstQuestion("Event1Data60.csv","firstQuestionTimes");
        //mycg.generateRadarData("Event1Data60.csv","trajectory_points.csv","questionLocations.txt","radarData",60,1.92,2500);
        //mycg.generateAverageDistanceTraveledPerQuestion("Event1Data60.csv","trajectory_points.csv","AverageDistanceTraveledPerQuestion3",1377334800000l);
        //mycg.printNumberOfTeamTypes("Event1Data60.csv", "trajectory_points.csv", 1.92, 2500);
        //mycg.printNumberOfAnswerTypes("Event1Data60.csv", "trajectory_points.csv","questionLocations.txt",60, 1.92, 2500);
        /*for(int i= 1; i < 5; i++) {
            mycg.generateTrajectoryDistancesAndTimeForAnswerType("Event1Data60.csv", "questionLocations.txt", "scatter", 60, i);
       }*/
    }


    /**
     * readTrajectories
     * loads the global trajectories hash map of the trajectory data in a given input file
     * the hash map contains ArrayLists of trajectory objects, each ArrayList represents trajectories for each unique device ID
     *
     * @param trajFileName
     */
    public HashMap<String, ArrayList<Trajectory>> readTrajectories(String trajFileName) {
        Scanner csv = null;
        HashMap<String, ArrayList<Trajectory>> trajectories = new HashMap<String, ArrayList<Trajectory>>();
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
        return trajectories;
    }


    /**
     * generateSurfacePercentages
     * creates a .txt file that contains a 2d array of the surface area data based on the .csv created by "generateTrajectoryDistancesAndTime"
     * Data is directly used for Matlab's surf function to create a surface chart.
     *
     * @param csvName     name of the .csv file created by generateTrajectoryDistancesAndTime
     * @param fileName    name of the output file
     * @param distanceBin size of the distance bin, a larger value results in a larger bin
     * @param timeBin     size of the time bin, a larger value results in a larger bin
     */
    public void generateSurfacePercentages(String csvName, String fileName, int distanceBin, int timeBin) {
        Scanner csv = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            FileWriter writer = new FileWriter(fileName + ".txt");
            ArrayList<SaTrajectory> traj = new ArrayList<SaTrajectory>();
            csv.nextLine();
            int dbi = 1;        //distance bin index
            int tbi = 1;        //time bin index
            double dist, ts;
            int answer;
            String id;
            while (csv.hasNextLine()) {         //go through each answer entry and add trajectories of the answer list for each unique device id
                String[] line = csv.nextLine().split(",");
                dist = Double.parseDouble(line[0]);
                ts = Double.parseDouble(line[1]);
                //System.out.println(ts);
                id = line[3];
                answer = Integer.parseInt(line[2]);
                dbi = (int) (Math.floor(dist / (double) (distanceBin)));
                tbi = (int) (Math.floor(ts / (double) (timeBin)));
                traj.add(new SaTrajectory(dist, id, ts, answer, dbi, tbi));
            }

            double minTime = findMinTime(traj);
            //System.out.println(minTime);
            double maxTime = findMaxTime(traj);
            //System.out.println(maxTime);          //determine the maximum and minimum time
            for (int i = 0; i < traj.size(); i++) {
                traj.get(i).modifyTimeStamp(-minTime);      //format the timestamp to seconds by subtracting the minimum timestamp from each
            }
            double maxDist = findMaxDist(traj);
            maxTime = findMaxTime(traj);


            int md = (int) Math.ceil(maxDist / distanceBin);       //determine the maximum number of indices
            int mt = (int) Math.ceil(maxTime / timeBin);           //determine the maximum number of indices

            double[][] cc = new double[md][mt];
            for (int i = 0; i < md; i++) {
                dbi = i * distanceBin;                            //the bin range for distance
                for (int j = 0; j < mt; j++) {
                    tbi = j * timeBin;                            //the bin range for time

                    ArrayList<SaTrajectory> inBin = new ArrayList<SaTrajectory>();
                    for (int a = 0; a < traj.size(); a++)       //iterate through each trajectory and see if it is within the range of the bin
                    {
                        SaTrajectory sa = traj.get(a);
                        double dBinLoc = sa.getDistance();
                        double tBinLoc = sa.getTimeStamp();

                        // System.out.println(dBinLoc+":::::::"+dbi+"-"+ (dbi+distanceBin)+"\t"+tBinLoc+":::::::"+tbi+"-"+(tbi+timeBin)+"\t"+sa.getId()+"\tbin#: "+i+","+j);}}

                        //System.out.println(dBinLoc+":::::::"+dbi+"-"+ (dbi+distanceBin)+"\t"+tBinLoc+":::::::"+tbi+"-"+(tbi+timeBin)+"\t"+sa.getId());
                        // System.out.println(dBinLoc+" "+tBinLoc);
                        if (dBinLoc > dbi && dBinLoc < (dbi + distanceBin)) {
                            if ((tBinLoc > tbi) && (tBinLoc < (tbi + timeBin))) {
                                //this value is in the bin
                                inBin.add(sa);
                                //System.out.println(sa.toString());
                                traj.remove(a);
                                a--;
                            }
                        }
                    }
                    int totalCC = 0;
                    for (int a = 0; a < inBin.size(); a++)                // for all values found in the bin, determine the percentage of CC's inside the bin, then store it into the 2d Array
                    {
                        SaTrajectory sa = inBin.get(a);
                        if (sa.getAnswer() == 1)
                            totalCC++;
                    }
                    /*if(inBin.size()==0)
                        cc[i][j] = (0);
                    else*/
                    cc[i][j] = (((double) totalCC) / ((double) inBin.size()));
                    // System.out.println((((double)totalCC)/((double)inBin.size())));
                }
            }


            for (int i = 0; i < cc[0].length; i++) {
                for (int j = 0; j < cc.length; j++) {
                    writer.write("" + cc[j][i] + ",");
                }
                writer.write("\n");
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

    public double findMaxDist(ArrayList<SaTrajectory> t) {
        double ret = Double.MIN_VALUE;
        for (SaTrajectory tj : t) {
            if (ret < tj.getDistance())
                ret = tj.getDistance();
        }
        return ret;
    }

    public double findMaxTime(ArrayList<SaTrajectory> t) {
        double ret = Double.MIN_VALUE;
        for (SaTrajectory tj : t) {
            if (ret < tj.getTimeStamp())
                ret = tj.getTimeStamp();
        }
        return ret;
    }

    public double findMinTime(ArrayList<SaTrajectory> t) {
        double ret = Double.MAX_VALUE;
        for (SaTrajectory tj : t) {
            if (ret > tj.getTimeStamp())
                ret = tj.getTimeStamp();
        }
        return ret;
    }

    public double findMax(double[] ar) {
        double ret = Double.MIN_VALUE;
        for (double d : ar) {
            if (d > ret)
                ret = d;
        }
        return ret;
    }


    /**
     * generateTrajectoryDistancesAndTimeForAnswerType
     * creates a .csv file that contains 5 columns for one of the four answer types: Distance, Time, AnswerType, ID, Question Number
     * Used in creating the Scatter plot for all of the answer types
     *
     * @param csvName               name of the answer list file
     * @param trajFileName          name of the trajectory data
     * @param questionLocationsName name of the question locations file
     * @param fileName              name of output file
     * @param meters                range answer is considered close
     * @param answerType            (1-4) for one of the four answer types
     */
    public void generateTrajectoryDistancesAndTimeForAnswerType(String csvName, String trajFileName, String questionLocationsName, String fileName, double meters, int answerType) {
        Scanner csv = null;
        Scanner ql = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            ql = new Scanner(new File(questionLocationsName));      //question locations (index 0 corresponds to question 1)

            String ap = "";
            if (answerType == 1)
                ap = "CC";
            else if (answerType == 2)
                ap = "CD";
            else if (answerType == 3)
                ap = "IC";
            else
                ap = "ID";

            FileWriter writer = new FileWriter(fileName + ap + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID
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
            writer.append("Distance,Time,AnswerType,ID,Question\n");

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


                    int currentQuestion = anstj.get(0).getQuestion();
                    int currentAnswer = anstj.get(0).getAnswer();
                    int correct = 0;
                    if (currentAnswer == 1 || currentAnswer == 2)
                        correct = 1;

                    boolean BC = true;// before the first question
                    int total = 0;
                    for (Trajectory t : alltj) {

                        double timestamp = t.getTimeStamp() * 1000;

                        if (BC && timestamp < anstj.get(0).getTimeStamp())    //use first trajectory as if it's before the first question
                        {
                            total++;
                            //determine the distance and answer type
                            double qX = questionLocations.get(currentQuestion - 1).getX();  //question location x and y
                            double qY = questionLocations.get(currentQuestion - 1).getY();

                            double tX = t.getX();                                           //trajectory x and y
                            double tY = t.getY();

                            double aX = anstj.get(0).getX();                                //location when question was answered
                            double aY = anstj.get(0).getY();

                            double currentDist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));      //distance from current location to question location
                            double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                            int answerId = 0;
                            if (distToAnswer <= meters) {
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


                            if (answerId == answerType)
                                writer.append(currentDist + "," + t.getTimeStamp() + "," + answerId + "," + t.getId() + "," + anstj.get(0).getQuestion() + "\n");

                            BC = false;
                        } else {
                            break;
                        }

                    }

                    for (int a = 0; a < anstj.size() - 1; a++)   //for questions 1-n
                    {
                        //determine the distance and answer type

                        double qX = questionLocations.get(anstj.get(a + 1).getQuestion() - 1).getX();//question location x and y
                        double qY = questionLocations.get(anstj.get(a + 1).getQuestion() - 1).getY();

                        double tX = anstj.get(a).getX();                                //current location x and y
                        double tY = anstj.get(a).getY();

                        double aX = anstj.get(a + 1).getX();                                //location when question was answered
                        double aY = anstj.get(a + 1).getY();

                        double currentDist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));      //distance from current location to question location
                        double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                        int answerId = 0;
                        if (distToAnswer <= meters) {
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
                        if (answerId == answerType)
                            writer.append(currentDist + "," + anstj.get(a).getTimeStamp() / 1000.0 + "," + answerId + "," + anstj.get(a).getId() + "," + anstj.get(a).getQuestion() + "\n");
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

    /**
     * generateTrajectoryDistancesAndTime
     * creates a .csv file that contains 5 columns: Distance, Time, AnswerType, ID, Question Number
     * Used in creating the Surface Chart in Matlab, specifically followed up by using the function "generateSurfacePercentages"
     *
     * @param csvName               name of the answer list file
     * @param trajFileName          name of the trajectory data
     * @param questionLocationsName name of the question locations file
     * @param fileName              name of output file
     * @param meters                range answer is considered close
     */
    public void generateTrajectoryDistancesAndTime(String csvName, String trajFileName, String questionLocationsName, String fileName, double meters) {
        Scanner csv = null;
        Scanner ql = null;

        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            ql = new Scanner(new File(questionLocationsName));      //question locations (index 0 corresponds to question 1)
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID
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
            writer.append("Distance,Time,AnswerType,ID,Question\n");

            ArrayList<Trajectory> alltj = new ArrayList<Trajectory>();
            for (String s : Ids)        //for each device ID determine the corresponding answer list
            {
                //System.out.println(s);
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds all trajectories for this device
                alltj = trajectories.get(s);                        //get all the trajectories under this Id

                if (anstj == null || alltj == null)
                    System.out.println("Could not find trajectory data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);        //sort it by time
                    Collections.sort(alltj, Trajectory.TSComparator);              //sort it by time


                    int currentQuestion = anstj.get(0).getQuestion();
                    int currentAnswer = anstj.get(0).getAnswer();
                    int correct = 0;
                    if (currentAnswer == 1 || currentAnswer == 2)
                        correct = 1;

                    boolean BC = true;// before the first question
                    int total = 0;
                    for (Trajectory t : alltj) {

                        double timestamp = t.getTimeStamp() * 1000;

                        if (BC && timestamp < anstj.get(0).getTimeStamp())    //use first trajectory as if it's before the first question
                        {
                            total++;
                            //determine the distance and answer type
                            double qX = questionLocations.get(currentQuestion - 1).getX();  //question location x and y
                            double qY = questionLocations.get(currentQuestion - 1).getY();

                            double tX = t.getX();                                           //trajectory x and y
                            double tY = t.getY();

                            double aX = anstj.get(0).getX();                                //location when question was answered
                            double aY = anstj.get(0).getY();

                            double currentDist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));      //distance from current location to question location
                            double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                            int answerId = 0;
                            if (distToAnswer <= meters) {
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

                            writer.append(currentDist + "," + t.getTimeStamp() + "," + answerId + "," + t.getId() + "," + anstj.get(0).getQuestion() + "\n");
                            BC = false;
                        } else {
                            break;
                        }

                    }

                    for (int a = 0; a < anstj.size() - 1; a++)   //for questions 1-n
                    {
                        //determine the distance and answer type

                        double qX = questionLocations.get(anstj.get(a + 1).getQuestion() - 1).getX();//question location x and y
                        double qY = questionLocations.get(anstj.get(a + 1).getQuestion() - 1).getY();

                        double tX = anstj.get(a).getX();                                //current location x and y
                        double tY = anstj.get(a).getY();

                        double aX = anstj.get(a + 1).getX();                                //location when question was answered
                        double aY = anstj.get(a + 1).getY();

                        double currentDist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));      //distance from current location to question location
                        double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                        int answerId = 0;
                        if (distToAnswer <= meters) {
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

                        writer.append(currentDist + "," + anstj.get(a).getTimeStamp() / 1000.0 + "," + answerId + "," + anstj.get(a).getId() + "," + anstj.get(a).getQuestion() + "\n");
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

    /**
     * generateTrajectoryDistances
     * creates a .csv file that contains the columns: Distance,AnswerType,ID,Question.
     * The Distances are the current location between the correct answer location, and the answer type is determined by the distance between where the question was answered and the correct question location
     * Used for the current location area chart (note the above function generateTrajectoryDistancesAndTime was derived off this one)
     *
     * @param csvName               name of the answer list file
     * @param trajFileName          name of the trajectory data
     * @param questionLocationsName name of the question locations file
     * @param fileName              name of output file
     * @param meters                range answer is considered close
     */
    public void generateTrajectoryDistances(String csvName, String trajFileName, String questionLocationsName, String fileName, double meters) {
        Scanner csv = null;
        Scanner ql = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            ql = new Scanner(new File(questionLocationsName));
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID
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
            writer.append("Distance,AnswerType,ID,Question\n");

            ArrayList<Trajectory> alltj = new ArrayList<Trajectory>();
            for (String s : Ids)        //for each device ID determine the corresponding answer list
            {
                System.out.println(s);
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds all trajectories for this device
                alltj = trajectories.get(s);                        //get all the trajectories under this Id

                if (anstj == null || alltj == null)
                    System.out.println("Could not find trajectory data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);        //sort answer list data by time
                    Collections.sort(alltj, Trajectory.TSComparator);              //sort trajectory data by time


                    int currentQuestion = anstj.get(0).getQuestion();               //the first question
                    int currentAnswer = anstj.get(0).getAnswer();
                    int correct = 0;
                    if (currentAnswer == 1 || currentAnswer == 2)
                        correct = 1;

                    boolean BC = true;// before the first question
                    int total = 0;
                    for (Trajectory t : alltj) {

                        double timestamp = t.getTimeStamp() * 1000;

                        if (BC && timestamp < anstj.get(0).getTimeStamp())    //use first trajectory as if it's before the first question
                        {
                            total++;
                            //determine the distance and answer type
                            double qX = questionLocations.get(currentQuestion - 1).getX();  //question location x and y
                            double qY = questionLocations.get(currentQuestion - 1).getY();

                            double tX = t.getX();                                           //trajectory x and y
                            double tY = t.getY();

                            double aX = anstj.get(0).getX();                                //location when question was answered
                            double aY = anstj.get(0).getY();

                            double currentDist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));      //distance from current location to question location
                            double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                            int answerId = 0;
                            if (distToAnswer <= meters) {
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

                            writer.append(currentDist + "," + answerId + "," + t.getId() + "," + anstj.get(0).getQuestion() + "\n");
                            BC = false;
                        } else {
                            break;
                        }

                    }

                    for (int a = 0; a < anstj.size() - 1; a++)   //for questions 1-n
                    {
                        //determine the distance and answer type

                        double qX = questionLocations.get(anstj.get(a + 1).getQuestion() - 1).getX();//current question location x and y
                        double qY = questionLocations.get(anstj.get(a + 1).getQuestion() - 1).getY();

                        double tX = anstj.get(a).getX();                                //current location x and y
                        double tY = anstj.get(a).getY();

                        double aX = anstj.get(a + 1).getX();                                //location where question was answered
                        double aY = anstj.get(a + 1).getY();

                        double currentDist = Math.sqrt((tX - qX) * (tX - qX) + (tY - qY) * (tY - qY));      //distance from current location to question location
                        double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                        int answerId = 0;
                        if (distToAnswer <= meters) {
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

                        writer.append(currentDist + "," + answerId + "," + anstj.get(a).getId() + "," + anstj.get(a).getQuestion() + "\n");
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


    /**
     * generateAverageDistanceTraveledPerQuestion
     * Creates a .csv file that contains the columns: Question, AverageDistance. This function uses the answer list and trajectory data to determine the distances that each ID traveled before answering a question (from 1-20) questions.
     * The Distances are averaged by the total number of people who answered the question, resulting in a 20x2 matrix .csv file
     * Used in creating the line graph for average distance traveled per question
     *
     * @param csvName       name of the answer list file
     * @param trajFileName  name of the trajectory data
     * @param fileName      name of output file
     * @param ignoreBefore  all trajectory points before this UNIX ms time stamp will be ignored
     */
    public void generateAverageDistanceTraveledPerQuestion(String csvName, String trajFileName, String fileName, double ignoreBefore) {
        Scanner csv = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID
            csv.nextLine();
            String currentId = "";
            ArrayList<AnswerTrajectory> anstj = new ArrayList<AnswerTrajectory>();
            ArrayList<String> Ids = new ArrayList<String>();

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
            double[] totalDists = new double[20];                                   //keeping track of the distances
            int[] totalAnswers = new int[20];                                       //keeping track of the total number of times a question was answered
            ArrayList<Trajectory> alltj = new ArrayList<Trajectory>();
            for (String s : Ids)        //for each device ID determine the corresponding answer list
            {
                //System.out.println(s);
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds the answer list for this device
                alltj = trajectories.get(s);                        //get all the trajectories under this Id

                if (anstj == null || alltj == null)
                    System.out.println("Could not find trajectory data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);        //sort answer list by by time
                    Collections.sort(alltj, Trajectory.TSComparator);              //sort trajectory data by time

                    int questionIndex = 0;
                    double currentQuestionTimeStamp = anstj.get(questionIndex).getTimeStamp();      //after trajectory data points breach this time, update the current question

                    for (int i = 0; i < alltj.size() - 1; i++) {        //pull two trajectory points at a time
                        Trajectory t1 = alltj.get(i);
                        Trajectory t2 = alltj.get(i + 1);
                        double timeStamp = t1.getTimeStamp() * 1000.0;  //determine the time of each trajectory (trajectory data time stamps are in seconds, but the answer list data is in milliseconds)
                        if (timeStamp < ignoreBefore)                   //do not account for trajectories that are before a certain time (event start)
                        {

                        } else {
                            double timeStamp2 = t2.getTimeStamp() * 1000.0;
                            if (timeStamp2 > currentQuestionTimeStamp && questionIndex == anstj.size() - 1) {       //if the second trajectory is beyond the last question, stop
                                totalAnswers[questionIndex]++;
                                i = alltj.size();
                            } else {
                                if (timeStamp2 > currentQuestionTimeStamp) {                                        //if the second trajectory is beyond the question, use the answer list trajectory instead

                                    double t1x, t1y, t2x, t2y;
                                    t1x = t1.getX();
                                    t1y = t1.getY();
                                    t2x = anstj.get(questionIndex).getX();
                                    t2y = anstj.get(questionIndex).getY();
                                    double dist = Math.sqrt((t1x - t2x) * (t1x - t2x) + (t1y - t2y) * (t1y - t2y));
                                    totalDists[questionIndex] += dist;
                                    totalAnswers[questionIndex]++;
                                    questionIndex++;
                                    currentQuestionTimeStamp = anstj.get(questionIndex).getTimeStamp();             //update the current question
                                } else {                                                                            //use the two trajectories to determine the distance
                                    double t1x, t1y, t2x, t2y;
                                    t1x = t1.getX();
                                    t1y = t1.getY();
                                    t2x = t2.getX();
                                    t2y = t2.getY();
                                    double dist = Math.sqrt((t1x - t2x) * (t1x - t2x) + (t1y - t2y) * (t1y - t2y));
                                    totalDists[questionIndex] += dist;
                                }
                            }
                        }
                    }
                }
            }
            writer.append("Question,AverageDistance\n");
            for (int i = 0; i < totalDists.length; i++) {
                writer.append((i + 1) + "," + (totalDists[i] / ((double) totalAnswers[i])) + "\n");             //write to the .csv file
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
     * generateAverageSpeeds
     * creates a .csv file that contains the average speed for each device ID. Contains two columns: ID, Average Speed
     * Used in histogram of average speeds
     *
     * @param trajFileName name of the trajectory data
     * @param fileName     name of output file
     */
    public void generateAverageSpeeds(String trajFileName, String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID

            writer.append("ID,AverageSpeed\n");
            for (ArrayList<Trajectory> tlist : trajectories.values()) {
                Collections.sort(tlist, Trajectory.TSComparator);            //sort trajectory data by time

                double sumSpeed = 0;
                for (int i = 0; i < tlist.size() - 1; i++) {
                    Trajectory t1 = tlist.get(i);
                    Trajectory t2 = tlist.get(i + 1);

                    double dist = Math.sqrt(((t1.getX() - t2.getX()) * (t1.getX() - t2.getX())) + ((t1.getY() - t2.getY()) * (t1.getY() - t2.getY())));
                    double dt = t2.getTimeStamp() - t1.getTimeStamp();

                    if (dt == 0) {
                        System.out.println(t1 + "\n" + t2 + "\tDIST: " + dist + "\n\n");
                        tlist.remove(i + 1);
                        i--;
                    } else
                        sumSpeed += dist / dt;
                }
                sumSpeed = sumSpeed / ((double) (tlist.size() - 1));
                writer.append(tlist.get(0).getId() + "," + sumSpeed + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * determineAverageSpeed
     * returns the average speed of all trajectories under a given ID
     * used as a helper function
     *
     * @param trajectories hash map of all the trajectories
     * @param id           string id
     * @return double of the average speed of all the trajectories
     */
    public double determineAverageSpeed(HashMap<String, ArrayList<Trajectory>> trajectories, String id) {
        try {
            ArrayList<Trajectory> tlist = trajectories.get(id);

            if (tlist == null) {
                System.out.println("Determining Average Speed: No Trajectory Data Found for ID: " + id);
            } else {
                Collections.sort(tlist, Trajectory.TSComparator);            //sort trajectory data by time

                double sumSpeed = 0;
                for (int i = 0; i < tlist.size() - 1; i++) {                //pull out two trajectories and determine the speed between them
                    Trajectory t1 = tlist.get(i);
                    Trajectory t2 = tlist.get(i + 1);

                    double dist = Math.sqrt(((t1.getX() - t2.getX()) * (t1.getX() - t2.getX())) + ((t1.getY() - t2.getY()) * (t1.getY() - t2.getY())));
                    double dt = t2.getTimeStamp() - t1.getTimeStamp();

                    if (dt == 0) {                                          // special case where the timestamps are identical between two trajectories
                        //System.out.println(t1 + "\n" + t2 + "\tDIST: " + dist + "\n\n");
                        tlist.remove(i + 1);                                //toss out the "duplicate"
                        i--;
                    } else
                        sumSpeed += dist / dt;                              //add to the speed
                }
                sumSpeed = sumSpeed / ((double) (tlist.size() - 1));        //return the average
                return sumSpeed;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * generateTimeOfFirstQuestion
     * creates a .csv file that contains two columns: ID, Time Of Answering the First Question Since the event start
     * used in making the histogram for the time of answering the first questions
     *
     * @param csvName  name of the answer list file
     * @param fileName name of output file
     */
    public void generateTimeOfFirstQuestion(String csvName, String fileName) {
        Scanner csv = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list

            csv.nextLine();
            String currentId = "";
            ArrayList<AnswerTrajectory> anstj = new ArrayList<AnswerTrajectory>();
            ArrayList<String> Ids = new ArrayList<String>();

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
            writer.append("ID,TimeFirstQuestionAnsweredSinceEventStart\n");

            for (String s : Ids)        //for each device ID determine the corresponding answer list
            {
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds all trajectories for this device

                if (anstj == null)
                    System.out.println("Could not find data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);        //sort answer list data by time
                    writer.append(anstj.get(0).getId() + "," + (anstj.get(0).getTimeStamp() - 1377334800000.0) / 1000.0 + "\n");
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
     * generateRadarData
     * creates a .txt file that contains a 4x4 matrix of comma-separated-values.
     * Each row represents one of the four team types (Serious, GIOW, Hurried, Lazy), each column represents one of the four answer types (CC, CD, IC, ID)
     * Used in making a radar chart, use this 4x4 matrix in the "spider.m" matlab function.
     *
     * @param csvName               name of the answer list file
     * @param trajFileName          name of the trajectory data
     * @param questionLocationsName name of the question locations file
     * @param fileName              name of output file
     * @param meters                range answer is considered close
     * @param speedThreshold        threshold to separate team types by average speed
     * @param timeThreshold         threshold to separate team types by time started
     */
    public void generateRadarData(String csvName, String trajFileName, String questionLocationsName, String fileName, double meters, double speedThreshold, double timeThreshold) {
        Scanner csv = null;
        Scanner ql = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            ql = new Scanner(new File(questionLocationsName));
            FileWriter writer = new FileWriter(fileName + ".txt");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID

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

            HashMap<Integer, ArrayList<AnswerTrajectory>> teams = new HashMap<Integer, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list

            for (String s : Ids) {
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds all trajectories for this device

                if (anstj == null)
                    System.out.println("Could not find data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);                     //sort answer list data by time
                    double startTime = (anstj.get(0).getTimeStamp() - 1377334800000.0) / 1000.0;//converts the time stamp to time after the event started
                    double speed = determineAverageSpeed(trajectories, anstj.get(0).getId());
                    //System.out.println(speed+" "+startTime);
                    if (speed != -1) // ensure the trajectory data exists for this ID
                    {
                        //classify the team type
                        int teamType = 0;

                        if (startTime < timeThreshold) {
                            if (speed > speedThreshold) // serious
                                teamType = 1;
                            else
                                teamType = 2;// get it over with
                        } else {
                            if (speed > speedThreshold) // hurried
                                teamType = 3;
                            else
                                teamType = 4; //lazy
                        }
                        if (teams.get(teamType) == null) {
                            System.out.println("first team type for: " + teamType);
                            teams.put(teamType, new ArrayList<AnswerTrajectory>(anstj));
                        } else {
                            teams.get(teamType).addAll(anstj);
                        }
                    }
                }
            }
            int totalcc = 0;
            int totalcd = 0;
            int totalic = 0;
            int totalid = 0;
            for (int i = 1; i < 5; i++) {
                ArrayList<AnswerTrajectory> trajs = teams.get(i);
                int cc = 0;
                int cd = 0;
                int ic = 0;
                int id = 0;

                for (AnswerTrajectory at : trajs) {
                    int question = at.getQuestion();
                    double qX = questionLocations.get(question - 1).getX();  //question location x and y
                    double qY = questionLocations.get(question - 1).getY();

                    double aX = at.getX();                                //location when question was answered
                    double aY = at.getY();

                    double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                    int correct = 0;
                    if (at.getAnswer() == 1 || at.getAnswer() == 2)
                        correct = 1;

                    if (distToAnswer <= meters) {
                        if (correct == 1)
                            cc++;

                        if (correct == 0)
                            ic++;
                    } else {
                        if (correct == 1)
                            cd++;

                        if (correct == 0)
                            id++;
                    }

                }
                totalcc += cc;
                totalcd += cd;
                totalic += ic;
                totalid += id;
                double pcc = (double) cc / trajs.size();
                double pcd = (double) cd / trajs.size();
                double pic = (double) ic / trajs.size();
                double pid = (double) id / trajs.size();
                System.out.println("Team Type ID: " + i + " " + trajs.size());
                writer.append(pcc + "," + pcd + "," + pic + "," + pid + "\n");
            }
            System.out.println("Answer Type Totals\n" + "CC: " + totalcc + "\nCD: " + totalcd + "\nIC: " + totalic + "\nID: " + totalid);

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

    /**
     * printNumberOfAnswerTypes
     * prints the totals of the four answer types
     *
     * @param csvName               name of the answer list file
     * @param trajFileName          name of the trajectory data
     * @param questionLocationsName name of the question locations file
     * @param meters                range answer is considered close
     * @param speedThreshold        threshold to separate team types by average speed
     * @param timeThreshold         threshold to separate team types by time started
     */
    public void printNumberOfAnswerTypes(String csvName, String trajFileName, String questionLocationsName, double meters, double speedThreshold, double timeThreshold) {
        Scanner csv = null;
        Scanner ql = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            ql = new Scanner(new File(questionLocationsName));
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID

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

            HashMap<Integer, ArrayList<AnswerTrajectory>> teams = new HashMap<Integer, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list

            for (String s : Ids) {
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds all trajectories for this device

                if (anstj == null)
                    System.out.println("Could not find data for deviceId: " + s);

                else {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);                     //sort answer list data by time
                    double startTime = (anstj.get(0).getTimeStamp() - 1377334800000.0) / 1000.0;//converts the time stamp to time after the event started
                    double speed = determineAverageSpeed(trajectories, anstj.get(0).getId());
                    //System.out.println(speed+" "+startTime);
                    if (speed != -1) // ensure the trajectory data exists for this ID
                    {
                        //classify the team type
                        int teamType = 0;

                        if (startTime < timeThreshold) {
                            if (speed > speedThreshold) // serious
                                teamType = 1;
                            else
                                teamType = 2;// get it over with
                        } else {
                            if (speed > speedThreshold) // hurried
                                teamType = 3;
                            else
                                teamType = 4; //lazy
                        }
                        if (teams.get(teamType) == null) {
                            System.out.println("first team type for: " + teamType);
                            teams.put(teamType, new ArrayList<AnswerTrajectory>(anstj));
                        } else {
                            teams.get(teamType).addAll(anstj);
                        }
                    }
                }
            }
            int totalcc = 0;
            int totalcd = 0;
            int totalic = 0;
            int totalid = 0;
            for (int i = 1; i < 5; i++) {
                ArrayList<AnswerTrajectory> trajs = teams.get(i);
                int cc = 0;
                int cd = 0;
                int ic = 0;
                int id = 0;

                for (AnswerTrajectory at : trajs) {
                    int question = at.getQuestion();
                    double qX = questionLocations.get(question - 1).getX();  //question location x and y
                    double qY = questionLocations.get(question - 1).getY();

                    double aX = at.getX();                                //location when question was answered
                    double aY = at.getY();

                    double distToAnswer = Math.sqrt((aX - qX) * (aX - qX) + (aY - qY) * (aY - qY));     //distance from answer to answer location

                    int correct = 0;
                    if (at.getAnswer() == 1 || at.getAnswer() == 2)
                        correct = 1;

                    if (distToAnswer <= meters) {
                        if (correct == 1)
                            cc++;

                        if (correct == 0)
                            ic++;
                    } else {
                        if (correct == 1)
                            cd++;

                        if (correct == 0)
                            id++;
                    }

                }
                totalcc += cc;
                totalcd += cd;
                totalic += ic;
                totalid += id;

            }
            System.out.println("Answer Type Totals\n" + "CC: " + totalcc + "\nCD: " + totalcd + "\nIC: " + totalic + "\nID: " + totalid);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csv != null)
                csv.close();

            if (ql != null)
                ql.close();

        }
    }

    /**
     * printNumberOfTeamTyeps
     * prints the number of each team type to the console
     *
     * @param csvName           name of the answer list file
     * @param trajFileName      name of the trajectory data
     * @param speedThreshold    threshold to separate team types by average speed
     * @param timeThreshold     threshold to separate team types by time started
     */
    public void printNumberOfTeamTypes(String csvName, String trajFileName, double speedThreshold, double timeThreshold) {
        Scanner csv = null;
        try {
            csv = new Scanner(new File(csvName));       //csv of answer list
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID

            csv.nextLine();
            String currentId = "";
            ArrayList<AnswerTrajectory> anstj = new ArrayList<AnswerTrajectory>();
            ArrayList<String> Ids = new ArrayList<String>();


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

            HashMap<Integer, ArrayList<AnswerTrajectory>> teams = new HashMap<Integer, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list

            int serious = 0;
            int giow = 0;
            int hurried = 0;
            int lazy = 0;
            System.out.println("Total IDS: " + Ids.size());

            for (String s : Ids) {
                anstj = csvTraj.get(s);                             //get the hashmap arraylist that holds all trajectories for this device
/*
                if (anstj == null)
                    System.out.println("Could not find data for deviceId: " + s);
*/
                {
                    Collections.sort(anstj, AnswerTrajectory.TSComparator);        //sort answer list data by time
                    double startTime = (anstj.get(0).getTimeStamp() - 1377334800000.0) / 1000.0;
                    double speed = determineAverageSpeed(trajectories, anstj.get(0).getId());
                    //System.out.println(speed+" "+startTime);
                    if (speed != -1) // ensure the trajectory data exists for this ID
                    {
                        //classify the team type
                        int teamType = 0;

                        if (startTime < timeThreshold) {
                            if (speed > speedThreshold) {// serious
                                teamType = 1;
                                serious++;
                            } else {
                                teamType = 2;// get it over with
                                giow++;
                            }
                        } else {
                            if (speed > speedThreshold) { // hurried
                                teamType = 3;
                                hurried++;
                            } else {
                                teamType = 4; //lazy
                                lazy++;
                            }
                        }
                        if (teams.get(teamType) == null) {
                            System.out.println("first team type for: " + teamType);
                            teams.put(teamType, new ArrayList<AnswerTrajectory>(anstj));
                        } else {
                            teams.get(teamType).addAll(anstj);
                        }
                    }
                }
            }
            System.out.println("Serious: " + serious + "\nGIOW: " + giow + "\nHurried: " + hurried + "\nLazy: " + lazy);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csv != null)
                csv.close();


        }
    }

    /**
     * generateTrajectorySpeeds
     * creates a .csv file of the speeds in the answer list based on time stamps and current locations
     *
     * @param csvName  name of the answer list file
     * @param fileName name of output file
     */
    public void generateTrajectorySpeeds(String csvName, String trajFileName, String fileName) {
        Scanner csv = null;
        try {
            csv = new Scanner(new File(csvName));                       //open the answer list file
            FileWriter writer = new FileWriter(fileName + ".csv");
            HashMap<String, ArrayList<AnswerTrajectory>> csvTraj = new HashMap<String, ArrayList<AnswerTrajectory>>();      //hash map that keys on trajectory id, storing all answer list trajectories in an array list
            HashMap<String, ArrayList<Trajectory>> trajectories = readTrajectories(trajFileName);                           //load the trajectory data into a hash map keyed by ID
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
                    Collections.sort(alltj, Trajectory.TSComparator);            //sort trajectory data by time

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
                            alltj.remove(i + 1);
                            i--;
                        } else {
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
     * generateGradeDistCutTime
     * creates a .csv file that adds x,y,Answer,AnswerInt,Distance columns to a base .csv answer list
     *
     * @param csvName                   name of the csv file that was created using the .csv maker
     * @param answersName               name of the answers properties file
     * @param questionLocationsName     name of the question locations file
     * @param fileName                  name of output file
     * @param meters                    range that the answer is considered close
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
     * creates a .csv file that adds x,y,Answer,AnswerInt,Distance columns to a base .csv answer list and excludes all entries that are outside of the first event time
     *
     * @param csvName                   name of the csv file that was created using the .csv maker
     * @param answersName               name of the answers properties file
     * @param questionLocationsName     name of the question locations file
     * @param fileName                  name of output file
     * @param meters                    range that the answer is considered close
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


}
