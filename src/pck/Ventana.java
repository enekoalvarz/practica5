package pck;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
    protected JTable tabla;
    protected Integer countUsuarioEnTabla;
    protected String etiquetaBuscar;
    protected String nombreClickTabla;
    public Ventana() {
        lineasProcesadas = 0;
        usuariosconAmistades = new TreeSet<>(); //TODO ESTO NO LO HE HECHO, EN VEZ LO HE GUARDADO EN UN HASHMAP
        usuariosconAmistadesMap = new HashMap<>();
        countUsuarioEnTabla =0;
        etiquetaBuscar = "";
        nombreClickTabla = "";

        setSize(1200,800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        add(main);

        // -------- TABLA --------
        modelo = new DefaultTableModel();
        tabla = new JTable(modelo);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        String[] cabeceras = {"ID", "ScreenName","Tags","Followers Count", "Friends Count", "Language", "Last Seen"};
        modelo.setColumnIdentifiers(cabeceras);
        main.add(scrollTabla, BorderLayout.CENTER);

            //RENDERER DE TABLA
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if(column==3){
                    table.getColumnModel().getColumn(2).setPreferredWidth(160);
                }

                //busqueda de tags
                c.setBackground(Color.white);
                if(!etiquetaBuscar.isEmpty() && column==2){
                    String id = (String) tabla.getValueAt(row, 0);
                    UsuarioTwitter usuarioFila = encontrarUsuarioID(id);
                    Boolean concuerdan = checkTags(usuarioFila, etiquetaBuscar);
                    if(concuerdan){
                        c.setBackground(Color.green);
                    }
                }

                tabla.repaint();
                return c;
            }
        });

        // ------------- JTREE -------------------

        //Obtener nombre de la tabla
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(nombreClickTabla);
        DefaultTreeModel modeloTree = new DefaultTreeModel(root);
        JTree tree = new JTree(modeloTree);
        tree.setMinimumSize(new Dimension(200, 50));
        JScrollPane scrollTree = new JScrollPane(tree);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(tabla.columnAtPoint(e.getPoint()) == 1){
                    nombreClickTabla = (String) tabla.getValueAt(tabla.rowAtPoint(e.getPoint()), 1);
                    DefaultMutableTreeNode nuevoNodo = new DefaultMutableTreeNode(nombreClickTabla);
                    modeloTree.setRoot(nuevoNodo);

                    //hacer las ramas
                    UsuarioTwitter usuarioTabla = encontrarUsuarioNom(nombreClickTabla);
                    for(String amigoID : usuarioTabla.getFriends()){
                        UsuarioTwitter amigo = encontrarUsuarioID(amigoID);
                        if(amigo != null){
                            DefaultMutableTreeNode nodoAmigoEnSistema= new DefaultMutableTreeNode(amigo.getScreenName());
                            nuevoNodo.add(nodoAmigoEnSistema);
                        }
                    }
                    modeloTree.nodeStructureChanged(nuevoNodo); //notificar al programa que he cambiado el tree, sino no cargan bien las hijas
                }
                main.revalidate();
                main.repaint();
            }
        });



        main.add(scrollTree, BorderLayout.WEST);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode nodoSelec = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if(nodoSelec != null){
                    String stringSelec = nodoSelec.getUserObject().toString();
                    UsuarioTwitter usuarioSelec = encontrarUsuarioNom(stringSelec);
                    for(String amigoID : usuarioSelec.getFriends()){
                        UsuarioTwitter usuarioAmigo = encontrarUsuarioID(amigoID);
                        if(usuarioAmigo != null){
                            DefaultMutableTreeNode nuevoNodo = new DefaultMutableTreeNode(usuarioAmigo.getScreenName());
                            nodoSelec.add(nuevoNodo);
                        }
                    }
                }
                modeloTree.nodeStructureChanged(nodoSelec);
                main.revalidate();
                main.repaint();
            }
        });


        // ------------ CONSOLA POR PANTALLA -----------
        JPanel panelAbajo = new JPanel(new BorderLayout());
        main.add(panelAbajo, BorderLayout.SOUTH);
        textarea = new JTextArea();
        textarea.setRows(13);
        DefaultCaret caret = (DefaultCaret) textarea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); //scrollea solo
        JScrollPane scroll = new JScrollPane(textarea);
        panelAbajo.add(scroll, BorderLayout.CENTER);

        // ------------ BUSQUEDA DE ETIQUETA ------------
        JPanel panelBEtiqueta = new JPanel(new BorderLayout());
        JTextArea textBusquedaEtiqueta = new JTextArea();
        textBusquedaEtiqueta.setColumns(15);
        panelBEtiqueta.add(textBusquedaEtiqueta);
        panelBEtiqueta.add(new JLabel("Etiqueta a buscar:"), BorderLayout.NORTH);
        JButton buscarEtiqueta = new JButton("Buscar");
        panelBEtiqueta.add(buscarEtiqueta, BorderLayout.SOUTH);
        panelAbajo.add(panelBEtiqueta, BorderLayout.EAST);
        buscarEtiqueta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                etiquetaBuscar = textBusquedaEtiqueta.getText();
            }
        });

        // -------- PROGRESS BAR ----------
        barraProgresoCarga = new JProgressBar(0,100);
        barraProgresoCarga.setStringPainted(true);
        this.add(barraProgresoCarga, BorderLayout.SOUTH);

        setVisible(true);

        // ----------- GESTIONAR DATOS ------------
        GestionTwitter.cargarDatosdeCSV(); //carga datos a usuarios_id
        GestionTwitter.añadirUsuariosPorNick(usuarios_id); //carga a usuarios_nick
        establecerRelacionesEntreAmistades(usuarios_nick); //establece relaciones para el TreeSet(Supongo) y añade a tabla
        System.out.println(countUsuarioEnTabla);


    }

    protected void establecerRelacionesEntreAmistades(HashMap<String, UsuarioTwitter> mapadeusuarios){ // TODO FALTA EL FOKIN TREE
        int countUsuariosconAmigos = 0;
        for (Map.Entry<String, UsuarioTwitter> entry : mapadeusuarios.entrySet()) {
            UsuarioTwitter usuario = entry.getValue();
            int amigosDento = contarAmigosDentro(usuario);
            int amigosFuera = contarAmigosFuera(usuario);
            //System.out.println("Usuario "+usuario.getScreenName()+" tiene "+amigosDento+" amigos dentro del sistema y "+amigosFuera+" amigos fuera.");
            textarea.append("Usuario "+usuario.getScreenName()+" tiene "+amigosDento+" amigos dentro del sistema y "+amigosFuera+" amigos fuera.\n");


            //AÑADIR A LA TABLA LOS QUE TIENEN MAS DE 10
            //usuariosconAmistadesMap.put(amigosDento, usuario); //TODO DEBERIA SER UN TREESET EN VEZ DE HASHAMP, no necesario en verdad
            if(amigosDento > 10){
                añadirUsuarioTabla(usuario);
                countUsuarioEnTabla++;
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
        ArrayList<String> tags = usuario.getTags();
        String tagsString = String.join(", ", tags);
        Object[] nuevo = {usuario.getId(), usuario.getScreenName(), tagsString, usuario.getFollowersCount(), usuario.getFriendsCount(), usuario.getLang(), usuario.getLastSeen()};
        modelo.addRow(nuevo);
        //tabla.repaint();
    }
    private UsuarioTwitter encontrarUsuarioID(String id){
        UsuarioTwitter usuarioTwitter = usuarios_id.get(id);
        return usuarioTwitter;
    }
    private UsuarioTwitter encontrarUsuarioNom(String nombre){
        UsuarioTwitter usuario = usuarios_nick.get(nombre);
        return usuario;
    }
    private Boolean checkTags(UsuarioTwitter usuario, String tagBuscada){
        ArrayList<String> tags = usuario.getTags();
        Boolean resultado = false;
        for(String tag : tags){
            if(tag.equals(tagBuscada)){
                resultado = true;
            }
        }
        return resultado;
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
