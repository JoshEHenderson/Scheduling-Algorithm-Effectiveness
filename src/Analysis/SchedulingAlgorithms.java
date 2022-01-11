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
import java.util.*;

/* =====================================================================================
// A class for defining all methods needed for analyzing scheduling algorithms
// These methods are used in the CPU simulator to analyze the various scheduling algorithms
   ===================================================================================== */
public class SchedulingAlgorithms {
    public static boolean demo = false; //True iff doing a demo
    //Names for all the columns:
    // Index, Process ID, Burst Time, Arrival Time, 
    // Burst Time(milliseconds), Arrival Time(milliseconds)
    // Remaining Time, Completion Time, Turnaround Time, Waiting Time
    public static String [] columns = {"PID", "BT", "AT", "BTms", "ATms", "RT", "CT", "TaT", "WT"};
    public static int [] debugCols = {0, 5, 2};//Column numbers used when printing (PID, BT, AT, RT)
    public static HashMap<String, Integer> colIdx = new HashMap<>(); //For easy access of the number of each column
    static int numCt = 0; //to make distinct time instances
    
/* =====================================================================================
// TimeInst is used whenever we are storing arrival or remaining times in a treeset
// Since multiple processes can have the same arrival time and/or remaining time,
//   we need to store multiple instances of the same time.
// TimeInst allows us to put times into a treeset and have them sorted first by the time
//   value and then by the order in which it was processed (as numCt strictly increases)
   ===================================================================================== */ 
    public static class TimeInst implements Comparable<TimeInst>{
        int time, num;                                   //Time and unique number
        public TimeInst(int t){time = t; num = numCt++;} //Initialize time and num
        @Override
        public int compareTo(TimeInst o) {
            if(time != o.time) return time-o.time;       //Compare first by time
            return num-o.num;                            //then by order of processing
        }
    }
    
    public static void main(String [] args) throws Exception{
        //This is blank because all work is done in the CPU Simulator
    }
    
/* =====================================================================================
// Pre: is passed length>0, maxBurst>0 and maxArrival>0;
// Post: return a 3-D int array with numProcesses number of copies of the same 2-D array
//         The first dimension is the number of the copy (0..numProcesses-1)
//         The second dimension is the value of the column attribute
//             columns are determined by the String array columns and the hashMap colIdx
//         The third dimension is to delineate processes by rows (0..len-1)
   ===================================================================================== */
    public static int [][][] makeNewProcess(int len, int minBurst, int maxBurst, int maxArrival, int numProcesses){
        Random rand = new Random(); //For randomly generating process information
        if(colIdx.isEmpty()) for(int i=0; i<columns.length; i++) colIdx.put(columns[i], i);
        //3-D int array for processes
        int [][][] process = new int[numProcesses][columns.length][len];
        for(int i=0; i<len; i++){   //for each process
            int BT = rand.nextInt(maxBurst-minBurst)+minBurst;  //Get a random burst time
            int AT = rand.nextInt(maxArrival);                  //Get a random arrival time
            for(int p=0; p<numProcesses; p++){  //Initialize this process for each copy
                process[p][colIdx.get("PID")][i] = i;
                process[p][colIdx.get("BT")][i] = BT;
                process[p][colIdx.get("AT")][i] = AT;
                process[p][colIdx.get("RT")][i] = process[p][colIdx.get("BT")][i];
            }
        }
        return process;
    }
    
/* =====================================================================================
// Pre: passed a process with values>0 in the columns pid, bt, at, and rt and values == 0 in all other columns
// Post: returns Double array containing: [idle time, total time, average wait time]
// 
// About: First-Come-First-Serve (FCFS) is a non-preemptive scheduling algorithm that follows 
//          a FIFO queue.
//        Processes that arrive first get processed first and the algorithm follows this until
//          all processes are completed.
   ===================================================================================== */
    public static double [] FCFS(int [][] process){
        TreeMap <TimeInst, Integer> tree = new TreeMap<>(); //For efficiently storing sorted arrival times
        for(int i=0; i<process[0].length; i++) tree.put(new TimeInst(process[colIdx.get("AT")][i]), process[colIdx.get("PID")][i]);
        //System.out.println(tree.size()); // Debugging code
        
        Queue <Integer> q = new LinkedList<>(); //Process queue
        
        double idle = 0.0, avgWait = 0.0;           // Accumulative counters
        long currClock = 0, idx = 0;                 // currClock is for "running" the processes; idx is for debugging
        long start = System.currentTimeMillis();    // get the start time
        TimeInst next = tree.firstKey();            // next process to arrive is the smallest in the tree
        int nextArrival = next.time, nextIndex = tree.remove(next); // get arrival time of next process
        
        //while there is still a process to be run or retrieved
        while(!q.isEmpty() || !tree.isEmpty() || nextArrival>=0){
            if(q.isEmpty()){ // need to wait for next process because it hasn't arrived yet
                double idleTimer = System.currentTimeMillis(); // this is idle time
                while(currClock<nextArrival){currClock++;}     // increment the clock to the next arrival time
                idle += System.currentTimeMillis()-idleTimer;  // store the idle time spent
                q.add(nextIndex);                              // add the next index to the queue
                process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start); //store the arrival time in ms
                if(!tree.isEmpty()){                // if there are still processes to be found
                    next = tree.firstKey();         // get the next ATInst
                    nextArrival = next.time;        // update next arrival time
                    nextIndex = tree.remove(next);  // get the next index
                }else nextArrival = -1;             // mark that the last item has been found
            }
            int curr = q.remove();                  // get the next process
            
            //===============================DEBUGGING CODE=========================//
            if(demo){ for(int c: debugCols) System.out.print(process[c][curr]+"\t");
                        System.out.println(""); }
            //======================================================================//
            
            long burstSt = System.currentTimeMillis();  // start of the burst in ms
            while(process[colIdx.get("RT")][curr]>0){   // while the process isn't done
                process[colIdx.get("RT")][curr] --;     //  decrement the remaining process time
                while(currClock == nextArrival){        // if we have reached the next arrival time
                    q.add(nextIndex);                   //  add that process to the queue
                                                        //  initialize its arrival time in ms
                    process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start);
                    if(!tree.isEmpty()){                // get the next process (see lines 95-99)
                        next = tree.firstKey();
                        nextArrival = next.time;
                        nextIndex = tree.remove(next);
                    }else nextArrival = -1;
                }
                currClock++;    // increment the current clock
            }
            
