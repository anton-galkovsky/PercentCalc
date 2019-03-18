package comp;

import java.util.ArrayList;

class Hunter {

    static ArrayList<double[]> results;

    Hunter(Record[] records, int type, boolean loop) throws Exception {
        if (!loop) {
            results = new ArrayList<>();
            Producer.produce(records);
        }
        else {
            double min = Double.MAX_VALUE;
            boolean effective = true;
            boolean correct;

            int iters = 0;
            double error;
            while (Constants.iter(type, effective)) {
                results = new ArrayList<>();
                iters++;
                error = 0;
                effective = false;
                correct = true;

                Producer.produce(records);

//                for (Record record : records) {
//                    AccPoint ap = Producer.produce(record.file);
//                    if (ap != null)
//                        error = Math.max(error, Math.abs(ap.fun - record.verdict));
////                    error += Math.abs(ap.fun - record.verdict);
//                    else
//                        correct = false;
//                    System.out.printf("%9.2f", ap != null ? ap.fun : -1);
//                }
//                System.out.printf("            %10.5f         ", correct ? error : -1);
//
//                for (double c : Constants.constant)
//                    System.out.printf("%10.4f", c);
//
//                if (correct && error < min) {
//                    effective = true;
//                    min = error;
//                    System.out.print("        ++");
//                }
//                System.out.println();
//
//
//                if (effective)
//                    iters = 0;
//
//                if (iters > Constants.length * 3)
//                    return;
//                System.out.println();
            }
        }
    }
}
