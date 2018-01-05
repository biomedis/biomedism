package ru.biomedis.tests;

import org.junit.*;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class CommonTests {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("Начало тестов  ModelDataApp");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void roundTest(){
        double f = 115092 / 100;
        double res =  new BigDecimal(f).setScale(2, RoundingMode.UP).doubleValue();
        Assert.assertEquals(1150.92, res);
    }
}