            // finalize metrics for this process
            process[colIdx.get("BTms")][curr] = (int)(System.currentTimeMillis()-burstSt);
            process[colIdx.get("CT")][curr] = (int)(System.currentTimeMillis()-start);
            process[colIdx.get("TaT")][curr] = process[colIdx.get("CT")][curr]-process[colIdx.get("ATms")][curr];
            process[colIdx.get("WT")][curr] = process[colIdx.get("TaT")][curr]-process[colIdx.get("BTms")][curr];
            // add this process's waiting time to the aggregate
            avgWait += process[colIdx.get("WT")][curr];
        }
        /*
        //===============================DEBUGGING CODE=========================//
        for(String s: columns) System.out.print(s+"\t");
        System.out.println("");
        for(int r=0; r<process[0].length; r++){
            for(int c=0; c<columns.length; c++) System.out.print(process[c][r]+"\t");
            System.out.println("");
        }
        int ct = 0;
        for(int i: process[colIdx.get("RT")]) if(i>0){ct++; System.out.println(i);}
        System.out.println(ct+" Not Done\n");
        */
        //======================================================================//
        // Return the idle time, total time, and average wait time
        double [] result = {idle, (double)(System.currentTimeMillis()-start), avgWait/process[0].length};
        return result;
    }
    
    
