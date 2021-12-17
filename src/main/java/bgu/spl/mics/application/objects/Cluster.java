package bgu.spl.mics.application.objects;


import bgu.spl.mics.MessageBusImpl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private HashSet<GPU> GPUs;
	private LinkedBlockingQueue<CPU> CPUs;
	private LinkedList<String> trainedModelsNames;


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
		CPUs = new LinkedBlockingQueue<>();
		trainedModelsNames = new LinkedList<>();
	}

	public void setGPUs (LinkedList<GPU> gpu) {
		for (GPU g : gpu) {
			GPUs.add(g);
		}
	}

	public void setCPUs (LinkedList<CPU> cpu) {
		for (CPU c : cpu) {
			CPUs.add(c);
		}
	}

	public void sendUnprocessedBatch(DataBatch db) {
		int minLF = Integer.MAX_VALUE;
		CPU minCPU = null;
		for (CPU cpu : CPUs) {
			int currLF = cpu.getLoadFactor()+(32/cpu.getCores())*db.getTicksForType();
			if(currLF<minLF){
				minLF = currLF;
				minCPU = cpu;
			}
		}
		minCPU.pushToInnerQueue(db);
	}

	public void addModelName(String name) {
		//WE DECIDED NOT TO USE SYNCHRONIZED HERE *********
		trainedModelsNames.add(name);
	}

	public void addBatchToVRAM(DataBatch db) {
		db.getGpuSender().pushToVRAM(db);
	}
}
