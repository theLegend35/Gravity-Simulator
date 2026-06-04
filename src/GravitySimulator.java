import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class GravitySimulator extends JPanel {

    public static final double G = 0.5;
    private final ArrayList<Body> bodies = new ArrayList<>();

    private Point mouseStart = null;
    private Point mouseCurrent = null;
    private boolean isDrag = false;


    public GravitySimulator() {
        setBackground(Color.BLACK);

        // The sun
        bodies.add(new Body(600, 400, 0, 0, 20000, 60, Color.YELLOW));

        // The mouse logic
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseStart = e.getPoint();
                mouseCurrent = e.getPoint();
                isDrag = true;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrag) {
                    mouseCurrent = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrag) {
                    isDrag = false;

                    double dx = mouseStart.x - e.getX();
                    double dy = mouseStart.y - e.getY();

                    double launchVx = dx * 0.05;
                    double launchVy = dy * 0.05;

                    bodies.add(new Body(mouseStart.x, mouseStart.y, launchVx, launchVy, 10, 12, Color.CYAN));

                    repaint();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        // Timer to update graphics
        Timer timer = new Timer(16, e -> {
            for (int k = 0; k < 10; k++) {
                updatePhysics(0.1);
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draws the trails
        for (Body body : bodies) {
            g2.setColor(new Color(body.color.getRed(), body.color.getGreen(), body.color.getBlue(), 75)); // Faded trail
            for (Point p : body.trail) {
                g2.fillOval(p.x -2, p.y -2, 4, 4);
            }
        }

        // Draw the planets and sun
        for (Body body : bodies) {
            g2.setColor(body.color);
            g2.fillOval(
                    (int) (body.x - body.radius / 2),
                    (int) (body.y - body.radius / 2),
                    (int) body.radius,
                    (int) body.radius
            );
        }

        // Draws the slingshot line
        if (isDrag && mouseStart != null && mouseCurrent != null) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(mouseStart.x, mouseStart.y, mouseCurrent.x, mouseCurrent.y);
            g2.fillOval(mouseStart.x - 4, mouseStart.y - 4, 8, 8);
        }
    }

    private void updatePhysics(double dt) {
        if (bodies.isEmpty()) return;
        Body sun = bodies.get(0);

        for (int i = bodies.size() - 1; i >= 1; i--) {
            Body a = bodies.get(i);

            double dx = sun.x - a.x;
            double dy = sun.y - a.y;
            double distSq = dx * dx + dy * dy;
            double dist = Math.sqrt(distSq);

            if (dist < (sun.radius / 2 + a.radius / 2)) {
                bodies.remove(i);
                continue;
            }

            double force = G * a.mass * sun.mass / distSq;
            double ratx = dx / dist;
            double raty = dy / dist;

            double ax = ratx * (force / a.mass);
            double ay = raty * (force / a.mass);

            a.vx += ax * dt;
            a.vy += ay * dt;
        }

        for (int i = 1; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            b.x += b.vx * dt;
            b.y += b.vy * dt;

            b.trailCount++;
            if (b.trailCount >= 5) {
                b.trail.add(new Point((int) b.x, (int) b.y));
                if (b.trail.size() > 40) {
                    b.trail.removeFirst();
                }
                b.trailCount = 0;
            }
        }
    }

    static class Body {
        double x, y;
        double vx, vy;
        double mass;
        double radius;
        Color color;

        LinkedList<Point> trail = new LinkedList<>();
        int trailCount = 0;

        public Body(double x, double y, double vx, double vy, double mass, double radius, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.mass = mass;
            this.radius = radius;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Gravity Simulator");
        GravitySimulator panel = new GravitySimulator();

        frame.add(panel);
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}