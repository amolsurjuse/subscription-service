package com.electrahub.subscription.grpc;

import com.electrahub.proto.subscription.v1.SubscriptionUtilizationServiceGrpc;
import com.electrahub.proto.subscription.v1.PreviewSubscriptionUtilizationRequest;
import com.electrahub.proto.subscription.v1.RecordSubscriptionUtilizationRequest;
import com.electrahub.proto.subscription.v1.ListSubscriptionUtilizationsRequest;
import com.electrahub.proto.subscription.v1.SubscriptionUtilizationPreviewResponse;
import com.electrahub.proto.subscription.v1.SubscriptionUtilizationResponse;
import com.electrahub.proto.subscription.v1.SubscriptionUtilizationListResponse;
import com.electrahub.subscription.api.dto.PreviewSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.RecordSubscriptionUtilizationRequest;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationPreviewResponse;
import com.electrahub.subscription.api.dto.SubscriptionUtilizationResponse;
import com.electrahub.subscription.service.SubscriptionPricingService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class SubscriptionUtilizationGrpcService extends SubscriptionUtilizationServiceGrpc.SubscriptionUtilizationServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionUtilizationGrpcService.class);

    private final SubscriptionPricingService subscriptionPricingService;

    public SubscriptionUtilizationGrpcService(SubscriptionPricingService subscriptionPricingService) {
        this.subscriptionPricingService = subscriptionPricingService;
    }

    @Override
    public void previewUtilization(
            PreviewSubscriptionUtilizationRequest request,
            StreamObserver<SubscriptionUtilizationPreviewResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Previewing subscription utilization for allocation: {}", request.getAllocationId());

            com.electrahub.subscription.api.dto.PreviewSubscriptionUtilizationRequest previewRequest =
                    new com.electrahub.subscription.api.dto.PreviewSubscriptionUtilizationRequest(
                            UUID.fromString(request.getAllocationId()),
                            request.getProposedUsage()
                    );

            SubscriptionUtilizationPreviewResponse preview = subscriptionPricingService.preview(previewRequest);
            com.electrahub.proto.subscription.v1.SubscriptionUtilizationPreviewResponse response = convertToPreviewProto(preview);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in previewUtilization", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in previewUtilization", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    @Override
    public void recordUtilization(
            RecordSubscriptionUtilizationRequest request,
            StreamObserver<SubscriptionUtilizationResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Recording subscription utilization for allocation: {}", request.getAllocationId());

            com.electrahub.subscription.api.dto.RecordSubscriptionUtilizationRequest recordRequest =
                    new com.electrahub.subscription.api.dto.RecordSubscriptionUtilizationRequest(
                            UUID.fromString(request.getAllocationId()),
                            request.getAmount()
                    );

            SubscriptionUtilizationResponse utilization = subscriptionPricingService.record(recordRequest);
            com.electrahub.proto.subscription.v1.SubscriptionUtilizationResponse response = convertToUtilizationProto(utilization);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in recordUtilization", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in recordUtilization", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    @Override
    public void listUtilizations(
            ListSubscriptionUtilizationsRequest request,
            StreamObserver<SubscriptionUtilizationListResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Listing subscription utilizations for user: {}", request.getUserId());

            java.util.List<SubscriptionUtilizationResponse> utilizations =
                    subscriptionPricingService.listByUser(UUID.fromString(request.getUserId()));

            SubscriptionUtilizationListResponse response = convertToListProto(utilizations);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in listUtilizations", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in listUtilizations", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    private com.electrahub.proto.subscription.v1.SubscriptionUtilizationPreviewResponse convertToPreviewProto(
            SubscriptionUtilizationPreviewResponse preview
    ) {
        return com.electrahub.proto.subscription.v1.SubscriptionUtilizationPreviewResponse.newBuilder()
                .setAllocationId(preview.allocationId().toString())
                .setCurrentUsage(preview.currentUsage())
                .setProposedUsage(preview.proposedUsage())
                .setAllowedUsage(preview.allowedUsage())
                .setWillExceed(preview.willExceed())
                .setEstimatedCost(preview.estimatedCost() != null ? preview.estimatedCost().toPlainString() : "")
                .build();
    }

    private com.electrahub.proto.subscription.v1.SubscriptionUtilizationResponse convertToUtilizationProto(
            SubscriptionUtilizationResponse utilization
    ) {
        return com.electrahub.proto.subscription.v1.SubscriptionUtilizationResponse.newBuilder()
                .setUtilizationId(utilization.utilizationId().toString())
                .setAllocationId(utilization.allocationId().toString())
                .setAmount(utilization.amount())
                .setCost(utilization.cost() != null ? utilization.cost().toPlainString() : "")
                .setRecordedAt(utilization.recordedAt() != null ? utilization.recordedAt().toString() : "")
                .build();
    }

    private SubscriptionUtilizationListResponse convertToListProto(
            java.util.List<SubscriptionUtilizationResponse> utilizations
    ) {
        var builder = SubscriptionUtilizationListResponse.newBuilder();

        if (utilizations != null) {
            utilizations.forEach(utilization ->
                    builder.addUtilizations(convertToUtilizationProto(utilization))
            );
        }

        return builder.build();
    }
}
