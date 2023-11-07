public class Item {
    private int key;                            // Key of the item
    private String value;                       // Value of the item
    private int version;                        // Version of the item

    private boolean lockedUpdate = false;       // If the item is lock for update 
    private int nLockedRead = 0;                // Number of lock for read the item

    /**
     * Constructor for Item
     * @param key Key of the item
     * @param value Value of the item
     * @param version Version of the item
     */
    public Item (int key, String value, int version) {
        this.key = key;
        this.value = value;
        this.version = version;
    }

    /**
     * Method used to get the version
     * @return Version of the item
     */
    public int getVersion () {
        return version;
    }

    /**
     * Method used to get the key
     * @return Key of the item
     */
    public int getKey () {
        return key;
    }

    /**
     * Method used to get the value
     * @return Value of the item
     */
    public String getValue () {
        return value;
    }

    /**
     * Method used to update the value of the item and increment the version
     * @param newValue New value for the item
     */
    public void updateItem (String newValue) {
        this.value = newValue;
        this.version ++;
    }
    
    /**
     * Method used to see if the item is lock or not for an update
     * @return True if the item is not lock, false if it is lock
     */
    public boolean lockUpdate() {

        // If the item is lock for an update or for a read
        if (this.lockedUpdate || this.nLockedRead > 0) {
            return false;
        }

        // Set the lock to true
        this.lockedUpdate = true;
        return true;
    }

    /**
     * Method used to unlock an item for update
     */
    public void unlockUpdate() {
        this.lockedUpdate = false;
    }

    /**
     * Method used to see if the item is lock or not for update
     * @return If the item is lock for update
     */
    public boolean isLockedUpdate() {
        return this.lockedUpdate;
    }
    
    /**
     * Method used to see if an item is lock for read or not
     * @return If the item is lock for read 
     */
    public boolean lockRead() {

        // If the item is lock for update
        if (this.lockedUpdate) {
            return false;
        }

        // Increment the number of lock for read
        nLockedRead ++;
        return true;
    }

    /**
     * Method used to unlock the item for read
     */
    public void unlockRead() {
        nLockedRead --;
        if (nLockedRead < 0) {
            nLockedRead = 0;
        }
    }

    /**
     * Method used to see if the is lock or not for read
     * @return If the item is lock for read
     */
    public boolean isLockedRead() {
        return (nLockedRead > 0);
    }
}
