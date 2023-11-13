package pck;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class GestionTwitter {

	private static final String PROPERTIES_FILE = "lastFile.properties";
	protected static String rutaFichero = "";

	public static void main(String[] args) {

		Ventana v = new Ventana();

	}
	protected static void añadirUsuariosPorNick(HashMap<String, UsuarioTwitter> mapa) {
		for (Map.Entry<String, UsuarioTwitter> entry : mapa.entrySet()) {
			UsuarioTwitter usuario = entry.getValue();
			if(!Ventana.usuarios_nick.containsKey(usuario.getScreenName())){
				Ventana.usuarios_nick.put(usuario.getScreenName(), usuario);
				//System.out.println(usuario.getScreenName() +" añadido. (NICK)\n");
				Ventana.textarea.append(usuario.getScreenName() +" añadido. (NICK)\n");
			}else{
				System.out.println("usuario ("+usuario.getScreenName()+") evitado!");
			}
		}
	}

	protected static void cargarDatosdeCSV(){
		/*
		try {
			rutaFichero = "C:\\Users\\alvar\\Downloads\\data.csv";
			CSV.processCSV( new File( rutaFichero ) );
		} catch (Exception e) {
			e.printStackTrace();
		}
		 */

		Properties properties = new Properties();
		String lastFilePath = "";
		File lastFile = new File(PROPERTIES_FILE);

		try (FileInputStream fis = new FileInputStream(lastFile)) {
			properties.load(fis);
			lastFilePath = properties.getProperty("lastFile", "");
		} catch (IOException e) {
			e.printStackTrace();
		}

		JFileChooser fileChooser = new JFileChooser(lastFilePath);
		int returnVal = fileChooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			lastFilePath = selectedFile.getAbsolutePath();
			rutaFichero = lastFilePath;
			try {
				properties.setProperty("lastFile", lastFilePath);
				try (FileOutputStream fos = new FileOutputStream(lastFile)) {
					properties.store(fos, "Last file path");
				} catch (IOException e) {
					e.printStackTrace();
				}

				CSV.processCSV(selectedFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No se ha seleccionado ningún archivo.");
		}

	}

	protected static int obtenerTotalLineasCSV(String ruta){
		int total =0;
		try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
			while (br.readLine() != null) {
				total++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return total;
	}

}
