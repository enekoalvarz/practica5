package pck;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Ventana extends JFrame {

    protected static HashMap<String, UsuarioTwitter> usuarios_id;
    protected static HashMap<String, UsuarioTwitter> usuarios_nick;
    protected TreeSet<String> usuariosconAmistades;
    protected static JTextArea textarea;
    public Ventana() {
        usuariosconAmistades = new TreeSet<>();

        setSize(1200,800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        add(main);

        textarea = new JTextArea();
        DefaultCaret caret = (DefaultCaret) textarea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); //scrollea solo
        JScrollPane scroll = new JScrollPane(textarea);
        main.add(scroll);

        setVisible(true);

        establecerRelacionesEntreAmistades();
    }

    protected void establecerRelacionesEntreAmistades(){ // TODO FALTA EL FOKIN TREE
        int countUsuariosconAmigos = 0;
        for (Map.Entry<String, UsuarioTwitter> entry : usuarios_nick.entrySet()) {
            UsuarioTwitter usuario = entry.getValue();
            int amigosDento = contarAmigosDentro(usuario);
            int amigosFuera = contarAmigosFuera(usuario);
            //System.out.println("Usuario "+usuario.getScreenName()+" tiene "+amigosDento+" amigos dentro del sistema y "+amigosFuera+" amigos fuera.");
            textarea.append("Usuario "+usuario.getScreenName()+" tiene "+amigosDento+" amigos dentro del sistema y "+amigosFuera+" amigos fuera.\n");
            if(amigosDento>0){
                countUsuariosconAmigos++;
            }
        }
        //System.out.println(countUsuariosconAmigos +" con algunos amigos dentro del sistema.");
        textarea.append(countUsuariosconAmigos +" con algunos amigos dentro del sistema.");
    }

    private int contarAmigosDentro(UsuarioTwitter usuario){
        int count = 0;
        for(String amigo : usuario.getFriends()){
            if(usuarios_id.containsKey(amigo)){
                count++;
            }
        }
        return count;
    }

    private int contarAmigosFuera(UsuarioTwitter usuario){
        int count = 0;
        for(String amigo : usuario.getFriends()){
            if(!usuarios_id.containsKey(amigo)){
                count++;
            }
        }
        return count;
    }

    /*
    protected static void sacarConsolaPorVentana() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                actualizarTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                actualizarTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
    }

    private static void actualizarTextArea(final String text) {
        SwingUtilities.invokeLater(() -> textarea.append(text));
    }

     */

}
