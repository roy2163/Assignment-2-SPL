package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        if (computationRoot == null) {
            throw new IllegalArgumentException("Computation root cannot be null");
        }
        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX){
            ComputationNode resolvableNode = computationRoot.findResolvable();
            if(resolvableNode == null) {
                throw new IllegalStateException("No resolvable node found, but computation is not complete");
            }
            loadAndCompute(resolvableNode);
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        if(node == null) {
            return;
        }
        List<Runnable> tasks;
        switch (node.getNodeType()) {
            case ADD:
                if(node.getChildren().size() != 2) {
                    throw new IllegalArgumentException("ADD node must have exactly 2 children");
                }
                if(node.getChildren().get(0).getNodeType() != ComputationNodeType.MATRIX ||
                   node.getChildren().get(1).getNodeType() != ComputationNodeType.MATRIX) {
                    throw new IllegalArgumentException("ADD node children must be of type MATRIX");
                }
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                rightMatrix.loadRowMajor(node.getChildren().get(1).getMatrix());
                tasks = createAddTasks();
                executeAndWait(tasks);
                break;
            case MULTIPLY:
                if(node.getChildren().size() != 2) {
                    throw new IllegalArgumentException("MULTIPLY node must have exactly 2 children");
                }
                if(node.getChildren().get(0).getNodeType() != ComputationNodeType.MATRIX ||
                   node.getChildren().get(1).getNodeType() != ComputationNodeType.MATRIX) {
                    throw new IllegalArgumentException("MULTIPLY node children must be of type MATRIX");
                }
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                rightMatrix.loadRowMajor(node.getChildren().get(1).getMatrix());
                tasks = createMultiplyTasks();
                executeAndWait(tasks);
                break;

            case NEGATE:
                if(node.getChildren().size() != 1) {
                    throw new IllegalArgumentException("NEGATE node must have exactly 1 child");
                }
                if(node.getChildren().get(0).getNodeType() != ComputationNodeType.MATRIX) {
                    throw new IllegalArgumentException("NEGATE node child must be of type MATRIX");
                }
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                tasks = createNegateTasks();
                executeAndWait(tasks);
                break;
            case TRANSPOSE:
                if(node.getChildren().size() != 1) {
                    throw new IllegalArgumentException("TRANSPOSE node must have exactly 1 child");
                }
                if(node.getChildren().get(0).getNodeType() != ComputationNodeType.MATRIX) {
                    throw new IllegalArgumentException("TRANSPOSE node child must be of type MATRIX");
                }
                leftMatrix.loadRowMajor(node.getChildren().get(0).getMatrix());
                tasks = createTransposeTasks();
                executeAndWait(tasks);

                break;
            default:
                throw new IllegalArgumentException("Computation root cannot be a matrix");
        }
        node.resolve(leftMatrix.readRowMajor());

    }

    private void executeAndWait(List<Runnable> tasks) {
        CountDownLatch latch = new CountDownLatch(tasks.size());
        
        List<Runnable> wrappedTasks = new ArrayList<>();
        for (Runnable task : tasks) {
            wrappedTasks.add(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        executor.submitAll(wrappedTasks);
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for tasks", e);
        }
    }
    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        List<Runnable> tasks = new ArrayList<>();
        if(leftMatrix.getOrientation()!= VectorOrientation.ROW_MAJOR){
            leftMatrix.loadRowMajor(leftMatrix.readRowMajor());
        }
        if(rightMatrix.getOrientation()!= VectorOrientation.ROW_MAJOR){
            rightMatrix.loadRowMajor(rightMatrix.readRowMajor());
        }
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add (() -> { leftMatrix.get(rowIndex).add(rightMatrix.get(rowIndex));} );
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        List<Runnable> tasks = new ArrayList<>();
        if(leftMatrix.getOrientation()!= VectorOrientation.ROW_MAJOR){
            leftMatrix.loadRowMajor(leftMatrix.readRowMajor());
        }
        for(int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add (() -> { leftMatrix.get(rowIndex).vecMatMul(rightMatrix);});
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> tasks = new ArrayList<>();
        if(leftMatrix.getOrientation()!= VectorOrientation.ROW_MAJOR){
            leftMatrix.loadRowMajor(leftMatrix.readRowMajor());
        }
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add (() -> { leftMatrix.get(rowIndex).negate();} );
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int rowIndex = i;
            tasks.add (() -> { leftMatrix.get(rowIndex).transpose();} );
        }
        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return executor.getWorkerReport();
    }
    public void shutdown() throws InterruptedException {
        executor.shutdown();
    }
}
