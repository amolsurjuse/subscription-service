package com.electrahub.subscription.grpc;

import com.electrahub.proto.subscription.v1.SubscriptionAllocationServiceGrpc;
import com.electrahub.proto.subscription.v1.CreateSubscriptionAllocationRequest;
import com.electrahub.proto.subscription.v1.ListSubscriptionAllocationsRequest;
import com.electrahub.proto.subscription.v1.UpdateAllocationStatusRequest;
import com.electrahub.proto.subscription.v1.SubscriptionAllocationResponse;
import com.electrahub.proto.subscription.v1.SubscriptionAllocationListResponse;
import com.electrahub.subscription.api.dto.CreateSubscriptionAllocationRequest;
import com.electrahub.subscription.api.dto.SubscriptionAllocationResponse;
import com.electrahub.subscription.api.dto.UpdateAllocationStatusRequest;
import com.electrahub.subscription.service.SubscriptionAllocationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class SubscriptionAllocationGrpcService extends SubscriptionAllocationServiceGrpc.SubscriptionAllocationServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAllocationGrpcService.class);

    private final SubscriptionAllocationService subscriptionAllocationService;

    public SubscriptionAllocationGrpcService(SubscriptionAllocationService subscriptionAllocationService) {
        this.subscriptionAllocationService = subscriptionAllocationService;
    }

    @Override
    public void createAllocation(
            CreateSubscriptionAllocationRequest request,
            StreamObserver<SubscriptionAllocationResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Creating subscription allocation for plan: {}", request.getPlanId());

            com.electrahub.subscription.api.dto.CreateSubscriptionAllocationRequest createRequest =
                    new com.electrahub.subscription.api.dto.CreateSubscriptionAllocationRequest(
                            UUID.fromString(request.getPlanId()),
                            UUID.fromString(request.getUserId()),
                            request.getOrganizationId().isEmpty() ? null : UUID.fromString(request.getOrganizationId()),
                            request.getGroupId().isEmpty() ? null : UUID.fromString(request.getGroupId())
                    );

            SubscriptionAllocationResponse allocation = subscriptionAllocationService.create(createRequest);
            com.electrahub.proto.subscription.v1.SubscriptionAllocationResponse response = convertToProto(allocation);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in createAllocation", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in createAllocation", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    @Override
    public void listAllocations(
            ListSubscriptionAllocationsRequest request,
            StreamObserver<SubscriptionAllocationListResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Listing subscription allocations");

            java.util.List<SubscriptionAllocationResponse> allocations =
                    subscriptionAllocationService.list(
                            request.getUserId().isEmpty() ? null : UUID.fromString(request.getUserId()),
                            request.getOrganizationId().isEmpty() ? null : UUID.fromString(request.getOrganizationId()),
                            request.getGroupId().isEmpty() ? null : UUID.fromString(request.getGroupId()),
                            request.getActiveOnly() ? true : null
                    );

            SubscriptionAllocationListResponse response = convertToListProto(allocations);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in listAllocations", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in listAllocations", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    @Override
    public void updateAllocationStatus(
            UpdateAllocationStatusRequest request,
            StreamObserver<SubscriptionAllocationResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Updating allocation status: {}", request.getAllocationId());

            com.electrahub.subscription.api.dto.UpdateAllocationStatusRequest updateRequest =
                    new com.electrahub.subscription.api.dto.UpdateAllocationStatusRequest(
                            request.getStatus()
                    );

            SubscriptionAllocationResponse allocation = subscriptionAllocationService.updateStatus(
                    UUID.fromString(request.getAllocationId()),
                    updateRequest
            );

            com.electrahub.proto.subscription.v1.SubscriptionAllocationResponse response = convertToProto(allocation);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in updateAllocationStatus", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in updateAllocationStatus", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    private com.electrahub.proto.subscription.v1.SubscriptionAllocationResponse convertToProto(
            SubscriptionAllocationResponse allocation
    ) {
        var builder = com.electrahub.proto.subscription.v1.SubscriptionAllocationResponse.newBuilder()
                .setAllocationId(allocation.allocationId().toString())
                .setPlanId(allocation.planId().toString())
                .setUserId(allocation.userId().toString())
                .setStatus(allocation.status() != null ? allocation.status() : "");

        if (allocation.organizationId() != null) {
            builder.setOrganizationId(allocation.organizationId().toString());
        }
        if (allocation.groupId() != null) {
            builder.setGroupId(allocation.groupId().toString());
        }

        return builder.build();
    }

    private SubscriptionAllocationListResponse convertToListProto(java.util.List<SubscriptionAllocationResponse> allocations) {
        var builder = SubscriptionAllocationListResponse.newBuilder();

        if (allocations != null) {
            allocations.forEach(allocation ->
                    builder.addAllocations(convertToProto(allocation))
            );
        }

        return builder.build();
    }
}
