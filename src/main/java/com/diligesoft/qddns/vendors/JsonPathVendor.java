package com.diligesoft.qddns.vendors;

import com.jayway.jsonpath.JsonPath;

public class JsonPathVendor extends Vendor {

    String extractor;

    public JsonPathVendor(String url, String extractor) {
        super(url);
        this.extractor = extractor;
    }

    @Override
    public String extract(String str) {
        Object dataObject = JsonPath.parse(str).read(extractor);
        return dataObject.toString();
    }
}
