package csce315.kalah.model;

public class Pit {

	public enum PIT_TYPE {PLAYER_PIT, END_PIT}
	private PIT_TYPE pitType;
	private int count;
	private int owner;
	
	/**
	 * Constructor sets the initial number of stones in the pit
	 * @param count The initial number of stones in the pit
	 */
	public Pit(int count, PIT_TYPE type, int owner) {
		this.count = count;
		this.pitType = type;
		this.owner = owner;
	}
	
	
	/**
	 * Add a single stone to this pit
	 * @return Returns the new number of stones in the pit
	 */
	public int addStone() {
		return ++this.count;
	}
	
	
	/**
	 * Add an arbitrary number of stones to this pit
	 * @param count The number of stones to add to this pit
	 * @return Returns the new number of stones in the pit
	 */
	public int addStones(int count) {
		if(count < 0) return this.count;
		this.count += count;
		return this.count;
	}
	
	
	/**
	 * Returns the number of stones the pit currently contains
	 * @return The number of stones in this pit
	 */
	public int count() {
		return this.count;
	}
	
	
	/**
	 * Remove all the stones from this pit
	 * @return Returns the number of stones that were removed
	 */
	public int removeAll() {
		int returnValue = this.count;
		this.count = 0;
		return returnValue;
	}
	
	/**
	 * Return the owner of the pit type
	 * @return the owner of the pit
	 */
	public int getOwner() {
		return owner;
	}
	
	/**
	 * Return the type of the pit type
	 * @return the type of the pit
	 */
	public PIT_TYPE getType() {
		return this.pitType;
	}
}
