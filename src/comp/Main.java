package comp;

public class Main {

    private static Record[] records = new Record[]{

// before
            new Record("new_i/19_03_03_17_20_10.txt", 30),
            new Record("new_i/19_03_07_13_25_29.txt", 30),
            new Record("new_a/19_03_03_17_59_49.txt", 30),
            new Record("new_a/19_03_07_13_24_30.txt", 30),
            new Record("new_m/19_03_08_11_52_43.txt", 30),
            new Record("new_n/19_03_07_13_25_30.txt", 30),
            new Record("new_v/19_03_07.txt",          30),

// during
            new Record("new_a/19_03_03_20_08_48.txt", 15),
            new Record("new_m/19_03_03_20.txt",       15),
            new Record("new_n/19_03_03_20_08_37.txt", 15),
            new Record("new_v/19_03_03_20.txt",       15),

// after
            new Record("new_i/19_03_03_22_37_13.txt", 80),
            new Record("new_i/19_03_03_23_30_58.txt", 80),
            new Record("new_a/19_03_03_22_36_44.txt", 80),
            new Record("new_m/19_03_03_22.txt",       80),
            new Record("new_n/19_03_03_22_36_48.txt", 80),
            new Record("new_v/19_03_03_22.txt",       80),

    };


    public static void main(String[] args) throws Exception {
        int type = 1;
        new Constants(type);
        new Producer();

        new Hunter(records, type, false);
    }
}
