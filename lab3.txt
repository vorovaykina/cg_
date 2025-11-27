import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private DrawingPanel drawingPanel;
    private JComboBox<String> algorithmSelector;
    private JSpinner x1Spinner, y1Spinner, x2Spinner, y2Spinner, radiusSpinner;
    private JLabel timeLabel;
    private JButton drawButton, clearButton;

    public Main() {
        setTitle("Растровые алгоритмы - Лабораторная работа 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Панель управления
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Параметры"));

        // Выбор алгоритма
        controlPanel.add(new JLabel("Алгоритм:"));
        String[] algorithms = {
                "Пошаговый алгоритм",
                "Алгоритм ЦДА (DDA)",
                "Алгоритм Брезенхема (отрезок)",
                "Алгоритм Брезенхема (окружность)"
        };
        algorithmSelector = new JComboBox<>(algorithms);
        algorithmSelector.addActionListener(e -> updateControlsVisibility());
        controlPanel.add(algorithmSelector);

        // Координаты точек
        controlPanel.add(new JLabel("X1:"));
        x1Spinner = new JSpinner(new SpinnerNumberModel(5, -50, 50, 1));
        controlPanel.add(x1Spinner);

        controlPanel.add(new JLabel("Y1:"));
        y1Spinner = new JSpinner(new SpinnerNumberModel(5, -50, 50, 1));
        controlPanel.add(y1Spinner);

        controlPanel.add(new JLabel("X2:"));
        x2Spinner = new JSpinner(new SpinnerNumberModel(20, -50, 50, 1));
        controlPanel.add(x2Spinner);

        controlPanel.add(new JLabel("Y2:"));
        y2Spinner = new JSpinner(new SpinnerNumberModel(15, -50, 50, 1));
        controlPanel.add(y2Spinner);

        controlPanel.add(new JLabel("Радиус:"));
        radiusSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 40, 1));
        radiusSpinner.setVisible(false);
        controlPanel.add(radiusSpinner);

        // Кнопки
        drawButton = new JButton("Нарисовать");
        drawButton.addActionListener(e -> drawAlgorithm());
        controlPanel.add(drawButton);

        clearButton = new JButton("Очистить");
        clearButton.addActionListener(e -> clearDrawing());
        controlPanel.add(clearButton);

        // Метка времени
        timeLabel = new JLabel("Время выполнения: - ");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        controlPanel.add(timeLabel);

        add(controlPanel, BorderLayout.NORTH);

        // Панель рисования
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        // Информационная панель
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));

        JTextArea infoArea = new JTextArea(5, 20);
        infoArea.setEditable(false);
        infoArea.setText(
                "Инструкция:\n" +
                        "1. Выберите алгоритм из списка\n" +
                        "2. Укажите координаты точек (для отрезков) или радиус (для окружности)\n" +
                        "3. Нажмите 'Нарисовать'\n" +
                        "4. Целочисленные координаты соответствуют центрам пикселей на сетке"
        );
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        add(infoPanel, BorderLayout.SOUTH);
    }

    private void updateControlsVisibility() {
        boolean isCircle = algorithmSelector.getSelectedIndex() == 3;
        x2Spinner.setVisible(!isCircle);
        y2Spinner.setVisible(!isCircle);
        radiusSpinner.setVisible(isCircle);

        // Обновляем метки
        Component[] components = ((JPanel)algorithmSelector.getParent()).getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JLabel) {
                JLabel label = (JLabel) components[i];
                if (label.getText().equals("X2:") || label.getText().equals("Y2:")) {
                    label.setVisible(!isCircle);
                } else if (label.getText().equals("Радиус:")) {
                    label.setVisible(isCircle);
                }
            }
        }
    }

    private void drawAlgorithm() {
        int algorithmIndex = algorithmSelector.getSelectedIndex();
        int x1 = (Integer) x1Spinner.getValue();
        int y1 = (Integer) y1Spinner.getValue();

        long startTime = System.nanoTime();
        List<Point> pixels = new ArrayList<>();
        StringBuilder calculations = new StringBuilder();

        if (algorithmIndex == 3) {
            // Окружность
            int radius = (Integer) radiusSpinner.getValue();
            pixels = RasterAlgorithms.bresenhamCircle(x1, y1, radius);
            calculations = RasterAlgorithms.getCircleCalculations(x1, y1, radius);
        } else {
            // Отрезки
            int x2 = (Integer) x2Spinner.getValue();
            int y2 = (Integer) y2Spinner.getValue();

            switch (algorithmIndex) {
                case 0:
                    pixels = RasterAlgorithms.stepByStep(x1, y1, x2, y2);
                    calculations = RasterAlgorithms.getStepByStepCalculations(x1, y1, x2, y2);
                    break;
                case 1:
                    pixels = RasterAlgorithms.dda(x1, y1, x2, y2);
                    calculations = RasterAlgorithms.getDDACalculations(x1, y1, x2, y2);
                    break;
                case 2:
                    pixels = RasterAlgorithms.bresenhamLine(x1, y1, x2, y2);
                    calculations = RasterAlgorithms.getBresenhamLineCalculations(x1, y1, x2, y2);
                    break;
            }
        }

        long endTime = System.nanoTime();
        double elapsedTime = (endTime - startTime) / 1000000.0;

        drawingPanel.setPixels(pixels);
        timeLabel.setText(String.format("Время выполнения: %.4f мс | Пикселей: %d", elapsedTime, pixels.size()));

        // Показываем окно с вычислениями
        showCalculationsWindow(calculations.toString());
    }

    private void showCalculationsWindow(String calculations) {
        JDialog dialog = new JDialog(this, "Пошаговые вычисления", false);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea(calculations);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        dialog.add(scrollPane);

        dialog.setVisible(true);
    }

    private void clearDrawing() {
        drawingPanel.clearPixels();
        timeLabel.setText("Время выполнения: - ");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}

