package ru.biomedis.biomedismair3.utils.USB;

import java.util.ArrayList;
import java.util.List;

/**
 * Методы для байтовых операций
 *
 */
public class ByteHelper {
    /**
     * Порядок байтов в массиве. Тк часто путаются в терминах, здесь упрощено название.
     * SMALL_TO_BIG - сначала младшие потом старшие
     * BIG_TO_SMALL - сначала старшие потом младшие.
     */
    public  enum ByteOrder{SMALL_TO_BIG,BIG_TO_SMALL}


    /**
     * int в байтовый массив длиной 4 с указаным порядком байт
     * @param value значение
     * @param order порядок байт в выходном массиве
     * @return байтовый массив с заданным порядком
     */
    public static  byte[] intToByteArray(int value, ByteOrder order ) {
        if(order== ByteOrder.SMALL_TO_BIG)
        {
            return new byte[]{
                    (byte) value,
                    (byte) (value >>> 8),
                    (byte) (value >>> 16),
                    (byte) (value >>> 24)
            };
        } else {
            return new byte[] {
                    (byte)(value >>> 24),
                    (byte)(value >>> 16),
                    (byte)(value >>> 8),
                    (byte)value
            };
        }

    }
    /**
     * int в байтовый массив длиной 3 с указаным порядком байт. Если int больше чем может поместиться в 3 байта(2^24-1) 16 777 216, то буде урезан
     * @param value значение
     * @param order порядок байт в выходном массиве
     * @return байтовый массив с заданным порядком
     */
    public static  byte[] intTo3ByteArray(int value, ByteOrder order ) {
        if(order== ByteOrder.SMALL_TO_BIG)
        {
            return new byte[]{
                    (byte) value,
                    (byte) (value >>> 8),
                    (byte) (value >>> 16)

            };
        } else {
            return new byte[] {

                    (byte)(value >>> 16),
                    (byte)(value >>> 8),
                    (byte)value
            };
        }

    }

    /**
     * int в байтовый массив длиной 2 с указаным порядком байт. Если int больше чем может поместиться в 2 байта(2^16-1) 655 535, то буде урезан
     * @param value значение
     * @param order порядок байт в выходном массиве
     * @return байтовый массив с заданным порядком
     */
    public static  byte[] intTo2ByteArray(int value, ByteOrder order ) {
        if(order== ByteOrder.SMALL_TO_BIG)
        {
            return new byte[]{
                    (byte) value,
                    (byte) (value >>> 8)


            };
        } else {
            return new byte[] {


                    (byte)(value >>> 8),
                    (byte)value
            };
        }

    }
    /**
     * int в байтовый список длиной 4 с указаным порядком байт
     * @param value значение
     * @param order порядок байт в выходном массиве
     * @return байтовый список с заданным порядком
     */
    public static  List<Byte> intToByteList(int value, ByteOrder order ) {

        List<Byte> res=new ArrayList<>();
        if(order== ByteOrder.BIG_TO_SMALL){
            res.add((byte)(value >>> 24));
            res.add((byte)(value >>> 16));
            res.add((byte)(value >>> 8));
            res.add((byte)value);
        }else {
            res.add((byte)value);
            res.add((byte)(value >>> 8));
            res.add((byte)(value >>> 16));
            res.add((byte)(value >>> 24));

        }
        return res;

    }

    /**
     * Преобразует int в 3 байта. Если int больше чем может поместиться в 3 байта(2^24-1) 16 777 216, то буде урезан
     * @param value значение
     * @param order порядок байт в выходном массиве
     * @return байтовый список с заданным порядком
     */
    public static  List<Byte> intTo3ByteList(int value, ByteOrder order ) {

        List<Byte> res=new ArrayList<>();
        if(order== ByteOrder.BIG_TO_SMALL){

            res.add((byte)(value >>> 16));
            res.add((byte)(value >>> 8));
            res.add((byte)value);
        }else {
            res.add((byte)value);
            res.add((byte)(value >>> 8));
            res.add((byte)(value >>> 16));


        }
        return res;

    }

