package com.core.webhook.service.impl;

import com.core.webhook.entity.GatewayMetadataEntity;
import com.core.webhook.repository.MetadataRepository;
import com.core.webhook.service.MetadataService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetadataServiceImpl implements MetadataService {

    private final MetadataRepository metadataRepository;

    public MetadataServiceImpl(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Map<String, String> retrieveGatewayMetadata(String connectorName) {
        return metadataRepository.findByGatewayConnectorName(connectorName)
                .stream()
                .collect(Collectors.toMap(
                        GatewayMetadataEntity::getMetaKey,
                        GatewayMetadataEntity::getMetaValue
                ));
    }
}
