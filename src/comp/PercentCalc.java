package comp;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.ArrayList;

import static comp.Constants.constant;


public class PercentCalc {

    private double[] delay;
    private double[] stretch;
    private int[] filter;             //  filter[i] == -1 if bad
    private int filter_length;        //  filter_length != filter.length

    private double N_smooth;
    private double fun_smooth;

    private static double DT = 0.001;

    PercentCalc() {
        N_smooth = 0.0;
        fun_smooth = 0.0;
    }

    private ArrayList<Double> steps_t;

    public Double percent_calc(NormalizedAccelerometerData nad) throws Exception {

        steps_t = new ArrayList<>();
        double cur_t = 0;
        steps_t.add(cur_t);
        for (Double t : nad.t) {
            cur_t += t;
            steps_t.add(cur_t);
        }
        steps_t.remove(steps_t.size() - 1);


        delay = nad.t;
        stretch = nad.acc;

        filter = new int[delay.length];
        for (int i = 0; i < filter.length; ++i)
            filter[i] = i;
        filter_length = filter.length;


        Double fun = calc_fun();
        if (fun == null || filter_length == 0)
            return null;

        fun_smooth = (fun_smooth * N_smooth + fun * filter_length) / (N_smooth + filter_length);
        N_smooth = 0.8 * (N_smooth + filter_length);

        return fun_smooth;
    }

//    boolean b = true;

