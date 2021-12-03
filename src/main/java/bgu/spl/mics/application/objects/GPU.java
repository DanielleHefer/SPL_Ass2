package bgu.spl.mics.application.objects;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    /**
     * @INV:
     *      0<=getCurrVRAMSize()<=getVRAMLimitation()
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private int currVRAMSize;
    private int VRAMLimitation;

    /**
     * @PRE:
     *      none
     * @POST:
     *      m.getStatus()!=@PRE(m.getStatus())
     */
    public void process(Model m){}


    public int getCurrVRAMSize(){
        return currVRAMSize;
    }

    public int getVRAMLimitation() {
        return VRAMLimitation;
    }
}
