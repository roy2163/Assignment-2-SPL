package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      OutputWriter writer = new OutputWriter();
      InputParser parser = new InputParser();
      ComputationNode root = null;

      int numberOfThreads = Integer.parseInt(args[0]);
      String InputPath = args[1];
      String OutputPath = args[2];
      LinearAlgebraEngine lae = null;
      try{
          root = parser.parse(InputPath);
          lae = new LinearAlgebraEngine(numberOfThreads);
          ComputationNode resultNode = lae.run(root);
          double[][] resultMatrix = resultNode.getMatrix();
          writer.write(resultMatrix, OutputPath);
      } catch (Exception e){
          writer.write(e.getMessage(), OutputPath);
      }
      finally {
          try{
            lae.shutdown();          
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
      }


    }
}