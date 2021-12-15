package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main extends Application {
    private TextArea mensajes;
    private Integer idMensaje = 0;

    @Override
    public void start(Stage primaryStage) throws Exception{
       BorderPane root = new BorderPane();
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        mensajes = new TextArea();
        mensajes.setEditable(false);
        root.setCenter(mensajes);
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        HiloServidor hiloServidor = new HiloServidor();
        hiloServidor.start();
    }
    public void agregarMensaje(String mensaje){
        Date hoy = new Date();
         String mensaje2 = String.format("[%2d/%2d/%4d %2d:%2d] %s\n",
            hoy.getDate(), hoy.getMonth()+1, hoy.getYear()+1900, hoy.getHours(),
            hoy.getMinutes(), mensaje);
         mensajes.appendText(mensaje2+ "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }

    class HiloServidor extends Thread{

        public void run(){
            try {
                List<HiloAtencion> listaHilos = new ArrayList<HiloAtencion>();
                ServerSocket servidor = new ServerSocket(4500, 5);

                while (true) {
                    Socket cliente = servidor.accept();
                    Platform.runLater(new Runnable() {
                        public void run() {
                            agregarMensaje("Se ha conectado" + cliente);
                        }
                    });

                    HiloAtencion hilo = new HiloAtencion(cliente, listaHilos);
                    listaHilos.add(hilo);
                    hilo.start();

                }
            }catch (IOException e){

            }
        }
    }
    class HiloAtencion extends Thread{
        private Socket cliente;
        private BufferedReader lector;
        private BufferedWriter escritor;
        private List<HiloAtencion> listaHilos;
        private String apodo = "";


        public HiloAtencion(Socket cliente, List<HiloAtencion> listaHilos){
            this.cliente = cliente;
            this.listaHilos = listaHilos;

        }
        public boolean enviarMensaje(String mensaje){
            try {
                escritor.write(mensaje+"\r\n");
                escritor.flush();
                return true;
            }catch (IOException e){
                return false;
            }
        }
        public void run(){
            try {
                lector = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                escritor = new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream()));

                enviarMensaje("+ OK Bienvenido al servidor de mensaje");
                enviarMensaje("+ REQ Enviar su nombre de usuario y dar Enter");

                apodo = lector.readLine();

                boolean salir = false;
                while(!salir) {
                    String cad = lector.readLine();
                    Platform.runLater(new Runnable() {

                        public void run() {
                            agregarMensaje("Usuario: "+apodo+" Cadena recibida: "+ cad);
                        }
                    });
                    if (cad.equals("**SALIR**")){
                        salir = true;
                        break;
                    }
                    idMensaje ++;
                    String mensaje = String.format("%d,%s>%s", idMensaje,apodo,cad);
                    for(HiloAtencion conexion : listaHilos)
                        conexion.enviarMensaje(mensaje );

                }
                enviarMensaje("+ OK. Adios...");

                escritor.close();
                lector.close();
                cliente.close();
                listaHilos.remove(this);
            }catch (IOException e){

            }
        }
    }
}


