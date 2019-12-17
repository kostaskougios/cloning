package com.rits.tests.cloning.domain;


import com.rits.tests.cloning.TestAnnotation;

import java.util.Objects;

/**
 * @author th8ra4
 * <p>
 * 16 Dez 2019
 */
public class E {


    @TestAnnotation
    private Long id = 5L;
    private A a = new A();

    public A getA() {
        return a;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof E)) return false;
        E e = (E) o;
        return Objects.equals(id, e.id) &&
                Objects.equals(a, e.a);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, a);
    }
}
