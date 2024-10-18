import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class SkipListSet <T extends Comparable<T>> implements SortedSet<T> {
    private class SkipListSetIterator implements Iterator<T> {
        Item walker;

        private SkipListSetIterator() {
            walker = null;
        }
        public SkipListSetIterator(Item head) {
            walker = head.right;
        }

        @Override
        public boolean hasNext() {
            return (walker.right != null);
        }

        @Override
        public T next() {
            return walker.data;
        }
    }
    private class Item implements Comparable<T> {
        T data;
        int level; // current level: [1, height]
        Item left, right, up, down;
        ItemTower tower;

        private Item() {
            this(null, 0, null);
        }
        public Item(T data, int level, ItemTower tower) {
            this.data = data;
            this.level = level;
            this.tower = tower;
            left = null;
            right = null;
            up = null;
            down = null;
        }

        public boolean isHead() {
            return left == null;
        }

        public boolean isTop() {
            return this == tower.top;
        }

        public boolean isBottom() {
            return this == tower.bottom;
        }

        @Override
        public int compareTo(T o) {
            if(this.isHead()) return -1;
            return data.compareTo(o);
        }

        @Override
        public String toString() {
            return String.format("Data = %s, Level = %d", data, level);
        }
    }
    private class ItemTower implements Comparable<T> {
        Item top, bottom;
        int height;

        private ItemTower() {
            this(null, 0);
        }
        public ItemTower(T data, int height) {
            bottom = null;
            top = null;
            this.height = 0;
            for(int i = 1; i <= height; i++) {
                add(new Item(data, i, this));
            }
        }

        private boolean isEmpty() {
            return bottom == null && top == null;
        }

        // Adds an item to top of tower
        private void add(Item item) {
            if(this.isEmpty()) {
                bottom = item;
            }
            else {
                top.up = item;
                item.down = top;
            }
            top = item;
            height++;
        }

        // Links the pointers of this tower to the pointers that are at the same level in the other tower.
        // Also reconnects any broken links properly.
        public void connect(ItemTower other) {
            Item left = this.bottom;
            Item right = other.bottom;

            while(left != null && right != null) {
                    right.right = left.right;
                    if(right.right != null) right.right.left = right;

                    left.right = right;
                    right.left = left;

                    left = left.up;
                    right = right.up;
            }
        }

        // does the same as the function above, but starts at a certain level
        public void connect(ItemTower other, int level) {
            Item left = this.bottom;
            Item right = other.bottom;

            while(left != null && right != null && left.level < level && right.level < level) {
                left = left.up;
                right = right.up;
            }

            while(left != null && right != null) {
                right.right = left.right;
                if(right.right != null) right.right.left = right;

                left.right = right;
                right.left = left;

                left = left.up;
                right = right.up;
            }
        }

        public int compareTo(T o) {
            return top.compareTo(o);
        }

        @Override
        public String toString() {
            String s = "";
            Item walker = bottom;
            for(int i = 1; i <= height; i++) {
                s.concat(walker.toString());
                s.concat("\n");
                walker = walker.up;
            }

            return s;
        }
    }

    ItemTower head;
    int size;
    int maxHeight;

    public SkipListSet() {
        head = new ItemTower(null, 1);
        maxHeight = head.height;
        size = 0;
    }

    public void reBalance() {

    }

    // Returns item containing the target, or returns the item that contains the greatest value smaller than target.
    private Item search(T target) {
        Item walker = head.top;
        while(walker.down != null) {
            walker = walker.down;
            while(walker.right != null && walker.right.compareTo(target) <= 0) walker = walker.right;
        }

        return walker;
    }

    private  int generateHeight() {
        int height = 1;
        while(Math.random() >= 0.5) height++;

        return height;
    }

    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return null;
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return null;
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return null;
    }

    @Override
    public T first() {
        return null;
    }

    @Override
    public T last() {
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator(head.top);
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        int height = generateHeight();
        ItemTower tower = new ItemTower(t, height);

        // Extending head to be the tallest tower if needed
        if(height >= maxHeight) {
            maxHeight = height+1;
            while(head.height < maxHeight) head.add(new Item(head.top.data, head.top.level+1, head));
        }

        if(this.isEmpty()) {
            head.connect(tower);
        }
        else {
            Item maxSmaller = search(t);
            if(!maxSmaller.isBottom()) throw new IllegalStateException("Should be bottom of tower");
            if(maxSmaller.data.compareTo(t) == 0) return false; // Item already in list
            else {
                ItemTower left = maxSmaller.tower;
                left.connect(tower);

                int prevHeight;
                while(tower.height > left.height) {
                    prevHeight = left.height;

                    maxSmaller = left.top;
                    while(maxSmaller.up == null) maxSmaller = maxSmaller.left;

                    left = maxSmaller.tower;
                    left.connect(tower, prevHeight+1);
                }
            }
        }
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        Item walker = head.bottom;

        while(walker != null) {

            if(walker.isHead()) s.append("H ");
            else s.append(String.format("%s ", walker.data));

            s.append("□ ".repeat(walker.tower.height));
            s.append("\n");

            walker = walker.right;
        }

        return s.toString();
    }

    // For testing
    public static void main(String[] args) {
        SkipListSet<Integer> skipList = new SkipListSet<>();
        System.out.println(skipList);
        System.out.println("---------------\n");

        skipList.add(1);
        System.out.println(skipList);
        System.out.println("---------------\n");
        skipList.add(2);
        System.out.println(skipList);
        System.out.println("---------------\n");
        skipList.add(5);
        System.out.println(skipList);
        System.out.println("---------------\n");
        skipList.add(3);
        System.out.println(skipList);
        System.out.println("---------------\n");
        skipList.add(4);
        System.out.println(skipList);
        System.out.println("---------------\n");
    }
}