class RasterAlgorithms {

    // Пошаговый алгоритм
    public static List<Point> stepByStep(int x1, int y1, int x2, int y2) {
        List<Point> pixels = new ArrayList<>();

        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        double xIncrement = (double) dx / steps;
        double yIncrement = (double) dy / steps;

        double x = x1;
        double y = y1;

        for (int i = 0; i <= steps; i++) {
            pixels.add(new Point((int) Math.round(x), (int) Math.round(y)));
            x += xIncrement;
            y += yIncrement;
        }

        return pixels;
    }

    public static StringBuilder getStepByStepCalculations(int x1, int y1, int x2, int y2) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("  ПОШАГОВЫЙ АЛГОРИТМ (Step-by-Step)\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        sb.append(String.format("Начальная точка: (%d, %d)\n", x1, y1));
        sb.append(String.format("Конечная точка: (%d, %d)\n\n", x2, y2));

        int dx = x2 - x1;
        int dy = y2 - y1;
        sb.append(String.format("dx = x2 - x1 = %d - %d = %d\n", x2, x1, dx));
        sb.append(String.format("dy = y2 - y1 = %d - %d = %d\n\n", y2, y1, dy));

        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        sb.append(String.format("steps = max(|dx|, |dy|) = max(%d, %d) = %d\n\n", Math.abs(dx), Math.abs(dy), steps));

        double xIncrement = (double) dx / steps;
        double yIncrement = (double) dy / steps;
        sb.append(String.format("xIncrement = dx / steps = %d / %d = %.4f\n", dx, steps, xIncrement));
        sb.append(String.format("yIncrement = dy / steps = %d / %d = %.4f\n\n", dy, steps, yIncrement));

        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append("Шаг │    X (точн)    │    Y (точн)    │  Пиксель (x,y)\n");
        sb.append("─────────────────────────────────────────────────────────\n");

        double x = x1;
        double y = y1;

        int limit = Math.min(15, steps + 1); // Показываем первые 15 шагов
        for (int i = 0; i < limit; i++) {
            int pixelX = (int) Math.round(x);
            int pixelY = (int) Math.round(y);
            sb.append(String.format(" %2d  │  %7.4f      │  %7.4f      │    (%3d, %3d)\n",
                    i, x, y, pixelX, pixelY));
            x += xIncrement;
            y += yIncrement;
        }

        if (steps + 1 > limit) {
            sb.append("... (остальные шаги опущены)\n");
        }

        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append(String.format("Всего пикселей: %d\n", steps + 1));

        return sb;
    }

