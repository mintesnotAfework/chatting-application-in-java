import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private String ipAddress;
    private Scanner input;
    public Client(){
        done = false;
        input = new Scanner(System.in);
    }
    private int port = 0;
    @Override
    public void run() {
       
            System.out.print("enter the ip address(between 1-255): ");
            ipAddress = "localhost";
                
            while(port >65535 || port < 1025){
                System.out.print("enter the port number: ");
                port = input.nextInt();
            }
            try {    
                client = new Socket(ipAddress, port);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
    
                InputHandler inputHandler = new InputHandler();
                Thread inputThread = new Thread(inputHandler);
                inputThread.start();
    
                String inMessage;
                while ((inMessage = in.readLine()) != null) {
                    System.out.print(inMessage);
                }
            } catch (IOException e) {
                    shutdown(); 
            }
        }

    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch(Exception e){
            //ignore
        }
    }

    class InputHandler implements  Runnable{
        private BufferedReader inreader = new BufferedReader(new InputStreamReader(System.in));
        private boolean done = false;

        @Override
        public void run() {
            while(!done){
                String inMessage;
                    try {
                        while((inMessage=in.readLine()) != null){
                                System.out.print(inMessage);
                        }
                    } catch (IOException e) {
                        shutdown();
                    }
                    try {
                        String message = inreader.readLine();

                        if(message.equals("/quit")){
                            out.println(message);
                            inreader.close();
                            shutdown();
                        }
                        else{
                            out.println(message);
                        }
                    } catch (IOException e) {
                        System.out.println("1");
                        shutdown();
                    }
            }
        }
    }

    public static void main(String[] args) {
        Client client =new Client();
        client.run();
    }
}