package com.core.webhook.service;

import java.util.Map;

public interface MetadataService {

    Map<String, String> retrieveGatewayMetadata(String connectorName);

    Map<String, String> retrieveGatewayMetadataByUserId(String connectorName, String userId);

}
