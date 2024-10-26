import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

public class SkipListSet <T extends Comparable<T>> implements SortedSet<T> {
    private class SkipListSetIterator implements Iterator<T> {
        Item current;
        Item prev;

        private SkipListSetIterator() {
            prev = null;
            current = null;
        }
        public SkipListSetIterator(Item head) {
            prev = null;
            current = head.right;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if(hasNext()) {
                prev = current;
                current = current.right;
                return prev.data;
            }
            else return null;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while(hasNext()) {
                action.accept(next());
            }
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
            return String.format("(h%d: %s)", level, (data==null)? "null":data);
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

        // Links the pointers of the other tower to all the pointers of the same level to the left
        public void connect(ItemTower other) {
            Item left = this.bottom;
            Item right = other.bottom;


            if(left != null && right != null) {
                while(true) {
                    right.right = left.right;
                    if(right.right != null) right.right.left = right;

                    left.right = right;
                    right.left = left;

                    if(left.up != null && right.up != null) {
                        left = left.up;
                        right = right.up;
                    }
                    else break;
                }
            }
            else throw new IllegalStateException("Cannot connect a null tower");

            while(left.tower.height < right.tower.height) {
                while(left.up == null) left = left.left;

                left = left.up;
                right = right.up;
                while(true) {
                    right.right = left.right;
                    if(right.right != null) right.right.left = right;

                    left.right = right;
                    right.left = left;

                    if(left.up != null && right.up != null) {
                        left = left.up;
                        right = right.up;
                    }
                    else break;
                }
            }
        }

        @Override
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

        if(walker.tower == head) return walker.tower.bottom;
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
        return new SkipListSetIterator(head.bottom);
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
        if(t == null) return false;

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
            if(maxSmaller.data != null && maxSmaller.data.compareTo(t) == 0) return false; // Item already in list
            else {
                ItemTower left = maxSmaller.tower;
                left.connect(tower);
            }
        }
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if(o == null) return false;

        try {
            Item target = search((T) o);

            if(target.data == o) {
                // Target found

                // Removing target from skipList by linking the left and right to each other
                while(target != null) {
                    target.left.right = target.right;
                    if(target.right != null) target.right.left = target.left;

                    target = target.up;
                }

                return true;
            }
            else {
                // Target not found
                return false;
            }
        }
        catch(ClassCastException e) {
            throw new IllegalStateException("Object isn't same type as list");
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for(T ele : c) {
            if(add(ele)) changed = true;
//            System.out.println(this);
//            System.out.println("---------------\n");
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for(Object ele : c) {
            if(remove(ele)) changed = true;
//            System.out.println(this);
//            System.out.println("---------------\n");
        }
        return changed;
    }

    @Override
    public void clear() {
        head = new ItemTower(null, 1);
        size = 0;
        maxHeight = 1;
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

    // For debugging
    public void printPointers() {
        Item horizontalWalker = head.bottom;
        Item verticalWalker = null;
        while(horizontalWalker != null) {
            verticalWalker = horizontalWalker;
            while(verticalWalker != null) {
                System.out.printf("[%s -> %s] ", verticalWalker, verticalWalker.right);
                verticalWalker = verticalWalker.up;
            }
            horizontalWalker = horizontalWalker.right;
            System.out.println();
        }
    }

    // For testing
    public static void main(String[] args) {
        SkipListSet<Integer> skipList = new SkipListSet<>();
        System.out.println(skipList);
        System.out.println("---------------\n");

        ArrayList<Integer> input = new ArrayList<>();
        input.add(2);
        input.add(4);
        input.add(7);
        input.add(5);
        input.add(9);
        input.add(3);
        input.add(6);
        input.add(1);

        skipList.addAll(input);
        System.out.println(skipList);
        System.out.println("---------------\n");

//        skipList.printPointers();

        ArrayList<Integer> remove = new ArrayList<>();
        remove.add(7);
        remove.add(1);
        remove.add(8);
        remove.add(6);

        skipList.removeAll(remove);

        System.out.println(skipList);
        System.out.println("---------------\n");


//        skipList.printPointers();
    }
}
