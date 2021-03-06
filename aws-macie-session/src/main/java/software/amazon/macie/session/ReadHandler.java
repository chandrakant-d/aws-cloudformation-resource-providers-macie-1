package software.amazon.macie.session;

import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionRequest;
import software.amazon.awssdk.services.macie2.model.GetMacieSessionResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseMacieSessionHandler {
    private static final String OPERATION = "macie2::getMacieSession";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<Macie2Client> client,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        // We use awsAccountId as Macie session primary identifier
        model.setAwsAccountId(request.getAwsAccountId());

        // initiate the call context.
        return proxy.initiate(OPERATION, client, model, callbackContext)
            // transform Resource model properties to getMacieSession API
            .translateToServiceRequest((m) -> GetMacieSessionRequest.builder().build())
            // Make a service call. Handler does not worry about credentials, they are auto injected
            .makeServiceCall((r, c) -> c.injectCredentialsAndInvokeV2(r, c.client()::getMacieSession))
            // return appropriate failed progress event status by mapping business exceptions.
            .handleError((_request, _exception, _client, _model, _context) -> handleError(OPERATION, request, _exception, _model, _context, logger))
            // return success progress event with resource details
            .done(this::buildModelFromResponse);
    }

    private ProgressEvent<ResourceModel, CallbackContext> buildModelFromResponse(GetMacieSessionRequest getMacieSessionRequest,
        GetMacieSessionResponse macieSession,
        ProxyClient<Macie2Client> clientProxyClient, ResourceModel model, CallbackContext callbackContext) {
        model.setStatus(macieSession.statusAsString());
        model.setAwsAccountId(model.getAwsAccountId());
        model.setFindingPublishingFrequency(macieSession.findingPublishingFrequencyAsString());
        model.setServiceRole(macieSession.serviceRole());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
