package dev.pulceo.prm.model.event;

public enum EventType {
    AZURE_NODE_DEPLOYMENT_STARTED,
    AZURE_NODE_DEPLOYMENT_COMPLETED,
    NODE_CREATED,
    NODE_UPDATED,
    NODE_CPU_RESOURCES_UPDATED,
    NODE_MEMORY_RESOURCES_UPDATED,
    NODE_STORAGE_RESOURCES_UPDATED,
    NODE_NETWORK_RESOURCES_UPDATED,
    LINK_CREATED,
    LINK_DELETED,
    SHUTDOWN;
}
