package ru.biomedis.starter.USB;


import org.hid4java.HidDevice;

/**
 * Интерфейс для реализации слушателей конкретных устройств
 */
public abstract class PlugDeviceListener{
    private enum State { ATTACHED, DETACHED }
    private State state;

    public PlugDeviceListener() {
        state= State.DETACHED;
    }

    void attachDevice(HidDevice device){
        if(state == State.DETACHED){
            state = State.ATTACHED;
            onAttachDevice(device);
        }
     }

     void detachDevice(HidDevice device){
         if(state == State.ATTACHED) {
             state = State.DETACHED;
             onDetachDevice(device);
         }
    }

     void failure(USBHelper.USBException e){
         state = State.DETACHED;
         onFailure(e);

    }

    public abstract  void onAttachDevice(HidDevice device);
    public abstract void onDetachDevice(HidDevice device);
    public abstract void onFailure(USBHelper.USBException e);
}