/* =====================================================================================
// Pre:  passed a process with values>0 in the columns pid, bt, at, and rt 
//          and values == 0 in all other columns
//       passed a time quantum for the round robin scheduling that is > 0
// Post: returns Double array containing: [idle time, total time, average wait time]
// 
// About: Round Robin (RR) is a preemptive scheduling algorithm that follows a FIFO queue which
//          allows processes to run for a specified quantum, or the remaining time of the process,
//          whichever comes first.
//        At the end of this quantum, if the process is not completed, it is preempted and moved to
//          the back of the queue.
//        This continues until all processes have run to completion.
   ===================================================================================== */
    public static double [] RR(int [][] process, long quant){
        TreeMap <TimeInst, Integer> tree = new TreeMap<>(); //For efficiently storing sorted arrival times
        for(int i=0; i<process[0].length; i++) tree.put(new TimeInst(process[colIdx.get("AT")][i]), process[colIdx.get("PID")][i]);
        //System.out.println(tree.size()); //Debug code
        
        Queue <Integer> q = new LinkedList<>();     //Process queue
        
        double idle = 0.0, avgWait = 0.0;           // Accumulative counters
        long currClock = 0, idx = 0;                // currClock is for "running" the processes; idx is for debugging
        long start = System.currentTimeMillis();    // get the start time
        TimeInst next = tree.firstKey();            // next process to arrive is the smallest in the tree
        int nextArrival = next.time, nextIndex = tree.remove(next); // get arrival time of next process
        
        //while there is still a process to be run or retrieved
        while(!q.isEmpty() || !tree.isEmpty() || nextArrival>=0){ 
            //if(idx%100 == 0) System.out.println(idx); // Debug Code
            if(q.isEmpty()){ // need to wait for next process because it hasn't arrived yet
                double idleTimer = System.currentTimeMillis(); // this is idle time
                while(currClock<nextArrival){currClock++;}     // increment the clock to the next arrival time
                idle += System.currentTimeMillis()-idleTimer;  // store the idle time spent
                q.add(nextIndex);                              // add the next index to the queue
                process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start); //store the arrival time in ms
                if(!tree.isEmpty()){                  // if there are still processes to be found
                    next = tree.firstKey();           // get the next ATInst
                    nextArrival = next.time;          // update next arrival time
                    nextIndex = tree.remove(next);    // get the next index
                }else nextArrival = -1;               // mark that the last item has been found
            }
            int curr = q.remove();                      // get the next process
            
            //===============================DEBUGGING CODE=========================//
            if(demo){ for(int c: debugCols) System.out.print(process[c][curr]+"\t");
                        System.out.println(""); }
            //======================================================================//
            long clockSt = currClock;                    // clock at the start of the process
            long burstSt = System.currentTimeMillis();   // start of the burst in ms
            
            // while the process isn't done and we haven't reached the end of the quantum
            while(process[colIdx.get("RT")][curr]>0 && (currClock-clockSt)<quant){
                process[colIdx.get("RT")][curr] --;     // decrement the remaining time
                while(currClock == nextArrival){        // if we reach the next process...
                    q.add(nextIndex);                       // add it to the queue
                    process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start);
                    if(!tree.isEmpty()){                // get next process (see lines 190-194)
                        next = tree.firstKey();
                        nextArrival = next.time;
                        nextIndex = tree.remove(next);
                    }else nextArrival = -1;
                }
                currClock++;    // increment currClock
            }
            // add this burst time to the time taken for the burst
            process[colIdx.get("BTms")][curr] += (int)(System.currentTimeMillis()-burstSt);
            
            
            if(process[colIdx.get("RT")][curr] == 0){ // if the process is done, finalize the metrics
                process[colIdx.get("CT")][curr] = (int)(System.currentTimeMillis()-start);
                process[colIdx.get("TaT")][curr] = process[colIdx.get("CT")][curr]-process[colIdx.get("ATms")][curr];
                process[colIdx.get("WT")][curr] = process[colIdx.get("TaT")][curr]-process[colIdx.get("BTms")][curr];
                avgWait += process[colIdx.get("WT")][curr]; // add wait time to aggregate
                idx++;
            }
            else q.add(curr); // else, add it back into the queue
        }
        
        /*
        //===============================DEBUGGING CODE=========================//
        for(String s: columns) System.out.print(s+"\t");
        System.out.println("");
        for(int r=0; r<process[0].length; r++){
            for(int c=0; c<columns.length; c++) System.out.print(process[c][r]+"\t");
            System.out.println("");
        }
        int ct = 0;
        for(int i: process[colIdx.get("RT")]) if(i>0){ct++; System.out.println(i);}
        System.out.println(ct+" Not Done\n");
        */
        //======================================================================//
        double [] result = {idle, (double)(System.currentTimeMillis()-start), avgWait/process[0].length};
        return result;
    }
    
