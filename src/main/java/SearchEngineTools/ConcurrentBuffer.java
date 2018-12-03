package SearchEngineTools;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class ConcurrentBuffer<T> {
    private Semaphore semaphoreEmpty;
    private Semaphore semaphoreFull;
    private ConcurrentLinkedQueue<T> concurrentLinkedQueue;

    public ConcurrentBuffer(int maxSize) {
        semaphoreEmpty =new Semaphore(0);
        semaphoreFull=new Semaphore(maxSize);
        concurrentLinkedQueue=new ConcurrentLinkedQueue<>();
    }

    public void add(T terms){
        try {
            semaphoreFull.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        concurrentLinkedQueue.add(terms);
        semaphoreEmpty.release();
    }

    public T get(){
        try {
            semaphoreEmpty.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        T pooled= concurrentLinkedQueue.poll();
        semaphoreFull.release();
        return pooled;
    }
}
