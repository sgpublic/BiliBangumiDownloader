package io.github.sgpublic.bilidownload;

import org.junit.Test;

import lombok.Data;

@Data
public class LombokTest {
    private int a;
    private int b;

    @Test
    public void test() {
        LombokTest test = new LombokTest();
        test.setA(1);
        System.out.println(test.getA());
    }
}
