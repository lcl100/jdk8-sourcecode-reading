/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util;

import java.util.function.Consumer;

/**
 * Doubly-linked list implementation of the {@code List} and {@code Deque}
 * interfaces.  Implements all optional list operations, and permits all
 * elements (including {@code null}).
 *
 * <p>All of the operations perform as could be expected for a doubly-linked
 * list.  Operations that index into the list will traverse the list from
 * the beginning or the end, whichever is closer to the specified index.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a linked list concurrently, and at least
 * one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more elements; merely setting the value of
 * an element is not a structural modification.)  This is typically
 * accomplished by synchronizing on some object that naturally
 * encapsulates the list.
 * <p>
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new LinkedList(...));</pre>
 *
 * <p>The iterators returned by this class's {@code iterator} and
 * {@code listIterator} methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in
 * any way except through the Iterator's own {@code remove} or
 * {@code add} methods, the iterator will throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than
 * risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:   <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <E> the type of elements held in this collection
 * @author Josh Bloch
 * @see List
 * @see ArrayList
 * @since 1.2
 */

public class LinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
    /**
     * 链表中实际元素个数
     */
    transient int size = 0;

    /**
     * 首结点，指向双链表的第一个结点
     */
    transient Node<E> first;

    /**
     * 尾结点，指向双链表的最后一个结点
     */
    transient Node<E> last;

    /**
     * 无参构造器
     */
    public LinkedList() {
    }

    /**
     * 带参构造器
     *
     * @param c 集合
     */
    public LinkedList(Collection<? extends E> c) {
        this();
        // 调用addAll()方法将集合c中所有元素添加到当前LinkedList中
        addAll(c);
    }

    /**
     * 向双链表的首位置添加结点
     *
     * @param e 元素值，泛型类型的
     */
    private void linkFirst(E e) {
        // 局部变量，存放双链表的首结点
        final Node<E> f = first;
        // 创建一个新结点，prev指针指向null，结点值为输入的e，而next指针指向原首结点f
        // 采用的是头插法
        final Node<E> newNode = new Node<>(null, e, f);
        // 然后将首结点指针指向新插入的结点，即新结点是首结点了
        first = newNode;
        // 判断原首结点f是否为null
        // 为null表示当前双链表还是空表
        if (f == null)
            // 既然是空表，那么尾结点也是新结点，毕竟只有一个结点
            last = newNode;
            // 不为null表示当前双链表存在多个元素
        else
            // 然后将原链表的前驱指针指向新结点，连接起来
            f.prev = newNode;
        // 集合元素个数加1
        size++;
        // 修改次数加1
        modCount++;
    }

    /**
     * 向双链表的尾位置添加结点
     *
     * @param e 元素值，泛型类型的
     */
    void linkLast(E e) {
        // 局部变量，存放双链表的尾结点（链表的最后一个结点）
        final Node<E> l = last;
        // 创建一个新结点，由于是最后一个结点，所以前驱结点是原尾结点，结点值是输入的e，next指针指向null
        final Node<E> newNode = new Node<>(l, e, null);
        // 然后将新结点置为尾结点
        last = newNode;
        // 判断原尾结点是否为null
        // 如果为null，表示双链表是空表
        if (l == null)
            // 所以首结点和尾结点都是newNode，毕竟只有一个结点
            first = newNode;
            // 如果不为null，表示双链表不是空表
        else
            // 那么将原尾结点的next指针指向新结点
            l.next = newNode;
        // 元素个数加1
        size++;
        // 修改次数加1
        modCount++;
    }

    /**
     * 在指定结点之前插入新结点
     *
     * @param e    新结点的值
     * @param succ 指定结点
     */
    void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        // 局部变量，存放指定结点的前驱结点
        final Node<E> pred = succ.prev;
        // 创建一个新结点，新结点的前驱结点是pred，新结点的值是输入的e，新结点的后继结点是succ
        final Node<E> newNode = new Node<>(pred, e, succ);
        // 将指定结点srcc的前驱结点指向新结点newNode
        succ.prev = newNode;
        // 如果pred为null，表示succ结点是双链表的第一个结点，表示要在第一个结点之前插入结点
        if (pred == null)
            // 那么首结点就是新结点newNode
            first = newNode;
            // 如果pred不为null，表示在某两个结点之间插入新结点
        else
            // 那么将前驱结点pred的next指针指向新结点
            pred.next = newNode;
        // 链表元素个数加1
        size++;
        // 修改次数加1
        modCount++;
    }

    /**
     * 删除首结点（即链表的第一个结点）
     *
     * @param f 首结点（链表的第一个结点）
     * @return 返回被删除结点的值
     */
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        // 获取首结点f的值
        final E element = f.item;
        // 获取首结点f的后继结点
        final Node<E> next = f.next;
        // 将首结点f的item属性置为null
        f.item = null;
        // 将首结点的next指针指向null，便于回收
        f.next = null;
        // 那么此时链表的首结点就是f结点的后继结点（就是原链表的第二个结点）
        first = next;
        // 如果next为null，表示当前链表只有一个结点
        if (next == null)
            // 那么last就置为null，同时first也为null，此时就变成了空链表了
            last = null;
            // 如果next不为null，那么链表在删除第一个结点后还有其他结点
        else
            // 所以next是新的头结点，那么头结点的前驱指针prev就为null
            next.prev = null;
        // 既然删除了一个结点，那么LinkedList集合元素个数减1
        size--;
        // 修改次数减1
        modCount++;
        // 病返回被删除结点的值
        return element;
    }

    /**
     * 删除尾结点（即链表的最后一个结点）
     *
     * @param l 尾结点
     * @return 返回被删除结点的值
     */
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        // 局部变量，存放的是尾结点l的内容
        final E element = l.item;
        // 局部变量，存放的是尾结点的前驱结点（也是链表倒数第二个结点）
        final Node<E> prev = l.prev;
        // 将尾结点的item和prev属性都置为null，便于回收资源
        l.item = null;
        l.prev = null;
        // 那么prev结点就变成了新的尾结点
        last = prev;
        // 判断结点l的前驱结点prev是否为null
        // 如果为null表示双链表只有一个结点，删除后就变成了空表
        if (prev == null)
            // 那么首结点就为null，尾结点也为null，因为是空表了
            first = null;
            // 如果不为null，表示双链表的结点个数大于1
        else
            // 所以prev成了尾结点，既然是尾结点，那么它的next指针应该指向null
            prev.next = null;
        // 删除一个结点，记录元素个数的size减1
        size--;
        // 同时修改次数加1
        modCount++;
        // 返回被删除结点的内容
        return element;
    }

    /**
     * 删除指定结点x
     *
     * @param x 指定结点
     * @return 返回被删除结点的内容
     */
    E unlink(Node<E> x) {
        // assert x != null;
        // 1. 局部变量
        // 1.1 存放被删除结点x的内容
        final E element = x.item;
        // 1.2 存放被删除结点x的后继结点
        final Node<E> next = x.next;
        // 1.3 存放被删除结点x的前驱结点
        final Node<E> prev = x.prev;

        // 2. 判断前驱结点prev是否为null
        // 2.1 如果prev为null，表示没有前驱结点，那么x就是首结点
        if (prev == null) {
            // 将next置为新的首结点
            first = next;
            // 2.2 如果prev不为null，表示x结点是某两个结点的中间结点
        } else {
            // 将prev结点的next指针指向后继结点next
            prev.next = next;
            // 将x结点的prev指向null，以便于回收资源
            x.prev = null;
        }

        // 3. 判断后继结点next是否为null
        // 3.1 如果next为null，表示没有后继结点，那么x就是尾结点
        if (next == null) {
            // 那么prev就是新的尾结点last
            last = prev;
            // 3.2 如果next不为null，表示x结点是某两个结点的中间结点
        } else {
            // 将next结点的prev指针指向前驱结点prev，连接next和prev两个结点
            next.prev = prev;
            // 将x结点的next指针指向null，以便于回收资源
            x.next = null;
        }

        // 4. 收尾处理
        // 4.1 将x结点的item属性置为null，便于资源回收
        x.item = null;
        // 4.2 链表实际元素个数减1
        size--;
        // 4.3 修改次数加1
        modCount++;
        // 4.4 返回被删除结点的内容
        return element;
    }

    /**
     * 得到首结点的内容
     *
     * @return 返回首结点（链表的第一个结点）的内容
     * @throws NoSuchElementException 如果结点（如first、last等）不存在（为null），则抛出该异常
     */
    public E getFirst() {
        // 1. 获取首结点
        final Node<E> f = first;
        // 2. 判断首结点f的有效性
        if (f == null)
            // 如果为null，抛出没有这个结点的异常
            throw new NoSuchElementException();
        // 3. 执行到这，表示first结点存在，返回结点内容
        return f.item;
    }

    /**
     * 得到尾结点的内容
     *
     * @return 返回尾结点（链表的最后一个结点）的内容
     * @throws NoSuchElementException 如果链表为空则抛出此异常，不存在所谓的结点
     */
    public E getLast() {
        // 1. 获取尾结点
        final Node<E> l = last;
        // 2. 判断尾结点l的有效性
        if (l == null)
            // 如果l为null，表示是空表，抛出此异常
            throw new NoSuchElementException();
        // 3. 执行到这，表示不是空表，返回结点内容
        return l.item;
    }

    /**
     * 移除首结点
     *
     * @return 返回被删除结点的内容
     * @throws NoSuchElementException 如果链表为空，抛出此异常
     */
    public E removeFirst() {
        // 1. 获取首结点
        final Node<E> f = first;
        // 2. 判断结点的有效性
        if (f == null)
            // 如果f为null，表示为空表，抛出此异常
            throw new NoSuchElementException();
        // 3. 表示不是空表，那么调用unlinkFirst()方法删除首结点，返回被删除结点的值
        return unlinkFirst(f);
    }

    /**
     * 移除尾结点
     *
     * @return 返回被删除结点的内容
     * @throws NoSuchElementException 如果链表为空，抛出此异常
     */
    public E removeLast() {
        // 1. 获取尾结点
        final Node<E> l = last;
        // 2. 判断结点l的有效性
        if (l == null)
            // 如果l为null，表示为空链表，那么抛出此异常
            throw new NoSuchElementException();
        // 3. 调用unlinkLast()方法删除尾结点
        return unlinkLast(l);
    }

    /**
     * 向LinkedList中添加首结点
     *
     * @param e 结点内容
     */
    public void addFirst(E e) {
        // 1. 调用linkFirst()方法向链表中添加首结点
        linkFirst(e);
    }

    /**
     * 向LinkedList中添加尾结点
     *
     * @param e 结点内容
     */
    public void addLast(E e) {
        // 1. 调用linkLast()方法向链表添加尾结点
        linkLast(e);
    }

    /**
     * 判断双链表中是否存在指定元素e
     *
     * @param o 指定元素
     * @return 如果链表中存在指定内容的结点，那么返回true，否则返回false
     */
    public boolean contains(Object o) {
        // 1. 调用indexOf()方法查找内容尾o的结点第一次出现的索引
        // 2. 如果返回值为-1，表示没有找到该结点，不存在返回false，如果返回值不为-1，表示链表中存在该结点，返回true
        return indexOf(o) != -1;
    }

    /**
     * 获取链表中元素的实际个数
     *
     * @return 返回链表中元素个数
     */
    public int size() {
        // 1. 直接返回size即可，因为size记录了链表中元素的个数
        return size;
    }

    /**
     * 向链表中添加元素，默认是添加在链表尾部的
     *
     * @param e 待添加的元素
     * @return 返回true
     */
    public boolean add(E e) {
        // 1. 调用linkLast()方法向链表中添加结点，但默认是添加在链表尾部的
        linkLast(e);
        // 2. 返回true，添加成功
        return true;
    }

    /**
     * 移除链表中的指定元素
     *
     * @param o 指定元素o
     * @return 如果链表中包含此元素调用方法删除则返回true，不包含则返回false
     */
    public boolean remove(Object o) {
        // 1. 将待删除元素o分为null和!null两种情况
        // 1.1 如果待删除元素o为null
        if (o == null) {
            // 1.1.1 从首结点向后遍历整个链表
            for (Node<E> x = first; x != null; x = x.next) {
                // 1.1.2 判断结点的item属性值是否为null
                if (x.item == null) {
                    // 1.1.3 如果为null，表示找到该结点，则调用unlink()方法删除结点x
                    unlink(x);
                    // 1.1.4 删除成功则返回true
                    return true;
                }
            }
            // 1.2 如果待删除元素o不为null
        } else {
            // 1.2.1 从首结点向后遍历整个链表
            for (Node<E> x = first; x != null; x = x.next) {
                // 1.2.2 判断结点的item属性值是否为o
                if (o.equals(x.item)) {
                    // 1.2.3 如果为o，表示找到该结点，则调用unlink()方法删除结点x
                    unlink(x);
                    // 1.2.4 删除成功则返回true
                    return true;
                }
            }
        }
        // 2. 如果链表中不包含指定元素o，那么返回false，表示移除失败
        return false;
    }

    /**
     * 添加指定集合中的所有元素到当前LinkedList中
     *
     * @param c 指定集合
     * @return 如果链表发生改变则返回true，如果没有则返回false
     * @throws NullPointerException 如果给定集合c为null则抛出空指针异常
     */
    public boolean addAll(Collection<? extends E> c) {
        // 1. 调用addAll()的重载方法进行添加
        // 2. 该方法有两个参数：size指索引，即在链表中的所以你；c是待添加的集合
        return addAll(size, c);
    }

    /**
     * 添加指定集合中所有元素到索引index的位置
     *
     * @param index 指定索引
     * @param c     指定集合
     * @return 如果链表发生改变则返回true，如果没有则返回false
     * @throws IndexOutOfBoundsException 如果索引index不合法，则抛出索引越界异常
     * @throws NullPointerException      如果集合c为null，那么抛出空指针异常
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        // 1. 检测索引index的有效性，调用checkPositionIndex()方法
        checkPositionIndex(index);

        // 2. 将集合c转换成Object[]数组
        // 2.1 将c转换成数组
        Object[] a = c.toArray();
        // 2.2 局部变量，存放转换后数组a的长度，元素集合c中实际元素个数
        int numNew = a.length;
        // 2.3 如果numNew为0，表示c是空集合，那么返回false，表示没有对链表做任何改变操作
        if (numNew == 0)
            return false;

        // 3. 获取succ和pred结点
        // 3.1 局部变量，pred指的是前驱结点，succ指的是指定索引的结点
        Node<E> pred, succ;
        // 3.2 如果index等于size，表示向LinkedList链表末尾添加元素
        if (index == size) {
            // 3.2.1 index等于size表示指定索引的结点不存在，因为索引从0开始的
            succ = null;
            // 3.2.2 那么前驱结点就是尾结点
            pred = last;
            // 3.3 如果index不等于size，表示向LinkedList链表中某位置添加元素
        } else {
            // 3.3.1 调用node()方法获取指定索引index的结点
            succ = node(index);
            // 3.3.2 succ结点的前驱结点
            pred = succ.prev;
        }

        // 4. 循环遍历数组a
        for (Object o : a) {
            // 4.1 将Object类型强制转换成E类型
            @SuppressWarnings("unchecked") E e = (E) o;
            // 4.2 创建一个新结点，新结点的prev指针指向pred，结点指是当前遍历的e，next指针指向null
            Node<E> newNode = new Node<>(pred, e, null);
            // 4.3 判断前驱结点pred是否为null
            // 4.3.1 如果前驱结点pred为null，表示succ为首结点
            if (pred == null)
                // 那么将新结点newNode置为头结点
                first = newNode;
                // 4.3.2 如果前驱结点pred不为null，表示在pred结点后面添加结点
            else
                // 那么在pred结点后面连接新结点
                pred.next = newNode;
            // 4.4 将新结点置为pred，然后开始下一次循环
            pred = newNode;
        }

        // 5. 处理原索引index位置的succ结点
        // 5.1 如果succ为null，表示指定索引不存在结点，那么将pred置为尾结点
        if (succ == null) {
            last = pred;
            // 5.2 如果succ不为null，表示指定索引存在结点
        } else {
            // 那么将pred结点的next指针指向succ结点，连接起来
            pred.next = succ;
            // 同时将succ结点的prev指针指向pred结点，连接起来
            succ.prev = pred;
        }

        // 6. 收尾处理
        // 6.1 链表元素个数size更新
        size += numNew;
        // 6.2 修改次数modCount加1
        modCount++;
        // 6.3 执行到这，已经对链表进行了改变，所以返回true
        return true;
    }

    /**
     * 清空LinkedList中所有元素
     */
    public void clear() {
        // 1. 循环遍历整个链表，从首结点first开始
        for (Node<E> x = first; x != null; ) {
            // 1.1 局部变量，保存当前结点的后继结点，用于下一轮循环
            Node<E> next = x.next;
            // 1.2 将x结点的item属性置为null，便于资源回收
            x.item = null;
            // 1.3 将x结点的next指针指向null
            x.next = null;
            // 1.4 将x结点的prev指针指向null
            x.prev = null;
            // 1.5 开始下一轮循环，相当于x=x.next
            x = next;
        }
        // 2. 收尾处理
        // 2.1 删除所有结点后，同时将first指针和last指针都指向null
        first = last = null;
        // 2.2 也需要将size置为0，表示没有元素了
        size = 0;
        // 2.3 修改次数加1
        modCount++;
    }


    // 位置访问操作

    /**
     * 获取指定索引的结点内容
     *
     * @param index 指定索引
     * @return 返回对应结点的内容
     * @throws IndexOutOfBoundsException 如果索引index超出范围，则抛出索引越界异常
     */
    public E get(int index) {
        // 1. 调用checkElementIndex()方法检测索引是否越界，如果越界则抛出异常
        checkElementIndex(index);
        // 2. 调用node()方法获取对应索引的结点，然后通过item属性获取它的内容
        return node(index).item;
    }

    /**
     * 替换指定索引处的结点用新值
     *
     * @param index   指定索引
     * @param element 新值
     * @return 返回旧值
     * @throws IndexOutOfBoundsException 如果index越界则抛出该异常
     */
    public E set(int index, E element) {
        // 1. 检测索引index是否越界，如果越界则抛出异常
        checkElementIndex(index);
        // 2. 通过node()方法查找指定索引的结点x
        Node<E> x = node(index);
        // 3. 保存结点x的内容，也就是旧值
        E oldVal = x.item;
        // 4. 用新值替换旧值
        x.item = element;
        // 5. 返回旧值
        return oldVal;
    }

    /**
     * 向指定索引位置添加新结点
     *
     * @param index   指定索引
     * @param element 新结点内容
     * @throws IndexOutOfBoundsException 抛出索引越界异常
     */
    public void add(int index, E element) {
        // 1. 检测索引index的有效性
        checkPositionIndex(index);

        // 2. 判断索引index是否等于size
        // 2.1 如果index等于size，表示添加位置是在尾结点的后面
        if (index == size)
            // 那么直接添加结点到链表的最后一个结点
            linkLast(element);
            // 2.2 如果index不等于size，则表示在某两个结点之间添加结点
        else
            // 那么调用linkBefore()方法在指定索引结点之前添加结点
            linkBefore(element, node(index));
    }

    /**
     * 移除指定索引处的结点
     *
     * @param index 指定索引位置
     * @return 返回被删除结点的值
     * @throws IndexOutOfBoundsException 如果索引越界则抛出越界异常
     */
    public E remove(int index) {
        // 1. 检测索引index的有效性
        checkElementIndex(index);
        // 2. 调用unlink()方法删除指定索引结点
        return unlink(node(index));
    }

    /**
     * 判断给定的索引是否越界
     *
     * @param index 指定索引
     * @return 如果索引在[0, size)范围内，则返回true，否则返回false
     */
    private boolean isElementIndex(int index) {
        // 判断返回index是否在[0, size)范围内，size是表示已有元素的个数
        return index >= 0 && index < size;
    }

    /**
     * 判断索引位置的合法性
     *
     * @param index 指定索引
     * @return 如果指定索引在[0, size]范围内，那么返回true表示索引合法，如果不在这个范围，那么返回false表示索引不合法
     */
    private boolean isPositionIndex(int index) {
        // 索引index的合法范围：[0, size]
        return index >= 0 && index <= size;
    }

    /**
     * 拼接索引越界异常信息
     *
     * @param index 索引
     * @return 返回异常信息字符串
     */
    private String outOfBoundsMsg(int index) {
        // index指的是越界的索引；size指的是链表中真实的元素个数
        return "Index: " + index + ", Size: " + size;
    }

    /**
     * 检测输入的索引index是否越界
     *
     * @param index 指定索引
     */
    private void checkElementIndex(int index) {
        // 1. 调用isElementIndex()方法判断索引是否越界
        if (!isElementIndex(index))
            // 2. 如果越界则抛出异常
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 检测索引位置index的有效性
     *
     * @param index 指定索引
     */
    private void checkPositionIndex(int index) {
        // 1. 如果isPositionIndex(index)返回false，则表示索引有问题
        if (!isPositionIndex(index))
            // 2. 那么抛出索引越界异常，outOfBoundsMsg()方法返回异常信息字符串
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 返回链表中指定索引的结点
     *
     * @param index 指定索引
     * @return 返回链表中指定索引的结点
     */
    Node<E> node(int index) {
        // assert isElementIndex(index);
        // 1. size >> 1相当于size/2，判断索引index是否小于一半的size
        // 1.1 表示如果小于size/2，那么从首结点开始向指定索引处的结点遍历
        if (index < (size >> 1)) {
            // 1.1.1 局部变量，存放的是首结点
            Node<E> x = first;
            // 1.1.2 从头结点开始向后遍历，遍历index个元素
            for (int i = 0; i < index; i++)
                x = x.next;
            // 1.1.3 返回对应索引的结点x
            return x;
            // 1.2 表示如果大于等于size/2，那么从尾结点开始向前遍历，之所以这样，可以减少遍历
        } else {
            // 1.2.1 局部变量，存放的是尾结点
            Node<E> x = last;
            // 1.2.2 从尾结点向前遍历，直到index索引位置
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            // 1.2.3 返回指定索引处的结点x
            return x;
        }
    }

    // 搜索操作

    /**
     * 查找指定元素在链表中第一次出现的索引
     *
     * @param o 指定元素
     * @return 如果链表中包含该元素则返回对应的索引，如果链表中不包含该元素则返回-1
     */
    public int indexOf(Object o) {
        // 0. 局部变量，存放元素o在链表中所对应的索引值
        int index = 0;
        // 1. 将o为null和不为null的情况分开处理
        // 1.1 如果o为null
        if (o == null) {
            // 1.1.1 从首结点first开始遍历整个链表
            for (Node<E> x = first; x != null; x = x.next) {
                // 1.1.2 查找遍历中结点内容为null的结点，找到了则返回对应的索引
                if (x.item == null)
                    return index;
                // 1.1.3 没有找到则将索引加1，继续下一轮循环
                index++;
            }
            // 1.2 如果o不为null
        } else {
            // 1.2.1 从首结点first开始遍历整个链表
            for (Node<E> x = first; x != null; x = x.next) {
                // 1.2.2 查找遍历中结点内容为o的结点，找到了则返回对应的索引
                if (o.equals(x.item))
                    return index;
                // 1.2.3 没有找到则将索引加1，继续下一轮循环
                index++;
            }
        }
        // 2. 如果链表中不存在指定元素e，那么返回-1
        return -1;
    }

    /**
     * 查找指定元素在链表中最后一次出现的索引
     *
     * @param o 指定元素
     * @return 如果链表中包含该元素则返回对应的索引，如果链表中不包含该元素则返回-1
     */
    public int lastIndexOf(Object o) {
        // 1. 局部变量，由于是倒序遍历（从尾结点开始向前遍历），所以将size赋给index使用
        int index = size;
        // 2. 将o为null和不为null分成两种情况处理
        // 2.1 如果o为null
        if (o == null) {
            // 2.1.1 从尾结点向前遍历
            for (Node<E> x = last; x != null; x = x.prev) {
                // 2.1.2 元素索引减1，因为是从size减，所以先减size再判断
                index--;
                // 2.1.3 判断结点内容是否为null，如果为null则返回对应的索引下标
                if (x.item == null)
                    return index;
            }
            // 2.2 如果o不为null
        } else {
            // 2.2.1 从尾结点向前遍历
            for (Node<E> x = last; x != null; x = x.prev) {
                // 2.2.2 元素索引减1，因为是从size减，所以先减size再判断
                index--;
                // 2.2.3 判断结点内容是否为o，如果为null则返回对应的索引下标
                if (o.equals(x.item))
                    return index;
            }
        }
        // 3. 若链表中不包含该元素o，那么返回-1
        return -1;
    }

    // 队列操作

    /**
     * 获得首结点的内容，不删除首结点
     *
     * @return 如果首结点为null则返回null，不为null则返回结点内容
     */
    public E peek() {
        // 1. 局部变量，存放首结点first
        final Node<E> f = first;
        // 2. 判断首结点f是否为null，如果为null表示链表为空则返回null，不为null则返回首结点的内容
        return (f == null) ? null : f.item;
    }

    /**
     * 获取首结点的内容，不删除首结点，与peek()方法不同的是，该方法如果链表为空则抛出异常,peek()方法则返回null
     *
     * @return 返回链表的首结点
     * @throws NoSuchElementException 如果链表为空则抛出该异常
     */
    public E element() {
        // 1. 调用getFirst()方法获取首结点
        return getFirst();
    }

    /**
     * 获取首结点的内容，但删除首结点
     *
     * @return 返回链表的首结点或者null
     */
    public E poll() {
        // 1. 获取链表的首结点
        final Node<E> f = first;
        // 2. 判断f是否为null，如果为null表示空表，则返回null，不为空表则返回首结点，并且删除首结点
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 移除首结点
     *
     * @return 返回首结点的内容
     * @throws NoSuchElementException 如果链表为空，则抛出该异常
     */
    public E remove() {
        // 1. 调用removeFirst()方法删除首结点，并返回被删除结点的内容
        return removeFirst();
    }

    /**
     * 添加元素到链表尾部
     *
     * @param e 结点内容
     * @return 返回操作结果
     */
    public boolean offer(E e) {
        // 调用add()方法进行添加，是调用linkLast()方法，所以添加在链表尾部
        return add(e);
    }

    // 双端队列操作

    /**
     * 插入首结点
     *
     * @param e 待插入的结点
     * @return 如果链表发生改变则返回true，表示已经插入成功
     */
    public boolean offerFirst(E e) {
        // 1. 调用addFirst()方法将元素添加到链表的第一个捷尔的位置
        addFirst(e);
        // 2. 返回true
        return true;
    }

    /**
     * 插入结点在链表的尾部
     *
     * @param e 待加入的结点
     * @return 如果链表发生改变则表示插入成功
     */
    public boolean offerLast(E e) {
        // 1. 将元素添加在链表尾部
        addLast(e);
        // 2. 返回true
        return true;
    }

    /**
     * 检索，但不删除结点，返回链表的第一个结点
     *
     * @return 返回链表的第一个结点，如果链表为空则返回null
     */
    public E peekFirst() {
        // 1. 链表的首结点
        final Node<E> f = first;
        // 2. 判断首结点是否为空决定返回值，如果为空待被是空表则返回null，不为空则返回结点的内容
        return (f == null) ? null : f.item;
    }

    /**
     * 检索，但不删除结点，得到链表的最后一个结点值
     *
     * @return 返回链表最后一个结点的内容，如果为空则返回null
     */
    public E peekLast() {
        // 1. 链表的尾结点
        final Node<E> l = last;
        // 2. 判断尾结点是否为null决定返回值
        return (l == null) ? null : l.item;
    }

    /**
     * 检索，并移除首结点
     *
     * @return 返回首结点的内容，如果链表为空返回null
     */
    public E pollFirst() {
        // 1. 链表的首结点
        final Node<E> f = first;
        // 2. 根据f是否为null决定返回值，并调用unlinkFirst()删除首结点
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 检索，并删除链表的最后一个结点
     *
     * @return 返回链表最后一个结点的内容，如果链表为空则返回null
     */
    public E pollLast() {
        // 1. 链表的尾结点
        final Node<E> l = last;
        // 2. 删除链表的尾结点并返回结点的内容
        return (l == null) ? null : unlinkLast(l);
    }

    /**
     * 将元素压入此列表所表示的栈中，换句话说，在链表的最前面插入元素
     *
     * @param e 待插入的元素
     */
    public void push(E e) {
        // 调用addFirst()方法向链表添加首元素，即入栈操作
        addFirst(e);
    }

    /**
     * 元素出栈，换句话说，就是移除并返回链表第一个结点
     * 该方法等效于removeFirst()
     *
     * @return 返回出栈的结点值
     * @throws NoSuchElementException 如果链表为空，则抛出该异常
     */
    public E pop() {
        // 即本质上就是调用remvoeFirst()方法
        return removeFirst();
    }

    /**
     * 删除链表中第一次出现的指定元素（从头到尾遍历列表时），如果列表不包含该元素，则该元素不变。
     *
     * @param o 如果存在，移除元素从列表中
     * @return 如果列表中包含该元素则返回true
     */
    public boolean removeFirstOccurrence(Object o) {
        // 调用remove()方法删除指定元素
        return remove(o);
    }

    /**
     * 删除此列表中最后一次出现的指定元素（从头到尾遍历列表时）。如果列表不包含该元素，则该元素不变。
     *
     * @param o 要从此列表中删除的元素（如果存在）
     * @return 如果列表包含指定的元素则返回true
     */
    public boolean removeLastOccurrence(Object o) {
        // 1. 将o分为null和!null两种情况
        // 1.1 如果o为null
        if (o == null) {
            // 1.1.1 从尾结点向前遍历
            for (Node<E> x = last; x != null; x = x.prev) {
                // 1.1.2 判断当前结点是否为null
                if (x.item == null) {
                    // 1.1.3 删除掉该结点
                    unlink(x);
                    // 1.1.4 返回true
                    return true;
                }
            }
            // 1.2 如果o不为null
        } else {
            // 1.2.1 从尾结点向前遍历
            for (Node<E> x = last; x != null; x = x.prev) {
                // 1.2.2 判断当前结点是否为o
                if (o.equals(x.item)) {
                    // 1.2.3 删除掉该结点
                    unlink(x);
                    // 1.2.4 返回true
                    return true;
                }
            }
        }
        // 2. 如果链表中不包含该元素，则返回false
        return false;
    }

    /**
     * 返回列表中元素的List集合迭代器（以适当的顺序），从列表中的指定位置开始。
     *
     * @param index 指定索引
     * @return 此列表中元素的ListIterator（以正确的顺序），从列表中的指定位置开始
     * @throws IndexOutOfBoundsException 如果索引越界则抛出此异常
     */
    public ListIterator<E> listIterator(int index) {
        // 1. 检测输入的索引是否越界，如果越界则抛出异常
        checkPositionIndex(index);
        // 2. 如果索引正常，则返回指定索引的迭代器
        return new ListItr(index);
    }

    /**
     * ListIterator<E>迭代器的实现类
     */
    private class ListItr implements ListIterator<E> {
        // 全局变量，上一次执行 next() 或者 previos() 方法时的节点
        private Node<E> lastReturned;
        // 后继结点
        private Node<E> next;
        // 后继结点的索引
        private int nextIndex;
        // 将修改次数modCount赋给expectedModCount
        // modCount是指实际修改次数
        // expectedModCount是指期望修改次数
        private int expectedModCount = modCount;

        /**
         * 带参构造器
         *
         * @param index 指定索引
         */
        ListItr(int index) {
            // assert isPositionIndex(index);
            // 对next和nextIndex进行赋值，所以next就是根据索引index查询出来的结点
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        /**
         * 判断是否还有下一个结点
         *
         * @return 如果还有后继结点则返回true，否则返回false
         */
        public boolean hasNext() {
            // nextIndex表示当前结点的索引
            // size表示元素的实际个数
            // 如果nextIndex小于size则表示仍然还有后继结点，如果大于等于size那么表示要么是尾结点，要么索引越界了
            return nextIndex < size;
        }

        // 取下一个元素
        public E next() {
            // 1. 检查modCount和expectedModCount是否相等，如果不相等，表示发生了修改
            checkForComodification();
            // 2. 判断是否有下一个元素，如果没有则抛出NoSuchElementException异常
            if (!hasNext()) // 表示hashNext()为false才会执行
                throw new NoSuchElementException();

            // 3. 保存next结点
            lastReturned = next;
            // 4. 迭代器指向下一个结点
            next = next.next;
            // 5. 索引加1
            nextIndex++;
            // 6. 返回旧next结点的内容
            return lastReturned.item;
        }

        /**
         * 判断是否有前驱结点
         *
         * @return 如果有前驱结点返回true，否则返回false
         */
        public boolean hasPrevious() {
            // 即判断nextIndex是否大于0
            return nextIndex > 0;
        }

        /**
         * 获取前驱结点
         *
         * @return 返回前驱结点
         */
        public E previous() {
            // 1. 检查modCount和expectedModCount是否相等，如果不相等，表示发生了修改
            checkForComodification();
            // 2. 判断是否有上一个元素，空表或只有一个元素都没有前驱结点，如果没有则抛出NoSuchElementException异常
            if (!hasPrevious())
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        /**
         * 返回下一个结点的索引
         *
         * @return 下一个结点的索引值，从0开始的
         */
        public int nextIndex() {
            // 直接返回nextIndex即可
            return nextIndex;
        }

        /**
         * 返回前驱结点的索引
         *
         * @return 前驱结点的索引
         */
        public int previousIndex() {
            // 即nextIndex减去1的结果
            return nextIndex - 1;
        }

        /**
         * 使用迭代器进行迭代的时候不能进行调用list.remove()或list.add()删除修改元素，否则会抛出ConcurrentModificationException异常
         * 所以如果要增加或删除元素需要使用迭代器Iterator内部的remove()和add()方法
         */
        public void remove() {
            // 1. 检查modCount和expectedModCount是否相等，如果不相等，表示发生了修改
            checkForComodification();
            // 2. 判断lastReturned是否为null来判断迭代器的状态
            if (lastReturned == null)
                throw new IllegalStateException();

            // 3. 获取上一个结点的next结点的next结点，就是当前结点的后继结点
            Node<E> lastNext = lastReturned.next;
            // 4. 删除当前结点
            unlink(lastReturned);

            if (next == lastReturned)
                // 重新设置next结点，该指向被删除结点的下一个结点
                next = lastNext;
            else
                nextIndex--;
            // 将lastReturned置为null，便于回收
            lastReturned = null;
            // 同时expectedModCount修改次数加1
            expectedModCount++;
        }

        /**
         * 修改结点的值
         *
         * @param e 新值
         */
        public void set(E e) {
            // 1. 检查迭代器的状态
            if (lastReturned == null)
                throw new IllegalStateException();
            // 2. 检查在迭代器进行迭代时是否修改了List集合
            checkForComodification();
            // 3. 直接修改当前结点的item属性值
            lastReturned.item = e;
        }

        /**
         * 添加结点
         *
         * @param e 待添加的结点内
         */
        public void add(E e) {
            // 1. 检查在迭代时是否有修改List集合
            checkForComodification();

            // 2. 将lastReturned置为null
            lastReturned = null;
            // 3. 判断next是否为null
            // 3.1 如果为null，表示next是尾结点，那么将结点添加在末尾即可
            if (next == null)
                linkLast(e);
                // 3.2 表示不为null，那么插入在next结点之前
            else
                linkBefore(e, next);
            // 4. 收尾处理
            // 4.1 nextIndex需要加1
            nextIndex++;
            // 4.2 由于添加了元素，expectedModCount也需要加1
            expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }

        /**
         * 验证modCount的值和expectedModCount的值是否相等，所以当你在调用了ArrayList.add()或者ArrayList.remove()时，只更新了modCount的状态，而迭代器中的expectedModCount未同步，因此才会导致再次调用Iterator.next()方法时抛出异常。
         */
        final void checkForComodification() {
            // 本质上判断modCount是否等于expectedModCount
            if (modCount != expectedModCount)
                // 如果不相等表示在迭代时调用了list.add()或list.remove()，那么抛出此异常
                throw new ConcurrentModificationException();
        }
    }

    /**
     * LinkedList中的结点类Node<E>
     *
     * @param <E> 泛型
     */
    private static class Node<E> {
        // 结点的内容
        E item;
        // 结点的后继指针
        Node<E> next;
        // 结点的前驱指针
        Node<E> prev;

        /**
         * 全参构造器
         *
         * @param prev    前驱结点
         * @param element 结点内容
         * @param next    后继结点
         */
        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    /**
     * 降序迭代器，如果ListIterator输出的顺序是[1,2,3,4]，那么DescendingIterator输出的顺序就是[4,3,2,1]，这就是降序迭代器
     *
     * @return 降序迭代器DescendingIterator
     */
    public Iterator<E> descendingIterator() {
        // 实例化DescendingIterator迭代器
        return new DescendingIterator();
    }

    /**
     * 通过ListItr.previous提供降序迭代器的适配器
     */
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());

        public boolean hasNext() {
            return itr.hasPrevious();
        }

        public E next() {
            return itr.previous();
        }

        public void remove() {
            itr.remove();
        }
    }

    /**
     * 私有方法，浅克隆LinkedList
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 返回此LinkedList的浅表副本。 （元素本身不会被克隆。）
     *
     * @return 返回克隆后的对象
     */
    public Object clone() {
        LinkedList<E> clone = superClone();

        // 将克隆置于“原始”状态
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // 用我们的元素初始化克隆
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }

    /**
     * 以正确的顺序（从第一个元素到最后一个元素）返回一个包含此列表中所有元素的数组。
     * 返回的数组将是“安全的”，因为此列表不保留对其的引用。 （换句话说，此方法必须分配一个新数组）。
     * 因此，调用者可以自由修改返回的数组。
     * 此方法充当基于数组的API和基于集合的API之间的桥梁。
     *
     * @return 包含此列表中所有元素的序列按适当顺序的数组，注意，返回的是Object，不能转换成String[]
     */
    public Object[] toArray() {
        // 创建一个长度为size的新Object[]数组
        Object[] result = new Object[size];
        // 索引，初始值为0
        int i = 0;
        // 循环遍历链表
        for (Node<E> x = first; x != null; x = x.next)
            // 为数组中每个位置赋值
            result[i++] = x.item;
        // 返回转换完成的数组
        return result;
    }

    /**
     * 将集合转换成指定类型的数组
     * 返回以适当顺序（从第一个元素到最后一个元素）包含此列表中所有元素的数组；
     * 返回数组的运行时类型是指定数组的运行时类型。如果列表在指定数组中适合，则在其中返回它。
     * 否则，将为新数组分配指定数组的运行时类型和此列表的大小。
     *
     * 如果列表适合指定的数组并有剩余空间（即数组中的元素多于列表），则紧接列表结尾之后的数组中的元素将设置为null。
     * （这在确定调用者知道列表不包含任何null元素的情况下仅用于确定列表的长度很有用。）
     *
     * 像{@link #toArray（）}方法一样，此方法充当基于数组的API和基于集合的API之间的桥梁。
     * 此外，该方法允许对输出数组的运行时类型进行精确控制，并且在某些情况下可以节省分配成本。
     *
     * 请注意，{@code toArray（new Object [0]）}在功能上与{@code toArray()}
     *
     * @param a 指定数组
     * @param <T> 泛型，数组的类型
     * @return 包含列表元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此列表中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定数组a为null，抛出该异常
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        // 如果输入的数组a的长度小于集合List中元素个数，那么重新构造一个size长度的数组
        if (a.length < size)
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        // 将集合中的元素赋值到数组中
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

        // 如果列表适合指定的数组并有剩余空间（即数组中的元素多于列表），则紧接列表结尾之后的数组中的元素将设置为null。
        if (a.length > size)
            a[size] = null;

        // 返回以适当顺序（从第一个元素到最后一个元素）包含此列表中所有元素的数组
        return a;
    }

    // 序列化操作
    private static final long serialVersionUID = 876323262645176354L;

    /**
     * 序列化当前集合
     * @param s
     * @throws java.io.IOException
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }

    /**
     * 反序列化当前集合
     * @param s
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            linkLast((E) s.readObject());
    }

    /**
     * 创建一个LLSpliterator（splitable iterator可分割迭代器）
     * @return 返回一个LinkedList实现的可分割迭代器LLSpliterator
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }

    /**
     * Spliterators.IteratorSpliterator的自定义变体
     */
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // 批处理数组大小增量
        static final int MAX_BATCH = 1 << 25;  // 最大批处理数组大小；
        final LinkedList<E> list; // 除非遍历，否则为null
        Node<E> current;      // 当前节点；在初始化之前为null
        int est;              // 大小估计； -1直到第一次需要
        int expectedModCount; // initialized when est set
        int batch;            // 分割的批次大小

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }

        public long estimateSize() {
            return (long) getEst();
        }

        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do {
                    a[j++] = p.item;
                } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p;
            int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}