/* =====================================================================================
// Pre: passed a process with values>0 in the columns pid, bt, at, and rt 
//         and values == 0 in all other columns
// Post: returns Double array containing: [idle time, total time, average wait time]
// 
// About: Shortest-Job-First (SJF) is a non-preemptive scheduling algorithm that runs the
//          process with the shortest burst time first.
//        SJF picks the shortest job out of the processes that have arrived and runs that until
//          completion
//        It continues in this way until all processes have been run.
   ===================================================================================== */
    public static double [] SJF(int [][] process){
        TreeMap <TimeInst, Integer> tree = new TreeMap<>(); //For efficiently storing sorted arrival times
        for(int i=0; i<process[0].length; i++) tree.put(new TimeInst(process[colIdx.get("AT")][i]), process[colIdx.get("PID")][i]);
        //Process queue, sorted by burst time then arrival time, then process ID
        TreeMap <TimeInst, Integer> currQ = new TreeMap<>();
        
        double idle = 0.0, avgWait = 0.0;           // Accumulative counters
        long currClock = 0;                           // currClock is for "running" the processes; idx is for debugging
        long start = System.currentTimeMillis();    // get the start time
        TimeInst next = tree.firstKey();               // next process to arrive is the smallest in the tree
        int nextArrival = next.time, nextIndex = tree.remove(next); // get arrival time of next process
        
        //while there is still a process to be run or retrieved
        while(!currQ.isEmpty() || !tree.isEmpty() || nextArrival>=0){ 
            if(currQ.isEmpty()){ // need to wait for next process because it hasn't arrived yet
                double idleTimer = System.currentTimeMillis(); // this is idle time
                while(currClock<nextArrival){currClock++;}     // increment the clock to the next arrival time
                idle += System.currentTimeMillis()-idleTimer;  // store the idle time spent
                currQ.put(new TimeInst(process[colIdx.get("RT")][nextIndex]), nextIndex); // add the next index to the queue
                process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start); //store the arrival time in ms
                if(!tree.isEmpty()){                  // if there are still processes to be found
                    next = tree.firstKey();           // get the next ATInst
                    nextArrival = next.time;          // update next arrival time
                    nextIndex = tree.remove(next);    // get the next index
                }else nextArrival = -1;               // mark that the last item has been found
            }
            int curr = currQ.remove(currQ.firstKey()); // get the next process
            
            //===============================DEBUGGING CODE=========================//
            if(demo){ for(int c: debugCols) System.out.print(process[c][curr]+"\t");
                        System.out.println(""); }
            //======================================================================//
            long burstSt = System.currentTimeMillis();  // start of the burst in ms
            while(process[colIdx.get("RT")][curr]>0){   // while this process is not done
                process[colIdx.get("RT")][curr] --;     // decrement the remaining time for the process
                while(currClock == nextArrival){        // if we reach the next process...
                    currQ.put(new TimeInst(process[colIdx.get("RT")][nextIndex]), nextIndex); // add it to the queue
                    process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start);
                    if(!tree.isEmpty()){                // get next process (see lines 280-284)
                        next = tree.firstKey();
                        nextArrival = next.time;
                        nextIndex = tree.remove(next);
                    }else nextArrival = -1;
                }
                currClock++;    // increment currClock
            }
                // finalize the metrics
                process[colIdx.get("BTms")][curr] += (int)(System.currentTimeMillis()-burstSt);
                process[colIdx.get("CT")][curr] = (int)(System.currentTimeMillis()-start);
                process[colIdx.get("RT")][curr] = 0;
                process[colIdx.get("TaT")][curr] = process[colIdx.get("CT")][curr]-process[colIdx.get("ATms")][curr];
                process[colIdx.get("WT")][curr] = process[colIdx.get("TaT")][curr]-process[colIdx.get("BTms")][curr];
                //add this process's waiting time to the aggregate
                avgWait += process[colIdx.get("WT")][curr];
        }
        
        /*
        //===============================DEBUGGING CODE=========================//
        for(String s: columns) System.out.print(s+"\t");
        System.out.println("");
        for(int r=0; r<process[0].length; r++){
            for(int c=0; c<columns.length; c++) System.out.print(process[c][r]+"\t");
            System.out.println("");
        }
        int ct = 0;
        for(int i: process[colIdx.get("RT")]) if(i>0){ct++; System.out.println(i);}
        System.out.println(ct+" Not Done\n");
        */
        //======================================================================//
        double [] result = {idle, (double)(System.currentTimeMillis()-start), avgWait/process[0].length};
        return result;
    }
    
