package java.lang;

import java.lang.annotation.Native;

/**
 * @author lcl100
 * @start 2021/05/25
 * @end 未知
 * @desc 基本数据类型int的包装类Integer
 */
/*
    类说明：
        1.使用final关键字修饰类，表示类不可继承。
        2.Integer类继承自抽象类Number，并实现了接口Comparable<Integer>
 */
public final class Integer extends Number implements Comparable<Integer> {

    /**
     * 常量，int的最小常数，可以通过Integer.MIN_VALUE进行调用
     * 打印结果为-2147483648
     * 转换成二进制是1000 0000 0000 0000 0000 0000 0000 0000，最前面的一位是符号位，1表示负数，0表示正数
     * 至于为什么不是0000 0000 0000 0000 0000 0000 0000 0000，因为最高位是符号位，其他位都为0则最小
     * 注意，由于正数的补码就是它的源码，而MIN_VALUE是负数，1000 0000 0000 0000 0000 0000 0000 0000是它的补码表示形式，而实际上返回的是它的原码表示十进制数
     */
    @Native
    public static final int MIN_VALUE = 0x80000000;

    /**
     * 常量，int的最大常数，可以通过Integer.MAX_VALUE进行调用
     * 打印结果为2147483647
     * 转换成二进制是0111 1111 1111 1111 1111 1111 1111 1111，最前面的一位是符号位，1表示负数，0表示正数
     * 至于为什么不是1111 1111 1111 1111 1111 1111 1111 1111，因为最高位是符号位，其他位都为1则最大
     */
    @Native
    public static final int MAX_VALUE = 0x7fffffff;

    /**
     * 获取当前包装类Integer对应着什么基本数据类型，可以通过Integer.TYPE调用打印结果是int
     * 表示基本数据类型int的Class实例
     */
    @SuppressWarnings("unchecked")
    public static final Class<Integer> TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

    /**
     * 用于将数字表示为字符串的所有可能字符：[0,9]和[a,z]
     */
    final static char[] digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    /**
     * 返回第二个参数指定的基数中第一个参数的字符串表示形式。
     * 如果基数小于Character.MIN_RADIX或大于Character.MAX_RADIX ，则使用基数10 。
     * 如果第一个参数为负，则结果的第一个元素是 ASCII 减号字符'-' ( '\u002D' )。 如果第一个参数不是负数，则结果中不会出现符号字符。
     * 结果的其余字符表示第一个参数的大小。 如果幅度为零，则由单个零字符'0' ( '\u0030' ) 表示； 否则，幅度表示的第一个字符将不是零字符。 以下 ASCII 字符用作数字：0123456789abcdefghijklmnopqrstuvwxyz
     * 它们是'\u0030'到'\u0039'和'\u0061'到'\u007A' 。 如果radix为N ，则这些字符的前N 个将按所示顺序用作基数-N数字。 因此，十六进制（基数 16）的数字是0123456789abcdef 。 如果需要大写字母，可以对结果调用String.toUpperCase()方法：Integer.toString(n, 16).toUpperCase()
     * 例如：Integer.toString(10,2)的返回结果是"1010"，表示将十进制数字10转换成二进制字符串返回
     * 例如：Integer.toString(16,8)的返回结果是"20"，表示将十进制数字16转换成八进制字符串返回
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117406222
     *
     * @param i     要转换为字符串的整数
     * @param radix 在字符串表示中使用的基数（进制）
     * @return 指定基数（进制）中参数的字符串表示形式
     */
    public static String toString(int i, int radix) {
        // Character.MIN_RADIX=2；Character.MAX_RADIX=36
        // 即输入参数radix的有效范围应该是[2, 36]
        // 但如果输入[2, 36]范围之外的radix，那么会自动当作radix=10来处理
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        // 使用更快的版本，即如果传入的radix参数为10，直接调用toString()的重载方法，不再进行下面的操作
        if (radix == 10) {
            return toString(i);
        }

        // 创建一个长度为33的字符数组，为什么是33呢，因为一个int类型的数据最多是32位二进制，如果算上符号那么最多可以有33的字符
        char buf[] = new char[33];
        // 局部变量，记录是否是负数的标志，如果i是负数则negative为true，如果i是正数或0则negative为false
        boolean negative = (i < 0);
        // 指向buf数组中元素的指针，初始值指向数组中最后一个元素
        int charPos = 32;

        // 如果传入的参数i是一个正数，则转换成负数，当作负数来处理更加方便
        if (!negative) {
            i = -i;// 将正数转换成负数
        }
        /* 核心代码 start */
        // 将整数转换为r进制数时，采用除r取余方法，即将十进制整数不断除以r取余数，直到商为0，所得的余数按逆序排列
        while (i <= -radix) {
            // 倒序保存字符
            buf[charPos--] = digits[-(i % radix)];// 因为是将i作为负数来处理，所以前面再添加一个"-"号转换成正数，来作为数组下标索引
            // 不断除以r取余数，直到商为0
            i = i / radix;
        }
        buf[charPos] = digits[-i];
        /* 核心代码 end */

        // 对是否要为字符串添加负号"-"作判断，如果negative标志为true表示是负数则添加"-"号
        if (negative) {
            buf[--charPos] = '-';
        }

        // 将字符数组中的有效字符拼接成字符串返回
        return new String(buf, charPos, (33 - charPos));
    }

    /**
     * Returns a string representation of the first argument as an
     * unsigned integer value in the radix specified by the second
     * argument.
     *
     * <p>If the radix is smaller than {@code Character.MIN_RADIX}
     * or larger than {@code Character.MAX_RADIX}, then the radix
     * {@code 10} is used instead.
     *
     * <p>Note that since the first argument is treated as an unsigned
     * value, no leading sign character is printed.
     *
     * <p>If the magnitude is zero, it is represented by a single zero
     * character {@code '0'} ({@code '\u005Cu0030'}); otherwise,
     * the first character of the representation of the magnitude will
     * not be the zero character.
     *
     * <p>The behavior of radixes and the characters used as digits
     * are the same as {@link #toString(int, int) toString}.
     *
     * @param i     an integer to be converted to an unsigned string.
     * @param radix the radix to use in the string representation.
     * @return an unsigned string representation of the argument in the specified radix.
     * @see #toString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i, int radix) {
        return Long.toUnsignedString(toUnsignedLong(i), radix);
    }

    /**
     * Returns a string representation of the integer argument as an
     * unsigned integer in base&nbsp;16.
     *
     * <p>The unsigned integer value is the argument plus 2<sup>32</sup>
     * if the argument is negative; otherwise, it is equal to the
     * argument.  This value is converted to a string of ASCII digits
     * in hexadecimal (base&nbsp;16) with no extra leading
     * {@code 0}s.
     *
     * <p>The value of the argument can be recovered from the returned
     * string {@code s} by calling {@link
     * Integer#parseUnsignedInt(String, int)
     * Integer.parseUnsignedInt(s, 16)}.
     *
     * <p>If the unsigned magnitude is zero, it is represented by a
     * single zero character {@code '0'} ({@code '\u005Cu0030'});
     * otherwise, the first character of the representation of the
     * unsigned magnitude will not be the zero character. The
     * following characters are used as hexadecimal digits:
     *
     * <blockquote>
     * {@code 0123456789abcdef}
     * </blockquote>
     * <p>
     * These are the characters {@code '\u005Cu0030'} through
     * {@code '\u005Cu0039'} and {@code '\u005Cu0061'} through
     * {@code '\u005Cu0066'}. If uppercase letters are
     * desired, the {@link java.lang.String#toUpperCase()} method may
     * be called on the result:
     *
     * <blockquote>
     * {@code Integer.toHexString(n).toUpperCase()}
     * </blockquote>
     *
     * @param i an integer to be converted to a string.
     * @return the string representation of the unsigned integer value
     * represented by the argument in hexadecimal (base&nbsp;16).
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since JDK1.0.2
     */
    public static String toHexString(int i) {
        return toUnsignedString0(i, 4);
    }

    /**
     * Returns a string representation of the integer argument as an
     * unsigned integer in base&nbsp;8.
     *
     * <p>The unsigned integer value is the argument plus 2<sup>32</sup>
     * if the argument is negative; otherwise, it is equal to the
     * argument.  This value is converted to a string of ASCII digits
     * in octal (base&nbsp;8) with no extra leading {@code 0}s.
     *
     * <p>The value of the argument can be recovered from the returned
     * string {@code s} by calling {@link
     * Integer#parseUnsignedInt(String, int)
     * Integer.parseUnsignedInt(s, 8)}.
     *
     * <p>If the unsigned magnitude is zero, it is represented by a
     * single zero character {@code '0'} ({@code '\u005Cu0030'});
     * otherwise, the first character of the representation of the
     * unsigned magnitude will not be the zero character. The
     * following characters are used as octal digits:
     *
     * <blockquote>
     * {@code 01234567}
     * </blockquote>
     * <p>
     * These are the characters {@code '\u005Cu0030'} through
     * {@code '\u005Cu0037'}.
     *
     * @param i an integer to be converted to a string.
     * @return the string representation of the unsigned integer value
     * represented by the argument in octal (base&nbsp;8).
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since JDK1.0.2
     */
    public static String toOctalString(int i) {
        return toUnsignedString0(i, 3);
    }

