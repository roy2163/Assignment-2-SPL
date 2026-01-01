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
    try {
        TiredThread worker = idleMinHeap.take();
        inFlight.incrementAndGet();

        Runnable wrappedTask = () -> {
            try {
                task.run();
            } finally {
                idleMinHeap.add(worker);
                inFlight.decrementAndGet();
            }
        };
        while (true) {
            try {
                worker.newTask(wrappedTask);
                break;
            } catch (IllegalStateException e) {
                if ("Worker is busy".equals(e.getMessage())) {
                    synchronized (worker) {
                        if (worker.isBusy()) {
                            try {
                                worker.wait();
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt(); 
                                throw new RuntimeException("Interrupted while waiting for worker", ie);
                            }
                        }
                    }
                } else {
                    idleMinHeap.add(worker);
                    inFlight.decrementAndGet();
                    throw e;
                }
            }
        }

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Executor interrupted", e);
    }
}
    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable task : tasks) {
            submit(task);
        }
    }

    public void shutdown() throws InterruptedException {
        for(TiredThread worker : workers){
            worker.shutdown();
            
        }
    }

    public synchronized String getWorkerReport() {
        StringBuilder report = new StringBuilder();
        for (TiredThread worker : workers) {
            report.append(String.format("Worker %d: Time Used = %d ns, Time Idle = %d ns, Fatigue = %.2f\n",
                    worker.getWorkerId(),
                    worker.getTimeUsed(),
                    worker.getTimeIdle(),
                    worker.getFatigue()));
        }
        return report.toString();
    }
}
