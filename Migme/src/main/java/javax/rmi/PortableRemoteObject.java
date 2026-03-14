package javax.rmi;

public class PortableRemoteObject {

    protected PortableRemoteObject() {
    }

    public static void exportObject(java.rmi.Remote obj) throws java.rmi.RemoteException {
    }

    public static void unexportObject(java.rmi.Remote obj) throws java.rmi.NoSuchObjectException {
    }

    public static Object narrow(Object narrowFrom, Class<?> narrowTo) throws ClassCastException {
        if (narrowFrom == null) return null;
        if (narrowTo.isInstance(narrowFrom)) return narrowFrom;
        throw new ClassCastException("Cannot narrow " + narrowFrom.getClass() + " to " + narrowTo);
    }

    public static void connect(java.rmi.Remote target, java.rmi.Remote source) throws java.rmi.RemoteException {
    }
}