    /**
     * 将参数i转换成二进制字符串返回
     * 该值将转换为二进制（以2为底）的ASCII数字字符串，且没有多余的前导0。
     *
     * @param i 要转换为二进制字符串的整数。
     * @return 返回二进制字符串
     */
    public static String toBinaryString(int i) {
        // 调用toUnsignedString0()方法，传入数字i和1
        return toUnsignedString0(i, 1);
    }


    /**
     * 将整数转换为无符号数字
     *
     * @param val   传入数据
     * @param shift
     * @return
     */
    private static String toUnsignedString0(int val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);
        char[] buf = new char[chars];

        formatUnsignedInt(val, shift, buf, 0, chars);

        // Use special constructor which takes over "buf".
        return new String(buf, true);
    }

    /**
     * Format a long (treated as unsigned) into a character buffer.
     *
     * @param val    the unsigned int to format
     * @param shift  the log2 of the base to format in (4 for hex, 3 for octal, 1 for binary)
     * @param buf    the character buffer to write to
     * @param offset the offset in the destination buffer to start at
     * @param len    the number of characters to write
     * @return the lowest character  location used
     */
    static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[offset + --charPos] = Integer.digits[val & mask];
            val >>>= shift;
        } while (val != 0 && charPos > 0);

        return charPos;
    }

    final static char[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    final static char[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    // I use the "invariant division by multiplication" trick to
    // accelerate Integer.toString.  In particular we want to
    // avoid division by 10.
    //
    // The "trick" has roughly the same performance characteristics
    // as the "classic" Integer.toString code on a non-JIT VM.
    // The trick avoids .rem and .div calls but has a longer code
    // path and is thus dominated by dispatch overhead.  In the
    // JIT case the dispatch overhead doesn't exist and the
    // "trick" is considerably faster than the classic code.
    //
    // TODO-FIXME: convert (x * 52429) into the equiv shift-add
    // sequence.
    //
    // RE:  Division by Invariant Integers using Multiplication
    //      T Gralund, P Montgomery
    //      ACM PLDI 1994
    //

    /**
     * 将int类型的数值i转换成十进制的字符串形式返回
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117406222
     *
     * @param i 一个int类型的数值
     * @return 以10为底的自变量的字符串表示形式
     */
    public static String toString(int i) {
        // 如果当前参数i直接等于Integer类型所能表示的最小值
        // 为什么要有这个判断？因为将-2147483648的值直接返回的原因就是整数最大只能表示2147483647，无法将stringSize(-i)中的i赋值成-2147483648。
        if (i == Integer.MIN_VALUE)
            // 那么直接返回最小值字符串即可
            return "-2147483648";
        // 调用stringSize()方法获取整数的数字位数，如果是负数则加个负号强制转换成正数
        // 并且stringSize()方法必须传入一个正整数才能计算数字位数
        // 如果是i是负数则调用stringSize(-i) + 1，会把负号计算在内，例如-123得到的值是4位
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        // 创建一个size长度的字符数组，用来按序存放数字中的每一位
        char[] buf = new char[size];
        // 调用getChars()方法为buf字符数组填充内容，填充的内容就是i的每一位数字（如果是负号，则包括符号位）
        getChars(i, size, buf);
        // 然后将字符数组转换成字符串返回
        return new String(buf, true);
    }

    /**
     * Returns a string representation of the argument as an unsigned
     * decimal value.
     * <p>
     * The argument is converted to unsigned decimal representation
     * and returned as a string exactly as if the argument and radix
     * 10 were given as arguments to the {@link #toUnsignedString(int,
     * int)} method.
     *
     * @param i an integer to be converted to an unsigned string.
     * @return an unsigned string representation of the argument.
     * @see #toUnsignedString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i) {
        return Long.toString(toUnsignedLong(i));
    }

    /**
     * 将代表整数i的字符放入字符数组buf。字符从指定索引处的最低有效数字（不包括最高字符）开始向后放置，并从那里开始向后工作。
     * 如果i == Integer.MIN_VALUE将失败
     *
     * @param i     传入的int类型的整数
     * @param index 索引，实际上传入的是int类型整数的数字位数，如123传入的位数是3，-456传入的位数是4
     * @param buf   传入生成的字符数组buf
     */
    static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;
        // 标志位，用来判断是否是负数，如果sign==0表示是一个正整数，如果sign=='-'表示一个负整数同时存储了这个负号
        char sign = 0;

        // 如果传入的数是负数，那么需要特殊处理
        if (i < 0) {
            sign = '-';// 用sign记录负号
            i = -i;// 并将该数重置为正数
        }

        // 每次迭代产生两位字符
        while (i >= 65536) {
            // i除以100，例如1234/100=12，余34，就是要把34这两位数字放入字符数组
            q = i / 100;
            // 使用位运算，更加高效，这一步就是得到i/100的余数
            r = i - ((q << 6) + (q << 5) + (q << 2));// 等价于r=i-(q*100)
            // 处理完相除的结果和余数后，那么就可以开始下一次循环了，重新对i进行赋值
            i = q;
            // 得到的r是一个两位数，包括十位和个位数字，例如34等
            buf[--charPos] = DigitOnes[r];// DigitOnes数组可以获取数字r的个位数字符，例如r=34，那么得到的是数字4对应的字符'4'
            buf[--charPos] = DigitTens[r];// DigitTens数组可以获取数字r的十位数字符，例如r=34，那么得到的是数字3对应的字符'3'
        }

        // 进入快速模式以获取较小的数字，意思是如果是小于65536的数就不执行上面的while循环了，直接用下面的代码快速处理
        // 当i<65536时直接执行这个for死循环
        for (; ; ) {
            // q = (i * 52429) >>> (16 + 3);是i/10，乘法代替除法，更加高效
            // 2^19=524288，所以(i * 52429) >>> (16 + 3)=(i*52429)/(2^19)=(i*52429)/524288
            // 52429/524288 = 0.10000038146972656，约为0.1，所以这一步实际就是在除以10，只不过是换种效率更高的方法而已。
            // x >>> y相当于x/(2^y)，例如x=8，y=2，则x>>>y=8>>>2=8/(2^2)=8/4=2
            // 为什么选52429呢？因为它是在不超出整型范围内，精度最高的一个数
            q = (i * 52429) >>> (16 + 3);// 等价于q=i/10
            // ((q << 3) + (q << 1))等价于q*10
            // 事实上r就是得到i的最后一位，例如：123=12*10+3，在这里面i为123，q为12，r为3
            r = i - ((q << 3) + (q << 1));// 等价于r = i-(q*10)
            // 将digits数组中r索引位置的字符添加到buf字符数组中
            // 这里digits[r]实际上就是根据数字r获取对应的字符，例如数字4对应的字符是'4'，数字5对应的字符是'5'
            // 这里面数组担当着数字和字符映射的转换媒介
            buf[--charPos] = digits[r];
            i = q;// 将i重新赋值
            if (i == 0) break;
        }
        // 此时sign=='-'的话，表示是一个负整数，那么就要把'-'这个符号位也添加到字符数组中
        if (sign != 0) {
            // 其实就是buf[0]='-'，把负号这个符号位添加到字符数组buf中
            buf[--charPos] = sign;
        }
    }

    final static int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999,
            99999999, 999999999, Integer.MAX_VALUE};

    /**
     * 统计传入整数的数字位数，比如123是3位数字，4568是4位数字
     *
     * @param x 正整数，但事实在该方法内并没有判断该数是否是一个正整数
     * @return 返回x的位数
     */
    static int stringSize(int x) {
        // i是临时变量，用来统计一个数的位数
        for (int i = 0; ; i++)
            // 判断传入的参数x是否小于等于sizeTable[i]，则返回x的位数
            if (x <= sizeTable[i])
                return i + 1;// 因为索引是从0开始的，所以要加1
    }

    /**
     * 将字符串参数解析为第二个参数指定的基数中的有符号整数。
     * 字符串中的所有字符都必须是指定基数的数字（由Character.digit(char, int)是否返回非负值来确定），除了第一个字符可以是ASCII减号'-' （ '\u002D' ）表示负值，或ASCII加号'+' （ '\u002B' ）表示正值。 返回结果整数值。
     * <p>
     * 如果发生以下任一情况，将引发NumberFormatException类型的异常：<br/>
     * 第一种情况，第一个参数为null或长度为零的字符串。<br/>
     * 第二种情况，基数小于Character.MIN_RADIX或大于Character.MAX_RADIX 。<br/>
     * 第三种情况，字符串的任何字符都不是指定基数的数字，但第一个字符可以是减号'-' （ '\u002D' ）或加号'+' （ '\u002B' ），前提是该字符串是比长度1长。<br/>
     * 第四种情况，字符串表示的值不是int类型的值。<br/>
     * <p>
     * 示例如下：<br/><br/>
     * parseInt("0", 10) returns 0 <br/>
     * parseInt("473", 10) returns 473 <br/>
     * parseInt("+42", 10) returns 42 <br/>
     * parseInt("-0", 10) returns 0 <br/>
     * parseInt("-FF", 16) returns -255 <br/>
     * parseInt("1100110", 2) returns 102 <br/>
     * parseInt("2147483647", 10) returns 2147483647 <br/>
     * parseInt("-2147483648", 10) returns -2147483648 <br/>
     * parseInt("2147483648", 10) throws a NumberFormatException <br/>
     * parseInt("99", 8) throws a NumberFormatException <br/>
     * parseInt("Kona", 10) throws a NumberFormatException <br/>
     * parseInt("Kona", 27) returns 411787 <br/>
     *
     * @param s     即要被解析成整数的字符串
     * @param radix 解析s要使用的基数，即数字字符串s是几进制的，比如s="1010"，那么radix为2的话就是把字符串s按照二进制进行处理转换成十进制返回
     * @return 返回string参数表示的十进制整数
     * @throws NumberFormatException 如果该字符串不是正确的格式则抛出此异常
     */
    public static int parseInt(String s, int radix)
            throws NumberFormatException {
        /*
         * 警告：在初始化IntegerCache之前，VM初始化之前可能会调用此方法。必须注意不要使用valueOf方法。
         */

        /* 校验参数 start */
        // 如果s为空，则抛出NumberFormatException异常，并且提示s为null
        if (s == null) {
            throw new NumberFormatException("null");
        }

        // 校验radix参数，Character.MIN_RADIX是可用于字符串之间转换的最小基数，即可转换的最小进制
        // Character.MIN_RADIX等于2
        if (radix < Character.MIN_RADIX) {
            // 如果给出的radix小于Character.MIN_RADIX，那么会抛出NumberFormatException异常，并且提示进制不正确。
            // 例如：radix=-1
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }

        // 校验radix参数，Character.MAX_RADIX是可用于在字符串之间转换的最大基数
        // Character.MAX_RADIX等于36
        if (radix > Character.MAX_RADIX) {
            // 如果给出的radix超过Character.MAX_RADIX，那么会抛出NumberFormatException异常，并且提示进制超过了Character.MAX_RADIX
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }
        /* 校验参数 end */

        // 局部变量，存储转换后的结果
        int result = 0;
        // 局部变量，标志，用来记录是否是负数，如果是true表示该字符串s中存在"-"号即是负数，如果是false表示该字符串s中不存在"-"号，那么有极大可能是正数
        boolean negative = false;
        // i表示指针；len表示字符串s的长度，即字符个数
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        // 局部变量，记录Character.digit()的结果
        int digit;

        // 判断字符串s是否是空字符串，即没有字符的字符串，如""
        if (len > 0) {
            // 如果字符串s的长度大于0
            // s.charAt(0)表示获取字符串s中的第一个字符，就是为了判断是否有正负号出现
            char firstChar = s.charAt(0);
            // 如果第一个字符小于'0'，那么表示可能存在"+"或"-"正负号
            if (firstChar < '0') {
                // 判断第一个字符是否是"-"负号
                if (firstChar == '-') {
                    // 设置negative标志位，表示字符串s中第一个字符是"-"号，表示转换后的数可能是负数
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    // 执行到这里表示第一个字符firstChar既不是"-"号又不是"+"，而且还不是数字，那么就是非法字符，不能转换成数字
                    // 所以抛出NumberFormatException异常，并提示
                    throw NumberFormatException.forInputString(s);

                // len表示字符串的长度，如果为1，表示存在单个的"+"或"-"字符，那么这是错误的
                if (len == 1)
                    // 抛出异常，并给出提示
                    throw NumberFormatException.forInputString(s);
                // 同时i指针指向下一个字符，也就是第二个字符，可能是数字字符
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                // Character.digit(char ch, int radix)方法的作用是在指定的基数返回字符ch的数值
                // s.charAt(i++)获取在字符串i索引位置的字符，并且指针i++
                // “12345”按照十进制转成12345的方法其实就是以下方式： ((1*10)+2)*10)+3)*10+4)*10+5
                digit = Character.digit(s.charAt(i++), radix);
                // digit当为-1时就小于0，此时会抛出异常。例如：Integer.parseInt("88", 2);
                // 例如：Character.digit('3',2);当传入的ch字符超过radix的范围后就会返回-1，如这里的radix为2，ch字符的取值只能是'0'或'1'，其他值就会返回-1
                if (digit < 0) {
                    // 如果digit小于0则抛出NumberFormatException异常并给出提示
                    throw NumberFormatException.forInputString(s);
                }
                if (result < multmin) {
                    // 溢出异常，例如：Ingeter.valueOf("2147483648");
                    // 如果result小于multmin的话，后面一定会溢出，如果这里没有判断的话，溢出就麻烦了，正数也会变负数了。
                    throw NumberFormatException.forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    // 溢出溢出，例如：Ingeter.valueOf("21474836471");
                    throw NumberFormatException.forInputString(s);
                }
                result -= digit;
            }
        } else {
            // 如果是空字符串，则抛出此异常，并提示空字符串内容
            throw NumberFormatException.forInputString(s);
        }
        // 根据negative标志位来返回正数或负数
        return negative ? result : -result;
    }

    /**
     * Parses the string argument as a signed decimal integer. The
     * characters in the string must all be decimal digits, except
     * that the first character may be an ASCII minus sign {@code '-'}
     * ({@code '\u005Cu002D'}) to indicate a negative value or an
     * ASCII plus sign {@code '+'} ({@code '\u005Cu002B'}) to
     * indicate a positive value. The resulting integer value is
     * returned, exactly as if the argument and the radix 10 were
     * given as arguments to the {@link #parseInt(java.lang.String,
     * int)} method.
     *
     * @param s a {@code String} containing the {@code int}
     *          representation to be parsed
     * @return the integer value represented by the argument in decimal.
     * @throws NumberFormatException if the string does not contain a
     *                               parsable integer.
     */
    public static int parseInt(String s) throws NumberFormatException {
        return parseInt(s, 10);
    }

    /**
     * Parses the string argument as an unsigned integer in the radix
     * specified by the second argument.  An unsigned integer maps the
     * values usually associated with negative numbers to positive
     * numbers larger than {@code MAX_VALUE}.
     * <p>
     * The characters in the string must all be digits of the
     * specified radix (as determined by whether {@link
     * java.lang.Character#digit(char, int)} returns a nonnegative
     * value), except that the first character may be an ASCII plus
     * sign {@code '+'} ({@code '\u005Cu002B'}). The resulting
     * integer value is returned.
     *
     * <p>An exception of type {@code NumberFormatException} is
     * thrown if any of the following situations occurs:
     * <ul>
     * <li>The first argument is {@code null} or is a string of
     * length zero.
     *
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or
     * larger than {@link java.lang.Character#MAX_RADIX}.
     *
     * <li>Any character of the string is not a digit of the specified
     * radix, except that the first character may be a plus sign
     * {@code '+'} ({@code '\u005Cu002B'}) provided that the
     * string is longer than length 1.
     *
     * <li>The value represented by the string is larger than the
     * largest unsigned {@code int}, 2<sup>32</sup>-1.
     *
     * </ul>
     *
     * @param s     the {@code String} containing the unsigned integer
     *              representation to be parsed
     * @param radix the radix to be used while parsing {@code s}.
     * @return the integer represented by the string argument in the
     * specified radix.
     * @throws NumberFormatException if the {@code String}
     *                               does not contain a parsable {@code int}.
     * @since 1.8
     */
    public static int parseUnsignedInt(String s, int radix)
            throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                        NumberFormatException(String.format("Illegal leading minus sign " +
                        "on unsigned string %s.", s));
            } else {
                if (len <= 5 || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
                        (radix == 10 && len <= 9)) { // Integer.MAX_VALUE in base 10 is 10 digits
                    return parseInt(s, radix);
                } else {
                    long ell = Long.parseLong(s, radix);
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return (int) ell;
                    } else {
                        throw new
                                NumberFormatException(String.format("String value %s exceeds " +
                                "range of unsigned int.", s));
                    }
                }
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

    /**
     * Parses the string argument as an unsigned decimal integer. The
     * characters in the string must all be decimal digits, except
     * that the first character may be an an ASCII plus sign {@code
     * '+'} ({@code '\u005Cu002B'}). The resulting integer value
     * is returned, exactly as if the argument and the radix 10 were
     * given as arguments to the {@link
     * #parseUnsignedInt(java.lang.String, int)} method.
     *
     * @param s a {@code String} containing the unsigned {@code int}
     *          representation to be parsed
     * @return the unsigned integer value represented by the argument in decimal.
     * @throws NumberFormatException if the string does not contain a
     *                               parsable unsigned integer.
     * @since 1.8
     */
    public static int parseUnsignedInt(String s) throws NumberFormatException {
        return parseUnsignedInt(s, 10);
    }

    /**
     * 将一个String类型的参数转换成Integer对象，指定String数字字符串的基数（进制）来处理
     *
     * @param s     数字字符串
     * @param radix 指定的基数，如s="1101"，那么radix为2，表示将字符串s当作二进制来处理；如果radix为10，表示将字符串s当作十进制来处理
     * @return 返回处理后的Integer对象，并且值是十进制数
     * @throws NumberFormatException 如果字符串不能被解析为整数则抛出此错误
     */
    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        // 调用parseInt()方法将字符串s当作radix进制来处理，然后再利用Ineger.valueOf()方法将基本int类型的数值转换成包装类型Integer
        return Integer.valueOf(parseInt(s, radix));
    }


    /**
     * 将一个String类型的参数转换成Integer对象，是一个十进制的整数
     *
     * @param s 要解析的字符串，默认当作十进制数处理
     * @return 被转换成功的Integer对象
     * @throws NumberFormatException 如果字符串不能被解析为整数则抛出此错误
     */
    public static Integer valueOf(String s) throws NumberFormatException {
        // 调用parseInt()方法将字符串s当作十进制来处理，然后再利用Ineger.valueOf()方法将基本int类型的数值转换成包装类型Integer
        return Integer.valueOf(parseInt(s, 10));
    }


    /**
     * 内部类，缓存以支持JLS要求的-128到127（含）之间的值的自动装箱的对象标识语义
     * 主要是初始化[-128,127]范围之内的Integer对象，在需要使用的时候直接调用即可，不需要再new新的了
     */
    private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];// Integer对象缓存数组

        // 静态代码块，初始化配置
        static {
            // 高值可能由属性配置
            int h = 127;
            String integerCacheHighPropValue =
                    sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.max(i, 127);
                    // 最大数组大小为Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) - 1);
                } catch (NumberFormatException nfe) {
                    // 如果无法将属性解析为int，则将其忽略。
                }
            }
            high = h;

            // 初始化cache数组，cache数组的长度为(high - low) + 1，即(127-(-128))+1=256
            cache = new Integer[(high - low) + 1];// 加1是因为要把0也计算在内
            int j = low;// 局部变量，保存最低值-128
            // 循环遍历cache数组，为每个数组元素赋值
            for (int k = 0; k < cache.length; k++)
                // cache[k]表示指定k位置的数组元素，范围是[0,255]
                cache[k] = new Integer(j++);// j表示从low到high，即要赋给cache[k]的值，范围是[-128,127]

            // 必须设置范围[-128，127]（JLS7 5.1.7）
            assert IntegerCache.high >= 127;
        }

        /**
         * 无参构造器并且私有化
         */
        private IntegerCache() {
        }
    }

    /**
     * 返回表示指定int值的Integer实例。
     * 如果不需要新的Integer实例，则通常应优先于构造方法Integer(int)此方法，因为此方法通过缓存经常请求的值可能会产生明显更好的空间和时间性能。
     * 此方法将始终缓存-128至127（包括）范围内的值，并且可能缓存该范围之外的其他值。
     * 即如果i在[-128,127]范围内则不需要new新对象直接在缓存数组中获取即可，如果i不在[-128,127]范围之内则需要new新对象
     *
     * @param i 一个int类型的参数
     * @return 返回Integer类型的实例，值为i所表示的值
     */
    public static Integer valueOf(int i) {
        // 如果i在[-128,127]范围之内，则使用缓存数组中的对象
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            // i + (-IntegerCache.low)是计算在cache数组中的索引位置
            return IntegerCache.cache[i + (-IntegerCache.low)];
        // 如果i不在[-128,127]范围之内，则使用new关键字实例化对象
        return new Integer(i);
    }

    /**
     * Integer对象的值
     */
    private final int value;

    /**
     * 带一个参数的构造器，将int基本类型的数据转换成包装类型Integer
     *
     * @param value int类型的数值
     */
    public Integer(int value) {
        // 为value初始化赋值
        this.value = value;
    }

    /**
     * 构造一个新分配的Integer对象，该对象表示String参数指示的int值。
     * 完全按照parseInt方法用于基数10的方式将字符串转换为int值。
     *
     * @param s 待转换成整数的字符串s
     * @throws NumberFormatException 如果字符串s不符合转换格式，则抛出NumberFormatException异常
     */
    public Integer(String s) throws NumberFormatException {
        // 调用parseInt()方法将字符串s转换成十进制整数，并赋值给value
        this.value = parseInt(s, 10);
    }

    /**
     * 抽象方法的实现，将当前对象的值以byte类型的形式返回，由于当前对象是Integer类型的，对应int
     * 范围比byte类型的范围大，所以进行强制类型转换，可能发生精度缺失
     *
     * @return byte类型的值
     */
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * 抽象方法，返回一个short类型的数字，可能涉及舍入或截断 将int类型的数值强制转换成short类型的数值结果，
     * 如果int类型的数值超过short类型的数值范围，则会出现问题
     *
     * @return short类型的值
     */
    public short shortValue() {
        return (short) value;
    }

    /**
     * 将当前Integer对象的值以基本数据类型int的形式返回
     *
     * @return 转换成int类型后的数值
     */
    public int intValue() {
        // 直接返回value值即可
        return value;
    }

    /**
     * 将当前Integer对象的值强制转换成基本数据类型long的形式返回
     *
     * @return 返回转换后的long类型的值
     */
    public long longValue() {
        return (long) value;
    }

    /**
     * 将当前Integer对象的值强制转换成基本数据类型float的形式返回
     *
     * @return 返回转换后的float类型的值
     */
    public float floatValue() {
        return (float) value;
    }

    /**
     * 将当前Integer对象的值强制转换成基本数据类型double的形式返回
     *
     * @return 返回转换后的double类型的值
     */
    public double doubleValue() {
        return (double) value;
    }

    /**
     * 将当前对象的值以字符串的形式返回
     *
     * @return 返回数字字符串
     */
    public String toString() {
        // toString()方法的重载方法
        return toString(value);
    }

    /**
     * Returns a hash code for this {@code Integer}.
     *
     * @return a hash code value for this object, equal to the
     * primitive {@code int} value represented by this
     * {@code Integer} object.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /**
     * Returns a hash code for a {@code int} value; compatible with
     * {@code Integer.hashCode()}.
     *
     * @param value the value to hash
     * @return a hash code value for a {@code int} value.
     * @since 1.8
     */
    public static int hashCode(int value) {
        return value;
    }

    /**
     * Compares this object to the specified object.  The result is
     * {@code true} if and only if the argument is not
     * {@code null} and is an {@code Integer} object that
     * contains the same {@code int} value as this object.
     *
     * @param obj the object to compare with.
     * @return {@code true} if the objects are the same;
     * {@code false} otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer) obj).intValue();
        }
        return false;
    }

    /**
     * Determines the integer value of the system property with the
     * specified name.
     *
     * <p>The first argument is treated as the name of a system
     * property.  System properties are accessible through the {@link
     * java.lang.System#getProperty(java.lang.String)} method. The
     * string value of this property is then interpreted as an integer
     * value using the grammar supported by {@link Integer#decode decode} and
     * an {@code Integer} object representing this value is returned.
     *
     * <p>If there is no property with the specified name, if the
     * specified name is empty or {@code null}, or if the property
     * does not have the correct numeric format, then {@code null} is
     * returned.
     *
     * <p>In other words, this method returns an {@code Integer}
     * object equal to the value of:
     *
     * <blockquote>
     * {@code getInteger(nm, null)}
     * </blockquote>
     *
     * @param nm property name.
     * @return the {@code Integer} value of the property.
     * @throws SecurityException for the same reasons as
     *                           {@link System#getProperty(String) System.getProperty}
     * @see java.lang.System#getProperty(java.lang.String)
     * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm) {
        return getInteger(nm, null);
    }

    /**
     * Determines the integer value of the system property with the
     * specified name.
     *
     * <p>The first argument is treated as the name of a system
     * property.  System properties are accessible through the {@link
     * java.lang.System#getProperty(java.lang.String)} method. The
     * string value of this property is then interpreted as an integer
     * value using the grammar supported by {@link Integer#decode decode} and
     * an {@code Integer} object representing this value is returned.
     *
     * <p>The second argument is the default value. An {@code Integer} object
     * that represents the value of the second argument is returned if there
     * is no property of the specified name, if the property does not have
     * the correct numeric format, or if the specified name is empty or
     * {@code null}.
     *
     * <p>In other words, this method returns an {@code Integer} object
     * equal to the value of:
     *
     * <blockquote>
     * {@code getInteger(nm, new Integer(val))}
     * </blockquote>
     * <p>
     * but in practice it may be implemented in a manner such as:
     *
     * <blockquote><pre>
     * Integer result = getInteger(nm, null);
     * return (result == null) ? new Integer(val) : result;
     * </pre></blockquote>
     * <p>
     * to avoid the unnecessary allocation of an {@code Integer}
     * object when the default value is not needed.
     *
     * @param nm  property name.
     * @param val default value.
     * @return the {@code Integer} value of the property.
     * @throws SecurityException for the same reasons as
     *                           {@link System#getProperty(String) System.getProperty}
     * @see java.lang.System#getProperty(java.lang.String)
     * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, int val) {
        Integer result = getInteger(nm, null);
        return (result == null) ? Integer.valueOf(val) : result;
    }

    /**
     * Returns the integer value of the system property with the
     * specified name.  The first argument is treated as the name of a
     * system property.  System properties are accessible through the
     * {@link java.lang.System#getProperty(java.lang.String)} method.
     * The string value of this property is then interpreted as an
     * integer value, as per the {@link Integer#decode decode} method,
     * and an {@code Integer} object representing this value is
     * returned; in summary:
     *
     * <ul><li>If the property value begins with the two ASCII characters
     * {@code 0x} or the ASCII character {@code #}, not
     * followed by a minus sign, then the rest of it is parsed as a
     * hexadecimal integer exactly as by the method
     * {@link #valueOf(java.lang.String, int)} with radix 16.
     * <li>If the property value begins with the ASCII character
     * {@code 0} followed by another character, it is parsed as an
     * octal integer exactly as by the method
     * {@link #valueOf(java.lang.String, int)} with radix 8.
     * <li>Otherwise, the property value is parsed as a decimal integer
     * exactly as by the method {@link #valueOf(java.lang.String, int)}
     * with radix 10.
     * </ul>
     *
     * <p>The second argument is the default value. The default value is
     * returned if there is no property of the specified name, if the
     * property does not have the correct numeric format, or if the
     * specified name is empty or {@code null}.
     *
     * @param nm  property name.
     * @param val default value.
     * @return the {@code Integer} value of the property.
     * @throws SecurityException for the same reasons as
     *                           {@link System#getProperty(String) System.getProperty}
     * @see System#getProperty(java.lang.String)
     * @see System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, Integer val) {
        String v = null;
        try {
            v = System.getProperty(nm);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (v != null) {
            try {
                return Integer.decode(v);
            } catch (NumberFormatException e) {
            }
        }
        return val;
    }

    /**
     * 该方法的作用是将 String 解码为 Integer。接受十进制、十六进制和八进制数字。
     * 根据要解码的 String（mn)的形式转成不同进制的数字。 mn由三部分组成：符号、基数说明符和字符序列。
     * -0X123中-是符号位，0X是基数说明符（0表示八进制，0x,0X，#表示十六进制，什么都不写则表示十进制），123是数字字符序列。
     * 示例如下：
     * System.out.println(Integer.decode("0x123"));// 291
     * System.out.println(Integer.decode("0X654"));// 1620
     * System.out.println(Integer.decode("#852"));// 2130
     * System.out.println(Integer.decode("0123"));// 83
     * System.out.println(Integer.decode("-753"));// -753
     * System.out.println(Integer.decode("-0x741"));// -1857
     * System.out.println(Integer.decode("" + Integer.MIN_VALUE));// -2147483648
     *
     * @param nm 要解码的字符串
     * @return 一个Integer对象，包含由nm所表示的int值
     * @throws NumberFormatException 如果String不包含可解析的整数则抛出此异常
     */
    public static Integer decode(String nm) throws NumberFormatException {
        // 基数，初始值为10，表示十进制
        int radix = 10;
        // 指向字符串nm中每个字符的指针索引，从0开始，表示第一个字符
        int index = 0;
        // 标志位，判断是否为负数，如果为true表示是负数，如果为false表示是正数
        boolean negative = false;
        // 记录解码后的结果值
        Integer result;

        // 参数校验：如果字符串为空字符串（即长度为0）
        if (nm.length() == 0)
            // 则抛出NumberFormatException异常并提示字符串长度为0
            throw new NumberFormatException("Zero length string");

        /* 处理符号位 start */
        // 获取字符串nm的第一个字符
        char firstChar = nm.charAt(0);
        // 处理正负号（"-"或"+"）标志，如果存在
        if (firstChar == '-') {
            // 判断第一个字符是否是"-"号，如果是表示是一个负数，那么将negative标志置为true，并且index加1
            negative = true;
            index++;
        } else if (firstChar == '+')
            // 执行到这里表示不是负数，那么就是正数了，无论有"+"号还是没有都表示是正数
            // 如果有"+"号那么index指针加1，指向它的下一位字符
            // 如果没有"+"号也表示正数，但下一位可能是数值字符，所以index不必加1
            index++;
        /* 处理符号位 end */

        /* 处理基数说明符 start */
        // 处理基数说明符（如果存在），确定字符串nm是几进制
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            // 执行到这里，表示字符串nm是以"0x"或"0X"开头（这里的开头是从index开始起的，而不是从第一个字符开头的）的，说明这是一个十六进制的数字字符串
            // 为了获取到真正的数字，将index索引指针加2，并且用radix记录当前是一个十六进制字符串
            index += 2;
            radix = 16;
        } else if (nm.startsWith("#", index)) {
            // 执行到这里，表示字符串nm是以"#"开头（这里的开头是从index开始起的，而不是从第一个字符开头的）的，说明这是一个十六进制的数字字符串
            // 为了获取到真正的数字，将index索引指针加1（因为"#"只有一个字符，上面的"0x"或"0X"是两个字符所以加2），并用radix记录当前是一个十六进制的字符串
            index++;
            radix = 16;
        } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            // 执行到这里，表示字符串nm是以"0"开头（这里的开头是从index开始起的，而不是从第一个字符开头的）并且"0"字符之后的字符不止一个，说明这是一个八进制字符串
            // 为了获取真正的数字，将index索引指针加1，并用radix记录当前是一个八进制的字符串
            index++;
            radix = 8;
        }
        /* 处理基数说明符 end */

        // 参数校验：如果在处理完符号位和基数说明符后，后面还有"-"或"+"号，那么直接抛出NumberFormatException异常并且提示符号位在错误的位置
        // 例如：0x-124就会抛出此异常
        if (nm.startsWith("-", index) || nm.startsWith("+", index))
            throw new NumberFormatException("Sign character in wrong position");

        /* 处理纯数字部分 start */
        // 最后就是获取真正的数字字符串，并且转换成十进制整数作为结果返回
        try {
            // nm.substring(index)是获取nm字符串中真正的数字，如"0x123"获取数字字符串就是"123"
            // Integer.valueOf(nm.substring(index), radix)就是将指定数字字符串按照指定基数（进制）radix进行处理
            result = Integer.valueOf(nm.substring(index), radix);
            // 获取到数字后，最后还需要对符号位进行处理，如果negative是true则表示是一个负数，那么返回一个负数，如果negative是false表示是一个正数，那么返回原值即可
            result = negative ? Integer.valueOf(-result.intValue()) : result;
        } catch (NumberFormatException e) {
            // 如果 number 是 Integer.MIN_VALUE，我们将在这里结束。
            // 下一行处理这种情况，并导致重新抛出任何真正的格式错误。
            String constant = negative ? ("-" + nm.substring(index))
                    : nm.substring(index);
            result = Integer.valueOf(constant, radix);
        }
        return result;
        /* 处理纯数字部分 end */
    }

    /**
     * 将当前对象的值与指定对象的值进行比较
     * 示例如下：
     * Integer i = 12;
     * int result = i.compareTo(10);
     * System.out.println(result);
     *
     * @param anotherInteger 另一个Integer对象
     * @return 本质上是调用Integer.compare(x, y)方法，如果x<y, 则返回-1 ; 如果x = = y, 则返回0 ; 如果x>y，则返回1
     */
    public int compareTo(Integer anotherInteger) {
        // 本质是调用Integer.compare(x, y)方法
        // this.value指的是当前对象的值；anotherInteger.value指的是传入对象的值
        return compare(this.value, anotherInteger.value);
    }

    /**
     * 比较两个数的大小
     * 示例如下：
     * System.out.println(Integer.compare(3,4));// -1
     * System.out.println(Integer.compare(3,3));// 0
     * System.out.println(Integer.compare(3,2));// 1
     *
     * @param x 第一个操作数
     * @param y 第二个操作数
     * @return 如果x<y, 则返回-1 ; 如果x = = y, 则返回0 ; 如果x>y，则返回1
     */
    public static int compare(int x, int y) {
        // 判断两个数x和y的大小
        /*
            等价于
                if(x<y){
                    return -1;
                }else{
                    if(x==y){
                        return 0;
                    }else{
                        return 1;
                    }
                }
         */
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * 比较两个int类型的值，视为无符号值比较（即不关注是否是正负数，只比较大小）
     * 这里的无符号数的意思是默认二进制最高位不再作为符号位，而是计入数的大小
     *
     * @param x 要比较的第一个数
     * @param y 要比较的第二个数
     * @return 如果x==y则返回0；如果x<y作为无符号值则返回-1；如果x>y作为无符号值则返回1
     */
    public static int compareUnsigned(int x, int y) {
        // MIN_VALUE=1000 0000 0000 0000 0000 0000 0000 0000
        // 使用此方法时，所有正数都比负数小，最大值为-1，因为-1的二进制所有位都是1
        // 即1111 1111 1111 1111 1111 1111 1111 1111大于其他任何32位二进制数
        // 由于x和y都加了MIN_VALUE，所以都是转换为负数进行比较的
        // 若x是正数，y是正数：如果x>y，那么加上MIN_VALUE后仍然是x>y；如果x<y，那么加上MIN_VALUE后仍然是x<y。
        // 若x是0，y是0：那么x==y，返回0。
        // 若x是正数，y是负数：如果x>y，那么加上MIN_VALUE后变成了x<y（因为x变成了正数【一定会变成正数，因为两个负数的最高位都是1，相加后最高位会进1位，留下的是0，那么就是一个正数了】，y变成了负数）
        // 若x是负数，y是正数：如果x<y，那么加上MIN_VALUE后变成了x>y（调用此方法，如果是正数与负数比较，那么一定是负数比较大）
        // 若x是负数，y是负数：如果x>y，那么加上MIN_VALUE后仍然是x>y；如果x<y，那么加上MIN_VALUE后仍然是x<y。
        return compare(x + MIN_VALUE, y + MIN_VALUE);
    }

    /**
     * 通过无符号转换将int参数转换成long类型的。
     * 因此，零和正int值被映射到数字上相等的long值，负int值被映射到等于输入加上2^32的long值
     * 注意，下面的示例说明了负int值被映射到等于输入加上2^32的long值：
     * System.out.println(Long.toBinaryString((long)(-123456+Math.pow(2,32))));// 11111111111111100001110111000000
     * System.out.println(Long.toBinaryString(((long)-123456)&0xffffffffL));//    11111111111111100001110111000000
     *
     * @param x 要转换为unsigned long的值
     * @return 返回通过无符号转换为long的参数
     */
    public static long toUnsignedLong(int x) {
        /*
            0xffffffffL=0000 0000 0000 0000 0000 0000 0000 0000 1111 1111 1111 1111 1111 1111 1111 1111
            若x是一个正数，如x=123456，低32位保持原值，高32位填充为0
                ((long)123456)&0xffffffffL
                    0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0001 1110 0010 0100 0000
                   &
                    0000 0000 0000 0000 0000 0000 0000 0000 1111 1111 1111 1111 1111 1111 1111 1111
                   =0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0001 1110 0010 0100 0000
            若x是一个零，则为0
            若x是一个负数，如x=-123456
                ((long)-123456)&0xffffffffL
                    1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1110 0001 1101 1100 0000
                   &
                    0000 0000 0000 0000 0000 0000 0000 0000 1111 1111 1111 1111 1111 1111 1111 1111
                   =0000 0000 0000 0000 0000 0000 0000 0000 1111 1111 1111 1110 0001 1101 1100 0000
         */
        return ((long) x) & 0xffffffffL;
    }

    /**
     * 返回第一个参数除以第二个参数的无符号商值，其中每个参数和结果都被解释为无符号值
     * 注意：在二进制补码算术中，如果将两个操作数都视为有符号或无符号，则加、减和乘这个三个其他基本运算在位上相同。因此不提供单独的addUnsigned等方法。
     *
     * @param dividend 要被除的值，即a/b的a
     * @param divisor  进行除法的值，即a/b的b
     * @return 返回第一个参数除以第二个参数的无符号商
     */
    public static int divideUnsigned(int dividend, int divisor) {
        // 代替棘手的代码，现在只使用long算术。
        return (int) (toUnsignedLong(dividend) / toUnsignedLong(divisor));
    }

    /**
     * Returns the unsigned remainder from dividing the first argument
     * by the second where each argument and the result is interpreted
     * as an unsigned value.
     *
     * @param dividend the value to be divided
     * @param divisor  the value doing the dividing
     * @return the unsigned remainder of the first argument divided by
     * the second argument
     * @see #divideUnsigned
     * @since 1.8
     */
    public static int remainderUnsigned(int dividend, int divisor) {
        // In lieu of tricky code, for now just use long arithmetic.
        return (int) (toUnsignedLong(dividend) % toUnsignedLong(divisor));
    }


    // Bit twiddling

    /**
     * 使用 @Native 注解修饰成员变量，则表示这个变量可以被本地代码引用，常常被代码生成工具使用。
     * 用于以二进制补码形式表示int值的位数。
     * 在Java中int类型数据用4个字节（32位二进制）来表示
     */
    @Native
    public static final int SIZE = 32;// 32表示三十二位二进制补码

    /**
     * 用于以二进制补码形式表示int值的字节数。
     * 1个字节等于8位二进制。
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     * 如果是负数, 则返回 -2147483648：【1000,0000,0000,0000,0000,0000,0000,0000】(二进制表示的数)
     * 如果是0，则返回0
     * 如果是正数，则返回从左往右数第一个"1"（从右往左数最后一个"1"）所表示的权值
     * 例如987654321的二进制是0011 1010 1101 1110 0110 1000 1011 0001，从右往左数最后一个"1"的位置是第30位，返回的值就是2^(30-1)
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117399430
     *
     * @param i 待查找的int类型数值
     * @return 返回从左到右第一个1所表示的权值
     */
    public static int highestOneBit(int i) {
        // 思路就是不停得到右移再或运算，使得i最后变成从最高位1开始后面全是1
        // 因为int类型的数为32位，考虑最坏的情况就是最高位是1，那么就需要移动31位让最高位1之后的所有位变成1
        // 让最高位1之后的1位变成1
        i |= (i >> 1);
        // 让最高位1之后的3位（注意：3=1+2）变成1
        i |= (i >> 2);
        // 让最高位1之后的7位（注意：7=1+2+4）变成1
        i |= (i >> 4);
        // 让最高位1之后的15位（注意：15=1+2+4+8）变成1
        i |= (i >> 8);
        // 让最高位1之后的31位（注意：31=1+2+4+8+15）变成1
        i |= (i >> 16);
        // 最后这个减法就可以去除除了最高位1之后的所有1
        return i - (i >>> 1);
    }

    /**
     * 在数值i的二进制表示中，除去符号位，从低（右）到高（左）找到第一个为1的位置p，返回数值为2的(p-1)次方。
     * 如果数值i的二进制表示中除去符号位没有1，则返回0。
     * 例如数字8的二进制是0000 0000 0000 0000 0000 0000 0000 1000的结果是2^3=8
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117399430
     *
     * @param i 待查找的int类型数值
     * @return 返回从右到左第一个1所表示的权值
     */
    public static int lowestOneBit(int i) {
        /*
            例如数字8的二进制是0000 0000 0000 0000 0000 0000 0000 1000
            那么-8的二进制是  1111 1111 1111 1111 1111 1111 1111 1000
            那么8&-8结果是   0000 0000 0000 0000 0000 0000 0000 1000
            所以最后的结果是  (0000 0000 0000 0000 0000 0000 0000 1000)2=(8)10
            注：括号外面的数字表示进制，2表示二进制，10表示10进制，即二进制1000转换成十进制是8。注意，无论是正数还是负数，都是补码参与运算
         */
        return i & -i;
    }

    /**
     * 回指定int值的二进制补码二进制表示形式中最高位（“最左端”）一位之前的零位数目。 如果指定值的二进制补码表示中没有一位（即等于零），则返回32。
     * 注意，此方法是密切相关的数底2对所有正int值x：
     * floor（log 2 （x））= 31 - numberOfLeadingZeros(x)
     * ceil（log 2 （x））= 32 - numberOfLeadingZeros(x - 1)
     * 比如数字3的二进制是0000 0000 0000 0000 0000 0000 0000 0011，那么可以得到它的前置0有30位，最后两位是11
     * 比如数字8的二进制是0000 0000 0000 0000 0000 0000 0000 1000，那么可以得到它的前置0有28位，最后四位是1000
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117338989
     *
     * @param i 要计算前导零个数的值
     * @return 指定的int值的二进制补码二进制表示形式中（"最左端"）一位之前的零位数目；如果该值等于0，则为32位
     */
    public static int numberOfLeadingZeros(int i) {
        // 如果传入的数是0，那么32位二进制一定都是0，所以直接返回32即可，不需要再进行下面的操作
        if (i == 0)
            return 32;
        // 局部变量，统计前导零的个数，初始值为1，即至少存在1个，因为0个情况已经被上面的if判断排除了
        int n = 1;
        // 这个思路就是二分查找，首先把32位的数分为高低16位，如果非零值位于高16位，后续再将高16位继续二分为高低8位，一直二分到集合中只有1个元素
        /*
            将i无符号右移16位后，有二种情况：
                第一种情况，i=0，则第一个非零值位于低16位，i至少有16个前导0，同时将i左移16位（把低16位移到原高16位的位置，这样情况1和情况2就能统一后续的判断方式）
                    例如：数字123的二进制是0000 0000 0000 0000  0000 0000 0111 1011，无符号右移16位后二进制是0000 0000 0000 0000  0000 0000 0000 0000，表明第一个非零值在低16位
                第二种情况，i!=0,则第一个非零值位于高16位，后续在高16位中继续判断
         */
        // 只是判断当i无符号右移16位后是否等于0，如果等于0则表示第一个非零值在低16位，如果不等于则表示第一个非零值在高16位
        if (i >>> 16 == 0) {
            // 执行到这里，表示第一个非零值在低16位，那么表示数字i的高16位全是0，也就是说至少有16个前导0，用n记录起来
            n += 16;
            // 既然i的第一个非零值在低16位，那么将i左移16位，将低16位移动到原来高16位的位置，继续判断
            // 例如：数字123的二进制是0000 0000 0000 0000  0000 0000 0111 1011，左移16位后二进制是0000 0000 0111 1011  0000 0000 0000 0000
            i <<= 16;
        }
        // 判断第一个非零值是否在高8位，如果i>>>24==0为true表示第一个非零值在低8位，为false表示在高8位
        if (i >>> 24 == 0) {
            // 执行到这里，表示第一个非零值在低8位，那么表示数字i的高8位全是0，也就是说至少有8个前导0，用n记录起来
            n += 8;
            // 既然i的第一个非零值在低8位，那么将i左移8位，将低8位移动到原来高8位的位置，继续判断
            // 例如：数字123的二进制是0000 0000 0000 0000  0000 0000 0111 1011
            // 左移16位后二进制是    0000 0000 0111 1011  0000 0000 0000 0000
            // 又左移8位后二进制是   0111 1011 0000 0000  0000 0000 0000 0000
            i <<= 8;
        }
        // 判断第一个非零值是否在高4位，如果i>>>28==0为true表示第一个非零值在低4位，为false表示在高4位
        if (i >>> 28 == 0) {
            // 执行到这里，表示第一个非零值在低4位，那么表示数字i的高4位全是0，也就是说至少有4个前导0，用n记录起来
            n += 4;
            // 既然i的第一个非零值在低4位，那么将i左移4位，将低4位移动到原来高4位的位置，继续判断
            // 例如：数字1的二进制是0000 0000 0000 0000  0000 0000 0000 0001
            // 左移16位后二进制是  0000 0000 0000 0001  0000 0000 0000 0000
            // 又左移8位后二进制是 0000 0001 0000 0000  0000 0000 0000 0000
            // 又左移4位后二进制是 0001 0000 0000 0000  0000 0000 0000 0000
            i <<= 4;
        }
        // 判断第一个非零值是否在高2位，如果i>>>30==0为true表示第一个非零值在低2位，为false表示在高2位
        if (i >>> 30 == 0) {
            // 执行到这里，表示第一个非零值在低2位，那么表示数字i的高2位全是0，也就是说至少有2个前导0，用n记录起来
            n += 2;
            // 既然i的第一个非零值在低2位，那么将i左移2位，将低2位移动到原来高2位的位置，继续判断
            // 例如：数字1的二进制是0000 0000 0000 0000  0000 0000 0000 0001
            // 左移16位后二进制是  0000 0000 0000 0001  0000 0000 0000 0000
            // 又左移8位后二进制是 0000 0001 0000 0000  0000 0000 0000 0000
            // 又左移4位后二进制是 0001 0000 0000 0000  0000 0000 0000 0000
            // 又左移2位后二进制是 0100 0000 0000 0000  0000 0000 0000 0000
            i <<= 2;
        }
        // i >>> 31是判断第一个非零值是否在高1位，如果在高1位则i>>>31得到的结果是1，如果在低1位则i>>>31得到的结果是0
        // n由于是从1开始的，所以这里需要减，如果是从0开始，就需要加
        n -= i >>> 31;
        // 返回统计的前导零个数
        return n;
    }

    /**
     * 即计算整数的尾随零的个数
     * 返回指定int值的二进制补码二进制表示形式中最低位（“最右边”）一位之后的零位数目。 如果指定值的二进制补码表示中没有一位（即等于零），则返回32。
     * 比如数字8的二进制是0000 0000 0000 0000 0000 0000 0000 1000，那么可以得到它的尾随零为3
     * 参考链接：https://mp.csdn.net/editor/html/117340044
     *
     * @param i 要计算其尾随零的数量的值
     * @return 指定的int值的二进制补码二进制表示形式中最低位（“最右”）一位之后的零位数目；如果该值等于零，则为32。
     */
    public static int numberOfTrailingZeros(int i) {
        // 局部变量，临时保存将i左移n（n可能是16、8、4、2）位后的结果
        int y;
        // 快速处理，如果i为0，表示32位都是0，那么不进行下面的判断，直接返回32
        if (i == 0) return 32;
        // 局部变量，记录尾随零的个数
        int n = 31;
        // 用变量y保存i左移16位后的值
        // 例如：数字1的二进制是  0000 0000 0000 0000  0000 0000 0000 0001
        // 左移16位后的二进制是  0000 0000 0000 0001  0000 0000 0000 0000
        y = i << 16;
        if (y != 0) {// 判断i左移16位后的结果是否等于0，如果等于0表示倒数第一个非零值在高16位，如果不等于0表示倒数第一个非零值在低16位
            // 执行到这里，表示倒数第一个非零值在低16位，所以n减去16，因为高16位全是0，需要记录
            n = n - 16;
            // 然后将低16位全部左移到高16位，为了下面继续判断尾随零的个数
            i = y;
        }
        // 左移8位后的二进制是  0000 0001 0000 0000  0000 0000 0000 0000
        y = i << 8;
        if (y != 0) {// 判断i左移8位后的结果是否等于0，如果等于0表示倒数第一个非零值在高8位，如果不等于0表示倒数第一个非零值在低8位
            // 执行到这里，表示倒数第一个非零值在低8位，所以n减去8，因为高8位全是0，需要记录
            n = n - 8;
            // 然后将低8位全部左移到高8位，为了下面继续判断尾随零的个数
            i = y;
        }
        // 左移4位后的二进制是  0001 0000 0000 0000  0000 0000 0000 0000
        y = i << 4;
        if (y != 0) {// 判断i左移4位后的结果是否等于0，如果等于0表示倒数第一个非零值在高4位，如果不等于0表示倒数第一个非零值在低4位
            // 执行到这里，表示倒数第一个非零值在低4位，所以n减去4，因为高4位全是0，需要记录
            n = n - 4;
            // 然后将低4位全部左移到高4位，为了下面继续判断尾随零的个数
            i = y;
        }
        // 左移2位后的二进制是  0100 0000 0000 0000  0000 0000 0000 0000
        y = i << 2;
        if (y != 0) {// 判断i左移2位后的结果是否等于0，如果等于0表示倒数第一个非零值在高2位，如果不等于0表示倒数第一个非零值在低2位
            // 执行到这里，表示倒数第一个非零值在低2位，所以n减去2，因为高2位全是0，需要记录
            n = n - 2;
            // 然后将低2位全部左移到高2位，为了下面继续判断尾随零的个数
            i = y;
        }
        // 左移1位后的二进制是        1000 0000 0000 0000  0000 0000 0000 0000
        // 再无符号右移31后的二进制是  0000 0000 0000 0000  0000 0000 0000 0001
        return n - ((i << 1) >>> 31);
    }

    /**
     * 返回指定int值的二进制补码二进制表示形式中的一位数
     * 即统计指定int值的二进制补码中1的出现次数
     * 例如整数987654321的二进制是0011 1010 1101 1110 0110 1000 1011 0001，其中1出现的次数为17
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117375334
     *
     * @param i 要计数的值
     * @return 返回指定int值的二进制补码中1的出现次数
     */
    public static int bitCount(int i) {
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        i = (i + (i >>> 4)) & 0x0f0f0f0f;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        return i & 0x3f;
    }

    /**
     * 返回通过将指定int值的二进制补码二进制数左移指定位数获得的值。 （位从左手移出，或移到高位，在右侧重新移入，或移出低位。）
     * 请注意，向左旋转负距离等效于向右旋转： rotateLeft(val, -distance) == rotateRight(val, distance) 。
     * 还要注意，以32的任意倍数进行旋转都是空操作，因此，即使距离的最后五个位为负数，也可以忽略所有旋转距离，即使该距离是负数： rotateLeft(val, distance) == rotateLeft(val, distance & 0x1F) 。
     * 例如数字987654321的二进制是               0011 1010 1101 1110 0110 1000 1011 0001
     * 调用rotateLeft(987654321,3)方法后二进制是 1101 0110 1111 0011 0100 0101 1000 1001
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117362244
     *
     * @param i        要向左旋转其位的值
     * @param distance 向左旋转的位的数量
     * @return 通过将指定的int值的二进制补码的二进制补码表示旋转指定的位数而获得的值
     */
    public static int rotateLeft(int i, int distance) {
        /*
            例如：i=987654321, distance=3
            i                                       0011 1010 1101 1110 0110 1000 1011 0001
            i<<distance                             1101 0110 1111 0011 0100 0101 1000 1000
            i >>> -distance                         0000 0000 0000 0000 0000 0000 0000 0001
            (i << distance) | (i >>> -distance)     1101 0110 1111 0011 0100 0101 1000 1001
         */
        // 在移位的时候，如果distance小于0，会根据被移位数的长度进行转换。就比如说这里我们对long进行移位，那么-distance就会被转换成(64 + distance)(注，这里的distance是小于0的)。
        return (i << distance) | (i >>> -distance);// (i >>> -distance)等价于(i >>> 32-distance)，注意是因为int是32位，long是64位的
    }

    /**
     * 返回通过将指定的int值的二进制补码二进制表示右旋转指定的位数获得的值。 （位从右手或低阶移出，左侧重新进入，或高阶移出。）
     * 请注意，负距离的右旋等效于左旋： rotateRight(val, -distance) == rotateLeft(val, distance) 。 还要注意，以32的任意倍数旋转是空操作，因此，即使距离的最后五个位为负数，也可以忽略所有旋转距离，即使该距离为负： rotateRight(val, distance) == rotateRight(val, distance & 0x1F) 。
     * 例如987654321的二进制是                    0011 1010 1101 1110 0110 1000 1011 0001
     * 调用rotateRight(987654321,3)方法后二进制是 0010 0111 0101 1011 1100 1101 0001 0110
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117364187
     *
     * @param i        要向右旋转其位的值
     * @param distance 向右旋转的位的数量
     * @return 通过将指定的int值的二进制补码二进制表示右旋转指定的位数获得的值。
     */
    public static int rotateRight(int i, int distance) {
        /*
            例如：i=987654321, distance=3
            i                                       0011 1010 1101 1110 0110 1000 1011 0001
            i >>> distance                          0000 0111 0101 1011 1100 1101 0001 0110
            i << -distance                          0010 0000 0000 0000 0000 0000 0000 0000
            (i >>> distance) | (i << -distance)     0010 0111 0101 1011 1100 1101 0001 0110
         */
        // 在移位的时候，如果distance小于0，会根据被移位数的长度进行转换。就比如说这里我们对long进行移位，那么-distance就会被转换成(64 + distance)(注，这里的distance是小于0的)。
        return (i >>> distance) | (i << -distance);// (i << -distance)等价于(i << 32-distance)，注意是因为int是32位，long是64位的
    }

    /**
     * 返回通过反转指定int值的二进制补码二进制表示中的位顺序而获得的值。
     * 例如数字987654321的二进制是0011 1010 1101 1110 0110 1000 1011 0001
     * 反转二进制位后是1000 1101 0001 0110 0111 1011 0101 1100
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117388322
     *
     * @param i 要进行反转的int值
     * @return 返回反转二进制位后的数值
     */
    public static int reverse(int i) {
        /*
            基本思路：将32位二进制每两位分组，然后交换每组中相邻的单1位；再每四位分组，交换每组中相邻的2位；再每八位分组，交换每组中相邻的4位；再每十六位、三十二位进行同样的操作。
            例如对十进制数字12345678进行反转
                第一步，两两分组12 34 56 78，交换每组中相邻的单1位21 43 65 87
                第二步，四四分组2143 6587，交换每组中相邻的2位4321 8765
                第三步，八八分组43218765，交换每组中相邻的4位87654321，完成反转
            例如数字987654321的二进制0011 1010 1101 1110 0110 1000 1011 0001
                第一步，两两分组        00 11 10 10 11 01 11 10 01 10 10 00 10 11 00 01
                    交换每组中相邻的1位 00 11 01 01 11 10 11 01 10 01 01 00 01 11 00 10
                第二步，四四分组        0011 0101 1110 1101 1001 0100 0111 0010
                    交换每组中相邻的2位 1100 0101 1011 0111 0110 0001 1101 1000
                第三步，八八分组        11000101 10110111 01100001 11011000
                    交换每组中相邻的4位 01011100 01111011 00010110 10001101
                第四步，十六十六分组     0101110001111011 0001011010001101
                    交换每组中相邻的8位 0111101101011100 1000110100010110
                第五步，三十二三十二分组 01111011010111001000110100010110
                   交换每组中相邻的16位 10001101000101100111101101011100
         */
        i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
        i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
        i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
        i = (i << 24) | ((i & 0xff00) << 8) |
                ((i >>> 8) & 0xff00) | (i >>> 24);
        return i;
    }

    /**
     * 返回指定int值的符号函数（所谓的符号函数就是确定输入值的符号【正数|负数|0】）
     * 如果指定值为负数，则返回值为-1；如果指定值为0，则返回值为0；如果指定值为正数，则返回值为1
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117400525
     *
     * @param i 要计算其符号的值
     * @return 返回指定int值的符号，如果是负数返回-1，0返回0，正数返回1
     */
    public static int signum(int i) {
        // 如果是正数，则可以通过(-i >>> 31)确定符号，即获取最高位的符号。因为(i >> 31)会为0。
        // 如果是负数，则可以通过(i >> 31)确定符号，即获取最高位的符号。因为(-i >>> 31)会为0。
        return (i >> 31) | (-i >>> 31);
    }

    /**
     * 返回通过反转指定的int值的二进制补码表示形式获得的值。
     * 比如3的二进制位是00000000 00000000 00000000 00000011
     * 经过该方法转换后的数的二进制位是00000011 00000000 00000000 00000000
     * 位翻转就是将int当做二进制，左边的位与右边的位进行互换，reverse是按位进行互换，reverseBytes是按byte进行互换。
     * 参考链接：https://blog.csdn.net/cnds123321/article/details/117387242
     *
     * @param i 要反转其字节的值
     * @return 通过反转指定字节中的字节获得的值
     */
    public static int reverseBytes(int i) {
        /*
            知识点
                >>>    :     无符号右移，忽略符号位，空位都以0补齐
                >>     :     右移运算符，num >> 1,相当于num除以2
                <<     :     左移运算符，num << 1,相当于num乘以2
            以3为例：（8位二进制为1个字节）
                i = 3               00000000 00000000 00000000 00000011
                i >>> 24            00000000 00000000 00000000 00000000
                i >> 8              00000000 00000000 00000000 00000000
                0xFF00              00000000 00000000 11111111 00000000
                (i >> 8) & 0xFF00   00000000 00000000 00000000 00000000
                i << 8              00000000 00000000 00000011 00000000
                0xFF0000            00000000 11111111 00000000 00000000
                (i << 8) & 0xFF0000 00000000 00000000 00000000 00000000
                i << 24             00000011 00000000 00000000 00000000
                return              00000011 00000000 00000000 00000000
           以123456789为例：（8位二进制为1个字节）
                i = 123456789       00000111 01011011 11001101 00010101
                i >>> 24            00000000 00000000 00000000 00000111
                i >> 8              00000000 00000111 01011011 11001101
                0xFF00              00000000 00000000 11111111 00000000
                (i >> 8) & 0xFF00   00000000 00000000 01011011 00000000
                i << 8              01011011 11001101 00010101 00000000
                0xFF0000            00000000 11111111 00000000 00000000
                (i << 8) & 0xFF0000 00000000 11001101 00000000 00000000
                i << 24             00010101 00000000 00000000 00000000
                return              00010101 11001101 01011011 00000111
         */
        return ((i >>> 24)) |
                ((i >> 8) & 0xFF00) |
                ((i << 8) & 0xFF0000) |
                ((i << 24));
    }

    /**
     * 求两个数的和
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return 返回两个操作数的和
     */
    public static int sum(int a, int b) {
        // 直接返回两数之和
        return a + b;
    }

    /**
     * 返回两个int类型数间的最大值
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return 两个操作数之间的最大值
     */
    public static int max(int a, int b) {
        // 调用Math类的静态方法max()来获取两个操作数的最大值
        return Math.max(a, b);
    }

    /**
     * 返回两个数之间的最小值
     *
     * @param a 第一个操作数
     * @param b 第二个操作数
     * @return 两个操作数之间的最小值
     */
    public static int min(int a, int b) {
        // 调用Math类的静态方法min()来获取两个操作数的最小值
        return Math.min(a, b);
    }

    /**
     * 序列版本ID号
     */
    @Native
    private static final long serialVersionUID = 1360826667806852920L;
}
