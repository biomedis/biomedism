package ru.biomedis.biomedismair3.utils.USB;


/**
 * Интерфейс для реализации слушателей конкретных устройств
 */
public interface PlugDeviceListener{

    void onAttachDevice();
    void onDetachDevice();
}