    // Алгоритм ЦДА (DDA)
    public static List<Point> dda(int x1, int y1, int x2, int y2) {
        List<Point> pixels = new ArrayList<>();

        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        double xIncrement = (double) dx / steps;
        double yIncrement = (double) dy / steps;

        double x = x1;
        double y = y1;

        for (int i = 0; i <= steps; i++) {
            pixels.add(new Point((int) x, (int) y));
            x += xIncrement;
            y += yIncrement;
        }

        return pixels;
    }

    public static StringBuilder getDDACalculations(int x1, int y1, int x2, int y2) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("  АЛГОРИТМ ЦДА (Digital Differential Analyzer)\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        sb.append(String.format("Начальная точка: (%d, %d)\n", x1, y1));
        sb.append(String.format("Конечная точка: (%d, %d)\n\n", x2, y2));

        int dx = x2 - x1;
        int dy = y2 - y1;
        sb.append(String.format("dx = x2 - x1 = %d - %d = %d\n", x2, x1, dx));
        sb.append(String.format("dy = y2 - y1 = %d - %d = %d\n\n", y2, y1, dy));

        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        sb.append(String.format("steps = max(|dx|, |dy|) = max(%d, %d) = %d\n\n", Math.abs(dx), Math.abs(dy), steps));

        double xIncrement = (double) dx / steps;
        double yIncrement = (double) dy / steps;
        sb.append(String.format("xIncrement = dx / steps = %d / %d = %.4f\n", dx, steps, xIncrement));
        sb.append(String.format("yIncrement = dy / steps = %d / %d = %.4f\n\n", dy, steps, yIncrement));

        sb.append("Отличие от пошагового: используется отбрасывание дробной части\n");
        sb.append("вместо округления: (int)x вместо round(x)\n\n");

        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append("Шаг │    X (точн)    │    Y (точн)    │  Пиксель (x,y)\n");
        sb.append("─────────────────────────────────────────────────────────\n");

        double x = x1;
        double y = y1;

        int limit = Math.min(15, steps + 1);
        for (int i = 0; i < limit; i++) {
            int pixelX = (int) x;
            int pixelY = (int) y;
            sb.append(String.format(" %2d  │  %7.4f      │  %7.4f      │    (%3d, %3d)\n",
                    i, x, y, pixelX, pixelY));
            x += xIncrement;
            y += yIncrement;
        }

        if (steps + 1 > limit) {
            sb.append("... (остальные шаги опущены)\n");
        }

        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append(String.format("Всего пикселей: %d\n", steps + 1));

        return sb;
    }

