package ai.nextbillion.utils.polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * @link https://en.wikipedia.org/wiki/Point_in_polygon
 */
public class PolygonUtils {

    public static List<Point> getPolygon(boolean isClose, List<Point> polygon) {
        if (polygon == null || polygon.isEmpty()) {
            return new ArrayList<>();
        }
        Point p;
        if (isClose) {
            p = polygon.get(polygon.size() - 1);
            polygon.add(p);
            p = polygon.get(0);
            polygon.add(p);
        }
        return polygon;
    }

    public static boolean isPointInPolygon(Point point, List<Point> polygon) {
        if (polygon == null || polygon.isEmpty()) {
            return false;
        }
        boolean isIn = false;
        Point p0;
        Point p1;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            p0 = polygon.get(j);
            p1 = polygon.get(i);
            if (point.equals(p0)) {
                return true;
            }
            if (isIntersectWithLine(point, p0, p1)) {
                isIn = !isIn;
            }
        }
        return isIn;
    }

    private static boolean isIntersectWithLine(Point p, Point p0, Point p1) {
        boolean p0Flag = (p0.getY() - p.getY()) > 0;
        boolean p1Flag = (p1.getY() - p.getY()) > 0;
        if (p0Flag == p1Flag) {
            return false;
        }
        double x = (p0.getX() - p1.getX()) * (p.getY() - p1.getY())
                / (p0.getY() - p1.getY()) + p1.getX();
        return x >= p.getX();
    }
}
