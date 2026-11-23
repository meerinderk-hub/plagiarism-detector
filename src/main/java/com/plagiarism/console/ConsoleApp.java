package com.plagiarism.console;

import com.plagiarism.model.Doc;
import com.plagiarism.model.PairRecord;
import com.plagiarism.model.PairResult;
import com.plagiarism.util.SimilarityCalculator;
import com.plagiarism.util.TextProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Java-only version: no servlets and no database.
 *   java com.plagiarism.console.ConsoleApp file1.txt file2.txt [file3.txt ...]
 * With no arguments it asks you to type or paste the texts instead.
 */
public final class ConsoleApp {

    public static void main(String[] args) throws IOException {
        List<String> titles = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        if (args.length >= 2) {
            for (String a : args) {
                titles.add(Path.of(a).getFileName().toString());
                texts.add(Files.readString(Path.of(a), StandardCharsets.UTF_8));
            }
        } else {
            Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
            System.out.print("How many documents (2-10)? ");
            int n = Math.max(2, Math.min(10, Integer.parseInt(in.nextLine().trim())));
            for (int i = 1; i <= n; i++) {
                System.out.println("Document " + i + " - type or paste text, finish with an empty line:");
                StringBuilder sb = new StringBuilder();
                String line;
                while (in.hasNextLine() && !(line = in.nextLine()).isEmpty()) sb.append(line).append('\n');
                titles.add("Document " + i);
                texts.add(sb.toString());
            }
        }

        List<Doc> docs = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            List<String> tokens = TextProcessor.tokenize(texts.get(i), true);
            if (tokens.isEmpty()) {
                System.out.println("Skipping " + titles.get(i) + " (no words).");
                continue;
            }
            docs.add(new Doc(titles.get(i), texts.get(i), TextProcessor.frequencies(tokens), tokens.size()));
        }
        if (docs.size() < 2) {
            System.out.println("Need at least two documents with words.");
            return;
        }

        for (PairRecord p : SimilarityCalculator.compareAll(docs)) {
            PairResult r = p.result();
            System.out.printf("%n%s  vs  %s%n", docs.get(p.indexA()).title(), docs.get(p.indexB()).title());
            System.out.printf("  Similarity: %.2f%%   (words %d / %d, shared distinct words %d)%n",
                    r.similarity(), r.wordsA(), r.wordsB(), r.commonWords().size());
            System.out.print("  Common words: ");
            int shown = 0;
            for (Map.Entry<String, Integer> e : r.commonWords().entrySet()) {
                if (shown++ >= 15) break;
                System.out.print(e.getKey() + "(" + e.getValue() + ") ");
            }
            System.out.println();
        }
        System.out.println("\nNote: an educational similarity indicator, not a plagiarism verdict.");
    }
}
