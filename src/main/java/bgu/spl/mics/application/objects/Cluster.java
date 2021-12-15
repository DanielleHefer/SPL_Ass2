package bgu.spl.mics.application.objects;


import bgu.spl.mics.MessageBusImpl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private HashSet<GPU> GPUs;
	private PriorityQueue<CPU> CPUs;
	private LinkedList<String> trainedModelsNames;
	private Integer totalDBProcessedCPU;
	private int CPUTimeUnits;
	private int GPUTimeUnits;
	private Object lockGPUTimeUnits;
	private Object lockCPUTimeUnits;


    /**
     * Retrieves the single instance of this class.
     */
	private static class ClusterInstance{
		private static Cluster instance = new Cluster();
	}

	public static Cluster getInstance() {
		return ClusterInstance.instance;
	}

	private Cluster () {
		GPUs = new HashSet<>();
		CPUs = new PriorityQueue<>((a,b) -> Double.compare(a.getLoadFactor(),b.getLoadFactor()));
		trainedModelsNames = new LinkedList<>();
		totalDBProcessedCPU=0;
		CPUTimeUnits=0;
		GPUTimeUnits=0;
		lockGPUTimeUnits = new Object();
		lockCPUTimeUnits = new Object();
	}


	public void sendUnprocessedBatch(DataBatch db) {
		//Remove the CPU with the minimal load factor, and then add it with the new db
		CPU currCPU = CPUs.poll();
		currCPU.pushToInnerQueue(db);
		CPUs.offer(currCPU);
	}

	public void increaseGPUTimeUnits(int ticks) {
		synchronized (lockGPUTimeUnits) {
			GPUTimeUnits += ticks;
		}
	}

	public void increaseCPUTimeUnits(int ticks) {
		synchronized (lockCPUTimeUnits) {
			CPUTimeUnits += ticks;
		}
	}

	public void addModelName(String name) {
		//WE DECIDED NOT TO USE SYNCHRONIZED HERE *********
		trainedModelsNames.add(name);
	}

	public void increaseTotalDBProcessedCPU() {
		synchronized (totalDBProcessedCPU) {
			totalDBProcessedCPU++;
		}
	}

	public void addBatchToVRAM(DataBatch db) {
		db.getGpuSender().pushToVRAM(db);
	}

	public void setLoadFactor(CPU cpu, int processTick) {
		CPUs.remove(cpu);
		cpu.updateLoadFactor(-processTick);
		CPUs.offer(cpu);
	}
}
