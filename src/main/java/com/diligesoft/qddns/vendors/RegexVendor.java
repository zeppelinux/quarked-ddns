package com.diligesoft.qddns.vendors;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterForReflection
public class RegexVendor extends Vendor {

    Pattern pattern;

    public RegexVendor(String url, String extractor) {
        super(url);
        pattern = Pattern.compile(extractor);
    }

    @Override
    public String extract(String str) {
        Matcher matcher = pattern.matcher(str.trim());
        if (matcher.find()){
            return matcher.group(0);
        }
        throw new IllegalArgumentException(String.format("can't extract ip from the string: %s", str));
    }
}
