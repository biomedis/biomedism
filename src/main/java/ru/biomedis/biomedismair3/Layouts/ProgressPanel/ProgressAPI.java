package ru.biomedis.biomedismair3.Layouts.ProgressPanel;

public interface ProgressAPI {
    /**
     * Отобразит строку информации
     * @param message
     */
     void setInfoMessage(String message);

    /**
     *
     * @param value 0 - 1.0
     * @param textAction ниже textInfo
     * @param textInfo вверху
     */
     void setProgressBar(double value,String textAction,String textInfo);


     void hideProgressBar(boolean animation);




    /**
     * Установит значение прогресса и текст, сделает все видимым
     * @param value
     * @param text
     */
     void setProgressIndicator(double value,String text);


     void setProgressIndicator(double value);

    /**
     * Установит неопределенное значение прогресса и текст. Все сделает видимым
     * @param text
     */
     void setProgressIndicator(String text);


    /**
     * Скрывает круговой индикатор прогресса
     */
     void hideProgressIndicator(boolean animation);
}
