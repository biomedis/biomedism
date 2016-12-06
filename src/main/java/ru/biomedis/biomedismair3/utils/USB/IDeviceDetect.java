package ru.biomedis.biomedismair3.utils.USB;


public interface IDeviceDetect {
    /**
     * Запускает поток детектирования устройст
     */
     void startDeviceDetecting();

    /**
     * Останавливает поток детектирования устройств
     */
    void stopDeviceDetecting();
}
