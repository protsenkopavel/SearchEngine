package SearchEngineApp.service;

import SearchEngineApp.models.Lemma;
import SearchEngineApp.models.Site;

import java.util.List;

public interface LemmaService
{
    Lemma saveLemma(Lemma lemma);
    Lemma getLemma(String lemma, long siteId);
    List<Lemma> getLemmas(List<String> nameList);
    List<Lemma> getLemmasFromSite(List<String> nameList, Site site);
    void resetLemmas(List<Lemma> lemma);
    List<Lemma> getLemmas(long siteId);
    long lemmaCount();
}
