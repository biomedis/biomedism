package ru.biomedis.biomedismair3.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ananta on 12.04.2017.
 */
public class SearchName {
    private List<NamePart> nameParts = new ArrayList<>(3);
    private List<NamePart> oNameParts = new ArrayList<>(3);

    private boolean matchedName;
    private boolean matchedOName;
    private boolean matched;

    private boolean search(String target, String pattern, List<NamePart> dstParts) {
        if (target.isEmpty()) return false;
        if (!dstParts.isEmpty()) dstParts.clear();

        String noCaseTarget = target.toLowerCase();
        String noCasePattern = pattern.toLowerCase();

        if (!noCaseTarget.contains(noCasePattern)) return false;
        if (pattern.length() == target.length()) {
            //если строки равны по длине и найдено вхождение, значит они вообще равны
            dstParts.add(new NamePart(target, true));
            return true;
        }
        int start = 0;

        //используется тот факт что есть совпадение и они меньше всей строки и хотябы одно есть
        int ind;
        while (true) {
            ind = noCaseTarget.indexOf(noCasePattern, start);
            if (ind != -1) {
                if (ind == start) {
                    dstParts.add(new NamePart(pattern, true));
                    start += pattern.length();
                    continue;
                } else if (ind != -1) {
                    dstParts.add(new NamePart(target.substring(start, ind), false));
                    dstParts.add(new NamePart(pattern, true));
                    start = ind + pattern.length();
                    if (start >= target.length() - 1) break;
                }
            } else {
                if (start < target.length() - 1) {
                    dstParts.add(new NamePart(target.substring(start, target.length()), false));
                    break;
                }
            }
        }
        return true;

    }

    public boolean search(String name, String oName, String pattern) {
        if (matched) clean();
        if (pattern.isEmpty()) return false;
        matchedName = search(name, pattern, nameParts);
        matchedOName = search(oName, pattern, oNameParts);

        matched = matchedName || matchedOName;
        return matched;
    }


    public List<NamePart> getNameParts() {
        return Collections.unmodifiableList(nameParts);
    }

    public List<NamePart> getONameParts() {
        return Collections.unmodifiableList(oNameParts);
    }

    public boolean isMatchedName() {
        return matchedName;
    }

    public boolean isMatchedOName() {
        return matchedOName;
    }

    public boolean hasMatching() {
        return matched;
    }

    public void clean() {
        matched = false;
        matchedName = false;
        matchedOName = false;
        nameParts.clear();
        oNameParts.clear();
    }

    @Data
    @AllArgsConstructor
    public static class NamePart {
        private String part;
        private boolean matched;
    }
}
