# TODOs

- [ ] Ensure that a providerName is unique for all providers
- [ ] Rename `providerService.findProviderMetaDataByName()` to `readProviderMetaDataByName()`
- [ ] Add tests for `findProviderMetaDataByName`
- [ ] Add tests for `readOnPremProviderByProviderMetaData`
- [ ] Add tests for `readOnPremProviderByProviderName`
- [ ] Remove `toString()` annotations from model and DTO classes
- [ ] Implement transaction-like processing of node creation and link creation
- [ ] !!! Review CORS-HEADERS
- [ ] Make sure that differentiation between NodeLink and AbstractLink is clear, remove this because it won't be expanded
- [ ] Add tests for resources / cpus
- [ ] Move to datamodel, this classes are currently not used for interacting with pna src/main/java/dev/pulceo/prm/dto/pna/node/cpu
- [ ] Move to datamodel, this classes are currently not used for interacting with pna src/main/java/dev/pulceo/prm/dto/pna/node/memory
- [ ] !!! Set pnaToken in CloudRegistration back to pnaInitToken, otherwise security issues might occur 