package id.pbol.galon;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tampilan.aturGaya();
            UIManager.put("OptionPane.okButtonText", "Oke");
            UIManager.put("OptionPane.cancelButtonText", "Kembali");
            new LoginForm().setVisible(true);
        });
    }
}
