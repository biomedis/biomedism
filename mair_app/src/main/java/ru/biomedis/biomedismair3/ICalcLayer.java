package ru.biomedis.biomedismair3;

/**
 * Created by Anama on 01.03.2016.
 */
public interface ICalcLayer
{

    /**
     * Установить текст
     * @param txt
     */
    public void setInfo(String txt);

    /**
     * Блокирование/разблокирование кнопки отмена
     * @param val
     */
    public void setDisableCancel(boolean val);

}
