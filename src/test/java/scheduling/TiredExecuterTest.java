package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class TiredExecutorTest {

    private TiredExecutor executor;
    private final int numThreads = 4;

    @BeforeEach
    void setUp() {
        executor = new TiredExecutor(numThreads);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdown();
    }

    @Test
    void testSubmitSingleTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        executor.submit(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Task should be executed");
    }

    @Test
    void testSubmitAllTasks() throws InterruptedException {
        int numTasks = 10;
        CountDownLatch latch = new CountDownLatch(numTasks);
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            tasks.add(latch::countDown);
        }

        executor.submitAll(tasks);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "All tasks in batch should complete");
    }

    @Test
    void testFairnessAndFatigueDistribution() throws InterruptedException {
        int numTasks = 20;
        CountDownLatch latch = new CountDownLatch(numTasks);
        
        for (int i = 0; i < numTasks; i++) {
            executor.submit(() -> {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                latch.countDown();
            });
        }
        latch.await();

        String report = executor.getWorkerReport();
        for (int i = 0; i < numThreads; i++) {
            assertTrue(report.contains("Worker " + i), "Report should contain info for all workers");
        }
    }

@Test
void testBlockingWhenNoWorkersAvailable() throws InterruptedException {
    CountDownLatch blockLatch = new CountDownLatch(numThreads);
    CountDownLatch finishLatch = new CountDownLatch(numThreads);

    for (int i = 0; i < numThreads; i++) {
        executor.submit(() -> {
            blockLatch.countDown();
            try { finishLatch.await(); } catch (InterruptedException e) {}
        });
    }

    blockLatch.await();

    AtomicInteger extraTaskCompleted = new AtomicInteger(0);
    CountDownLatch extraTaskLatch = new CountDownLatch(1);

    Thread submitterThread = new Thread(() -> {
        executor.submit(() -> {
            extraTaskCompleted.incrementAndGet();
            extraTaskLatch.countDown();
        });
    });

    submitterThread.start();
    
    Thread.sleep(100); 
    assertEquals(0, extraTaskCompleted.get(), "Task should be blocked/queued as all workers are busy");

    for(int i=0; i<numThreads; i++) finishLatch.countDown(); 

    boolean completedInTime = extraTaskLatch.await(2, TimeUnit.SECONDS);
    
    assertTrue(completedInTime, "Extra task should have completed after workers were freed");
    assertEquals(1, extraTaskCompleted.get(), "Task count should be updated");
    
    submitterThread.join(); 


    }
}