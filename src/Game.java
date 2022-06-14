import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JFrame;

public class Game extends Component implements MouseListener, MouseMotionListener {

    JFrame jFrame;
    private int mVertici;
    private Point[] pList;
    private int tmpIndex = -1;
    private int mLivello = 10;
    private double mTolleranza;
    private boolean showDiag, showPs, showSides;
    private long diagIntrsc = 0;
    private int nDiag = 0;

    private class Segment {
        public int p1, p2;
    }

    private Segment[] lato, diagonale;

    private Game() {
    };

    public Game(int nOfVertices, int lineWidth, double tollerance, String title, boolean showDiagonals, boolean showVertices,
            boolean showEdges) {
        showDiag = showDiagonals;
        showPs = showVertices;
        showSides = showEdges;
        mLivello = lineWidth;
        mTolleranza = tollerance;
        mVertici = nOfVertices;
        lato = new Segment[(mVertici > 2) ? mVertici : 1];
        diagonale = new Segment[nDiag = (mVertici > 3) ? (mVertici * (mVertici - 3) / 2) : 0];
        diagIntrsc = mVertici * (mVertici - 1) * (mVertici - 2) * (mVertici - 3) / 24;

        for (int i = 0; i < lato.length; i++) {
            lato[i] = new Segment();
            lato[i].p1 = i;
            lato[i].p2 = (i + 1) % mVertici;
        }

        pList = new Point[mVertici];

        int nd = 0;
        for (int i = 2; i < mVertici - 1; i++) {
            for (int j = 0; j < mVertici; j++) {
                diagonale[nd] = new Segment();
                diagonale[nd].p1 = j;
                diagonale[nd].p2 = (j + i) % mVertici;
                if (++nd == nDiag)
                    break;
            }
            if (nd == nDiag)
                break;
        }

        for (int i = 0; i < mVertici; i++) {
            pList[i] = new Point();
            pList[i].setLocation(lineWidth / 2 + Math.random() * 1000, lineWidth / 2 + Math.random() * 1000);
        }

        jFrame = new JFrame(title);
        jFrame.add(this);
        jFrame.getContentPane().setBackground(new Color(150, 150, 150));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(1000 + lineWidth * 2, 1000 + lineWidth * 2);
        jFrame.setVisible(true);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    private boolean onSegment(Point p, Point q, Point r) {
        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) && q.y <= Math.max(p.y, r.y)
                && q.y >= Math.min(p.y, r.y))
            return true;
        return false;
    }

    private int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0)
            return 0;
        return (val > 0) ? 1 : 2;
    }

    private boolean doIntersect(Point p1, Point q1, Point p2, Point q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        if (o1 != o2 && o3 != o4)
            return true;
        if (o1 == 0 && onSegment(p1, p2, q1))
            return true;
        if (o2 == 0 && onSegment(p1, q2, q1))
            return true;
        if (o3 == 0 && onSegment(p2, p1, q2))
            return true;
        if (o4 == 0 && onSegment(p2, q1, q2))
            return true;
        return false;
    }

    private double polygonArea() {
        double area = 0.0;
        int j = mVertici - 1;
        for (int i = 0; i < mVertici; i++) {
            area += (pList[j].x + pList[i].x) * (pList[j].y - pList[i].y);
            j = i;
        }
        return Math.abs(area / 2.0);
    }

    private double dot(Point p1, Point p2, Point p3) {
        int x1 = p1.x - p2.x;
        int y1 = p1.y - p2.y;
        int x2 = p3.x - p2.x;
        int y2 = p3.y - p2.y;

        return Math.acos((x1 * x2 + y1 * y2) / ((Math.sqrt(x1 * x1 + y1 * y1) * Math.sqrt(x2 * x2 + y2 * y2))));
    }

    private double segLength(Point p1, Point p2) {
        int x = p1.x - p2.x;
        int y = p1.y - p2.y;
        return Math.sqrt(x * x + y * y);
    }

    private boolean doubleCompare(double a, double b, double tollerance) {
        return Math.abs(a - b) < (tollerance * (a + b) / 100);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        tmpIndex = -1;
        int sideIntrsc = 0, tmpDiaIntrsc = 0;

        System.out.println("##########################");

        if (mVertici > 3) {
            for (Segment seg1 : lato)
                for (Segment seg2 : lato)
                    if (seg1.p1 != seg2.p2 && seg1.p2 != seg2.p1 && seg1.p1 != seg2.p1 && seg1.p2 != seg2.p2)
                        if (doIntersect(pList[seg1.p1], pList[seg1.p2], pList[seg2.p1], pList[seg2.p2]))
                            sideIntrsc++;

            for (Segment seg1 : diagonale)
                for (Segment seg2 : diagonale)
                    if (seg1.p1 != seg2.p2 && seg1.p2 != seg2.p1 && seg1.p1 != seg2.p1 && seg1.p2 != seg2.p2)
                        if (doIntersect(pList[seg1.p1], pList[seg1.p2], pList[seg2.p1], pList[seg2.p2]))
                            tmpDiaIntrsc++;

            if (sideIntrsc != 0)
                System.out.println("Intersections between sides: " + (sideIntrsc / 2) + ", what is that?!");

            System.out.println("Intersections between diagonals: " + (tmpDiaIntrsc / 2)
                    + ((tmpDiaIntrsc / 2 != diagIntrsc) ? (" instead of " + diagIntrsc + ", it's concave!") : " it's convex!"));
        }
        if (mVertici > 2) {
            if (sideIntrsc == 0)
                System.out.println("Polygon area: " + polygonArea() + "px");

            double perimetro = 0;
            System.out.print("Polygon perimeter: ");
            for (int i = 0; i < mVertici; i++)
                perimetro += segLength(pList[i], pList[(i + 1) % mVertici]);
            System.out.println(perimetro + "px");

            for (int i = 0; i < mVertici; i++)
                System.out.println("Angle at vertex " + i + ": "
                        + dot(pList[(mVertici + i - 1) % mVertici], pList[i], pList[(i + 1) % mVertici]) * 180
                                / Math.PI);

            for (int i = 0; i < mVertici; i++)
                System.out
                        .println("Length side " + i + ": " + segLength(pList[i], pList[(i + 1) % mVertici]) + "px");
        } else if (mVertici == 2)
            System.out.println("Line length: " + segLength(pList[0], pList[1]) + "px");

        if (mVertici == 4 && sideIntrsc == 0) {
            double l1 = segLength(pList[0], pList[1]);
            double l2 = segLength(pList[1], pList[2]);
            double l3 = segLength(pList[2], pList[3]);
            double l4 = segLength(pList[3], pList[0]);
            double a1 = dot(pList[3], pList[0], pList[1]) * 180 / Math.PI;
            double a2 = dot(pList[0], pList[1], pList[2]) * 180 / Math.PI;
            double a3 = dot(pList[1], pList[2], pList[3]) * 180 / Math.PI;
            double a4 = dot(pList[2], pList[3], pList[0]) * 180 / Math.PI;

            if (doubleCompare(l1, l2, mTolleranza) && doubleCompare(l2, l3, mTolleranza)
                    && doubleCompare(l3, l4, mTolleranza)) {
                if (!doubleCompare(a1, a2, mTolleranza))
                    System.out.println("Looks like a diamond!");
                else
                    System.out.println("Looks like a square!");
            } else if (doubleCompare(l1, l3, mTolleranza) && doubleCompare(l2, l4, mTolleranza)) {
                if (!doubleCompare(a1, a2, mTolleranza))
                    System.out.println("Looks like a parallelogram!");
                else
                    System.out.println("Looks like a rectangle!");
            } else {
                Point tmpP;
                double tmpA1;
                double tmpA2;
                boolean si = false;

                tmpP = new Point(pList[0]);
                tmpP.translate(0, 1);
                tmpA1 = dot(tmpP, pList[0], pList[1]) * 180 / Math.PI;
                tmpP = new Point(pList[3]);
                tmpP.translate(0, 1);
                tmpA2 = dot(tmpP, pList[3], pList[2]) * 180 / Math.PI;

                if (doubleCompare(tmpA1, tmpA2, mTolleranza)) {
                    System.out.println("Looks like a trapezoid!");
                    si = true;
                }

                tmpP = new Point(pList[1]);
                tmpP.translate(0, 1);
                tmpA1 = dot(tmpP, pList[1], pList[2]) * 180 / Math.PI;
                tmpP = new Point(pList[0]);
                tmpP.translate(0, 1);
                tmpA2 = dot(tmpP, pList[0], pList[3]) * 180 / Math.PI;

                if (!si)
                    if (doubleCompare(tmpA1, tmpA2, mTolleranza)) {
                        System.out.println("Looks like a trapezoid!");
                        si = true;
                    }

                if (si)
                    if (doubleCompare(a1, 90, mTolleranza) || doubleCompare(a2, 90, mTolleranza)
                            || doubleCompare(a3, 90, mTolleranza)) {
                        System.out.println("Looks rectangular!");
                        System.out.println("Looks scalene!");
                    } else if (doubleCompare(a1, a2, mTolleranza) || doubleCompare(a2, a3, mTolleranza)
                            || doubleCompare(a3, a4, mTolleranza) || doubleCompare(a4, a1, mTolleranza))
                        System.out.println("Looks isosceles!");
                    else
                        System.out.println("Looks scalene!");
            }

        } else if (mVertici == 3) {
            double l1 = segLength(pList[0], pList[1]);
            double l2 = segLength(pList[1], pList[2]);
            double l3 = segLength(pList[2], pList[0]);
            double a1 = dot(pList[2], pList[0], pList[1]) * 180 / Math.PI;
            double a2 = dot(pList[0], pList[1], pList[2]) * 180 / Math.PI;
            double a3 = dot(pList[1], pList[2], pList[0]) * 180 / Math.PI;

            if (doubleCompare(l1, l2, mTolleranza) && doubleCompare(l2, l3, mTolleranza)) {
                System.out.println("Looks equilateral!");
            } else {
                if (doubleCompare(a1, 90, mTolleranza) || doubleCompare(a2, 90, mTolleranza)
                        || doubleCompare(a3, 90, mTolleranza))
                    System.out.println("Looks like a right triangle!");
                if (doubleCompare(l1, l2, mTolleranza) || doubleCompare(l2, l3, mTolleranza)
                        || doubleCompare(l3, l1, mTolleranza))
                    System.out.println("Looks isosceles!");
                else
                    System.out.println("Looks scalene!");
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON3)
            jFrame.dispose();
        Point p = event.getPoint();

        for (int i = 0; i < mVertici; i++) {
            double distance = Point.distance(pList[i].getX(), pList[i].getY(), p.getX(), p.getY());

            if (tmpIndex == -1 && distance < mLivello / 2)
                tmpIndex = i;
            else if (distance < mLivello / 2)
                if (Point.distance(pList[tmpIndex].getX(), pList[tmpIndex].getY(), p.getX(), p.getY()) > distance)
                    tmpIndex = i;
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (tmpIndex != -1)
            pList[tmpIndex] = event.getPoint();
        repaint();
    }

    @Override
    public void paint(Graphics graphic) {
        Graphics2D g2d = (Graphics2D) graphic;

        if (showPs)
            for (Point point : pList)
                graphic.fillOval(point.x - mLivello / 2, point.y - mLivello / 2, mLivello, mLivello);
        if (mVertici > 1) {
            if (showSides) {
                g2d.setStroke(new BasicStroke(mLivello / 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (Segment seg : lato)
                    graphic.drawLine(pList[seg.p1].x, pList[seg.p1].y, pList[seg.p2].x, pList[seg.p2].y);
            }
            if (showDiag) {
                g2d.setStroke(new BasicStroke(mLivello / 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(new Color(200, 0, 0));
                for (Segment seg : diagonale)
                    graphic.drawLine(pList[seg.p1].x, pList[seg.p1].y, pList[seg.p2].x, pList[seg.p2].y);
            }

            g2d.setStroke(new BasicStroke(mLivello / 8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(0, 0, 255));
            graphic.drawLine(pList[0].x, pList[0].y, (pList[1].x - pList[0].x) / 4 + pList[0].x,
                    (pList[1].y - pList[0].y) / 4 + pList[0].y);
        }
    }
}