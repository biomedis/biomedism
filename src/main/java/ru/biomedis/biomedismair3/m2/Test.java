package ru.biomedis.biomedismair3.m2;


import ru.biomedis.biomedismair3.utils.USB.PlugDeviceListener;
import ru.biomedis.biomedismair3.utils.USB.USBHelper;

import java.io.IOException;
import java.util.Arrays;

public class Test
{
    private int [] testResult ={0,0,0,40,8,2,0,130,8,2,0,168,8,2,0,215,8,2,0,250,8,2,1,28,8,2,1,58,8,2,1,91,8,2,1,124,8,2,1,156,5,77,105,107,111,101,1,0,30,0,30,0,3,4,109,121,95,48,3,0,0,3,232,0,0,39,16,0,1,134,160,3,0,0,8,152,0,0,85,240,0,3,99,48,1,0,0,100,100,0,6,115,101,99,111,110,100,2,0,0,12,228,0,0,128,232,0,4,77,105,110,100,2,0,0,156,64,0,1,60,104,0,6,75,118,97,122,97,114,1,0,30,0,30,0,1,4,109,121,95,49,3,0,0,18,242,0,0,1,224,0,0,189,116,1,0,7,102,136,0,4,75,101,105,108,1,0,17,0,17,0,2,4,109,121,95,50,3,0,0,133,52,0,0,1,85,0,0,11,224,0,5,68,111,112,95,49,2,0,5,52,8,0,0,13,82,0,8,84,105,114,105,115,116,111,114,1,0,30,0,30,0,1,4,109,121,95,51,3,0,1,144,0,0,0,48,112,0,0,0,124,0,7,86,97,114,105,99,97,112,1,0,30,0,30,0,1,4,109,121,95,52,3,0,1,73,127,0,2,208,31,0,0,72,19,0,3,79,105,108,1,0,30,0,30,0,1,4,109,121,95,53,3,0,0,57,208,0,0,5,200,0,0,96,224,0,6,80,108,97,122,109,97,1,0,30,0,30,0,1,4,109,121,95,54,3,0,1,113,186,0,1,35,154,0,0,57,58,0,6,86,101,99,116,111,114,1,0,30,0,30,0,1,4,109,121,95,55,3,0,23,81,236,0,2,84,254,0,0,59,176,0,5,83,99,114,101,119,1,0,30,0,30,0,1,4,109,121,95,56,3,0,30,132,128,0,3,13,64,0,0,78,32,0,5,80,111,105,110,116,1,0,30,0,30,0,1,4,109,121,95,57,3,0,13,247,100,0,1,101,138,0,0,31,164};



    public M2BinaryFile testData() throws M2Complex.MaxTimeByFreqBoundException, M2Complex.MaxPauseBoundException, M2Program.ZeroValueFreqException, M2Program.MaxProgramIDValueBoundException, M2Program.MinFrequenciesBoundException, M2Complex.MaxCountProgramBoundException {


        M2BinaryFile binaryFile=new M2BinaryFile();


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



            complex=new M2Complex(30,30,"Kvazar","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(48.5, 4.8, 485.0, 4850.0),
                    0,
                    "my_1",
                    "en"));

            binaryFile.addComplex(complex);



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


