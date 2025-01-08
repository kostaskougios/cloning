package com.rits.cloning;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Accessor Functions} for {@link #ACCESSOR accessing} {@link Field}s.
 *
 * <p>The access method can be specified via the {@code com.rits.cloning.Fields.accessor} system-property setting to:
 * <ul>
 *     <li>{@code auto} - (default) auto-select best available accessor</li>
 *     <li>{@code unsafe} - fastest and allows for accessing JDK internal fields without the need for {@code --add-opens}</li>
 *     <li>{@code handles} - fast, but requires {@code --add-opens} to access JDK internal fields; falls back on {@code reflection} to set {@code final}s</li>
 *     <li>{@code reflection} - legacy approach, but requires {@code --add-opens} to access JDK internal fields</li>
 * </ul>
 *
 * <p>These APIs are considered low level and is intentionally restricted to package-private access. Mismatching objects
 * and fields, or {@link Accessor#getCookie cookies} can lead to undefined behavior.
 *
 * @author mark.falco
 * @since December 2024
 */
class Fields {
    /**
     * Available {@link Accessor} types.
     */
    private enum AccessorType {
        AUTO,
        UNSAFE,
        HANDLES,
        REFLECTION;

        /**
         * @return the {@link Accessor} of the specified {@link AccessorType} or closest available match.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Accessor<Object> resolve(AccessorType type) {
            switch (type) {
                case AUTO:   try {return (Accessor) UnsafeAccessor    .INSTANCE;} catch (Throwable e) {/*fall through*/}
                case HANDLES:     return (Accessor) VarHandleAccessor .INSTANCE;
                case REFLECTION:  return (Accessor) ReflectionAccessor.INSTANCE;
                case UNSAFE:      return (Accessor) UnsafeAccessor    .INSTANCE;
                default:          throw new IllegalArgumentException("Unknown accessor type: " + type);
            }
        }
    }

    /**
     * The configured {@link Accessor} for accessing fields.
     */
    static final Accessor<Object> ACCESSOR = AccessorType.resolve(AccessorType.valueOf(System.getProperty(
            Fields.class.getName() + ".accessor", AccessorType.AUTO.name()).trim().toUpperCase()));

    /**
     * Blocked constructor.
     */
    private Fields() {}

    /**
     * Interface for accessing fields of objects.
     *
     * @param <C> the cookie type for this accessor
     */
    interface Accessor<C> {
        /**
         * Return the cookie for a given field.
         *
         * @param field the field to get a cookie for
         * @return the cookie
         */
        C getCookie(Field field);

        /**
         * Return the specified field from the source object.
         *
         * @param field the field to get
         * @param cookie the field {@link #getCookie cookie}
         * @param src the object to get the field value from
         * @return the field value
         */
        Object get(Field field, C cookie, Object src) throws IllegalAccessException;

        /**
         * Set the specified field in the destination object.
         *
         * @param field the field to set
         * @param cookie the field {@link #getCookie cookie}
         * @param dst the object to set the field in
         * @param value the new value for the field
         */
        void set(Field field, C cookie, Object dst, Object value) throws IllegalAccessException;

        /**
         * Copy (shallow clone) the specified field from the source to destination object.
         *
         * @param field the field to copy
         * @param cookie the field {@link #getCookie cookie}
         * @param src the source object
         * @param dst the destination object
         */
        void copy(Field field, C cookie, Object src, Object dst) throws IllegalAccessException;
    }

    /**
     * Legacy reflection based {@link Accessor}.
     */
    private static class ReflectionAccessor implements Accessor<Field> {
        private static final ReflectionAccessor INSTANCE = new ReflectionAccessor();

        @Override
        public Field getCookie(Field field) {
            field.trySetAccessible();
            return field;
        }

        @Override
        public Object get(Field field, Field cookie, Object src) throws IllegalAccessException {
            return cookie.get(src);
        }

        @Override
        public void set(Field field, Field cookie, Object dst, Object value) throws IllegalAccessException {
            cookie.set(dst, value);
        }

        @Override
        public void copy(Field field, Field cookie, Object src, Object dst) throws IllegalAccessException {
            Class<?> t = field.getType();
            if      (!t.isPrimitive()  ) cookie.set       (dst, cookie.get       (src));
            else if (t == int.class    ) cookie.setInt    (dst, cookie.getInt    (src));
            else if (t == long.class   ) cookie.setLong   (dst, cookie.getLong   (src));
            else if (t == boolean.class) cookie.setBoolean(dst, cookie.getBoolean(src));
            else if (t == double.class ) cookie.setDouble (dst, cookie.getDouble (src));
            else if (t == float.class  ) cookie.setFloat  (dst, cookie.getFloat  (src));
            else if (t == char.class   ) cookie.setChar   (dst, cookie.getChar   (src));
            else if (t == byte.class   ) cookie.setByte   (dst, cookie.getByte   (src));
            else if (t == short.class  ) cookie.setShort  (dst, cookie.getShort  (src));
            else                         cookie.set       (dst, cookie.get       (src));
        }
    }

    /**
     * {@link VarHandle} implementation of {@link Accessor} avoiding per invocation access checks made with reflection.
     */
    private static class VarHandleAccessor implements Accessor<VarHandle> {
        private static final VarHandleAccessor INSTANCE = new VarHandleAccessor();

        /**
         * Mapping of fields to their {@link VarHandle} for a given class.
         */
        private final ClassValue<Map<Field, VarHandle>> handleByField = new ClassValue<>() {
            /**
             * The {@link MethodHandles.Lookup} used to find {@link VarHandle}s.
             */
            private final MethodHandles.Lookup lookup = MethodHandles.lookup();

            @Override
            protected Map<Field, VarHandle> computeValue(Class<?> clz) {
                Map<Field, VarHandle> map = new HashMap<>();
                try {
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clz, this.lookup);
                    for (Field f : clz.getDeclaredFields()) {
                        f.trySetAccessible();
                        map.put(f, lookup.unreflectVarHandle(f));
                    }
                } catch (ReflectiveOperationException e) {
                    throw new CloningException(e);
                }

                return map;
            }
        };

        @Override
        public VarHandle getCookie(Field field) {
            return handleByField.get(field.getDeclaringClass()).get(field);
        }

        @Override
        public Object get(Field field, VarHandle h, Object src) {
            boolean v = Modifier.isVolatile(field.getModifiers());
            return src == null
                    ? v ? h.getVolatile()    : h.get()
                    : v ? h.getVolatile(src) : h.get(src);
        }

        @Override
        public void set(Field field, VarHandle h, Object dst, Object value) throws IllegalAccessException {
            // note we don't need volatile writes during cloning as dst is not yet visible to other threads
            if (Modifier.isFinal(field.getModifiers())) { // VarHandle can't update finals; fall back on reflection
                ReflectionAccessor.INSTANCE.set(field, ReflectionAccessor.INSTANCE.getCookie(field), dst, value);
            } else if (dst == null) {
                h.set(value);
            } else {
                h.set(dst, value);
            }
        }

        @Override
        public void copy(Field field, VarHandle hand, Object src, Object dst) throws IllegalAccessException {
            int mods = field.getModifiers();
            if (Modifier.isFinal(mods)) { // VarHandle can't update finals; fall back on reflection
                ReflectionAccessor.INSTANCE.copy(field, ReflectionAccessor.INSTANCE.getCookie(field), src, dst);
                return;
            }

            // note: we don't need volatile writes during cloning as dst is not yet visible to other threads; volatile
            // reads are still performed as the source may be visible to other threads
            Class<?> t = field.getType();
            boolean v = Modifier.isVolatile(mods);

            // the seemingly needless casts allow VarHandle to optimize out the autoboxing and its garbage
            if      (!t.isPrimitive()  ) hand.set(dst, v ?           hand.getVolatile(src) :           hand.get(src));
            else if (t == int.class    ) hand.set(dst, v ?     (int) hand.getVolatile(src) :     (int) hand.get(src));
            else if (t == long.class   ) hand.set(dst, v ?    (long) hand.getVolatile(src) :    (long) hand.get(src));
            else if (t == boolean.class) hand.set(dst, v ? (boolean) hand.getVolatile(src) : (boolean) hand.get(src));
            else if (t == double.class ) hand.set(dst, v ?  (double) hand.getVolatile(src) :  (double) hand.get(src));
            else if (t == float.class  ) hand.set(dst, v ?   (float) hand.getVolatile(src) :   (float) hand.get(src));
            else if (t == char.class   ) hand.set(dst, v ?    (char) hand.getVolatile(src) :    (char) hand.get(src));
            else if (t == byte.class   ) hand.set(dst, v ?    (byte) hand.getVolatile(src) :    (byte) hand.get(src));
            else if (t == short.class  ) hand.set(dst, v ?   (short) hand.getVolatile(src) :   (short) hand.get(src));
            else                         hand.set(dst, v ?           hand.getVolatile(src) :           hand.get(src));
        }
    }

    /**
     * {@code sun.misc.Unsafe} implementation of {@link Accessor}.
     *
     * <p>This is the only accessor which does not require explicit JVM configuration of {@code --add-opens} to allow
     * access to JDK internal fields.
     *
     * <p>Support for {@code sun.misc.Unsafe} may eventually go away, but this still work up through at least Java 23.
     */
    private static class UnsafeAccessor implements Accessor<Long> {
        private static final UnsafeAccessor INSTANCE = new UnsafeAccessor();

        /**
         * The {@code sun.misc.Unsafe} reference.
         *
         * <p>This is our one explicit reference to {@code sun.misc.Unsafe} in order to minimize compiler warnings
         */
        @SuppressWarnings("all")
        private final sun.misc.Unsafe u = findUnsafe();

        /**
         * Find {@code sun.misc.Unsafe}.
         */
        @SuppressWarnings("unchecked")
        private static <UNSAFE> UNSAFE findUnsafe() {
            try {
                Field theUnsafe = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
                theUnsafe.trySetAccessible();
                return (UNSAFE) theUnsafe.get(null);
            } catch (ReflectiveOperationException e) {
                throw new CloningException(e);
            }
        }

        /**
         * Mapping of fields to their offsets for a given class.
         */
        @SuppressWarnings("deprecation")
        private final ClassValue<Map<Field, Long>> offsetByField = new ClassValue<>() {
            @Override
            protected Map<Field, Long> computeValue(Class<?> clz) {
                Map<Field, Long> map = new HashMap<>();
                for (Field f : clz.getDeclaredFields()) {
                    try {
                        long of = Modifier.isStatic(f.getModifiers()) ? u.staticFieldOffset(f) : u.objectFieldOffset(f);
                        map.put(f, of < 0 ? null : of);
                    } catch (UnsupportedOperationException e) {
                        map.put(f, null); // skip the field, this will result in get/set/copy falling back on VarHandle
                    }
                }
                return map;
            }
        };

        /**
         * Mapping of static fields to their "base object" for a given class.
         */
        @SuppressWarnings("deprecation")
        private final ClassValue<Map<Field, Object>> baseByStaticField = new ClassValue<>() {
            @Override
            protected Map<Field, Object> computeValue(Class<?> clz) {
                Map<Field, Object> map = new HashMap<>();
                for (Field f : clz.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())) {
                        try {
                            map.put(f, u.staticFieldBase(f));
                        } catch (UnsupportedOperationException e) {
                            map.put(f, null); // skip the field, this will result in get/set/copy falling back on VarHandle
                        }
                    }
                }
                return map;
            }
        };

        @Override
        public Long getCookie(Field field) {
            return offsetByField.get(field.getDeclaringClass()).get(field);
        }

        @Override
        public Object get(Field field, Long of, Object src) {
            if (of == null && (of = getCookie(field)) == null) { // fall back on safe mechanisms
                return VarHandleAccessor.INSTANCE.get(field, VarHandleAccessor.INSTANCE.getCookie(field), src);
            }

            Class<?> t = field.getType();
            int mods = field.getModifiers();
            boolean v = Modifier.isVolatile(mods);
            Object src2 = Modifier.isStatic(mods) ? baseByStaticField.get(field.getDeclaringClass()).get(field) : src;
            return !t.isPrimitive()      ? v ? u.getObjectVolatile (src2, of) : u.getObject (src2, of)
                    : t == int.class     ? v ? u.getIntVolatile    (src2, of) : u.getInt    (src2, of)
                    : t == long.class    ? v ? u.getLongVolatile   (src2, of) : u.getLong   (src2, of)
                    : t == boolean.class ? v ? u.getBooleanVolatile(src2, of) : u.getBoolean(src2, of)
                    : t == double.class  ? v ? u.getDoubleVolatile (src2, of) : u.getDouble (src2, of)
                    : t == float.class   ? v ? u.getFloatVolatile  (src2, of) : u.getFloat  (src2, of)
                    : t == char.class    ? v ? u.getCharVolatile   (src2, of) : u.getChar   (src2, of)
                    : t == byte.class    ? v ? u.getByteVolatile   (src2, of) : u.getByte   (src2, of)
                    : t == short.class   ? v ? u.getShortVolatile  (src2, of) : u.getShort  (src2, of)
                    : VarHandleAccessor.INSTANCE.get(field, VarHandleAccessor.INSTANCE.getCookie(field), src);
        }

        @Override
        public void set(Field field, Long of, Object dst, Object value) throws IllegalAccessException {
            if (of == null && (of = getCookie(field)) == null) { // fall back on safe mechanisms
                VarHandleAccessor.INSTANCE.set(field, VarHandleAccessor.INSTANCE.getCookie(field), dst, value);
                return;
            }

            // note we don't need volatile writes during cloning as the object is not yet visible to other threads
            Object dst2 = Modifier.isStatic(field.getModifiers()) ? baseByStaticField.get(field.getDeclaringClass()).get(field) : dst;
            Class<?> t = field.getType();
            if      (!t.isPrimitive()  ) u.putObject (dst2, of,           value);
            else if (t == int.class    ) u.putInt    (dst2, of,     (int) value);
            else if (t == long.class   ) u.putLong   (dst2, of,    (long) value);
            else if (t == boolean.class) u.putBoolean(dst2, of, (boolean) value);
            else if (t == double.class ) u.putDouble (dst2, of,  (double) value);
            else if (t == float.class  ) u.putFloat  (dst2, of,   (float) value);
            else if (t == char.class   ) u.putChar   (dst2, of,    (char) value);
            else if (t == byte.class   ) u.putByte   (dst2, of,    (byte) value);
            else if (t == short.class  ) u.putShort  (dst2, of,   (short) value);
            else VarHandleAccessor.INSTANCE.set(field, VarHandleAccessor.INSTANCE.getCookie(field), dst, value);
        }

        @Override
        public void copy(Field field, Long of, Object src, Object dst) throws IllegalAccessException {
            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || (of == null && (of = getCookie(field)) == null)) { // fall back on safe mechanisms
                VarHandleAccessor.INSTANCE.copy(field, VarHandleAccessor.INSTANCE.getCookie(field), src, dst);
                return;
            }

            // note: we don't need volatile writes during cloning as the object is not yet visible to other threads
            // volatile reads are still performed as the source may be visible to other threads
            Class<?> t = field.getType();
            boolean v = Modifier.isVolatile(mods);
            if      (!t.isPrimitive()  ) u.putObject (dst, of, v ? u.getObjectVolatile (src, of) : u.getObject (src, of));
            else if (t == int.class    ) u.putInt    (dst, of, v ? u.getIntVolatile    (src, of) : u.getInt    (src, of));
            else if (t == long.class   ) u.putLong   (dst, of, v ? u.getLongVolatile   (src, of) : u.getLong   (src, of));
            else if (t == boolean.class) u.putBoolean(dst, of, v ? u.getBooleanVolatile(src, of) : u.getBoolean(src, of));
            else if (t == double.class ) u.putDouble (dst, of, v ? u.getDoubleVolatile (src, of) : u.getDouble (src, of));
            else if (t == float.class  ) u.putFloat  (dst, of, v ? u.getFloatVolatile  (src, of) : u.getFloat  (src, of));
            else if (t == char.class   ) u.putChar   (dst, of, v ? u.getCharVolatile   (src, of) : u.getChar   (src, of));
            else if (t == byte.class   ) u.putByte   (dst, of, v ? u.getByteVolatile   (src, of) : u.getByte   (src, of));
            else if (t == short.class  ) u.putShort  (dst, of, v ? u.getShortVolatile  (src, of) : u.getShort  (src, of));
            else VarHandleAccessor.INSTANCE.copy(field, VarHandleAccessor.INSTANCE.getCookie(field), src, dst);
        }
    }
}