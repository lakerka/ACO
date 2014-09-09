import java.util.List;

public interface AntBrainz {

    /**
     * 
     * @param availableVertexes
     *            vertexes that has not been visited.
     * @param currentVertexIndex
     *            current postion of ant.
     * @return Returns index of next vertex to visit.
     */
    public int chooseNextVertexForAnt(List<Integer> availableVertexes,
	    int currentVertexIndex);

}
