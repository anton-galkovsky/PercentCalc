package comp;

import javafx.geometry.Point2D;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.*;

class AccelerometerListener {

    private List<Double> ts;
    private List<Double> accs;
    private double[] ts_md;
    private double[] accs_md;

    private volatile boolean counting;

    private double[][] pers_map;
    private double[] interp;
    //    private int[] pers_hyp_path;
    private int per_st, per_end, integ_segm;
    //    private double[][] loc_mins_map;
    private ArrayList<Integer> steps_pers;
    private NormalizedAccelerometerData steps;
    private double alpha;

    private double last_walk;
//    private double last_

    AccelerometerListener() {

        ts = new ArrayList<>();
        accs = new ArrayList<>();

        counting = false;

        interp = new double[Constants.interp_length_p];
        steps_pers = new ArrayList<>();

        last_walk = -10;
    }

    boolean waiting = true;

    void onSensorChanged(double acc, double t) {
        if (acc > 13)
            last_walk = t;
        if (t - last_walk > 1) {
            ts.clear();
            accs.clear();
            return;
        }
        if (ts.size() != 0) {
            if (t - ts.get(ts.size() - 1) < 0.006)
                return;
            if (t - ts.get(ts.size() - 1) > 0.05) {
                ts.clear();
                accs.clear();
                return;
            }
        }


        ts.add(t);
        accs.add(acc);

        if (!counting && t - ts.get(0) >= Constants.measure_dur_t + 3 + 5) {

            int ind_l = 0;
            int ind_r = ts.size() - 1;
            while (t - ts.get(ind_r) < 3)
                ind_r--;
            while (ts.get(ind_r) - ts.get(ind_l) > Constants.measure_dur_t)
                ind_l++;

            System.out.println();
            System.out.print("                                        ");
            System.out.printf("%10.3f", ts.get(ind_l));

            int ts_md_size = ind_r - ind_l;
            ts_md = new double[ts_md_size];
            accs_md = new double[ts_md_size];

            for (int i = 0; i < ts_md_size; i++) {
                ts_md[i] = ts.get(i + ind_l);
                accs_md[i] = accs.get(i + ind_l);
            }


            while (t - ts.get(ind_r) < 5 + 3)
                ind_r--;
            ts = ts.subList(ind_r + 1, ts.size() - 1);
            accs = accs.subList(ind_r + 1, accs.size() - 1);

            Hunter.results.add(new double[]{0, 0, 0, 0, Producer.current_record.verdict});
            analyze();
        }
    }

    private void analyze() {
        find_pers();
        fill_steps();

        try {
            Double fun = new PercentCalc().percent_calc(steps);
            if (fun != null) {
                Hunter.results.get(Hunter.results.size() - 1)[3] = fun;
                System.out.printf("   fun: %10.6f", fun);
            } else {
                Hunter.results.get(Hunter.results.size() - 1)[0] = 1;
                System.out.print("   fun: null");
            }
        } catch (Exception e) {
            Hunter.results.get(Hunter.results.size() - 1)[0] = -1;
            System.out.print("   fun: error");
        }
    }

    private void fill_steps() {
        double[] steps_t = new double[steps_pers.size() - 1];
        double[] steps_acc = new double[steps_pers.size() - 1];

        double integral;
        double integ_sup = 0;
        double aver = 0;
        int start = 0;

        for (int i = 0; i < steps_pers.size() - 1; i++) {
            integral = 0;
            for (int j = start, end = Math.min(steps_pers.get(i) + start, Constants.interp_length_p - integ_segm); j < end; j++)
//                integral += Math.abs(interp[j] - 9.8);
//                integral += Math.pow(Math.abs(interp[j] - 9.8), 5);
//                integral += interp[j] - 9.8;
                integral = Math.max(Math.abs(interp[j] - 9.8), integral);

//            integral = Math.pow(integral, 1.0 / 5);

            //integral /= (Math.min(steps_pers.get(i) + start, Constants.interp_length_p - integ_segm) - start);
            integ_sup = Math.max(integ_sup, integral);
            aver += integral;
            steps_t[i] = steps_pers.get(i) * alpha;
            steps_acc[i] = integral;
            start += steps_pers.get(i);
        }
        for (int i = 0; i < steps_acc.length; i++)
            steps_acc[i] *= (steps_pers.size() - 1) / aver;

        steps = new NormalizedAccelerometerData(steps_t, steps_acc);
    }

    private void find_pers() {
        steps_pers.clear();

        alpha = (ts_md[ts_md.length - 1] - ts_md[0]) / Constants.interp_length_p;

        fill_interp(ts_md[0], ts_md.length, alpha, new SplineInterpolator().interpolate(ts_md, accs_md));

        per_st = (int) (Constants.min_period_t / alpha);
        per_end = (int) (Constants.max_period_t / alpha);
        integ_segm = (int) (Constants.integ_segment_t / alpha);

        find_steps_path();

        if (Constants.draw_pers_map) {

            pers_map = new double[per_end - per_st + 1][Constants.interp_length_p - integ_segm];

            fill_pers_map();

            int start = 0;
            for (Integer step : steps_pers) {
                for (int j = start; j < step + start && j < pers_map[0].length; j++)
                    pers_map[step - per_st][j] = -1;
                start += step;
            }

            new XYZChart("Periods Map", pers_map,
                    (int) (alpha * 1000000), (int) (alpha * 1000000), "x", "y");
        }
    }