/* =====================================================================================
// Pre: passed a process with values>0 in the columns pid, bt, at, and rt 
//         and values == 0 in all other columns
// Post: returns Double array containing: [idle time, total time, average wait time]
// 
// About: Shortest-Remaining-Time-First (SRTF) is a preemptive scheduling algorithm that
//          runs the process with the shortest remaining time first.
//        When a process arrives in the queue, if its burst time is shorter than the remaining
//          time of the process currently being run, the scheduler preempts the current process.
//        If a process completes without being preempted, the process with the shortest remaining
//          time is chosen to be the next process.
//        The SRTF scheduler continues in this way until all process have run to completion.
   ===================================================================================== */
    public static double [] SRTF(int [][] process){
        TreeMap <TimeInst, Integer> tree = new TreeMap<>(); //For efficiently storing sorted arrival times
        for(int i=0; i<process[0].length; i++) tree.put(new TimeInst(process[colIdx.get("AT")][i]), process[colIdx.get("PID")][i]);
        
        //Process queue, sorted by remaining time then arrival time, then process ID
        TreeMap <TimeInst, Integer> currQ = new TreeMap<>();
        
        double idle = 0.0, avgWait = 0.0;        // Accumulative counters
        long currClock = 0;                      // currClock is for "running" the processes; idx is for debugging
        long start = System.currentTimeMillis(); // get the start time
        TimeInst next = tree.firstKey();         // next process to arrive is the smallest in the tree
        int nextArrival = next.time, nextIndex = tree.remove(next); // get arrival time of next process
        
        //while there is still a process to be run or retrieved
        while(!currQ.isEmpty() || !tree.isEmpty() || nextArrival>=0){ 
            if(currQ.isEmpty()){ // need to wait for next process because it hasn't arrived yet
                double idleTimer = System.currentTimeMillis(); // this is idle time
                while(currClock<nextArrival){currClock++;}     // increment the clock to the next arrival time
                idle += System.currentTimeMillis()-idleTimer;  // store the idle time spent
                currQ.put(new TimeInst(process[colIdx.get("RT")][nextIndex]), nextIndex);                              // add the next index to the queue
                process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start); //store the arrival time in ms
                if(!tree.isEmpty()){                   // if there are still processes to be found
                    next = tree.firstKey();            // get the next ATInst
                    nextArrival = next.time;           // update next arrival time
                    nextIndex = tree.remove(next);     // get the next index
                }else nextArrival = -1;                // mark that the last item has been found
            }
            int curr = currQ.remove(currQ.firstKey()); // get the next process
            boolean newCurr = true;
            long burstSt = System.currentTimeMillis();  // start of the burst in ms
            boolean preempt = false;                    // boolean for preempting
            while(process[colIdx.get("RT")][curr]>0){   // while the process is not done
                while(currClock == nextArrival){        // if we reach the next process...
                    currQ.put(new TimeInst(process[colIdx.get("RT")][nextIndex]), nextIndex); // add it to the queue
                    process[colIdx.get("ATms")][nextIndex] = (int)(System.currentTimeMillis()-start);
                    
                    // If the new process's RT is less than the current process's RT, preempt
                    if(process[colIdx.get("RT")][nextIndex] < process[colIdx.get("RT")][curr]) preempt = true;
                    
                    if(!tree.isEmpty()){                // get next process (see lines 367-371)
                        next = tree.firstKey();
                        nextArrival = next.time;
                        nextIndex = tree.remove(next);
                    }else nextArrival = -1;
                }
                if(preempt) break;   // preempt if necessary
                
                //===============================DEBUGGING CODE=========================//
                if(demo && newCurr){ for(int c: debugCols) System.out.print(process[c][curr]+"\t");
                            System.out.println(""); 
                            newCurr = false;
                }
                //======================================================================//
                
                currClock++;         // increment currClock
                process[colIdx.get("RT")][curr]--;      // decrement the remaining time
            }
            // add the time of this burst to the burst time of the current process
            process[colIdx.get("BTms")][curr] += (int)(System.currentTimeMillis()-burstSt);
            if(preempt){ // preempt if necessary
                currQ.put(new TimeInst(process[colIdx.get("RT")][curr]), curr); // put process back into queue
                continue; // go to next iteration
            }
            // if done, finalize the metrics
            process[colIdx.get("CT")][curr] = (int)(System.currentTimeMillis()-start);
            process[colIdx.get("TaT")][curr] = process[colIdx.get("CT")][curr]-process[colIdx.get("ATms")][curr];
            process[colIdx.get("WT")][curr] = process[colIdx.get("TaT")][curr]-process[colIdx.get("BTms")][curr];
            //add this process's waiting time to the aggregate
            avgWait += process[colIdx.get("WT")][curr];
        }
        
        /*
        //===============================DEBUGGING CODE=========================//
        for(String s: columns) System.out.print(s+"\t");
        System.out.println("");
        for(int r=0; r<process[0].length; r++){
            for(int c=0; c<columns.length; c++) System.out.print(process[c][r]+"\t");
            System.out.println("");
        }
        int ct = 0;
        for(int i: process[colIdx.get("RT")]) if(i>0){ct++; System.out.println(i);}
        System.out.println(ct+" Not Done\n");
        */
        //======================================================================//
        double [] result = {idle, (double)(System.currentTimeMillis()-start), avgWait/process[0].length};
        return result;
    }
}