    private Double calc_fun() {
        int length = delay.length;
        if (length < 2)
            return null;

        int L_window = 3;

        del_outliers(stretch, Constants.constant[5]);
        del_outliers(delay, Constants.constant[6]);

        double[] loc_var_stretch = new double[length];
        double[] loc_var_delay = new double[length];
        double[] loc_mean_stretch = new double[filter_length];
        double[] loc_mean_delay = new double[filter_length];
        DescriptiveStatistics ds_stretch;
        DescriptiveStatistics ds_delay;

        int filter_ind = 0;
        for (int ind : filter)
            if (ind != -1) {
                ds_stretch = new DescriptiveStatistics();
                ds_delay = new DescriptiveStatistics();

                for (int j = ind, num = 0; j >= 0 && num < L_window; --j)
                    if (filter[j] != -1) {
                        num++;
                        ds_stretch.addValue(stretch[j]);
                        ds_delay.addValue(delay[j]);
                    }
                for (int j = ind + 1, num = 0; j < filter.length && num < L_window - 1; ++j)
                    if (filter[j] != -1) {
                        num++;
                        ds_stretch.addValue(stretch[j]);
                        ds_delay.addValue(delay[j]);
                    }

                loc_var_stretch[ind] = ds_stretch.getStandardDeviation();
                loc_var_delay[ind] = ds_delay.getStandardDeviation();
                loc_mean_stretch[filter_ind] = ds_stretch.getMean();
                loc_mean_delay[filter_ind] = ds_delay.getMean();
                filter_ind++;
            }

        ds_stretch = new DescriptiveStatistics();
        ds_delay = new DescriptiveStatistics();

        for (int ind : filter)
            if (ind != -1) {
                ds_stretch.addValue(loc_var_stretch[ind]);
                ds_delay.addValue(loc_var_delay[ind]);
            }

        double ampl = ds_stretch.getMean() / new Median().evaluate(loc_mean_stretch);
        double dur = ds_delay.getMean() / new Median().evaluate(loc_mean_delay);

        double fun = (dur * constant[0] + ampl * constant[1] + constant[2]) * 100.0;

        System.out.print("          ");
        System.out.printf("   dur:%10.6f   ampl:%10.6f", dur, ampl);

        Hunter.results.get(Hunter.results.size() - 1)[1] = dur;
        Hunter.results.get(Hunter.results.size() - 1)[2] = ampl;

        if (Constants.draw_analysis) {
            Producer.graphs.clear();

            Producer.add_map_chart(filter, steps_t, stretch, "ampls_filter");
            Producer.add_map_chart(filter, steps_t, delay, "delays_filter");

            Producer.add_chart(steps_t, stretch, "ampls");
            Producer.add_map_chart(filter, steps_t, loc_var_delay, "delay_deviation");

            Producer.add_map_chart(filter, steps_t, loc_var_stretch, "ampl_deviation");
            Producer.add_filter(filter, steps_t, "filter");
            Producer.add_chart(steps_t, delay, "delays");


            XYChart demo = new XYChart("Analysis");
            demo.pack();
            demo.setVisible(true);
        }

        return fun;

//        double[] mean_stretch = new double[filter_length];
//        double[] var_stretch = new double[length];
//
//        for (int i = 0; i < filter_length; ++i) {
//            DescriptiveStatistics ds = new DescriptiveStatistics();
//            for (int j = Math.max(i - L_window, 0); j < Math.min(i + 1 + L_window, filter_length); ++j)
//                ds.addValue(stretch_cluster[j]);
//
//            mean_stretch[i] = ds.getMean();
//            var_stretch[ind_cluster.get(i)] = ds.getStandardDeviation();
//        }
//        //нашли разброс и среднее значение
//
//
//        double[] t_stretch = new double[filter_length];
//        for (int i = 0; i < filter_length; ++i)
//            t_stretch[i] = filter[ind_cluster.get(i)] * DT;
//
//        double[] mean_stretch_smooth = new double[filter_length];
//        SmoothingCubicSpline scs_stretch = new SmoothingCubicSpline(t_stretch, mean_stretch, 0.5);
//        for (int i = 0; i < mean_stretch_smooth.length; ++i)
//            mean_stretch_smooth[i] = scs_stretch.evaluate(t_stretch[i]);
//        //сгладили среднюю амплитуду
//
//
//        double[] der_mean_stretch = new double[filter_length - 1];
//        for (int i = 0; i < der_mean_stretch.length; ++i)
//            der_mean_stretch[i] = mean_stretch_smooth[i + 1] - mean_stretch_smooth[i];
//        //нашли производную сглаженной усреднённой амплитуды
//
//        boolean[] chi_thresh = new boolean[der_mean_stretch.length];
//        for (int i = 0; i < chi_thresh.length; ++i)
//            chi_thresh[i] = Math.abs(der_mean_stretch[i]) < constant[3];
//        //фильтр: производная сглаженной усреднённой амплитуды не очень велика
//
//        boolean[] chi_extended = new boolean[chi_thresh.length + 5];
//        for (int i = 0; i < chi_extended.length; ++i)
//            if (i <= 1 || i >= chi_extended.length - 2)
//                chi_extended[i] = true;
//            else if (i == 2)
//                chi_extended[i] = chi_thresh[i - 2];
//            else if (i == chi_extended.length - 3)
//                chi_extended[i] = chi_thresh[i - 3];
//            else
//                chi_extended[i] = (chi_thresh[i - 2] & chi_thresh[i - 3]);
//        //доопределили на расширенный интервал
//
//        boolean[] chi_der_stretch = new boolean[chi_extended.length - 4]; // length == ind_cluster_size
//        for (int i = 2; i < chi_extended.length - 2; ++i)
//            chi_der_stretch[i - 2] = chi_extended[i - 2] & chi_extended[i - 1] & chi_extended[i] & chi_extended[i + 1] & chi_extended[i + 2];
//        //фильтр: производные среднесглаженной амплитуды не очень велики подряд
//
//        boolean[] chi_ampl_stretch = new boolean[filter_length];
//        for (int i = 0; i < chi_ampl_stretch.length; ++i)
//            chi_ampl_stretch[i] = mean_stretch_smooth[i] > constant[4];
//        //фильтр: среднесглаженная амплитуда достаточно велика
//
//        boolean[] chi_stretch = new boolean[length];
//        for (int i = 0; i < filter_length; ++i)
//            chi_stretch[ind_cluster.get(i)] = chi_der_stretch[i] & chi_ampl_stretch[i];
//        //пересекли фильтры
//
//
//        double[] mean_delay = new double[filter_length];
//        double[] var_delay = new double[length];
//
//        for (int i = 0; i < filter_length; ++i) {
//            DescriptiveStatistics ds = new DescriptiveStatistics();
//            for (int j = Math.max(i - L_window, 0); j < Math.min(i + 1 + L_window, filter_length); ++j)
//                ds.addValue(delay_cluster[j]);
//
//            mean_delay[i] = ds.getMean();
//            var_delay[ind_cluster.get(i)] = ds.getStandardDeviation();
//        }
//        //нашли разброс и среднее значение
//
//        double[] t_delay = new double[filter_length];
//        for (int i = 0; i < t_delay.length; ++i)
//            t_delay[i] = filter[ind_cluster.get(i)] * DT;
//
//        double[] mean_delay_smooth = new double[filter_length];
//        SmoothingCubicSpline scs_delay = new SmoothingCubicSpline(t_delay, mean_delay, 0.8);
//        for (int i = 0; i < mean_delay_smooth.length; ++i)
//            mean_delay_smooth[i] = scs_delay.evaluate(t_delay[i]);
//        //сгладили среднюю задержку
//
//        double[] der_mean_delay = new double[filter_length - 1];
//        for (int i = 0; i < der_mean_delay.length; ++i)
//            der_mean_delay[i] = mean_delay_smooth[i + 1] - mean_delay_smooth[i];
//        //нашли производную сглаженной усреднённой задержки
//
//        double threshold_delay = 0.001;
//        boolean[] chi_der_delay_temp = new boolean[filter_length - 1];
//        int N_ampl_stretch = sum(chi_ampl_stretch, filter_length - 1);
//        while (sum(chi_der_delay_temp, chi_ampl_stretch, filter_length - 1) < N_ampl_stretch * 0.9) {
//            for (int i = 0; i < chi_der_delay_temp.length; ++i)
//                chi_der_delay_temp[i] = Math.abs(der_mean_delay[i]) < threshold_delay;
//            threshold_delay *= 1.1;
//        }
//        //фильтр: производные среднесглаженной задержки не очень велики; они наполовину там же, где и предыдущий фильтр
//
//        boolean[] chi_der_delay = new boolean[filter_length];
//        chi_der_delay[0] = chi_der_delay_temp[0];
//        chi_der_delay[filter_length - 1] = chi_der_delay_temp[filter_length - 2];
//        for (int i = 1; i < filter_length - 1; ++i)
//            chi_der_delay[i] = chi_der_delay_temp[i - 1] & chi_der_delay_temp[i];
//        //фильтр: производные среднесглаженной задержки не очень велики подряд
//
//        boolean[] chi_delay = new boolean[length];
//        for (int i = 0; i < filter_length; ++i)
//            chi_delay[ind_cluster.get(i)] = chi_der_delay[i];
//
//
//        List<Integer> ind_full = new ArrayList<Integer>();
//        for (int i = 0; i < length; ++i)
//            if (chi_delay[i] & chi_stretch[i])
//                ind_full.add(i);
//        //пересекли все фильтры
//
//        if (ind_full.size() == 0)
//            return new CalcFunStruct(0.0, 0);
//
//        DescriptiveStatistics ds_ampl = new DescriptiveStatistics();
//        for (Integer value : ind_full)
//            ds_ampl.addValue(var_stretch[value]);
//        double amplitude = ds_ampl.getMean();
//        //усреднили разброс амплитуды
//
//        DescriptiveStatistics ds_delay = new DescriptiveStatistics();
//        for (Integer value : ind_full)
//            ds_delay.addValue(var_delay[value]);
//        double duration = ds_delay.getMean();
//        //усреднили разброс задержки
//
//
//        if (b) {
//            Producer.add_map_chart(filter, steps_t, stretch, "ampls");
//            Producer.add_map_chart(filter, steps_t, delay, "steps_t");
//
//            Producer.add_map_chart(filter, steps_t, var_stretch, "ampl_deviation");
//            Producer.add_map_chart(filter, steps_t, var_delay, "delay_deviation");
//            Producer.add_map_chart(filter, steps_t, ind_full, "filter");
//
////            int[] ind_der = new int[ind_cluster_size - 1];
////            System.arraycopy(filter, 0, ind_der, 0, ind_der.length);
////            Producer.add_map_chart(ind_der, t_grid, der_mean_stretch, "der_ampl_mean_smooth");
////
////            Producer.add_map_chart(filter, steps_t, chi_ampl_stretch, "ampl_mean_smooth_filter");
//
//
////            Producer.add_map_chart(filter, t_grid, stretch, "ampl_odd");
////            Producer.add_map_chart(filter, t_grid, mean_stretch, "ampl_mean");
////
////
////            Producer.add_map_chart(filter, t_grid, var_stretch, "ampl_deviation");
////            Producer.add_map_chart(filter, t_grid, var_delay, "delay_deviation");
////
////
////            Producer.add_map_chart(filter, t_grid, mean_stretch_smooth, "ampl_mean_smooth");
////            int[] ind_der = new int[ind_cluster_size - 1];
////            System.arraycopy(filter, 0, ind_der, 0, ind_der.length);
////            Producer.add_map_chart(ind_der, t_grid, der_mean_stretch, "der_ampl_mean_smooth");
////
////            Producer.add_map_chart(filter, t_grid, ind_full, "odd_filter");
////            Producer.add_map_chart(filter, t_grid, chi_ampl_stretch, "ampl_mean_smooth_filter");
//
//            b = false;
//        }
//
//        double ratio = amplitude / new Median().evaluate(mean_stretch);
//        double fun = (duration * constant[0] + ratio * constant[1] + constant[2]) * 100.0;
//
//        System.out.printf("   dur:%8.4f   rat:%8.4f", duration, ratio);
//
//        int N_reliable = ind_full.size();
//        return new CalcFunStruct(fun, N_reliable);
    }

