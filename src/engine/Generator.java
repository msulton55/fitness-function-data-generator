package src.engine;

import src.components.Graph;
import src.components.Node;

import javax.swing.*;
import java.io.*;
import java.util.*;

class Generator {

    // Global Variables
    static String mainGraphDatabaseLocation;
    static String targetLocation;
    static String feederPortDatabaseLocation;
    static File mainGraphFile;
    static File feederPortFile;
    static File targetFile;
    static ArrayList<String> feederPort;
    static ArrayList<String> feederPortBackup;
    static ArrayList<String> feederRemoved;
    static ArrayList<String> seaName;
    static ArrayList<String> hubPort;
    static ArrayList<String> portFiltered;
    static ArrayList<Double> jarakPerTrayek;
    static HashMap<Integer, String> listTrayek;
    static List<Node> listNode;
    static Graph g;
    static int hubPortCounter;
    static int randomize;
    static int trayekIdentifier;
    static int checkCounter;
    static double totalDistancePerTrayek;
    static StringBuilder temporaryString;

    static void initialize() {

        // File location
        mainGraphDatabaseLocation = "graph_list/KOORDINAT PELABUHAN-SELAT v8.4.txt";
        targetLocation = "fitness_function/list_trayek.txt";
        feederPortDatabaseLocation = "graph_list/PELABUHAN_FEEDER.txt";

        // Input file to the program
        mainGraphFile = new File(mainGraphDatabaseLocation);
        feederPortFile = new File(feederPortDatabaseLocation);
        targetFile = new File(targetLocation);

        // Input src.components.Graph object
        g = new Graph(mainGraphDatabaseLocation);

        // Create main entity for the generator
        listTrayek = new HashMap<>();
        listNode = new ArrayList<>();
        feederPort = new ArrayList<>();
        feederPortBackup = new ArrayList<>();
        feederRemoved = new ArrayList<>();
        seaName = new ArrayList<>();
        portFiltered = new ArrayList<>();
        jarakPerTrayek = new ArrayList<>();
        hubPort = new ArrayList<>();
        trayekIdentifier = 1;
        checkCounter = 1;
        totalDistancePerTrayek = 0;
        temporaryString = new StringBuilder();
        randomize = 1;
        hubPortCounter = 0;

    }

    static void createHubPortList() {

        hubPort.add("TANJUNG-PERAK");
        hubPort.add("MAKASAR");
        hubPort.add("BITUNG");
        hubPort.add("MAKASAR");

    }

