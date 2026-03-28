package com.electrahub.subscription.grpc;

import com.electrahub.proto.subscription.v1.SubscriptionAuditServiceGrpc;
import com.electrahub.proto.subscription.v1.SearchSubscriptionAuditRequest;
import com.electrahub.proto.subscription.v1.SubscriptionAuditLogResponse;
import com.electrahub.proto.subscription.v1.SubscriptionAuditLogListResponse;
import com.electrahub.subscription.api.dto.SubscriptionAuditLogResponse;
import com.electrahub.subscription.service.SubscriptionAuditQueryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GrpcService
public class SubscriptionAuditGrpcService extends SubscriptionAuditServiceGrpc.SubscriptionAuditServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAuditGrpcService.class);

    private final SubscriptionAuditQueryService subscriptionAuditQueryService;

    public SubscriptionAuditGrpcService(SubscriptionAuditQueryService subscriptionAuditQueryService) {
        this.subscriptionAuditQueryService = subscriptionAuditQueryService;
    }

    @Override
    public void searchAuditLogs(
            SearchSubscriptionAuditRequest request,
            StreamObserver<SubscriptionAuditLogListResponse> responseObserver
    ) {
        try {
            LOGGER.debug("gRPC: Searching subscription audit logs");

            java.util.List<SubscriptionAuditLogResponse> logs = subscriptionAuditQueryService.search(
                    request.getPlanId().isEmpty() ? null : UUID.fromString(request.getPlanId()),
                    request.getAllocationId().isEmpty() ? null : UUID.fromString(request.getAllocationId()),
                    request.getUserId().isEmpty() ? null : UUID.fromString(request.getUserId()),
                    request.getOrganizationId().isEmpty() ? null : UUID.fromString(request.getOrganizationId()),
                    request.getGroupId().isEmpty() ? null : UUID.fromString(request.getGroupId())
            );

            SubscriptionAuditLogListResponse response = convertToListProto(logs);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument in searchAuditLogs", e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asException()
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error in searchAuditLogs", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asException()
            );
        }
    }

    private com.electrahub.proto.subscription.v1.SubscriptionAuditLogResponse convertToProto(
            SubscriptionAuditLogResponse log
    ) {
        var builder = com.electrahub.proto.subscription.v1.SubscriptionAuditLogResponse.newBuilder()
                .setLogId(log.logId().toString())
                .setAction(log.action() != null ? log.action() : "")
                .setTimestamp(log.timestamp() != null ? log.timestamp().toString() : "");

        if (log.planId() != null) {
            builder.setPlanId(log.planId().toString());
        }
        if (log.allocationId() != null) {
            builder.setAllocationId(log.allocationId().toString());
        }
        if (log.userId() != null) {
            builder.setUserId(log.userId().toString());
        }
        if (log.organizationId() != null) {
            builder.setOrganizationId(log.organizationId().toString());
        }
        if (log.groupId() != null) {
            builder.setGroupId(log.groupId().toString());
        }

        return builder.build();
    }

    private SubscriptionAuditLogListResponse convertToListProto(
            java.util.List<SubscriptionAuditLogResponse> logs
    ) {
        var builder = SubscriptionAuditLogListResponse.newBuilder();

        if (logs != null) {
            logs.forEach(log ->
                    builder.addLogs(convertToProto(log))
            );
        }

        return builder.build();
    }
}