    // Алгоритм Брезенхема для отрезка
    public static List<Point> bresenhamLine(int x1, int y1, int x2, int y2) {
        List<Point> pixels = new ArrayList<>();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;
        int x = x1;
        int y = y1;

        while (true) {
            pixels.add(new Point(x, y));

            if (x == x2 && y == y2) break;

            int err2 = 2 * err;

            if (err2 > -dy) {
                err -= dy;
                x += sx;
            }

            if (err2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return pixels;
    }

    public static StringBuilder getBresenhamLineCalculations(int x1, int y1, int x2, int y2) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("  АЛГОРИТМ БРЕЗЕНХЕМА (отрезок)\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        sb.append(String.format("Начальная точка: (%d, %d)\n", x1, y1));
        sb.append(String.format("Конечная точка: (%d, %d)\n\n", x2, y2));

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        sb.append(String.format("dx = |x2 - x1| = |%d - %d| = %d\n", x2, x1, dx));
        sb.append(String.format("dy = |y2 - y1| = |%d - %d| = %d\n\n", y2, y1, dy));

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        sb.append(String.format("sx = %d  (направление по X: %s)\n", sx, sx > 0 ? "вправо" : "влево"));
        sb.append(String.format("sy = %d  (направление по Y: %s)\n\n", sy, sy > 0 ? "вверх" : "вниз"));

        int err = dx - dy;
        sb.append(String.format("err (начальная ошибка) = dx - dy = %d - %d = %d\n\n", dx, dy, err));

        sb.append("Преимущество: только целочисленная арифметика!\n\n");

        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append("Шаг │ (x, y) │  err  │ err2 │ Условия          │ Действие\n");
        sb.append("─────────────────────────────────────────────────────────\n");

        int x = x1;
        int y = y1;
        int step = 0;
        int limit = 15;

        while (step < limit) {
            int err2 = 2 * err;
            String conditions = "";
            String action = "";

            if (err2 > -dy) {
                conditions += "err2>-dy ";
                action += String.format("x+=%d, err-=%d ", sx, dy);
            }
            if (err2 < dx) {
                conditions += "err2<dx ";
                action += String.format("y+=%d, err+=%d", sy, dx);
            }
            if (conditions.isEmpty()) {
                conditions = "—";
                action = "—";
            }

            sb.append(String.format(" %2d  │ (%2d,%2d) │ %4d │ %4d │ %-16s │ %s\n",
                    step, x, y, err, err2, conditions.trim(), action));

            if (x == x2 && y == y2) {
                step++;
                break;
            }

            if (err2 > -dy) {
                err -= dy;
                x += sx;
            }

            if (err2 < dx) {
                err += dx;
                y += sy;
            }

            step++;
        }

        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append(String.format("Всего итераций: %d\n", step));

        return sb;
    }

    // Алгоритм Брезенхема для окружности
    public static List<Point> bresenhamCircle(int xc, int yc, int r) {
        List<Point> pixels = new ArrayList<>();

        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        addCirclePoints(pixels, xc, yc, x, y);

        while (y >= x) {
            x++;

            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }

            addCirclePoints(pixels, xc, yc, x, y);
        }

        return pixels;
    }

    public static StringBuilder getCircleCalculations(int xc, int yc, int r) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("  АЛГОРИТМ БРЕЗЕНХЕМА (окружность)\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        sb.append(String.format("Центр окружности: (%d, %d)\n", xc, yc));
        sb.append(String.format("Радиус: %d\n\n", r));

        sb.append("Используем симметрию окружности относительно 8 октантов\n");
        sb.append("Для каждой точки (x,y) рисуем 8 симметричных точек:\n");
        sb.append("  (±x, ±y) и (±y, ±x)\n\n");

        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        sb.append(String.format("x = 0, y = r = %d\n", r));
        sb.append(String.format("d (параметр решения) = 3 - 2*r = 3 - 2*%d = %d\n\n", r, d));

        sb.append("─────────────────────────────────────────────────────────\n");
        sb.append("Шаг │ x │ y │   d   │ Условие │ Новый d          │ 8 точек\n");
        sb.append("─────────────────────────────────────────────────────────\n");

        int step = 0;
        int limit = 10;

        sb.append(String.format(" %2d │%2d │%2d │ %5d │   —     │        —         │ отрисованы\n",
                step, x, y, d));

        while (y >= x && step < limit) {
            x++;
            step++;

            String condition;
            String formula;
            if (d > 0) {
                condition = "d > 0";
                formula = String.format("4*(x-y)+10 = 4*(%d-%d)+10", x, y);
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                condition = "d ≤ 0";
                formula = String.format("4*x+6 = 4*%d+6", x);
                d = d + 4 * x + 6;
            }

            sb.append(String.format(" %2d │%2d │%2d │ %5d │ %-7s │ %-16s │ отрисованы\n",
                    step, x, y, d, condition, formula));

            if (step >= limit) {
                sb.append("... (остальные шаги опущены)\n");
                break;
            }
        }

        sb.append("─────────────────────────────────────────────────────────\n");

        // Подсчитаем общее количество точек
        x = 0;
        y = r;
        d = 3 - 2 * r;
        int totalSteps = 1;
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            totalSteps++;
        }

        sb.append(String.format("Всего шагов: %d\n", totalSteps));
        sb.append(String.format("Всего пикселей: %d (по 8 на каждый шаг)\n", totalSteps * 8));

        return sb;
    }

