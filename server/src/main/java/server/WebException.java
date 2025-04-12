package server;

public class WebException extends Exception {
    public int getStatusCode() {
        return 500;
    }

    WebException(String message) {
        super(message);
    }

    public WebException(Exception e) {
        super(e.getMessage());
    }

    public static class UnauthorizedRequestException extends WebException {
        public int getStatusCode() {
            return 401;
        }

        public UnauthorizedRequestException() {
            super("Error: unauthorized");
        }
    }

    public static class AlreadyTakenException extends WebException {
        public int getStatusCode() {
            return 403;
        }

        public AlreadyTakenException() {
            super("Error: already taken");
        }
    }

    public static class BadRequestException extends WebException {
        public int getStatusCode() {
            return 400;
        }

        public BadRequestException() {
            super("Error: bad request");
        }
    }
}