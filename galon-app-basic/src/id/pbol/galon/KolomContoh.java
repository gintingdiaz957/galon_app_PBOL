package id.pbol.galon;

import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JTextField;

public class KolomContoh extends JTextField {
    private String contoh = "";

    public void setContoh(String contoh) {
        this.contoh = contoh;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gambar) {
        super.paintComponent(gambar);
        if (getDocument().getLength() == 0) {
            Insets tepi = getInsets();
            gambar.setColor(getDisabledTextColor());
            gambar.drawString(contoh, tepi.left + 2, getBaseline(getWidth(), getHeight()));
        }
    }
}