            complex=new M2Complex(30,30,"Tiristor","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(1024.0, 124.0, 1.24),
                    0,
                    "my_3",
                    "en"));

            binaryFile.addComplex(complex);

            complex=new M2Complex(30,30,"Varicap","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(843.51, 1843.51, 184.51),
                    0,
                    "my_4",
                    "en"));

            binaryFile.addComplex(complex);


            complex=new M2Complex(30,30,"Oil","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(148.0, 14.8,248.0),
                    0,
                    "my_5",
                    "en"));

            binaryFile.addComplex(complex);


            complex=new M2Complex(30,30,"Plazma","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(946.5, 746.5, 146.5),
                    0,
                    "my_6",
                    "en"));

            binaryFile.addComplex(complex);


            complex=new M2Complex(30,30,"Vector","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(15283.0,1528.3, 152.8),
                    0,
                    "my_7",
                    "en"));

            binaryFile.addComplex(complex);
            complex=new M2Complex(30,30,"Screw","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList(20000.0, 2000.0, 200.0),
                    0,
                    "my_8",
                    "en"));

            binaryFile.addComplex(complex);


            complex=new M2Complex(30,30,"Point","en");
            complex.addProgram(new M2Program(
                    Arrays.<Double>asList( 9153.0, 915.3, 81.0 ),
                    0,
                    "my_9",
                    "en"));

            binaryFile.addComplex(complex);

            return binaryFile;
        }

    public void test(){




            /*
            System.out.println("Образец="+testResult.length+"  результат="+data.length );
            System.out.println("--Образец--+--Результат--");
            for (int i=0;i<data.length;i++) {
                if(i==binaryFile.getComplexesList().size()*4)System.out.println("___________");
                System.out.println(testResult[i]+" "+(data[i]<0?data[i]+256:data[i]));

            }
*/
            try {
    byte[] data = testData().getData();

            M2BinaryFile parsed=new M2BinaryFile(data,1);
            System.out.println(parsed);

        } catch (M2Complex.MaxPauseBoundException e) {
            e.printStackTrace();
        } catch (M2Complex.MaxTimeByFreqBoundException e) {
            e.printStackTrace();
        } catch (M2Program.ZeroValueFreqException e) {
            e.printStackTrace();
        } catch (M2Complex.MaxCountProgramBoundException e) {
            e.printStackTrace();
        } catch (M2Program.MaxProgramIDValueBoundException e) {
            e.printStackTrace();
        } catch (M2Program.MinFrequenciesBoundException e) {
            e.printStackTrace();
        } catch (M2BinaryFile.MaxBytesBoundException e) {
            e.printStackTrace();
        } catch (LanguageDevice.NoLangDeviceSupported noLangDeviceSupported) {
            noLangDeviceSupported.printStackTrace();
        } catch (M2Complex.ZeroCountProgramBoundException e) {
            e.printStackTrace();
        } catch (M2BinaryFile.FileParseException e) {
            e.printStackTrace();
        }


    }

    public static void main(final String[] args) {
        // Test test=new Test();
        //test.test();


        try {
            USBHelper.initContext();
        } catch (USBHelper.USBException e) {
            throw new RuntimeException(e);
        }

        USBHelper.addPlugEventHandler(M2.productId, M2.vendorId, new PlugDeviceListener() {
            @Override
            public void onAttachDevice() {
                System.out.println("Device connected");
                try {

                    System.out.println("Device read name: "+M2.readDeviceName(false));

                    System.out.println("Device read data: ");
                    M2BinaryFile biofonBinaryFile = M2.readFromDevice(false);
                    for (int i=0;i<biofonBinaryFile.getRawReadedData().length;i++) {

                        System.out.print(biofonBinaryFile.getRawReadedData()[i]<0?biofonBinaryFile.getRawReadedData()[i]+256:biofonBinaryFile.getRawReadedData()[i]);
                        System.out.print(", ");
                    }
                    System.out.println("");



                    System.out.println("CREATE AND WRITE DATA");
                    Test test=new Test();
                    M2BinaryFile binaryFile = test.testData();

                    System.out.println("Writed data:");
                    for (int i=0;i<binaryFile.getRawReadedData().length;i++) {

                        System.out.print(binaryFile.getRawReadedData()[i]<0?binaryFile.getRawReadedData()[i]+256:binaryFile.getRawReadedData()[i]);
                        System.out.print(", ");
                    }
                    System.out.println("");

                    M2.writeToDevice(binaryFile,1,false);

                    System.out.println("COMPLETED!");

                } catch (M2.ReadFromDeviceException e) {
                    e.printStackTrace();
                } catch (M2Program.ZeroValueFreqException e) {
                    e.printStackTrace();
                } catch (M2Complex.MaxPauseBoundException e) {
                    e.printStackTrace();
                } catch (M2BinaryFile.MaxBytesBoundException e) {
                    e.printStackTrace();
                } catch (M2Program.MaxProgramIDValueBoundException e) {
                    e.printStackTrace();
                } catch (M2Complex.MaxCountProgramBoundException e) {
                    e.printStackTrace();
                } catch (M2Program.MinFrequenciesBoundException e) {
                    e.printStackTrace();
                } catch (LanguageDevice.NoLangDeviceSupported noLangDeviceSupported) {
                    noLangDeviceSupported.printStackTrace();
                } catch (M2Complex.ZeroCountProgramBoundException e) {
                    e.printStackTrace();
                } catch (M2Complex.MaxTimeByFreqBoundException e) {
                    e.printStackTrace();
                } catch (M2.WriteToDeviceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDetachDevice() {
                System.out.println("Device disconnected");
            }
        });

        USBHelper.startHotPlugListener(2);

        try {
            System.in.read();
            System.out.println("Выкл");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            USBHelper.stopHotPlugListener();

            USBHelper.closeContext();

        }

    }


}
