import server.Server;

public class Main {
    public static void main(String[] args) {
        Server sparkServer = new Server();
        sparkServer.run(8080);
    }
}