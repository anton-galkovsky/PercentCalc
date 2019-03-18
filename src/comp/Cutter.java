package comp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Cutter {

    private static String file = "new_v/19(7).txt";
    private static String suffix = "_cut2";
    private static double start = 670;
    private static double end = 1285;

    public static void main(String[] args) throws Exception {
        BufferedReader fin = new BufferedReader(new FileReader(file));
        BufferedWriter fout = new BufferedWriter(new FileWriter(file + suffix));

        String line;
        double t0 = -1;
        while ((line = fin.readLine()) != null) {
            double t1 = Double.valueOf(line.substring(0, line.indexOf(':'))) / 1000000000;
            if (t0 == -1)
                t0 = t1;
            t1 -= t0;
            if (start <= t1 && t1 <= end) {
                fout.write(line);
                fout.newLine();
            }
        }
        fin.close();
        fout.close();
    }
}
