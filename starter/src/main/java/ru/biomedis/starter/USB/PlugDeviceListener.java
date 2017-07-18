package ru.biomedis.starter.USB;


/**
 * Интерфейс для реализации слушателей конкретных устройств
 */
public interface PlugDeviceListener{

    void onAttachDevice();
    void onDetachDevice();
}
