package com.core.webhook.constant;

import com.core.webhook.exception.NotFoundException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ConnectorEnum {

    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    MERCADOPAGO("Mercado Pago"),
    CONNECTOR("connector"),
    MERCADO_PAGO_CHECKOUT_PRO("MercadoPagoCheckoutPro");

    private final String name;

    ConnectorEnum(String name) {
        this.name = name;
    }

    public static ConnectorEnum fromDisplayName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unknown GatewayConnector: " + name));
    }

}
