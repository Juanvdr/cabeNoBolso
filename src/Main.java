import controller.PlanejadorController;
import model.Persistencia;
import model.Repositorio;
import view.TelaPrincipal;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Persistencia persistencia = new Repositorio();
                PlanejadorController controller = new PlanejadorController(persistencia);
                TelaPrincipal tela = new TelaPrincipal(controller);
                tela.setVisible(true);
                tela.carregarDadosIniciais();
            }
        });
    }
}
