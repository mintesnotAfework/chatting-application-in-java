import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket server;
    private boolean done;
    ExecutorService threadPool;
    private ArrayList<connectionHandler> connections;
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;
    private String message;
    public Server(){

        connections= new ArrayList<>();
    }

    public void run(){
        try {
            System.out.println("...............The Server is running. at port 12345.............");
            server = new ServerSocket(33330);
            threadPool = Executors.newCachedThreadPool();
            done = false;
        }
       catch (IOException ex) {
                shutdown();
            }

        while (!done) {
            try {
                Socket client = server.accept();
                connectionHandler handler = new connectionHandler();
                connections.add(handler);
                threadPool.execute(handler);
            } catch (IOException e) {
                    shutdown(); 
            }
        }
    }

    public void broadcast(String message){

        for (connectionHandler ch: connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void  shutdown() {
        try{
            done =true;
            if(!server.isClosed()){
                server.close();
            }
            for(connectionHandler ch : connections){
                ch.shutdown();
            }
            threadPool.shutdownNow();
            connections.clear();
        }catch(Exception e){
            //ignore
        }
    }

    class  connectionHandler implements Runnable{
    
        @Override
        public void run(){
                try {
                    out = new PrintWriter((client.getOutputStream()),true);
                } catch (IOException e1) {
                   shutdown();
                }
                try {
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                } catch (IOException e1) {
                   shutdown();
                }

                out.println("enter a nickname: ");
                try {   
                
                    nickname = in.readLine();
                    while (nickname.isEmpty() || nickname.contains(" ")) {
                        out.println("Invalid nickname. Re-enter a nickname: ");
                        nickname = in.readLine();
                    }
                    System.out.println(nickname + " is connected!");
                    broadcast(nickname + " joined the chat!");
                    
                }catch(IOException e){
                    shutdown();
                }
                try{
                    while((message =in.readLine() )!= null) {
                        if (message.startsWith("/quit")) {
                            broadcast(nickname + " leave the connection!");
                            shutdown();
                        } else if (message.startsWith("/rename")) {
                            broadcast(nickname + " changes his name to " + (nickname = in.readLine()));
                            System.out.print(nickname + " is chaned his name!");
                            out.write(nickname + " changed successfully!");
                        }
                        else if(nickname.isEmpty()) {
                            out.write("null value, reenter correctly");
                        }
                        else {
                            broadcast(nickname +" : "+ message);
                        }
                    }

                }catch(IOException e){
                    shutdown();
                }
            }
        public void sendMessage(String message){
                out.println(message);
        }

        public void shutdown() {
            try {
                if(in != null){
                    this.in.close();
                }
                if(out !=null){
                    this.out.close();
                }
                if(!client.isClosed()){
                    client.close();
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

        public static void main(String[] args) {

            Server obj = new Server();
            obj.run();
        }
}