    /**
     * Преобразует int в 2 байта. Если int больше чем может поместиться в 2 байта(2^16-1) 655 535, то буде урезан
     * @param value значение
     * @param order порядок байт в выходном массиве
     * @return байтовый список с заданным порядком
     */
    public static  List<Byte> intTo2ByteList(int value, ByteOrder order ) {

        List<Byte> res=new ArrayList<>();
        if(order== ByteOrder.BIG_TO_SMALL){


            res.add((byte)(value >>> 8));
            res.add((byte)value);
        }else {
            res.add((byte)value);
            res.add((byte)(value >>> 8));



        }
        return res;

    }


    /**
     * Преоюразует 4 байта начиная с startPos в int
     * @param val байтовый массив
     * @param startPos начальная позиция(позиция первого читаемого байта)
     * @param order порядок байт в массиве
     * @return значение в int
     * @throws Exception в случае ошибки
     */
    public static int byteArray4ToInt(byte[] val, int startPos, ByteOrder order) throws Exception{
        return byteArrayToInt(val,startPos,startPos+3,order);
    }

    /**
     * Преоюразует 3 байта начиная с startPos в int
     * @param val байтовый массив
     * @param startPos начальная позиция(позиция первого читаемого байта)
     * @param order порядок байт в массиве
     * @return значение в int
     * @throws Exception в случае ошибки
     */
    public static int byteArray3ToInt(byte[] val, int startPos, ByteOrder order) throws Exception{
        return byteArrayToInt(val,startPos,startPos+2,order);
    }

    /**
     * Преоюразует 2 байта начиная с startPos в int
     * @param val байтовый массив
     * @param startPos начальная позиция(позиция первого читаемого байта)
     * @param order порядок байт в массиве
     * @return значение в int
     * @throws Exception в случае ошибки
     */
    public static int byteArray2ToInt(byte[] val, int startPos, ByteOrder order) throws Exception{
        return byteArrayToInt(val,startPos,startPos+1,order);
    }

    /**
     * Преоюразует 1 байт  в позиции startPos в int
     * @param val байтовый масиив
     * @param startPos позиция байта
     *
     * @return
     * @throws Exception в случае ошибки
     */
    public static int byteArray1ToInt(byte[] val, int startPos) throws Exception{
        return (val[startPos] & 0xFF);
    }

    /**
     * Преобразует в int до 4  байтов(задается startPos и stopPos)
     * @param val байтовый массив
     * @param order порядок байт в массиве
     *  @param startPos начальный элемент массива(с которого начнется чтение)
     *  @param stopPos конечный элемент массива, может быть равен startPos, тогда будет обрабатываться один байт.(последний элемент для чтения)
     * @return
     */
    public static int byteArrayToInt(byte[] val, int startPos,int stopPos, ByteOrder order) throws Exception {

        int len = stopPos-startPos+1;
        if(len > 4) throw new Exception("Массив должен иметь не более 4 элементов");
        else if(len <= 0) throw new Exception("startPos должен быть меньше stopPos");
        else if(startPos >=val.length || startPos < 0)throw new Exception("startPos должен в пределах индексов массива");
        else if(stopPos >=val.length || startPos < 0)throw new Exception("stopPos должен в пределах индексов массива");

        if(order== ByteOrder.BIG_TO_SMALL) {
            if(len==4) return  ((val[startPos] & 0xFF) << 24) + ((val[startPos+1] & 0xFF) << 16) + ((val[startPos+2] & 0xFF) << 8) + (val[startPos+3] & 0xFF);
            else if(len==3) return    ((val[startPos] & 0xFF) << 16) + ((val[startPos+1] & 0xFF) << 8) + (val[startPos+2] & 0xFF);
            else if(len==2) return     ((val[startPos] & 0xFF) << 8) + (val[startPos+1] & 0xFF);
            else if(len==1) return    (val[startPos] & 0xFF);
            else throw new Exception("Отрезок массива должен быть не более 4 элементов и не менее 1");
        }else {

            if(len==4) return   ((val[startPos+3] & 0xFF) << 24) + ((val[startPos+2] & 0xFF) << 16) + ((val[startPos+1] & 0xFF) << 8) + (val[startPos] & 0xFF);
            else if(len==3)return    ((val[startPos+2] & 0xFF) << 16) + ((val[startPos+1] & 0xFF) << 8) + (val[startPos] & 0xFF);
            else if(len==2) return    ((val[startPos+1] & 0xFF) << 8) + (val[startPos] & 0xFF);
            else if(len==1) return     (val[startPos] & 0xFF);
            else throw new Exception("Отрезок массив должен быть не более 4 элементов и не менее 1");
        }
    }


