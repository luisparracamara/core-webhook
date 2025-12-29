package com.core.webhook.webhook.mercadopago;

import com.core.webhook.constant.ConnectorEnum;
import com.core.webhook.service.ConnectorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MercadoPagoCheckoutPro implements ConnectorService {

    @Override
    public void processWebhook(HttpServletRequest request, HttpServletResponse response, String payload) {
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro {}", request);
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro {}", response);
        log.debug("[MercadoPagoCheckoutPro] MercadoPagoCheckoutPro {}", payload);

    }

    @Override
    public ConnectorEnum getConnector() {
        return ConnectorEnum.MERCADO_PAGO_CHECKOUT_PRO;
    }
}
