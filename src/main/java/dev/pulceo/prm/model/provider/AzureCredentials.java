package dev.pulceo.prm.model.provider;

import dev.pulceo.prm.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AzureCredentials extends BaseEntity {

    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String subscriptionId;

}
