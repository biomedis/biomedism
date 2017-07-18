package ru.biomedis.starter.USB;


public interface IDeviceDetect {
    /**
     * Запускает поток детектирования устройст
     */
     void startDeviceDetecting()throws USBHelper.USBException;

    /**
     * Останавливает поток детектирования устройств
     */
    void stopDeviceDetecting()throws USBHelper.USBException;
}
