package bgu.spl.mics.application.objects;


import bgu.spl.mics.MessageBusImpl;

import java.util.Hashtable;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {


	/**
     * Retrieves the single instance of this class.
     */
	private static class ClusterInstance{
		private static Cluster instance = new Cluster();
	}

	public static Cluster getInstance() {
		return ClusterInstance.instance;
	}

	//Implement constructor *******************
	private Cluster () {

	}

}
