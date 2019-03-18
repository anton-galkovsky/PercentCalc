package comp;

import javafx.geometry.Point2D;
import java.util.ArrayList;

public class Graph {

    ArrayList<Point2D> points;
    String name;

    Graph(ArrayList<Point2D> points, String name) {
        this.points = points;
        this.name = name;
    }
}
