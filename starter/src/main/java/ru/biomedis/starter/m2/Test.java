package ru.biomedis.starter.m2;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test
{

    public static  M2BinaryFile testData() throws M2Complex.MaxTimeByFreqBoundException, M2Complex.MaxPauseBoundException, M2Program.ZeroValueFreqException, M2Program.MaxProgramIDValueBoundException, M2Program.MinFrequenciesBoundException, M2Complex.MaxCountProgramBoundException {


        M2BinaryFile binaryFile=new M2BinaryFile();
        List<M2Complex> listComplexes =new ArrayList<>();

            M2Complex complex=new M2Complex(30,30,"Mikoe","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(10.0, 100.0, 1000.0, 22.0, 220.0, 2222.0, 257.0),
                    0,
                    "my_0",
                    "en"));
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(33.0,330.0),
                    0,
                    "second",
                    "en"));
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(400.0,810.0),
                    0,
                    "Mind",
                    "en"));
            binaryFile.addComplex(complex);
        listComplexes.add(complex);



            complex=new M2Complex(30,30,"Kvazar","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(48.5, 4.8, 485.0, 4850.0),
                    0,
                    "my_1",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);


            complex=new M2Complex(17,17,"Keil","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(341.0, 3.41, 30.4),
                    0,
                    "my_2",
                    "en"));

            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(3410.0, 34.1),
                    0,
                    "Dop_1",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

            complex=new M2Complex(30,30,"Tiristor","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(1024.0, 124.0, 1.24),
                    0,
                    "my_3",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

            complex=new M2Complex(30,30,"Varicap","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(843.51, 1843.51, 184.51),
                    0,
                    "my_4",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

            complex=new M2Complex(30,30,"Oil","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(148.0, 14.8,248.0),
                    0,
                    "my_5",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

            complex=new M2Complex(30,30,"Plazma","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(946.5, 746.5, 146.5),
                    0,
                    "my_6",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

            complex=new M2Complex(30,30,"Vector","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(15283.0,1528.3, 152.8),
                    0,
                    "my_7",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

            complex=new M2Complex(30,30,"Screw","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(20000.0, 2000.0, 200.0),
                    0,
                    "my_8",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

            complex=new M2Complex(30,30,"Point","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList( 9153.0, 915.3, 81.0 ),
                    0,
                    "my_9",
                    "en"));

            binaryFile.addComplex(complex);
        listComplexes.add(complex);

        for (M2Complex m2Complex : listComplexes) {
            binaryFile.addComplex(m2Complex);
        }

        for (M2Complex m2Complex : listComplexes) {
            binaryFile.addComplex(m2Complex);
        }

        for (M2Complex m2Complex : listComplexes) {
            binaryFile.addComplex(m2Complex);
        }
        for (M2Complex m2Complex : listComplexes) {
            binaryFile.addComplex(m2Complex);
        }

        return binaryFile;
        }

}
