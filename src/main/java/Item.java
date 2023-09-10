public class Item {
    private int key;
    private String value;
    private int version;


    private boolean lockedUpdate = false;
    private int nLockedRead = 0;

    public Item (int key, String value, int version) {
        this.key = key;
        this.value = value;
        this.version = version;
    }

    public int getVersion () {
        return version;
    }

    public int getKey () {
        return key;
    }

    public String getValue () {
        return value;
    }

    public void updateItem (String newValue) {
        this.value = newValue;
        this.version ++;
    }
    public boolean lockUpdate() {
        if (this.lockedUpdate || this.nLockedRead > 0) {
            return false;
        }
        this.lockedUpdate = true;
        return true;
    }
    public void unlockUpdate() {
        this.lockedUpdate = false;
    }
    public boolean isLockedUpdate() {
        return this.lockedUpdate;
    }
    public boolean lockRead() {
        if (this.lockedUpdate) {
            return false;
        }
        nLockedRead ++;
        return true;
    }
    public void unlockRead() {
        nLockedRead --;
        if (nLockedRead < 0) { // per sicurezza
            nLockedRead = 0;
        }
    }
    public boolean isLockedRead() {
        return (nLockedRead > 0);
    }
}
