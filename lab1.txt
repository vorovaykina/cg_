import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Лабораторная работа 1:
 * Вариант (неч): CMYK – RGB – HLS
 * Приложение позволяет изменять цвет в трёх моделях,автоматически пересчитывая все остальные при изменении любой.
 */

public class Main extends JFrame {
    //панель предпросмотра
    private final JPanel preview = new JPanel();

    //RGB
    private final JSlider rSlider = new JSlider(0, 255);
    private final JSlider gSlider = new JSlider(0, 255);
    private final JSlider bSlider = new JSlider(0, 255);
    private final JTextField rField = new JTextField(3);
    private final JTextField gField = new JTextField(3);
    private final JTextField bField = new JTextField(3);

    //CMYK
    private final JSlider cSlider = new JSlider(0, 100);
    private final JSlider mSlider = new JSlider(0, 100);
    private final JSlider ySlider = new JSlider(0, 100);
    private final JSlider kSlider = new JSlider(0, 100);
    private final JTextField cField = new JTextField(3);
    private final JTextField mField = new JTextField(3);
    private final JTextField yField = new JTextField(3);
    private final JTextField kField = new JTextField(3);

    //HLS
    private final JSlider hSlider = new JSlider(0, 360);
    private final JSlider sSlider = new JSlider(0, 100);
    private final JSlider lSlider = new JSlider(0, 100);
    private final JTextField hField = new JTextField(4);
    private final JTextField sField = new JTextField(3);
    private final JTextField lField = new JTextField(3);

    private final JButton chooseColorBtn = new JButton("Выбрать цвет (палитра)");

    private boolean programmaticChange = false;

    public Main() {
        super("Лабораторная: CMYK - RGB - HLS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLayout(new BorderLayout(10, 10));

        JPanel center = new JPanel(new GridLayout(1, 3, 10, 10));
        center.add(buildCMYKPanel());
        center.add(buildRGBPanel());
        center.add(buildHLSPanel());
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        preview.setPreferredSize(new Dimension(150, 150));
        preview.setBorder(BorderFactory.createTitledBorder("Просмотр цвета"));
        bottom.add(preview);
        bottom.add(chooseColorBtn);
        add(bottom, BorderLayout.SOUTH);

        setupListeners();

        setColorFromRGB(128, 64, 192);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildRGBPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("RGB (0–255)"));
        JPanel sliders = new JPanel(new GridLayout(3, 1, 5, 5));
        sliders.add(makeLabeledSlider("R:", rSlider, rField));
        sliders.add(makeLabeledSlider("G:", gSlider, gField));
        sliders.add(makeLabeledSlider("B:", bSlider, bField));
        p.add(sliders, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCMYKPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("CMYK (0–100 %)"));
        JPanel sliders = new JPanel(new GridLayout(4, 1, 5, 5));
        sliders.add(makeLabeledSlider("C:", cSlider, cField));
        sliders.add(makeLabeledSlider("M:", mSlider, mField));
        sliders.add(makeLabeledSlider("Y:", ySlider, yField));
        sliders.add(makeLabeledSlider("K:", kSlider, kField));
        p.add(sliders, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildHLSPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("HLS (H° 0–360, S/L % 0–100)"));
        JPanel sliders = new JPanel(new GridLayout(3, 1, 5, 5));
        sliders.add(makeLabeledSlider("H:", hSlider, hField));
        sliders.add(makeLabeledSlider("S:", sSlider, sField));
        sliders.add(makeLabeledSlider("L:", lSlider, lField));
        p.add(sliders, BorderLayout.CENTER);
        return p;
    }

    private JPanel makeLabeledSlider(String label, JSlider slider, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JLabel jl = new JLabel(label);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(slider.getMaximum() / 4);
        slider.setMinorTickSpacing(slider.getMaximum() / 20);
        field.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(jl, BorderLayout.WEST);
        p.add(slider, BorderLayout.CENTER);
        p.add(field, BorderLayout.EAST);
        return p;
    }

