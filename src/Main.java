import controller.PlanejadorController;
import model.Repositorio;
import view.TelaPrincipal;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Repositorio repositorio = new Repositorio();
                PlanejadorController controller = new PlanejadorController(repositorio);
                TelaPrincipal tela = new TelaPrincipal(controller);
                tela.setVisible(true);
                tela.carregarDadosIniciais();
            }
        });
    }
}
