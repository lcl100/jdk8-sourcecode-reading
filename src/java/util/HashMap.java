package java.util;

import sun.misc.SharedSecrets;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * HashMap类注释
 *
 * @param <K> 泛型，表示键值对中的键，全称是key
 * @param <V> 泛型，表示键值对中的值，全称是value
 * @author lcl100
 * @author 二木成林
 */
/*
    extends
        AbstractMap<K, V>   继承自抽象类AbstractMap<K, V>
    implements
        Map<K, V>   实现Map接口，Map接口中定义了Map的公有方法
        Cloneable   实现Cloneable接口，是一个标记性接口，表示该类可以被克隆
        Serializable    实现Serializable接口，是一个标记性接口，表示该类可以被序列化和反序列化
 */
public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {
    // 序列版本ID，用来标识该类的序列化版本的
    private static final long serialVersionUID = 362498820763181265L;

    /**
     * 常量，HashMap默认的初始化容量，必定是2的n次方
     * 1 << 4 = 2^4 = 16
     * 使用位运算（<<）比四则运算（*）效率高
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

    /**
     * 常量，HashMap可扩容或者构造方法指定的最大容量
     * 1 << 30 = 2^30 = 1073741824
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 常量，HashMap默认的负载因子
     * 1.它表示的是HashMap的容量在自动增长前，可存储键-值对映射充满程度，其值可以大于1。
     * 2.负载因子过高，虽然减小了空间开销，但同时也导致查找效率下降，键与键之间碰撞率高的情况下，可能会变成线性查找；
     * 3.负载因子过低，键与键之间的碰撞率减小，理想情况下，table中每个索引处值存储一个节点Node，查找的效率大大提升，但同时也会浪费了一些的空间。
     * 4.默认负载因子0.75，是时间成本和空间成本上的权衡，一般是不需要修改此参数默认值。
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 常量，触发由链表结构转换成红黑树结构的阈值，即超过这个阈值，那么就由链表结构转换成红黑树了
     * threshold英文意思有"阈；界"
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 常量，触发由红黑树结构转换链表结构的阈值
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 常量链表转换成红黑树之前，进行判断
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 内部类，HashMap中每个索引位置都是链表，也称为桶，链表中每个结点就是Node<K, V>
     * 实现Map.Entry<K, V>接口
     *
     * @param <K> 泛型，键值对中的键
     * @param <V> 泛型，键值对中的值
     */
    static class Node<K, V> implements Map.Entry<K, V> {
        // 该结点元素key的哈希值
        final int hash;
        // 键值对中的键
        final K key;
        // 键值对中的值
        V value;
        // 后继结点
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /**
     * 计算键（key）的哈希值
     *
     * @param key 键值对中的键（key）
     * @return 返回经过扰动算法计算出来的哈希值
     */
    static final int hash(Object key) {
        // 局部变量，存储key通过hashCode()方法计算出来的哈希值
        int h;
        // h = key.hashCode()是通过hashCode()方法计算出来的哈希值
        // h^(h >>> 16)表示进行扰动运算，降低哈希碰撞的概率
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 判断是否实现了Comparable接口
     *
     * @param x 应该是实现了Comparable的类
     * @return
     */
    static Class<?> comparableClassFor(Object x) {
        // 判断x是否是Comparable类型
        if (x instanceof Comparable) {
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                            ((p = (ParameterizedType) t).getRawType() ==
                                    Comparable.class) &&
                            (as = p.getActualTypeArguments()) != null &&
                            as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * Returns k.compareTo(x) if x matches kc (k's screened comparable
     * class), else 0.
     */
    @SuppressWarnings({"rawtypes", "unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable) k).compareTo(x));
    }

    /**
     * 检查所传的初始值是否是2的n次方，如果不是，则将其变为2的幂次方
     * 且不能为负数（负数则返回1），且不能超过常量MAXIMUM_CAPACITY
     * 比如输入10，则返回16；输入100，则返回128
     *
     * @param cap 初始值
     * @return 返回的值肯定是2的n次方，如果输入的参数就是2的n次方，那么直接返回原值
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        // 中间过程的目的就是使n的二进制数的低位全部变为1，比如10，11变为11，100，101，110，111变为111
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * HashMap底层的结点数组，用来存储链表，首次使用会初始化，分配该数组容量时必定是2的n次方
     * 在第一次使用的时候初始化，或者必要时进行扩容
     * 1.table是用于存储数据的变量，可以看到table实际一个Node类型数组，称为哈希桶数组。
     * 2.数组每个索引位置存储的元素是一个链表（或者红黑树），table的默认初始容量是16。
     * 3.如果哈希桶数组长度比较小，即使哈希算法比较优化情况下，当数组中元素增加到一定的数量时，会产生碰撞。
     * 4.如果哈希桶数组的长度比较大，这会浪费一部分的空间。需要在空间和时间上进行权衡，使得哈希桶发生碰撞概率较低，并且不会浪费空间。
     * 5.HashMap有两个可供调节的参数，loadFactor和threshold。
     */
    transient Node<K, V>[] table;

    /**
     * 对应键值对的映射集
     */
    transient Set<Map.Entry<K, V>> entrySet;

    /**
     * HashMap中键值对的数量，即有多少个键值对
     */
    transient int size;

    /**
     * 用来记录HashMap中被修改的次数
     * 1.任何对HashMap的修改都会导致ModCount++，使用迭代器进行迭代时会将ModCount值赋值给expectedModCount。
     * 2.在迭代过程中会判断modCount是否等于expectedModCount。
     * 3.如果不等，说明在迭代过程中其他线程修改了HashMap，就会抛出ConcurrentModificationException异常。
     */
    transient int modCount;

    /**
     * HashMap中当前能够容纳键值对的数量，即阈值，超过此值就会进行扩容
     * threshold = capacity * load factor
     * 1.所以实际上决定阈值大小因素除负载因子以外，与容量大小有关，而初始容量可以通过HashMap有参构造方法来指定。
     * 2.当哈希桶数组中元素个数超过阈值，便会进行扩容。比如哈希桶数组初始容量capacity=16，负载因子loadFactor是0.75，阈值threshold= 16*0.75=12，当桶数量超过12时，会进行扩容。
     * 3.HashMap无参构造方法会在put方法时计算threshold，有参构造方法会调用tableSizeFor(int cap)方法来计算初始阈值threshold
     */
    int threshold;

    /**
     * HashMap中的负载因子
     * 1.它表示的是HashMap的容量在自动增长前，可存储键-值对映射充满程度，其值可以大于1。
     * 2.负载因子过高，虽然减小了空间开销，但同时也导致查找效率下降，键与键之间碰撞率高的情况下，可能会变成线性查找；
     * 3.负载因子过低，键与键之间的碰撞率减小，理想情况下，table中每个索引处值存储一个节点Node，查找的效率大大提升，但同时也会浪费了一些的空间。
     * 4.默认负载因子0.75，是时间成本和空间成本上的权衡，一般是不需要修改此参数默认值。
     */
    final float loadFactor;

    /* ---------------- HashMap的构造方法 -------------- */

    /**
     * 待两个参数的构造方法
     *
     * @param initialCapacity 输入的初始化容量
     * @param loadFactor      输入的负载因子
     * @throws IllegalArgumentException initialCapacity参数或者loadFactor参数不正确抛出的异常
     */
    public HashMap(int initialCapacity, float loadFactor) {
        /**
         * 初始化容量不能为负数，如-5，将抛出IllegalArgumentException异常
         * 原因：毕竟你不可能设置容量为负数的数组
         * 例如：Illegal initial capacity: -5
         */
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        /**
         * 初始化容量是否大于HashMap中指定的最大容量MAXIMUM_CAPACITY
         * 如果大于，则将初始化容量指定为MAXIMUM_CAPACITY，不能更大了
         */
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        /**
         * 负载因子小于等于0或者负载因子不是一个数字，将抛出IllegalArgumentException异常
         * 例如：loadFactor = -3 或者 loadFactor = NaN
         * 例如：Illegal load factor: -3
         */
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        // 初始化负载因子loadFactor
        this.loadFactor = loadFactor;
        // 初始化阈值threshold，调用tableSizeFor方法判断传入的初始化容量是否是2的n次方，如果不是则调整为2的n次方，是则返回原值
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * 带一个参数的构造方法
     *
     * @param initialCapacity 输入的初始化容量
     */
    public HashMap(int initialCapacity) {
        /**
         * 调用带有两个参数的重载构造方法，DEFAULT_LOAD_FACTOR是默认负载因子，为0.75f
         */
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * HashMap的无参构造函数
     */
    public HashMap() {
        // 构建的是初始容量是DEFAULT_INITIAL_CAPACITY
        // 初始化负载因子loadFactor为默认值DEFAULT_LOAD_FACTOR
        this.loadFactor = DEFAULT_LOAD_FACTOR;// 7.5f
    }

    /**
     * 带一个参数的构造方法
     *
     * @param m HashMap集合
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        // 初始化负载因子loadFactor为默认值DEFAULT_LOAD_FACTOR
        this.loadFactor = DEFAULT_LOAD_FACTOR;// 7.5f
        // 调用putMapEntries()方法添加m到HashMap集合中，false参数表示当前正处于创建模式
        putMapEntries(m, false);
    }

    /**
     * 添加Map集合的元素到HashMap中
     *
     * @param m     传入的Map接口
     * @param evict 如果为 false，说明 table 处于创建中，evict这个变量当在map构造器传入指定map初始化的时候是false，其他情况为true，也即其他构造器创建map之后再调用put方法，该参数则为true
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        // 表示传入Map接口中键值对的数量，即长度
        int s = m.size();
        // 判断传入的Map是否有值，长度大于0则表示有若干个键值对
        if (s > 0) {
            // 判断当前的哈希桶数组是否为空，为空则表示table还没有初始化
            if (table == null) {
                // threshold(阈值，即实际最大容量) =capacity(容量，即理论最大容量) * loadFactor(负载因子）
                // s为m的实际元素个数，计算出来的ft是HashMap所需的最大负载容量
                // 可能s/loadFactor计算出来的结果是小数，但如果算出来的capacity是小数，却又向下取整，会造成容量不够大，所以，如果是小数的capacity，那么必须向上取整。
                float ft = ((float) s / loadFactor) + 1.0F;
                // 判断计算出来的ft是否在MAXIMUM_CAPACITY的范围内，如果小于则返回ft，如果超出则指定为MAXIMUM_CAPACITY
                int t = ((ft < (float) MAXIMUM_CAPACITY) ? (int) ft : MAXIMUM_CAPACITY);
                // 如果计算出来的阈值超过指定的阈值
                if (t > threshold)
                    // 则修改当前阈值为计算出来的新阈值，tableSizeFor()方法是为了检查t是否是2的n次方，如果不是则进行调整并返回
                    threshold = tableSizeFor(t);
                // 表示table!=null并且s>threshold的情况，即传入的Map中键值对的数量超过了设定的阈值
            } else if (s > threshold)
                // 那么就进行扩容
                resize();
            // 使用foreach循环遍历传入的Map
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                // 键
                K key = e.getKey();
                // 值
                V value = e.getValue();
                // 调用putVal()方法向HashMap中添加键值对
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * 得到HashMap中键值对的个数
     *
     * @return 返回键值对的数量
     */
    public int size() {
        return size;
    }

    /**
     * 判断HashMap是否为空
     *
     * @return 如果为空则返回true，不为空返回false
     */
    public boolean isEmpty() {
        // size是计数器，用来记录HashMap中键值对的个数，因此如果size为0，则表示HashMap为空
        return size == 0;
    }

    /**
     * 得到HashMap中指定key对应的value
     *
     * @param key 指定的key
     * @return 返回根据key查找到的value或者返回null
     */
    public V get(Object key) {
        // 局部变量，存放根据key值获取到的结点Node
        Node<K, V> e;
        // 根据指定的key查找HashMap中的结点，如果结点为null则返回null，否则返回结点的值value
        return (e = getNode(hash(key), key)) == null ? null : e.value;
        /**
         等价于（只是语句太复杂了，下面详细说明）
         int hash = hash(key);
         Node<K, V> e = getNode(hash, key);
         if (e==null)
         return null;
         else
         return e.value;
         */
    }

    /**
     * 根据指定的hash值和key值查找结点
     *
     * @param hash 键的哈希值
     * @param key  键
     * @return 返回查找到的结点或者如果没有查找到则返回null
     */
    final Node<K, V> getNode(int hash, Object key) {
        // 局部变量，用来保存哈希桶数组
        Node<K, V>[] tab;
        Node<K, V> first, e;
        int n;
        K k;
        /*
            且哈希桶数组不能为null，表示table已经初始化了
            且tab.length大于0，表示已经哈希桶数组中已经有元素结点了
            且(n - 1) & hash计算的是key应该存储在哈希桶数组中的位置（下标），first=tab[(n - 1) & hash]表示该存储位置链表的第一个结点，是链头
         */
        if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) {
            /*
                first.hash == hash  判断链头结点的哈希值是否等于传入的哈希值
                k = first.key) == key   如果key是基本类型，则使用"=="来比较链头结点的key是否等于传入的key
                key != null && key.equals(k) 如果key是引用类型，必须判断对象不能为null并且判断链头结点的key是否等于传入的key
             */
            // 如果链表的链头（第一个结点）的哈希值与key同传入的参数值相同，则表示链头结点就是我们要查找的指定结点
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k))))
                // 然后返回该链头结点即可
                return first;
            // 如果链头结点不是要查找的结点，并且如果链表除了头结点还有其他的结点，那么遍历整个链表
            if ((e = first.next) != null) {
                // 判断链头结点是否是树结点TreeNode，则表示当前结构不是链表结构，而是红黑树结构
                if (first instanceof TreeNode)
                    // 那么调用getTreeNode()方法在红黑树中根据指定的key查找结点然后返回
                    return ((TreeNode<K, V>) first).getTreeNode(hash, key);
                // 如果链头结点不是树结点，表示当前是链表结构，并且头结点不是要查找的结点，所以遍历整个链表
                do {
                    // 比较循环遍历中的链表结点的哈希值和key是否等于要查找的指定哈希值和key
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                        // 如果符合，则返回
                        return e;
                } while ((e = e.next) != null);
            }
        }
        // 如果都没有查找到，则返回null，表示在HashMap中没有找到指定的值
        return null;
    }

    /**
     * 判断HashMap中是否包含指定的key
     *
     * @param key 输入的key
     * @return 如果HashMap中存在指定的key则返回true，如果没有查找到则返回false
     */
    public boolean containsKey(Object key) {
        // 调用getNode()方法查找指定key的结点，getNode()方法的返回结果是如果查找到结点则返回该结点，如果没有查找到则返回null
        // 因此判断它是否等于null就可以得到HashMap中是否包含指定键
        return getNode(hash(key), key) != null;
    }

    /**
     * 添加键值对到HashMap中
     *
     * @param key   键值对中的key
     * @param value 键值对中的value
     * @return 返回旧值（如果已经存在该key，那么新值覆盖旧值，并返回旧值）或者null（）
     */
    public V put(K key, V value) {
        // 调用putVal()方法向HashMap中添加键值对
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * 添加键值对到HashMap中
     *
     * @param hash         key所对应的哈希值
     * @param key          键值对中的键（key）
     * @param value        键值对中的值（value）
     * @param onlyIfAbsent 如果存在相同的值，是否替换已有的值，true表示替换，false表示不替换
     * @param evict        表是否在创建模式，如果为false，则表是在创建模式
     * @return 返回旧值或者null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        HashMap.Node<K, V>[] tab;// 临时变量，用来临时存放哈希桶数组
        HashMap.Node<K, V> p;
        int n, i;
        // 检查链表数组table是否为空，table为null或者table数组的长度为0都表示为空
        if ((tab = table) == null || (n = tab.length) == 0)
            // 如果为空则初始化，并扩容，然后返回新链表数组的长度，将长度赋值给变量n
            // resize()方法就是初始化并扩容，该方法具体请参考：
            n = (tab = resize()).length;
        // (n-1)&hash这条语句就是JDK1.7中HashMap源码中的indexFor()方法的功能，即得到该对象存放在数组中的具体位置（下标）
        // 判断该位置的元素是否为null，即是否存在元素，如果存在则表示已经发生哈希冲突，如果不存在，则添加元素结点
        if ((p = tab[i = (n - 1) & hash]) == null)
            // 表示不存在元素的情况，不发生哈希冲突
            // 则新添加一个元素到链表数组的对应下标位置，该结点也是链表的链头
            tab[i] = newNode(hash, key, value, null);
        else {
            // 表示存在元素的情况
            // 则发生了哈希冲突，下面的代码则是尝试解决冲突问题
            HashMap.Node<K, V> e;
            K k;
            // 判断待添加元素的hash值和key值是否同已经存在（冲突）的元素的hash值和key值同时相等
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                // 如果相等，则表示两个元素相互重复了，那么使用变量e来临时存储这个重复元素
                e = p;
                // 如果不相等，表示没有重复，并且判断结点类型是否是红黑树类型
            else if (p instanceof HashMap.TreeNode)
                // 那么就将该键值对存储到红黑树中
                e = ((HashMap.TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
                // 如果不相等，且结点类型不是红黑树类型，那么就是链表，即采用拉链法解决冲突
            else {
                // 遍历链表中所有结点，这是一个死循环，需要通过break跳出循环
                for (int binCount = 0; ; ++binCount) {
                    // 如果p的下一个结点为null，则p是链表中的最后一个结点
                    if ((e = p.next) == null) {
                        // 则将键值对添加到最后一个结点的后面
                        p.next = newNode(hash, key, value, null);
                        // 同时binCount也是一个计数器，统计该链表已经有几个元素了
                        // TREEIFY_THRESHOLD是常量，表示阈值，默认值为8
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            // 但链表中元素个数超过了阈值，则将链表转换成红黑树
                            treeifyBin(tab, hash);
                        // 跳出循环
                        break;
                    }
                    // 判断待添加元素的hash值和key值是否同链表中已有元素的hash值和key值同时相等
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                        // 如果相等，则表示已经存在相同的键，跳出循环
                        break;
                    // 将下一个节点赋值给当前节点，继续往下遍历链表
                    p = e;
                }
            }
            // 如果e不为空，则表示已经存在重复的值，即存在hash值和key值同时相等的元素
            if (e != null) {
                // 保存旧值
                V oldValue = e.value;
                // 然后替换为新值
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                // 此函数会将链表中最近使用的Node节点放到链表末端，因为未使用的节点下次使用的概率较低
                afterNodeAccess(e);
                // 返回旧值
                return oldValue;
            }
        }
        // 记录修改次数
        ++modCount;
        // 如果添加元素后，超过阈值
        if (++size > threshold)
            // 则对HashMap进行扩容
            resize();
        // 给LinkedHashMap使用
        afterNodeInsertion(evict);
        return null;
    }

    /**
     * 初始化或扩容
     *
     * @return 返回初始化或扩容后的哈希桶数组
     */
    final Node<K, V>[] resize() {
        // 局部变量，存放扩容前（或未初始化）的哈希桶数组
        Node<K, V>[] oldTab = table;
        // 旧哈希桶数组的容量（即已有的元素个数）
        // 如果oldTab为null，表示table没有初始化，那么oldTab就为0，否则oldTab就是旧哈希桶数组的长度
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // 旧阈值
        int oldThr = threshold;
        // 新容量，新阈值初始都为0
        int newCap, newThr = 0;
        // 如果oldTab大于0，则表示table已经初始化，并且存储了若干个元素
        if (oldCap > 0) {
            // 如果oldCap已经大于设定的最大容量MAXIMUM_CAPACITY
            /*
                MAXIMUM_CAPACITY    1 << 30=2^30=1073741824
                MAX_VALUE           0x7fffffff=2147483647
                DEFAULT_INITIAL_CAPACITY  1 << 4=2^4=16
             */
            if (oldCap >= MAXIMUM_CAPACITY) {
                // 则将阈值继续扩大，扩大为Integer.MAX_VALUE
                threshold = Integer.MAX_VALUE;
                // 然后返回oldTab
                return oldTab;
                // 表示oldCap在MAXIMUM_CAPACITY范围内，同时如果要进行扩容，那么需要判断扩容后的容量是否小于设定的最大容量MAXIMUM_CAPACITY和旧容量是否大于设定的默认初始化容量DEFAULT_INITIAL_CAPACITY
            /*
                    等价于
                    newCap = oldCap << 1 = oldCap *2 // 即扩容为原来容量的二倍
                    if(newCap<1073741824 && oldCap >= 16)
            */
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY) {
                // newCap = oldCap << 1;     新容量扩容为原来容量的2倍
                // newThr = oldThr << 1;     新阈值扩容为原来阈值的2倍
                newThr = oldThr << 1;
            }
        /*
            等价于
            if (oldThr>0 && oldCap==0)
            表示原先是没有创建过哈希桶数组，属于HashMap有参构造方法创建的实例，第一次进行put()方法操作。此处将新容量设置为原先阈值，并没有进行设置新阈值
         */
        } else if (oldThr > 0) {
            newCap = oldThr;
        /*
            表示oldThr==0 && oldCap==0
            属于HashMap无参构造方法创建的实例，第一次进行put()方法操作。
            会将新容量设置为默认容量，新阈值设置为默认负载因子乘以默认容量。
         */
        } else {
            // 新容量为16
            newCap = DEFAULT_INITIAL_CAPACITY;
            // 新阈值为16*0.75=12
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // 如果扩容后，阈值为0，重新计算阈值
        if (newThr == 0) {
            // 新阈值为 newCap * loadFactor
            float ft = (float) newCap * loadFactor;
            // 判断新阈值是否在MAXIMUM_CAPACITY范围内，设置新阈值
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        // 创建一个新容量的哈希桶数组
        @SuppressWarnings({"rawtypes", "unchecked"})
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        // 将新数组赋给table
        table = newTab;
        // 将旧表中的元素都复制到新表
        if (oldTab != null) {
            // 循环遍历旧哈希桶数组
            for (int j = 0; j < oldCap; ++j) {
                // 局部变量，存放哈希桶数组中的每个结点
                Node<K, V> e;
                // 如果当前位置的结点不为null，则复制该结点，为null的话则继续遍历循环下一个结点
                if ((e = oldTab[j]) != null) {
                    // 将旧哈希桶当前位置的结点置为null
                    oldTab[j] = null;
                    // 如果该位置只有e一个结点，即没有后继结点（e.next==null）
                    if (e.next == null)
                        // 直接将e结点存储在新哈希桶中计算出来的位置中
                        newTab[e.hash & (newCap - 1)] = e;
                        // 表示不止一个结点，判断是否是红黑树结点
                    else if (e instanceof TreeNode)
                        // 调用split方法对这个桶中红黑树所有节点进行重新hash分布
                        ((TreeNode<K, V>) e).split(this, newTab, j, oldCap);
                    // 表示是链表结构
                    else {
                        // 将原先的链表分成两条，以e.hash&oldCap是否等于0为分割线
                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        // 循环遍历
                        do {
                            // 该语句用于下面的循环，指向下一个结点
                            next = e.next;
                            // 以e.hash&oldCap是否等于0将原先的链表分割，然后分别连接成两条链表
                            if ((e.hash & oldCap) == 0) {
                                // 将e.hash&oldCap等于0的结点连接成一条新的链表
                                // loTail等于null，表示当前是一个空链表
                                if (loTail == null)
                                    // 将e赋给当前链表的头指针
                                    loHead = e;
                                // loTail不等于null，表示当前不是一个空链表
                                else
                                    // 那么将loTail的next指针指向e
                                    loTail.next = e;
                                // 然后将新插入的e置为尾结点
                                loTail = e;
                            } else {
                                // 将e.hash&oldCap不等于0的结点连接成一条新的链表
                                // hiTail等于null，表示当前是一个空链表
                                if (hiTail == null)
                                    // 将e赋给当前链表的头指针
                                    hiHead = e;
                                // hiTail不等于null，表示当前不是一个空链表
                                else
                                    // 那么将loTail的next指针指向e
                                    hiTail.next = e;
                                // 然后将新插入的e置为尾结点
                                hiTail = e;
                            }
                        } while ((e = next) != null);// e=e.next;
                        // 当loTail不等于null时，将新哈希桶数组中j位置设置尾loHead链表
                        if (loTail != null) {
                            // 将尾结点的next指针指向null，表示链表结束
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 当hiTail不等于null时，将新哈希桶数组中j+oldCap位置设置为hiHead链表
                        if (hiTail != null) {
                            // 将尾结点的next指针指向null，表示链表结束
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        // 返回扩容后的新表
        return newTab;
    }

    /**
     * 将链表转换成红黑树，具体需要转化红黑树时，会先将普通链表节点转化为树节点，并且构造成双向链表，然后调用treeify将此链表转化为红黑树
     *
     * @param tab  哈希桶数组
     * @param hash 由putVal()方法传入的参数值hash值
     */
    final void treeifyBin(Node<K, V>[] tab, int hash) {
        // 局部变量，n存放哈希桶数组的长度，index存放数组下标
        int n, index;
        // 局部变量，存放下标位置的TreeNode<K, V>结点
        Node<K, V> e;
        // 如果哈希桶数组未初始化或者数组长度小于64，则进行初始化或扩容
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            // 调用resize()方法进行扩容
            resize();
            // 表示tab!=null并且数组长度大于等于64，那么就将链表转换成红黑树
            // e = tab[index = (n - 1) & hash]表示将数组中的元素取出赋值给e，e是链表头结点
            // 注意：只是计算出来的下标位置index那里的链表才转换成红黑树，数组其他位置的链表不转换
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            // 局部变量，hd指的是双向链表的头结点，tl指的是双向链表的尾结点
            // hd应该是head的缩写，表示"头"，tl应该是"tail"的缩写，表示"尾"
            TreeNode<K, V> hd = null, tl = null;
            // 循环遍历整个链表，构建一个双向链表
            do {
                // 新创建一个双链表结点，内容和当前遍历的链表结点e一致，但next指针为null
                TreeNode<K, V> p = replacementTreeNode(e, null);
                // 如果双向链表的尾结点为null，表示向空双链表添加结点时执行
                if (tl == null)
                    // 将新创建的p结点赋给双链表的头结点
                    hd = p;
                    // 执行到这里，表示双链表不为空，向双链表尾部插入结点
                else {
                    // 将新待插入结点的前驱指针指向双链表的尾结点
                    p.prev = tl;
                    // 将双链表尾结点的后继指针指向新待插入结点，完成插入
                    tl.next = p;
                }
                // 最后再将新插入结点设置为为结点
                tl = p;
            } while ((e = e.next) != null);// e = e.next 将当前节点的下一个节点赋值给e，进行循环遍历
            // tab[index] = hd 指的是将完成的双链表放到数组指定索引处，也就是原来单链表的位置，单链表释放资源
            if ((tab[index] = hd) != null)
                // 调用treeify()方法将双链表转换成红黑树
                hd.treeify(tab);
        }
    }

    /**
     * 添加一个Map集合中的所有元素到现在的HashMap中
     *
     * @param m Map集合
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        // 调用putMapEntries()方法添加集合中的元素
        putMapEntries(m, true);
    }

    /**
     * 移除HashMap中指定key的键值对
     *
     * @param key 指定的key
     * @return 返回被删除key的键值对中的值或者null
     */
    public V remove(Object key) {
        // 局部变量，存放被删除的结点
        Node<K, V> e;
        // 调用removeNode()方法移除HashMap中的元素
        return (e = removeNode(hash(key), key, null, false, true)) == null ? null : e.value;
        /*
            等价于（复杂化代码）
            int hash = hash(key);
            Node<K, V> e = removeNode(hash, key, null, false, true);
            if (e==null) {
                return null;
            } else {
                return e.value;
            }
         */
    }

    /**
     * 移除指定结点
     *
     * @param hash       键的哈希值
     * @param key        键
     * @param value      值
     * @param matchValue 如果为true，表示只有和传入的值相同，才删除
     * @param movable    如果为false，在删除时不移动其他节点
     * @return 返回删除的节点，如果没找到删除的点，返回null
     */
    final Node<K, V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable) {
        // 局部变量，保存当前的哈希桶数组
        Node<K, V>[] tab;
        // 局部变量，保存的是在哈希桶数组中所根据key查找到的结点
        // 注意：p是根据计算出来的下标位置获得的结点，而node是匹配值相等后得到的要被删除的结点，它们不一定相等
        Node<K, V> p;
        // 局部变量，n表示哈希桶数组table的长度，index表示根据哈希值计算出来的数组下标
        int n, index;
        /*
            (tab = table) != null   判断哈希桶数组table是否初始化，如果为null，则表示table没有初始化，则返回null
            (n = tab.length) > 0    判断哈希桶数组table中是否有足够的元素，大于0则表示table有键值对
            index = (n - 1) & hash     获取要被删除的key在数组中的位置（下标）
            (p = tab[index = (n - 1) & hash]) != null   判断根据计算出来下标然后获得的结点是否为null
         */
        if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = (n - 1) & hash]) != null) {
            // 局部变量，node保存要被删除的结点
            Node<K, V> node = null, e;
            K k;
            V v;
            // 注意：p是链表的头结点或者红黑树的根结点
            // 判断头（根）结点的哈希值和key值是否等于输入的哈希值和key值
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                // 如果相等则用node保存该结点，表示node就是要被删除的结点
                node = p;
                // 执行到这，表示头（根）结点不是要被删除的结点，那么判断该结点的下一个结点是否存在
            else if ((e = p.next) != null) {
                // 如果头（根）结点的下一个结点存在
                // 如果头（根）结点p是TreeNode类型，那么代表是红黑树结构
                if (p instanceof TreeNode)
                    // 那么调用getTreeNode()方法获取要被删除的结点
                    node = ((TreeNode<K, V>) p).getTreeNode(hash, key);
                    // 那么就表示头（根）结点p是链表结构
                else {
                    // 遍历循环整个链表
                    do {
                        // 判断链表中是否有结点的哈希值和key值符合查找的条件
                        if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                            // 在链表中查找到要被删除的结点，则用node保存该结点，然后使用break跳出循环
                            node = e;
                            break;
                        }
                        // p节点更新为本次循环的结点。如果上一步找到打断了，则p保存了找到结点的上个结点
                        p = e;
                    } while ((e = e.next) != null);// 指向下一个结点
                }
            }
            // 执行到这里，表示已经找到了要被删除的结点node
            // 如果找到了这个node，就判断是否传入了值，如果传入value，还要判断value相同
            if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))) {
                // 判断要被删除的结点是否是红黑树结构
                if (node instanceof TreeNode)
                    // 既然被删除的结点是红黑树结点，那么就需要调用红黑树中的方法来删除该结点
                    // 调用removeTreeNode()方法来删除结点
                    ((TreeNode<K, V>) node).removeTreeNode(this, tab, movable);
                    // 表示不是红黑树结构，那么判断要被删除的结点node是否链表的头结点
                else if (node == p)
                    // 如果node是链表的头结点，那么直接将指针指向头结点的下一个结点
                    tab[index] = node.next;
                    // 表示既不是红黑树结构，也不是链表的头结点，那么就是链表结构
                else
                    // 将要删除的上个节点的next赋值为删除节点的下一个节点
                    p.next = node.next;
                // 删除成功后，记录哈希表改变结构的次数
                ++modCount;
                // 同时删除成功后，HashMap中键值对的个数减少一个
                --size;
                // 提供给linkedHashMap使用
                afterNodeRemoval(node);
                // 返回被删除的结点
                return node;
            }
        }
        // 如果table为null或者table.length为0或者要被删除的结点没有找到，则返回null
        return null;
    }

    /**
     * 移除HashMap中的所有键值对，清空
     */
    public void clear() {
        // 局部变量，保存当前的哈希桶数组
        Node<K, V>[] tab;
        // 修改次数加1
        modCount++;
        // table不为null并且size不为0，即HashMap中有键值对才需要进行清空，否则没有必要
        if ((tab = table) != null && size > 0) {
            // 将size重置为0
            size = 0;
            // 同时将哈希桶数组中的所有元素置为null
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    /**
     * 判断HashMap中是否包含指定的value
     *
     * @param value 指定的value
     * @return 如果包含则返回true，不包含则返回false
     */
    public boolean containsValue(Object value) {
        // 局部变量，保存当前的哈希桶数组table
        Node<K, V>[] tab;
        // 局部变量，保存找到的值
        V v;
        // 如果哈希桶数组不为null并且size大于0，即HashMap存在键值对，才有意义
        if ((tab = table) != null && size > 0) {
            // 循环遍历哈希桶数组table，因为每个桶都是链表或者红黑树
            for (int i = 0; i < tab.length; ++i) {
                // 循环遍历每个桶的结点
                for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                    // 如果该结点的值符合查询
                    if ((v = e.value) == value || (value != null && value.equals(v)))
                        // 则返回true，表示包含该value
                        return true;
                }
            }
        }
        // 如果没有找到，或者table为空或者size为0，那么就返回false，表示不包含
        return false;
    }

    /**
     * 获取HashMap的键的集合，以Set<K>保存
     *
     * @return 返回key的集合
     * @see 更多详细参考：https://blog.csdn.net/cnds123321/article/details/113791846
     */
    public Set<K> keySet() {
        /*
            说明：
                1.可以看到其实该方法中，并没有将HashMap中的键添加到Set集合中，那么是如何实现的呢？
                2.但实际上，我们访问Set集合，根本就无法通过索引，而是需要通过迭代器Iterator才能访问到元素，foreach本质上也是迭代器
                3.这里的 ks 就仅仅只是一个Set引用，指向HashMap内部类KeySet的一个实例，重点在于该实例拥有自己的迭代器，当我们在使用增强for循环时才会调用该迭代器，也才会输出我们想要的东西
         */
        // 获取keySet
        Set<K> ks = keySet;
        // 判断ks是否为null,如果为null，表示为空
        if (ks == null) {
            // 实例化KeySet对象
            ks = new KeySet();
            // 然后将ks设置给keySet
            keySet = ks;
        }
        // 返回ks
        return ks;
    }

    final class KeySet extends AbstractSet<K> {
        public final int size() {
            return size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<K> iterator() {
            return new KeyIterator();
        }

        public final boolean contains(Object o) {
            return containsKey(o);
        }

        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }

        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super K> action) {
            Node<K, V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K, V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 获取HashMap中value的集合
     *
     * @return 返回value集合
     * @see 更多详细参考：https://blog.csdn.net/cnds123321/article/details/113792213
     */
    public Collection<V> values() {
        /*
            1.与keySet()方法一样，最重要的还是new Values()，实例化Values对象
         */
        Collection<V> vs = values;
        if (vs == null) {
            // 实例化Values类对象
            vs = new Values();
            values = vs;
        }
        // 返回该对象
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size() {
            return size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<V> iterator() {
            return new ValueIterator();
        }

        public final boolean contains(Object o) {
            return containsValue(o);
        }

        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super V> action) {
            Node<K, V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K, V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 该方法返回值就是这个map中各个键值对映射关系的集合
     * 1.Map中采用Entry内部类来表示一个映射项，映射项包含Key和Value
     * 2.Map.Entry里面包含getKey()和getValue()方法
     *
     * @return 返回map中各个键值对映射关系的集合
     */
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
        /*
            等价于（代码复杂化）
            Set<Map.Entry<K, V>> es = entrySet;
            if (es == null) {
                entrySet = new EntrySet();
                return entrySet;
            } else {
                return es;
            }
         */
    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        public final int size() {
            return size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            Object key = e.getKey();
            Node<K, V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }

        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }

        public final Spliterator<Map.Entry<K, V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super Map.Entry<K, V>> action) {
            Node<K, V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K, V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /* -----------------------------有@Override注解的都是重写的方法------------------------------- */

    /**
     * 通过指定key获取值，如果该值不存在，则返回默认值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 返回在HashMap中查找到的值，如果不存在则返回默认值defaultValue
     */
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K, V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
        // 与get()方法的return语句比较
        // return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 添加元素，但onlyAbsent为true
     *
     * @param key   键
     * @param value 值
     * @return 返回旧值或者null
     * @see 更多详细参考：https://blog.csdn.net/cnds123321/article/details/113793574
     */
    @Override
    public V putIfAbsent(K key, V value) {
        // onlyIfAbsent表示是否替换键相同的情况已有的value值，true表示不替换，false表示替换
        return putVal(hash(key), key, value, true, true);
    }

    /**
     * 移除指定key和value在HashMap中
     *
     * @param key   键
     * @param value 值
     * @return 返回删除的节点，如果没找到删除的点，返回null
     */
    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    /**
     * 用新值替换指定key的旧值
     *
     * @param key      指定的key
     * @param oldValue 旧值，旧值必须与根据key查找出来的value相等才能替换成功
     * @param newValue 新值
     * @return 如果替换成功则返回true，替换失败则返回false
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        // 局部变量，用来保存在根据key查找到的结点
        Node<K, V> e;
        // 保存结点e的value值
        V v;
        // 检测是否查找到指定key的value，并且与输入的oldValue相等
        if ((e = getNode(hash(key), key)) != null && ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            // 将旧值替换为新值newValue
            e.value = newValue;
            // 给LinkedHashMap使用
            afterNodeAccess(e);
            // 替换成功，则返回true
            return true;
        }
        // 如果要查找的结点不存在（即e==null），或者查找到的旧值不存在（v==null），或者查找到结点的值与输入的旧值不相等（e.value!=oldValue）那么则返回false
        return false;
    }

    /**
     * 用新值替换指定key的旧值
     * 与replace(K key, V oldValue, V newValue)的区别是：没有oldValue参数，即只要查找到key对应的结点，那么就替换值，不要求oldValue值等于查找到的值e.value
     *
     * @param key   键
     * @param value 新值
     * @return 返回旧值
     */
    @Override
    public V replace(K key, V value) {
        // 局部变量，用来保存通过getNode()方法查找到的结点
        Node<K, V> e;
        // 如果该结点存在则进行替换
        if ((e = getNode(hash(key), key)) != null) {
            // 旧值
            V oldValue = e.value;
            // 用新值替换旧值
            e.value = value;
            // 留给LinkedHashMap使用
            afterNodeAccess(e);
            // 返回旧值
            return oldValue;
        }
        // 如果没有找到该结点（e==null）则返回null
        return null;
    }

    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K, V>[] tab;
        Node<K, V> first;
        int n, i;
        int binCount = 0;
        TreeNode<K, V> t = null;
        Node<K, V> old = null;
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K, V>) first).getTreeNode(hash, key);
            else {
                Node<K, V> e = first;
                K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        } else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K, V> e;
        V oldValue;
        int hash = hash(key);
        if ((e = getNode(hash, key)) != null &&
                (oldValue = e.value) != null) {
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                return v;
            } else
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K, V>[] tab;
        Node<K, V> first;
        int n, i;
        int binCount = 0;
        TreeNode<K, V> t = null;
        Node<K, V> old = null;
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K, V>) first).getTreeNode(hash, key);
            else {
                Node<K, V> e = first;
                K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            } else
                removeNode(hash, key, null, false, true);
        } else if (v != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K, V>[] tab;
        Node<K, V> first;
        int n, i;
        int binCount = 0;
        TreeNode<K, V> t = null;
        Node<K, V> old = null;
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K, V>) first).getTreeNode(hash, key);
            else {
                Node<K, V> e = first;
                K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            else
                v = value;
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            } else
                removeNode(hash, key, null, false, true);
            return v;
        }
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K, V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K, V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K, V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // Cloning and serialization

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K, V> result;
        try {
            result = (HashMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }

    /**
     * 得到HashMap的负载因子，该方法在HashSet中被使用
     *
     * @return 返回负载因子
     */
    final float loadFactor() {
        return loadFactor;
    }

    /**
     * 返回HashMap的容量，即哈希桶数组的长度
     *
     * @return 返回容量
     */
    final int capacity() {
        return (table != null) ? table.length : (threshold > 0) ? threshold : DEFAULT_INITIAL_CAPACITY;
        /*
            等价于（代码复杂化）
            if (table != null) {
                return table.length;
            } else {
                if (threshold > 0) {
                    return threshold;
                } else {
                    return DEFAULT_INITIAL_CAPACITY;
                }
            }
         */
    }

    /**
     * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the
     * bucket array) is emitted (int), followed by the
     * <i>size</i> (an int, the number of key-value
     * mappings), followed by the key (Object) and value (Object)
     * for each key-value mapping.  The key-value mappings are
     * emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " +
                    loadFactor);
        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                    mappings);
        else if (mappings > 0) { // (if zero, use defaults)
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float) mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                    DEFAULT_INITIAL_CAPACITY :
                    (fc >= MAXIMUM_CAPACITY) ?
                            MAXIMUM_CAPACITY :
                            tableSizeFor((int) fc));
            float ft = (float) cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                    (int) ft : Integer.MAX_VALUE);

            // Check Map.Entry[].class since it's the nearest public type to
            // what we're actually creating.
            SharedSecrets.getJavaOISAccess().checkArray(s, Map.Entry[].class, cap);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Node<K, V>[] tab = (Node<K, V>[]) new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    /* ------------------------------------------------------------ */
    // iterators

    abstract class HashIterator {
        Node<K, V> next;        // next entry to return
        Node<K, V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K, V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        /**
         * 获取下一个结点
         *
         * @return 返回下一个结点信息
         */
        final Node<K, V> nextNode() {
            // 局部变量，保存哈希桶数组table
            Node<K, V>[] t;
            // 局部变量，保存当前结点的下一个结点
            Node<K, V> e = next;
            // 如果修改次数不对，那么多线程修改了HashMap
            if (modCount != expectedModCount)
                // 则抛出异常
                throw new ConcurrentModificationException();
            // 如果下一个结点是null
            if (e == null)
                // 则抛出NoSuchElementException异常
                throw new NoSuchElementException();
            // 当前的链表遍历完了就开始遍历下一个链表
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
            // 返回下一个结点
            return e;
        }

        public final void remove() {
            Node<K, V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<K> {
        public final K next() {
            return nextNode().key;
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V> {
        public final V next() {
            return nextNode().value;
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K, V>> {
        public final Map.Entry<K, V> next() {
            return nextNode();
        }
    }

    /* ------------------------------------------------------------ */
    // spliterators

    static class HashMapSpliterator<K, V> {
        final HashMap<K, V> map;
        Node<K, V> current;          // current node
        int index;                  // current index, modified on advance/split
        int fence;                  // one past last index
        int est;                    // size estimate
        int expectedModCount;       // for comodification checks

        HashMapSpliterator(HashMap<K, V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K, V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K, V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K, V> extends HashMapSpliterator<K, V> implements Spliterator<K> {
        KeySpliterator(HashMap<K, V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K, V> m = map;
            Node<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            } else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K, V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K, V> extends HashMapSpliterator<K, V> implements Spliterator<V> {
        ValueSpliterator(HashMap<K, V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K, V> m = map;
            Node<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            } else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K, V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K, V> extends HashMapSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(HashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null : new EntrySpliterator<>(map, lo, index = mid, est >>>= 1, expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K, V> m = map;
            Node<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            } else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K, V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K, V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap support
    /* 下面的方法被设计用于LinkedHashMap类重写 0 */

    // Create a regular (non-tree) node
    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);
    }

    // For conversion from TreeNodes to plain nodes
    Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // Create a tree bin node
    TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // For treeifyBin
    TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * Reset to initial default state.  Called by clone and readObject.
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K, V> p) {
    }

    void afterNodeInsertion(boolean evict) {
    }

    void afterNodeRemoval(Node<K, V> p) {
    }

    // Called only from writeObject, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K, V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }
    // LinkedHashMap support
    /* 上面的方法被设计用于LinkedHashMap类重写 1 */
    /* ------------------------------------------------------------ */

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * extends Node) so can be used as extension of either regular or
     * linked node.
     */
    static final class TreeNode<K, V> extends LinkedHashMap.Entry<K, V> {
        // 父节点
        TreeNode<K, V> parent;
        // 左孩子结点
        TreeNode<K, V> left;
        // 右孩子结点
        TreeNode<K, V> right;
        // 前驱结点
        TreeNode<K, V> prev;
        // 红黑标识
        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        /**
         * 返回红黑树的根节点
         *
         * @return 根节点
         */
        final TreeNode<K, V> root() {
            /*
                红黑树的根节点如何判断：
                    没有父结点的结点就是根节点，即如果某结点的parent为null，那么该结点就是根结点
             */
            // 从this处向上循环遍历，即获取该结点的父结点
            for (TreeNode<K, V> r = this, p; ; ) {
                // 判断该结点的父结点是否为null，如果结果为true，那么该结点就是根节点
                if ((p = r.parent) == null)
                    // 返回结点即可
                    return r;
                // 开始下一次循环遍历
                r = p;
            }
        }

        /**
         * 确保root结点是红黑树的头结点，在树化的过程中，所有结点的prev、next指向没有改变
         * @param tab 哈希桶数组
         * @param root root结点
         * @param <K> 泛型，键
         * @param <V> 泛型，值
         */
        static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            // 局部变量，存放哈希桶数组tab的长度length
            int n;
            // 校验root是否为空、哈希桶数组tab是否为空、哈希桶数组tab的长度length是否大于0，如果只要有一个不满足，则毫无意义
            if (root != null && tab != null && (n = tab.length) > 0) {
                // 计算root结点在哈希桶数组中的位置（下标）
                int index = (n - 1) & root.hash;
                // 得到数组位置的结点，也就是头结点
                TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
                // 如果该索引位置的头结点不是root结点，则将该索引位置的头结点替换为root结点
                if (root != first) {
                    // 局部变量，存放root结点的后继结点，rn的全称是root next
                    Node<K, V> rn;
                    // 将索引位置的头结点置为root结点
                    tab[index] = root;
                    // 局部变量，存放root结点的前驱结点，rp的全称是root previous
                    TreeNode<K, V> rp = root.prev;
                    // 如果root结点的后继结点不为空，
                    if ((rn = root.next) != null)
                        // 则将rn结点的prev指针指向rp
                        ((TreeNode<K, V>) rn).prev = rp;
                    // 如果root结点的prev结点不为空，则将rp结点的next指针指向root结点的next结点
                    if (rp != null)
                        rp.next = rn;
                    // 如果索引位置原头结点不为空，则将原头结点的prev指针指向root结点
                    if (first != null)
                        first.prev = root;
                    // 将root结点的next指针指向原头结点first
                    root.next = first;
                    // root此时已经是该索引位置的头结点，因此它的前驱指针应该为null
                    root.prev = null;
                }
                // 检查红黑树树是否正常
                assert checkInvariants(root);
            }
        }

        /**
         * 根据给定的hash值和key值在红黑树中查找符合的结点
         *
         * @param h  哈希值
         * @param k  键
         * @param kc
         * @return 键（k）的Class对象，该Class应该是实现了Comparable<K>的，否则应该是null
         */
        final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
            // 局部变量，保存当前调用了该方法的结点对象
            TreeNode<K, V> p = this;
            // 从当前结点p开始循环遍历红黑树
            do {
                // 局部变量，ph指的是当前结点哈希值，dir指的是当前结点的方向（左右）
                int ph, dir;
                // 局部变量，当前结点的键key
                K pk;
                // pl是当前结点的左孩子结点，pr是当前结点的右孩子结点，q是定义一个对象q用来存储并返回找到的对象
                TreeNode<K, V> pl = p.left, pr = p.right, q;
                // 当前结点的哈希值是否大于输入的哈希值
                if ((ph = p.hash) > h)
                    // 则向p结点的左边遍历
                    p = pl;
                    // 表示当前结点的哈希值没有大于输入的哈希值，则判断当前结点的哈希值是否小于输入的哈希值
                else if (ph < h)
                    // 则向p结点的右边遍历
                    p = pr;
                    // 表示当前结点的哈希值既不大于输入的哈希值也不小于输入的哈希值，则判断当前结点的哈希值是否等于输入的哈希值，并且该结点的键是否等于输入的键值
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    // 那么就直接返回该结点
                    return p;
                    // pl表示左孩子结点，如果pl为null，那么p结点没有左孩子结点，因此pl为null
                else if (pl == null)
                    // 则向右遍历
                    p = pr;
                    // pr表示右孩子结点，如果pr为null，那么p结点没有右孩子结点，因此pr为null
                else if (pr == null)
                    // 则向左遍历
                    p = pl;
                /*
                    kc!=null    指的是kc参数传了值，不为null
                    kc = comparableClassFor(k)) != null     kc不为空代表k实现了Comparable
                    dir = compareComparables(kc, k, pk)) != 0   k<pk则dir<0, k>pk则dir>0
                 */
                else if ((kc != null
                        || (kc = comparableClassFor(k)) != null)
                        && (dir = compareComparables(kc, k, pk)) != 0)
                    // k<pk则向左遍历(p赋值为p的左节点), 否则向右遍历
                    p = (dir < 0) ? pl : pr;
                    // 代码走到此处, 代表key所属类没有实现Comparable, 直接指定向p的右边遍历
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                    // 代码走到此处代表“pr.find(h, k, kc)”为空, 因此直接向左遍历
                else
                    p = pl;
            } while (p != null);
            // 如果没有找到，则返回空null
            return null;
        }

        /**
         * 根据指定的哈希值和key查找红黑树中的结点
         *
         * @param h 哈希值
         * @param k 键
         * @return
         */
        final TreeNode<K, V> getTreeNode(int h, Object k) {
            /*
                parent!=null    判断当前结点的父结点是否为null，如果为null则表示当前结点是根结点，如果不为null则表示当前结点不为根结点，是红黑树中的普通结点
                root()      即parent!=null得到的结果是true，那么证明该结点this不是根结点，那么调用root()方法获得根结点
                this        即parent!=null得到的结果是false，那么证明该结点this是根结点
                find(h,k,null)  从红黑树的根结点开始向整棵树遍历查找符合指定hash值和key值的结点，并返回查找结果
             */
            return ((parent != null) ? root() : this).find(h, k, null);
            /*
                等价于（复杂化代码）
                if (parent != null) {
                    TreeNode<K, V> root = root();
                    TreeNode<K, V> node = root.find(h, k, null);
                    return node;
                } else {
                    TreeNode<K, V> node = this.find(h, k, null);
                    return node;
                }
             */
        }

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                    (d = a.getClass().getName().
                            compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                        -1 : 1);
            return d;
        }

        /**
         * 将双链表转换红黑树
         *
         * @param tab 哈希桶数组
         */
        final void treeify(Node<K, V>[] tab) {
            // 局部变量，保存红黑树的根结点
            TreeNode<K, V> root = null;
            // this就是调用treeify()方法的TreeNode<K, V>对象，也是该处需要转换红黑树
            // 循环遍历该双链表
            // TreeNode<K, V> next是定义的局部变量，用来存放当前结点的后继结点
            for (TreeNode<K, V> x = this, next; x != null; x = next) {
                // 用next存放当前结点的后继结点
                next = (TreeNode<K, V>) x.next;
                // 将当前结点的左孩子结点和右孩子结点置为null，因为还没有构建红黑树，孩子结点不能有值
                x.left = x.right = null;
                // 如果没有根结点，那么将x置为根结点
                if (root == null) {
                    // 既然添加的是第一个结点，那么根结点就没有父结点，并且根据红黑树的性质，根结点是黑色的
                    x.parent = null;
                    x.red = false;// true表示该结点颜色是红色的，false表示非红色（即黑色）
                    // 将该结点置为红黑树的根结点
                    root = x;
                } else {
                    // 表示红黑树至少有一个结点（根结点），在根结点下面插入其他结点
                    // 局部变量，存放当前结点x的键
                    K k = x.key;
                    // 局部变量，存放当前结点x的哈希值
                    int h = x.hash;
                    Class<?> kc = null;
                    // 从根结点开始遍历
                    for (TreeNode<K, V> p = root; ; ) {
                        // 局部变量，dir存放当前结点的方向，ph存放当前结点p的哈希值
                        int dir, ph;
                        // 局部变量，pk存放当前结点p的键
                        K pk = p.key;

                        /* 比较hash值，决定向右还是向左查找 start */
                        // 如果结点x的hash值小于p结点的hash值
                        if ((ph = p.hash) > h)
                            // 则将dir赋值为-1, 代表向p的左边查找
                            dir = -1;
                        // 如果结点x的hash值大于p结点的hash值
                        else if (ph < h)
                            // 则将dir赋值为1, 代表向p的右边查找
                            dir = 1;
                        // 表示结点x的hash值和结点p的hash值相等，下面比较key直
                        else if ((kc == null && // 如果k没有实现Comparable接口 或者 x结点的key和p结点的key相等
                                (kc = comparableClassFor(k)) == null) ||
                                (dir = compareComparables(kc, k, pk)) == 0)
                            // 使用定义的一套规则来比较x节点和p节点的大小，用来决定向左还是向右查找
                            dir = tieBreakOrder(k, pk);
                        /* 比较hash值，决定向右还是向左查找 end */

                        // 局部变量，保存x结点的父结点
                        TreeNode<K, V> xp = p;
                        // dir<=0则向p结点的左边查找，否则向p结点的右边查找
                        // 如果为null，则代表该位置即为x的目标位置
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            // x的父结点即为最后一次遍历的p结点
                            x.parent = xp;
                            // 如果dir<=0，则代表x结点是父结点的左子结点
                            if (dir <= 0)
                                xp.left = x;
                            // 表示dir>0，则代表x结点是父结点的右子结点
                            else
                                xp.right = x;
                            // 插入元素后，对红黑树进行平衡处理（变色、左旋、右旋）
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            // 如果root结点不在table索引位置的头结点, 则将其调整为头结点
            moveRootToFront(tab, root);
        }

        /**
         * 将红黑树转换成链表结构，在结点个数小于等于6时触发
         * @param map Map集合
         * @return 返回转换完成的链表
         */
        final Node<K, V> untreeify(HashMap<K, V> map) {
            // 局部变量，hd指的是链表的头结点，tl指的是链表的尾结点
            Node<K, V> hd = null, tl = null;
            // this指的是当前调用该方法的Node<K, V>对象，是一棵红黑树
            // 循环遍历该红黑树
            for (Node<K, V> q = this; q != null; q = q.next) {
                // 构造一个链表结点p，replacementNode()方法就是构造一个链表结点，内容与q结点一样，但next指针指向null
                Node<K, V> p = map.replacementNode(q, null);
                // 如果tl为null，则代表当前结点是第一个结点，即链表是空表
                if (tl == null)
                    // 那么将结点p置为链表的头结点
                    hd = p;
                // 表示链表中已有结点
                else
                    // 则将尾结点的next指针指向新待添加的结点p
                    tl.next = p;
                // 然后将新结点p置为尾结点
                tl = p;
            }
            // 最后返回转换成功的链表hd
            return hd;
        }

        /**
         * Tree version of putVal.
         */
        final TreeNode<K, V> putTreeVal(HashMap<K, V> map, Node<K, V>[] tab,
                                        int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K, V> root = (parent != null) ? root() : this;
            for (TreeNode<K, V> p = root; ; ) {
                int dir, ph;
                K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                        (kc = comparableClassFor(k)) == null) ||
                        (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K, V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                                (q = ch.find(h, k, kc)) != null) ||
                                ((ch = p.right) != null &&
                                        (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K, V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K, V> xpn = xp.next;
                    TreeNode<K, V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K, V>) xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * 删除红黑树中指定的结点
         *
         * @param map     该HashMap对象
         * @param tab     该HashMap对象中的table数组
         * @param movable 如果为false，在删除时不移动其他节点
         */
        final void removeTreeNode(HashMap<K, V> map, Node<K, V>[] tab, boolean movable) {
            /* --------- 链表的处理 0 --------- */
            // 局部变量，保存table数组的长度
            int n;
            // 判断table是否为空
            if (tab == null || (n = tab.length) == 0)
                // 如果为空，则直接返回，空数组都没有元素则无法进行删除
                return;
            // 获取要被删除结点的索引（在哈希桶数组中的下标）
            int index = (n - 1) & hash;
            // first是哈希桶数组中根据索引得到的第一个结点，是红黑树的根结点，也是第一个结点；root是根结点；rl是root结点的左子结点
            TreeNode<K, V> first = (TreeNode<K, V>) tab[index], root = first, rl;
            // this是要被删除的node结点，next是this的下一个结点，因此succ是当前待被删除结点的后继结点，pred是当前待被删除结点的前驱结点
            TreeNode<K, V> succ = (TreeNode<K, V>) next, pred = prev;
            /* 删除当前结点 0 */
            // 如果pred为空，表示当前待被移除的结点是头结点，因为头结点的前驱结点为null，尾结点的后继结点为null
            if (pred == null)
                // 则将tab[index]和first结点的值置为succ结点（即当前待被删除结点的next结点）
                tab[index] = first = succ;
            // 表示不是头结点，那么
            else
                // 将前驱结点的next指针指向后继结点，直接删除掉当前待被删除结点
                pred.next = succ;
            // 由于是双链表，因此还需要将后继结点的prev指针指向前驱结点pred
            if (succ != null)
                succ.prev = pred;
            /* 删除当前结点 1 */
            // 如果first结点为空，则表示该索引位置没有结点，直接返回，那么后面的操作也不会进行了
            if (first == null)
                return;
            // 如果root结点还存在父节点，表示root结点不是红黑树的根结点
            if (root.parent != null)
                // 那么将该红黑树的根结点查询出来，重新赋给root结点
                // root()方法是查询一棵红黑树的根结点，从给定结点向上遍历
                root = root.root();
            // 通过root结点来判断红黑树是否太小
            if (root == null || root.right == null ||
                    (rl = root.left) == null || rl.left == null) {
                // 如果红黑树太小，那么直接将红黑树转换成链表结构赋给tab[index]
                tab[index] = first.untreeify(map);  // too small
                // 既然转成了链表结构，那么就不需要执行后面的红黑树处理了
                return;
            }
            /* --------- 链表的处理 1 --------- */
            /* --------- 红黑树的处理 0 --------- */
            // 局部变量，p指的是当前要被移除的结点，pl指的是p结点的左子结点，pr指的是p结点的右子结点，replacement指的是
            TreeNode<K, V> p = this, pl = left, pr = right, replacement;
            // 如果当前待被删除结点的左右子结点都不为空
            if (pl != null && pr != null) {
                // 局部变量，s存放的是p结点的右子结点，sl存放的是s结点的左子结点
                TreeNode<K, V> s = pr, sl;
                // 一直向p结点的右子结点的左子树查找，直到查找最左的一个结点
                while ((sl = s.left) != null)
                    // 然后用s来保存这个结点，这个结点将用来交换待被删除的结点
                    s = sl;
                // 局部变量，保存s结点的颜色
                // 下面三句是交换s结点和p结点的颜色
                boolean c = s.red;
                s.red = p.red;
                p.red = c;
                // 局部变量，sr存放的是s结点的右子结点
                TreeNode<K, V> sr = s.right;
                // 局部变量，pp存放的是p结点的父节点
                TreeNode<K, V> pp = p.parent;
                // 第一次调整和第二次调整：将p结点和s结点进行交换
                // 第一次调整
                // 如果p结点的右子结点为s结点，则将p的父节点指向s，将s的右子结点指向p
                if (s == pr) {
                    p.parent = s;
                    s.right = p;
                // 表示p结点的右子结点不是s结点
                } else {
                    // 局部变量，sp存放s结点的父结点
                    TreeNode<K, V> sp = s.parent;
                    // 将sp赋给p结点的父结点
                    if ((p.parent = sp) != null) {
                        // 如果s结点等于sp结点的左子结点
                        if (s == sp.left)
                            // 那么就将sp的左子结点赋为p结点
                            sp.left = p;
                        // 如果不等于，表示s结点是sp结点的右子结点
                        else
                            // 那么将sp的右子结点赋值为p结点
                            sp.right = p;
                    }
                    // s的右子结点赋值为p结点的右子结点
                    if ((s.right = pr) != null)
                        // 如果pr不为空，则将pr的父结点赋值为s
                        pr.parent = s;
                }
                // 第二次调整
                // 将p的左子结点赋值为null，pl已经保存了该结点的信息
                p.left = null;
                // 将p结点的右子结点赋值为sr，如果不为null
                if ((p.right = sr) != null)
                    // 则将sr的父结点赋值为p结点
                    sr.parent = p;
                // 将p结点的左子结点赋值为pl，如果pl不为空
                if ((s.left = pl) != null)
                    // 则将pl的父结点赋值为s结点
                    pl.parent = s;
                // 将s结点的父结点赋值为p的父结点pp，如果pp为空
                if ((s.parent = pp) == null)
                    // 则表示p结点为根结点，那么将s成为新的root结点
                    root = s;
                // 表示不是根结点，并且p是pp的左子结点
                else if (p == pp.left)
                    // 那么将pp的左子结点赋值为s结点
                    pp.left = s;
                // 表示不是根结点，并且p是pp的右子结点
                else
                    // 那么将pp的右子结点赋值为s结点
                    pp.right = s;
                // 寻找replacement结点，用来替换p结点
                // 如果sr不为空，则replacement结点为sr，因为s没有左子结点，所以使用s的右子结点来替换p结点的位置
                if (sr != null)
                    replacement = sr;
                // 如果sr为空，则s为叶子结点，replacement为p结点本身，只需要将p结点直接去除即可
                else
                    replacement = p;
            // 如果p的左子结点不为空，右子结点为空，则replacement结点为p的左子结点
            } else if (pl != null)
                replacement = pl;
            // 如果p的右子结点不为空，左子结点为空，则replacement结点为p的右子结点
            else if (pr != null)
                replacement = pr;
            // 如果p的左右子结点都为空，则p为叶子结点，replacement结点为p结点本身
            else
                replacement = p;
            // 第三次调整：使用replacement结点替换掉p结点的位置，将p结点移除
            // 如果p结点不是叶子结点
            if (replacement != p) {
                // 将p结点的父结点赋值给replacement结点的父结点，同时赋值给pp结点
                TreeNode<K, V> pp = replacement.parent = p.parent;
                // 如果p结点没有父结点，即p为根结点，则将root结点赋值为replacement结点即可
                if (pp == null)
                    root = replacement;
                // 如果p不是root结点，并且p是pp的左子结点，则将pp的左子结点赋值为替换结点replacement
                else if (p == pp.left)
                    pp.left = replacement;
                // 如果p不是root结点，并且p是pp的右子结点，则将pp的右子结点赋值为替换结点replacement
                else
                    pp.right = replacement;
                // p结点的位置已经被替换为replacement，将p结点清空，以便回收
                p.left = p.right = p.parent = null;
            }
            // 如果p结点不是红色，则进行红黑树删除平衡调整
            // 如果删除的结点是红色结点，那么不好破坏红黑树的平衡
            TreeNode<K, V> r = p.red ? root : balanceDeletion(root, replacement);
            // 如果p结点是叶子结点，则简单的将p结点删除即可
            if (replacement == p) {
                TreeNode<K, V> pp = p.parent;
                // 将p的parent指针指向null
                p.parent = null;
                if (pp != null) {
                    // 如果p结点是父结点的左子结点，则将父结点的左子结点置为空
                    if (p == pp.left)
                        pp.left = null;
                    // 如果p结点是父结点的右子结点，则将父结点的右子结点置为空
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            // 如果movable是true
            if (movable)
                // 则将root结点移到索引位置的头结点
                moveRootToFront(tab, r);
        }

        /**
         * 拆分红黑树
         *
         * @param map 当前的HashMap
         * @param tab 新哈希桶数组
         * @param index 需要被拆分的红黑树在旧哈希桶数组中的索引位置
         * @param bit 旧哈希桶容量
         */
        final void split(HashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
            // this是当前调用split()方法的红黑树对象
            TreeNode<K, V> b = this;
            // Relink into lo and hi lists, preserving order
            // 分割后的红黑树，其实是连接成了两个链表（单链表）
            // loHead是e.hash&bit==0的单链表的头结点，loTail是尾结点
            // hiHead是e.hash&bit!=0的单链表的头结点，hiTail是尾结点
            TreeNode<K, V> loHead = null, loTail = null;
            TreeNode<K, V> hiHead = null, hiTail = null;
            // 局部变量，lc是计数器，统计loHead链表的结点个数；lc也是计数器，统计hiHead链表的结点个数
            int lc = 0, hc = 0;
            // 循环遍历红黑树，分割成两个红黑树
            // 注意：虽然是分割的是两个红黑树，但它们的联系是通过链表的next指针来维系的
            for (TreeNode<K, V> e = b, next; e != null; e = next) {
                // 存放当前结点e的下一个结点
                next = (TreeNode<K, V>) e.next;
                // 将旧表的next指针指向设为null
                e.next = null;
                // 以e.hash&bit是否等于0将红黑树结点进行分割
                if ((e.hash & bit) == 0) {
                    // 如果loTail为空，表示该结点是第一个结点
                    if ((e.prev = loTail) == null)
                        // 将e赋给loHead作为第一个结点
                        loHead = e;
                    // 表示至少存在一个结点，将其他结点添加在后面
                    else
                        // 挂在已有结点的后面
                        loTail.next = e;
                    // 最后将尾指针指向新添加的结点e
                    loTail = e;
                    // lc计数器加1，表示又添加了一个结点
                    ++lc;
                // 表示e.hash&bit不等于0的结点
                } else {
                    // 如果hiTail为null，表示是第一个待添加的结点
                    if ((e.prev = hiTail) == null)
                        // 将e置为头结点
                        hiHead = e;
                    // 表示链表中有其他结点，只需要将新加入的结点连接在后面即可
                    else
                        // 连接新插入结点e到原尾结点后面
                        hiTail.next = e;
                    // 将尾指针指向新添加的结点e
                    hiTail = e;
                    // hc计数器加1，表示hiHead链表又添加了一个元素
                    ++hc;
                }
            }

            // 表示loHead这个红黑树不为空
            if (loHead != null) {
                // 判断该红黑树的结点元素个数是否小于等于阈值6
                if (lc <= UNTREEIFY_THRESHOLD)
                    // 如果小于则将红黑树转换成链表结构，并保存在tab[index]索引处
                    tab[index] = loHead.untreeify(map);
                // 表示红黑树的结点元素个数大于阈值6
                else {
                    // 不用转换成链表，直接将该红黑树保存在tab[index]索引处
                    tab[index] = loHead;
                    // 执行到这里，表示旧表的红黑树已经分割成了两个，红黑树的平衡已经被打破了，所以需要重新调整平衡构建新的红黑树
                    if (hiHead != null)
                        // 以loHead尾根结点，构造新的红黑树
                        loHead.treeify(tab);
                }
            }
            // 表示hiHead这颗红黑树不为空
            if (hiHead != null) {
                // 判断该红黑树的结点元素个数是否小于等于阈值6
                if (hc <= UNTREEIFY_THRESHOLD)
                    // 如果小于，则将红黑树转换成链表结构，保存在tab[index+bit]位置
                    tab[index + bit] = hiHead.untreeify(map);
                // 表示大于阈值6
                else {
                    // 不用转换成链表，直接存放该红黑树于tab[index+bit]位置
                    tab[index + bit] = hiHead;
                    // loHead不为空代表旧红黑树已经被分割，需要重新构建新的红黑树
                    if (loHead != null)
                        // 以hiHead为根结点，构建新的红黑树
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        /**
         * 将红黑树以p为支点左旋
         * @param root 红黑树
         * @param p 支点
         * @param <K> 泛型，键
         * @param <V> 泛型，值
         * @return 返回左旋完成后的红黑树
         */
        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
            // 局部变量，r是存放p结点的右子结点，pp是存放p结点的父结点，rl是存放r结点的左子结点
            TreeNode<K, V> r, pp, rl;
            // 如果支点p或支点p的右子结点为null，那么无法进行旋转，直接返回
            if (p != null && (r = p.right) != null) {
                /*
                    1. p的右子结点指向r的左子结点（即rl），如果rl不为空，其父结点指向支点p
                    2. r的父结点指向p的父结点pp
                        2.1 如果pp为null，说明p结点是根结点，直接将root指向r，同时将根结点颜色设为黑色
                        2.2 如果pp的右孩子为p，则将pp的右子结点指向r
                        2.3 如果pp的左孩子为p，则将pp的左子结点指向r
                    3. 将r的左子结点指向p
                    4. 将p的父节点指向r
                 */

                if ((rl = p.right = r.left) != null)
                    rl.parent = p;

                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;

                r.left = p;
                p.parent = r;
            }
            // 返回红黑树
            return root;
        }

        /**
         * 以结点p为支点进行右旋
         * @param root 要进行右旋的红黑树
         * @param p 支点
         * @param <K> 泛型，键
         * @param <V> 泛型，值
         * @return 返回右旋完成后的红黑树
         */
        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
            // 局部变量，l指结点p的左子结点，pp指结点p的父结点，lr指结点l的右子结点
            TreeNode<K, V> l, pp, lr;
            // 支点p不能为空，支点p的左子结点不能为空，这时左旋的必备条件，否则无法左旋
            if (p != null && (l = p.left) != null) {
                /*
                    1. p的左子结点指向l的右子结点（lr），如果lr不为空，它的父结点指向p
                    2. l的父结点指向p的父结点
                        2.1 如果pp为null，则说明p为根结点，直接root指向l，同时将颜色置为黑色，因为红黑树根结点颜色为黑色
                        2.2 如果pp的右孩子为p，则将pp的右孩子指向l
                        2.3 如果pp的左孩子为p，则将pp的左孩子指向l
                    3. 将l的右孩子指向p
                    4. 将p的父结点指向l
                 */
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;

                if ((pp = l.parent = p.parent) == null)
                    // 表示p是根结点，将root指向l，同时将根结点root颜色置为黑色
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;

                l.right = p;
                p.parent = l;
            }
            // 返回右旋完成的红黑树
            return root;
        }

        /**
         * 插入结点后，对红黑树进行平衡调整
         * @param root 红黑树的根结点
         * @param x 新插入的结点
         * @param <K> 泛型，键
         * @param <V> 泛型，值
         * @return 返回调整后的已经平衡的红黑树
         */
        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
            // 将新插入的结点x置为红色，一般来说，红黑树新插入的结点颜色都是红色的
            x.red = true;
            /*
                局部变量：
                        x——新插入的结点（为了便于同红黑树的插入调整图理解，就是N结点）
                        xp——x结点的父结点
                        xpp——x结点的祖父结点
                        xppl——x结点的叔叔结点，是祖父结点的左子结点
                        xppr——x结点的叔叔结点，是祖父结点的右子结点
             */
            // 死循环，仅通过return跳出
            for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
                // 【第一种情况】N为根结点
                // xp==null如果为真，表示x结点是根结点，根结点没有父结点，因此x.parent为null
                if ((xp = x.parent) == null) {
                    // 调整方法：染黑N，即染黑x结点
                    // x.red为true表示红色，为false表示黑色
                    x.red = false;
                    // 调整完成，返回
                    return x;
                // 【第二种情况】N的父结点是黑色，即xp.red为false的情况下，或者xp是根结点的情况下
                } else if (!xp.red || (xpp = xp.parent) == null)
                    // 调整方法：不需要调整
                    return root;
                // 执行到这里，表示是第三或第四种情况，x的父结点xp是红色的
                // 判断x的父结点xp是否是祖父结点xpp的左子结点
                if (xp == (xppl = xpp.left)) {
                    // 表示父结点xp是祖父结点xpp的左子结点，那么祖父结点xpp的右子结点xppr就是叔叔结点
                    if ((xppr = xpp.right) != null && xppr.red) {
                        // 在叔叔结点存在并且叔叔结点是红色的情况下
                        // 【第三种情况】：x的父节点xp是红色，x的叔叔结点xppr是红色
                        // 调整方法：染黑父节点xpp和叔叔结点xppr，染红祖父结点xpp
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        // 将祖父结点置为新的x结点，向上递归调整红黑树平衡
                        x = xpp;
                    } else {
                        // 表示叔叔结点不存在或者叔叔结点为黑色的情况下
                        // 【第四种情况】：x的父节点xp是红色，x的叔叔结点xppr是黑色或没有
                        if (x == xp.right) {
                            // 【情形三】父结点xp是祖父结点xpp的左子结点，x结点是父节点xp的右子结点
                            // 调整方法：以父节点p为支点左旋，然后转至【情形一】处理
                            // rotateLeft()方法就是左旋的，其中第一个参数是红黑树，第二个参数是要旋转的支点，返回值是旋转完成后的红黑树
                            root = rotateLeft(root, x = xp);
                            // 重新赋值x的父结点，因为旋转后，x的父结点会发生变化
                            // 重新赋值x的祖父结点，因为旋转后，x的父节点发生变化，它的祖父结点也会发生变化
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        // 转至【情形一】：父节点xp是祖父结点xpp的左子结点，x结点是父节点xp的左子结点
                        // 调整方法：以祖父结点xpp右旋，染黑原父节点xp，染红原祖父结点xpp
                        // 下面的操作是先染色，再右旋的
                        if (xp != null) {
                            // 将父节点染黑
                            xp.red = false;
                            if (xpp != null) {
                                // 将祖父结点染红
                                xpp.red = true;
                                // 以祖父结点xpp为支点右旋
                                // rotateRight()方法就是右旋红黑树的
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                // 执行到这里，表示x的父结点xp是祖父结点xpp的右子结点
                } else {
                    // 那么表示叔叔结点是xppl，是左子结点
                    if (xppl != null && xppl.red) {
                        // 在叔叔结点存在并且叔叔结点是红色的情况下
                        // 【第三种情况】：x结点的父节点xp是红色，x的叔叔结点xppl也是红色
                        // 调整方法：染黑父节点xp和叔叔结点xppl，染红祖父结点xpp
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        // 将祖父结点置为新的x结点，向上递归调整红黑树平衡
                        x = xpp;
                    // 表示叔叔结点不存在，或者叔叔结点是黑色的情况
                    } else {
                        // 【第四种情况】：x结点的父节点xp是红色，x结点的叔叔结点xppl是黑色
                        // 判断x结点是否是父结点xp的左子结点
                        if (x == xp.left) {
                            // 表示x结点是父结点的左子结点
                            // 【情形四】：父结点xp是祖父结点xpp的右子结点，x结点是父结点p的左子结点
                            // 调整方法：以父结点p为支点右旋，转至【情形二】处理
                            // rotateRight()方法是右旋红黑树的方法
                            root = rotateRight(root, x = xp);
                            // 重新赋值x的父结点，因为旋转后，x的父结点会发生变化
                            // 重新赋值x的祖父结点，因为旋转后，x的父节点发生变化，它的祖父结点也会发生变化
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        // 转至【情形二】：父结点xp是祖父结点的右子结点，x结点是父结点的右子结点
                        // 调整方法：以祖父结点xpp为支点左旋，染黑原父结点xp，染红原祖父结点xpp
                        // 下面的操作是先染色，再左旋的
                        if (xp != null) {
                            // 染黑原父结点xp
                            xp.red = false;
                            if (xpp != null) {
                                // 染黑原祖父结点xpp
                                xpp.red = true;
                                // 以祖父结点xpp为支点左旋
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        /**
         * 删除结点后，对红黑树进行平衡调整
         * @param root 红黑树的根结点
         * @param x 待删除的结点
         * @param <K> 泛型，键
         * @param <V> 泛型，值
         * @return 返回调整后的已经平衡的红黑树
         */
        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
            /*
                x——被删除的结点（就是图中的N结点）
                xp——x结点父结点
                xpl——叔叔结点，xp结点的左子结点
                xpr——叔叔结点，xp结点的右子结点
             */
            // 死循环
            for (TreeNode<K, V> xp, xpl, xpr; ; ) {
                if (x == null || x == root)
                    // 那么无需调整，直接返回该红黑树
                    return root;
                /*
                    【第一种情况】：结点x是根结点
                    调整方法：无需调整平衡
                 */
                // x结点的父结点是null，表示结点x是根结点
                else if ((xp = x.parent) == null) {
                    // 所以需要将根结点的颜色置为黑色
                    x.red = false;
                    return x;
                /*
                  x 的颜色为红色的，设置x的颜色为黑色即可
                     1、此时x可能是删除的元素本身，这时候元素已经移除了，设置颜色也不会影响；
                     2、x也可能是删除元素的左节点，或者右节点，此时已经使用x替换了删除元素的位置，只要把颜色设置为黑色即可
                     3、上溯过程中，如果x的颜色是红色的，设置为黑色的即可
                 */
                } else if (x.red) {
                    x.red = false;
                    return root;
                // 如果x结点是父结点xp的左子结点
                } else if ((xpl = xp.left) == x) {
                    // 那么兄弟结点xpr是父结点xp的右子结点
                    // 如果x结点的兄弟结点xpr存在并且颜色是红色的
                    /*
                        【第三种情况-情况二】：结点x的兄弟结点xpr是红色的，兄弟结点xpr是父结点的右子结点
                        调整方法：以父结点xp为支点左旋，交换xp和xpr的颜色，x的兄弟结点xpr变为黑色，转至【第二种情况】
                     */
                    if ((xpr = xp.right) != null && xpr.red) {
                        // 将兄弟结点xpr颜色设为黑色
                        xpr.red = false;
                        // 将父结点xp颜色设为红色
                        xp.red = true;
                        // 以结点xp为支点进行左旋
                        root = rotateLeft(root, xp);
                        // 如果x结点没有父结点，那么将xpr置为null，否则为父结点xp的右子结点
                        // 这一步的目的是因为旋转后，结点发生变化，所以重新设置兄弟结点xpr和父结点xp
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    // 下面是转至【第二种情况】的处理
                    if (xpr == null)
                        x = xp;
                    else {
                        // 局部变量，sl存放的是兄弟结点xpr的左子结点，sr存放的是兄弟结点xpr的右子结点
                        TreeNode<K, V> sl = xpr.left, sr = xpr.right;
                        /*
                            【第二种情况-情况一-情形二】：结点x的兄弟结点xpr是黑色的，兄弟结点xpr的子结点都是黑色的，父结点xp是黑色的
                            调整方法：染红兄弟结点xpr，将父结点xp作为新的x结点，递归处理
                         */
                        if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        // sr和sl不全是黑色的，即是【情况二】
                        } else {
                            /*
                                【第二种情况-情况二-情形四】：结点x的兄弟结点xpr是黑色的，兄弟结点xpr的子结点不全是黑色的，兄弟结点xpr是父结点p的右子并且sl是红色的和sr是黑色的
                                调整方法：以兄弟结点xpr为支点右旋，交换兄弟结点xpr和兄弟结点的左子结点sl的颜色，转至【第二种情况-情况二-情形二】
                             */
                            if (sr == null || !sr.red) {
                                // 将兄弟结点的左子结点的颜色设置为黑色
                                if (sl != null)
                                    sl.red = false;
                                // 将兄弟结点的颜色设置为红色
                                xpr.red = true;
                                // 以兄弟xpr结点为支点进行右旋
                                root = rotateRight(root, xpr);
                                // 旋转后，结点位置发生变化，重新赋值xpr和xp结点
                                xpr = (xp = x.parent) == null ? null : xp.right;
                            }
                            // 转至【情况二-情形二】
                            /*
                                【第二种情况-情况二-情形二】：结点x的兄弟结点xpr是黑色的，xpr的子结点不全是黑色的，弟结点xpr是父结点p的右子并且sr是红色的（上面的!sr.read为true表示sr是黑色的，那么下面的就表示是红的情况）
                                调整方法：以父结点xp为支点左旋，交换父结点xp和兄弟结点xpr的颜色，染黑sr，平衡完成
                             */
                            if (xpr != null) {
                                // 交换xp和xpr结点的颜色
                                xpr.red = (xp == null) ? false : xp.red;// 结点xp可能是红色的，所以设置为xp的颜色
                                if ((sr = xpr.right) != null)
                                    // 染黑sr
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                // 以父结点xp为支点左旋
                                root = rotateLeft(root, xp);
                            }
                            // 通过x=toot跳出循环
                            x = root;
                        }
                    }
                // 表示x结点是父结点xp的右子结点
                } else {
                    // 那么兄弟结点xpl是父节点xp的左子结点
                    // 如果x结点的兄弟结点xplr存在并且颜色是红色的
                    /*
                        【第三种情况-情况一】：x结点的兄弟结点xpl是红色的，并且xpl是父节点xp的左子结点
                        调整方法：以父节点xp为支点右旋，交换父节点xp和兄弟结点xpl的颜色，x的兄弟结点xpl变为黑色，转至【第二种情况】
                     */
                    if (xpl != null && xpl.red) {
                        // 交换xp和xpl的颜色
                        xpl.red = false;
                        xp.red = true;
                        // 以父结点xp为支点进行右旋
                        root = rotateRight(root, xp);
                        // 由于旋转后，结点发生变化，所以重新设置xp和xpl
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    // 下面是转至【第二种情况】的处理
                    if (xpl == null)
                        x = xp;
                    else {
                        // 局部变量，sl存放的兄弟结点xpl的左子结点，sr存放的兄弟结点xpl的右子结点
                        TreeNode<K, V> sl = xpl.left, sr = xpl.right;
                        /*
                            【第二种情况-情况一-情形二】：结点x的兄弟结点xpl是黑色的，兄弟结点xpl的子结点都是黑色的，父结点xp是黑色的
                            调整方法：染红兄弟结点xpl，将父结点xp作为新的x结点，递归处理
                         */
                        if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        // sr和sl不全是黑色的，即是【情况二】
                        } else {
                            /*
                                【第二种情况-情况二-情形三】：结点x的兄弟结点xpl是黑色的，xpl的子结点不全是黑色的，兄弟结点xpl是父结点xp的左子结点，并且sl是黑色的
                                调整方法：以兄弟结点xpl为支点左旋，交换兄弟结点xpl和sr的颜色，转至【第二种情况-情况二-情形一】
                             */
                            if (sl == null || !sl.red) {// 只有当sl.red为false时（即sl是黑色），!sl.red才是true
                                // 交换xpl和sr的颜色
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                // 以兄弟结点xpl为支点左旋
                                root = rotateLeft(root, xpl);
                                // 旋转后，结点发生变化，所以重置xp和xpl结点
                                xpl = (xp = x.parent) == null ? null : xp.left;
                            }
                            // 下面是转至【情况二-情形一】
                            /*
                                【第二种情况-情况二-情形一】：结点x的兄弟结点xpl是黑色的，xpl的子结点不全是黑色的，兄弟结点xpl是父结点xp的左子结点，并且sl是红色的
                                调整方法：以父结点xp为支点右旋，交换父结点xp和兄弟结点xpl颜色，染黑sl，平衡完成
                             */
                            if (xpl != null) {// 执行到这里，表示sl的颜色是红色
                                // 交换xp和xpl的颜色
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    // 染黑sl
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                // 以父结点xp为支点右旋
                                root = rotateRight(root, xp);
                            }
                            // 通过x=toot跳出循环
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * 检查红黑树是否一棵符合性质定义的红黑树
         * @param t 红黑树的根结点
         * @param <K> 泛型，键
         * @param <V> 泛型，键
         * @return 如果正常返回true，否则返回false
         */
        static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            /*
                局部变量介绍：
                    t——传入的红黑树根结点
                    tp——t结点的父结点（红黑树中）
                    tl——t结点的左子结点（红黑树中）
                    tr——t结点的右子结点（红黑树中）
                    tb——t结点的前驱结点（双向链表中）
                    tn——t结点的后继结点（双向链表中）
             */
            TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode<K, V>) t.next;
            // 如果前驱结点存在，但前驱结点的后继结点不是当前结点t【正常情况下，当前结点的前驱结点的后继结点一定是当前结点】
            if (tb != null && tb.next != t)
                // 则返回false
                return false;
            // 如果后继结点存在，但后继结点的前驱结点不是当前结点t【正常情况下，当前结点的后继结点的前驱结点一定是当前结点】
            if (tn != null && tn.prev != t)
                // 则返回false
                return false;
            // 如果父结点存在，但父结点的左子结点和右子结点都不是当前结点t【正常情况下，父节点存在，那么当前结点一定是父节点的左子结点或右子结点】
            if (tp != null && t != tp.left && t != tp.right)
                // 则返回false
                return false;
            //如果左子结点存在，但左子结点的父节点不是当前结点t或者左子结点的hash值大于当前结点的hash值【正常情况下，当前结点的左子结点的父结点一定是当前结点，并且左子结点的hash值小于等于当前结点的hash值】
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                // 则返回false
                return false;
            // 如果右子结点存在，但右子结点的父节点不是当前结点t或者右子结点的hash值大于当前结点的hash值【正常情况下，当前结点的右子结点的父结点一定是当前结点，并且右子结点的hash值大于等于当前结点的hash值】
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                // 则返回false
                return false;
            // 如果当前结点是红色，但孩子结点也是红色【正常情况下，当前结点的颜色如果是黑色，那么子结点的颜色一定是黑色，如果是红色则违反红黑树的第五条性质】
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                // 则返回false
                return false;
            // 递归检查左子树
            if (tl != null && !checkInvariants(tl))
                return false;
            // 递归检查右子树
            if (tr != null && !checkInvariants(tr))
                return false;
            // 都正确则返回true
            return true;
        }
    }

}
/**
 * 参考资料：
 * Java集合源码分析08----HashMap源码分析 —— https://blog.csdn.net/lili13897741554/article/details/83827370
 * JDK8 HashMap源码 putMapEntries解析 —— https://blog.csdn.net/anlian523/article/details/103639094
 * Java高级之HashMap中的keySet()方法 —— https://blog.csdn.net/cnds123321/article/details/113791846
 * Java高级之HashMap中的values()方法 —— https://blog.csdn.net/cnds123321/article/details/113792213
 * Java高级之HashMap中的put()方法和putIfAbsent()方法 —— https://blog.csdn.net/cnds123321/article/details/113793574
 * 史上最详细的 JDK 1.8 HashMap 源码解析 —— https://joonwhee.blog.csdn.net/article/details/78996181
 */