    /**
     * Преобразует в int до 4  байтов массива, начиная с начала массива. Определяется длиной массива.
     * Если массив больше 4 будет выброшено исключение
     * @param val
     * @param order порядок байт в массиве
     * @return
     */
    public static int byteArrayToInt(byte[] val, ByteOrder order) throws Exception {

        if(val.length>4) throw new Exception("Массив должен иметь не более 4 элементов");
        if(order== ByteOrder.BIG_TO_SMALL) {
            if(val.length==4) return  ((val[0] & 0xFF) << 24) + ((val[1] & 0xFF) << 16) + ((val[2] & 0xFF) << 8) + (val[3] & 0xFF);
            else if(val.length==3) return    ((val[0] & 0xFF) << 16) + ((val[1] & 0xFF) << 8) + (val[2] & 0xFF);
            else if(val.length==2) return     ((val[0] & 0xFF) << 8) + (val[1] & 0xFF);
            else if(val.length==1) return    (val[0] & 0xFF);
            else throw new Exception("Массив должен иметь не более 4 элементов и не менее 1");
        }else {

            if(val.length==4) return   ((val[3] & 0xFF) << 24) + ((val[2] & 0xFF) << 16) + ((val[1] & 0xFF) << 8) + (val[0] & 0xFF);
            else if(val.length==3)return    ((val[2] & 0xFF) << 16) + ((val[1] & 0xFF) << 8) + (val[0] & 0xFF);
            else if(val.length==2) return    ((val[1] & 0xFF) << 8) + (val[0] & 0xFF);
            else if(val.length==1) return     (val[0] & 0xFF);
            else throw new Exception("Массив должен иметь не более 4 элементов и не менее 1");
        }
    }
    /**
     * Преобразует массив байт в список байт
     * @param src Исходный массив байт
     * @return
     */
    public static List<Byte> byteArrayToByteList(byte[] src){
        List<Byte> dst=new ArrayList<>();
        for(int i=0;i<src.length;i++) dst.add(src[i]);
        return dst;
    }

    /**
     * Получает строку из масиива байт, закодированную в кодировке  srcEncode
     * @param val
     * @param startPos
     * @param size
     * @param order
     * @param srcEncode
     * @return
     * @throws Exception
     */
    public static String byteArrayToString(byte[] val, int startPos,int size, ByteOrder order, String srcEncode) throws Exception{

        byte src[]=new byte[size];

        if(order== ByteOrder.BIG_TO_SMALL){
            for(int i=0;i<size;i++)src[i]=val[startPos+i];
        }else  for(int i=size-1;i<=0;i--)src[i]=val[startPos+i];

        return new String(src,srcEncode);


    }

    /**
     * Преобразует байтовый массив в строку
     * @param array байтовый массив
     * @param byteInLine сколько байт на линию в строке, до переноса
     * @param delmitter разделитель между байтами
     * @return
     */
    public static String bytesToHex(byte[] array,int byteInLine, char delmitter)
    {

        int lineDelmitterNum = array.length/byteInLine;
        char[] val = new char[3*array.length+lineDelmitterNum];
        String hex = "0123456789ABCDEF";
        int lineDelmitterCounter=0;
        for (int i = 0; i < array.length; i++)
        {
            int b = array[i] & 0xff;
            val[3*i+lineDelmitterCounter] = hex.charAt(b >>> 4);
            val[3*i + 1+lineDelmitterCounter] = hex.charAt(b & 15);
            val[3*i + 2+lineDelmitterCounter] =delmitter;
            if( byteInLine*(lineDelmitterCounter+1) == (i+1) ) {
                val[3*i + 2+lineDelmitterCounter+1]='\n';
                lineDelmitterCounter++;
            }
        }
        return String.valueOf(val);
    }
}
