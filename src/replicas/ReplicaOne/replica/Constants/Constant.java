package replicas.ReplicaOne.replica.Constants;

public class Constant {
    private static String ATWServer = "rmi://localhost/atw";
    private static String OUTServer = "rmi://localhost/out";
    private static String VERServer = "rmi://localhost/ver";

    public static String getATWServer() {
        return ATWServer;
    }

    public static String getOUTServer() {
        return OUTServer;
    }

    public static String getVERServer() {
        return VERServer;
    }
}
