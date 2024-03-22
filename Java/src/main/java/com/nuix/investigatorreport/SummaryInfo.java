package com.nuix.investigatorreport;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryInfo {
    @Setter
    @Getter
    private String tag = "";

    @Getter
    @Setter
    private String profile = "";

    @Setter
    @Getter
    private String title = "Report Title";

    @Setter
    @Getter
    private String sort = "Item Position";

    @Getter
    @Setter
    private boolean productExportDisabled = false;

    @Getter
    @Setter
    private boolean pdfHyperlinked = false;

    @Getter
    @Setter
    private boolean nativeHyperlinked = false;

    @Getter
    @Setter
    private boolean itemDetailsHyperlinked = true;

    public SummaryInfo() {
    }

    public SummaryInfo(String title) {
        this.title = title;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("tag", tag);
        result.put("profile", profile);
        result.put("title", title);
        result.put("sort", sort);
        result.put("disable_export", productExportDisabled);
        result.put("hyperlink_pdf", pdfHyperlinked);
        result.put("hyperlink_native", nativeHyperlinked);
        result.put("hyperlink_item_details", itemDetailsHyperlinked);
        return result;
    }

    protected boolean isNullOrWhitespace(String value) {
        if (value == null || value.trim().isEmpty())
            return true;
        else
            return false;
    }

    public boolean isValid() {
        if (isNullOrWhitespace(tag) || isNullOrWhitespace(title) || isNullOrWhitespace(profile) || isNullOrWhitespace(sort)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean allAreValid(List<SummaryInfo> infos) {
        for (SummaryInfo info : infos) {
            if (!info.isValid())
                return false;
        }
        return true;
    }
}
