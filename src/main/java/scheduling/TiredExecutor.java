package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads];
        Random rnd = new Random();
        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = rnd.nextDouble() + 0.5;
            TiredThread worker = new TiredThread(i, fatigueFactor);
            workers[i] = worker;
            idleMinHeap.add(worker);
            worker.start();
        }
        inFlight.set(0);
    }

    public void submit(Runnable task) {
        // TODO
    }

    public void submitAll(Iterable<Runnable> tasks) {
        while(tasks.iterator().hasNext()){
            submit(tasks.iterator().next());
        }
    }

    public void shutdown() throws InterruptedException {
        for(TiredThread worker : workers){
            worker.shutdown();
            
        }
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        return null;
    }
}