    private void del_outliers(double[] arr, double coef) {
        int old_length;
        double sigma;
        double mean;
        DescriptiveStatistics ds;

        do {
            old_length = filter_length;

            ds = new DescriptiveStatistics();
            for (int ind : filter)
                if (ind != -1)
                    ds.addValue(arr[ind]);

            mean = ds.getMean();
            sigma = ds.getStandardDeviation();

            for (int i = 0; i < filter.length; ++i)
                if (filter[i] != -1)
                    if ((arr[filter[i]] - mean) * (arr[filter[i]] - mean) > coef * coef * sigma * sigma) {
                        filter[i] = -1;
                        filter_length--;
                    }
        } while (old_length > filter_length);
    }

    private int sum(boolean[] b, int length) {
        int sum = 0;
        for (int i = 0; i < length; ++i)
            if (b[i])
                ++sum;
        return sum;
    }

    private int sum(boolean[] a, boolean[] b, int length) {
        int sum = 0;
        for (int i = 0; i < length; ++i)
            if (b[i] & a[i])
                ++sum;
        return sum;
    }

    private class CalcFunStruct {
        double fun;
        int N_reliable;

        CalcFunStruct(double fun, int n_reliable) {
            this.fun = fun;
            this.N_reliable = n_reliable;
        }
    }
}