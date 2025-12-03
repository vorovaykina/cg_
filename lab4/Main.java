import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private ClippingPanel panel;

    public Main() {
        setTitle("Алгоритмы отсечения отрезков и многоугольников");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);

        panel = new ClippingPanel();
        add(panel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton loadButton = new JButton("Загрузить файл");
        JButton manualInputButton = new JButton("Ввести данные вручную");
        JButton clipLinesButton = new JButton("Отсечь отрезки");
        JButton clipPolygonButton = new JButton("Отсечь многоугольник");
        JButton clearButton = new JButton("Очистить");

        loadButton.addActionListener(e -> loadFromFile());
        manualInputButton.addActionListener(e -> panel.loadFromTextDialog());
        clipLinesButton.addActionListener(e -> panel.clipLines());
        clipPolygonButton.addActionListener(e -> panel.clipPolygon());
        clearButton.addActionListener(e -> panel.clear());

        controlPanel.add(loadButton);
        controlPanel.add(manualInputButton);
        controlPanel.add(clipLinesButton);
        controlPanel.add(clipPolygonButton);
        controlPanel.add(clearButton);

        add(controlPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                panel.loadData(fileChooser.getSelectedFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки файла: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}

class ClippingPanel extends JPanel {
    private List<Line> lines = new ArrayList<>();
    private List<Line> clippedLines = new ArrayList<>();
    private List<Point2D> polygon = new ArrayList<>();
    private List<Point2D> clippedPolygon = new ArrayList<>();
    private Rectangle2D clipWindow;
    private boolean showClipped = false;
    private boolean showClippedPolygon = false;

    private double minX, maxX, minY, maxY;
    private int padding = 50;

    public ClippingPanel() {
        setBackground(Color.WHITE);
    }

    public void loadData(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder textBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            textBuilder.append(line).append("\n");
        }
        reader.close();
        loadDataFromText(textBuilder.toString());
    }

    public void loadFromTextDialog() {
        JTextArea textArea = new JTextArea(20, 40);
        textArea.setText("3\n1 1 4 4\n2 0 2 5\n0 2 5 2\n1 1 3 3");
        JScrollPane scrollPane = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Введите данные (пример загружен)",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                loadDataFromText(textArea.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка чтения данных: " + ex.getMessage());
            }
        }
    }

    public void loadDataFromText(String text) throws IOException {
        lines.clear();
        polygon.clear();
        clippedLines.clear();
        clippedPolygon.clear();
        showClipped = false;
        showClippedPolygon = false;

        BufferedReader reader = new BufferedReader(new StringReader(text));

        String line = reader.readLine();
        if (line == null) throw new IOException("Нет данных.");
        int n = Integer.parseInt(line.trim());

        for (int i = 0; i < n; i++) {
            line = reader.readLine();
            if (line == null) throw new IOException("Недостаточно строк для отрезков.");
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 4) throw new IOException("Неверный формат строки.");
            double x1 = Double.parseDouble(parts[0]);
            double y1 = Double.parseDouble(parts[1]);
            double x2 = Double.parseDouble(parts[2]);
            double y2 = Double.parseDouble(parts[3]);
            lines.add(new Line(x1, y1, x2, y2));
        }

        line = reader.readLine();
        if (line == null) throw new IOException("Нет строки окна отсечения.");
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 4) throw new IOException("Неверный формат окна отсечения.");
        double xmin = Double.parseDouble(parts[0]);
        double ymin = Double.parseDouble(parts[1]);
        double xmax = Double.parseDouble(parts[2]);
        double ymax = Double.parseDouble(parts[3]);

        if (xmin > xmax) { double temp = xmin; xmin = xmax; xmax = temp; }
        if (ymin > ymax) { double temp = ymin; ymin = ymax; ymax = temp; }

        clipWindow = new Rectangle2D(xmin, ymin, xmax, ymax);

        calculateBounds();
        repaint();
    }

    private void calculateBounds() {
        if (clipWindow == null) return;

        minX = clipWindow.xmin;
        maxX = clipWindow.xmax;
        minY = clipWindow.ymin;
        maxY = clipWindow.ymax;

        for (Line l : lines) {
            minX = Math.min(minX, Math.min(l.x1, l.x2));
            maxX = Math.max(maxX, Math.max(l.x1, l.x2));
            minY = Math.min(minY, Math.min(l.y1, l.y2));
            maxY = Math.max(maxY, Math.max(l.y1, l.y2));
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        if (rangeX == 0) rangeX = 1;
        if (rangeY == 0) rangeY = 1;

        minX -= rangeX * 0.1;
        maxX += rangeX * 0.1;
        minY -= rangeY * 0.1;
        maxY += rangeY * 0.1;
    }

    public void clipLines() {
        if (clipWindow == null) return;

        clippedLines.clear();
        MidpointClipping clipper = new MidpointClipping(clipWindow);

        for (Line l : lines) {
            Line clipped = clipper.clip(l);
            if (clipped != null) {
                clippedLines.add(clipped);
            }
        }

        showClipped = true;
        showClippedPolygon = false;
        repaint();
    }

    public void clipPolygon() {
        if (lines.isEmpty() || clipWindow == null) return;

        polygon.clear();
        if (isClosedPolygon()) {
            extractPolygonFromLines();
        } else {
            for (Line l : lines) {
                polygon.add(new Point2D(l.x1, l.y1));
            }
            if (!polygon.isEmpty()) {
                polygon.add(new Point2D(polygon.get(0).x, polygon.get(0).y));
            }
        }

        SutherlandHodgmanClipping clipper = new SutherlandHodgmanClipping(clipWindow);
        clippedPolygon = clipper.clip(polygon);
        showClippedPolygon = true;
        showClipped = false;
        repaint();
    }

    private boolean isClosedPolygon() {
        if (lines.size() < 3) return false;

        for (int i = 0; i < lines.size(); i++) {
            Line current = lines.get(i);
            Line next = lines.get((i + 1) % lines.size());

            if (Math.abs(current.x2 - next.x1) > 0.001 ||
                    Math.abs(current.y2 - next.y1) > 0.001) {
                return false;
            }
        }
        return true;
    }

    private void extractPolygonFromLines() {
        polygon.clear();
        if (lines.isEmpty()) return;

        polygon.add(new Point2D(lines.get(0).x1, lines.get(0).y1));

        for (Line line : lines) {
            polygon.add(new Point2D(line.x2, line.y2));
        }
    }

    public void clear() {
        lines.clear();
        clippedLines.clear();
        polygon.clear();
        clippedPolygon.clear();
        clipWindow = null;
        showClipped = false;
        showClippedPolygon = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (clipWindow == null) return;

        drawAxes(g2d);
        drawClipWindow(g2d);

        // ВСЕГДА рисуем исходные синие линии
        drawOriginalLines(g2d);

        if (showClippedPolygon) {
            // Для многоугольника: рисуем отсечённую часть
            drawClippedPolygon(g2d);
        } else if (showClipped) {
            // Для отрезков: рисуем зелёные отсечённые части ПОВЕРХ синих
            drawClippedLines(g2d);
        }
    }

    private void drawAxes(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));

        int w = getWidth() - 2 * padding;
        int h = getHeight() - 2 * padding;

        g2d.drawLine(padding, padding, padding, padding + h);
        g2d.drawLine(padding, padding + h, padding + w, padding + h);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        for (int i = 0; i <= 10; i++) {
            double x = minX + (maxX - minX) * i / 10.0;
            int px = padding + (int)(w * i / 10.0);
            g2d.drawString(String.format("%.1f", x), px - 15, padding + h + 15);

            double y = minY + (maxY - minY) * i / 10.0;
            int py = padding + h - (int)(h * i / 10.0);
            g2d.drawString(String.format("%.1f", y), padding - 35, py + 5);
        }
    }

    private void drawClipWindow(Graphics2D g2d) {
        g2d.setColor(new Color(255, 200, 200, 100));
        g2d.setStroke(new BasicStroke(2));

        int x1 = toScreenX(clipWindow.xmin);
        int y1 = toScreenY(clipWindow.ymax);
        int x2 = toScreenX(clipWindow.xmax);
        int y2 = toScreenY(clipWindow.ymin);

        g2d.fillRect(x1, y1, x2 - x1, y2 - y1);
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawRect(x1, y1, x2 - x1, y2 - y1);
    }

    private void drawOriginalLines(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 255, 150));
        g2d.setStroke(new BasicStroke(2));

        for (Line l : lines) {
            int x1 = toScreenX(l.x1);
            int y1 = toScreenY(l.y1);
            int x2 = toScreenX(l.x2);
            int y2 = toScreenY(l.y2);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawClippedLines(Graphics2D g2d) {
        g2d.setColor(new Color(0, 200, 0));
        g2d.setStroke(new BasicStroke(4)); // Толще синих

        for (Line l : clippedLines) {
            int x1 = toScreenX(l.x1);
            int y1 = toScreenY(l.y1);
            int x2 = toScreenX(l.x2);
            int y2 = toScreenY(l.y2);

            // Рисуем только если отрезок не вырожден в точку
            if (!(Math.abs(l.x1 - l.x2) < 0.001 && Math.abs(l.y1 - l.y2) < 0.001)) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private void drawClippedPolygon(Graphics2D g2d) {
        if (clippedPolygon.size() < 2) return;

        int[] xPoints = new int[clippedPolygon.size()];
        int[] yPoints = new int[clippedPolygon.size()];

        for (int i = 0; i < clippedPolygon.size(); i++) {
            xPoints[i] = toScreenX(clippedPolygon.get(i).x);
            yPoints[i] = toScreenY(clippedPolygon.get(i).y);
        }

        g2d.setColor(new Color(0, 200, 0, 200));
        g2d.fillPolygon(xPoints, yPoints, clippedPolygon.size());

        g2d.setColor(new Color(0, 150, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawPolygon(xPoints, yPoints, clippedPolygon.size());
    }

    private int toScreenX(double x) {
        int w = getWidth() - 2 * padding;
        return padding + (int)((x - minX) / (maxX - minX) * w);
    }

    private int toScreenY(double y) {
        int h = getHeight() - 2 * padding;
        return padding + h - (int)((y - minY) / (maxY - minY) * h);
    }
}

class Line {
    double x1, y1, x2, y2;
    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
    }
}

class Point2D {
    double x, y;
    public Point2D(double x, double y) {
        this.x = x; this.y = y;
    }
}

class Rectangle2D {
    double xmin, ymin, xmax, ymax;
    public Rectangle2D(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin; this.ymin = ymin; this.xmax = xmax; this.ymax = ymax;
    }
}

class MidpointClipping {
    private Rectangle2D w;

    public MidpointClipping(Rectangle2D window) {
        this.w = window;
    }

    public Line clip(Line L) {
        Line result = clipRec(L.x1, L.y1, L.x2, L.y2);
        // Проверяем, что отрезок не вырожден
        if (result != null && dist(result.x1, result.y1, result.x2, result.y2) < 0.1) {
            // Если очень короткий - проверяем, внутри ли он окна
            if (isInside(result.x1, result.y1) || isInside(result.x2, result.y2)) {
                return result;
            }
            return null;
        }
        return result;
    }

    private Line clipRec(double x1, double y1, double x2, double y2) {
        int c1 = code(x1, y1);
        int c2 = code(x2, y2);

        if ((c1 | c2) == 0) return new Line(x1, y1, x2, y2);
        if ((c1 & c2) != 0) return null;

        if (dist(x1, y1, x2, y2) < 0.5) {
            if (isInside(x1, y1) && isInside(x2, y2))
                return new Line(x1, y1, x2, y2);
            if (isInside(x1, y1))
                return new Line(x1, y1, x1, y1);
            if (isInside(x2, y2))
                return new Line(x2, y2, x2, y2);
            return null;
        }

        double xm = (x1 + x2) / 2;
        double ym = (y1 + y2) / 2;

        Line left = clipRec(x1, y1, xm, ym);
        Line right = clipRec(xm, ym, x2, y2);

        if (left == null) return right;
        if (right == null) return left;

        return new Line(left.x1, left.y1, right.x2, right.y2);
    }

    private boolean isInside(double x, double y) {
        return x >= w.xmin && x <= w.xmax && y >= w.ymin && y <= w.ymax;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    private int code(double x, double y) {
        int code = 0;
        if (x < w.xmin) code |= 1;
        if (x > w.xmax) code |= 2;
        if (y < w.ymin) code |= 4;
        if (y > w.ymax) code |= 8;
        return code;
    }
}

class SutherlandHodgmanClipping {
    private Rectangle2D window;

    public SutherlandHodgmanClipping(Rectangle2D window) {
        this.window = window;
    }

    public List<Point2D> clip(List<Point2D> polygon) {
        if (polygon == null || polygon.size() < 3) {
            return new ArrayList<>();
        }

        List<Point2D> result = new ArrayList<>(polygon);

        result = clipAgainstEdge(result, 0);
        result = clipAgainstEdge(result, 1);
        result = clipAgainstEdge(result, 2);
        result = clipAgainstEdge(result, 3);

        return result;
    }

    private List<Point2D> clipAgainstEdge(List<Point2D> polygon, int edge) {
        List<Point2D> result = new ArrayList<>();

        if (polygon.isEmpty()) {
            return result;
        }

        Point2D prev = polygon.get(polygon.size() - 1);

        for (Point2D curr : polygon) {
            boolean prevInside = isInside(prev, edge);
            boolean currInside = isInside(curr, edge);

            if (currInside) {
                if (!prevInside) {
                    Point2D intersection = getIntersection(prev, curr, edge);
                    if (intersection != null) {
                        result.add(intersection);
                    }
                }
                result.add(curr);
            } else if (prevInside) {
                Point2D intersection = getIntersection(prev, curr, edge);
                if (intersection != null) {
                    result.add(intersection);
                }
            }

            prev = curr;
        }

        return result;
    }

    private boolean isInside(Point2D p, int edge) {
        switch (edge) {
            case 0: return p.x >= window.xmin;
            case 1: return p.x <= window.xmax;
            case 2: return p.y >= window.ymin;
            case 3: return p.y <= window.ymax;
            default: return false;
        }
    }

    private Point2D getIntersection(Point2D p1, Point2D p2, int edge) {
        double x = 0, y = 0;
        double t;

        switch (edge) {
            case 0:
                if (Math.abs(p2.x - p1.x) < 1e-10) return null;
                t = (window.xmin - p1.x) / (p2.x - p1.x);
                y = p1.y + t * (p2.y - p1.y);
                x = window.xmin;
                break;

            case 1:
                if (Math.abs(p2.x - p1.x) < 1e-10) return null;
                t = (window.xmax - p1.x) / (p2.x - p1.x);
                y = p1.y + t * (p2.y - p1.y);
                x = window.xmax;
                break;

            case 2:
                if (Math.abs(p2.y - p1.y) < 1e-10) return null;
                t = (window.ymin - p1.y) / (p2.y - p1.y);
                x = p1.x + t * (p2.x - p1.x);
                y = window.ymin;
                break;

            case 3:
                if (Math.abs(p2.y - p1.y) < 1e-10) return null;
                t = (window.ymax - p1.y) / (p2.y - p1.y);
                x = p1.x + t * (p2.x - p1.x);
                y = window.ymax;
                break;

            default:
                return null;
        }

        return new Point2D(x, y);
    }
}
