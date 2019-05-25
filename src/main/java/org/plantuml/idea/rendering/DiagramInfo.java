package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

public class DiagramInfo {
    private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(DiagramInfo.class);
    private final int totalPages;
    private final Titles titles;
    private final String filename;

    public DiagramInfo(int totalPages, Titles titles, String filename) {
        this.totalPages = totalPages;
        this.titles = titles;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public Titles getTitles() {
        return titles;
    }

    String getTitle(int i) {
        return getTitles().get(i);
    }

    public static class Titles {
        protected static final Logger logger = Logger.getInstance(Titles.class);

        private final List<String> titles;

        public Titles(List<String> titles) {
            this.titles = titles;
        }

        public int size() {
            return titles.size();
        }

        public String get(int i) {
            if (titles.size() > i) {
                return titles.get(i);
            }
            logger.debug("no title, for page ", i, ", titles=", this);
            return null;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("titles", titles)
                    .toString();
        }
    }
}
