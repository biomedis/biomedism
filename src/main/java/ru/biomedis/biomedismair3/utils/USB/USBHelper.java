package ru.biomedis.biomedismair3.utils.USB;

import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Обязательна инициализация initContext()
 * Контекст статческий.
 * Если был вызван closeContext(), то требуется переинициализация
 * <pre><code>
        Device device = USBHelper.findDevice(vendorId, productId);
        if(device==null) {
        System.out.println("Устройство не обнаружено");
        System.exit(0);
        }
        USBHelper.dumpDevice(device);


        USBHelper.addPlugEventHandler(productId, vendorId, new USBHelper.PlugDeviceListener() {
        @Override
        public void onAttachDevice(Device device) {
        System.out.println("Устройство присобачили");
        }

        @Override
        public void onDetachDevice(Device device) {
        System.out.println("Устройство отсобачили");
        }
        });

        USBHelper.startHotPlugListener();

        try {
        System.in.read();
        System.out.println("Выкл");
        } catch (IOException e) {
        e.printStackTrace();
        }finally {
        USBHelper.stopHotPlugListener();
        USBHelper.closeContext();
        }
 </code></pre>
 *
 *
 */
public class USBHelper{

    private static  Context context=null;
    private static List<PlugListenerContainer> plugDeviceListenerList=new ArrayList<>();


    protected static List<PlugListenerContainer> getPlugDeviceListenerList() {
        return plugDeviceListenerList;
    }

    protected static Context getContext() {
        return context;
    }


    public static class USBException extends Exception{
        public USBException(String message, int errorCode) {

            super(String.format("USB error %d: %s: %s", -errorCode, message,
                    LibUsb.strError(errorCode)));
        }
    }

    /**
     * Инициализация контекста USB библиотеки.
     * Выполняется один раз перед началом работы. По окончанию программы следует вызвате его закрытие
     * @throws USBException
     */
    public static void initContext() throws USBException {
       if(context!=null) return;
        // Create the libusb context
         context = new Context();

        // Initialize the libusb context
        int result = LibUsb.init(context);
        if (result < 0)
        {
            throw new USBException("Unable to initialize libusb.", result);
        }
    }


    /**
     * Поиск устройства по идентификаторам
     * @param vendorId
     * @param productId
     * @return Вернет Device или null если ничего не найдено
     * @throws USBException выбрасывается если была ошибка обращения с USB
     */
    public static Device findDevice(short vendorId, short productId) throws USBException {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) throw new USBException("Unable to get device list", result);

        try
        {
            // Iterate over all devices and scan for the right one
            for (Device device: list)
            {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new USBException("Unable to read device descriptor", result);
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
            }
        }
        finally
        {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }

    /**
     * Печатает дамп устройства в консоль
     *
     * @param device  Device
     *
     */
    public static void dumpDevice(final Device device) throws USBException {
        // Dump device address and bus number
        final int address = LibUsb.getDeviceAddress(device);
        final int busNumber = LibUsb.getBusNumber(device);
        System.out.println(String
                .format("Device %03d/%03d", busNumber, address));

        // Dump port number if available
        final int portNumber = LibUsb.getPortNumber(device);
        if (portNumber != 0)
            System.out.println("Connected to port: " + portNumber);

        // Dump parent device if available
        final Device parent = LibUsb.getParent(device);
        if (parent != null)
        {
            final int parentAddress = LibUsb.getDeviceAddress(parent);
            final int parentBusNumber = LibUsb.getBusNumber(parent);
            System.out.println(String.format("Parent: %03d/%03d",
                    parentBusNumber, parentAddress));
        }

        // Dump the device speed
        System.out.println("Speed: "
                + DescriptorUtils.getSpeedName(LibUsb.getDeviceSpeed(device)));

        // Read the device descriptor
        final DeviceDescriptor descriptor = new DeviceDescriptor();
        int result = LibUsb.getDeviceDescriptor(device, descriptor);
        if (result < 0)
        {
            throw new USBException("Unable to read device descriptor",
                    result);
        }

        // Try to open the device. This may fail because user has no
        // permission to communicate with the device. This is not
        // important for the dumps, we are just not able to resolve string
        // descriptor numbers to strings in the descriptor dumps.
        DeviceHandle handle = new DeviceHandle();
        result = LibUsb.open(device, handle);
        if (result < 0)
        {
            System.out.println(String.format("Unable to open device: %s. "
                            + "Continuing without device handle.",
                    LibUsb.strError(result)));
            handle = null;
        }

        // Dump the device descriptor
        System.out.print(descriptor.dump(handle));

        // Dump all configuration descriptors
        dumpConfigurationDescriptors(device, descriptor.bNumConfigurations());

        // Close the device if it was opened
        if (handle != null)
        {
            LibUsb.close(handle);
        }
    }


