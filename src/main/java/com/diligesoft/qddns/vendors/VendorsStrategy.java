package com.diligesoft.qddns.vendors;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

@RegisterForReflection
public class VendorsStrategy implements AggregationStrategy {

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        // put order together in old exchange by adding the order from new exchange

        if (oldExchange == null) {
            // the first time we aggregate we only have the new exchange,
            // so we just return it
            return newExchange;
        }

        String ip1 = oldExchange.getIn().getBody(String.class);
        String ip2 = newExchange.getIn().getBody(String.class);

        if (ip1.equals(ip2)) {
            oldExchange.getIn().setBody(ip1);
        }else {
            oldExchange.getIn().setBody(ip1);
        }

        return oldExchange;
    }
}
