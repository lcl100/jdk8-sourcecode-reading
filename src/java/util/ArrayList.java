package java.util;

import sun.misc.SharedSecrets;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/*
    ArrayList<E>
        继承
            AbstractList<E>     抽象类
        实现
            List<E>     接口，定义了List的方法
            RandomAccess    标志性接口，一旦实现了该接口，表示该实现类具有随机访问的特性，即根据数组下标快速访问到元素
            Cloneable       标志性接口，表示该类可以进行克隆
            Serializable    标志性接口，表示该类可以进行序列化
*/

/**
 * 注：如果使用IDEA调试集合源码，需要对IDEA进行设置，参考：https://blog.csdn.net/cnds123321/article/details/113732388
 */
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    /**
     * 序列版本ID
     */
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 初始化ArrayList时的默认容量大小
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 使用有参构造函数实例化时所使用的空数组，即初始化ArrayList中的数组
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 默认空elementData数组，用于ArrayList()无参构造器使用
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 在ArrayList中装元素的Object数组
     */
    transient Object[] elementData;

    /**
     * ArrayList中实际元素的个数，不是Object数组的长度
     */
    private int size;

    /**
     * 带初始容量的ArrayList构造器，实例化ArrayList
     *
     * @param initialCapacity 设定初始化容量大小，即设定ArrayList初始时能够放多少个元素，默认是10个
     * @throws IllegalArgumentException 如果输入的initialCapacity小于0，则抛出该异常
     */
    public ArrayList(int initialCapacity) {
        // 对initialCapacity的值进行校验，判断边界是否合法
        if (initialCapacity > 0) {
            // 如果传入的initialCapacity大于0，表示合法，即创建一个Object数组来存放元素，即初始化一个给定大小的数组
            // 等价于：Object[] elementData=new Object[initialCapacity];
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            // 如果传入的initialCapacity等于0，实际上就是实例化一个空的Object数组
            // 等价于：Object[] elementData={};
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            // 如果传入的initialCapacity小于0，那么抛出IllegalArgumentException异常
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    /**
     * 无参构造器
     */
    public ArrayList() {
        // 初始化Object数组
        // 等价于：Object[] elementData={};
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * 带Collection<? extends E>参数的构造器
     *
     * @param c 可以传入是Collection子类的集合，比如ArrayList等
     *          注：Arrays.copyOf()方法的说明：https://blog.csdn.net/cnds123321/article/details/113738103
     */

    public ArrayList(Collection<? extends E> c) {
        // c.toArray()是将传入的集合转换成一个数组，然后赋给elementData
        elementData = c.toArray();
        // 将elementData数组的长度赋给ArrayList的size，size指的是ArrayList中元素的实际个数，并判断是否等于0
        if ((size = elementData.length) != 0) {
            // 如果ArrayList中实际的元素个数不等于0，先判断elementData类是否是Object[]类型
            // 下面这个判断语句说明可以参考博客：https://blog.csdn.net/cnds123321/article/details/113730763
            if (elementData.getClass() != Object[].class)

/*
                    为什么要重新复制一个数组呢？
                        1.因为触发这个复制的条件是elementData的类型类不是Object[]的类型类，例如elementData是String[]类型的，那么就会触发
                        2.Arrays.copyOf()方法返回的是一个Object[]类型的数据，符合要求，即将不是Object[]类型的elementData数组转换成Object类型的
                        3.但事实上，几乎不可能触发，因为elementData来自于c.toArray()，而toArray()返回的是一个Object[]数组
                 */

                // 复制一个指定长度的数组，然后赋给elementData
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // 如果传入的是一个空的集合，即elementData.length等于0，那么还是初始化一个空数组
            // 等价于：Object[] elementData={};
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * 将ArrayList的实际容量调整为实际元素总个数大小，原是数组容量大小
     * 该方法更加详细的说明，请参考博客：https://blog.csdn.net/cnds123321/article/details/113735261
     */
    public void trimToSize() {
        // modCount用来记录修改次数，是父类AbstractList中的属性
        modCount++;// 修改次数加1
        // size是ArrayList中实际有的元素的个数；elementData.length是数组的长度
        // 如果ArrayList中实际元素个数小于数组长度，那么就需要清除掉数组中空的元素，然后将数组长度设置为ArrayList中实际元素个数
        if (size < elementData.length) {
            // 又是一个三元表达式，判断ArrayList中是否没有元素，即0个元素，则将elementData设置为一个空数组
            // 如果元素个数大于0，则调用Arrays.copyOf()方法重新生成一个长度为size的数组，然后赋给elementData
            elementData = (size == 0) ? EMPTY_ELEMENTDATA : Arrays.copyOf(elementData, size);
        }
    }

    /**
     * 确保容量的有效性在add()方法中应用
     *
     * @param minCapacity 最小容量
     */
    public void ensureCapacity(int minCapacity) {
        // 局部变量，表示最小扩展容量
        // 如果elementData数组不是空数组则返回0，否则返回默认容量值10
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                ? 0
                : DEFAULT_CAPACITY;
        // 如果最小容量大于最小扩展容量
        if (minCapacity > minExpand) {
            // 则进行扩容
            ensureExplicitCapacity(minCapacity);
        }
    }

    /**
     * 计算容量
     *
     * @param elementData
     * @param minCapacity
     * @return
     */
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        // 如果elementData为{}，则选10和minCapacity中的最大值返回
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        // 如果不为{}，在返回minCapacity
        return minCapacity;
    }

    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    /**
     * 进行扩容处理
     *
     * @param minCapacity 最小容量
     */
    private void ensureExplicitCapacity(int minCapacity) {
        // 修改次数modCount++
        modCount++;
        // 如果给出的最小容量已经大于elementData数组的长度了
        if (minCapacity - elementData.length > 0)
            // 那么调用grow()方法进行扩容
            grow(minCapacity);
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 真正的扩容方法
     *
     * @param minCapacity 期望的最小容量
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        /* 可能发生溢出代码的考虑 0 */
        // 旧容量，即elementData数组的长度
        int oldCapacity = elementData.length;
        // 新容量，扩容为原来的1.5倍
        // oldCapacity >> 1等价于oldCapacity/2
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        /* 可能发生溢出代码的考虑 1 */
        // 最小容量minCapacity经常接近于数组的长度size
        // 调用Arrays.copyOf()方法复制一个新长度的数组
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    /**
     * @param minCapacity
     * @return
     */
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    /**
     * 返回ArrayList中元素的实际个数
     * 注意：size指存储在ArrayList中实际的元素个数；而elementData.length指的是数组的长度
     *
     * @return 元素个数
     */
    public int size() {
        return size;
    }

    /**
     * 判断ArrayList集合是否为空
     *
     * @return 如果为空则返回true，否则返回false
     */
    public boolean isEmpty() {
        // ArrayList为空的情况就是size为0，即一个元素也没有
        return size == 0;
    }

    /**
     * 判断ArrayList集合中是否包含某个元素
     *
     * @param o 要判断的元素
     * @return 如果集合中包含这个元素则返回true，否则返回false
     */
    public boolean contains(Object o) {
        // 调用了indexOf()方法来判断，如果集合中存在该元素则indexOf()方法返回该元素的索引，如果该元素在集合中的第一个位置，返回索引为0
        // 如果集合中不存在该元素，则indexOf()方法返回-1，所以判断的依据就是如此
        return indexOf(o) >= 0;
    }

    /**
     * 返回ArrayList中首次出现指定元素的索引，如果不包含指定元素，返回-1
     *
     * @param o 要查找的指定元素
     * @return 如果集合中存在该元素，则返回该元素第一次出现位置的索引，如果不存在，则返回-1
     */
    public int indexOf(Object o) {
/*
            说明：
                1.要判断一个元素是否为null，需要使用"=="运算符，而equals()方法只能判断对象
                2.ArrayList中可以添加null
         */

        // 判断指定元素是否为null
        if (o == null) {
            // 如果指定元素为null
            // 循环遍历ArrayList集合中的每个元素，判断它们的值是否为null，如果是则返回该元素的索引（即elementData数组的下标）
            for (int i = 0; i < size; i++)
                if (elementData[i] == null)
                    return i;
        } else {
            // 如果指定元素不为null
            // 循环遍历ArrayList集合中的每个元素，判断它们的值是否与参数o相等，使用的是equals()方法，比较的是对象，如果相等则返回对应数组下标
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        // 如果在集合中没有找到指定元素，则返回-1
        return -1;
    }

    /**
     * 返回指定元素在List集合中最后一次出现的位置索引
     *
     * @param o 指定元素
     * @return 如果找到了该元素则返回对应索引，如果没有找到或其他情况则返回-1
     */
    public int lastIndexOf(Object o) {
        // 判断给定的o是否为null
        if (o == null) {
            // 倒序循环遍历ArrayList集合
            for (int i = size - 1; i >= 0; i--)
                // 如果某个元素为null，则返回该元素的索引
                if (elementData[i] == null)
                    return i;
        } else {
            // 倒序循环遍历ArrayList集合
            for (int i = size - 1; i >= 0; i--)
                // 如果某个元素值为o，则返回该元素的索引
                if (o.equals(elementData[i]))
                    return i;
        }
        // 没有找到或者其他情况返回-1
        return -1;
    }

    /**
     * 返回当前ArrayList对象的克隆对象
     *
     * @return 一个克隆的ArrayList实例对象
     */
    public Object clone() {
        try {
            // 获取一个克隆实例
            ArrayList<?> v = (ArrayList<?>) super.clone();
            // 赋予该实例elementData属性
            v.elementData = Arrays.copyOf(elementData, size);
            // 设置modCount为0
            v.modCount = 0;
            // 返回克隆后的实例
            return v;
        } catch (CloneNotSupportedException e) {
            // 不应该发生，如果实现了Cloneable接口
            throw new InternalError(e);
        }
    }

    /**
     * 将ArrayList集合以数组形式返回
     *
     * @return 返回Object数组
     */
    public Object[] toArray() {
        // 实质上是调用了Arrays.copyOf()方法，复制了一个size长度的elementData数组
        return Arrays.copyOf(elementData, size);
    }

    /**
     * 将集合转换成指定类型的数组，注意与toArray()方法的区别
     *
     * @param a   指定类型的数组
     * @param <T> 泛型，参数类型和返回值类型
     * @return 返回指定类型的数组
     * @throws ArrayStoreException
     * @throws NullPointerException 如果给定的数组a为null，抛出空指针异常
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        // 如果给定数组a的长度小于当前ArrayList集合中的实际元素个数
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            // 创建一个新的数组，但是是给定a类型的数组
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        // 表示给定数组a的长度大于等于当前ArrayList集合中的实际元素个数，则直接复制elementData数组中的所有元素到a数组中
        System.arraycopy(elementData, 0, a, 0, size);
        // 将数组a中指定size位置的元素设置为null，不清楚下面两行代码的作用是什么
        if (a.length > size)
            a[size] = null;
        // 返回数组a
        return a;
    }

    /**
     * 返回指定索引位置的元素
     *
     * @param index 指定索引
     * @return 返回指定位置的元素
     */
    @SuppressWarnings("unchecked")
    E elementData(int index) {
        // 即返回数组elementData中指定下标位置index的元素
        return (E) elementData[index];
    }

    /**
     * 获取ArrayList集合中指定索引位置的元素
     *
     * @param index 给定的索引
     * @return 返回根据索引得到的元素
     * @throws IndexOutOfBoundsException 如果索引index越界则报错
     */
    public E get(int index) {
        // 检查索引index的范围是否越界
        rangeCheck(index);
        // 返回集合中指定索引的元素，调用elementData()方法
        return elementData(index);
    }

    /**
     * 替换指定索引位置的元素
     *
     * @param index   指定索引位置
     * @param element 替换后的元素
     * @return 返回该索引位置替换前的旧值
     * @throws IndexOutOfBoundsException 索引index越界发生的异常
     */
    public E set(int index, E element) {
        // 检查索引是否越界
        rangeCheck(index);
        // 根据索引获取元素的旧值
        E oldValue = elementData(index);
        // 将指定索引位置index的值替换为新值element
        elementData[index] = element;
        // 返回旧值
        return oldValue;
    }

    /**
     * 向ArrayList中添加元素
     *
     * @param e 待添加的元素
     * @return 添加成功返回true
     */
    public boolean add(E e) {
        // 检查size+1后是否超过数组容量
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        // 将新加入的元素赋给elementData[size]
        elementData[size++] = e;
        /*
            elementData[size++] = e;
            等价于
            elementData[size] = e;
            size++;
            说明：即如果size为0，那么添加元素后size就变成了1
         */
        // 返回true
        return true;
    }

    /**
     * 向指定索引添加元素，add()的重载方法
     *
     * @param index   指定索引
     * @param element 待添加的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see System#arraycopy(Object, int, Object, int, int)
     */
    public void add(int index, E element) {
        // 检查添加索引的有效性
        rangeCheckForAdd(index);
        // 确保size+1后容量是否超过数组长度，如果超过则进行扩容，还要将modCount++
        ensureCapacityInternal(size + 1);
        // 将elementData数组从index索引开始复制size-index个元素，到新数组elementData的index+1索引开始的size-index的元素
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        // 然后将空出来的index位置填上待添加的元素
        elementData[index] = element;
        // 元素个数加1
        size++;
    }

    /**
     * 移除ArrayList中指定索引的元素
     *
     * @param index 指定索引，即数组的下标
     * @return 返回被删除的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        // 检查index是否超出范围
        rangeCheck(index);
        // 修改次数modCount++
        modCount++;
        // 获取到数组中指定下标的元素，即旧值
        E oldValue = elementData(index);
        // 要移动的元素个数，需要将索引index之后的所有元素向前移动一个
        int numMoved = size - index - 1;
        // 如果numMoved有效
        if (numMoved > 0)
            // 那么将index+1之后的所有元素向前移动一位
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        // 让移动后空出来的位置（一般是最后一个索引位置）置为null，便于垃圾回收器回收
        elementData[--size] = null; // clear to let GC do its work
        // 然后返回被删除的旧值
        return oldValue;
    }

    /**
     * 根据元素内容来移除ArrayList中的元素
     *
     * @param o 指定内容
     * @return 如果删除成功则返回true，否则返回false
     */
    public boolean remove(Object o) {
        // 判断要删除的元素是否是null
        if (o == null) {
            // 循环遍历整个elementData数组
            for (int index = 0; index < size; index++)
                // 判断某个位置的元素是否是null
                if (elementData[index] == null) {
                    // 如果找到了元素，则调用fastRemove()删除该索引位置的元素
                    fastRemove(index);
                    // 删除成功则返回true
                    return true;
                }
            // 表示被删除的元素不是null
        } else {
            // 循环遍历整个elementData数组
            for (int index = 0; index < size; index++)
                // 判断某个位置的元素是否等于o
                if (o.equals(elementData[index])) {
                    // 如果找到了元素，则调用fastRemove()删除该索引位置的元素
                    fastRemove(index);
                    // 删除成功则返回true
                    return true;
                }
        }
        // 删除失败则返回false
        return false;
    }

    /**
     * 私有remove方法，删除指定索引的元素
     *
     * @param index 指定索引
     */
    private void fastRemove(int index) {
        // 修改次数modCount++
        modCount++;
        // 计算要移动的元素个数
        int numMoved = size - index - 1;
        if (numMoved > 0)
            // 将索引index之后的所有元素向前移动一个位置
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        // 然后将移动后空出来的元素置为null，便于垃圾回收器回收
        elementData[--size] = null;
    }

    /**
     * 清空ArrayList中所有的元素
     */
    public void clear() {
        // 修改次数modCount++
        modCount++;
        // 将elementData数组中所有元素置为null即可，便于垃圾回收器回收
        for (int i = 0; i < size; i++)
            elementData[i] = null;
        // 然后将size置为0
        size = 0;
    }

    /**
     * 添加一个集合中所有元素到ArrayList中
     *
     * @param c 集合
     * @return 如果操作改变了ArrayList集合，则返回true，否则返回false
     * @throws NullPointerException 如果给定的集合c为null则抛出空指针异常
     */
    public boolean addAll(Collection<? extends E> c) {
        // 将给定的集合c转换成数组
        Object[] a = c.toArray();
        // 其中numNew是数组a的长度
        int numNew = a.length;
        // 确保添加指定集合所有元素后新集合的容量在范围内
        ensureCapacityInternal(size + numNew);  // Increments modCount
        // 复制数组a中的所有元素到elementData中
        System.arraycopy(a, 0, elementData, size, numNew);
        // 同时集合元素个数变化，设置为size+numNew
        size += numNew;
        // 返回操作结果，如果numNew为0表示a是空数组，如果不为0则表示复制成功
        return numNew != 0;
    }

    /**
     * 添加指定集合中所有元素到指定索引位置（即批量添加）
     *
     * @param index 指定索引位置
     * @param c     指定集合
     * @return 如果ArrayList集合发生改变操作，则返回true，否则返回false
     * @throws IndexOutOfBoundsException 索引index越界异常
     * @throws NullPointerException      如果特定集合c为null，则抛出空指针异常
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        // 检测索引是否越界，如果越界则抛出异常
        rangeCheckForAdd(index);

        // 将集合c转换成Object[]数组
        Object[] a = c.toArray();
        // 数组a的长度
        int numNew = a.length;
        // 修改次数modCount++
        // 确保新添加多个元素后集合的容量在范围内
        ensureCapacityInternal(size + numNew);  // Increments modCount

        // 计算要移动的元素
        int numMoved = size - index;
        // 将index位置起的numMoved个元素向后移动，为待添加的元素腾出位置
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        // 将整个a数组复制到index起的位置
        System.arraycopy(a, 0, elementData, index, numNew);
        // 修改size
        size += numNew;
        // 返回操作结果
        return numNew != 0;
    }

    /**
     * 移除集合中指定范围内的所有元素
     *
     * @param fromIndex 起始始索引
     * @param toIndex   结束索引
     * @throws IndexOutOfBoundsException 如果索引fromIndex或toIndex超出范围(fromIndex <0 || fromIndex >= size() || toIndex > size() || toIndex<fromIndex)，则抛出越界异常
     */
    protected void removeRange(int fromIndex, int toIndex) {
        // 修改次数modCount++
        modCount++;
        // 计算要移动的元素个数
        int numMoved = size - toIndex;
        // 删除元素，即通过复制数组来完成
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

        // 将其他元素置为null，以便垃圾回收器回收
        int newSize = size - (toIndex - fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        // 将size重置为批量删除元素后的新元素个数
        size = newSize;
    }


    /**
     * 用于get、set、remove方法，对索引范围检测
     *
     * @param index 指定索引
     */
    private void rangeCheck(int index) {
        // 检测指定索引是否超过集合中实际元素个数
        // 但没有检测index为负数的情况，因为总是在数组访问之前使用，即执行elementData[index]，如果index越界会抛出索引越界异常，而不会执行到该方法这里
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 该方法用于add()和addAll()方法，用于检查给定索引index的范围
     *
     * @param index 指定索引
     */
    private void rangeCheckForAdd(int index) {
        // 如果给定索引超出实际元素个数或者index小于0
        if (index > size || index < 0)
            // 抛出索引越界异常
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 拼接索引越界异常的异常信息
     *
     * @param index 索引
     * @return 返回越界异常信息
     */
    private String outOfBoundsMsg(int index) {
        // 异常信息，就是越界的索引和实际的元素个数
        return "Index: " + index + ", Size: " + size;
    }

    /**
     * 批量删除，移除当前ArrayList中指定集合中的所有元素
     *
     * @param c 指定集合
     * @return 返回操作结果
     * @throws ClassCastException   如果传入的类型与当前ArrayList的类型不同，则抛出该异常
     * @throws NullPointerException 如果c为null则抛出空指针异常
     */
    public boolean removeAll(Collection<?> c) {
        // 检测c是否为null
        Objects.requireNonNull(c);
        // 调用batchRemove()方法批量删除【
        return batchRemove(c, false);
    }

    /**
     * 求当前ArrayList与给定集合c的交集
     *
     * @param c 给定集合c
     * @return 返回操作结果
     * @throws ClassCastException   类型异常
     * @throws NullPointerException 空指针异常，如果c为null
     */
    public boolean retainAll(Collection<?> c) {
        // 检测c是否为null，如果是则抛出空指针异常
        Objects.requireNonNull(c);
        // 调用batchRemove()方法
        return batchRemove(c, true);
    }

    /**
     * 根据给定集合c批量删除当前ArrayList中的元素
     *
     * @param c          给定集合
     * @param complement 在retainAll()方法中调用为true，在removeAll()方法中调用为false
     * @return
     */
    private boolean batchRemove(Collection<?> c, boolean complement) {
        // 当前集合的元素数组
        final Object[] elementData = this.elementData;
        // 局部变量，
        int r = 0, w = 0;
        // 操作结果，为true表示有操作改变了集合，为false表示没有
        boolean modified = false;
        try {
            // 当complement为true时，elementData中所有包含在集合c中的元素被保留
            // 当complement为false，elementData中所有不包含在集合c中的元素被保留
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // c.contains()可能会抛出异常
            if (r != size) {
                System.arraycopy(elementData, r, elementData, w, size - r);
                w += size - r;
            }
            // w是保留最后一个索引值，for循环中最后会进行w++操作，如果size==w,表明所有元素保留，返回false
            // w！=size表明w后面索引位置现在没有元素，但之前的elementData中w后面位置后元素存在，需要将这些位置置为null
            if (w != size) {
                // 置为null，便于回收
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     * instance is emitted (int), followed by all of its elements
     * (each an <tt>Object</tt>) in the proper order.
     */
    /**
     * 将ArrayList中的数据信息序列化保存
     *
     * @param s 输出流
     * @throws java.io.IOException 抛出IO异常
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        // ArrayList的 elementData 用 transient 修饰，表明不能被序列化

        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // 写出元素实际个数
        s.writeInt(size);

        // 写出elementData中所有元素按照顺序
        for (int i = 0; i < size; i++) {
            s.writeObject(elementData[i]);
        }

        // 如果modCount不等于expectedModCount，则抛出ConcurrentModificationException异常
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * 反序列化ArrayList
     *
     * @param s 输出流
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // 将elementData置为{}，如果里面有元素置为空，没有无所谓
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // 读取容量，忽略
        s.readInt();

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            int capacity = calculateCapacity(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // 读取所有元素
            for (int i = 0; i < size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the element with the specified index minus one.
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index);
        return new ListItr(index);
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * An optimized version of AbstractList.ListItr
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations.
     *
     * <p>This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for {@link #indexOf(Object)} and
     * {@link #lastIndexOf(Object)}, and all of the algorithms in the
     * {@link Collections} class can be applied to a subList.
     *
     * <p>The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException  {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                    parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize == 0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: " + index + ", Size: " + this.size;
        }

        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                    offset + this.size, this.modCount);
        }
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked") final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    /**
     * Index-based split-by-two, lazily initialized Spliterator
     */
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        /*
         * If ArrayLists were immutable, or structurally immutable (no
         * adds, removes, etc), we could implement their spliterators
         * with Arrays.spliterator. Instead we detect as much
         * interference during traversal as practical without
         * sacrificing much performance. We rely primarily on
         * modCounts. These are not guaranteed to detect concurrency
         * violations, and are sometimes overly conservative about
         * within-thread interference, but detect enough problems to
         * be worthwhile in practice. To carry this out, we (1) lazily
         * initialize fence and expectedModCount until the latest
         * point that we need to commit to the state we are checking
         * against; thus improving precision.  (This doesn't apply to
         * SubLists, that create spliterators with current non-lazy
         * values).  (2) We perform only a single
         * ConcurrentModificationException check at the end of forEach
         * (the most performance-sensitive method). When using forEach
         * (as opposed to iterators), we can normally only detect
         * interference after actions, not before. Further
         * CME-triggering checks apply to all other possible
         * violations of assumptions for example null or too-small
         * elementData array given its size(), that could only have
         * occurred due to interference.  This allows the inner loop
         * of forEach to run without any further checks, and
         * simplifies lambda-resolution. While this does entail a
         * number of checks, note that in the common case of
         * list.stream().forEach(a), no checks or other computation
         * occur anywhere other than inside forEach itself.  The other
         * less-often-used methods cannot take advantage of most of
         * these streamlinings.
         */

        private final ArrayList<E> list;
        private int index; // current index, modified on advance/split
        private int fence; // -1 until used; then one past last index
        private int expectedModCount; // initialized when fence set

        /**
         * Create new spliterator covering the given  range
         */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                    new ArrayListSpliterator<E>(list, lo, index = mid,
                            expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E) list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst;
            Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                } else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked") final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i = 0, j = 0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k = newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