    private static void dumpConfigurationDescriptors(final Device device,
                                                    final int numConfigurations) throws USBException {
        for (byte i = 0; i < numConfigurations; i += 1)
        {
            final ConfigDescriptor descriptor = new ConfigDescriptor();
            final int result = LibUsb.getConfigDescriptor(device, i, descriptor);
            if (result < 0)
            {
                throw new USBException("Unable to read config descriptor",
                        result);
            }
            try
            {
                System.out.println(descriptor.dump().replaceAll("(?m)^",
                        "  "));
            }
            finally
            {
                // Ensure that the config descriptor is freed
                LibUsb.freeConfigDescriptor(descriptor);
            }
        }
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

private static IDeviceDetect deviceDetector;
    /**
     * Начать отслеживать устройства, обработчики для которых были переданы через addPlugEventHandler()
     * Метод самостоятельно выбирает реализацию класса( HotPlugDeviceDetect или FindDeviceDetector)
     * @param periodSec период опроса шины в секундах
     */
    public synchronized static void startHotPlugListener(int periodSec){
        if( deviceDetector !=null) return;

        if(LibUsb.hasCapability(LibUsb.CAP_HAS_HOTPLUG)) deviceDetector=new HotPlugDeviceDetect(periodSec);
        else  deviceDetector=new FindDeviceDetector(periodSec);

        try {
            deviceDetector.startDeviceDetecting();
        } catch (USBException e) {
            e.printStackTrace();
            closeContext();

        }
    }

    /**
     * Остановка слушателей событий устройств
     */
    public static synchronized void stopHotPlugListener()
    {

        try {
            deviceDetector.stopDeviceDetecting();
        } catch (USBException e) {
            e.printStackTrace();
            closeContext();
        }
        deviceDetector=null;


    }


static public class USBDeviceHandle{
    private DeviceHandle handle;
    private boolean needDetach;

    public USBDeviceHandle(DeviceHandle handle, boolean needDetach) {
        this.handle = handle;
        this.needDetach = needDetach;
    }

    public DeviceHandle getHandle() {
        return handle;
    }

    public boolean isNeedDetach() {
        return needDetach;
    }
}



    /**
     * Открывает устройство для чтения и записи.
     * После завершения работы необходимо вызвать closeDevice(USBDeviceHandle handle, int interfaceNum)
     * @param pid
     * @param vid
     * @param interfaceNum номер интерфейса
     * @return USBDeviceHandle открытого устройства, который используется в методах чтения и записи
     * @throws USBException в случае ошибки открытия устройства.
     */
    public static USBDeviceHandle openDevice(int pid, int vid, int interfaceNum) throws USBException {
        DeviceHandle handle = LibUsb.openDeviceWithVidPid(context, (short)vid,
                (short)pid);

        if (handle == null)
        {
            System.err.println("Test device not found.");
            System.exit(1);
        }

        boolean detach = LibUsb.hasCapability(LibUsb.CAP_SUPPORTS_DETACH_KERNEL_DRIVER)
                && LibUsb.kernelDriverActive(handle, interfaceNum)==1;

// Detach the kernel driver
        if (LibUsb.kernelDriverActive(handle, interfaceNum)==1)
        {
            int result = LibUsb.detachKernelDriver(handle,  interfaceNum);
            if (result != LibUsb.SUCCESS) throw new USBException("Unable to detach kernel driver", result);
        }


        // Claim the ADB interface
        int result = LibUsb.claimInterface(handle, interfaceNum);
        if (result != LibUsb.SUCCESS)
        {
            throw new USBException("Unable to claim interface", result);
        }
        USBDeviceHandle usbDeviceHandle=new USBDeviceHandle(handle,detach);

        return usbDeviceHandle;
    }

    /**
     * Закрывает устройство. Освобождает ресурсы
     * @param handle  USBDeviceHandle полученый при открытии устройства
     * @param interfaceNum  номер интерфейса, который открывался
     *  @throws USBException в случае ошибки закрытия устройства.
     */
    public static void closeDevice(USBDeviceHandle handle, int interfaceNum) throws USBException {
        // Release the ADB interface
        int result = LibUsb.releaseInterface(handle.getHandle(), interfaceNum);
        if (result != LibUsb.SUCCESS)
        {
            throw new USBException("Unable to release interface", result);
        }


        // Close the device
        LibUsb.close(handle.getHandle());

        if (handle.isNeedDetach())
        {
             result = LibUsb.attachKernelDriver(handle.getHandle(),  interfaceNum);
            if (result != LibUsb.SUCCESS) throw new USBException("Unable to re-attach kernel driver", result);
        }
    }

    /**
     *Производит запись буффера data на устройство. Необходимо в конфигурации устройства определить размер передаваемых буфферов.
     * @param handle USBDeviceHandle получаемый при открытии устройства
     * @param data байтовый массив
     * @param outEndPoint адрес конечной точки
     * @param timeout таймаут операции в миллисекундах
     * @throws USBException в случае ошибки записи на устройства.
     */
    public static void write(USBDeviceHandle handle, byte[] data, byte outEndPoint, long timeout) throws USBException {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
        buffer.put(data);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        int result = LibUsb.interruptTransfer(handle.getHandle(), outEndPoint, buffer, transferred, timeout);
        if (result != LibUsb.SUCCESS)
        {
            throw new USBException("Unable to send data", result);
        }

    }

    /**
     * Читает буффер с устройства
     * @param handle USBDeviceHandle
     * @param size размер читаемого буффера
     * @param inEndPoint адрес конечной точки чтения
     * @param timeout таймаут операции в миллисекундах
     * @return возвращает ByteBuffer
     * @throws USBException в случае ошибки чтения устройства.
     */
    public static ByteBuffer read(USBDeviceHandle handle, int size, byte inEndPoint, long timeout) throws USBException {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(size);
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        int result = LibUsb.interruptTransfer(handle.getHandle(), inEndPoint, buffer,
                transferred, timeout);
        if (result != LibUsb.SUCCESS)
        {
            throw new USBException("Unable to read data", result);
        }

        return buffer;
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

       LibUsb.exit(context);
       context=null;
   }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LibUsb.exit(context);
        context=null;

    }


}
