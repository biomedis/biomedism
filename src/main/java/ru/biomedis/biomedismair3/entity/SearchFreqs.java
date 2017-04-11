package ru.biomedis.biomedismair3.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Ananta on 11.04.2017.
 */
public class SearchFreqs {
    private List<Freq> freqs=new ArrayList<>();
    private boolean hasMatching;
    private static String END_SEQ=".0";
    private static Pattern replaceCommaPattern = Pattern.compile(",", Pattern.LITERAL);

    /**
     * Очистка поиска
     */
    public void clean(){
        hasMatching=false;
        freqs.forEach(f->f.matched=false);
    }

    public List<Freq> getFreqs() {
        return Collections.unmodifiableList(freqs);
    }

    private void setHasMatching(boolean hasMatching) {
        this.hasMatching = hasMatching;
    }

    /**
     * Были ли совкадения при последнем поиске
     * @return
     */
    public boolean hasMatching() {
        return hasMatching;
    }

    /**
     * Замена , на .
     * @param target
     * @return
     */
    private String replaceComma(String target){

        return replaceCommaPattern.matcher(target).replaceAll(Matcher.quoteReplacement("."));
    }

    /**
     * ,добавление .0
     * @param f обрабатываемая строка
     * @return
     */
    private String normalizeFreq(String f){
        if(!f.contains("."))return f.concat(END_SEQ);
        else if(f.indexOf('.') == f.length()-1) return f.concat("0");
        else return f;
    }


    private void search(String pattern){
        if(hasMatching)clean();
        pattern = replaceComma(pattern);
        List<String> patternList = Arrays.stream(pattern.replace(",", ".").split(" ")).map(this::normalizeFreq).collect(Collectors.toList());
        freqs.forEach(f->{
            f.matched = patternList.contains(f.freq);
            if(f.matched && !hasMatching())setHasMatching(true);
        });


    }
    /**
     * Производит поиск в частотах и выдает  список всех частот Freq
     * @param pattern строка с частотами через пробел, можно задавать числа с нулем и без 4.0 или 4 просто
     * @return
     */
    public List<Freq> searchFreqsResult(String pattern){
        search(pattern);
        return Collections.unmodifiableList(freqs);
    }

    public boolean searchFreqs(String pattern){
        search(pattern);
        return hasMatching;
    }
    /**
     * Парсит новую строку частот из базы. Вызывается при первом создании SearchFreqs и при изменениях значения строки частот в сущности TherapyProgram
     * @param str строка частот
     */
    protected void parseFreqString(String str){
        //распарсить сроку частот. Привести ее к каноническому виду - все частоты заканчиваются 0 те 4.0 итп.
        setHasMatching(false);
        str = replaceComma(str);
        if(str.charAt(str.length()-1)!=';')str=str.concat(";");
        freqs.clear();
        CharSequence freqSeq = str.subSequence(0, str.length());
        int start=0;

        for(int i=0;i<freqSeq.length();i++){
            if(freqSeq.charAt(i)=='+' || freqSeq.charAt(i)==';'){
                freqs.add(new Freq(normalizeFreq(freqSeq.subSequence(start,i).toString()),String.valueOf(freqSeq.charAt(i)),false));
                start = i+1;
            }

        }
    }

    @Override
    public String toString() {
        return "\nSearchFreqs{" +
                "\nfreqs=" + freqs +
                ", \nhasMatching=" + hasMatching +
                '}';
    }

    @Data
    @AllArgsConstructor
    public static class Freq {
        private String freq;
        private String delmitter;
        private boolean matched;
    }

}


