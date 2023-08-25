public class Item {
    private String value;
        private int version;

        private boolean lockedUpdate = false;
        private int nLockedRead = 0;

        public Item (String value, int version) {
            this.value = value;
            this.version = version;
        }

        public int getVersion () {
            return version;
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
}