    static void createSeaNameList() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(mainGraphFile));
            int counter = 1;
            String line;
            String type;
            StringTokenizer tokenizer;
            while ((line = reader.readLine()) != null) {
                tokenizer = new StringTokenizer(line, "\t");
                type = tokenizer.nextToken().toLowerCase();
                if (type.equals("i")) {
                    if (counter > 87)
                        seaName.add(tokenizer.nextToken());
                    counter++;
                }
                else
                    break;
            }
            reader.close();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }

    }

    static void createFeederPortList() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(feederPortFile));
            String line;
            StringTokenizer tokenizer;
            while ((line = reader.readLine()) != null) {
                tokenizer = new StringTokenizer(line, "\t");
                feederPort.add(tokenizer.nextToken());
            }
            reader.close();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }

        feederPortBackup.addAll(feederPort);

    }

    static void scrambleLastHubPortList() {

        int max = hubPort.size() - 1;
        int min = 0;
        int randomNumber = (int) (Math.random() * ((max - min) + 1)) + min;

        hubPort.add(hubPort.get(randomNumber));
    }

    static void refillFeederPortList() {

        feederPort.addAll(feederPortBackup);

    }

    static void clearEverything() {
        hubPort.remove(hubPort.size()-1);
        listTrayek.clear();
        jarakPerTrayek.clear();
        feederPort.clear();
        feederRemoved.clear();
        trayekIdentifier = 1;
        temporaryString.setLength(0);
        totalDistancePerTrayek = 0;
        checkCounter = 1;
        portFiltered.clear();
        hubPortCounter = 0;
    }

    static int findFrequency(double distance) {
         final int YEAR_IN_DAYS = 365;
         final double VELOCITY = 50;
         double time;
         int freq;

         time = distance / VELOCITY;
         freq = (int) (YEAR_IN_DAYS / (time/24));

         return freq;
    }

    static double findFitnessFunction(ArrayList<Integer> freq, ArrayList<Double> distance) {
        final double CAPACITY = 400;
        double totalCapacity;
        double result;
        int sumFreq = 0;
        double sumDistance = 0;

        for (Integer value : freq) {
            sumFreq += value;
        }

        for (Double value : distance) {
            sumDistance += value;
        }

        totalCapacity = CAPACITY * sumFreq;
        result = totalCapacity / sumDistance;

        return result;

    }

    static void writeToTextFile() {

        try {
            FileWriter fr = new FileWriter(targetFile, false);
            BufferedWriter br = new BufferedWriter(fr);

            int counter = 0;
            ArrayList<Integer> freq = new ArrayList<>();

            for (Double value : jarakPerTrayek) {
                freq.add(findFrequency(value));
            }

            br.append("Kombinasi\tTrayek\tJarak");
            br.append("\n");
            br.append("     Random ").append(String.valueOf(randomize));
            for (Map.Entry<Integer, String> entry : listTrayek.entrySet()) {
                br.append("\n");
                br.append(entry.getKey().toString()).append("\t").append(entry.getValue()).append("\t")
                  .append(String.valueOf(jarakPerTrayek.get(counter)));
                counter++;
            }
            br.append("\n");
            br.append("Fitness Function: ").append("\t")
              .append(String.valueOf(findFitnessFunction(freq, jarakPerTrayek)));

            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        initialize();

        createHubPortList();

        //scrambleLastHubPortList();

        createSeaNameList();

        createFeederPortList();

        //================src.engine.Generator=======================
        //==================START=========================

        while (randomize <= 2) {

            // Create local variable

            int max = feederPort.size() - 1;
            int min = 0;
            int randomNumber = (int) (Math.random() * ((max - min) + 1)) + min;

            if (portFiltered.size() == 0) {
                listNode = g.shortestPath(hubPort.get(hubPortCounter), feederPort.get(randomNumber));
            } else if (portFiltered.size() < 5) {
                listNode = g.shortestPath(portFiltered.get(portFiltered.size() - 1), feederPort.get(randomNumber));
            } else {
                listNode = g.shortestPath(portFiltered.get(portFiltered.size() - 1), hubPort.get(hubPortCounter));
            }

            totalDistancePerTrayek += Node.pathLength(listNode);

            Iterator<Node> itrList1 = listNode.iterator();
            while (itrList1.hasNext()) {
                Node node = itrList1.next();
                for (String string : seaName) {
                    if (node.id.equals(string))
                        itrList1.remove();
                }
            }

            for (Node node : listNode) {
                if (portFiltered.size() == 0)
                    portFiltered.add(node.id);
                else {
                    if (!portFiltered.get(portFiltered.size() - 1).equals(node.id))
                        portFiltered.add(node.id);

                }
            }

            int listNodeCounter = listNode.size();
            if (portFiltered.size() != 0) {
                while (checkCounter <= portFiltered.size() - 1) {
                    String checker = portFiltered.get(checkCounter);
                    for (String string : feederRemoved) {
                        if (string.equals(checker)) {
                            portFiltered.remove(checkCounter);
                        }
                    }
                    if (listNodeCounter <= 1)
                        checkCounter++;
                    else
                        listNodeCounter--;

                }
            }

            String temp;
            while (portFiltered.size() > 5) {
                if (portFiltered.get(portFiltered.size() - 1).equals(portFiltered.get(0))) {
                    break;
                } else {
                    temp = portFiltered.get(portFiltered.size() - 1);
                    if (portFiltered.get(portFiltered.size() - 1).equals(temp))
                        totalDistancePerTrayek -= Node.pathLength(
                                g.shortestPath(portFiltered.get(portFiltered.size() - 2),
                                               portFiltered.get(portFiltered.size() - 1)));
                    portFiltered.remove(portFiltered.size() - 1);
                }
            }

            Iterator<String> itrPortName = feederPort.iterator();
            while (itrPortName.hasNext()) {
                String string = itrPortName.next();
                for (String string2 : portFiltered) {
                    if (string2.equals(string)) {
                        feederRemoved.add(string2);
                        itrPortName.remove();
                    }
                }
            }

            if (portFiltered.size() == 6) {
                for (String string : portFiltered) {
                    temporaryString.append(string).append(", ");
                }
                listTrayek.put(trayekIdentifier, temporaryString.toString());
                jarakPerTrayek.add(totalDistancePerTrayek);
                trayekIdentifier++;
                hubPortCounter++;
                temporaryString.setLength(0);
                totalDistancePerTrayek = 0;
                checkCounter = 1;
                portFiltered.clear();
            }

            if (listTrayek.size() == 4) {

                System.out.println();
                System.out.println("Random " + randomize);
                System.out.println("Kombinasi" + "     ||     " + "                        Trayek");
                for (Map.Entry<Integer, String> entry : listTrayek.entrySet()) {
                    System.out.println("     " + entry.getKey() + "        ||     " + entry.getValue());
                }

                System.out.println();
                System.out.println("Kombinasi" + "     ||     " + "     Jarak");
                int jarakCounter = 0;
                for (Map.Entry<Integer, String> entry : listTrayek.entrySet()) {
                    System.out.println("     " + entry.getKey() + "        ||     " + jarakPerTrayek.get(jarakCounter));
                    jarakCounter++;
                }

                //writeToTextFile();

                clearEverything();
                refillFeederPortList();
                randomize++;

            }

        }

        //=================src.engine.Generator======================
        //====================END=========================

        JOptionPane.showMessageDialog(null, "Generator successfull!", "", JOptionPane.INFORMATION_MESSAGE);

    }
}
