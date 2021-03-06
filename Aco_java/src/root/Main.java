package root;

import java.util.Arrays;
import java.util.List;


public class Main {

    public static final String PATH = "/home/zilva/vu/bakalaurinis/myWork/programs/data";
    public static final String SEPARATOR = "/";
    public static final String FILENAME = "eil76.tsp";
    public static final String PLOT_FILENAME = "test.dat";

    // eil51, Best 426
    // eil76, Best 538

    static List<Integer> optPathList =
    // new ArrayList<>();

    // eil 51
    Arrays.asList(1, 22, 8, 26, 31, 28, 3, 36, 35, 20, 2, 29, 21, 16, 50, 34,
	    30, 9, 49, 10, 39, 33, 45, 15, 44, 42, 40, 19, 41, 13, 25, 14, 24,
	    43, 7, 23, 48, 6, 27, 51, 46, 12, 47, 18, 4, 17, 37, 5, 38, 11, 32);

    // eil 76
    // Arrays.asList(1, 33, 63, 16, 3, 44, 32, 9, 39, 72, 58, 10, 31, 55, 25,
    // 50,
    // 18, 24, 49, 23, 56, 41, 43, 42, 64, 22, 61, 21, 47, 36, 69, 71, 60,
    // 70, 20, 37, 5, 15, 57, 13, 54, 19, 14, 59, 66, 65, 38, 11, 53, 7,
    // 35, 8, 46, 34, 52, 27, 45, 29, 48, 30, 4, 75, 76, 67, 26, 12, 40,
    // 17, 51, 6, 68, 2, 74, 28, 62, 73);

    public static void main(String[] args) {
	
	Graph graph = new Graph(PATH + SEPARATOR + FILENAME);
	
	startAntSystem(graph);

	// Scanner finput;
	// int input = 0;
	// finput = new Scanner(System.in);
	//
	//
	// while ((input = finput.nextInt()) != 0) {
	//
	// System.out.println("OUTPUT: " + input);
	// demo.setDataSet(input);
	// }
	//
	// finput.close();

	// g.plot(optPathList, 0, 0, null);

	// for (int i = 1; i <= 442; i++) {
	// optPathList.add(i);
	// }

	// Long optPathDistance = g.calcDistance(optPathList);
	// System.out.println(optPathDistance + "");
	
    }

    public static void startAntSystem(Graph graph) {

	AntSystem acoSys = new AntSystem(51, 70, graph);
	acoSys.start();

    }

    public static void startGreedy(Graph graph) {

	Greedy greedy = new Greedy(51, 70, graph);
	greedy.start();
    }

}
