package com.ibm.streams.tutorial.exercise4;

import java.util.Arrays;
import java.util.List;
import com.ibm.streams.tutorial.exercise4.Document.Page;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class Documents {

    /**
     * Return the titles from a list of documents.
     */
    public static List<String> titlesOf(Document... documents) {
        return Arrays.stream(documents)
                .map(d -> d.getTitle())
                .collect(toList());
    }

    public static Integer characterCount(Page page) {
        return page.getContent().length();
    }

    public static List<Integer> pageCharacterCounts(Document document) {
        return document.getPages().stream()
                .map(doc -> Documents.characterCount(doc))
                .collect(toList());
    }

    public static String print(Document document, PagePrinter pagePrinter) {
        StringBuilder output = new StringBuilder();

        output.append(pagePrinter.printTitlePage(document));
        document.getPages().stream()
                .map(p -> pagePrinter.printPage(p))
                .forEach(s -> output.append(s));

        return output.toString();
    }

    public static Document translate(Document document, Translator translator) {
        return document.getPages().stream()
                .map(page -> page.getContent())
                .map(content -> translator.translate(content))
                .map(translated -> new Page(translated))
                .collect(collectingAndThen(toList(),
                                           pages -> new Document(translator.translate(document.getTitle()), pages)));
    }
}
