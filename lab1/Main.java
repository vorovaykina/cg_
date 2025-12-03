import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {
    private JSlider rSlider, gSlider, bSlider;
    private JTextField rField, gField, bField;
    private JSlider cSlider, mSlider, ySlider, kSlider;
    private JTextField cField, mField, yField, kField;
    private JSlider hSlider, sSlider, vSlider;
    private JTextField hField, sField, vField;

    private JPanel preview;

    private boolean isUpdating = false;
    private boolean ignoreCMYKUpdate = false;

    public Main() {
        super("Color Palette");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        JPanel main = new JPanel(new BorderLayout(8,8));
        main.setBorder(new EmptyBorder(8,8,8,8));
        add(main, BorderLayout.CENTER);

        JPanel controllers = new JPanel();
        controllers.setLayout(new BoxLayout(controllers, BoxLayout.Y_AXIS));
        main.add(controllers, BorderLayout.CENTER);

        controllers.add(rgbPanel());
        controllers.add(Box.createVerticalStrut(8));
        controllers.add(cmykPanel());
        controllers.add(Box.createVerticalStrut(8));
        controllers.add(hsvPanel());
        controllers.add(Box.createVerticalStrut(8));
        controllers.add(button());

        JPanel right = new JPanel(new BorderLayout(8,8));
        preview = new JPanel();
        preview.setPreferredSize(new Dimension(220,220));
        preview.setBorder(new LineBorder(Color.BLACK,1));
        right.add(preview, BorderLayout.NORTH);
        right.add(palette(), BorderLayout.CENTER);

        main.add(right, BorderLayout.EAST);

        updateFromRGB(255,255,255);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel rgbPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("RGB (0-255)"));
        JPanel sliders = new JPanel(new GridLayout(3,1,4,4));

        rSlider = createSlider(0,255,255);
        gSlider = createSlider(0,255,255);
        bSlider = createSlider(0,255,255);

        rField = createNumberField("255");
        gField = createNumberField("255");
        bField = createNumberField("255");

        sliders.add(makeRow("R:", rSlider, rField));
        sliders.add(makeRow("G:", gSlider, gField));
        sliders.add(makeRow("B:", bSlider, bField));

        panel.add(sliders, BorderLayout.CENTER);

        rSlider.addChangeListener(e -> { if (!isUpdating) updateFromRGBSliders(); });
        gSlider.addChangeListener(e -> { if (!isUpdating) updateFromRGBSliders(); });
        bSlider.addChangeListener(e -> { if (!isUpdating) updateFromRGBSliders(); });

        addFieldListener(rField, 0,255, val -> { if (!isUpdating) updateFromRGBFields(); });
        addFieldListener(gField, 0,255, val -> { if (!isUpdating) updateFromRGBFields(); });
        addFieldListener(bField, 0,255, val -> { if (!isUpdating) updateFromRGBFields(); });

        return panel;
    }

    private JPanel cmykPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("CMYK (0-100)"));
        JPanel sliders = new JPanel(new GridLayout(4,1,4,4));

        cSlider = createSlider(0,100,0);
        mSlider = createSlider(0,100,0);
        ySlider = createSlider(0,100,0);
        kSlider = createSlider(0,100,0);

        cField = createNumberField("0");
        mField = createNumberField("0");
        yField = createNumberField("0");
        kField = createNumberField("0");

        sliders.add(makeRow("C:", cSlider, cField));
        sliders.add(makeRow("M:", mSlider, mField));
        sliders.add(makeRow("Y:", ySlider, yField));
        sliders.add(makeRow("K:", kSlider, kField));

        panel.add(sliders, BorderLayout.CENTER);

        cSlider.addChangeListener(e -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKSliders(); });
        mSlider.addChangeListener(e -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKSliders(); });
        ySlider.addChangeListener(e -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKSliders(); });
        kSlider.addChangeListener(e -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKSliders(); });

        addFieldListener(cField, 0,100, val -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKFields(); });
        addFieldListener(mField, 0,100, val -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKFields(); });
        addFieldListener(yField, 0,100, val -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKFields(); });
        addFieldListener(kField, 0,100, val -> { if (!isUpdating && !ignoreCMYKUpdate) updateFromCMYKFields(); });

        return panel;
    }

    private JPanel hsvPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("HSV (H:0-360, S/V:0-100%)"));
        JPanel sliders = new JPanel(new GridLayout(3,1,4,4));

        hSlider = createSlider(0,360,0);
        sSlider = createSlider(0,100,0);
        vSlider = createSlider(0,100,0);

        hField = createNumberField("0");
        sField = createNumberField("0");
        vField = createNumberField("0");

        sliders.add(makeRow("H:", hSlider, hField));
        sliders.add(makeRow("S:", sSlider, sField));
        sliders.add(makeRow("V:", vSlider, vField));

        panel.add(sliders, BorderLayout.CENTER);

        hSlider.addChangeListener(e -> { if (!isUpdating) updateFromHSVSliders(); });
        sSlider.addChangeListener(e -> { if (!isUpdating) updateFromHSVSliders(); });
        vSlider.addChangeListener(e -> { if (!isUpdating) updateFromHSVSliders(); });

        addFieldListener(hField, 0,360, val -> { if (!isUpdating) updateFromHSVFields(); });
        addFieldListener(sField, 0,100, val -> { if (!isUpdating) updateFromHSVFields(); });
        addFieldListener(vField, 0,100, val -> { if (!isUpdating) updateFromHSVFields(); });

        return panel;
    }

    private JPanel button() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));
        JButton chooser = new JButton("Выбрать цвет");
        chooser.addActionListener(e -> {
            showCustomColorPalette();
        });
        p.add(chooser);

        return p;
    }

    private void showCustomColorPalette() {
        JDialog colorDialog = new JDialog(this, "Выберите цвет", true);
        colorDialog.setLayout(new BorderLayout());
        colorDialog.setSize(500, 400);
        colorDialog.setLocationRelativeTo(this);

        JPanel colorGrid = new JPanel(new GridLayout(7, 7, 2, 2));
        colorGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        Color[][] customColors = generateCustomPalette();

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                Color color = customColors[i][j];
                JPanel colorBox = new JPanel();
                colorBox.setBackground(color);
                colorBox.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
                colorBox.setPreferredSize(new Dimension(40, 40));

                colorBox.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        updateFromRGB(color.getRed(), color.getGreen(), color.getBlue());
                        colorDialog.dispose();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        colorBox.setBorder(new LineBorder(Color.BLACK, 2));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        colorBox.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
                    }
                });

                colorGrid.add(colorBox);
            }
        }

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> colorDialog.dispose());
        bottomPanel.add(closeButton);

        colorDialog.add(colorGrid, BorderLayout.CENTER);
        colorDialog.add(bottomPanel, BorderLayout.SOUTH);
        colorDialog.setVisible(true);
    }

    private Color[][] generateCustomPalette() {
        Color[][] palette = new Color[7][7];

        palette[0][0] = new Color(255, 223, 223);
        palette[0][1] = new Color(255, 233, 213);
        palette[0][2] = new Color(255, 243, 203);
        palette[0][3] = new Color(243, 255, 203);
        palette[0][4] = new Color(223, 255, 223);
        palette[0][5] = new Color(223, 243, 255);
        palette[0][6] = new Color(233, 223, 255);

        palette[1][0] = new Color(139, 69, 19);
        palette[1][1] = new Color(160, 82, 45);
        palette[1][2] = new Color(205, 133, 63);
        palette[1][3] = new Color(210, 180, 140);
        palette[1][4] = new Color(188, 143, 143);
        palette[1][5] = new Color(165, 42, 42);
        palette[1][6] = new Color(222, 184, 135);

        palette[2][0] = new Color(72, 61, 139);
        palette[2][1] = new Color(65, 105, 225);
        palette[2][2] = new Color(70, 130, 180);
        palette[2][3] = new Color(100, 149, 237);
        palette[2][4] = new Color(32, 178, 170);
        palette[2][5] = new Color(46, 139, 87);
        palette[2][6] = new Color(60, 179, 113);

        palette[3][0] = new Color(75, 0, 130);
        palette[3][1] = new Color(138, 43, 226);
        palette[3][2] = new Color(147, 112, 219);
        palette[3][3] = new Color(186, 85, 211);
        palette[3][4] = new Color(153, 50, 204);
        palette[3][5] = new Color(139, 0, 139);
        palette[3][6] = new Color(148, 0, 211);

        palette[4][0] = new Color(112, 128, 144);
        palette[4][1] = new Color(119, 136, 153);
        palette[4][2] = new Color(176, 196, 222);
        palette[4][3] = new Color(230, 230, 250);
        palette[4][4] = new Color(245, 245, 220);
        palette[4][5] = new Color(255, 228, 196);
        palette[4][6] = new Color(255, 235, 205);

        palette[5][0] = new Color(220, 20, 60);
        palette[5][1] = new Color(255, 69, 0);
        palette[5][2] = new Color(255, 140, 0);
        palette[5][3] = new Color(255, 215, 0);
        palette[5][4] = new Color(154, 205, 50);
        palette[5][5] = new Color(0, 250, 154);
        palette[5][6] = new Color(0, 206, 209);

        palette[6][0] = new Color(25, 25, 112);
        palette[6][1] = new Color(47, 79, 79);
        palette[6][2] = new Color(85, 107, 47);
        palette[6][3] = new Color(101, 67, 33);
        palette[6][4] = new Color(139, 0, 0);
        palette[6][5] = new Color(72, 40, 100);
        palette[6][6] = new Color(30, 30, 30);

        return palette;
    }

    private JPanel palette() {
        JPanel panel = new JPanel(new BorderLayout(6,6));
        panel.setBorder(BorderFactory.createTitledBorder("Палитра"));
        JPanel grid = new JPanel(new GridLayout(3,6,6,6));

        Color[] colors = new Color[] {
                Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.WHITE, Color.RED,
                Color.PINK, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN,
                new Color(128,0,0), new Color(128,128,0), new Color(0,128,0), new Color(0,128,128), new Color(0,0,128), new Color(128,0,128)
        };

        for (Color c : colors) {
            JPanel colorBox = new JPanel();
            colorBox.setBackground(c);
            colorBox.setPreferredSize(new Dimension(40,40));
            colorBox.setBorder(new LineBorder(Color.BLACK,1));
            colorBox.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    updateFromRGB(c.getRed(), c.getGreen(), c.getBlue());
                }
            });
            grid.add(colorBox);
        }
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeRow(String label, JSlider slider, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(6,0));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(20,20));
        row.add(lbl, BorderLayout.WEST);
        row.add(slider, BorderLayout.CENTER);
        field.setPreferredSize(new Dimension(60,24));
        row.add(field, BorderLayout.EAST);
        return row;
    }

    private JSlider createSlider(int min, int max, int val) {
        JSlider s = new JSlider(min,max,val);
        s.setPaintTicks(false);
        s.setPaintLabels(false);
        s.setMajorTickSpacing((max-min)/4==0?1:(max-min)/4);
        return s;
    }

    private JTextField createNumberField(String text) {
        JTextField f = new JTextField(text,6);
        f.setHorizontalAlignment(JTextField.RIGHT);
        return f;
    }

    private void addFieldListener(JTextField field, int min, int max, IntConsumer onValid) {
        field.addActionListener(e -> {
            try {
                int v = Integer.parseInt(field.getText().trim());
                if (v < min) v = min;
                if (v > max) v = max;
                field.setText(String.valueOf(v));
                onValid.accept(v);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> field.setText(String.valueOf(min)));
            }
        });
        field.addFocusListener(new FocusAdapter(){
            public void focusLost(FocusEvent e) {
                try {
                    int v = Integer.parseInt(field.getText().trim());
                    if (v < min) v = min;
                    if (v > max) v = max;
                    field.setText(String.valueOf(v));
                    onValid.accept(v);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JSlider s = findCorrespondingSlider(field);
                        if (s != null) field.setText(String.valueOf(s.getValue()));
                    });
                }
            }
        });
    }

    private JSlider findCorrespondingSlider(JTextField f) {
        if (f==rField) return rSlider;
        if (f==gField) return gSlider;
        if (f==bField) return bSlider;
        if (f==cField) return cSlider;
        if (f==mField) return mSlider;
        if (f==yField) return ySlider;
        if (f==kField) return kSlider;
        if (f==hField) return hSlider;
        if (f==sField) return sSlider;
        if (f==vField) return vSlider;
        return null;
    }

    private void updateFromRGBSliders() {
        isUpdating = true;
        int r = rSlider.getValue();
        int g = gSlider.getValue();
        int b = bSlider.getValue();
        rField.setText(String.valueOf(r));
        gField.setText(String.valueOf(g));
        bField.setText(String.valueOf(b));
        applyRGBToAll(r,g,b);
        isUpdating = false;
    }

    private void updateFromRGBFields() {
        isUpdating = true;
        int r = parseSafeInt(rField.getText(), 0,255, rSlider.getValue());
        int g = parseSafeInt(gField.getText(), 0,255, gSlider.getValue());
        int b = parseSafeInt(bField.getText(), 0,255, bSlider.getValue());
        rSlider.setValue(r);
        gSlider.setValue(g);
        bSlider.setValue(b);
        applyRGBToAll(r,g,b);
        isUpdating = false;
    }

    private void updateFromCMYKSliders() {
        isUpdating = true;
        int c = cSlider.getValue();
        int m = mSlider.getValue();
        int y = ySlider.getValue();
        int k = kSlider.getValue();
        cField.setText(String.valueOf(c));
        mField.setText(String.valueOf(m));
        yField.setText(String.valueOf(y));
        kField.setText(String.valueOf(k));


        int[] rgb = cmykToRgb(c/100.0, m/100.0, y/100.0, k/100.0);


        ignoreCMYKUpdate = true;
        rSlider.setValue(rgb[0]);
        gSlider.setValue(rgb[1]);
        bSlider.setValue(rgb[2]);
        rField.setText(String.valueOf(rgb[0]));
        gField.setText(String.valueOf(rgb[1]));
        bField.setText(String.valueOf(rgb[2]));
        ignoreCMYKUpdate = false;


        Color color = new Color(rgb[0], rgb[1], rgb[2]);
        preview.setBackground(color);

        float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
        int hDeg = properRound(hsb[0] * 360f);
        int sPerc = properRound(hsb[1] * 100f);
        int vPerc = properRound(hsb[2] * 100f);

        hDeg = hDeg % 360;
        if (hDeg < 0) hDeg += 360;
        sPerc = clamp(sPerc, 0, 100);
        vPerc = clamp(vPerc, 0, 100);

        hSlider.setValue(hDeg);
        sSlider.setValue(sPerc);
        vSlider.setValue(vPerc);
        hField.setText(String.valueOf(hDeg));
        sField.setText(String.valueOf(sPerc));
        vField.setText(String.valueOf(vPerc));

        isUpdating = false;
    }

    private void updateFromCMYKFields() {
        isUpdating = true;
        int c = parseSafeInt(cField.getText(),0,100,cSlider.getValue());
        int m = parseSafeInt(mField.getText(),0,100,mSlider.getValue());
        int y = parseSafeInt(yField.getText(),0,100,ySlider.getValue());
        int k = parseSafeInt(kField.getText(),0,100,kSlider.getValue());
        cSlider.setValue(c);
        mSlider.setValue(m);
        ySlider.setValue(y);
        kSlider.setValue(k);

        int[] rgb = cmykToRgb(c/100.0, m/100.0, y/100.0, k/100.0);

        ignoreCMYKUpdate = true;
        rSlider.setValue(rgb[0]);
        gSlider.setValue(rgb[1]);
        bSlider.setValue(rgb[2]);
        rField.setText(String.valueOf(rgb[0]));
        gField.setText(String.valueOf(rgb[1]));
        bField.setText(String.valueOf(rgb[2]));
        ignoreCMYKUpdate = false;


        Color color = new Color(rgb[0], rgb[1], rgb[2]);
        preview.setBackground(color);

        float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
        int hDeg = properRound(hsb[0] * 360f);
        int sPerc = properRound(hsb[1] * 100f);
        int vPerc = properRound(hsb[2] * 100f);

        hDeg = hDeg % 360;
        if (hDeg < 0) hDeg += 360;
        sPerc = clamp(sPerc, 0, 100);
        vPerc = clamp(vPerc, 0, 100);

        hSlider.setValue(hDeg);
        sSlider.setValue(sPerc);
        vSlider.setValue(vPerc);
        hField.setText(String.valueOf(hDeg));
        sField.setText(String.valueOf(sPerc));
        vField.setText(String.valueOf(vPerc));

        isUpdating = false;
    }

    private void updateFromHSVSliders() {
        isUpdating = true;
        int h = hSlider.getValue();
        int s = sSlider.getValue();
        int v = vSlider.getValue();
        hField.setText(String.valueOf(h));
        sField.setText(String.valueOf(s));
        vField.setText(String.valueOf(v));

        Color c = hsvToRgb(h, s/100.0, v/100.0);

        rSlider.setValue(c.getRed());
        gSlider.setValue(c.getGreen());
        bSlider.setValue(c.getBlue());
        rField.setText(String.valueOf(c.getRed()));
        gField.setText(String.valueOf(c.getGreen()));
        bField.setText(String.valueOf(c.getBlue()));

        applyRGBToAll(c.getRed(), c.getGreen(), c.getBlue());
        isUpdating = false;
    }

    private void updateFromHSVFields() {
        isUpdating = true;
        int h = parseSafeInt(hField.getText(),0,360,hSlider.getValue());
        int s = parseSafeInt(sField.getText(),0,100,sSlider.getValue());
        int v = parseSafeInt(vField.getText(),0,100,vSlider.getValue());
        hSlider.setValue(h);
        sSlider.setValue(s);
        vSlider.setValue(v);

        Color c = hsvToRgb(h, s/100.0, v/100.0);

        rSlider.setValue(c.getRed());
        gSlider.setValue(c.getGreen());
        bSlider.setValue(c.getBlue());
        rField.setText(String.valueOf(c.getRed()));
        gField.setText(String.valueOf(c.getGreen()));
        bField.setText(String.valueOf(c.getBlue()));

        applyRGBToAll(c.getRed(), c.getGreen(), c.getBlue());
        isUpdating = false;
    }

    private void applyRGBToAll(int r, int g, int b) {
        Color color = new Color(clamp(r,0,255), clamp(g,0,255), clamp(b,0,255));
        preview.setBackground(color);

        if (rSlider.getValue() != r) rSlider.setValue(r);
        if (gSlider.getValue() != g) gSlider.setValue(g);
        if (bSlider.getValue() != b) bSlider.setValue(b);

        rField.setText(String.valueOf(r));
        gField.setText(String.valueOf(g));
        bField.setText(String.valueOf(b));

        double[] cmyk = rgbToCmyk(r,g,b);
        int cPerc = properRound(cmyk[0] * 100);
        int mPerc = properRound(cmyk[1] * 100);
        int yPerc = properRound(cmyk[2] * 100);
        int kPerc = properRound(cmyk[3] * 100);

        cPerc = clamp(cPerc, 0, 100);
        mPerc = clamp(mPerc, 0, 100);
        yPerc = clamp(yPerc, 0, 100);
        kPerc = clamp(kPerc, 0, 100);

        ignoreCMYKUpdate = true;
        cSlider.setValue(cPerc);
        mSlider.setValue(mPerc);
        ySlider.setValue(yPerc);
        kSlider.setValue(kPerc);

        cField.setText(String.valueOf(cPerc));
        mField.setText(String.valueOf(mPerc));
        yField.setText(String.valueOf(yPerc));
        kField.setText(String.valueOf(kPerc));
        ignoreCMYKUpdate = false;


        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        int hDeg = properRound(hsb[0] * 360f);
        int sPerc = properRound(hsb[1] * 100f);
        int vPerc = properRound(hsb[2] * 100f);

        hDeg = hDeg % 360;
        if (hDeg < 0) hDeg += 360;
        sPerc = clamp(sPerc, 0, 100);
        vPerc = clamp(vPerc, 0, 100);

        hSlider.setValue(hDeg);
        sSlider.setValue(sPerc);
        vSlider.setValue(vPerc);
        hField.setText(String.valueOf(hDeg));
        sField.setText(String.valueOf(sPerc));
        vField.setText(String.valueOf(vPerc));
    }

    private void updateFromRGB(int r, int g, int b) {
        isUpdating = true;
        rSlider.setValue(r);
        gSlider.setValue(g);
        bSlider.setValue(b);
        rField.setText(String.valueOf(r));
        gField.setText(String.valueOf(g));
        bField.setText(String.valueOf(b));
        applyRGBToAll(r,g,b);
        isUpdating = false;
    }

    private double[] rgbToCmyk(int r, int g, int b) {
        double r1 = r / 255.0;
        double g1 = g / 255.0;
        double b1 = b / 255.0;

        double k = 1.0 - Math.max(r1, Math.max(g1, b1));
        double c, m, y;

        if (k == 1.0) {
            c = m = y = 0.0;
        } else {
            c = (1.0 - r1 - k) / (1.0 - k);
            m = (1.0 - g1 - k) / (1.0 - k);
            y = (1.0 - b1 - k) / (1.0 - k);
        }

        return new double[]{
                clampDouble(c, 0.0, 1.0),
                clampDouble(m, 0.0, 1.0),
                clampDouble(y, 0.0, 1.0),
                clampDouble(k, 0.0, 1.0)
        };
    }

    private int[] cmykToRgb(double c, double m, double y, double k) {
        double r = 255.0 * (1.0 - c) * (1.0 - k);
        double g = 255.0 * (1.0 - m) * (1.0 - k);
        double b = 255.0 * (1.0 - y) * (1.0 - k);

        return new int[]{
                clamp((int)Math.round(r), 0, 255),
                clamp((int)Math.round(g), 0, 255),
                clamp((int)Math.round(b), 0, 255)
        };
    }

    private Color hsvToRgb(int hDeg, double s, double v) {
        float h = (float)((hDeg % 360) / 360.0);
        float sf = (float)s;
        float vf = (float)v;
        int rgbInt = Color.HSBtoRGB(h, sf, vf);
        Color c = new Color(rgbInt);
        return c;
    }

    private int properRound(double value) {
        return (int) Math.floor(value + 0.5);
    }

    private int properRound(float value) {
        return (int) Math.floor(value + 0.5f);
    }

    private int parseSafeInt(String txt, int min, int max, int fallback) {
        try {
            int v = Integer.parseInt(txt.trim());
            if (v < min) return min;
            if (v > max) return max;
            return v;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private int clamp(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    private double clampDouble(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    private interface IntConsumer { void accept(int val); }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored){}
        SwingUtilities.invokeLater(() -> new Main());
    }
}