    private static void addCirclePoints(List<Point> pixels, int xc, int yc, int x, int y) {
        pixels.add(new Point(xc + x, yc + y));
        pixels.add(new Point(xc - x, yc + y));
        pixels.add(new Point(xc + x, yc - y));
        pixels.add(new Point(xc - x, yc - y));
        pixels.add(new Point(xc + y, yc + x));
        pixels.add(new Point(xc - y, yc + x));
        pixels.add(new Point(xc + y, yc - x));
        pixels.add(new Point(xc - y, yc - x));
    }
}

class DrawingPanel extends JPanel {
    private static final int CELL_SIZE = 15;
    private static final int GRID_RANGE = 50;

    private List<Point> pixels = new ArrayList<>();

    public DrawingPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
    }

    public void setPixels(List<Point> pixels) {
        this.pixels = pixels;
        repaint();
    }

    public void clearPixels() {
        this.pixels.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Рисуем сетку
        g2d.setColor(new Color(230, 230, 230));
        g2d.setStroke(new BasicStroke(1));

        for (int i = -GRID_RANGE; i <= GRID_RANGE; i++) {
            int x = centerX + i * CELL_SIZE;
            int y = centerY + i * CELL_SIZE;

            if (x >= 0 && x <= getWidth()) {
                g2d.drawLine(x, 0, x, getHeight());
            }
            if (y >= 0 && y <= getHeight()) {
                g2d.drawLine(0, y, getWidth(), y);
            }
        }

        // Рисуем оси координат
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, centerY, getWidth(), centerY);
        g2d.drawLine(centerX, 0, centerX, getHeight());

        // Подписи осей
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("X", getWidth() - 20, centerY - 5);
        g2d.drawString("Y", centerX + 5, 15);
        g2d.drawString("0", centerX + 5, centerY - 5);

        // Деления и подписи на осях
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = -GRID_RANGE; i <= GRID_RANGE; i += 5) {
            if (i == 0) continue;

            int x = centerX + i * CELL_SIZE;
            int y = centerY + i * CELL_SIZE;

            // Деления на оси X
            g2d.drawLine(x, centerY - 3, x, centerY + 3);
            g2d.drawString(String.valueOf(i), x - 5, centerY + 15);

            // Деления на оси Y
            g2d.drawLine(centerX - 3, y, centerX + 3, y);
            g2d.drawString(String.valueOf(-i), centerX + 5, y + 5);
        }

        // Рисуем пиксели
        g2d.setColor(new Color(255, 0, 0, 180));
        for (Point p : pixels) {
            int screenX = centerX + p.x * CELL_SIZE;
            int screenY = centerY - p.y * CELL_SIZE;

            // Рисуем квадрат в центре ячейки
            g2d.fillRect(screenX - CELL_SIZE/2, screenY - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);

            // Обводка
            g2d.setColor(new Color(200, 0, 0));
            g2d.drawRect(screenX - CELL_SIZE/2, screenY - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
            g2d.setColor(new Color(255, 0, 0, 180));
        }

        // Пояснение
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString("Целочисленные координаты соответствуют центрам закрашенных ячеек сетки", 10, getHeight() - 10);
    }
}