    private void setupListeners() {
        //RGB
        addSliderListener(rSlider, rField, v -> setColorFromRGB(v, gSlider.getValue(), bSlider.getValue()));
        addSliderListener(gSlider, gField, v -> setColorFromRGB(rSlider.getValue(), v, bSlider.getValue()));
        addSliderListener(bSlider, bField, v -> setColorFromRGB(rSlider.getValue(), gSlider.getValue(), v));

        //CMYK
        addSliderListener(cSlider, cField, v -> setColorFromCMYK(cSlider.getValue(), mSlider.getValue(), ySlider.getValue(), kSlider.getValue()));
        addSliderListener(mSlider, mField, v -> setColorFromCMYK(cSlider.getValue(), mSlider.getValue(), ySlider.getValue(), kSlider.getValue()));
        addSliderListener(ySlider, yField, v -> setColorFromCMYK(cSlider.getValue(), mSlider.getValue(), ySlider.getValue(), kSlider.getValue()));
        addSliderListener(kSlider, kField, v -> setColorFromCMYK(cSlider.getValue(), mSlider.getValue(), ySlider.getValue(), kSlider.getValue()));

        //HLS
        addSliderListener(hSlider, hField, v -> setColorFromHSL(v, sSlider.getValue(), lSlider.getValue()));
        addSliderListener(sSlider, sField, v -> setColorFromHSL(hSlider.getValue(), v, lSlider.getValue()));
        addSliderListener(lSlider, lField, v -> setColorFromHSL(hSlider.getValue(), sSlider.getValue(), v));

        //поля ввода
        addFieldListener(rField, rSlider, 0, 255);
        addFieldListener(gField, gSlider, 0, 255);
        addFieldListener(bField, bSlider, 0, 255);
        addFieldListener(cField, cSlider, 0, 100);
        addFieldListener(mField, mSlider, 0, 100);
        addFieldListener(yField, ySlider, 0, 100);
        addFieldListener(kField, kSlider, 0, 100);
        addFieldListener(hField, hSlider, 0, 360);
        addFieldListener(sField, sSlider, 0, 100);
        addFieldListener(lField, lSlider, 0, 100);

        //палитра
        chooseColorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Выбор цвета", preview.getBackground());
            if (chosen != null) {
                setColorFromRGB(chosen.getRed(), chosen.getGreen(), chosen.getBlue());
            }
        });
    }

    private void addSliderListener(JSlider slider, JTextField field, SliderChangeHandler handler) {
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (programmaticChange) return;
                int v = slider.getValue();
                field.setText(Integer.toString(v));
                handler.changed(v);
            }
        });
    }

    private void addFieldListener(JTextField field, JSlider slider, int min, int max) {
        field.addActionListener(e -> applyFieldToSlider(field, slider, min, max));
        field.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                applyFieldToSlider(field, slider, min, max);
            }
        });
    }

    private void applyFieldToSlider(JTextField field, JSlider slider, int min, int max) {
        try {
            int v = Integer.parseInt(field.getText().trim());
            v = Math.max(min, Math.min(max, v));
            slider.setValue(v);
        } catch (NumberFormatException ex) {
            field.setText(String.valueOf(slider.getValue()));
        }
    }

    //пересчёт
    private void setColorFromRGB(int r, int g, int b) {
        r = clamp(r, 0, 255); g = clamp(g, 0, 255); b = clamp(b, 0, 255);
        programmaticChange = true;

        rSlider.setValue(r); gSlider.setValue(g); bSlider.setValue(b);
        rField.setText(String.valueOf(r)); gField.setText(String.valueOf(g)); bField.setText(String.valueOf(b));

        preview.setBackground(new Color(r, g, b));

        double[] cmyk = rgbToCmyk(r, g, b);
        int C = (int) Math.round(cmyk[0] * 100);
        int M = (int) Math.round(cmyk[1] * 100);
        int Y = (int) Math.round(cmyk[2] * 100);
        int K = (int) Math.round(cmyk[3] * 100);
        cSlider.setValue(C); mSlider.setValue(M); ySlider.setValue(Y); kSlider.setValue(K);
        cField.setText(String.valueOf(C)); mField.setText(String.valueOf(M));
        yField.setText(String.valueOf(Y)); kField.setText(String.valueOf(K));

        double[] hsl = rgbToHsl(r, g, b);
        int H = (int) Math.round(hsl[0]);
        int S = (int) Math.round(hsl[1] * 100);
        int L = (int) Math.round(hsl[2] * 100);
        hSlider.setValue(H); sSlider.setValue(S); lSlider.setValue(L);
        hField.setText(String.valueOf(H)); sField.setText(String.valueOf(S)); lField.setText(String.valueOf(L));

        programmaticChange = false;
    }

    private void setColorFromCMYK(int Cpct, int Mpct, int Ypct, int Kpct) {
        Cpct = clamp(Cpct, 0, 100); Mpct = clamp(Mpct, 0, 100);
        Ypct = clamp(Ypct, 0, 100); Kpct = clamp(Kpct, 0, 100);

        programmaticChange = true;
        cSlider.setValue(Cpct); mSlider.setValue(Mpct); ySlider.setValue(Ypct); kSlider.setValue(Kpct);
        cField.setText(String.valueOf(Cpct)); mField.setText(String.valueOf(Mpct));
        yField.setText(String.valueOf(Ypct)); kField.setText(String.valueOf(Kpct));
        programmaticChange = false;

        int[] rgb = cmykToRgb(Cpct / 100.0, Mpct / 100.0, Ypct / 100.0, Kpct / 100.0);
        setColorFromRGB(rgb[0], rgb[1], rgb[2]);
    }

    private void setColorFromHSL(int Hdeg, int Spct, int Lpct) {
        Hdeg = mod(Hdeg, 360);
        Spct = clamp(Spct, 0, 100);
        Lpct = clamp(Lpct, 0, 100);

        programmaticChange = true;
        hSlider.setValue(Hdeg); sSlider.setValue(Spct); lSlider.setValue(Lpct);
        hField.setText(String.valueOf(Hdeg)); sField.setText(String.valueOf(Spct)); lField.setText(String.valueOf(Lpct));
        programmaticChange = false;

        int[] rgb = hslToRgb(Hdeg, Spct / 100.0, Lpct / 100.0);
        setColorFromRGB(rgb[0], rgb[1], rgb[2]);
    }

    private double[] rgbToCmyk(int r, int g, int b) {
        double rd = r / 255.0, gd = g / 255.0, bd = b / 255.0;
        double K = 1 - Math.max(rd, Math.max(gd, bd));
        double C = 0, M = 0, Y = 0;
        if (K < 1) {
            C = (1 - rd - K) / (1 - K);
            M = (1 - gd - K) / (1 - K);
            Y = (1 - bd - K) / (1 - K);
        }
        return new double[]{C, M, Y, K};
    }

    private int[] cmykToRgb(double C, double M, double Y, double K) {
        int r = (int) Math.round(255 * (1 - C) * (1 - K));
        int g = (int) Math.round(255 * (1 - M) * (1 - K));
        int b = (int) Math.round(255 * (1 - Y) * (1 - K));
        return new int[]{clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255)};
    }

    private double[] rgbToHsl(int r, int g, int b) {
        double rd = r / 255.0, gd = g / 255.0, bd = b / 255.0;
        double max = Math.max(rd, Math.max(gd, bd));
        double min = Math.min(rd, Math.min(gd, bd));
        double h = 0, s, l = (max + min) / 2.0;

        double d = max - min;
        if (d == 0) {
            h = 0;
            s = 0;
        } else {
            s = d / (1 - Math.abs(2 * l - 1));
            if (max == rd) h = 60 * (((gd - bd) / d) % 6);
            else if (max == gd) h = 60 * (((bd - rd) / d) + 2);
            else h = 60 * (((rd - gd) / d) + 4);
            if (h < 0) h += 360;
        }
        return new double[]{h, s, l};
    }

    private int[] hslToRgb(double h, double s, double l) {
        double c = (1 - Math.abs(2 * l - 1)) * s;
        double x = c * (1 - Math.abs((h / 60) % 2 - 1));
        double m = l - c / 2.0;
        double r1, g1, b1;
        if (h < 60) { r1 = c; g1 = x; b1 = 0; }
        else if (h < 120) { r1 = x; g1 = c; b1 = 0; }
        else if (h < 180) { r1 = 0; g1 = c; b1 = x; }
        else if (h < 240) { r1 = 0; g1 = x; b1 = c; }
        else if (h < 300) { r1 = x; g1 = 0; b1 = c; }
        else { r1 = c; g1 = 0; b1 = x; }
        int r = (int) Math.round((r1 + m) * 255);
        int g = (int) Math.round((g1 + m) * 255);
        int b = (int) Math.round((b1 + m) * 255);
        return new int[]{clamp(r, 0, 255), clamp(g, 0, 255), clamp(b, 0, 255)};
    }

    private int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private int mod(int a, int m) { int r = a % m; return r < 0 ? r + m : r; }

    private interface SliderChangeHandler { void changed(int v); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
