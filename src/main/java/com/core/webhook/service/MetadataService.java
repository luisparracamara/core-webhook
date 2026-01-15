package com.core.webhook.service;

import java.util.Map;

public interface MetadataService {

    Map<String, String> retrieveGatewayMetadata(String connectorName);

}
