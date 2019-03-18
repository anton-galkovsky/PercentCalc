package comp;

import javafx.geometry.Point2D;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Producer {

    static ArrayList<Graph> graphs;
    private static ArrayList<Double> t_grid;
    private static ArrayList<Double> acc_grid;

    static Record current_record;

    Producer() {
        graphs = new ArrayList<>();
        t_grid = new ArrayList<>();
        acc_grid = new ArrayList<>();
    }

    static void produce(Record[] records) throws Exception {

        for (Record record : records) {
            System.out.print(record.file);
            current_record = record;

            graphs.clear();

            NormalizedAccelerometerData nad = read_file(record.file);
            if (nad == null)
                throw new Exception();

//            Constants.measure_dur_t = Math.max(Constants.integ_segment_t, nad.t[nad.t.length - 1]); //soon delete it
//            Constants.interp_length_p = (int) (100 * Constants.measure_dur_t); //soon delete it

            if (Constants.draw_interp) {
                t_grid.clear();
                acc_grid.clear();

                for (double t_loc = nad.t[0]; t_loc < nad.t[nad.t.length - 1]; t_loc += 0.001)
                    t_grid.add(t_loc);

                PolynomialSplineFunction f = new SplineInterpolator().interpolate(nad.t, nad.acc);
                for (double t : t_grid)
                    acc_grid.add(f.value(t));

                add_chart(t_grid, acc_grid, "graph");
                //add_chart(nad.t, nad.acc, "acc");


                XYChart demo = new XYChart("Interpolation");
                demo.pack();
                demo.setVisible(true);

                graphs.clear();
            }

            AccelerometerListener al = new AccelerometerListener();
            for (int i = 0; i < nad.t.length; i++)
                al.onSensorChanged(nad.acc[i], nad.t[i]);

            System.out.println();
        }

        if (Constants.draw_answers && Hunter.results.size() != 0) {
            Hunter.results.add(new double[] {0, 0.049, 0.53, 0, 0});
            double x_sup = 0.000001;
            double y_sup = 0.000001;
            for (double[] result : Hunter.results) {
                x_sup = Math.max(x_sup, result[2]);
                y_sup = Math.max(y_sup, result[1]);
            }
            x_sup *= 1.1;
            y_sup *= 1.1;


            int side = 180;
            double[][] answers_map = new double[side][side];
            for (int i = Hunter.results.size() - 1; i >= 0; i--) {
                if (Hunter.results.get(i)[0] == 0)
                    answers_map[(int) (Hunter.results.get(i)[1] / y_sup * side)][(int) (Hunter.results.get(i)[2] / x_sup * side)] = Hunter.results.get(i)[4];
                else
                    answers_map[(int) (Hunter.results.get(i)[1] / y_sup * side)][(int) (Hunter.results.get(i)[2] / x_sup * side)] = -1;
            }
            new XYZChart("Answers Map", answers_map,
                    (int) (x_sup * 1000000 / side), (int) (y_sup * 1000000 / side), "ampl", "dur");

            System.out.println();
            System.out.println();
            int a = 0, b = 0, c = 0, d = 0;
            for (double[] result : Hunter.results) {
                if (result[4] == 80 || result[4] == 15) {
                    if (result[1] > -result[2] / 10 + 0.0225)
                        d++;
                    else
                        c++;
                }
                if (result[4] == 30) {
                    if (result[1] > -result[2] / 10 + 0.0225)
                        b++;
                    else
                        a++;
                }
            }
            System.out.println(a);
            System.out.println(b);
            System.out.println(c);
            System.out.println(d);
            System.out.println();
        }

    }

    private static NormalizedAccelerometerData read_file(String file) {
        try {
            BufferedReader fid = new BufferedReader(new FileReader(file));

            String line;
            List<Double> acc = new ArrayList<>();
            List<Double> t = new ArrayList<>();
            double t0 = -1;
            while ((line = fid.readLine()) != null) {
                double t1 = Double.valueOf(line.substring(0, line.indexOf(':'))) / 1000000000;
                if (t0 == -1)
                    t0 = t1;
                t1 -= t0;

                if (t.size() != 0 && t1 - t.get(t.size() - 1) < 0.006)
                    continue;
//                if (t1 > 300)
//                    break;

                t.add(t1);
                //if (t.size() > 2 && t1 - t.get(t.size() - 2) > 0.05)
                //    System.out.println(t.get(t.size() - 2));

//                double x1, x2, x3;
//                line = line.substring(line.indexOf(':') + 1);
//                line = line.substring(line.indexOf(':') + 1);
//                x1 = Double.valueOf(line.substring(0, line.indexOf(':')));
//                line = line.substring(line.indexOf(':') + 1);
//                x2 = Double.valueOf(line.substring(0, line.indexOf(':')));
//                line = line.substring(line.indexOf(':') + 1);
//                x3 = Double.valueOf(line);
////                acc.add(Math.sqrt(x1 * x1 + x2 * x2 + x3 * x3));
//                acc.add(x3);

                line = line.substring(line.indexOf(':') + 1);
                acc.add(Double.valueOf(line.substring(0, line.indexOf(':'))));


            }
            fid.close();
            System.out.printf("%10.3f", t.get(t.size() - 1));
            double[] t_norm = new double[t.size()];
            double[] acc_norm = new double[acc.size()];
            for (int i = 0; i < t.size(); i++)
                t_norm[i] = t.get(i);
            for (int i = 0; i < acc.size(); i++)
                acc_norm[i] = acc.get(i);
            return new NormalizedAccelerometerData(t_norm, acc_norm);
        } catch (Exception e) {
            return null;
        }
    }

    static void add_map_chart(int[] inds, ArrayList<Double> ts, double[] chart, String name) {
        ArrayList<Point2D> al = new ArrayList<>();
        for (Integer ind : inds)
            if (ind != -1)
                al.add(new Point2D(ts.get(ind), chart[ind]));
        graphs.add(new Graph(al, name));
    }

    static void add_filter(int[] inds, ArrayList<Double> ts, String name) {
        ArrayList<Point2D> al = new ArrayList<>();
        for (int i = 0; i < inds.length; i++)
            al.add(new Point2D(ts.get(i), inds[i] != -1 ? 1 : 0));
        graphs.add(new Graph(al, name));
    }

    static void add_chart(ArrayList<Double> ts, double[] chart, String name) {
        ArrayList<Point2D> al = new ArrayList<>();
        for (int i = 0; i < ts.size(); i++)
            al.add(new Point2D(ts.get(i), chart[i] + 0.01));
        graphs.add(new Graph(al, name));
    }

    private static void add_chart(ArrayList<Double> ts, ArrayList<Double> chart, String name) {
        ArrayList<Point2D> al = new ArrayList<>();
        for (int i = 0; i < ts.size(); i++)
            al.add(new Point2D(ts.get(i), chart.get(i)));
        graphs.add(new Graph(al, name));
    }

    private static void add_chart(double[] ts, double[] chart, String name) {
        ArrayList<Point2D> al = new ArrayList<>();
        for (int i = 0; i < ts.length; i++)
            al.add(new Point2D(ts[i], chart[i]));
        graphs.add(new Graph(al, name));
    }
//
//    public static void add_map_chart(List<Integer> inds, ArrayList<Double> tt, ArrayList<Double> acc_tt, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        for (Integer ind : inds)
//            al.add(new Point2D(tt.get(ind), acc_tt.get(ind)));
//        graphs.add(new Graph(al, name));
//    }
//
//    public static void add_map_chart(List<Integer> inds, ArrayList<Double> tt, double[] chart, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer ind : inds) {
//            if (chart[j] != 0)
//                al.add(new Point2D(tt.get(ind), chart[j++]));
//            else
//                j++;
//            if (j == chart.length)
//                break;
//        }
//        graphs.add(new Graph(al, name));
//    }
//
//    public static void add_map_chart(int[] inds, ArrayList<Double> tt, double[] chart, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer ind : inds) {
//            if (chart[j] != 0)
//                al.add(new Point2D(tt.get(ind), chart[j++]));
//            else
//                j++;
//            if (j == chart.length)
//                break;
//        }
//        graphs.add(new Graph(al, name));
//    }
//
//    public static void add_map_chart(int[] inds, ArrayList<Double> tt, int[] chart, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer ind : inds) {
//            if (chart[j] != 0)
//                al.add(new Point2D(tt.get(ind), chart[j++]));
//            else
//                j++;
//            if (j == chart.length)
//                break;
//        }
//        graphs.add(new Graph(al, name));
//    }
//
//    public static void add_map_chart(int[] inds, ArrayList<Double> tt, List<Integer> chart, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer ind : inds)
//            al.add(new Point2D(tt.get(ind), chart.contains(j++) ? 1 : 0));
//        graphs.add(new Graph(al, name));
//    }
//
//    public static void add_map_chart(int[] cluster, int[] inds, ArrayList<Double> tt, double[] chart, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer cl : cluster)
//            al.add(new Point2D(tt.get(inds[cl]), chart[j++]));
//        graphs.add(new Graph(al, name));
//    }
//
//    public static void add_map_chart(int[] inds, ArrayList<Double> tt, boolean[] chart, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer ind : inds)
//            al.add(new Point2D(tt.get(ind), chart[j++] ? 1 : 0));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_double_arr1(List<Double> ar1, List<Double> ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        for (int i = 0; i < ar1.size(); i++)
//            al.add(new Point2D(ar1.get(i), ar2.get(i)));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_double_arr1(List<Integer> ind, List<Double> ar1, List<Double> ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        for (Integer i : ind)
//            al.add(new Point2D(ar1.get(i), ar2.get(i)));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_double_arr1(List<Integer> ar0, int[] ind, List<Double> ar1, double[] ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer i : ind)
//            al.add(new Point2D(ar1.get(ar0.get(i)), ar2[j++]));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_double_arr2(List<Integer> ind, List<Double> ar1, List<Double> ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer i : ind)
//            al.add(new Point2D(ar1.get(i), ar2.get(j++)));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_double_arr3(List<Integer> ar0, int[] ind, List<Double> ar1, double[] ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int n = ind.length;
//        int n2 = ar2.length;
//        int j = 0;
//        for (int i = (n - n2) / 2; i < (n + n2) / 2; i++)
//            al.add(new Point2D(ar1.get(ar0.get(ind[i])), ar2[j++]));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_double_arr3(List<Integer> ind, List<Double> ar1, double[] ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int n = ind.size();
//        int n2 = ar2.length;
//        int j = 0;
//        for (int i = (n - n2) / 2; i < (n + n2) / 2; i++)
//            al.add(new Point2D(ar1.get(ind.get(i)), ar2[j++]));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_boolean_arr(List<Integer> ind, List<Double> ar1, boolean[] ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer i : ind)
//            al.add(new Point2D(ar1.get(i), ar2[j++] ? 1 : 0));
//        graphs.add(new Graph(al, name));
//    }
//
//    static void add_boolean_arr(List<Integer> ar0, int[] ind, List<Double> ar1, boolean[] ar2, String name) {
//        ArrayList<Point2D> al = new ArrayList<Point2D>();
//        int j = 0;
//        for (Integer i : ind)
//            al.add(new Point2D(ar1.get(ar0.get(i)), ar2[j++] ? 1 : 0));
//        graphs.add(new Graph(al, name));
//    }
}
