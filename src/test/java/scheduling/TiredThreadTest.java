package scheduling; // תיקון שגיאת הכתיב

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
// הסרנו את ה-import scheduling.TiredThread

import static org.junit.jupiter.api.Assertions.*;

class TiredThreadTest {

    private TiredThread worker;
    private final double fatigueFactor = 1.2;

    @BeforeEach
    void setUp() {
        worker = new TiredThread(1, fatigueFactor);
        worker.start();
    }

    @Test
    void testInitialState() {
        assertEquals(1, worker.getWorkerId());
        assertFalse(worker.isBusy());
        assertEquals(0.0, worker.getFatigue(), "Initial fatigue should be zero");
    }

    @Test
    void testTaskExecutionAndFatigue() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);

        worker.newTask(() -> {
            startLatch.countDown();
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            finishLatch.countDown();
        });

        startLatch.await(1, TimeUnit.SECONDS); 
        
        assertTrue(worker.isBusy(), "Worker should be busy while executing task");
        
        finishLatch.await();
    }

    @Test
    void testNewTaskWhileBusyThrowsException() {
        worker.newTask(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        });

        assertThrows(IllegalStateException.class, () -> {
            worker.newTask(() -> {});
        }, "Should not accept a task while busy");
    }

    @Test
    void testCompareToBasedOnFatigue() throws InterruptedException {
        TiredThread worker2 = new TiredThread(2, 0.5);
        worker2.start();

        // גורמים לעובד 1 לעבוד כדי שיצבור עייפות
        CountDownLatch latch = new CountDownLatch(1);
        worker.newTask(latch::countDown);
        latch.await();
        Thread.sleep(50);

        assertTrue(worker.compareTo(worker2) > 0, "More fatigued worker should be 'greater' in priority queue");
        
        worker2.shutdown();
    }

    @Test
    void testShutdown() throws InterruptedException {
        worker.shutdown();
        worker.join(1000);
        assertFalse(worker.isAlive(), "Worker thread should exit after shutdown");
        
        assertThrows(IllegalStateException.class, () -> worker.newTask(() -> {}), 
            "Should not accept tasks after shutdown");
    }
}