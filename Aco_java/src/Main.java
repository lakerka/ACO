import java.util.Arrays;
import java.util.List;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;

public class Main {

    public static final String PATH = "/home/zilva/vu/kursinis/data";
    public static final String SEPARATOR = "/";
    public static final String FILENAME = "att48.tsp";
    public static final String PLOT_FILENAME = "test.dat";

    // opt path distance:
    // att48: 30725076

    static List<Integer> optPathList = Arrays.asList(1, 8, 38, 31, 44, 18, 7,
	    28, 6, 37, 19, 27, 17, 43, 30, 36, 46, 33, 20, 47, 21, 32, 39, 48,
	    5, 42, 24, 10, 45, 35, 4, 26, 2, 29, 34, 41, 16, 22, 3, 23, 14, 25,
	    13, 11, 12, 15, 40, 9);

    public static void main(String[] args) {

	 Graph g = new Graph(PATH + SEPARATOR + FILENAME);
	
	 ACO_system acoSys = new ACO_system(48, 48, g);
	 acoSys.start();

	// Long optPathDistance = g.calcDistance(optPathList);
	// System.out.println(optPathDistance + "");
    }

}
