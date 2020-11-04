import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.locks.StampedLock;

public class Almacen {
    //Datos
    private final ArrayList<Integer> listaproduct = new ArrayList<>();

    //Tools
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final StampedLock stampedLock = new StampedLock();

    public void updateStock(int idProduct) {
        long stamp = stampedLock.writeLock();
        try {
            addProduct(idProduct);
        } finally {
            stampedLock.unlockWrite(stamp);
        }

    }

    private void addProduct(int idProduct) {
        listaproduct.add(idProduct);
        System.out.printf("%s -> Se añadio el producto %d\n", LocalTime.now().format(dateTimeFormatter), idProduct);
    }

    public void seeStock(int idProduct) {
        long stamp = stampedLock.tryOptimisticRead();
        consultStock(idProduct, stamp);

    }

    private void consultStock(int idProduct, long stamp) {
        int stock;
        if (stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                stock = countStock(idProduct);
                System.out.printf("%s -> Producto %d# %d Hay %s\n", LocalTime.now().format(dateTimeFormatter), idProduct, stock, stock == 0 || stock > 1 ? "Productos" : "Prodcuto ");
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
    }

    private int countStock(int idProduct) {
        int result = 0;
        for (int product : listaproduct) {
            if (product == idProduct) result++;
        }
        return result;

    }
}
