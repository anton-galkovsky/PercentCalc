package comp;

import java.util.Random;

class Constants {

    final static double min_period_t = 0.8; // >> 0
    final static double max_period_t = 2.3;
    final static double integ_segment_t = 5;
    static double measure_dur_t = 20;
    static int interp_length_p = (int) (100 * measure_dur_t);

    static boolean draw_interp = false;
    static boolean draw_pers_map = false;
    static boolean draw_analysis = false;
    static boolean draw_answers = true;

    static double[] constant  = {10.04, 1.425, -0.05, 0.3, 2.0, 3, 2.5}; //1.5 2.5

    private static double[] range_l  = {10      , 0.5, -1.2              };//, 0.2, 1.0 };
    private static double[] range_r  = {100     , 1.5, -1.1              };//, 0.4, 3.0 };

//    private static double[] accuracy = {0.01      , 0.005,  0.005             };//, 0.1, 1.0};
    private static double[] accuracy = {0.001      , 0.0005,  0.0005             };//, 0.1, 1.0};

    private static boolean[] sign = {true, true,  true};//, 1  , 1  };

    static int length;
    private static int pointer;

    Constants(int type) {
        if (type == 0) {
            length = 3;
            System.arraycopy(range_l, 0, constant, 0, length);
            pointer = 0;
        }
        if (type == 1) {
            length = 3;
            pointer = 0;
        }
        if (type == 2) {
            length = 3;
            pointer = 0;

            for (int i = 0; i < length; i++)
                constant[i] = range_l[i] + (range_r[i] - range_l[i]) * new Random().nextDouble();
        }
    }

    static boolean iter(int type, boolean b) {
        if (type == 0) {
            while (pointer < length && constant[pointer] > range_r[pointer])
                pointer++;
            if (pointer == length)
                return false;

            constant[pointer] += accuracy[pointer];

            System.arraycopy(range_l, 0, constant, 0, pointer);

            pointer = 0;
            return true;
        }
        if (type == 1) {
            if (!b) {
                sign[pointer] = !sign[pointer];
                constant[pointer] += (sign[pointer] ? 1 : -1) * accuracy[pointer];
            }
            pointer = (pointer + 1) % length;
            constant[pointer] += (sign[pointer] ? 1 : -1) * accuracy[pointer];
            return true;
        }
        return true;
    }
}
