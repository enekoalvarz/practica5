package pck;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Ventana extends JFrame {

    protected static int lineasProcesadas;
    protected static HashMap<String, UsuarioTwitter> usuarios_id;
    protected static HashMap<String, UsuarioTwitter> usuarios_nick;
    protected static JTextArea textarea;
    protected static JProgressBar barraProgresoCarga;
    protected TreeSet<String> usuariosconAmistades;
    protected HashMap<Integer, UsuarioTwitter> usuariosconAmistadesMap;
    protected DefaultTableModel modelo;

    public Ventana() {
        lineasProcesadas = 0;
        usuariosconAmistades = new TreeSet<>(); //TODO ESTO NO LO HE HECHO, EN VEZ LO HE GUARDADO EN UN HASHMAP
        usuariosconAmistadesMap = new HashMap<>();

        setSize(1200,800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        add(main);

        modelo = new DefaultTableModel();
        JTable tabla = new JTable(modelo);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        String[] cabeceras = {"ID", "ScreenName", "Followers Count", "Friends Count", "Language", "Last Seen"};
        modelo.setColumnIdentifiers(cabeceras);
        main.add(scrollTabla, BorderLayout.CENTER);

        textarea = new JTextArea();
        textarea.setRows(13);
        DefaultCaret caret = (DefaultCaret) textarea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); //scrollea solo
        JScrollPane scroll = new JScrollPane(textarea);
        main.add(scroll, BorderLayout.SOUTH);

        //ProgessBar
        barraProgresoCarga = new JProgressBar(0,100);
        barraProgresoCarga.setStringPainted(true);
        this.add(barraProgresoCarga, BorderLayout.SOUTH);

        setVisible(true);

        // ----------- GESTIONAR DATOS ------------
        GestionTwitter.cargarDatosdeCSV(); //carga datos a usuarios_id
        GestionTwitter.añadirUsuariosPorNick(usuarios_id); //carga a usuarios_nick
        establecerRelacionesEntreAmistades(); //establece relaciones para el TreeSet(Supongo)
        //cargarTablaConUsuarios();


    }

    protected void establecerRelacionesEntreAmistades(){ // TODO FALTA EL FOKIN TREE
        int countUsuariosconAmigos = 0;
        for (Map.Entry<String, UsuarioTwitter> entry : usuarios_nick.entrySet()) {
            UsuarioTwitter usuario = entry.getValue();
            int amigosDento = contarAmigosDentro(usuario);
            int amigosFuera = contarAmigosFuera(usuario);
            //System.out.println("Usuario "+usuario.getScreenName()+" tiene "+amigosDento+" amigos dentro del sistema y "+amigosFuera+" amigos fuera.");
            textarea.append("Usuario "+usuario.getScreenName()+" tiene "+amigosDento+" amigos dentro del sistema y "+amigosFuera+" amigos fuera.\n");


            //AÑADIR A LA TABLA LOS QUE TIENEN MAS DE 10
            usuariosconAmistadesMap.put(amigosDento, usuario); //TODO DEBERIA SER UN TREESET EN VEZ DE HASHAMP
            if(amigosDento > 10){
                añadirUsuarioTabla(usuario);
            }


            //Para saber el total de gente con amigos en sistema
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

    private void añadirUsuarioTabla(UsuarioTwitter usuario){
        Object[] nuevo = {usuario.getId(), usuario.getScreenName(), usuario.getFollowersCount(), usuario.getFriendsCount(), usuario.getLang(), usuario.getLastSeen()};
        modelo.addRow(nuevo);
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
