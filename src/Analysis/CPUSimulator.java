/*
Copyright 2022 Joshua Henderson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package Analysis;
import static Analysis.SchedulingAlgorithms.*; // import all the methods from SchedulingAlgorithms

/* =====================================================================================
// CPU Simulator for analyzing the scheduling algorithms
   ===================================================================================== */
public class CPUSimulator {
    public static boolean demo = false; //True iff doing a demo (should choose small N for analyzeAll)
    
    public static void main(String [] args) throws Exception{
        if(demo) analyzeAll(1, 5);  // Analyze all the processes
        else analyzeAll(1, 100000);
    }
    
/* =====================================================================================
// Pre:  passed number of epochs > 0 and N > 0
// Post: returns nothing
   ===================================================================================== */
    public static void analyzeAll(int epoch, int N){
        // Initialize the double arrays for results for each algorithm
        double [] result = {0,0,0}, result2={0,0,0}, result3={0,0,0}, result4={0,0,0}, result5={0,0,0}, result6={0,0,0}, curr={0,0,0};
        
        for(int e=1; e<= epoch; e++){                       // run for the specified number of epochs
            System.out.println("Making Processes...");      // output for keeping track of the program
            int [][][] p1;  // for new set of processes
            if(demo) p1 = makeNewProcess(N, 1, 15, 10, 6);           // demo
            else p1 = makeNewProcess(N, 250, 5000, 50000, 6);        // no demo
            
            //===============================DEBUGGING CODE=========================//
            if(demo){
                System.out.println("Process List: ");
                for(int c: debugCols) System.out.print(columns[c]+"\t");
                System.out.println("");
                for(int r=0; r<N; r++){
                    for(int c: debugCols) System.out.print(p1[0][c][r]+"\t");
                    System.out.println("");
                }
            }
            //======================================================================//
                        
            if(demo){
                System.out.println("\nFCFS");
                for(int c: debugCols) System.out.print(columns[c]+"\t");
                System.out.println("");
                curr = FCFS(p1[0]);                     // run the FCFS algorithm
                for(int i=0; i<3; i++) result[i] += curr[i];    // add to the FCFS aggregate results
                
                System.out.println("\nRR (2)");
                for(int c: debugCols) System.out.print(columns[c]+"\t");
                System.out.println("");
                curr = RR(p1[1], 2);                  // run the RR algorithm with time quantum size 100
                for(int i=0; i<3; i++) result2[i] += curr[i];   // add to the aggregate results
                
                System.out.println("\nRR (5)");
                for(int c: debugCols) System.out.print(columns[c]+"\t");
                System.out.println("");
                curr = RR(p1[2], 5);                  // run the RR algorithm with time quantum size 250
                for(int i=0; i<3; i++) result3[i] += curr[i];   // add to the aggregate results
                
                System.out.println("\nRR (10)");
                for(int c: debugCols) System.out.print(columns[c]+"\t");
                System.out.println("");
                curr = RR(p1[3], 10);                 // run the RR algorithm with time quantum size 1000
                for(int i=0; i<3; i++) result4[i] += curr[i];   // add to the aggregate results
                
                System.out.println("\nSJF");
                for(int c: debugCols) System.out.print(columns[c]+"\t");
                System.out.println("");
                curr = SJF(p1[4]);                      // run the SJF algorithm
                for(int i=0; i<3; i++) result5[i] += curr[i];   // add to the aggregate results
                
                System.out.println("\nSRTF");
                for(int c: debugCols) System.out.print(columns[c]+"\t");
                System.out.println("");
                curr = SRTF(p1[5]);                     // run the SRTF algorithm
                for(int i=0; i<3; i++) result6[i] += curr[i];   // add to the aggregate results
            }else{
                System.out.println("Epoch "+e+"...");
                System.out.println("FCFS");
                curr = FCFS(p1[0]);                     // run the FCFS algorithm
                for(int i=0; i<3; i++) result[i] += curr[i];    // add to the FCFS aggregate results
                System.out.println("RR (100)");
                curr = RR(p1[1], 100);                  // run the RR algorithm with time quantum size 100
                for(int i=0; i<3; i++) result2[i] += curr[i];   // add to the aggregate results
                System.out.println("RR (250)");
                curr = RR(p1[2], 250);                  // run the RR algorithm with time quantum size 250
                for(int i=0; i<3; i++) result3[i] += curr[i];   // add to the aggregate results
                System.out.println("RR (1000)");
                curr = RR(p1[3], 1000);                 // run the RR algorithm with time quantum size 1000
                for(int i=0; i<3; i++) result4[i] += curr[i];   // add to the aggregate results
                System.out.println("SJF");
                curr = SJF(p1[4]);                      // run the SJF algorithm
                for(int i=0; i<3; i++) result5[i] += curr[i];   // add to the aggregate results
                System.out.println("SRTF");
                curr = SRTF(p1[5]);                     // run the SRTF algorithm
                for(int i=0; i<3; i++) result6[i] += curr[i];   // add to the aggregate results
            }
            
        }
        
        // Divide all aggregate numbers by the number of epochs to get the averages
        for(int i=0; i<3; i++) result[i] /= epoch;
        for(int i=0; i<3; i++) result2[i] /= epoch;
        for(int i=0; i<3; i++) result3[i] /= epoch;
        for(int i=0; i<3; i++) result4[i] /= epoch;
        for(int i=0; i<3; i++) result5[i] /= epoch;
        for(int i=0; i<3; i++) result6[i] /= epoch;
        
        
        // Print out the results
        System.out.println("\nFCFS\n");
        System.out.println("Idle (ms): "+result[0]+"\tTotal (ms): "+result[1]);
        System.out.println("Utilization: "+(1-(result[0]/result[1])));
        System.out.println("Avg Wait: "+(result[2]));
        
        if(demo) System.out.println("\nRR (2)\n");
        else System.out.println("\nRR (100)\n");
        System.out.println("Idle (ms): "+result2[0]+"\tTotal (ms): "+result2[1]);
        System.out.println("Utilization: "+(1-(result2[0]/result2[1])));
        System.out.println("Avg Wait: "+(result2[2]));
        
        if(demo) System.out.println("\nRR (5)\n");
        else System.out.println("\nRR (250)\n");
        System.out.println("Idle (ms): "+result3[0]+"\tTotal (ms): "+result3[1]);
        System.out.println("Utilization: "+(1-(result3[0]/result3[1])));
        System.out.println("Avg Wait: "+(result3[2]));
        
        if(demo) System.out.println("\nRR (10)\n");
        else System.out.println("\nRR (1000)\n");
        System.out.println("Idle (ms): "+result4[0]+"\tTotal (ms): "+result4[1]);
        System.out.println("Utilization: "+(1-(result4[0]/result4[1])));
        System.out.println("Avg Wait: "+(result4[2]));
        
        System.out.println("\nSJF\n");
        System.out.println("Idle (ms): "+result5[0]+"\tTotal (ms): "+result5[1]);
        System.out.println("Utilization: "+(1-(result5[0]/result5[1])));
        System.out.println("Avg Wait: "+(result5[2]));
        
        System.out.println("\nSRTF\n");
        System.out.println("Idle (ms): "+result6[0]+"\tTotal (ms): "+result6[1]);
        System.out.println("Utilization: "+(1-(result6[0]/result6[1])));
        System.out.println("Avg Wait: "+(result6[2]));
    }
}
