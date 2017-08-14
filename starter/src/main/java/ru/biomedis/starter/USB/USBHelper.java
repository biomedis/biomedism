package ru.biomedis.starter.USB;

import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class USBHelper{

    private static HidServices hidService=null;
    private static List<PlugListenerContainer> plugDeviceListenerList=new ArrayList<>();
    private static HIDListener hidlistener = new HIDListener();

    protected static List<PlugListenerContainer> getPlugDeviceListenerList() {
        return plugDeviceListenerList;
    }

    public static class USBException extends Exception{
        public USBException(String message, Throwable cause) {
            super(message, cause);
        }

        public USBException(String message) {
            super(message);
        }
    }

    /**
     * Инициализация контекста USB библиотеки.
     * Выполняется один раз перед началом работы. По окончанию программы следует вызвате его закрытие
     * @throws USBException
     */
    public static void initContext() throws USBException {

        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();
        hidServicesSpecification.setAutoShutdown(false);
        hidServicesSpecification.setScanInterval(500);
        hidServicesSpecification.setPauseInterval(5000);
        hidServicesSpecification.setScanMode(ScanMode.SCAN_AT_FIXED_INTERVAL_WITH_PAUSE_AFTER_WRITE);
        try {
            hidService = HidManager.getHidServices(hidServicesSpecification);
            hidService.addHidServicesListener(hidlistener);
        }catch (HidException e){
            throw new USBException("Unable to initialize usb.", e);
        }

    }

    private static void checkConnectedDevices() throws USBException {
        HidDevice device;
        for (PlugListenerContainer container : plugDeviceListenerList) {
            device = findDevice(container.getVid(), container.getPid());
            if(device!=null) container.getPlugDeviceListener().attachDevice(device);
        }

    }

    /**
     * Поиск устройства по идентификаторам. Устройство не открывается
     * @param vendorId
     * @param productId
     * @return Вернет HidDevice или null если ничего не найдено
     * @throws USBException выбрасывается если была ошибка обращения с USB
     */
    public static HidDevice findDevice(int vendorId, int productId) throws USBException {
        HidDevice hidDevice = null;
        try{
            // hidDevice = hidService.getHidDevice(vendorId, productId, null);
            List<HidDevice> devices = hidService.getAttachedHidDevices();
            for (HidDevice device : devices) {
                if (device.isVidPidSerial(vendorId, productId, null)) {
                    hidDevice = device;
                    break;
                }
            }
        }catch (Exception e){
            throw new USBException("Error find device",e);
        }

        return    hidDevice;
    }

    /**
     * Печатает дамп устройства в консоль
     *
     * @param device  HidDevice
     *
     */
    public static void dumpDevice(final HidDevice device) throws USBException {
        System.out.println(device.toString());
    }

    /**
     * Добавит слушатель подключений, отключений устройств
     * @param el  PlugDeviceListener, который будет вызываться при событиях с устройством
     */
    public static void addPlugEventHandler(int pid, int vid, PlugDeviceListener el){

        plugDeviceListenerList.add(new PlugListenerContainer(pid,vid,el));
    }

    /**
     * Удалит слушатель подключений и отключений устройств
     * @param el
     */
    public static void removePlugEventHandler(PlugDeviceListener el){

        Iterator<PlugListenerContainer> iterator = plugDeviceListenerList.iterator();
        PlugListenerContainer next=null;
        while (iterator.hasNext()){
            next=iterator.next();
            if(next.getPlugDeviceListener()==el) {
                next.clear();
                iterator.remove();
            }
        }


    }


    /**
     * Начать отслеживать устройства, обработчики для которых были переданы через addPlugEventHandler()
     * Метод самостоятельно выбирает реализацию класса( HotPlugDeviceDetect или FindDeviceDetector)
     *
     */
    public synchronized static void startHotPlugListener() throws USBException {
        if(hidService == null) return;
        try {
            checkConnectedDevices();
            hidService.start();
        }catch (Exception e){
            throw new USBException("Error start listen", e);
        }
    }




    /**
     * Остановка слушателей событий устройств
     */
    public static synchronized void stopHotPlugListener() throws USBException {
        if(hidService == null) return;
        try {
            hidService.shutdown();
        }catch (Exception e){
            throw new USBException("Error stop listen",e);
        }

    }



    private static class HIDListener implements HidServicesListener{
        @Override
        public void hidDeviceAttached(HidServicesEvent event) {
            System.out.println("HID Attach");
            int vid = event.getHidDevice().getVendorId();
            int pid = event.getHidDevice().getProductId();
            plugDeviceListenerList.forEach(el->{
                if(el.checkID(pid ,vid))el.getPlugDeviceListener().attachDevice(event.getHidDevice());
            });
        }

        @Override
        public void hidDeviceDetached(HidServicesEvent event) {
            System.out.println("HID detach");
            int vid = event.getHidDevice().getVendorId();
            int pid = event.getHidDevice().getProductId();
            plugDeviceListenerList.forEach(el->{
                if(el.checkID(pid ,vid))el.getPlugDeviceListener().detachDevice(event.getHidDevice());
            });
        }

        @Override
        public void hidFailure(HidServicesEvent event) {
            System.out.println("HID fail");
            int vid = event.getHidDevice().getVendorId();
            int pid = event.getHidDevice().getProductId();
            plugDeviceListenerList.forEach(el->{
                if(el.checkID(pid ,vid))el.getPlugDeviceListener().failure(new USBException("Device vid="+vid+" pid="+vid+ " error!  " +event.getHidDevice().getLastErrorMessage()));
            });
        }
    }


    /**
     * Открывает устройство для чтения и записи.
     * После завершения работы необходимо вызвать closeDevice(USBDeviceHandle handle, int interfaceNum)
     * @param pid
     * @param vid
     * @return HidDevice  устройства, который используется в методах чтения и записи
     * @throws USBException в случае ошибки открытия устройства.
     */
    public static HidDevice openDevice(int pid, int vid) throws USBException {

        HidDevice device =null;
        try{
            device = findDevice(vid, pid);
            if (!device.isOpen()) {
                if(!device.open()) throw new Exception();
            }

        }catch (Exception e){
            throw  new USBException("Error opening device");
        }

        return device;
    }


    public static HidDevice openDevice(HidDevice device) throws USBException {
        if(device ==null) throw new NullPointerException();
        try{
            if (!device.isOpen()) {
                if(!device.open()) throw new Exception();
            }
        }catch (Exception e){
            throw  new USBException("Error opening device");
        }

        return device;
    }

    /**
     * Закрывает устройство. Освобождает ресурсы
     * @param device  HidDevice полученый при открытии устройства
     *  @throws USBException в случае ошибки закрытия устройства.
     */
    public static void closeDevice(HidDevice device) throws USBException {
        try{
            if(device!=null)device.close();
        }catch (Exception e){
            throw  new USBException("Error closing device",e);
        }
    }

    /**
     *Производит запись буффера data на устройство. Необходимо в конфигурации устройства определить размер передаваемых буфферов.
     * @param device HidDevice получаемый при открытии устройства
     * @param data байтовый массив
     * @throws USBException в случае ошибки записи на устройства.
     */
    public static void write(HidDevice device, byte[] data) throws USBException {
        try{
            if(!device.isOpen())openDevice(device);
            int val = device.write(data, data.length, (byte) 0x00);
            if (val < 0)  throw new Exception(device.getLastErrorMessage());
        }catch (Exception e){
            throw  new USBException("Error writing device",e);
        }
    }

    /**
     * Читает буффер с устройства. Размер читаемого буффера определяется размером data
     * @param device HidDevice
     * @param timeOut таймаут в МС
     * @return возвращает колличество реально прочитанных байт
     * @throws USBException в случае ошибки чтения устройства.
     */
    public static int read(HidDevice device, byte[] data, int timeOut) throws USBException {
        int val = 0;
        try{
            if(!device.isOpen())openDevice(device);
            val = device.read(data, timeOut);

            if (val < 0)  throw new Exception(device.getLastErrorMessage());
        }catch (Exception e){
            if(e.getCause()!=null) e.printStackTrace();
            else e.printStackTrace();
            throw  new USBException("Error reading device. "+e.getMessage(),e);
        }
        return val;
    }



    /**
     * Контейнер слушателей событий устройств
     */
    protected static class PlugListenerContainer{
        private int pid;
        private int vid;
        private PlugDeviceListener pdl;
        private boolean connected=false;



        public PlugListenerContainer(int pid, int vid, PlugDeviceListener pdl) {
            this.pid = pid;
            this.vid = vid;
            this.pdl = pdl;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public int getPid() {
            return pid;
        }

        public int getVid() {
            return vid;
        }


        public PlugDeviceListener getPlugDeviceListener() {
            return pdl;
        }

        /**
         * Очистка ресурсов контейнера
         */
        public void clear(){this.pdl=null;}

        /**
         * Проверка на соответствие контейнера VID и PID
         * @param pid
         * @param vid
         * @return
         */
        public boolean checkID(int pid, int vid){
            if (pid != this.pid) return false;
            return vid == this.vid;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlugListenerContainer that = (PlugListenerContainer) o;

            if (pid != that.pid) return false;
            return vid == that.vid;

        }

        @Override
        public int hashCode() {
            int result = pid;
            result = 31 * result + vid;
            return result;
        }
    }


    /**
     * Явное закрытие контекста. Деинициализация работы с USB, освобождение ресурсов.
     * После чего класс становиться не работоспособным
     */
    public static void closeContext(){
        if(hidService==null) return;
        hidService.shutdown();
        hidService.removeUsbServicesListener(hidlistener);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        hidService.shutdown();
        hidService.removeUsbServicesListener(hidlistener);
    }


}
