package com.diligesoft.qddns.vendors;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@RegisterForReflection
public class XPathAndRegexVendor extends Vendor {

    XPathExpression expr;

    RegexVendor regexVendor;

    XPathAndRegexVendor(String url, String xpathExtractor, String regex) throws XPathExpressionException {
        super(url);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        expr = xpath.compile(xpathExtractor);
        regexVendor = new RegexVendor(url, regex);
    }

    @Override
    public String extract(String str) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(str)));
        return regexVendor.extract(expr.evaluate(doc));
    }
}
