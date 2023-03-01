package SearchEngineApp.utils;

import org.apache.log4j.Logger;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.analyzer.MorphologyAnalyzer;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianAnalyzer;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class CreateLemmasUtil
{
    private static final Logger log = Logger.getLogger(CreateLemmasUtil.class);

    public static HashMap<String,Integer> createLemmasWithCount (String text) throws IOException {
        LuceneMorphology luceneMorphRus= new RussianLuceneMorphology();
        LuceneMorphology luceneMorphEng= new EnglishLuceneMorphology();
        HashMap<String,Integer> lemmas = new HashMap<>();
        text = text.toLowerCase().replace('ё', 'е');
        String[] stringArray = text.split("[^а-яА-ЯёЁa-zA-Z]");
        for(String string : stringArray) {
            if (string.matches("[а-яА-ЯёЁ]+")) {
                for (String word  : getLemmas(string, luceneMorphRus)) {
                    if (lemmas.containsKey(word)) {
                        int value = lemmas.get(word);
                        lemmas.put(word, value + 1);
                    } else {
                        lemmas.put(word, 1);
                    }
                }
            } else {
                for (String word  : getLemmas(string, luceneMorphEng)) {
                    if (lemmas.containsKey(word)) {
                        int value = lemmas.get(word);
                        lemmas.put(word, value + 1);
                    } else {
                        lemmas.put(word, 1);
                    }
                }
            }
        }
        return lemmas;
    }

    private static List<String> getLemmas (String string, LuceneMorphology luceneMorph) {
        List<String> rightWords = new ArrayList<>();
        if (!string.equals("")) {
            try {
                List<String> wordList = luceneMorph.getNormalForms(string.toLowerCase());
                List<String> wordInfo = luceneMorph.getMorphInfo(string.toLowerCase());
                String[] infoArray = wordInfo.get(0).split("[|\\s]+");
                if (!(infoArray[1].equals("l") || infoArray[1].equals("n") || infoArray[1].equals("o") || infoArray[1].equals("p"))) {
                    rightWords.addAll(wordList);
                }
            }
            catch (Exception ex) {
                log.warn("Ошибка лемматизатора, на слове : " + string);
            }
        }
        return rightWords;
    }

    public static List<List<String>> createLemmas (String text) throws IOException {
        LuceneMorphology luceneMorphRus= new RussianLuceneMorphology();
        LuceneMorphology luceneMorphEng= new EnglishLuceneMorphology();
        List<List<String>> lemmasList = new ArrayList<>();
        text = text.toLowerCase().replace('ё', 'е');
        String[] stringArray = text.split("[^а-яА-ЯёЁa-zA-Z]");
        for(String string : stringArray) {
            List<String> list;
            if (string.matches("[а-яА-ЯёЁ]+")) {
                if (getLemmas(string, luceneMorphRus).size() > 0) {
                    list = new ArrayList<>(getLemmas(string, luceneMorphRus));
                    lemmasList.add(list);
                }
            } else {
                if (getLemmas(string, luceneMorphEng).size() > 0) {
                    list = new ArrayList<>(getLemmas(string, luceneMorphEng));
                    lemmasList.add(list);
                }
            }
        }
        return lemmasList;
    }

    public static TreeMap<Integer,List<String>> getIndexLemmas(String[] textArray) throws IOException {
        LuceneMorphology luceneMorphRus = new RussianLuceneMorphology();
        LuceneMorphology luceneMorphEng = new EnglishLuceneMorphology();
        TreeMap<Integer, List<String>> lemmasMap = new TreeMap<>();
        for (int i = 0; i < textArray.length; i++) {
            String[] clearString = textArray[i].split("[^а-яА-ЯёЁa-zA-Z]");
            if(clearString.length != 0) {
                if (textArray[i].matches("[а-яА-ЯёЁ]+")) {
                    if(getLemmas(clearString[0], luceneMorphRus).size() > 0) {
                        lemmasMap.put(i, getLemmas(clearString[0], luceneMorphRus));
                    }
                }
                else {
                    if(getLemmas(clearString[0], luceneMorphEng).size() > 0) {
                        lemmasMap.put(i, getLemmas(clearString[0], luceneMorphEng));
                    }
                }
            }
        }
        return lemmasMap;
    }
}
