import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class GravitySimulator extends JPanel {

    public static final double G = 0.5;
    private ArrayList<Body> bodies = new ArrayList<>();
    private ArrayList<Body> suns = new ArrayList<>();

    private Point mouseStart = null;
    private Point mouseCurrent = null;
    private boolean isDrag = false;

    public GravitySimulator() {
        title();
    }

    public void title(){
        removeAll();
        JLabel title = new JLabel(new ImageIcon("title.png"));
        title.setBounds(0,0,1200,800);
        JButton button = new JButton("Kepler's Laws");
        button.setBounds(700,300,150,50);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.DARK_GRAY);
        button.setFocusPainted(false);
        title.add(button);
        add(title);

        button.addActionListener(l->keplerMotion());
    }

    public void keplerMotion() {
        removeAll();
        bodies = new ArrayList<>();
        suns = new ArrayList<>();
        setBackground(Color.BLACK);

        JButton button = new JButton("Home");
        button.setBounds(30,30,125,30);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.DARK_GRAY);
        button.setFocusPainted(false);
        add(button);

        // The sun
        bodies.add(new Body(600, 400, 0, 0, 20000, 60, true));
        suns.add(bodies.get(0));

        // The mouse logic
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseStart = e.getPoint();
                mouseCurrent = e.getPoint();
                isDrag = !SwingUtilities.isRightMouseButton(e);
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

                    bodies.add(new Body(mouseStart.x, mouseStart.y, launchVx, launchVy, 10, 12, false));

                    repaint();
                }
                if(SwingUtilities.isRightMouseButton(e)){
                    bodies.add(new Body(e.getX(), e.getY(), 0, 0, 20000, 60, true));
                    suns.add(bodies.getLast());
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
        button.addActionListener(l-> {
            timer.stop();
            title();
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Body body : bodies) {
            g2.setColor(new Color(body.color.getRed(), body.color.getGreen(), body.color.getBlue(), 75));
            for (Point p : body.trail) {
                g2.fillOval(p.x -2, p.y -2, 4, 4);
            }
        }

        for (Body body : bodies) {
            g2.setColor(body.color);
            g2.fillOval(
                    (int) (body.x - body.radius / 2),
                    (int) (body.y - body.radius / 2),
                    (int) body.radius,
                    (int) body.radius
            );
        }

        if (isDrag && mouseStart != null && mouseCurrent != null) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(mouseStart.x, mouseStart.y, mouseCurrent.x, mouseCurrent.y);
            g2.fillOval(mouseStart.x - 4, mouseStart.y - 4, 8, 8);
        }
    }

    private void updatePhysics(double dt) {
        if (bodies.isEmpty()) return;

        for (int i = 0; i < bodies.size(); i++) {
            Body a = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                Body b = bodies.get(j);

                double dx = b.x - a.x;
                double dy = b.y - a.y;
                double distSq = dx * dx + dy * dy;
                double dist = Math.sqrt(distSq);

                if (dist < 1) continue;

                double force = G * a.mass * b.mass / distSq;
                double ax = (dx / dist) * (force / a.mass);
                double ay = (dy / dist) * (force / a.mass);
                double bx = (-dx / dist) * (force / b.mass);
                double by = (-dy / dist) * (force / b.mass);

                if (!a.isStar) {
                    a.vx += ax * dt;
                    a.vy += ay * dt;
                }
                if (!b.isStar) {
                    b.vx += bx * dt;
                    b.vy += by * dt;
                }
            }
        }

        for (Body b : bodies) {
            if (!b.isStar) {
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

        ArrayList<Body> toRemove = new ArrayList<>();
        for (Body b : bodies) {
            if (b.isStar) continue;
            for (Body sun : suns) {
                double dx = sun.x - b.x;
                double dy = sun.y - b.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < (sun.radius / 2 + b.radius / 2)) {
                    toRemove.add(b);
                    break;
                }
            }
        }
        bodies.removeAll(toRemove);
    }

    static class Body {
        double x, y;
        double vx, vy;
        double mass;
        double radius;
        Color color;
        boolean isStar;

        LinkedList<Point> trail = new LinkedList<>();
        int trailCount = 0;

        public Body(double x, double y, double vx, double vy, double mass, double radius, boolean isStar) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.mass = mass;
            this.radius = radius;
            this.isStar = isStar;
            this.color = new Color((int) (Math.random()*255), (int) (Math.random()*255), (int) (Math.random()*255));
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