    private void find_steps_path() {

        double integral;
        int increment = 1;
        double[] cur_pers = new double[per_end - per_st + 1];
        ArrayList<Point2D> loc_mins = new ArrayList<>();
        ArrayList<Point2D> first_loc_mins = new ArrayList<>();

        for (int start = 0; start < Constants.interp_length_p - integ_segm; start += increment) {
            for (int per = per_st; per <= per_end; per++) {
                integral = 0;
                for (int x = 0; x < integ_segm - per; x++) {
                    integral += Math.abs(interp[start + x] - interp[start + x + per]);
                }
                integral *= 1.0 * integ_segm / (integ_segm - per);

                cur_pers[per - per_st] = integral;
            }


            loc_mins.clear();
            first_loc_mins.clear();

            for (int per = 1; per < per_end - per_st; per++)
                if (cur_pers[per - 1] > cur_pers[per] && cur_pers[per] < cur_pers[per + 1])
                    loc_mins.add(new Point2D(cur_pers[per], per));

            try {
                loc_mins.sort((x, y) -> {
                    if (x.getX() == y.getX())
                        return (int) (100000.0 * (x.getY() - y.getY()));
                    else
                        return (int) (100000.0 * (x.getX() - y.getX()));
                });
            } catch (Exception e) {
                e.printStackTrace();
                for (Point2D p : loc_mins) {
                    System.out.println("loc_mins.add(new Point2D(" + p.getX() + ", " + p.getY() + "));");
                }
            }

            for (int i = 0; i < per_end / per_st && i < loc_mins.size(); i++)
                first_loc_mins.add(loc_mins.get(i));

            first_loc_mins.sort((x, y) -> {
                if (x.getY() == y.getY())
                    return (int) (100000.0 * (x.getX() - y.getX()));
                else
                    return (int) (100000.0 * (x.getY() - y.getY()));
            });

            for (int i = 0; i + 1 < first_loc_mins.size(); i++) {
                double a = first_loc_mins.get(i).getY() + per_st;
                double b = first_loc_mins.get(i + 1).getY() + per_st;
                if (first_loc_mins.get(i + 1).getX() > first_loc_mins.get(i).getX()
                        || (Math.rint(b / a) != 1 && Math.abs(b - a * Math.rint(b / a)) < 0.1 / alpha)) {
                    increment = (int) a;
                    steps_pers.add(increment);
                    break;
                }
            }
        }
    }

    private void fill_interp(double ts0_md, int ts_md_length, double alpha, PolynomialSplineFunction f) {
        for (int i = 0; i < Constants.interp_length_p - 1; i++)
            interp[i] = f.value(i * alpha + ts0_md);
        interp[Constants.interp_length_p - 1] = f.value(ts_md[ts_md_length - 1]);
    }


    private void fill_pers_map() {
        double integ_inf;
//        int per_inf;
        double integral;

        for (int start = 0; start < Constants.interp_length_p - integ_segm; start++) {
            integ_inf = Double.MAX_VALUE;
//            per_inf = -1;
            for (int per = per_st; per <= per_end; per++) {
                integral = 0;
                for (int x = 0; x < integ_segm - per; x++) {
                    integral += Math.abs(interp[start + x] - interp[start + x + per]);
                }
                integral *= 1.0 * integ_segm / (integ_segm - per);

                pers_map[per - per_st][start] = integral;

                if (integral < integ_inf) {
                    integ_inf = integral;
//                    per_inf = per;
                }
            }
//            pers_map[per_inf - per_st][start] = 1;
        }


        ArrayList<Point2D> loc_mins = new ArrayList<>();
        ArrayList<Point2D> loc_first_mins = new ArrayList<>();

        for (int start = 0; start < Constants.interp_length_p - integ_segm; start++) {
            loc_mins.clear();
            loc_first_mins.clear();
            for (int per = 1; per < per_end - per_st; per++)
                if (pers_map[per - 1][start] > pers_map[per][start] && pers_map[per][start] < pers_map[per + 1][start]) {
                    loc_mins.add(new Point2D(pers_map[per][start], per));
//                    pers_map[per][start] = 1;
                }
            loc_mins.sort((x, y) -> {
                if (x.getX() == y.getX())
                    return (int) (x.getY() - y.getY());
                else
                    return (int) (x.getX() - y.getX());
            });
            for (int i = 0; i < per_end / per_st && i < loc_mins.size(); i++)
                loc_first_mins.add(loc_mins.get(i));
            loc_first_mins.sort((x, y) -> {
                if (x.getY() == y.getY())
                    return (int) (x.getX() - y.getX());
                else
                    return (int) (x.getY() - y.getY());
            });
            for (int i = 0; i + 1 < loc_first_mins.size(); i++) {
                double a = loc_first_mins.get(i).getY() + per_st;
                double b = loc_first_mins.get(i + 1).getY() + per_st;
                if (loc_first_mins.get(i + 1).getX() > loc_first_mins.get(i).getX()
                        || (Math.rint(b / a) != 1 && Math.abs(b - a * Math.rint(b / a)) < 10)) {
                    //pers_map[(int) loc_first_mins.get(i).getY()][start] = 0;
                    break;
                }
            }

        }
    }
}