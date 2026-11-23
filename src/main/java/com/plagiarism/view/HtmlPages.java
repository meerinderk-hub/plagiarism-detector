package com.plagiarism.view;

import com.plagiarism.model.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/** Builds the HTML returned by the servlets. ctx is the web application's context path. */
public final class HtmlPages {

    private static final int COMMON_WORDS_SHOWN = 30;

    private HtmlPages() { }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String band(double s) {
        if (s >= 75) return "Very high overlap";
        if (s >= 50) return "High overlap";
        if (s >= 25) return "Moderate overlap";
        return "Low overlap";
    }

    static String bandClass(double s) {
        if (s >= 75) return "vhigh";
        if (s >= 50) return "high";
        if (s >= 25) return "mod";
        return "low";
    }

    private static String date(java.sql.Timestamp t) {
        return new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ENGLISH).format(t);
    }

    private static String pct(double v) {
        return String.format(Locale.ENGLISH, "%.2f", v);
    }

    private static String layout(String ctx, String title, String body) {
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
            + "<title>" + escape(title) + "</title><link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head><body>"
            + "<header class=\"top\"><a class=\"brand\" href=\"" + ctx + "/\">Word Frequency Similarity</a>"
            + "<nav><a href=\"" + ctx + "/\">New comparison</a><a href=\"" + ctx + "/reports\">Reports</a>"
            + "<a href=\"" + ctx + "/documents\">Documents</a></nav></header>"
            + "<main>" + body + "</main></body></html>";
    }

    private static String deleteForm(String ctx, String path, long id, String confirm, String label) {
        return "<form class=\"inline\" method=\"post\" action=\"" + ctx + path + "\" "
            + "onsubmit=\"return confirm('" + confirm + "')\">"
            + "<input type=\"hidden\" name=\"action\" value=\"delete\">"
            + "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">"
            + "<button class=\"btn danger small\" type=\"submit\">" + label + "</button></form>";
    }

    public static String errorPage(String ctx, String message) {
        return layout(ctx, "Error", "<h1>Something went wrong</h1><div class=\"notice error\">"
            + escape(message) + "</div><p><a class=\"btn\" href=\"" + ctx + "/\">Back</a></p>");
    }

    // ---------- reports ----------

    public static String reportPage(String ctx, RunDetail run) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"title-row\"><div><h1>Comparison report #").append(run.id()).append("</h1>");
        sb.append("<p class=\"meta\">").append(date(run.createdAt())).append(" &middot; ")
          .append(run.docCount()).append(" documents &middot; ").append(run.rows().size())
          .append(" pairs &middot; stop words ").append(run.ignoreStopWords() ? "ignored" : "included")
          .append("</p></div>")
          .append(deleteForm(ctx, "/reports", run.id(), "Delete this report?", "Delete report"))
          .append("</div>");
        sb.append("<div class=\"notice\"><strong>Educational indicator only.</strong> This score measures how much "
            + "vocabulary two texts share. It cannot tell copying from a common topic, quotation or shared "
            + "terminology, and it is not a plagiarism verdict. A person should review the texts.</div>");
        sb.append("<p class=\"formula\">similarity = 2 &times; &Sigma; min(freq<sub>A</sub>, freq<sub>B</sub>) "
            + "&divide; (words<sub>A</sub> + words<sub>B</sub>) &times; 100</p>");

        for (ReportRow r : run.rows()) {
            sb.append("<section class=\"card pair\">");
            sb.append("<div class=\"pair-head\"><h2>").append(escape(r.titleA())).append(" <span>vs</span> ")
              .append(escape(r.titleB())).append("</h2>");
            sb.append("<div class=\"score ").append(bandClass(r.similarity())).append("\">")
              .append(pct(r.similarity())).append("%</div></div>");
            sb.append("<div class=\"bar\"><div class=\"fill ").append(bandClass(r.similarity()))
              .append("\" style=\"width:").append(pct(r.similarity())).append("%\"></div></div>");
            sb.append("<p class=\"band\">").append(band(r.similarity())).append("</p>");
            sb.append("<dl class=\"stats\">")
              .append("<div><dt>Words in A</dt><dd>").append(r.wordsA()).append("</dd></div>")
              .append("<div><dt>Words in B</dt><dd>").append(r.wordsB()).append("</dd></div>")
              .append("<div><dt>Shared distinct words</dt><dd>").append(r.commonUnique()).append("</dd></div>")
              .append("<div><dt>Overlap (sum of min counts)</dt><dd>").append(r.overlap()).append("</dd></div>")
              .append("</dl>");
            appendCommonWords(sb, r.commonWords());
            sb.append("</section>");
        }
        return layout(ctx, "Report #" + run.id(), sb.toString());
    }

    private static void appendCommonWords(StringBuilder sb, String stored) {
        if (stored == null || stored.isEmpty()) {
            sb.append("<p class=\"muted\">No common words found.</p>");
            return;
        }
        String[] items = stored.split(",");
        sb.append("<h3>Most common shared words</h3><div class=\"chips\">");
        int shown = 0;
        for (String item : items) {
            if (shown++ >= COMMON_WORDS_SHOWN) break;
            int idx = item.lastIndexOf(':');
            if (idx < 0) continue;
            sb.append("<span class=\"chip\">").append(escape(item.substring(0, idx)))
              .append(" <b>&times;").append(escape(item.substring(idx + 1))).append("</b></span>");
        }
        sb.append("</div>");
        if (items.length > COMMON_WORDS_SHOWN) {
            sb.append("<p class=\"muted\">Showing ").append(COMMON_WORDS_SHOWN).append(" of ")
              .append(items.length).append(" stored shared words.</p>");
        }
    }

    public static String historyPage(String ctx, List<RunSummary> runs) {
        StringBuilder sb = new StringBuilder("<h1>Reports</h1>");
        if (runs.isEmpty()) {
            sb.append("<p class=\"muted\">No comparisons yet.</p>");
        } else {
            sb.append("<table class=\"history\"><thead><tr><th>Report</th><th>Date</th><th>Documents</th>"
                + "<th>Highest similarity</th><th></th></tr></thead><tbody>");
            for (RunSummary r : runs) {
                sb.append("<tr><td><a href=\"").append(ctx).append("/report?id=").append(r.id()).append("\">#")
                  .append(r.id()).append("</a></td><td>").append(date(r.createdAt())).append("</td><td>")
                  .append(r.docCount()).append("</td><td><span class=\"score small ")
                  .append(bandClass(r.maxSimilarity())).append("\">").append(pct(r.maxSimilarity()))
                  .append("%</span></td><td>")
                  .append(deleteForm(ctx, "/reports", r.id(), "Delete this report?", "Delete"))
                  .append("</td></tr>");
            }
            sb.append("</tbody></table>");
        }
        return layout(ctx, "Reports", sb.toString());
    }

    // ---------- stored documents ----------

    public static String documentsPage(String ctx, List<StoredDocument> docs) {
        StringBuilder sb = new StringBuilder("<h1>Documents</h1>");
        sb.append("<p class=\"lead\">Every text you compare is stored here. Deleting a document also deletes "
            + "the report pairs that include it.</p>");
        if (docs.isEmpty()) {
            sb.append("<p class=\"muted\">No documents stored yet.</p>");
        } else {
            sb.append("<table class=\"history\"><thead><tr><th>Title</th><th>Words</th><th>Added</th>"
                + "<th></th></tr></thead><tbody>");
            for (StoredDocument d : docs) {
                sb.append("<tr><td><a href=\"").append(ctx).append("/documents?id=").append(d.id()).append("\">")
                  .append(escape(d.title())).append("</a></td><td>").append(d.wordCount()).append("</td><td>")
                  .append(date(d.createdAt())).append("</td><td>")
                  .append(deleteForm(ctx, "/documents", d.id(), "Delete this document and its reports?", "Delete"))
                  .append("</td></tr>");
            }
            sb.append("</tbody></table>");
        }
        return layout(ctx, "Documents", sb.toString());
    }

    public static String documentPage(String ctx, StoredDocument d) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><a href=\"").append(ctx).append("/documents\">&larr; All documents</a></p>");
        sb.append("<h1>").append(escape(d.title())).append("</h1>");
        sb.append("<p class=\"meta\">").append(d.wordCount()).append(" words &middot; added ")
          .append(date(d.createdAt())).append("</p>");
        sb.append("<form class=\"rename\" method=\"post\" action=\"").append(ctx).append("/documents\">")
          .append("<input type=\"hidden\" name=\"action\" value=\"rename\">")
          .append("<input type=\"hidden\" name=\"id\" value=\"").append(d.id()).append("\">")
          .append("<input type=\"text\" name=\"title\" maxlength=\"200\" value=\"").append(escape(d.title())).append("\">")
          .append("<button class=\"btn small\" type=\"submit\">Rename</button></form>");
        sb.append("<pre class=\"content card\">").append(escape(d.content())).append("</pre>");
        sb.append(deleteForm(ctx, "/documents", d.id(), "Delete this document and its reports?", "Delete document"));
        return layout(ctx, d.title(), sb.toString());
    }
}
